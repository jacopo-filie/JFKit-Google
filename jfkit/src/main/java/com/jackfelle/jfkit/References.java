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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class References
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods
	
	public static @Nullable <T> PhantomReference<T> phantomWrap(@Nullable T referent, @NonNull ReferenceQueue<? super T> queue) {
		return (referent == null) ? null : new PhantomReference<>(referent, queue);
	}
	
	public static @Nullable <T> SoftReference<T> softWrap(@Nullable T referent) {
		return (referent == null) ? null : new SoftReference<>(referent);
	}
	
	public static @Nullable <T> SoftReference<T> softWrap(@Nullable T referent, @Nullable ReferenceQueue<? super T> queue) {
		return (referent == null) ? null : new SoftReference<>(referent, queue);
	}
	
	public static @Nullable <T> WeakReference<T> weakWrap(@Nullable T referent) {
		return (referent == null) ? null : new WeakReference<>(referent);
	}
	
	public static @Nullable <T> WeakReference<T> weakWrap(@Nullable T referent, @Nullable ReferenceQueue<? super T> queue) {
		return (referent == null) ? null : new WeakReference<>(referent, queue);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
