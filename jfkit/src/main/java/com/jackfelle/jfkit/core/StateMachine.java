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

package com.jackfelle.jfkit.core;

import com.jackfelle.jfkit.core.operations.OperationQueue;
import com.jackfelle.jfkit.data.Blocks;
import com.jackfelle.jfkit.data.Error;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StateMachine
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants - Errors
	
	public static final int ERROR_INVALID_FINAL_STATE_ON_FAILURE = 1;
	public static final int ERROR_INVALID_FINAL_STATE_ON_SUCCESS = 2;
	public static final int ERROR_INVALID_INITIAL_STATE = 3;
	public static final int ERROR_INVALID_TRANSITION = 4;
	public static final int ERROR_WRONG_INITIAL_STATE = 5;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants - States & Transitions
	
	public static final int STATE_NOT_AVAILABLE = Integer.MAX_VALUE;
	public static final int TRANSITION_NONE = 0;
	public static final int TRANSITION_NOT_AVAILABLE = Integer.MAX_VALUE;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private int currentState;
	private int currentTransition;
	private final @NonNull WeakReference<Delegate> delegate;
	private final @NonNull OperationQueue notificationQueue;
	private final @NonNull OperationQueue transitionQueue;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public synchronized int getCurrentState() {
		return this.currentState;
	}
	
	public synchronized int getCurrentTransition() {
		return this.currentTransition;
	}
	
	private synchronized void setCurrentStateAndTransition(int state, int transition) {
		this.currentState = state;
		this.currentTransition = transition;
	}
	
	public @Nullable Delegate getDelegate() {
		return this.delegate.get();
	}
	
	private @NonNull OperationQueue getNotificationQueue() {
		return this.notificationQueue;
	}
	
	private @NonNull OperationQueue getTransitionQueue() {
		return this.transitionQueue;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public StateMachine(int state, @NonNull Delegate delegate) {
		super();
		
		String queueName = this.getClass().getSimpleName();
		
		this.currentState = state;
		this.currentTransition = TRANSITION_NONE;
		this.delegate = new WeakReference<>(delegate);
		this.notificationQueue = OperationQueue.newSerialQueue(queueName + ".notifications");
		this.transitionQueue = OperationQueue.newSerialQueue(queueName + ".transitions");
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Concurrency
	
	private void executeOnNotificationQueue(@NonNull Blocks.Block block) {
		this.getNotificationQueue().addOperation(block, false);
	}
	
	private void executeOnNotificationQueueAndWaitUntilFinished(@NonNull Blocks.Block block) {
		this.getNotificationQueue().addOperation(block, true);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - States
	
	public int getFinalStateForFailedTransition(int transition) {
		return STATE_NOT_AVAILABLE;
	}
	
	public int getFinalStateForSucceededTransition(int transition) {
		return STATE_NOT_AVAILABLE;
	}
	
	public int getInitialStateForTransition(int transition) {
		return STATE_NOT_AVAILABLE;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Transitions
	
	private void completeTransition(boolean succeeded, @Nullable Throwable error, @Nullable Object context, @Nullable Blocks.SimpleCompletionBlock completion) {
		int transition = this.getCurrentTransition();
		
		int finalState = (succeeded ? this.getFinalStateForSucceededTransition(transition) : this.getFinalStateForFailedTransition(transition));
		
		this.setCurrentStateAndTransition(finalState, TRANSITION_NONE);
		
		Delegate delegate = this.getDelegate();
		this.executeOnNotificationQueue(() -> {
			if(delegate != null) {
				delegate.stateMachineDidPerformTransition(this, transition, context);
			}
			if(completion != null) {
				completion.execute(succeeded, error);
			}
			this.getTransitionQueue().setSuspended(false);
		});
	}
	
	private @Nullable Error isValidTransition(int transition) {
		if((transition == TRANSITION_NONE) || (transition == TRANSITION_NOT_AVAILABLE)) {
			return new Error("", ERROR_INVALID_TRANSITION, null);
		}
		
		int state = this.getInitialStateForTransition(transition);
		if(state == STATE_NOT_AVAILABLE) {
			return new Error("", ERROR_INVALID_INITIAL_STATE);
		}
		
		state = this.getFinalStateForSucceededTransition(transition);
		if(state == STATE_NOT_AVAILABLE) {
			return new Error("", ERROR_INVALID_FINAL_STATE_ON_SUCCESS);
		}
		
		state = this.getFinalStateForFailedTransition(transition);
		if(state == STATE_NOT_AVAILABLE) {
			return new Error("", ERROR_INVALID_FINAL_STATE_ON_FAILURE);
		}
		
		return null;
	}
	
	public void performTransition(int transition, @Nullable Blocks.SimpleCompletionBlock completion) {
		this.performTransition(transition, null, completion);
	}
	
	public void performTransition(int transition, @Nullable Object context, @Nullable Blocks.SimpleCompletionBlock completion) {
		Blocks.BlockWithError errorBlock = error -> {
			if(completion != null) {
				this.executeOnNotificationQueueAndWaitUntilFinished(() -> completion.execute(false, error));
			}
		};
		
		Error error = this.isValidTransition(transition);
		if(error != null) {
			errorBlock.execute(error);
			return;
		}
		
		this.getTransitionQueue().addOperation(() -> {
			if(this.getInitialStateForTransition(transition) != this.getCurrentState()) {
				errorBlock.execute(new Error("", ERROR_WRONG_INITIAL_STATE));
			} else {
				this.performTransitionOnQueue(transition, context, completion);
			}
		});
	}
	
	private void performTransitionOnQueue(int transition, @Nullable Object context, @Nullable Blocks.SimpleCompletionBlock completion) {
		this.getTransitionQueue().setSuspended(true);
		
		Delegate delegate = this.getDelegate();
		if(delegate != null) {
			this.executeOnNotificationQueueAndWaitUntilFinished(() -> delegate.stateMachineWillPerformTransition(this, transition, context));
		}
		
		this.setCurrentStateAndTransition(this.getCurrentState(), transition);
		
		if(delegate != null) {
			this.executeOnNotificationQueueAndWaitUntilFinished(() -> delegate.stateMachinePerformTransition(this, transition, context, (succeeded, error) -> this.completeTransition(succeeded, error, context, completion)));
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public @Nullable String getDebugStringForState(int state) {
		return ((state == STATE_NOT_AVAILABLE) ? "NotAvailable" : null);
	}
	
	public @Nullable String getDebugStringForTransition(int transition) {
		switch(transition) {
			case TRANSITION_NONE:
				return "None";
			case TRANSITION_NOT_AVAILABLE:
				return "NotAvailable";
			default:
				return null;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Delegate
	{
		void stateMachineDidPerformTransition(@NonNull StateMachine sender, int transition, @Nullable Object context);
		void stateMachinePerformTransition(@NonNull StateMachine sender, int transition, @Nullable Object context, @NonNull Blocks.SimpleCompletionBlock completion);
		void stateMachineWillPerformTransition(@NonNull StateMachine sender, int transition, @Nullable Object context);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
