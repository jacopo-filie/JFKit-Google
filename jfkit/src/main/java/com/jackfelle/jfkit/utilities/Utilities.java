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

package com.jackfelle.jfkit.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.data.DeepEquality;
import com.jackfelle.jfkit.data.Geometry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Utilities
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Arrays & Lists
	
	public static <T> boolean contains(@Nullable T[] array, @Nullable T value) {
		return Utilities.contains(array, value, Object::equals);
	}
	
	public static <T> boolean contains(@Nullable T[] array, @Nullable T value, @NonNull EqualityChecker<T> checker) {
		if((array != null) && (value != null)) {
			for(T item : array) {
				if(checker.areObjectsEqual(item, value)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static @NonNull <K, V> Map<K, V> filterMap(@NonNull Map<K, V> map, @NonNull CollectionFilter<Map.Entry<K, V>> filter) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return map.entrySet().stream().filter(filter::isAcceptable).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		
		Map<K, V> retObj = new HashMap<>();
		for(Map.Entry<K, V> entry : map.entrySet()) {
			if(filter.isAcceptable(entry)) {
				retObj.put(entry.getKey(), entry.getValue());
			}
		}
		return retObj;
	}
	
	public static @NonNull <T> ArrayList<T> getAsArrayList(@NonNull Collection<T> collection) {
		return (collection instanceof ArrayList) ? (ArrayList<T>)collection : new ArrayList<>(collection);
	}
	
	public static @NonNull <T> LinkedList<T> getAsLinkedList(@NonNull Collection<T> collection) {
		return (collection instanceof LinkedList) ? (LinkedList<T>)collection : new LinkedList<>(collection);
	}
	
	public static @NonNull <K, V> Map<K, V> getAsMap(@NonNull Collection<V> collection, @NonNull MapKeyGetter<K, V> keyGetter) {
		Map<K, V> retObj = new HashMap<>();
		for(V value : collection) {
			retObj.put(keyGetter.getKeyForValue(value), value);
		}
		return retObj;
	}
	
	public static @Nullable <T> T getFirstArrayItem(@Nullable T[] array) {
		return ((array != null) && (array.length > 0)) ? array[0] : null;
	}
	
	public static @Nullable <T> T getFirstListItem(@Nullable List<T> list) {
		return ((list != null) && (!list.isEmpty())) ? list.get(0) : null;
	}
	
	public static @Nullable <T> T getLastArrayItem(@Nullable T[] array) {
		int length = (array == null) ? 0 : array.length;
		return (length > 0) ? array[length - 1] : null;
	}
	
	public static @Nullable <T> T getLastListItem(@Nullable List<T> list) {
		int size = (list == null) ? 0 : list.size();
		return (size > 0) ? list.get(size - 1) : null;
	}
	
	public static boolean isNullOrEmptyList(@Nullable List<?> list) {
		return ((list == null) || (list.isEmpty()));
	}
	
	public static @NonNull <K, V> Map<K, V> newMapWithDefaultValues(@NonNull Collection<K> keys, @NonNull V defVal) {
		Map<K, V> retObj = new HashMap<>();
		for(K key : keys) {
			retObj.put(key, defVal);
		}
		return retObj;
	}
	
	public static <T> boolean replaceItemsInList(@Nullable List<T> list, @Nullable List<T> newItems) {
		if((list == null) || (newItems == null)) {
			return false;
		}
		
		list.clear();
		return list.addAll(newItems);
	}
	
	public static @Nullable <T> T seekListItem(@Nullable List<T> list, @NonNull ValueSeeker<T> seeker) {
		if(list != null) {
			for(T tested : list) {
				if(seeker.isSearchedValue(tested)) {
					return tested;
				}
			}
		}
		return null;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Closable
	
	public static boolean close(@NonNull Closeable closeable, @Nullable Blocks.FailureBlock failureBlock) {
		try {
			closeable.close();
			return true;
		} catch(IOException exception) {
			if(failureBlock != null) {
				failureBlock.execute(exception);
			}
			return false;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Colors
	
	public static @Nullable @ColorInt Integer parseColorHexString(@Nullable String hexString) {
		if(hexString == null) {
			return null;
		}
		
		if(!hexString.startsWith("#")) {
			hexString = "#" + hexString;
		}
		try {
			return Color.parseColor(hexString);
		} catch(IllegalArgumentException exception) {
			return null;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Comparison
	
	public static <T extends DeepEquality> boolean areArraysDeeplyEqual(@Nullable T[] array1, @Nullable T[] array2) {
		// If both are 'null', they are equal.
		if((array1 == null) && (array2 == null)) {
			return true;
		}
		
		// If anyone is still equal to 'null', they can't be equal.
		if((array1 == null) || (array2 == null)) {
			return false;
		}
		
		// If they haven't the same length, they can't be equal.
		int length = array1.length;
		if(length != array2.length) {
			return false;
		}
		
		if(length == 0) {
			return true;
		}
		
		for(int i = 0; i < length; i++) {
			if(!Utilities.areObjectsDeeplyEqual(array1[i], array2[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	public static <T extends DeepEquality> boolean areListsDeeplyEqual(@Nullable List<T> list1, @Nullable List<T> list2) {
		// If both are 'null', they are equal.
		if((list1 == null) && (list2 == null)) {
			return true;
		}
		
		// If anyone is still equal to 'null', they can't be equal.
		if((list1 == null) || (list2 == null)) {
			return false;
		}
		
		// If they haven't the same size, they can't be equal.
		int size = list1.size();
		if(size != list2.size()) {
			return false;
		}
		
		if(size == 0) {
			return true;
		}
		
		for(int i = 0; i < size; i++) {
			if(!Utilities.areObjectsDeeplyEqual(list1.get(i), list2.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static <T extends DeepEquality> boolean areObjectsDeeplyEqual(@Nullable T obj1, @Nullable T obj2) {
		// If both are 'null', they are equal.
		if((obj1 == null) && (obj2 == null)) {
			return true;
		}
		
		// If anyone is still equal to 'null', they can't be equal.
		if((obj1 == null) || (obj2 == null)) {
			return false;
		}
		
		return obj1.deeplyEquals(obj2);
	}
	
	public static boolean areObjectsEqual(@Nullable Object obj1, @Nullable Object obj2) {
		// If both are 'null', they are equal.
		if((obj1 == null) && (obj2 == null)) {
			return true;
		}
		
		// If anyone is still equal to 'null', they can't be equal.
		if((obj1 == null) || (obj2 == null)) {
			return false;
		}
		
		return obj1.equals(obj2);
	}
	
	public static <T extends DeepEquality> boolean areSetsDeeplyEqual(@Nullable Set<T> set1, @Nullable Set<T> set2) {
		// If both are 'null', they are equal.
		if((set1 == null) && (set2 == null)) {
			return true;
		}
		
		// If anyone is still equal to 'null', they can't be equal.
		if((set1 == null) || (set2 == null)) {
			return false;
		}
		
		// If they haven't the same size, they can't be equal.
		int size = set1.size();
		if(size != set2.size()) {
			return false;
		}
		
		if(size == 0) {
			return true;
		}
		
		for(T obj1 : set1) {
			if(!set2.contains(obj1)) {
				return false;
			}
			
			boolean failed = true;
			for(T obj2 : set2) {
				if(Utilities.areObjectsDeeplyEqual(obj1, obj2)) {
					failed = false;
					break;
				}
			}
			if(failed) {
				return false;
			}
		}
		
		return true;
	}
	
	public static int hashCode(@Nullable Object object) {
		return Objects.hashCode(object);
	}
	
	public static int hashCode(@NonNull Object... objects) {
		return Objects.hash(objects);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Copying
	
	public static @NonNull <T extends Parcelable, R extends T> R copy(@NonNull T object, @NonNull Parcelable.Creator<R> creator) {
		Parcel parcel = Parcel.obtain();
		object.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		R retObj = creator.createFromParcel(parcel);
		parcel.recycle();
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Display
	
	public static @Dimension(unit = Dimension.SP) float getFontSizeFromPixels(@NonNull Context context, @Dimension(unit = Dimension.PX) float pixels) {
		Resources resources = context.getResources();
		if(resources == null) {
			return pixels;
		}
		
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		if(displayMetrics == null) {
			return pixels;
		}
		
		return pixels / displayMetrics.scaledDensity;
	}
	
	public static @Dimension(unit = Dimension.SP) float getFontSizeFromPoints(@NonNull Context context, @Dimension(unit = Dimension.DP) float points) {
		Resources resources = context.getResources();
		if(resources == null) {
			return points;
		}
		
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		if(displayMetrics == null) {
			return points;
		}
		
		return points * displayMetrics.density / displayMetrics.scaledDensity;
	}
	
	public static @Dimension(unit = Dimension.PX) float getPixelsFromFontSize(@NonNull Context context, @Dimension(unit = Dimension.SP) float fontSize) {
		Resources resources = context.getResources();
		if(resources == null) {
			return fontSize;
		}
		
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		if(displayMetrics == null) {
			return fontSize;
		}
		
		return fontSize * displayMetrics.scaledDensity;
	}
	
	public static @Dimension(unit = Dimension.PX) float getPixelsFromPoints(@NonNull Context context, @Dimension(unit = Dimension.DP) float points) {
		Resources resources = context.getResources();
		if(resources == null) {
			return points;
		}
		
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		if(displayMetrics == null) {
			return points;
		}
		
		return points * displayMetrics.density;
	}
	
	public static @Dimension(unit = Dimension.DP) float getPointsFromFontSize(@NonNull Context context, @Dimension(unit = Dimension.SP) float fontSize) {
		Resources resources = context.getResources();
		if(resources == null) {
			return fontSize;
		}
		
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		if(displayMetrics == null) {
			return fontSize;
		}
		
		return fontSize * displayMetrics.scaledDensity / displayMetrics.density;
	}
	
	public static @Dimension(unit = Dimension.DP) float getPointsFromPixels(@NonNull Context context, @Dimension(unit = Dimension.PX) float pixels) {
		Resources resources = context.getResources();
		if(resources == null) {
			return pixels;
		}
		
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		if(displayMetrics == null) {
			return pixels;
		}
		
		return pixels / displayMetrics.density;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Drawables
	
	public static @NonNull Geometry.Size getDrawableSize(@Nullable Drawable drawable) {
		if(drawable == null) {
			return Geometry.SIZE_ZERO;
		}
		
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		
		return ((width == -1) || (height == -1)) ? Geometry.SIZE_ZERO : new Geometry.Size(width, height);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Enumerations
	
	public static @NonNull <T extends Enum<T>> EnumSet<T> newEnumSet(@Nullable T[] array, @NonNull Class<T> enumClass) {
		return ((array == null) || (array.length == 0)) ? EnumSet.noneOf(enumClass) : EnumSet.copyOf(Arrays.asList(array));
	}
	
	public static @Nullable <T extends Enum<T>, F> T seekEnumValue(@Nullable T[] values, @NonNull ValueSeeker<T> seeker) {
		if(values != null) {
			for(T tested : values) {
				if(seeker.isSearchedValue(tested)) {
					return tested;
				}
			}
		}
		return null;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Parcelable
	
	public static boolean readBooleanFromParcel(@NonNull Parcel parcel) {
		return (parcel.readInt() != 0);
	}
	
	public static @NonNull Date readDateFromParcel(@NonNull Parcel parcel) {
		return new Date(parcel.readLong());
	}
	
	public static @NonNull <T extends Enum<T>> T readEnumFromParcel(@NonNull Parcel parcel, @NonNull Class<T> tClass) {
		return Objects.requireNonNull(tClass.getEnumConstants())[parcel.readInt()];
	}
	
	public static @NonNull JSONArray readJSONArrayFromParcel(@NonNull Parcel parcel) {
		try {
			return new JSONArray(parcel.readString());
		} catch(JSONException exception) {
			return new JSONArray();
		}
	}
	
	public static @NonNull JSONObject readJSONObjectFromParcel(@NonNull Parcel parcel) {
		String jsonString = parcel.readString();
		if(jsonString == null) {
			return new JSONObject();
		}
		
		try {
			return new JSONObject(jsonString);
		} catch(JSONException exception) {
			return new JSONObject();
		}
	}
	
	public static @Nullable Date readOptionalDateFromParcel(@NonNull Parcel parcel) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> new Date(p.readLong()));
	}
	
	public static @Nullable Integer readOptionalIntegerFromParcel(@NonNull Parcel parcel) {
		return Utilities.readOptionalObjectFromParcel(parcel, Parcel::readInt);
	}
	
	public static @Nullable JSONArray readOptionalJSONArrayFromParcel(@NonNull Parcel parcel) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> {
			try {
				return new JSONArray(p.readString());
			} catch(JSONException exception) {
				return null;
			}
		});
	}
	
	public static @Nullable JSONObject readOptionalJSONObjectFromParcel(@NonNull Parcel parcel) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> {
			String jsonString = p.readString();
			if(jsonString == null) {
				return null;
			}
			
			try {
				return new JSONObject(jsonString);
			} catch(JSONException exception) {
				return null;
			}
		});
	}
	
	public static @Nullable <T> T readOptionalObjectFromParcel(@NonNull Parcel parcel, @NonNull ParcelReader<T> reader) {
		return (Utilities.readBooleanFromParcel(parcel) ? reader.read(parcel) : null);
	}
	
	public static @Nullable String readOptionalStringFromParcel(@NonNull Parcel parcel) {
		return Utilities.readOptionalObjectFromParcel(parcel, Parcel::readString);
	}
	
	public static @Nullable <T extends Parcelable> T[] readOptionalTypedArrayFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> p.createTypedArray(creator));
	}
	
	public static @Nullable <T extends Parcelable> List<T> readOptionalTypedListFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> p.createTypedArrayList(creator));
	}
	
	public static @Nullable <T extends Parcelable> T readOptionalTypedObjectFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> {
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				return creator.createFromParcel(p);
			}
			return p.readTypedObject(creator);
		});
	}
	
	public static @Nullable <T extends Parcelable> Set<T> readOptionalTypedSetFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> {
			List<T> retObj = new ArrayList<>();
			p.readTypedList(retObj, creator);
			return new HashSet<>(retObj);
		});
	}
	
	public static @NonNull <T extends Parcelable> T readTypedObjectFromParcel(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator, @NonNull Getter<T> replacementGetter) {
		T retObj;
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			retObj = creator.createFromParcel(parcel);
		} else {
			retObj = parcel.readTypedObject(creator);
		}
		return ((retObj == null) ? replacementGetter.get() : retObj);
	}
	
	public static @Nullable Uri readOptionalURIFromParcel(@NonNull Parcel parcel) {
		return Utilities.readOptionalObjectFromParcel(parcel, p -> Uri.parse(p.readString()));
	}
	
	public static void writeBooleanToParcel(@NonNull Parcel parcel, boolean value) {
		parcel.writeInt(value ? 1 : 0);
	}
	
	public static void writeDateToParcel(@NonNull Parcel parcel, @NonNull Date date) {
		parcel.writeLong(date.getTime());
	}
	
	public static <T extends Enum<T>> void writeEnumToParcel(@NonNull Parcel parcel, @NonNull T value) {
		parcel.writeInt(value.ordinal());
	}
	
	public static void writeJSONArrayToParcel(@NonNull Parcel parcel, @NonNull JSONArray jsonArray) {
		parcel.writeString(jsonArray.toString());
	}
	
	public static void writeJSONObjectToParcel(@NonNull Parcel parcel, @NonNull JSONObject jsonObject) {
		parcel.writeString(jsonObject.toString());
	}
	
	public static void writeOptionalDateToParcel(@NonNull Parcel parcel, @Nullable Date date) {
		Utilities.writeOptionalObjectToParcel(parcel, date, (p, v) -> p.writeLong(v.getTime()));
	}
	
	public static void writeOptionalIntegerToParcel(@NonNull Parcel parcel, @Nullable Integer value) {
		Utilities.writeOptionalObjectToParcel(parcel, value, Parcel::writeInt);
	}
	
	public static void writeOptionalJSONArrayToParcel(@NonNull Parcel parcel, @Nullable JSONArray jsonArray) {
		Utilities.writeOptionalObjectToParcel(parcel, jsonArray, (p, v) -> p.writeString(v.toString()));
	}
	
	public static void writeOptionalJSONObjectToParcel(@NonNull Parcel parcel, @Nullable JSONObject jsonObject) {
		Utilities.writeOptionalObjectToParcel(parcel, jsonObject, (p, v) -> p.writeString(v.toString()));
	}
	
	public static <T> void writeOptionalObjectToParcel(@NonNull Parcel parcel, @Nullable T object, @NonNull ParcelWriter<T> writer) {
		boolean valid = (object != null);
		Utilities.writeBooleanToParcel(parcel, valid);
		if(valid) {
			writer.write(parcel, object);
		}
	}
	
	public static void writeOptionalStringToParcel(@NonNull Parcel parcel, @Nullable String string) {
		Utilities.writeOptionalObjectToParcel(parcel, string, Parcel::writeString);
	}
	
	public static <T extends Parcelable> void writeOptionalTypedListToParcel(@NonNull Parcel parcel, @Nullable List<T> list) {
		Utilities.writeOptionalObjectToParcel(parcel, list, Parcel::writeTypedList);
	}
	
	public static <T extends Parcelable> void writeOptionalTypedObjectToParcel(@NonNull Parcel parcel, @Nullable T object, int parcelableFlags) {
		Utilities.writeOptionalObjectToParcel(parcel, object, (p, v) -> {
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				v.writeToParcel(p, parcelableFlags);
			} else {
				p.writeTypedObject(v, parcelableFlags);
			}
		});
	}
	
	public static <T extends Parcelable> void writeOptionalTypedSetToParcel(@NonNull Parcel parcel, @Nullable Set<T> set) {
		Utilities.writeOptionalObjectToParcel(parcel, set, (p, v) -> p.writeTypedList(new ArrayList<>(v)));
	}
	
	public static <T extends Parcelable> void writeTypedObjectToParcel(@NonNull Parcel parcel, @NonNull T object, int parcelableFlags) {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			object.writeToParcel(parcel, parcelableFlags);
		} else {
			parcel.writeTypedObject(object, parcelableFlags);
		}
	}
	
	public static void writeOptionalURIToParcel(@NonNull Parcel parcel, @Nullable Uri uri) {
		Utilities.writeOptionalObjectToParcel(parcel, uri, (p, v) -> p.writeString(v.toString()));
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - References
	
	public static @Nullable <T> SoftReference<T> softWrapObject(@Nullable T object) {
		return (object == null) ? null : new SoftReference<>(object);
	}
	
	public static @Nullable <T> T unwrapObject(@Nullable Reference<T> reference) {
		return (reference == null) ? null : reference.get();
	}
	
	public static <T> void unwrapObjectAndExecuteBlock(@Nullable Reference<T> reference, @Nullable Blocks.BlockWithObject<T> block) {
		if(block == null) {
			return;
		}
		
		T object = Utilities.unwrapObject(reference);
		if(object != null) {
			block.execute(object);
		}
	}
	
	public static @Nullable <T> WeakReference<T> weakWrapObject(@Nullable T object) {
		return (object == null) ? null : new WeakReference<>(object);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Selection
	
	public static <T> void ifLet(@Nullable T object, @NonNull IfLetBlock<T> letBlock) {
		Utilities.ifLet(object, letBlock, null);
	}
	
	public static <T> void ifLet(@Nullable T object, @NonNull IfLetBlock<T> letBlock, @Nullable Blocks.Block elseBlock) {
		if(object != null) {
			letBlock.execute(object);
		} else if(elseBlock != null) {
			elseBlock.execute();
		}
	}
	
	public static @Nullable <T, R> R ifLet(@Nullable T object, @NonNull IfLetGetBlock<T, R> letBlock) {
		return Utilities.ifLet(object, letBlock, null);
	}
	
	public static @Nullable <T, R> R ifLet(@Nullable T object, @NonNull IfLetGetBlock<T, R> letBlock, @Nullable IfLetElseBlock<R> elseBlock) {
		if(object != null) {
			return letBlock.execute(object);
		}
		if(elseBlock != null) {
			return elseBlock.execute();
		}
		return null;
	}
	
	public static <T> void ifLetAs(@Nullable Object object, @NonNull Class<T> type, @NonNull IfLetBlock<T> letBlock) {
		Utilities.ifLet(Utilities.filterByType(object, type), letBlock, null);
	}
	
	public static <T> void ifLetAs(@Nullable Object object, @NonNull Class<T> type, @NonNull IfLetBlock<T> letBlock, @Nullable Blocks.Block elseBlock) {
		Utilities.ifLet(Utilities.filterByType(object, type), letBlock, elseBlock);
	}
	
	public static @Nullable <T, R> R ifLetAs(@Nullable Object object, @NonNull Class<T> type, @NonNull IfLetGetBlock<T, R> letBlock) {
		return Utilities.ifLet(Utilities.filterByType(object, type), letBlock, null);
	}
	
	public static @Nullable <T, R> R ifLetAs(@Nullable Object object, @NonNull Class<T> type, @NonNull IfLetGetBlock<T, R> letBlock, @Nullable IfLetElseBlock<R> elseBlock) {
		return Utilities.ifLet(Utilities.filterByType(object, type), letBlock, elseBlock);
	}
	
	public static @NonNull <T> T replaceIfNull(@Nullable T object, @NonNull T replacement) {
		return (object == null) ? replacement : object;
	}
	
	public static @NonNull <T> T replaceIfNull(@Nullable T object, @NonNull Getter<T> replacementGetter) {
		return (object == null) ? replacementGetter.get() : object;
	}
	
	public static @Nullable <T> T tryToReplaceIfNull(@Nullable T object, @Nullable T replacement) {
		return (object == null) ? replacement : object;
	}
	
	public static @Nullable <T> T tryToReplaceIfNull(@Nullable T object, @NonNull OptionalGetter<T> replacementGetter) {
		return (object == null) ? replacementGetter.get() : object;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Types
	
	public static @Nullable <T> T filterByType(@Nullable Object object, @NonNull Class<T> type) {
		return type.isInstance(object) ? type.cast(object) : null;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Enumerations
	
	public enum ComparisonResult
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		ORDERED_ASCENDING(-1),
		ORDERED_SAME(0),
		ORDERED_DESCENDING(1);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		private final int value;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		public int getValue() {
			return this.value;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public static @Nullable ComparisonResult get(int value) {
			return Utilities.seekEnumValue(ComparisonResult.values(), tested -> (tested.getValue() == value));
		}
		
		ComparisonResult(int value) {
			this.value = value;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	@FunctionalInterface
	public interface Comparator <T>
	{
		@NonNull ComparisonResult compare(@NonNull T first, @NonNull T second);
	}
	
	@FunctionalInterface
	public interface CollectionFilter <T>
	{
		boolean isAcceptable(@NonNull T item);
	}
	
	@FunctionalInterface
	public interface EqualityChecker <T>
	{
		boolean areObjectsEqual(@NonNull T first, @NonNull T second);
	}
	
	@FunctionalInterface
	public interface Getter <T>
	{
		@NonNull T get();
	}
	
	@FunctionalInterface
	public interface IfLetBlock <T>
	{
		void execute(@NonNull T object);
	}
	
	@FunctionalInterface
	public interface IfLetElseBlock <R>
	{
		@Nullable R execute();
	}
	
	@FunctionalInterface
	public interface IfLetGetBlock <T, R>
	{
		@Nullable R execute(@NonNull T object);
	}
	
	@FunctionalInterface
	public interface MapKeyGetter <K, V>
	{
		@NonNull K getKeyForValue(@NonNull V value);
	}
	
	@FunctionalInterface
	public interface ParcelReader <T>
	{
		@Nullable T read(@NonNull Parcel parcel);
	}
	
	@FunctionalInterface
	public interface ParcelWriter <T>
	{
		void write(@NonNull Parcel parcel, @NonNull T value);
	}
	
	@FunctionalInterface
	public interface OptionalGetter <T>
	{
		@Nullable T get();
	}
	
	@FunctionalInterface
	public interface OptionalSetter <T>
	{
		void set(@Nullable T value);
	}
	
	@FunctionalInterface
	public interface Setter <T>
	{
		void set(@NonNull T value);
	}
	
	@FunctionalInterface
	public interface ValueSeeker <T>
	{
		boolean isSearchedValue(@NonNull T value);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
