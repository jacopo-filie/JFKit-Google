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

import com.jackfelle.jfkit.data.Blocks;

import androidx.annotation.NonNull;

public class SwitchMachine extends StateMachine
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	// States
	public static final int STATE_CLOSED = 0;
	public static final int STATE_OPEN = 1;
	
	// Transitions
	public static final int TRANSITION_CLOSING = 1;
	public static final int TRANSITION_OPENING = 2;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - State
	
	public boolean isClosed() {
		return (this.getCurrentState() == SwitchMachine.STATE_CLOSED);
	}
	
	public boolean isOpen() {
		return (this.getCurrentState() == SwitchMachine.STATE_OPEN);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Transitions
	
	public boolean isClosing() {
		return (this.getCurrentTransition() == SwitchMachine.TRANSITION_CLOSING);
	}
	
	public boolean isOpening() {
		return (this.getCurrentTransition() == SwitchMachine.TRANSITION_OPENING);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Memory management
	
	public SwitchMachine(@NonNull Delegate delegate) {
		super(SwitchMachine.STATE_CLOSED, delegate);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region State management
	
	public void close() {
		this.close(null);
	}
	
	public void close(Blocks.SimpleCompletionBlock completion) {
		this.close(null, completion);
	}
	
	public void close(Object context, Blocks.SimpleCompletionBlock completion) {
		this.performTransition(SwitchMachine.TRANSITION_CLOSING, context, completion);
	}
	
	@Override public int getFinalStateForFailedTransition(int transition) {
		switch(transition) {
			case SwitchMachine.TRANSITION_CLOSING:
				return SwitchMachine.STATE_OPEN;
			case SwitchMachine.TRANSITION_OPENING:
				return SwitchMachine.STATE_CLOSED;
			default:
				return super.getFinalStateForFailedTransition(transition);
		}
	}
	
	@Override public int getFinalStateForSucceededTransition(int transition) {
		switch(transition) {
			case SwitchMachine.TRANSITION_CLOSING:
				return SwitchMachine.STATE_CLOSED;
			case SwitchMachine.TRANSITION_OPENING:
				return SwitchMachine.STATE_OPEN;
			default:
				return super.getFinalStateForSucceededTransition(transition);
		}
	}
	
	@Override public int getInitialStateForTransition(int transition) {
		switch(transition) {
			case SwitchMachine.TRANSITION_CLOSING:
				return SwitchMachine.STATE_OPEN;
			case SwitchMachine.TRANSITION_OPENING:
				return SwitchMachine.STATE_CLOSED;
			default:
				return super.getInitialStateForTransition(transition);
		}
	}
	
	public void open() {
		this.open(null);
	}
	
	public void open(Blocks.SimpleCompletionBlock completion) {
		this.open(null, completion);
	}
	
	public void open(Object context, Blocks.SimpleCompletionBlock completion) {
		this.performTransition(SwitchMachine.TRANSITION_OPENING, context, completion);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Utilities management
	
	@Override public String getDebugStringForState(int state) {
		switch(state) {
			case SwitchMachine.STATE_CLOSED:
				return "Closed";
			case SwitchMachine.STATE_OPEN:
				return "Open";
			default:
				return super.getDebugStringForState(state);
		}
	}
	
	@Override public String getDebugStringForTransition(int transition) {
		switch(transition) {
			case SwitchMachine.TRANSITION_CLOSING:
				return "Closing";
			case SwitchMachine.TRANSITION_OPENING:
				return "Opening";
			default:
				return super.getDebugStringForTransition(transition);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
