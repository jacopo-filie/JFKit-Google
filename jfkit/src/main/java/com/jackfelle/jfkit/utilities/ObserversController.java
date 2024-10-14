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

package com.jackfelle.jfkit.utilities;

import com.jackfelle.jfkit.core.operations.BlockOperation;
import com.jackfelle.jfkit.core.operations.Operation;
import com.jackfelle.jfkit.core.operations.OperationQueue;
import com.jackfelle.jfkit.data.Blocks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ObserversController <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private boolean needsCleanUp;
	private final @NonNull OperationQueue queue;
	private final @NonNull List<WeakReference<T>> references;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public int getCount() {
		List<WeakReference<T>> references = this.getReferences();
		synchronized(references) {
			int retVal = 0;
			for(WeakReference<T> reference : references) {
				if(reference.get() != null) {
					retVal++;
				} else {
					this.setNeedsCleanUp(true);
				}
			}
			return retVal;
		}
	}
	
	protected boolean needsCleanUp() {
		return this.needsCleanUp;
	}
	
	protected void setNeedsCleanUp(boolean needsCleanUp) {
		if(needsCleanUp) {
			if(this.needsCleanUp) {
				return;
			}
			
			this.needsCleanUp = true;
			
			WeakReference<ObserversController<T>> weakThis = new WeakReference<>(this);
			Operation operation = new BlockOperation(() -> {
				ObserversController<T> strongThis = weakThis.get();
				if(strongThis != null) {
					strongThis.cleanUpIfNeeded();
				}
			});
			operation.setQueuePriority(Operation.QueuePriority.VERY_LOW);
			this.getQueue().addOperation(operation);
		} else {
			this.needsCleanUp = false;
		}
	}
	
	private @NonNull List<T> getObservers() {
		List<WeakReference<T>> references = this.getReferences();
		List<T> retObj = new ArrayList<>(references.size());
		synchronized(references) {
			for(WeakReference<T> reference : references) {
				T observer = reference.get();
				if(observer == null) {
					this.setNeedsCleanUp(true);
				} else {
					retObj.add(observer);
				}
			}
		}
		return retObj;
	}
	
	private @NonNull OperationQueue getQueue() {
		return this.queue;
	}
	
	private @NonNull List<WeakReference<T>> getReferences() {
		return this.references;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public ObserversController() {
		super();
		
		this.needsCleanUp = false;
		this.queue = OperationQueue.newConcurrentQueue(this.getClass().getSimpleName());
		this.references = new ArrayList<>();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Notifications
	
	public void notifyObservers(@NonNull NotificationBlock<T> notificationBlock) {
		this.notifyObservers(notificationBlock, true);
	}
	
	public void notifyObservers(@NonNull NotificationBlock<T> notificationBlock, boolean async) {
		if(async) {
			this.notifyObservers(this.getQueue(), notificationBlock, false);
			return;
		}
		
		for(T observer : this.getObservers()) {
			notificationBlock.execute(observer);
		}
	}
	
	public void notifyObservers(@NonNull OperationQueue queue, @NonNull NotificationBlock<T> notificationBlock, boolean waitUntilFinished) {
		List<T> observers = this.getObservers();
		List<Blocks.Block> blocks = new ArrayList<>(observers.size());
		
		for(T observer : observers) {
			blocks.add(() -> notificationBlock.execute(observer));
		}
		
		queue.addOperation(new BlockOperation(blocks), waitUntilFinished);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers
	
	public void addObserver(@NonNull T observer) {
		List<WeakReference<T>> references = this.getReferences();
		synchronized(references) {
			WeakReference<T> oldReference = this.getReferenceForObserver(observer);
			if(oldReference == null) {
				references.add(new WeakReference<>(observer));
			}
		}
	}
	
	private void cleanUp() {
		List<WeakReference<T>> references = this.getReferences();
		synchronized(references) {
			List<WeakReference<T>> obsolete = new ArrayList<>(references.size());
			for(WeakReference<T> reference : references) {
				if(reference.get() == null) {
					obsolete.add(reference);
				}
			}
			references.removeAll(obsolete);
		}
	}
	
	private void cleanUpIfNeeded() {
		if(this.needsCleanUp()) {
			this.cleanUp();
			this.setNeedsCleanUp(false);
		}
	}
	
	public void removeObserver(@NonNull T observer) {
		List<WeakReference<T>> references = this.getReferences();
		synchronized(references) {
			WeakReference<T> oldReference = this.getReferenceForObserver(observer);
			if(oldReference != null) {
				references.remove(oldReference);
			}
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	protected @Nullable WeakReference<T> getReferenceForObserver(@NonNull T observer) {
		List<WeakReference<T>> references = this.getReferences();
		synchronized(references) {
			boolean needsCleanUp = false;
			WeakReference<T> retObj = null;
			
			for(WeakReference<T> reference : references) {
				T temp = reference.get();
				if(temp == null) {
					needsCleanUp = true;
				}
				
				if(temp == observer) {
					retObj = reference;
					break;
				}
			}
			
			if(needsCleanUp) {
				this.setNeedsCleanUp(true);
			}
			
			return retObj;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface NotificationBlock <T>
	{
		void execute(@NonNull T observer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
