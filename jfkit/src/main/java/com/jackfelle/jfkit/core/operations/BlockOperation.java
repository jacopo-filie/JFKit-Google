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

import com.jackfelle.jfkit.data.Blocks;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class BlockOperation extends Operation
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Execution
	
	private final @NonNull List<Blocks.Block> executionBlocks;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Execution
	
	public @NonNull List<Blocks.Block> getExecutionBlocks() {
		return new ArrayList<>(this.executionBlocks);
	}
	
	protected @NonNull List<Blocks.Block> getInternalExecutionBlocks() {
		return this.executionBlocks;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory management
	
	public BlockOperation() {
		this.executionBlocks = new ArrayList<>();
	}
	
	public BlockOperation(@NonNull Blocks.Block executionBlock) {
		this();
		this.getInternalExecutionBlocks().add(executionBlock);
	}
	
	public BlockOperation(@NonNull List<Blocks.Block> executionBlocks) {
		this();
		this.getInternalExecutionBlocks().addAll(executionBlocks);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Execution management
	
	public void addExecutionBlock(@NonNull Blocks.Block executionBlock) {
		if(this.isExecuting()) {
			throw new RuntimeException("You can't add execution blocks to the operation while it is executing.");
		}
		
		this.getInternalExecutionBlocks().add(executionBlock);
	}
	
	public void addExecutionBlocks(@NonNull List<Blocks.Block> executionBlocks) {
		if(this.isExecuting()) {
			throw new RuntimeException("You can't add execution blocks to the operation while it is executing.");
		}
		
		this.getInternalExecutionBlocks().addAll(executionBlocks);
	}
	
	@Override protected void main() {
		List<Blocks.Block> executionBlocks = this.getExecutionBlocks();
		for(Blocks.Block executionBlock : executionBlocks) {
			executionBlock.execute();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
