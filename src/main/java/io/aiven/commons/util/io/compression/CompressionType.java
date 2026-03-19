/*
         Copyright 2026 Aiven Oy and project contributors

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.

        SPDX-License-Identifier: Apache-2
 */
package io.aiven.commons.util.io.compression;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IOSupplier;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Enumeration of standard compression types along with methods to compress and
 * decompress standard file extensions for the type.
 */
public enum CompressionType {
	/** No compression */
	NONE("", in -> in, out -> out),
	/** GZIP compression */
	GZIP(".gz", GZIPInputStream::new, out -> new GZIPOutputStream(out, true)),
	/** Snappy compression */
	SNAPPY(".snappy", SnappyInputStream::new, SnappyOutputStream::new),
	/** Zstandard compression */
	ZSTD(".zst", ZstdInputStream::new, ZstdOutputStream::new);

	/**
	 * The file extension associated with the compression.
	 */
	private final String extensionStr;
	/**
	 * A function that will return an input stream that decompresses the data in a
	 * provided input stream.
	 */
	private final IOFunction<InputStream, InputStream> decompressor;
	/**
	 * A function that will return an output stream that compresses the data in a
	 * provided output stream.
	 */
	private final IOFunction<OutputStream, OutputStream> compressor;

	/**
	 * Constructor
	 * 
	 * @param extensionStr
	 *            The file extension associated with the compression.
	 * @param decompressor
	 *            A function that will return an input stream that decompresses the
	 *            data in a provided input stream.
	 * @param compressor
	 *            A function that will return an output stream that compresses the
	 *            data in a provided output stream.
	 */
	CompressionType(final String extensionStr, final IOFunction<InputStream, InputStream> decompressor,
			final IOFunction<OutputStream, OutputStream> compressor) {
		this.extensionStr = extensionStr;
		this.decompressor = decompressor;
		this.compressor = compressor;
	}

	/**
	 * Gets the compression type for the specified name.
	 *
	 * @param name
	 *            the name to lookup
	 * @throws IllegalArgumentException
	 *             if the name is unknown.
	 * @return the Compression type.
	 */
	public static CompressionType forName(final String name) {
		Objects.requireNonNull(name, "name cannot be null");
		try {
			return CompressionType.valueOf(name.toUpperCase(Locale.ROOT));
		} catch (final IllegalArgumentException ignored) {
			throw new IllegalArgumentException("Unknown compression type: " + name);
		}
	}

	/**
	 * Gets the file name extension associated with the compression.
	 *
	 * @return the file name extension.
	 */
	public String extension() {
		return extensionStr;
	}

	/**
	 * Decompresses an input stream. To function correctly the resulting input
	 * stream must be used in a try/catch block. Failure to do so will result in
	 * compression errors. Example:
	 * 
	 * <pre>{@code
	 * try (InputStream inputStream = compressionType.decompress(originalInputStream)) {
	 * 	// read the inputStream.
	 * }
	 * }</pre>
	 *
	 * @param input
	 *            the input stream to read compressed data from.
	 * @return An input stream that returns decompressed data.
	 * @throws IOException
	 *             on error.
	 */
	public InputStream decompress(final InputStream input) throws IOException {
		return decompressor.apply(input);
	}

	/**
	 * Decompresses an input stream wrapped in an IOSupplier.
	 * 
	 * @param input
	 *            the input stream to read compressed data from.
	 * @return An input stream that returns decompressed data.
	 */
	public IOSupplier<InputStream> decompress(final IOSupplier<InputStream> input) {
		return () -> decompress(input.get());
	}

	/**
	 * Decompresses a byte array.
	 * 
	 * @param source
	 *            the compressed byte array.
	 * @return the decompressed byte array.
	 * @throws IOException
	 *             on IO error.
	 */
	public byte[] decompress(final byte[] source) throws IOException {
		final int EOF = -1;
		final byte[] buffer = new byte[source.length];
		final ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
		try (InputStream inputStream = this.decompress(new ByteArrayInputStream(source))) {
			int n;
			while (EOF != (n = inputStream.read(buffer))) {
				finalOutput.write(buffer, 0, n);
			}
		}
		return finalOutput.toByteArray();
	}

	/**
	 * Compresses an output stream. To function correctly the resulting output
	 * stream * must be used in a try/catch block. Failure to do so will result in
	 * compression errors. * Example: *
	 * 
	 * <pre>{@code
	 * 	 * try (OutputStream outputStream = compressionType.decompress(finalOutputStream)) {
	 * 	 *     // write to the outputStream.
	 * 	 * }
	 * 	 * }</pre>
	 * 
	 * *
	 *
	 * @param output
	 *            the output stream to write compressed data to.
	 * @return An output stream that writes compressed data.
	 * @throws IOException
	 *             on error.
	 */
	public OutputStream compress(final OutputStream output) throws IOException {
		return compressor.apply(output);
	}

	/**
	 * Compresses a byte array
	 * 
	 * @param source
	 *            the byte array to compress.
	 * @return the compressed byte array.
	 * @throws IOException
	 *             on IO error.
	 */
	public byte[] compress(final byte[] source) throws IOException {
		final ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
		try (OutputStream outputStream = this.compress(finalOutput)) {
			outputStream.write(source);
		}
		return finalOutput.toByteArray();
	}
}
