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

import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Parcels
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Readers
	
	public static boolean readBoolean(@NonNull Parcel parcel) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			return parcel.readBoolean();
		} else {
			return parcel.readInt() != 0;
		}
	}
	
	public static @NonNull Date readDate(@NonNull Parcel parcel) {
		return new Date(parcel.readLong());
	}
	
	public static @NonNull <T extends Enum<T>> T readEnum(@NonNull Parcel parcel, @NonNull Class<T> tClass) {
		return Objects.requireNonNull(tClass.getEnumConstants())[parcel.readInt()];
	}
	
	public static @NonNull JSONArray readJSONArray(@NonNull Parcel parcel) {
		try {
			return new JSONArray(Objects.requireNonNull(parcel.readString()));
		} catch(JSONException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static @NonNull JSONObject readJSONObject(@NonNull Parcel parcel) {
		try {
			return new JSONObject(Objects.requireNonNull(parcel.readString()));
		} catch(JSONException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static @NonNull <T extends Parcelable> T readTypedObject(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return parcel.readTypedObject(creator);
		} else {
			return creator.createFromParcel(parcel);
		}
	}
	
	public static @NonNull <T extends Parcelable> Set<T> readTypedSet(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		List<T> retObj = new ArrayList<>();
		parcel.readTypedList(retObj, creator);
		return new HashSet<>(retObj);
	}
	
	public static @NonNull Uri readURI(@NonNull Parcel parcel) {
		return Uri.parse(parcel.readString());
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Readers (Optionals)
	
	public static @Nullable Boolean readOptionalBoolean(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcels::readBoolean);
	}
	
	public static @Nullable Date readOptionalDate(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcels::readDate);
	}
	
	public static @Nullable Double readOptionalDouble(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcel::readDouble);
	}
	
	public static @Nullable <T extends Enum<T>> T readOptionalEnum(@NonNull Parcel parcel, @NonNull Class<T> tClass) {
		return Parcels.readOptionalObject(parcel, p -> Parcels.readEnum(p, tClass));
	}
	
	public static @Nullable Float readOptionalFloat(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcel::readFloat);
	}
	
	public static @Nullable Integer readOptionalInteger(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcel::readInt);
	}
	
	public static @Nullable JSONArray readOptionalJSONArray(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcels::readJSONArray);
	}
	
	public static @Nullable JSONObject readOptionalJSONObject(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcels::readJSONObject);
	}
	
	public static @Nullable Long readOptionalLong(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcel::readLong);
	}
	
	public static @Nullable <T> T readOptionalObject(@NonNull Parcel parcel, @NonNull Reader<T> reader) {
		return Parcels.readBoolean(parcel) ? reader.read(parcel) : null;
	}
	
	public static @Nullable String readOptionalString(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcel::readString);
	}
	
	public static @Nullable <T extends Parcelable> T[] readOptionalTypedArray(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Parcels.readOptionalObject(parcel, p -> p.createTypedArray(creator));
	}
	
	public static @Nullable <T extends Parcelable> List<T> readOptionalTypedList(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Parcels.readOptionalObject(parcel, p -> p.createTypedArrayList(creator));
	}
	
	public static @Nullable <T extends Parcelable> T readOptionalTypedObject(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Parcels.readOptionalObject(parcel, p -> Parcels.readTypedObject(p, creator));
	}
	
	public static @Nullable <T extends Parcelable> Set<T> readOptionalTypedSet(@NonNull Parcel parcel, @NonNull Parcelable.Creator<T> creator) {
		return Parcels.readOptionalObject(parcel, p -> Parcels.readTypedSet(p, creator));
	}
	
	public static @Nullable Uri readOptionalURI(@NonNull Parcel parcel) {
		return Parcels.readOptionalObject(parcel, Parcels::readURI);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Writers
	
	public static void writeBoolean(@NonNull Parcel parcel, boolean value) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			parcel.writeBoolean(value);
		} else {
			parcel.writeInt(value ? 1 : 0);
		}
	}
	
	public static void writeDate(@NonNull Parcel parcel, @NonNull Date date) {
		parcel.writeLong(date.getTime());
	}
	
	public static <T extends Enum<T>> void writeEnum(@NonNull Parcel parcel, @NonNull T value) {
		parcel.writeInt(value.ordinal());
	}
	
	public static void writeJSONArray(@NonNull Parcel parcel, @NonNull JSONArray jsonArray) {
		parcel.writeString(jsonArray.toString());
	}
	
	public static void writeJSONObject(@NonNull Parcel parcel, @NonNull JSONObject jsonObject) {
		parcel.writeString(jsonObject.toString());
	}
	
	public static <T extends Parcelable> void writeTypedObject(@NonNull Parcel parcel, @NonNull T object, int parcelableFlags) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			parcel.writeTypedObject(object, parcelableFlags);
		} else {
			object.writeToParcel(parcel, parcelableFlags);
		}
	}
	
	public static <T extends Parcelable> void writeTypedSet(@NonNull Parcel parcel, @NonNull Set<T> set, int parcelableFlags) {
		parcel.writeTypedList(new ArrayList<>(set));
	}
	
	public static void writeURI(@NonNull Parcel parcel, @NonNull Uri uri) {
		parcel.writeString(uri.toString());
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Writers (Optionals)
	
	public static void writeOptionalBoolean(@NonNull Parcel parcel, @Nullable Boolean value) {
		Parcels.writeOptionalObject(parcel, value, Parcels::writeBoolean);
	}
	
	public static void writeOptionalDate(@NonNull Parcel parcel, @Nullable Date date) {
		Parcels.writeOptionalObject(parcel, date, Parcels::writeDate);
	}
	
	public static void writeOptionalDouble(@NonNull Parcel parcel, @Nullable Double value) {
		Parcels.writeOptionalObject(parcel, value, Parcel::writeDouble);
	}
	
	public static <T extends Enum<T>> void writeOptionalEnum(@NonNull Parcel parcel, @Nullable T value) {
		Parcels.writeOptionalObject(parcel, value, Parcels::writeEnum);
	}
	
	public static void writeOptionalFloat(@NonNull Parcel parcel, @Nullable Float value) {
		Parcels.writeOptionalObject(parcel, value, Parcel::writeFloat);
	}
	
	public static void writeOptionalInteger(@NonNull Parcel parcel, @Nullable Integer value) {
		Parcels.writeOptionalObject(parcel, value, Parcel::writeInt);
	}
	
	public static void writeOptionalJSONArray(@NonNull Parcel parcel, @Nullable JSONArray jsonArray) {
		Parcels.writeOptionalObject(parcel, jsonArray, (p, v) -> p.writeString(v.toString()));
	}
	
	public static void writeOptionalJSONObject(@NonNull Parcel parcel, @Nullable JSONObject jsonObject) {
		Parcels.writeOptionalObject(parcel, jsonObject, (p, v) -> p.writeString(v.toString()));
	}
	
	public static void writeOptionalLong(@NonNull Parcel parcel, @Nullable Long value) {
		Parcels.writeOptionalObject(parcel, value, Parcel::writeLong);
	}
	
	public static <T> void writeOptionalObject(@NonNull Parcel parcel, @Nullable T object, @NonNull Writer<T> writer) {
		boolean isValid = (object != null);
		Parcels.writeBoolean(parcel, isValid);
		if(isValid) {
			writer.write(parcel, object);
		}
	}
	
	public static void writeOptionalString(@NonNull Parcel parcel, @Nullable String string) {
		Parcels.writeOptionalObject(parcel, string, Parcel::writeString);
	}
	
	public static <T extends Parcelable> void writeOptionalTypedArray(@NonNull Parcel parcel, @Nullable T[] array, int parcelableFlags) {
		Parcels.writeOptionalObject(parcel, array, (p, v) -> p.writeTypedArray(v, parcelableFlags));
	}
	
	public static <T extends Parcelable> void writeOptionalTypedList(@NonNull Parcel parcel, @Nullable List<T> list) {
		Parcels.writeOptionalObject(parcel, list, Parcel::writeTypedList);
	}
	
	public static <T extends Parcelable> void writeOptionalTypedObject(@NonNull Parcel parcel, @Nullable T object, int parcelableFlags) {
		Parcels.writeOptionalObject(parcel, object, (p, v) -> Parcels.writeTypedObject(p, v, parcelableFlags));
	}
	
	public static <T extends Parcelable> void writeOptionalTypedSet(@NonNull Parcel parcel, @Nullable Set<T> set) {
		Parcels.writeOptionalObject(parcel, set, (p, v) -> p.writeTypedList(new ArrayList<>(v)));
	}
	
	public static void writeOptionalURI(@NonNull Parcel parcel, @Nullable Uri uri) {
		Parcels.writeOptionalObject(parcel, uri, Parcels::writeURI);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	@FunctionalInterface
	public interface Reader <T>
	{
		@Nullable T read(@NonNull Parcel parcel);
	}
	
	@FunctionalInterface
	public interface Writer <T>
	{
		void write(@NonNull Parcel parcel, @NonNull T value);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
