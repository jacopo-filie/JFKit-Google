//
//	The MIT License (MIT)
//
//	Copyright © 2020-2024 Jacopo Filié
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ParameterizedLazy <T, P>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Fields - Data
	
	private final @NonNull Implementation<T, P> implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	public @NonNull T get(@NonNull P param) {
		return this.getImplementation().getValue(param);
	}
	
	private @NonNull Implementation<T, P> getImplementation() {
		return this.implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public static <T, P> ParameterizedLazy<T, P> newInstance(@NonNull Builder<T, P> builder) {
		return new ParameterizedLazy<>(new Implementation<>(builder));
	}
	
	public static <T, P> ParameterizedLazy<T, P> newSynchronizedInstance(@NonNull Builder<T, P> builder) {
		return new ParameterizedLazy<>(new SynchronizedImplementation<>(builder));
	}
	
	private ParameterizedLazy(@NonNull Implementation<T, P> implementation) {
		super();
		
		this.implementation = implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Builder <T, P>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		@NonNull T build(@NonNull P param);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class Implementation <T, P>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Data
		
		private final @NonNull Builder<T, P> builder;
		protected @Nullable T value;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Data
		
		protected @NonNull Builder<T, P> getBuilder() {
			return this.builder;
		}
		
		public @NonNull T getValue(@NonNull P param) {
			T retObj = this.value;
			if(retObj == null) {
				retObj = this.getBuilder().build(param);
				this.value = retObj;
			}
			return retObj;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public Implementation(@NonNull Builder<T, P> builder) {
			super();
			
			this.builder = builder;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T, P> extends Implementation<T, P>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Data
		
		@Override public @NonNull T getValue(@NonNull P param) {
			T retObj = this.value;
			if(retObj != null) {
				return retObj;
			}
			
			synchronized(this) {
				return super.getValue(param);
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public SynchronizedImplementation(@NonNull Builder<T, P> builder) {
			super(builder);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
