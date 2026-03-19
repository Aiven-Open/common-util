package io.aiven.commons.util.strings;
/*
         Copyright 2025 Aiven Oy and project contributors

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

/**
 * Handles converting from one string case to another (e.g. camel case to snake
 * case).
 * 
 * @since 0.17
 */
public class CasedString {
	/** the string of the cased format. */
	private final String[] parts;
	/** the case of the string. */
	private final StringCase stringCase;

	/**
	 * A method to join pascal string fragments together.
	 */
	private static final Function<String[], String> PASCAL_JOINER = strings -> {
		StringBuilder sb = new StringBuilder();
		Arrays.stream(strings).map(s -> s == null ? "" : s)
				.forEach(token -> sb.append(WordUtils.capitalize(token.toLowerCase(Locale.ROOT))));
		return sb.toString();
	};

	/**
	 * An enumeration of supported string cases. These cases tag strings as having a
	 * specific format.
	 */
	public static class StringCase {
		/**
		 * Camel case tags strings like 'CamelCase' or 'camelCase'. This conversion
		 * forces the first character to lower case. If the first character is desired
		 * to be upper case use PASCAL case instead.
		 */
		public static final StringCase CAMEL = new StringCase("CAMEL", Character::isUpperCase, true,
				PASCAL_JOINER.andThen(WordUtils::uncapitalize), x -> StringUtils.defaultIfEmpty(x, null));
		/**
		 * Camel case tags strings like 'PascalCase' or 'pascalCase'. This conversion
		 * forces the first character to upper case. If the first character is desired
		 * to be lower case use CAMEL case instead.
		 */
		public static final StringCase PASCAL = new StringCase("PASCAL", Character::isUpperCase, true, PASCAL_JOINER,
				x -> StringUtils.defaultIfEmpty(x, null));
		/**
		 * Snake case tags strings like 'Snake_Case'. This conversion does not change
		 * the capitalization of any characters in the string. If specific
		 * capitalization is required use {@link String#toUpperCase()},
		 * {@link String#toLowerCase()}, or the commons-text methods
		 * {@link WordUtils#capitalize(String)}, or
		 * {@link WordUtils#uncapitalize(String)} as required.
		 */
		public static final StringCase SNAKE = new StringCase("SNAKE", '_');
		/**
		 * Kebab case tags strings like 'kebab-case'. This conversion does not change
		 * the capitalization of any characters in the string. If specific
		 * capitalization is required use {@link String#toUpperCase()},
		 * {@link String#toLowerCase()}, * or the commons-text methods
		 * {@link WordUtils#capitalize(String)}, or
		 * {@link WordUtils#uncapitalize(String)} as required.
		 */
		public static final StringCase KEBAB = new StringCase("KEBAB", '-');

		/**
		 * Phrase case tags phrases of words like 'phrase case'. This conversion does
		 * not change the capitalization of any characters in the string. If specific
		 * capitalization is required use {@link String#toUpperCase()},
		 * {@link String#toLowerCase()}, * or the commons-text methods
		 * {@link WordUtils#capitalize(String)}, or
		 * {@link WordUtils#uncapitalize(String)} as required.
		 */
		public static final StringCase PHRASE = new StringCase("PHRASE", Character::isWhitespace, false,
				simpleJoiner(' '));

		/**
		 * Dot case tags phrases of words like 'phrase.case'. This conversion does not
		 * change the capitalization of any characters in the string. If specific
		 * capitalization is required use {@link String#toUpperCase()},
		 * {@link String#toLowerCase()}, * or the commons-text methods
		 * {@link WordUtils#capitalize(String)}, or
		 * {@link WordUtils#uncapitalize(String)} as required.
		 */
		public static final StringCase DOT = new StringCase("DOT", '.');

		/**
		 * Slash case tags phrases of words like 'phrase.case'. This conversion does not
		 * change the capitalization of any characters in the string. If specific
		 * capitalization is required use {@link String#toUpperCase()},
		 * {@link String#toLowerCase()}, * or the commons-text methods
		 * {@link WordUtils#capitalize(String)}, or
		 * {@link WordUtils#uncapitalize(String)} as required.
		 */
		public static final StringCase SLASH = new StringCase("SLASH", '/');

		/** The segment value for a null string */
		private static final String[] NULL_SEGMENT = new String[0];
		/** The segment value for an empty string */
		private static final String[] EMPTY_SEGMENT = {""};

		/** The name of the case */
		private final String name;
		/** test for split position character. */
		private final Predicate<Character> splitter;
		/**
		 * if {@code true} split position character will be preserved in following
		 * segment.
		 */
		private final boolean preserveSplit;
		/** a function to joining the segments into this case type. */
		private final Function<String[], String> joiner;
		/** A function to do post processing on the parsed input */
		private final Function<String, String> postProcess;

		/**
		 * Defines a String Case.
		 *
		 * @param name
		 *            The name of the string case
		 * @param splitter
		 *            The predicate that determines when a new word in the cased string
		 *            begins.
		 * @param preserveSplit
		 *            if {@code true} the character that the splitter detected is
		 *            preserved as the first character of the new word.
		 * @param joiner
		 *            The function to merge a list of strings into the cased String.
		 */
		public StringCase(final String name, final Predicate<Character> splitter, final boolean preserveSplit,
				final Function<String[], String> joiner) {
			this(name, splitter, preserveSplit, joiner, Function.identity());
		}

		/**
		 * Create a simple string case for the delimiter. Items will be split on the
		 * delimiter. Arrays of strings will be joined by the delimiter, null strings
		 * will be discarded.
		 * 
		 * @param name
		 *            the name of the case.
		 * @param delimiter
		 *            the delimiter
		 */
		public StringCase(final String name, char delimiter) {
			this(name, c -> c == delimiter, false, simpleJoiner(delimiter));
		}

		/**
		 * Creates a function to join a String array with a character delimiter. Null
		 * strings are discarded. Empty strings will be processed and result in leading
		 * or trailing delimiters (if at the start or end of the array) or extra
		 * delimiters in other positions.
		 * 
		 * @param delimiter
		 *            the character to join the string array with.
		 * @return the function to perform the array join.
		 */
		public static Function<String[], String> simpleJoiner(char delimiter) {
			return s -> String.join(String.valueOf(delimiter),
					Arrays.stream(s).filter(Objects::nonNull).toArray(String[]::new));
		}

		/**
		 * Defines a String Case.
		 *
		 * @param name
		 *            The name of the string case
		 * @param splitter
		 *            The predicate that determines when a new word in the cased string
		 *            begins.
		 * @param preserveSplit
		 *            if {@code true} the character that the splitter detected is
		 *            preserved as the first character of the new word.
		 * @param joiner
		 *            The function to merge a list of strings into the cased String.
		 * @param postProcess
		 *            A function to perform post split processing on the generated
		 *            String array.
		 */
		public StringCase(final String name, final Predicate<Character> splitter, final boolean preserveSplit,
				final Function<String[], String> joiner, Function<String, String> postProcess) {
			this.name = name;
			this.splitter = splitter;
			this.preserveSplit = preserveSplit;
			this.joiner = joiner;
			this.postProcess = postProcess;
		}

		@Override
		public String toString() {
			return name;
		}
		/**
		 * Creates a cased string from a collection of segments.
		 * 
		 * @param segments
		 *            the segments to create the CasedString from.
		 * @return a CasedString
		 */
		public String assemble(final String[] segments) {
			return this.joiner.apply(segments);
		}

		/**
		 * Returns an array of each of the segments in this CasedString. Segments are
		 * defined as the strings between the separators in the CasedString. For the
		 * CAMEL case the segments are determined by the presence of a capital letter.
		 * 
		 * @param string
		 *            The string to parse into segments.
		 * @return the array of Strings that are segments of the cased string.
		 */
		public String[] getSegments(final String string) {
			if (string == null) {
				return NULL_SEGMENT;
			}
			if (string.isEmpty()) {
				return EMPTY_SEGMENT;
			}
			List<String> lst = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (char c : string.toCharArray()) {
				if (splitter.test(c)) {
					lst.add(sb.toString());
					sb.setLength(0);
					if (preserveSplit) {
						sb.append(c);
					}
				} else {
					sb.append(c);
				}
			}
			if (!sb.isEmpty()) {
				lst.add(sb.toString());
			}
			return lst.stream().map(postProcess).filter(Objects::nonNull).toArray(String[]::new);
		}
	}

	/**
	 * A representation of a cased string and the identified case of that string.
	 * 
	 * @param stringCase
	 *            The {@code StringCase} that the {@code string} argument is in.
	 * @param string
	 *            The string.
	 */
	public CasedString(final StringCase stringCase, final String string) {
		this.parts = string == null ? StringCase.NULL_SEGMENT : stringCase.getSegments(string.trim());
		this.stringCase = stringCase;
	}

	/**
	 * A representation of a cased string and the identified case of that string.
	 * 
	 * @param stringCase
	 *            The {@code StringCase} that the {@code string} argument is in.
	 * @param parts
	 *            The string parts.
	 */
	public CasedString(final StringCase stringCase, final String[] parts) {
		this.parts = parts;
		this.stringCase = stringCase;
	}

	/**
	 * Creates a new cased string from this one but with the new case. If the
	 * {@code stringCase} is the same as the current case this object returned.
	 * 
	 * @param stringCase
	 *            the case to convert this CasedString to.
	 * @return a CasedString with the specified string case.
	 */
	public CasedString as(final StringCase stringCase) {
		if (stringCase.name.equals(this.stringCase.name)) {
			return this;
		}
		return new CasedString(stringCase, Arrays.copyOf(this.parts, this.parts.length));
	}

	/**
	 * Returns an array of each of the segments in this CasedString. Segments are
	 * defined as the strings between the separators in the CasedString. For the
	 * CAMEL case the segments are determined by the presence of a capital letter.
	 * 
	 * @return the array of Strings that are segments of the cased string.
	 */
	public String[] getSegments() {
		return parts;
	}

	/**
	 * Converts this cased string into a {@code String} of another format. The
	 * upper/lower case of the characters within the string are not modified.
	 * 
	 * @param stringCase
	 *            The format to convert to.
	 * @return the String current string represented in the new format.
	 */
	public String toCase(final StringCase stringCase) {
		return parts == StringCase.NULL_SEGMENT ? null : stringCase.joiner.apply(getSegments());
	}

	/**
	 * Returns the string representation provided in the constructor.
	 * 
	 * @return the string representation.
	 */
	@Override
	public String toString() {
		return toCase(stringCase);
	}
}