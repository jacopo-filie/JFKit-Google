//
//	The MIT License (MIT)
//
//	Copyright © 2019-2024 Jacopo Filié
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
import android.view.View;

import com.jackfelle.jfkit.utilities.Utilities;

import java.lang.ref.WeakReference;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OptionalView <T extends View>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Fields - Data
	
	private final @NonNull Builder<T> builder;
	private @IdRes final int resourceID;
	private @Nullable WeakReference<T> value;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	public @NonNull T get(@NonNull Context context) {
		T retObj = this.opt();
		if(retObj == null) {
			retObj = this.getBuilder().build(context);
			retObj.setId(this.getResourceID());
			this.value = Utilities.weakWrapObject(retObj);
		}
		return retObj;
	}
	
	private @NonNull Builder<T> getBuilder() {
		return this.builder;
	}
	
	public @IdRes int getResourceID() {
		return this.resourceID;
	}
	
	public @Nullable T opt() {
		return Utilities.unwrapObject(this.value);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public static <T extends View> OptionalView<T> newInstance(@NonNull Builder<T> builder) {
		return OptionalView.newInstance(builder, View.NO_ID);
	}
	
	public static <T extends View> OptionalView<T> newInstance(@NonNull Builder<T> builder, @IdRes int resourceID) {
		return new OptionalView<>(builder, resourceID);
	}
	
	private OptionalView(@NonNull Builder<T> builder, @IdRes int resourceID) {
		super();
		
		this.builder = builder;
		this.resourceID = resourceID;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Builder <T extends View>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		@NonNull T build(@NonNull Context context);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
