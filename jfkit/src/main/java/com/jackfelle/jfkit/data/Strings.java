//
//	The MIT License (MIT)
//
//	Copyright © 2017-2024 Jacopo Filié
//
//	Permission is hereby granted, free of charge, to any person obtaining a copy
//	of this software and associated documentation files (the "Software"), to deal
//	in the Software without restriction, including without limitation the rights
//	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//	copies of the Software, and to permit persons to whom the Software is
//	furnished to do so, subject to the following conditions:
//
//	The above copyright notice and this permission notice shall be included in all
//	copies or substantial portions of the Software.
//
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//	SOFTWARE.
//

package com.jackfelle.jfkit.data;

import android.os.Build;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Strings
{
	public static @Nullable String capitalizeString(@Nullable String string) {
		return Strings.capitalizeString(string, null);
	}
	
	public static @Nullable String capitalizeString(@Nullable String string, @Nullable Locale locale) {
		if((string == null) || string.isEmpty()) {
			return string;
		}
		
		if(locale == null) {
			locale = Locale.getDefault();
		}
		
		StringBuilder builder = new StringBuilder(string.substring(0, 1).toUpperCase(locale));
		if(string.length() > 1) {
			builder.append(string.substring(1).toLowerCase(locale));
		}
		return builder.toString();
	}
	
	public static long getReadingTimeMillis(@Nullable String text, @NonNull ReadingMode mode) {
		if(TextUtils.isEmpty(text)) {
			return 0;
		}
		
		long words = new StringTokenizer(text).countTokens();
		return TimeUnit.SECONDS.toMillis(words * 60 / mode.wordsPerMinute);
	}
	
	public static boolean isEmptyString(@Nullable String string) {
		return ((string != null) && string.isEmpty());
	}
	
	public static boolean isStringMadeOfCharacters(@NonNull String string, @NonNull String characters) {
		Set<Character> charactersSet = Strings.newCharacterSetWithCharactersInString(characters);
		Set<Character> stringSet = Strings.newCharacterSetWithCharactersInString(string);
		return ((charactersSet != null) && (stringSet != null) && charactersSet.containsAll(stringSet));
	}
	
	public static boolean isNullOrEmptyString(@Nullable String string) {
		return ((string == null) || string.isEmpty());
	}
	
	public static @Nullable String lowercaseString(@Nullable String string) {
		return (string == null) ? null : string.toLowerCase();
	}
	
	public static @Nullable String lowercaseString(@Nullable String string, @NonNull Locale locale) {
		return (string == null) ? null : string.toLowerCase(locale);
	}
	
	public static @Nullable Set<Character> newCharacterSetWithCharactersInString(@Nullable String string) {
		if(string == null) {
			return null;
		}
		
		Set<Character> retObj = new HashSet<>();
		for(char character : string.toCharArray()) {
			retObj.add(character);
		}
		return retObj;
	}
	
	public static @NonNull String newStringByDeletingPathExtension(@NonNull String path) {
		int index = path.lastIndexOf(".");
		return (index == -1) ? path : path.substring(0, index);
	}
	
	public static @NonNull String newStringByJoiningElements(@NonNull CharSequence delimiter, @NonNull CharSequence... elements) {
		return Strings.newStringByJoiningElements(delimiter, Arrays.asList(elements));
	}
	
	public static @NonNull String newStringByJoiningElements(@NonNull CharSequence delimiter, @NonNull Iterable<? extends CharSequence> elements) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return String.join(delimiter, elements);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			StringJoiner joiner = new StringJoiner(delimiter);
			for(CharSequence element : elements) {
				joiner.add(element);
			}
			return joiner.toString();
		}
		
		StringBuilder retObj = new StringBuilder();
		boolean isFirst = true;
		for(CharSequence element : elements) {
			if(isFirst) {
				isFirst = false;
			} else {
				retObj.append(delimiter);
			}
			retObj.append(element);
		}
		return retObj.toString();
	}
	
	public static @NonNull String newStringByReplacingKeysInFormat(@NonNull String format, Map<String, String> values) {
		int length = format.length();
		if((length == 0) || (values.isEmpty())) {
			return format;
		}
		
		StringBuilder builder = new StringBuilder(format.length());
		
		int formatIndex = 0;
		while(formatIndex != length) {
			int index = format.indexOf("%", formatIndex);
			if(index > -1) {
				builder.append(format.substring(formatIndex, index));
				formatIndex = index;
			} else {
				builder.append(format.substring(formatIndex));
				break;
			}
			
			boolean isFalsePositive = true;
			
			for(String key : values.keySet()) {
				if(!format.startsWith(key, formatIndex)) {
					continue;
				}
				
				isFalsePositive = false;
				builder.append(values.get(key));
				formatIndex += key.length();
			}
			
			if(isFalsePositive) {
				builder.append("%");
				formatIndex++;
			}
		}
		
		return builder.toString();
	}
	
	public static @Nullable String newStringFromPersonName(@Nullable String firstName, @Nullable String middleName, @Nullable String lastName) {
		firstName = ((firstName != null) ? firstName.trim() : null);
		middleName = ((middleName != null) ? middleName.trim() : null);
		lastName = ((lastName != null) ? lastName.trim() : null);
		
		boolean isFirstNameValid = !Strings.isNullOrEmptyString(firstName);
		boolean isMiddleNameValid = !Strings.isNullOrEmptyString(middleName);
		boolean isLastNameValid = !Strings.isNullOrEmptyString(lastName);
		
		if(!isFirstNameValid && !isMiddleNameValid && !isLastNameValid) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		
		if(isFirstNameValid) {
			builder.append(firstName);
		}
		
		if(isMiddleNameValid) {
			if(isFirstNameValid) {
				builder.append(" ");
			}
			builder.append(middleName);
		}
		
		if(isLastNameValid) {
			if(isFirstNameValid || isMiddleNameValid) {
				builder.append(" ");
			}
			builder.append(lastName);
		}
		
		return builder.toString();
	}
	
	public static @Nullable String uppercaseString(@Nullable String string) {
		return (string == null) ? null : string.toUpperCase();
	}
	
	public static @Nullable String uppercaseString(@Nullable String string, @NonNull Locale locale) {
		return (string == null) ? null : string.toUpperCase(locale);
	}
}
