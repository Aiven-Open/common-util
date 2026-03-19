package io.aiven.commons.util.collections;
/*
         Copyright 2025 Aiven Oy and project contributors

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.

        SPDX-License-Identifier: Apache-2
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of byte prefixes with conversions and formatting.
 */
public enum Scale {
	/**
	 * A byte.
	 */
	B(1) {
		@Override
		public String format(final long byteCount) {
			return String.format("%s %s", byteCount, this.name());
		}
	},
	/**
	 * SI scale Kilobytes bytes
	 */
	KB(1000),
	/**
	 * SI scale Megabytes
	 */
	MB(KB.bytes * KB.bytes),
	/**
	 * SI Scale Gigabttes
	 */
	GB(MB.bytes * KB.bytes),
	/**
	 * SI Scale Terabytes
	 */
	TB(GB.bytes * KB.bytes),
	/**
	 * SI Scale Petabytes
	 */
	PB(TB.bytes * KB.bytes),
	/**
	 * IEC Scale Kilobytes (2 ^ 10).
	 */
	KiB(1024),
	/**
	 * IEC Scale Megabytes (2 ^ 20)
	 */
	MiB(KiB.bytes * KiB.bytes),
	/**
	 * IEC Scale Gigabytes (2 ^ 30)
	 */
	GiB(MiB.bytes * KiB.bytes),
	/**
	 * IEC Scale Terabytes (2 ^ 40)
	 */
	TiB(GiB.bytes * KiB.bytes),
	/**
	 * IEC Scale Petabytes (2 ^ 50)
	 */
	PiB(TiB.bytes * KiB.bytes);

	/**
	 * The International Electrotechnical Commission (IEC) standardized binary
	 * prefixes. Developed by the IEC to avoid ambiguity through their similarity to
	 * the standard metric terms. These are based on powers of 2.
	 *
	 * @see <a href='https://www.iec.ch/prefixes-binary-multiples'>IEC prefixes for
	 *      binary multiples</a>.
	 */
	public static final List<Scale> IEC = Arrays.asList(KiB, MiB, GiB, TiB, PiB);

	/**
	 * The SI standardized prefix scales. These are the metric units, as such they
	 * are all powers of 10.
	 */
	public static final List<Scale> SI = Arrays.asList(KB, MB, GB, TB, PB);

	/**
	 * The format used to output the values.
	 */
	final DecimalFormat dec = new DecimalFormat("0.0 ");

	/**
	 * The number of bytes in a single unit of the scale.
	 */
	public final long bytes;

	/**
	 * Constructor.
	 * 
	 * @param bytes
	 *            the number of bytes in a single unit of the scale.
	 */
	Scale(final long bytes) {
		this.bytes = bytes;
	}

	/**
	 * Formats the {@code byteCount} at this scale.
	 * 
	 * @param byteCount
	 *            the number of bytes.
	 * @return A string representing the number of units that comprise the
	 *         {@code byteCount}.
	 */
	public String format(final long byteCount) {
		return dec.format(byteCount * 1.0 / bytes).concat(this.name());
	}

	/**
	 * Formats the {@code byteCount} at this scale.
	 * 
	 * @param unitCount
	 *            the number of units at this scale.
	 * @return A string representing the number of units at this scale.
	 */
	public String units(final int unitCount) {
		return dec.format(unitCount).concat(this.name());
	}

	/**
	 * Gets the number of bytes found in the specified number of units.
	 * 
	 * @param unitCount
	 *            the number of units.
	 * @return the number of bytes in {@code unitCount} units of this scale.
	 */
	public long asBytes(final double unitCount) {
		return (long) unitCount * bytes;
	}

	/**
	 * Determines the scale of the number of bytes. The largest Scale for which
	 * {@code byteCount} represents at least 1 unit is returned. If no scale in the
	 * possible scales matches then bytes are returned. Negative number will return
	 * {@link Scale#B}.
	 * 
	 * @param byteCount
	 *            the number of bytes
	 * @param possibleScales
	 *            the list of possible scales.
	 * @return the first matching scale.
	 */
	public static Scale scaleOf(final long byteCount, final List<Scale> possibleScales) {
		final List<Scale> ordered = new ArrayList<>(possibleScales);
		// sort descending size.
		ordered.sort((a, b) -> Long.compare(b.bytes, a.bytes));

		for (Scale scale : ordered) {
			if (scale.bytes <= byteCount) {
				return scale;
			}
		}
		return B;
	}

	/**
	 * Creates a formatted string for the scale that most closely represents the
	 * byteCount.
	 * 
	 * @param byteCount
	 *            the number of bytes.
	 * @param possibleScales
	 *            the possible Scales.
	 * @return a formatted string representation of the {@code byteCount} in the
	 *         best Scale representation.
	 * @see #scaleOf(long, List)
	 */
	public static String size(final int byteCount, final List<Scale> possibleScales) {
		return scaleOf(byteCount, possibleScales).format(byteCount);
	}

	/**
	 * Creates a String using the scale. if the scale is not {@link #B} then it is
	 * followed by the number of bytes within a set of parenthesis.
	 * 
	 * @param value
	 *            the number of bytes.
	 * @return a String using the scale. if the scale is not {@link #B} then it is
	 *         followed by the number of bytes
	 */
	public String displayValue(final long value) {
		return format(value) + (this == B ? "" : " (" + B.format(value) + ")");
	}
}
