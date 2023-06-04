//
//	The MIT License (MIT)
//
//	Copyright © 2019-2023 Jacopo Filié
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class OptionalHook <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private final @NonNull Implementation<T> implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public @Nullable T get() {
		return this.implementation.get();
	}
	
	public void set(@Nullable T object) {
		this.implementation.set(object);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	public static <T> OptionalHook<T> newInstance(@Nullable T object) {
		return new OptionalHook<>(new Implementation<>(object));
	}
	
	public static <T> OptionalHook<T> newSynchronizedInstance(@Nullable T object) {
		return new OptionalHook<>(new SynchronizedImplementation<>(object));
	}
	
	private OptionalHook(@NonNull Implementation<T> implementation) {
		this.implementation = implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class Implementation <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		protected @Nullable T object;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		public @Nullable T get() {
			return this.object;
		}
		
		public void set(@Nullable T object) {
			this.object = object;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public Implementation(@Nullable T object) {
			this.object = object;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T> extends Implementation<T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		@Override public @Nullable T get() {
			synchronized(this) {
				return super.get();
			}
		}
		
		@Override public void set(@Nullable T object) {
			synchronized(this) {
				super.set(object);
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public SynchronizedImplementation(@Nullable T object) {
			super(object);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
