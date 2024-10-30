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

import android.os.Handler;
import android.os.Looper;

import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.utilities.Lazy;
import com.jackfelle.jfkit.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OperationQueue implements Operation.Observer
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	private static final String TAG = OperationQueue.class.getSimpleName();
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Memory
	
	private static final @NonNull Lazy<OperationQueue> BACKGROUND_QUEUE;
	private static final @NonNull Lazy<OperationQueue> MAIN_QUEUE;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Concurrency
	
	private int executingConcurrentOperationCount;
	private final @NonNull Handler mainHandler;
	private int maxConcurrentOperationCount;
	private boolean needsStartWorkers;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private String name;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Execution
	
	private static final @NonNull Map<Thread, OperationQueue> WORKER_QUEUES;
	private final boolean mainQueue;
	private final @NonNull Map<Operation.QueuePriority, List<Operation>> queues;
	private boolean suspended;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Concurrency
	
	private static int getRuntimeAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	private synchronized int getExecutingConcurrentOperationCount() {
		return this.executingConcurrentOperationCount;
	}
	
	protected @NonNull Handler getMainHandler() {
		return this.mainHandler;
	}
	
	public synchronized int getMaxConcurrentOperationCount() {
		return this.maxConcurrentOperationCount;
	}
	
	public void setMaxConcurrentOperationCount(int maxConcurrentOperationCount) {
		if(this.isMainQueue()) {
			maxConcurrentOperationCount = 1;
		} else if(maxConcurrentOperationCount < 1) {
			maxConcurrentOperationCount = OperationQueue.getRuntimeAvailableProcessors();
		}
		
		synchronized(this) {
			if(this.maxConcurrentOperationCount == maxConcurrentOperationCount) {
				return;
			}
			
			int oldVal = this.maxConcurrentOperationCount;
			this.maxConcurrentOperationCount = maxConcurrentOperationCount;
			
			if(oldVal >= maxConcurrentOperationCount) {
				return;
			}
		}
		
		this.setNeedsStartWorkers();
	}
	
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
	
	private boolean isExecuting() {
		return (this.getExecutingConcurrentOperationCount() > 0);
	}
	
	public boolean isMainQueue() {
		return this.mainQueue;
	}
	
	private int getNeededWorkers() {
		int currentWorkers;
		int maxWorkers;
		
		synchronized(this) {
			if(this.suspended) {
				return 0;
			}
			
			currentWorkers = this.executingConcurrentOperationCount;
			maxWorkers = this.maxConcurrentOperationCount;
		}
		
		if(currentWorkers >= maxWorkers) {
			return 0;
		}
		
		maxWorkers -= currentWorkers;
		
		int retVal = 0;
		for(Operation operation : this.getOperations()) {
			if(operation.isReady() && (++retVal >= maxWorkers)) {
				break;
			}
		}
		return retVal;
	}
	
	public @NonNull List<Operation> getOperations() {
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		
		List<Operation> retObj = new ArrayList<>();
		for(List<Operation> queue : queues.values()) {
			synchronized(queue) {
				retObj.addAll(queue);
			}
		}
		return retObj;
	}
	
	private @NonNull Map<Operation.QueuePriority, List<Operation>> getQueues() {
		return this.queues;
	}
	
	protected void setNeedsStartWorkers() {
		synchronized(this) {
			if(this.needsStartWorkers) {
				return;
			}
			
			this.needsStartWorkers = true;
		}
		
		new Thread(() -> {
			synchronized(this) {
				if(!this.needsStartWorkers) {
					return;
				}
				
				this.needsStartWorkers = false;
			}
			
			this.startWorkers();
		}, this.newThreadName("Starter")).start();
	}
	
	public synchronized boolean isSuspended() {
		return this.suspended;
	}
	
	public synchronized void setSuspended(boolean suspended) {
		if(this.suspended == suspended) {
			return;
		}
		
		this.suspended = suspended;
		
		if(!suspended) {
			this.setNeedsStartWorkers();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	static {
		BACKGROUND_QUEUE = Lazy.newSynchronizedInstance(() -> OperationQueue.newQueue(OperationQueue.class.getSimpleName() + ".background", Integer.MAX_VALUE));
		MAIN_QUEUE = Lazy.newSynchronizedInstance(() -> {
			OperationQueue retObj = new OperationQueue(true);
			retObj.setName(OperationQueue.class.getSimpleName() + ".main");
			return retObj;
		});
		WORKER_QUEUES = new HashMap<>();
	}
	
	public static @NonNull OperationQueue getBackgroundQueue() {
		return OperationQueue.BACKGROUND_QUEUE.get();
	}
	
	public static @Nullable OperationQueue getCurrentQueue() {
		synchronized(OperationQueue.WORKER_QUEUES) {
			return OperationQueue.WORKER_QUEUES.get(Thread.currentThread());
		}
	}
	
	public static @NonNull OperationQueue getMainQueue() {
		return OperationQueue.MAIN_QUEUE.get();
	}
	
	public static @NonNull OperationQueue newConcurrentQueue(@Nullable String name) {
		return OperationQueue.newQueue(name, 0);
	}
	
	public static @NonNull OperationQueue newSerialQueue(@Nullable String name) {
		return OperationQueue.newQueue(name, 1);
	}
	
	public static @NonNull OperationQueue newQueue(@Nullable String name, int maxConcurrentOperations) {
		OperationQueue retObj = new OperationQueue();
		retObj.setMaxConcurrentOperationCount(maxConcurrentOperations);
		retObj.setName(name);
		return retObj;
	}
	
	public OperationQueue() {
		this(false);
	}
	
	protected OperationQueue(boolean isMainQueue) {
		Operation.QueuePriority[] queuePriorities = Operation.QueuePriority.values();
		Map<Operation.QueuePriority, List<Operation>> queues = new HashMap<>(queuePriorities.length);
		for(Operation.QueuePriority queuePriority : queuePriorities) {
			queues.put(queuePriority, new LinkedList<>());
		}
		
		// Concurrency
		this.executingConcurrentOperationCount = 0;
		this.mainHandler = new Handler(Looper.getMainLooper());
		this.maxConcurrentOperationCount = (isMainQueue ? 1 : OperationQueue.getRuntimeAvailableProcessors());
		this.needsStartWorkers = false;
		
		// Execution
		this.mainQueue = isMainQueue;
		this.queues = queues;
		this.suspended = false;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Concurrency
	
	private void startWorkers() {
		int neededWorkers = this.getNeededWorkers();
		if(neededWorkers == 0) {
			return;
		}
		
		String threadName = this.newThreadName("Worker");
		synchronized(this) {
			for(int i = 0; i < neededWorkers; i++) {
				this.executingConcurrentOperationCount++;
				new Thread(() -> {
					while(this.executeNextOperation()) {
						synchronized(this) {
							if(this.suspended || (this.executingConcurrentOperationCount > this.maxConcurrentOperationCount)) {
								break;
							}
						}
					}
					
					synchronized(this) {
						this.executingConcurrentOperationCount--;
					}
				}, threadName).start();
			}
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Execution
	
	private boolean executeNextOperation() {
		Operation operation = null;
		
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(Operation.QueuePriority queuePriority : Operation.QueuePriority.SORTED_VALUES_DESC) {
			List<Operation> queue = queues.get(queuePriority);
			if(queue == null) {
				continue;
			}
			
			synchronized(queue) {
				for(int i = 0; i < queue.size(); i++) {
					Operation temp = queue.get(i);
					if(temp.isReady()) {
						operation = temp;
						break;
					}
				}
			}
			
			if(operation != null) {
				break;
			}
		}
		
		if(operation == null) {
			return false;
		}
		
		Thread thread = Thread.currentThread();
		synchronized(OperationQueue.WORKER_QUEUES) {
			OperationQueue.WORKER_QUEUES.put(thread, this);
		}
		
		if(this.isMainQueue()) {
			this.getMainHandler().post(operation::start);
		} else {
			operation.start();
		}
		
		operation.waitUntilFinished();
		
		synchronized(OperationQueue.WORKER_QUEUES) {
			OperationQueue.WORKER_QUEUES.remove(thread);
		}
		
		return true;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Operations
	
	public void addOperation(@NonNull Blocks.Block executionBlock) {
		this.addOperation(new BlockOperation(executionBlock), false);
	}
	
	public void addOperation(@NonNull Blocks.Block executionBlock, boolean waitUntilFinished) {
		this.addOperation(new BlockOperation(executionBlock), waitUntilFinished);
	}
	
	public void addOperation(@NonNull Operation operation) {
		this.addOperation(operation, false);
	}
	
	public void addOperation(@NonNull Operation operation, boolean waitUntilFinished) {
		List<Operation> operations = new ArrayList<>();
		operations.add(operation);
		this.addOperations(operations, waitUntilFinished);
	}
	
	public void addOperations(@NonNull List<Operation> operations) {
		this.addOperations(operations, false);
	}
	
	public void addOperations(@NonNull List<Operation> operations, boolean waitUntilFinished) {
		if(operations.isEmpty()) {
			return;
		}
		
		operations = new ArrayList<>(operations);
		
		List<Operation> invalidOperations = null;
		for(Operation operation : operations) {
			if(operation.isExecuting() || operation.isFinished()) {
				if(invalidOperations == null) {
					invalidOperations = new ArrayList<>(operations.size());
				}
				invalidOperations.add(operation);
			}
		}
		if(invalidOperations != null) {
			operations.removeAll(invalidOperations);
			if(operations.isEmpty()) {
				return;
			}
		}
		
		Map<Operation.QueuePriority, List<Operation>> operationsByPriority = new HashMap<>();
		for(Operation operation : operations) {
			Operation.QueuePriority queuePriority = operation.getQueuePriority();
			List<Operation> queue = operationsByPriority.get(operation.getQueuePriority());
			if(queue == null) {
				queue = new ArrayList<>(1);
				operationsByPriority.put(queuePriority, queue);
			}
			queue.add(operation);
		}
		
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(Operation.QueuePriority queuePriority : operationsByPriority.keySet()) {
			List<Operation> newOperations = Objects.requireNonNull(operationsByPriority.get(queuePriority));
			List<Operation> queue = Objects.requireNonNull(queues.get(queuePriority));
			synchronized(queue) {
				queue.addAll(newOperations);
			}
		}
		
		for(Operation operation : operations) {
			operation.addObserver(this);
		}
		
		this.setNeedsStartWorkers();
		
		if(waitUntilFinished) {
			for(Operation operation : operations) {
				operation.waitUntilFinished();
			}
		}
	}
	
	public void cancelAllOperations() {
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(List<Operation> queue : queues.values()) {
			synchronized(queue) {
				for(Operation operation : queue) {
					if(!operation.isFinished()) {
						operation.cancel();
					}
				}
			}
		}
	}
	
	public void waitUntilAllOperationsAreFinished() {
		List<Operation> operations = this.getOperations();
		for(Operation operation : operations) {
			operation.waitUntilFinished();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	private @NonNull String newThreadName(@NonNull String suffix) {
		return String.format(Locale.US, "%s-%s", Utilities.replaceIfNull(this.getName(), () -> {
			return Objects.requireNonNull(OperationQueue.class.getSimpleName());
		}), suffix);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods (Operation.Observer)
	
	@Override public void operationIsCanceled(@NonNull Operation sender) {
		// Nothing to do.
	}
	
	@Override public void operationIsExecuting(@NonNull Operation sender) {
		// Nothing to do.
	}
	
	@Override public void operationIsFinished(@NonNull Operation sender) {
		sender.removeObserver(this);
		
		Map<Operation.QueuePriority, List<Operation>> queues = this.getQueues();
		for(List<Operation> queue : queues.values()) {
			synchronized(queue) {
				queue.remove(sender);
			}
		}
	}
	
	@Override public void operationIsReady(@NonNull Operation sender) {
		this.setNeedsStartWorkers();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
