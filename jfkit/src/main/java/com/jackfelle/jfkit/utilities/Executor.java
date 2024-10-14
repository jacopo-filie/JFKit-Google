package com.jackfelle.jfkit.utilities;

import com.jackfelle.jfkit.core.operations.BlockOperation;
import com.jackfelle.jfkit.core.operations.OperationQueue;
import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.data.Error;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Executor <OwnerType>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Observers
	
	private final @Nullable WeakReference<OwnerType> owner;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Service
	
	private final @NonNull OperationQueue queue;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Observers
	
	public @Nullable OwnerType getOwner() {
		return Utilities.unwrapObject(this.owner);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Service
	
	protected @NonNull OperationQueue getQueue() {
		return this.queue;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public Executor(@NonNull OwnerType owner) {
		this(owner, OperationQueue.newSerialQueue(Executor.class.getSimpleName()));
	}
	
	public Executor(@NonNull OwnerType owner, @NonNull OperationQueue queue) {
		super();
		
		this.owner = Utilities.weakWrapObject(owner);
		this.queue = queue;
	}
	
	@Override protected void finalize() throws Throwable {
		this.cancelEnqueuedBlocks();
		super.finalize();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Service
	
	public void cancelEnqueuedBlocks() {
		this.getQueue().cancelAllOperations();
	}
	
	public void enqueue(@NonNull ExecutorBlock<OwnerType> block) {
		this.enqueue(block, null, false);
	}
	
	public void enqueue(@NonNull ExecutorBlock<OwnerType> block, @Nullable Blocks.FailureBlock failureBlock) {
		this.enqueue(block, failureBlock, false);
	}
	
	public void enqueue(@NonNull ExecutorBlock<OwnerType> block, @Nullable Blocks.FailureBlock failureBlock, boolean waitUntilFinished) {
		boolean[] executed = {false};
		WeakReference<OwnerType> weakOwner = Utilities.weakWrapObject(this.getOwner());
		
		BlockOperation operation = new BlockOperation(() -> {
			executed[0] = true;
			OwnerType strongOwner = Utilities.unwrapObject(weakOwner);
			if(strongOwner != null) {
				block.execute(strongOwner);
			} else if(failureBlock != null) {
				failureBlock.execute(Executor.newFinalizedError());
			}
		});
		
		if(failureBlock != null) {
			operation.setCompletion(() -> {
				if(!executed[0]) {
					failureBlock.execute(Executor.newCanceledError());
				}
			});
		}
		
		this.getQueue().addOperation(operation, waitUntilFinished);
	}
	
	public void enqueue(@NonNull ExecutorBlock<OwnerType> block, boolean waitUntilFinished) {
		this.enqueue(block, null, waitUntilFinished);
	}
	
	public void execute(@NonNull ExecutorBlock<OwnerType> block) {
		this.execute(block, null, false);
	}
	
	public void execute(@NonNull ExecutorBlock<OwnerType> block, @Nullable Blocks.FailureBlock failureBlock) {
		this.execute(block, failureBlock, false);
	}
	
	public void execute(@NonNull ExecutorBlock<OwnerType> block, @Nullable Blocks.FailureBlock failureBlock, boolean waitUntilFinished) {
		if(OperationQueue.getCurrentQueue() != this.getQueue()) {
			this.enqueue(block, failureBlock, waitUntilFinished);
			return;
		}
		
		OwnerType owner = this.getOwner();
		if(owner != null) {
			block.execute(owner);
		} else if(failureBlock != null) {
			failureBlock.execute(Executor.newFinalizedError());
		}
	}
	
	public void execute(@NonNull ExecutorBlock<OwnerType> block, boolean waitUntilFinished) {
		this.execute(block, null, waitUntilFinished);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	protected static @NonNull Error newCanceledError() {
		return new Error(Executor.class.getSimpleName(), 1, "Operation canceled.");
	}
	
	protected static @NonNull Error newFinalizedError() {
		return new Error(Executor.class.getSimpleName(), 2, "Owner finalized.");
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface ExecutorBlock <OwnerType>
	{
		void execute(@NonNull OwnerType owner);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
