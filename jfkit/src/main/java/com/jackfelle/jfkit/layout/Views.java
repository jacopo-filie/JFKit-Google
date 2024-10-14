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

package com.jackfelle.jfkit.layout;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.jackfelle.jfkit.data.Geometry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Views
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public static void bringViewToFront(@Nullable View view) {
		if(view == null) {
			return;
		}
		
		view.bringToFront();
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return;
		}
		
		ViewParent parent = view.getParent();
		if(parent == null) {
			return;
		}
		
		parent.requestLayout();
		
		if(parent instanceof View) {
			((View)parent).invalidate();
		}
	}
	
	public static void copyViewMargins(@NonNull View source, @NonNull View destination) {
		ViewGroup.LayoutParams layoutParams = destination.getLayoutParams();
		if(!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
			layoutParams = new ViewGroup.MarginLayoutParams(source.getWidth(), source.getHeight());
		}
		
		ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)layoutParams;
		
		int bottom = source.getBottom();
		int left = source.getLeft();
		int right = source.getRight();
		int top = source.getTop();
		
		if((marginLayoutParams.bottomMargin == bottom) && (marginLayoutParams.leftMargin == left) && (marginLayoutParams.rightMargin == right) && (marginLayoutParams.topMargin == top)) {
			return;
		}
		
		marginLayoutParams.setMargins(left, top, right, bottom);
		destination.setLayoutParams(marginLayoutParams);
	}
	
	public static void copyViewMarginsAndPaddings(@NonNull View source, @NonNull View destination) {
		Views.copyViewMargins(source, destination);
		Views.copyViewPaddings(source, destination);
	}
	
	public static void copyViewPaddings(@NonNull View source, @NonNull View destination) {
		int left;
		int right;
		
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			left = source.getPaddingLeft();
			right = source.getPaddingRight();
		} else {
			left = source.getPaddingStart();
			right = source.getPaddingEnd();
		}
		
		destination.setPadding(left, source.getPaddingTop(), right, source.getPaddingBottom());
	}
	
	public static float getPixelsFromPoints(@NonNull Context context, float points) {
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
	
	public static float getPointsFromPixels(@NonNull Context context, float pixels) {
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
	
	public static @NonNull Geometry.Rect getViewBounds(@NonNull View view) {
		return Views.getViewBounds(view, true);
	}
	
	public static @NonNull Geometry.Rect getViewBounds(@NonNull View view, boolean convertPixelsToPoints) {
		float paddingBottom = view.getPaddingBottom();
		float paddingLeft = view.getPaddingLeft();
		float paddingRight = view.getPaddingRight();
		float paddingTop = view.getPaddingTop();
		
		float width = view.getWidth();
		float height = view.getHeight();
		
		width -= paddingLeft + paddingRight;
		height -= paddingTop + paddingBottom;
		
		if(convertPixelsToPoints) {
			Context context = view.getContext();
			if(context != null) {
				paddingLeft = Views.getPointsFromPixels(context, paddingLeft);
				paddingTop = Views.getPointsFromPixels(context, paddingTop);
				width = Views.getPointsFromPixels(context, width);
				height = Views.getPointsFromPixels(context, height);
			}
		}
		
		Geometry.Point origin = new Geometry.Point(paddingLeft, paddingTop);
		Geometry.Size size = new Geometry.Size(width, height);
		
		return new Geometry.Rect(origin, size);
	}
	
	public static int getViewVisibilityFromInvisibleBoolean(boolean invisible) {
		return (invisible ? View.INVISIBLE : View.VISIBLE);
	}
	
	public static int getViewVisibilityFromGoneBoolean(boolean gone) {
		return (gone ? View.GONE : View.VISIBLE);
	}
	
	public static void removeViewFromParent(@Nullable View view) {
		if(view == null) {
			return;
		}
		
		ViewParent parent = view.getParent();
		if(parent instanceof ViewGroup) {
			((ViewGroup)parent).removeView(view);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
