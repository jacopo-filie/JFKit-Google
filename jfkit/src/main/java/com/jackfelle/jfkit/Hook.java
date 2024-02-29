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

package com.jackfelle.jfkit;

import androidx.annotation.NonNull;

public final class Hook <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private final @NonNull Implementation<T> implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public @NonNull T get() {
		return this.implementation.get();
	}
	
	public void set(@NonNull T object) {
		this.implementation.set(object);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	public static <T> Hook<T> newInstance(@NonNull T object) {
		return new Hook<>(new Implementation<>(object));
	}
	
	public static <T> Hook<T> newSynchronizedInstance(@NonNull T object) {
		return new Hook<>(new SynchronizedImplementation<>(object));
	}
	
	private Hook(@NonNull Implementation<T> implementation) {
		this.implementation = implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class Implementation <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		protected @NonNull T object;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		public @NonNull T get() {
			return this.object;
		}
		
		public void set(@NonNull T object) {
			this.object = object;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public Implementation(@NonNull T object) {
			this.object = object;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T> extends Implementation<T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		@Override public @NonNull T get() {
			synchronized(this) {
				return super.get();
			}
		}
		
		@Override public void set(@NonNull T object) {
			synchronized(this) {
				super.set(object);
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public SynchronizedImplementation(@NonNull T object) {
			super(object);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
