//
//	The MIT License (MIT)
//
//	Copyright © 2020-2023 Jacopo Filié
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

public class ParameterizedLazy <T, P>
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private final @NonNull Implementation<T, P> implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public @NonNull T get(@NonNull P param) {
		return this.implementation.get(param);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	public static <T, P> ParameterizedLazy<T, P> newInstance(@NonNull Builder<T, P> builder) {
		return new ParameterizedLazy<>(new Implementation<>(builder));
	}
	
	public static <T, P> ParameterizedLazy<T, P> newSynchronizedInstance(@NonNull Builder<T, P> builder) {
		return new ParameterizedLazy<>(new SynchronizedImplementation<>(builder));
	}
	
	private ParameterizedLazy(@NonNull Implementation<T, P> implementation) {
		this.implementation = implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	@FunctionalInterface
	public interface Builder <T, P>
	{
		@NonNull T build(@NonNull P param);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class Implementation <T, P>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		private final @NonNull Builder<T, P> builder;
		protected @Nullable T object;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		public @NonNull T get(@NonNull P param) {
			T retObj = this.object;
			if(retObj == null) {
				retObj = this.builder.build(param);
				this.object = retObj;
			}
			return retObj;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public Implementation(@NonNull Builder<T, P> builder) {
			this.builder = builder;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T, P> extends Implementation<T, P>
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors)
		
		@Override public @NonNull T get(@NonNull P param) {
			synchronized(this) {
				return super.get(param);
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		public SynchronizedImplementation(@NonNull Builder<T, P> builder) {
			super(builder);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
