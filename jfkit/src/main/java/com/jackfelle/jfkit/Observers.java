//
//	The MIT License (MIT)
//
//	Copyright © 2017-2024 Jacopo Filié
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

import com.jackfelle.jfkit.functions.Consumer;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Observers <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private final @NonNull AtomicBoolean cleanerSwitch;
	private final @NonNull ReferenceQueue<T> queue;
	private final @NonNull List<Reference<T>> references;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public int size() {
		List<Reference<T>> references = this.references;
		synchronized(references) {
			return references.size();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	public Observers() {
		AtomicBoolean cleanerSwitch = new AtomicBoolean(true);
		ReferenceQueue<T> queue = new ReferenceQueue<>();
		List<Reference<T>> references = new ArrayList<>();
		
		this.cleanerSwitch = cleanerSwitch;
		this.queue = queue;
		this.references = references;
		
		Thread cleaner = new Thread(() -> {
			while(cleanerSwitch.get()) {
				try {
					Reference<? extends T> reference = queue.remove();
					synchronized(references) {
						references.remove(reference);
					}
				} catch(InterruptedException exception) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		});
		cleaner.setDaemon(true);
		cleaner.start();
	}
	
	@Override protected void finalize() throws Throwable {
		this.cleanerSwitch.set(false);
		super.finalize();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Notification
	
	public void notify(@NonNull Consumer<T> consumer) {
		this.forEach(consumer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Registration
	
	public void add(@NonNull T observer) {
		this.edit(references -> {
			if(this.seek(references, observer) == null) {
				references.add(new WeakReference<>(observer, this.queue));
			}
		});
	}
	
	public void remove(@NonNull T observer) {
		this.edit(references -> {
			If.let(this.seek(references, observer), references::remove);
		});
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	private void edit(@NonNull Consumer<List<Reference<T>>> consumer) {
		List<Reference<T>> references = this.references;
		synchronized(references) {
			consumer.consume(references);
		}
	}
	
	private void forEach(@NonNull Consumer<T> consumer) {
		this.edit(references -> {
			for(Reference<T> reference : references) {
				If.let(reference.get(), consumer);
			}
		});
	}
	
	private @Nullable Reference<T> seek(@NonNull List<Reference<T>> references, @NonNull T observer) {
		for(Reference<T> reference : references) {
			if(reference.get() == observer) {
				return reference;
			}
		}
		return null;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
