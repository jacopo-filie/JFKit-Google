//
//	The MIT License (MIT)
//
//	Copyright © 2018-2024 Jacopo Filié
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.jackfelle.jfkit.utilities.Utilities;

import java.io.ByteArrayOutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Images
{
	public static @Nullable String newBase64StringFromPNGRepresentationOfImage(@NonNull Bitmap image) {
		return Base64.encodeToString(Images.newPNGRepresentationOfImage(image), Base64.DEFAULT);
	}
	
	public static @Nullable Bitmap newBitmapFromBase64String(@NonNull String base64String) {
		byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
		return (bytes == null) ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}
	
	public static @NonNull Bitmap newBitmapFromDrawable(@NonNull Drawable drawable) {
		return Images.newBitmapFromDrawable(drawable, Bitmap.Config.ARGB_8888);
	}
	
	public static @NonNull Bitmap newBitmapFromDrawable(@NonNull Drawable drawable, @NonNull Bitmap.Config config) {
		BitmapDrawable bitmapDrawable = Utilities.filterByType(drawable, BitmapDrawable.class);
		if(bitmapDrawable != null) {
			Bitmap retObj = bitmapDrawable.getBitmap();
			if(retObj != null) {
				return retObj;
			}
		}
		
		int width = Math.max(drawable.getIntrinsicWidth(), 1);
		int height = Math.max(drawable.getIntrinsicHeight(), 1);
		
		Bitmap retObj = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(retObj);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return retObj;
	}
	
	public static @NonNull byte[] newJPEGRepresentationOfImage(@NonNull Bitmap image, int compressionQuality) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, compressionQuality, stream);
		return stream.toByteArray();
	}
	
	public static @NonNull byte[] newPNGRepresentationOfImage(@NonNull Bitmap image) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 0, stream);
		return stream.toByteArray();
	}
	
	public static @NonNull Bitmap resizeImage(@NonNull Bitmap source, @NonNull Geometry.Size maxSize) {
		return Images.resizeImage(source, maxSize, false);
	}
	
	public static @NonNull Bitmap resizeImage(@NonNull Bitmap source, @NonNull Geometry.Size maxSize, boolean shouldRecycleSourceOnSuccess) {
		float maxWidth = maxSize.getWidth();
		float maxHeight = maxSize.getHeight();
		
		if((maxWidth == 0) || (maxHeight == 0)) {
			return source;
		}
		
		float width = source.getWidth();
		float height = source.getHeight();
		
		if((width <= maxWidth) && (height <= maxHeight)) {
			return source;
		}
		
		float ratio = width / height;
		if(ratio > 1) {
			width = maxWidth;
			height = width / ratio;
		} else {
			height = maxHeight;
			width = height * ratio;
		}
		
		Bitmap retObj = Bitmap.createScaledBitmap(source, (int)width, (int)height, false);
		
		if(shouldRecycleSourceOnSuccess && (retObj != source)) {
			source.recycle();
		}
		
		return retObj;
	}
	
	public static @NonNull Bitmap rotateImage(@NonNull Bitmap source, float rotation) {
		return Images.rotateImage(source, rotation, false);
	}
	
	public static @NonNull Bitmap rotateImage(@NonNull Bitmap source, float rotation, boolean shouldRecycleSourceOnSuccess) {
		if(Float.compare(rotation, 0.0f) == 0) {
			return source;
		}
		
		Matrix matrix = new Matrix();
		matrix.preRotate(rotation);
		
		Bitmap retObj = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
		
		if(shouldRecycleSourceOnSuccess && (retObj != source)) {
			source.recycle();
		}
		
		return retObj;
	}
}
