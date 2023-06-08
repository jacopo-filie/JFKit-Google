//
//	The MIT License (MIT)
//
//	Copyright © 2017-2023 Jacopo Filié
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

package com.jackfelle.jfkit;

import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Texts
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Creation
	
	public static @NonNull Set<Character> newCharacterSetFromText(@NonNull CharSequence text) {
		Set<Character> retObj = new HashSet<>();
		for(char character : text.toString().toCharArray()) {
			retObj.add(character);
		}
		return retObj;
	}
	
	public static @NonNull String newStringByDeletingPathExtension(@NonNull CharSequence path) {
		int index = TextUtils.lastIndexOf(path, '.');
		return (index == -1) ? path.toString() : TextUtils.substring(path, 0, index);
	}
	
	public static @NonNull String newStringByJoiningElements(@NonNull CharSequence delimiter, @NonNull CharSequence... elements) {
		return Texts.newStringByJoiningElements(delimiter, Arrays.asList(elements));
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
	
	public static @NonNull String newStringByReplacingKeysInFormat(@NonNull CharSequence format, Map<String, String> keyValues) {
		int length = format.length();
		if((length == 0) || (keyValues.size() == 0)) {
			return format.toString();
		}
		
		StringBuilder builder = new StringBuilder(format.length());
		
		int formatIndex = 0;
		while(formatIndex != length) {
			int index = TextUtils.indexOf(format, "%", formatIndex);
			if(index > -1) {
				builder.append(TextUtils.substring(format, formatIndex, index));
				formatIndex = index;
			} else {
				builder.append(TextUtils.substring(format, formatIndex, length));
				break;
			}
			
			boolean isFalsePositive = true;
			
			for(String key : keyValues.keySet()) {
				if(!Texts.startsWith(format, key, formatIndex)) {
					continue;
				}
				
				isFalsePositive = false;
				builder.append(keyValues.get(key));
				formatIndex += key.length();
			}
			
			if(isFalsePositive) {
				builder.append("%");
				formatIndex++;
			}
		}
		
		return builder.toString();
	}
	
	public static @Nullable String newStringFromNameComponents(@Nullable CharSequence firstName, @Nullable CharSequence middleName, @Nullable CharSequence lastName) {
		List<String> components = Texts.appendNameComponent(null, firstName);
		components = Texts.appendNameComponent(components, middleName);
		components = Texts.appendNameComponent(components, lastName);
		return (components == null) ? null : Texts.newStringByJoiningElements(" ", components);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Queries
	
	public static boolean isEmpty(@Nullable CharSequence text) {
		return ((text != null) && (text.length() == 0));
	}
	
	public static boolean isMadeOf(@NonNull CharSequence text, @NonNull CharSequence characters) {
		return Texts.newCharacterSetFromText(characters).containsAll(Texts.newCharacterSetFromText(text));
	}
	
	public static boolean isNullOrEmpty(@Nullable CharSequence text) {
		return ((text == null) || (text.length() == 0));
	}
	
	public static boolean startsWith(@NonNull CharSequence text, @NonNull CharSequence prefix) {
		return Texts.startsWith(text, prefix, 0);
	}
	
	public static boolean startsWith(@NonNull CharSequence text, @NonNull CharSequence prefix, int offset) {
		if(offset < 0) {
			return false;
		}
		
		int prefixLength = prefix.length();
		if((offset + prefixLength) > text.length()) {
			return false;
		}
		
		for(int i = 0; i < prefixLength; i++) {
			if(text.charAt(offset + i) != prefix.charAt(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Transformation
	
	public static @NonNull String makeCapitalized(@NonNull CharSequence text) {
		return Texts.makeCapitalized(text, Locale.getDefault());
	}
	
	public static @NonNull String makeCapitalized(@NonNull CharSequence text, @NonNull Locale locale) {
		int length = text.length();
		if(length == 0) {
			return text.toString();
		} else if(length == 1) {
			return text.toString().toUpperCase(locale);
		} else {
			return TextUtils.substring(text, 0, 1).toUpperCase(locale) + TextUtils.substring(text, 1, length).toLowerCase(locale);
		}
	}
	
	public static @Nullable String tryToMakeCapitalized(@Nullable CharSequence text) {
		return Texts.tryToMakeCapitalized(text, Locale.getDefault());
	}
	
	public static @Nullable String tryToMakeCapitalized(@Nullable CharSequence text, @NonNull Locale locale) {
		return (text == null) ? null : Texts.makeCapitalized(text, locale);
	}
	
	public static @Nullable String tryToMakeLowercase(@Nullable CharSequence text) {
		return (text == null) ? null : text.toString().toLowerCase();
	}
	
	public static @Nullable String tryToMakeLowercase(@Nullable CharSequence text, @NonNull Locale locale) {
		return (text == null) ? null : text.toString().toLowerCase(locale);
	}
	
	public static @Nullable String tryToMakeUppercase(@Nullable CharSequence text) {
		return (text == null) ? null : text.toString().toUpperCase();
	}
	
	public static @Nullable String tryToMakeUppercase(@Nullable CharSequence text, @NonNull Locale locale) {
		return (text == null) ? null : text.toString().toUpperCase(locale);
	}
	
	public static @Nullable String tryToTrim(@Nullable CharSequence text) {
		return (text == null) ? null : text.toString().trim();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	private static @Nullable List<String> appendNameComponent(@Nullable List<String> components, @Nullable CharSequence component) {
		String trimmed = Texts.tryToTrim(component);
		if(Texts.isNullOrEmpty(trimmed)) {
			return components;
		}
		
		List<String> retObj = (components == null) ? new ArrayList<>() : components;
		retObj.add(trimmed);
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
