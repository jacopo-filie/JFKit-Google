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

public class Lazy <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private final @NonNull Implementation<T> implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public @NonNull T get() {
		return this.implementation.getValue();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	public static <T> Lazy<T> newInstance(@NonNull Builder<T> builder) {
		return new Lazy<>(new Implementation<>(builder));
	}
	
	public static <T> Lazy<T> newSynchronizedInstance(@NonNull Builder<T> builder) {
		return new Lazy<>(new SynchronizedImplementation<>(builder));
	}
	
	private Lazy(@NonNull Implementation<T> implementation) {
		super();
		this.implementation = implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	@FunctionalInterface
	public interface Builder <T>
	{
		@NonNull T build();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class Implementation <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		private final @NonNull Builder<T> builder;
		protected @Nullable T value;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		protected @NonNull Builder<T> getBuilder() {
			return this.builder;
		}
		
		public @NonNull T getValue() {
			T retObj = this.value;
			if(retObj == null) {
				retObj = this.getBuilder().build();
				this.value = retObj;
			}
			return retObj;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public Implementation(@NonNull Builder<T> builder) {
			super();
			this.builder = builder;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T> extends Implementation<T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		@Override public @NonNull T getValue() {
			T retObj = this.value;
			if(retObj != null) {
				return retObj;
			}
			
			synchronized(this) {
				return super.getValue();
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public SynchronizedImplementation(@NonNull Builder<T> builder) {
			super(builder);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
