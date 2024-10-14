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

package com.jackfelle.jfkit.core.operations;

import android.util.Log;

import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.utilities.BaseObserver;
import com.jackfelle.jfkit.utilities.ObserversController;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Operation
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private @Nullable String name;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Execution
	
	private @Nullable Blocks.Block completion;
	private @Nullable Set<Operation> dependencies;
	private @NonNull QueuePriority queuePriority;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Observers
	
	private @Nullable DependencyObserver dependencyObserver;
	private final @NonNull ObserversController<Observer> observers;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - State
	
	private boolean canceled;
	private boolean executing;
	private boolean finished;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Data
	
	public synchronized @Nullable String getName() {
		return this.name;
	}
	
	public synchronized void setName(@Nullable String name) {
		this.name = name;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Execution
	
	public synchronized @Nullable Blocks.Block getCompletion() {
		return this.completion;
	}
	
	public synchronized void setCompletion(@Nullable Blocks.Block completion) {
		this.completion = completion;
	}
	
	public @NonNull Set<Operation> getDependencies() {
		Set<Operation> retObj = this.getDependencies(false);
		return ((retObj == null) ? new HashSet<>() : new HashSet<>(retObj));
	}
	
	protected @Nullable Set<Operation> getDependencies(boolean createIfNeeded) {
		Set<Operation> retObj = this.dependencies;
		if((retObj == null) && createIfNeeded) {
			synchronized(this) {
				retObj = this.dependencies;
				if(retObj == null) {
					retObj = new HashSet<>();
					this.dependencies = retObj;
				}
			}
		}
		return retObj;
	}
	
	public synchronized @NonNull QueuePriority getQueuePriority() {
		return this.queuePriority;
	}
	
	public synchronized void setQueuePriority(@NonNull QueuePriority queuePriority) {
		this.queuePriority = queuePriority;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Observers
	
	protected @NonNull DependencyObserver getDependencyObserver() {
		DependencyObserver retObj = this.dependencyObserver;
		if(retObj == null) {
			synchronized(this) {
				retObj = this.dependencyObserver;
				if(retObj == null) {
					retObj = new DependencyObserver(this);
					this.dependencyObserver = retObj;
				}
			}
		}
		return retObj;
	}
	
	protected @NonNull ObserversController<Observer> getObservers() {
		return this.observers;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - State
	
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
		
		Set<Operation> dependencies = this.getDependencies(false);
		if(dependencies != null) {
			synchronized(dependencies) {
				for(Operation operation : dependencies) {
					if(!operation.isFinished()) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public Operation() {
		// Data
		this.name = null;
		
		// Execution
		this.completion = null;
		this.dependencies = null;
		this.queuePriority = QueuePriority.NORMAL;
		
		// Observers
		this.observers = new ObserversController<>();
		
		// State
		this.canceled = false;
		this.executing = false;
		this.finished = false;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Execution
	
	public <T extends Operation> void addDependencies(@NonNull Collection<T> operations) {
		if(operations.size() == 0) {
			return;
		}
		
		Set<Operation> dependencies = this.getDependencies(true);
		if(dependencies == null) {
			return;
		}
		
		synchronized(dependencies) {
			dependencies.addAll(operations);
			
			DependencyObserver observer = this.getDependencyObserver();
			for(Operation dependency : dependencies) {
				dependency.addObserver(observer);
			}
		}
	}
	
	public <T extends Operation> void addDependency(@NonNull T dependency) {
		Set<Operation> dependencies = this.getDependencies(true);
		if(dependencies == null) {
			return;
		}
		
		synchronized(dependencies) {
			dependencies.add(dependency);
			dependency.addObserver(this.getDependencyObserver());
		}
	}
	
	public void cancel() {
		synchronized(this) {
			if(this.canceled) {
				return;
			}
			
			this.canceled = true;
		}
		
		this.getObservers().notifyObservers(observer -> observer.operationIsCanceled(this), false);
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
		
		Blocks.Block completion = this.getCompletion();
		if(completion != null) {
			completion.execute();
		}
		
		this.getObservers().notifyObservers(observer -> observer.operationIsFinished(this), false);
	}
	
	protected void main() {
	}
	
	public <T extends Operation> void removeDependencies(@NonNull Collection<T> operations) {
		if(operations.size() == 0) {
			return;
		}
		
		Set<Operation> dependencies = this.getDependencies(false);
		if(dependencies == null) {
			return;
		}
		
		synchronized(dependencies) {
			DependencyObserver observer = this.getDependencyObserver();
			for(Operation dependency : dependencies) {
				dependency.removeObserver(observer);
			}
			
			dependencies.removeAll(operations);
		}
	}
	
	public <T extends Operation> void removeDependency(@NonNull T dependency) {
		Set<Operation> dependencies = this.getDependencies(false);
		if(dependencies == null) {
			return;
		}
		
		synchronized(dependencies) {
			dependency.removeObserver(this.getDependencyObserver());
			dependencies.remove(dependency);
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
		
		this.getObservers().notifyObservers(observer -> observer.operationIsExecuting(this), false);
		
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
		} catch(InterruptedException e) {
			Log.e("JFKit", "Thread interrupted.", e);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers
	
	public void addObserver(@NonNull Observer observer) {
		this.getObservers().addObserver(observer);
	}
	
	public void removeObserver(@NonNull Observer observer) {
		this.getObservers().removeObserver(observer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public static @NonNull List<Operation> chainOperations(@NonNull List<Operation> operations) {
		for(int i = 1; i < operations.size(); i++) {
			operations.get(i).addDependency(operations.get(i - 1));
		}
		return operations;
	}
	
	public static boolean isOperationCanceled(@Nullable Operation operation) {
		return (operation != null) && operation.isCanceled();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Types
	
	public enum QueuePriority
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		VERY_LOW,
		LOW,
		NORMAL,
		HIGH,
		VERY_HIGH;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Constants
		
		public static final @NonNull QueuePriority[] SORTED_VALUES_ASC = new Operation.QueuePriority[] {VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH};
		public static final @NonNull QueuePriority[] SORTED_VALUES_DESC = new Operation.QueuePriority[] {VERY_HIGH, HIGH, NORMAL, LOW, VERY_LOW};
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	@SuppressWarnings("EmptyMethod")
	public interface Observer
	{
		void operationIsCanceled(@NonNull Operation sender);
		void operationIsExecuting(@NonNull Operation sender);
		void operationIsFinished(@NonNull Operation sender);
		void operationIsReady(@NonNull Operation sender);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class DependencyObserver extends BaseObserver<Operation> implements Observer
	{
		public DependencyObserver(@NonNull Operation owner) {
			super(owner);
		}
		
		@Override public void operationIsCanceled(@NonNull Operation sender) {
			// Nothing to do.
		}
		
		@Override public void operationIsExecuting(@NonNull Operation sender) {
			// Nothing to do.
		}
		
		@Override public void operationIsFinished(@NonNull Operation sender) {
			this.execute(owner -> {
				if(owner.isReady()) {
					owner.getObservers().notifyObservers(observer -> observer.operationIsReady(owner));
				}
			});
		}
		
		@Override public void operationIsReady(@NonNull Operation sender) {
			// Nothing to do.
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
