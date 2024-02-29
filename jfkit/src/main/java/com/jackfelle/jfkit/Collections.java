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

package com.jackfelle.jfkit;

import android.os.Build;

import com.jackfelle.jfkit.blocks.EqualityChecker;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Collections
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Arrays
	
	public static <T> boolean contains(@Nullable T[] array, @Nullable T value) {
		return Collections.contains(array, value, Object::equals);
	}
	
	public static <T> boolean contains(@Nullable T[] array, @Nullable T value, @NonNull EqualityChecker<T> checker) {
		if((array != null) && (value != null)) {
			for(T item : array) {
				if(checker.areEqual(item, value)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static @Nullable <T> T getFirstItem(@Nullable T[] array) {
		return ((array != null) && (array.length > 0)) ? array[0] : null;
	}
	
	public static @Nullable <T> T getLastItem(@Nullable T[] array) {
		int length = (array == null) ? 0 : array.length;
		return (length > 0) ? array[length - 1] : null;
	}
	
	public static <T> boolean isNullOrEmpty(@Nullable T[] array) {
		return (array == null) || (array.length == 0);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Enumerations
	
	public static @NonNull <T extends Enum<T>> EnumSet<T> newEnumSet(@Nullable T[] array, @NonNull Class<T> enumClass) {
		return ((array == null) || (array.length == 0)) ? EnumSet.noneOf(enumClass) : EnumSet.copyOf(Arrays.asList(array));
	}
	
	public static @Nullable <T extends Enum<T>, F> T seekEnumValue(@Nullable T[] values, @NonNull Filter<T> filter) {
		if(values != null) {
			for(T value : values) {
				if(filter.isValid(value)) {
					return value;
				}
			}
		}
		return null;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Lists
	
	public static @Nullable <T> T findItem(@Nullable List<T> list, @NonNull Filter<T> filter) {
		if(list != null) {
			for(T item : list) {
				if(filter.isValid(item)) {
					return item;
				}
			}
		}
		return null;
	}
	
	public static @Nullable <T> T getFirstItem(@Nullable List<T> list) {
		return ((list != null) && (list.size() > 0)) ? list.get(0) : null;
	}
	
	public static @Nullable <T> T getLastItem(@Nullable List<T> list) {
		int size = (list == null) ? 0 : list.size();
		return (size > 0) ? list.get(size - 1) : null;
	}
	
	public static <T> boolean isNullOrEmpty(@Nullable List<T> list) {
		return (list == null) || (list.size() == 0);
	}
	
	public static <T> boolean replaceItems(@Nullable List<T> list, @Nullable List<T> newItems) {
		if((list == null) || (newItems == null)) {
			return false;
		}
		
		list.clear();
		return list.addAll(newItems);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Maps
	
	public static @NonNull <K, V> Map<K, V> filter(@NonNull Map<K, V> map, @NonNull Filter<Map.Entry<K, V>> filter) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return map.entrySet().stream().filter(filter::isValid).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		
		Map<K, V> retObj = new HashMap<>();
		for(Map.Entry<K, V> entry : map.entrySet()) {
			if(filter.isValid(entry)) {
				retObj.put(entry.getKey(), entry.getValue());
			}
		}
		return retObj;
	}
	
	public static @NonNull <K, V> Map<K, V> newMapFromCollection(@NonNull Collection<V> collection, @NonNull MapKeyGetter<K, V> keyGetter) {
		Map<K, V> retObj = new HashMap<>();
		for(V value : collection) {
			retObj.put(keyGetter.getKey(value), value);
		}
		return retObj;
	}
	
	public static @NonNull <K, V> Map<K, V> newMapWithDefaultValues(@NonNull Collection<K> keys, @NonNull V defVal) {
		Map<K, V> retObj = new HashMap<>();
		for(K key : keys) {
			retObj.put(key, defVal);
		}
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	@FunctionalInterface
	public interface Filter <T>
	{
		boolean isValid(@NonNull T item);
	}
	
	@FunctionalInterface
	public interface MapKeyGetter <K, V>
	{
		@NonNull K getKey(@NonNull V value);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
