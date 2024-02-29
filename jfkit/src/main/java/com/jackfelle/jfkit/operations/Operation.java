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

package com.jackfelle.jfkit.operations;

import com.jackfelle.jfkit.If;
import com.jackfelle.jfkit.Observers;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Operation
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private @Nullable String name;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Execution
	
	private @Nullable Runnable completion;
	private @Nullable Set<Operation> dependencies;
	private @NonNull QueuePriority queuePriority = QueuePriority.NORMAL;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Observers
	
	private @Nullable Observer dependencyObserver;
	private final @NonNull Observers<Observer> observers = new Observers<>();
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Status
	
	private boolean canceled;
	private boolean executing;
	private boolean finished;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Data
	
	public synchronized @Nullable String getName() {
		return this.name;
	}
	
	public synchronized void setName(@Nullable String name) {
		this.name = name;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Execution
	
	public synchronized @Nullable Runnable getCompletion() {
		return this.completion;
	}
	
	public synchronized void setCompletion(@Nullable Runnable completion) {
		this.completion = completion;
	}
	
	public synchronized @NonNull Set<Operation> getDependencies() {
		return If.letGet(this.dependencies, Collections::unmodifiableSet, Collections.emptySet());
	}
	
	private synchronized @NonNull Set<Operation> getWritableDependencies() {
		Set<Operation> retObj = this.dependencies;
		if(retObj == null) {
			retObj = new HashSet<>();
			this.dependencies = retObj;
		}
		return retObj;
	}
	
	private synchronized @Nullable Set<Operation> optDependencies() {
		return this.dependencies;
	}
	
	public synchronized @NonNull QueuePriority getQueuePriority() {
		return this.queuePriority;
	}
	
	public synchronized void setQueuePriority(@NonNull QueuePriority queuePriority) {
		this.queuePriority = queuePriority;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Observers
	
	private synchronized @NonNull Observer getDependencyObserver() {
		Observer retObj = this.dependencyObserver;
		if(retObj == null) {
			retObj = this.newDependencyObserver();
			this.dependencyObserver = retObj;
		}
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Status
	
	public boolean isAsynchronous() {
		return false;
	}
	
	public synchronized boolean isCanceled() {
		return this.canceled;
	}
	
	public synchronized boolean isExecuting() {
		return this.executing;
	}
	
	public synchronized boolean isFinished() {
		return this.finished;
	}
	
	public boolean isReady() {
		synchronized(this) {
			if(this.executing || this.finished) {
				return false;
			}
		}
		
		Set<Operation> dependencies = this.optDependencies();
		if(dependencies == null) {
			return true;
		}
		
		synchronized(dependencies) {
			for(Operation operation : dependencies) {
				if(!operation.isFinished()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Execution
	
	public <T extends Operation> void addDependencies(@NonNull Collection<T> operations) {
		if(operations.size() == 0) {
			return;
		}
		
		Observer observer = this.getDependencyObserver();
		Set<Operation> dependencies = this.getWritableDependencies();
		synchronized(dependencies) {
			dependencies.addAll(operations);
			for(Operation operation : operations) {
				operation.addObserver(observer);
			}
		}
	}
	
	public <T extends Operation> void addDependency(@NonNull T operation) {
		Observer observer = this.getDependencyObserver();
		Set<Operation> dependencies = this.getWritableDependencies();
		synchronized(dependencies) {
			dependencies.add(operation);
			operation.addObserver(observer);
		}
	}
	
	public void cancel() {
		synchronized(this) {
			if(this.canceled) {
				return;
			}
			
			this.canceled = true;
		}
		
		this.observers.notify(observer -> {
			observer.onCanceled(this);
		});
	}
	
	protected void finish() {
		synchronized(this) {
			if(this.finished || (!this.executing && !this.canceled)) {
				return;
			}
			
			this.executing = false;
			this.finished = true;
			
			this.notifyAll();
		}
		
		If.let(this.getCompletion(), Runnable::run);
		
		this.observers.notify(observer -> {
			observer.onFinished(this);
		});
	}
	
	protected abstract void main();
	
	public <T extends Operation> void removeDependencies(@NonNull Collection<T> operations) {
		if(operations.size() == 0) {
			return;
		}
		
		Set<Operation> dependencies = this.optDependencies();
		if(dependencies == null) {
			return;
		}
		
		Observer observer = this.getDependencyObserver();
		synchronized(dependencies) {
			for(Operation operation : operations) {
				operation.removeObserver(observer);
			}
			dependencies.removeAll(operations);
		}
	}
	
	public <T extends Operation> void removeDependency(@NonNull T operation) {
		Set<Operation> dependencies = this.optDependencies();
		if(dependencies == null) {
			return;
		}
		
		Observer observer = this.getDependencyObserver();
		synchronized(dependencies) {
			operation.removeObserver(observer);
			dependencies.remove(operation);
		}
	}
	
	public void start() {
		if(this.isCanceled()) {
			this.finish();
			return;
		}
		
		synchronized(this) {
			if(!this.isReady()) {
				return;
			}
			
			this.executing = true;
		}
		
		this.observers.notify(observer -> {
			observer.onExecuting(this);
		});
		
		this.main();
		
		if(!this.isAsynchronous()) {
			this.finish();
		}
	}
	
	public synchronized void waitUntilFinished() {
		try {
			while(!this.finished) {
				this.wait();
			}
		} catch(InterruptedException exception) {
			// TODO: add log.
			Thread.currentThread().interrupt();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers
	
	public void addObserver(@NonNull Observer observer) {
		this.observers.add(observer);
	}
	
	public void removeObserver(@NonNull Observer observer) {
		this.observers.remove(observer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public static @NonNull List<Operation> chain(@NonNull List<Operation> operations) {
		for(int i = 1; i < operations.size(); i++) {
			operations.get(i).addDependency(operations.get(i - 1));
		}
		return operations;
	}
	
	private @NonNull Observer newDependencyObserver() {
		WeakReference<Operation> weakThis = new WeakReference<>(this);
		return new Observer()
		{
			@Override public void onFinished(@NonNull Operation sender) {
				If.let(weakThis.get(), strongThis -> {
					if(strongThis.isReady()) {
						strongThis.observers.notify(observer -> {
							observer.onReady(strongThis);
						});
					}
				});
			}
		};
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Observer
	{
		default void onCanceled(@NonNull Operation sender) {}
		default void onExecuting(@NonNull Operation sender) {}
		default void onFinished(@NonNull Operation sender) {}
		default void onReady(@NonNull Operation sender) {}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Enumerations
	
	public enum QueuePriority
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		VERY_LOW,
		LOW,
		NORMAL,
		HIGH,
		VERY_HIGH;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Constants
		
		public static final @NonNull QueuePriority[] ASC = new Operation.QueuePriority[] {QueuePriority.VERY_LOW, QueuePriority.LOW, QueuePriority.NORMAL, QueuePriority.HIGH, QueuePriority.VERY_HIGH};
		public static final @NonNull QueuePriority[] DESC = new Operation.QueuePriority[] {QueuePriority.VERY_HIGH, QueuePriority.HIGH, QueuePriority.NORMAL, QueuePriority.LOW, QueuePriority.VERY_LOW};
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
