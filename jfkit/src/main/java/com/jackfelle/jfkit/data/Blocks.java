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

package com.jackfelle.jfkit.data;

import com.jackfelle.jfkit.core.operations.OperationQueue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Blocks
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public static void executeBlock(@Nullable Block block) {
		if(block != null) {
			block.execute();
		}
	}
	
	public static void executeBlock(@Nullable Block block, @NonNull OperationQueue queue) {
		if(block != null) {
			queue.addOperation(block);
		}
	}
	
	public static <T> void executeCompletion(@Nullable Completion<T> completion, @NonNull Throwable error) {
		if(completion != null) {
			completion.executeWithError(error);
		}
	}
	
	public static <T> void executeCompletion(@Nullable Completion<T> completion, @NonNull Throwable error, boolean async) {
		if(completion != null) {
			completion.executeWithError(error, async);
		}
	}
	
	public static <T> void executeCompletion(@Nullable Completion<T> completion, @NonNull T result) {
		if(completion != null) {
			completion.executeWithResult(result);
		}
	}
	
	public static <T> void executeCompletion(@Nullable Completion<T> completion, @NonNull T result, boolean async) {
		if(completion != null) {
			completion.executeWithResult(result, async);
		}
	}
	
	public static void executeCompletion(@Nullable SimpleCompletion completion, @NonNull Throwable error) {
		if(completion != null) {
			completion.executeWithError(error);
		}
	}
	
	public static void executeCompletion(@Nullable SimpleCompletion completion, @NonNull Throwable error, boolean async) {
		if(completion != null) {
			completion.executeWithError(error, async);
		}
	}
	
	public static void executeCompletion(@Nullable SimpleCompletion completion) {
		if(completion != null) {
			completion.execute();
		}
	}
	
	public static void executeCompletion(@Nullable SimpleCompletion completion, boolean async) {
		if(completion != null) {
			completion.execute(async);
		}
	}
	
	public static <T> void executeCompletionBlock(@Nullable CompletionBlock<T> block, @NonNull Throwable error) {
		if(block != null) {
			block.execute(false, null, error);
		}
	}
	
	public static <T> void executeCompletionBlock(@Nullable CompletionBlock<T> block, @NonNull T result) {
		if(block != null) {
			block.execute(true, result, null);
		}
	}
	
	public static void executeCompletionBlock(@Nullable SimpleCompletionBlock block, @NonNull Throwable error) {
		if(block != null) {
			block.execute(false, error);
		}
	}
	
	public static void executeCompletionBlock(@Nullable SimpleCompletionBlock block) {
		if(block != null) {
			block.execute(true, null);
		}
	}
	
	public static void executeFailureBlock(@Nullable FailureBlock block, @NonNull Throwable error) {
		if(block != null) {
			block.execute(error);
		}
	}
	
	public static void executeFailureBlock(@Nullable FailureBlock block, @NonNull Throwable error, @NonNull OperationQueue queue) {
		if(block != null) {
			queue.addOperation(() -> block.execute(error));
		}
	}
	
	public static <T> void executeSuccessBlock(@Nullable SuccessBlock<T> block, @NonNull T result) {
		if(block != null) {
			block.execute(result);
		}
	}
	
	public static <T> void executeSuccessBlock(@Nullable SuccessBlock<T> block, @NonNull T result, @NonNull OperationQueue queue) {
		if(block != null) {
			queue.addOperation(() -> block.execute(result));
		}
	}
	
	public static @Nullable <T> CompletionBlock<T> unwrapCompletionBlock(@Nullable Completion<T> completion) {
		return (completion == null) ? null : completion.getBlock();
	}
	
	public static @Nullable SimpleCompletionBlock unwrapCompletionBlock(@Nullable SimpleCompletion completion) {
		return (completion == null) ? null : completion.getBlock();
	}
	
	public static @Nullable <T> Completion<T> wrapCompletionBlock(@Nullable CompletionBlock<T> completionBlock) {
		return (completionBlock == null) ? null : new Completion<>(completionBlock);
	}
	
	public static @Nullable SimpleCompletion wrapCompletionBlock(@Nullable SimpleCompletionBlock completionBlock) {
		return (completionBlock == null) ? null : new SimpleCompletion(completionBlock);
	}
	
	public static @Nullable FailureBlock wrapInFailureBlock(@Nullable Block block) {
		return (block == null) ? null : error -> block.execute();
	}
	
	public static @Nullable FailureBlock wrapInFailureBlock(@Nullable CompletionBlock<?> completionBlock) {
		return (completionBlock == null) ? null : error -> completionBlock.execute(false, null, error);
	}
	
	public static @Nullable FailureBlock wrapInFailureBlock(@Nullable SimpleCompletionBlock completionBlock) {
		return (completionBlock == null) ? null : error -> completionBlock.execute(false, error);
	}
	
	public static @Nullable <T> SuccessBlock<T> wrapInSuccessBlock(@Nullable Block block) {
		return (block == null) ? null : result -> block.execute();
	}
	
	public static @Nullable <T> SuccessBlock<T> wrapInSuccessBlock(@Nullable CompletionBlock<T> completionBlock) {
		return (completionBlock == null) ? null : result -> completionBlock.execute(true, result, null);
	}
	
	public static @Nullable <T> SuccessBlock<T> wrapInSuccessBlock(@Nullable SimpleCompletionBlock completionBlock) {
		return (completionBlock == null) ? null : result -> completionBlock.execute(true, null);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Block
	{
		void execute();
	}
	
	public interface BlockWithArray <T>
	{
		void execute(@NonNull T[] objects);
	}
	
	public interface BlockWithBoolean
	{
		void execute(boolean value);
	}
	
	public interface BlockWithError
	{
		void execute(@NonNull Throwable error);
	}
	
	public interface BlockWithObject <T>
	{
		void execute(@NonNull T object);
	}
	
	public interface BlockWithOptionalArray <T>
	{
		void execute(@Nullable T[] objects);
	}
	
	public interface BlockWithOptionalError
	{
		void execute(@Nullable Throwable error);
	}
	
	public interface BlockWithOptionalObject <T>
	{
		void execute(@Nullable T object);
	}
	
	public interface CompletionBlock <T>
	{
		void execute(boolean succeeded, @Nullable T result, @Nullable Throwable error);
	}
	
	public interface FailureBlock extends BlockWithError
	{
		// Nothing to do.
	}
	
	public interface SimpleCompletionBlock
	{
		void execute(boolean succeeded, @Nullable Throwable error);
	}
	
	public interface SuccessBlock <T> extends BlockWithObject<T>
	{
		// Nothing to do.
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	public static class Completion <ResultType>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Execution
		
		private @Nullable CompletionBlock<ResultType> block;
		private @Nullable FailureBlock failureBlock;
		private @Nullable Block finallyBlock;
		private @Nullable FailureBlock internalFailureBlock;
		private @Nullable SuccessBlock<ResultType> internalSuccessBlock;
		private @Nullable SuccessBlock<ResultType> successBlock;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Execution
		
		public @Nullable CompletionBlock<ResultType> getBlock() {
			return this.block;
		}
		
		public @Nullable FailureBlock getFailureBlock() {
			return this.failureBlock;
		}
		
		private @Nullable FailureBlock getInternalFailureBlock() {
			return this.internalFailureBlock;
		}
		
		private @Nullable Block getFinallyBlock() {
			return this.finallyBlock;
		}
		
		private @Nullable SuccessBlock<ResultType> getInternalSuccessBlock() {
			return this.internalSuccessBlock;
		}
		
		public @Nullable SuccessBlock<ResultType> getSuccessBlock() {
			return this.successBlock;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public Completion(@NonNull CompletionBlock<ResultType> block) {
			super();
			
			this.block = block;
			this.internalFailureBlock = Completion.newFailureBlockForCompletionBlock(block);
			this.internalSuccessBlock = Completion.newSuccessBlockForCompletionBlock(block);
		}
		
		public Completion(@NonNull FailureBlock failureBlock) {
			super();
			
			this.failureBlock = failureBlock;
			this.internalFailureBlock = failureBlock;
		}
		
		public Completion(@NonNull SuccessBlock<ResultType> successBlock) {
			super();
			
			this.internalSuccessBlock = successBlock;
			this.successBlock = successBlock;
		}
		
		public Completion(@NonNull SuccessBlock<ResultType> successBlock, @NonNull FailureBlock failureBlock) {
			super();
			
			this.failureBlock = failureBlock;
			this.internalFailureBlock = failureBlock;
			this.internalSuccessBlock = successBlock;
			this.successBlock = successBlock;
		}
		
		public Completion(@NonNull SuccessBlock<ResultType> successBlock, @NonNull FailureBlock failureBlock, @NonNull Block finallyBlock) {
			super();
			
			this.failureBlock = failureBlock;
			this.finallyBlock = finallyBlock;
			this.internalFailureBlock = Completion.appendFinallyBlock(failureBlock, finallyBlock);
			this.internalSuccessBlock = Completion.appendFinallyBlock(successBlock, finallyBlock);
			this.successBlock = successBlock;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Execution
		
		public void executeWithError(@NonNull Throwable error) {
			this.executeWithError(error, false);
		}
		
		public void executeWithError(@NonNull Throwable error, boolean async) {
			if(async) {
				this.executeWithError(error, OperationQueue.getBackgroundQueue());
			} else {
				Blocks.executeFailureBlock(this.getInternalFailureBlock(), error);
			}
		}
		
		public void executeWithError(@NonNull Throwable error, @NonNull OperationQueue queue) {
			Blocks.executeFailureBlock(this.getInternalFailureBlock(), error, queue);
		}
		
		public void executeWithResult(@NonNull ResultType result) {
			this.executeWithResult(result, false);
		}
		
		public void executeWithResult(@NonNull ResultType result, boolean async) {
			if(async) {
				this.executeWithResult(result, OperationQueue.getBackgroundQueue());
			} else {
				Blocks.executeSuccessBlock(this.getInternalSuccessBlock(), result);
			}
		}
		
		public void executeWithResult(@NonNull ResultType result, @NonNull OperationQueue queue) {
			Blocks.executeSuccessBlock(this.getInternalSuccessBlock(), result, queue);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Utilities
		
		private static @NonNull FailureBlock appendFinallyBlock(@NonNull FailureBlock failureBlock, @NonNull Block finallyBlock) {
			return error -> {
				failureBlock.execute(error);
				finallyBlock.execute();
			};
		}
		
		private static @NonNull <ResultType> SuccessBlock<ResultType> appendFinallyBlock(@NonNull SuccessBlock<ResultType> successBlock, @NonNull Block finallyBlock) {
			return result -> {
				successBlock.execute(result);
				finallyBlock.execute();
			};
		}
		
		private static @NonNull <ResultType> FailureBlock newFailureBlockForCompletionBlock(@NonNull CompletionBlock<ResultType> block) {
			return error -> block.execute(false, null, error);
		}
		
		private static @NonNull <ResultType> SuccessBlock<ResultType> newSuccessBlockForCompletionBlock(@NonNull CompletionBlock<ResultType> block) {
			return result -> block.execute(true, result, null);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public static class SimpleCompletion
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Execution
		
		private @Nullable SimpleCompletionBlock block;
		private @Nullable FailureBlock failureBlock;
		private @Nullable Block finallyBlock;
		private @Nullable FailureBlock internalFailureBlock;
		private @Nullable Block internalSuccessBlock;
		private @Nullable Block successBlock;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Execution
		
		public @Nullable SimpleCompletionBlock getBlock() {
			return this.block;
		}
		
		public @Nullable FailureBlock getFailureBlock() {
			return this.failureBlock;
		}
		
		private @Nullable Block getFinallyBlock() {
			return this.finallyBlock;
		}
		
		private @Nullable FailureBlock getInternalFailureBlock() {
			return this.internalFailureBlock;
		}
		
		private @Nullable Block getInternalSuccessBlock() {
			return this.internalSuccessBlock;
		}
		
		public @Nullable Block getSuccessBlock() {
			return this.successBlock;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory management
		
		public SimpleCompletion(@NonNull SimpleCompletionBlock block) {
			super();
			
			this.block = block;
			this.internalFailureBlock = SimpleCompletion.newFailureBlockForCompletionBlock(block);
			this.internalSuccessBlock = SimpleCompletion.newSuccessBlockForCompletionBlock(block);
		}
		
		public SimpleCompletion(@NonNull FailureBlock failureBlock) {
			super();
			
			this.failureBlock = failureBlock;
			this.internalFailureBlock = failureBlock;
		}
		
		public SimpleCompletion(@NonNull Block successBlock) {
			super();
			
			this.internalSuccessBlock = successBlock;
			this.successBlock = successBlock;
		}
		
		public SimpleCompletion(@NonNull Block successBlock, @NonNull FailureBlock failureBlock) {
			super();
			
			this.failureBlock = failureBlock;
			this.internalFailureBlock = failureBlock;
			this.internalSuccessBlock = successBlock;
			this.successBlock = successBlock;
		}
		
		public SimpleCompletion(@NonNull Block successBlock, @NonNull FailureBlock failureBlock, @NonNull Block finallyBlock) {
			super();
			
			this.failureBlock = failureBlock;
			this.finallyBlock = finallyBlock;
			this.internalFailureBlock = SimpleCompletion.appendFinallyBlock(failureBlock, finallyBlock);
			this.internalSuccessBlock = SimpleCompletion.appendFinallyBlock(successBlock, finallyBlock);
			this.successBlock = successBlock;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Execution management
		
		public void execute() {
			this.execute(false);
		}
		
		public void execute(boolean async) {
			if(async) {
				this.execute(OperationQueue.getBackgroundQueue());
			} else {
				Blocks.executeBlock(this.getInternalSuccessBlock());
			}
		}
		
		public void execute(@NonNull OperationQueue queue) {
			Blocks.executeBlock(this.getInternalSuccessBlock(), queue);
		}
		
		public void executeWithError(@NonNull Throwable error) {
			this.executeWithError(error, false);
		}
		
		public void executeWithError(@NonNull Throwable error, boolean async) {
			if(async) {
				this.executeWithError(error, OperationQueue.getBackgroundQueue());
			} else {
				Blocks.executeFailureBlock(this.getInternalFailureBlock(), error);
			}
		}
		
		public void executeWithError(@NonNull Throwable error, @NonNull OperationQueue queue) {
			Blocks.executeFailureBlock(this.getInternalFailureBlock(), error, queue);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Utilities
		
		private static @NonNull FailureBlock appendFinallyBlock(@NonNull FailureBlock failureBlock, @NonNull Block finallyBlock) {
			return error -> {
				failureBlock.execute(error);
				finallyBlock.execute();
			};
		}
		
		private static @NonNull Block appendFinallyBlock(@NonNull Block successBlock, @NonNull Block finallyBlock) {
			return () -> {
				successBlock.execute();
				finallyBlock.execute();
			};
		}
		
		private static @NonNull FailureBlock newFailureBlockForCompletionBlock(@NonNull SimpleCompletionBlock block) {
			return error -> block.execute(false, error);
		}
		
		private static @NonNull Block newSuccessBlockForCompletionBlock(@NonNull SimpleCompletionBlock block) {
			return () -> block.execute(true, null);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
