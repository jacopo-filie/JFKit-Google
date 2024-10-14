package com.jackfelle.jfkit.utilities;

import com.jackfelle.jfkit.data.Blocks;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseObserver <OwnerType>
{
	private final @Nullable WeakReference<OwnerType> owner;
	
	public @Nullable OwnerType getOwner() {
		return Utilities.unwrapObject(this.owner);
	}
	
	public BaseObserver(@NonNull OwnerType owner) {
		super();
		
		this.owner = Utilities.weakWrapObject(owner);
	}
	
	protected void execute(@NonNull ExecutionBlock<OwnerType> block) {
		this.execute(block, null);
	}
	
	protected void execute(@NonNull ExecutionBlock<OwnerType> block, @Nullable Blocks.Block elseBlock) {
		OwnerType owner = this.getOwner();
		if(owner != null) {
			block.execute(owner);
		} else if(elseBlock != null) {
			elseBlock.execute();
		}
	}
	
	protected boolean execute(@NonNull ExecutionBlockWithBooleanResult<OwnerType> block, boolean fallback) {
		OwnerType owner = this.getOwner();
		if(owner != null) {
			return block.execute(owner);
		}
		return fallback;
	}
	
	protected interface ExecutionBlock <T>
	{
		void execute(@NonNull T owner);
	}
	
	protected interface ExecutionBlockWithBooleanResult <T>
	{
		boolean execute(@NonNull T owner);
	}
}
