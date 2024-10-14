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

public class ConnectionMachine extends StateMachine
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	// States
	public static final int STATE_READY = 0;
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_DISCONNECTED = 2;
	public static final int STATE_LOST = 3;
	public static final int STATE_DIRTY = 4;
	
	// Transitions
	public static final int TRANSITION_CONNECTING = 1;
	public static final int TRANSITION_DISCONNECTING_FROM_CONNECTED = 2;
	public static final int TRANSITION_DISCONNECTING_FROM_LOST = 3;
	public static final int TRANSITION_LOSING_CONNECTION = 4;
	public static final int TRANSITION_RECONNECTING = 5;
	public static final int TRANSITION_RESETTING_FROM_DISCONNECTED = 6;
	public static final int TRANSITION_RESETTING_FROM_DIRTY = 7;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - State
	
	public boolean isConnected() {
		return (this.getCurrentState() == STATE_CONNECTED);
	}
	
	public boolean isDirty() {
		return (this.getCurrentState() == STATE_DIRTY);
	}
	
	public boolean isDisconnected() {
		return (this.getCurrentState() == STATE_DISCONNECTED);
	}
	
	public boolean isLost() {
		return (this.getCurrentState() == STATE_LOST);
	}
	
	public boolean isReady() {
		return (this.getCurrentState() == STATE_READY);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Transitions
	
	public boolean isConnecting() {
		return (this.getCurrentTransition() == TRANSITION_CONNECTING);
	}
	
	public boolean isDisconnecting() {
		int transition = this.getCurrentTransition();
		return ((transition == TRANSITION_DISCONNECTING_FROM_CONNECTED) || (transition == TRANSITION_DISCONNECTING_FROM_LOST));
	}
	
	public boolean isLosingConnection() {
		return (this.getCurrentTransition() == TRANSITION_LOSING_CONNECTION);
	}
	
	public boolean isReconnecting() {
		return (this.getCurrentTransition() == TRANSITION_RECONNECTING);
	}
	
	public boolean isResetting() {
		int transition = this.getCurrentTransition();
		return ((transition == TRANSITION_RESETTING_FROM_DIRTY) || (transition == TRANSITION_RESETTING_FROM_DISCONNECTED));
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Memory management
	
	public ConnectionMachine(@NonNull Delegate delegate) {
		super(STATE_READY, delegate);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region State management
	
	public void connect() {
		this.connect(null);
	}
	
	public void connect(Blocks.SimpleCompletionBlock completion) {
		this.connect(null, completion);
	}
	
	public void connect(Object context, Blocks.SimpleCompletionBlock completion) {
		this.performTransition(TRANSITION_CONNECTING, context, completion);
	}
	
	public void disconnect() {
		this.disconnect(null);
	}
	
	public void disconnect(Blocks.SimpleCompletionBlock completion) {
		this.disconnect(null, completion);
	}
	
	public void disconnect(Object context, Blocks.SimpleCompletionBlock completion) {
		int transition;
		switch(this.getCurrentState()) {
			case STATE_CONNECTED:
				transition = TRANSITION_DISCONNECTING_FROM_CONNECTED;
				break;
			case STATE_LOST:
				transition = TRANSITION_DISCONNECTING_FROM_LOST;
				break;
			default:
				transition = TRANSITION_NOT_AVAILABLE;
				break;
		}
		this.performTransition(transition, context, completion);
	}
	
	@Override public int getFinalStateForFailedTransition(int transition) {
		switch(transition) {
			case TRANSITION_CONNECTING:
			case TRANSITION_LOSING_CONNECTION:
			case TRANSITION_RECONNECTING:
				return STATE_LOST;
			case TRANSITION_DISCONNECTING_FROM_CONNECTED:
			case TRANSITION_DISCONNECTING_FROM_LOST:
			case TRANSITION_RESETTING_FROM_DIRTY:
			case TRANSITION_RESETTING_FROM_DISCONNECTED:
				return STATE_DIRTY;
			default:
				return super.getFinalStateForFailedTransition(transition);
		}
	}
	
	@Override public int getFinalStateForSucceededTransition(int transition) {
		switch(transition) {
			case TRANSITION_CONNECTING:
			case TRANSITION_RECONNECTING:
				return STATE_CONNECTED;
			case TRANSITION_DISCONNECTING_FROM_CONNECTED:
			case TRANSITION_DISCONNECTING_FROM_LOST:
				return STATE_DISCONNECTED;
			case TRANSITION_LOSING_CONNECTION:
				return STATE_LOST;
			case TRANSITION_RESETTING_FROM_DIRTY:
			case TRANSITION_RESETTING_FROM_DISCONNECTED:
				return STATE_READY;
			default:
				return super.getFinalStateForSucceededTransition(transition);
		}
	}
	
	@Override public int getInitialStateForTransition(int transition) {
		switch(transition) {
			case TRANSITION_CONNECTING:
				return STATE_READY;
			case TRANSITION_DISCONNECTING_FROM_CONNECTED:
			case TRANSITION_LOSING_CONNECTION:
				return STATE_CONNECTED;
			case TRANSITION_DISCONNECTING_FROM_LOST:
			case TRANSITION_RECONNECTING:
				return STATE_LOST;
			case TRANSITION_RESETTING_FROM_DIRTY:
				return STATE_DIRTY;
			case TRANSITION_RESETTING_FROM_DISCONNECTED:
				return STATE_DISCONNECTED;
			default:
				return super.getInitialStateForTransition(transition);
		}
	}
	
	public void loseConnection() {
		this.loseConnection(null);
	}
	
	public void loseConnection(Blocks.SimpleCompletionBlock completion) {
		this.loseConnection(null, completion);
	}
	
	public void loseConnection(Object context, Blocks.SimpleCompletionBlock completion) {
		this.performTransition(TRANSITION_LOSING_CONNECTION, context, completion);
	}
	
	public void reconnect() {
		this.reconnect(null);
	}
	
	public void reconnect(Blocks.SimpleCompletionBlock completion) {
		this.reconnect(null, completion);
	}
	
	public void reconnect(Object context, Blocks.SimpleCompletionBlock completion) {
		this.performTransition(TRANSITION_RECONNECTING, context, completion);
	}
	
	public void reset() {
		this.reset(null);
	}
	
	public void reset(Blocks.SimpleCompletionBlock completion) {
		this.reset(null, completion);
	}
	
	public void reset(Object context, Blocks.SimpleCompletionBlock completion) {
		int transition;
		switch(this.getCurrentState()) {
			case STATE_DIRTY:
				transition = TRANSITION_RESETTING_FROM_DIRTY;
				break;
			case STATE_DISCONNECTED:
				transition = TRANSITION_RESETTING_FROM_DISCONNECTED;
				break;
			default:
				transition = TRANSITION_NOT_AVAILABLE;
				break;
		}
		this.performTransition(transition, context, completion);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Utilities management
	
	@Override public String getDebugStringForState(int state) {
		switch(state) {
			case STATE_CONNECTED:
				return "Connected";
			case STATE_DIRTY:
				return "Dirty";
			case STATE_DISCONNECTED:
				return "Disconnected";
			case STATE_LOST:
				return "Lost";
			case STATE_READY:
				return "Ready";
			default:
				return super.getDebugStringForState(state);
		}
	}
	
	@Override public String getDebugStringForTransition(int transition) {
		switch(transition) {
			case TRANSITION_CONNECTING:
				return "Connecting";
			case TRANSITION_DISCONNECTING_FROM_CONNECTED:
				return "DisconnectingFromConnected";
			case TRANSITION_DISCONNECTING_FROM_LOST:
				return "DisconnectingFromLost";
			case TRANSITION_LOSING_CONNECTION:
				return "LosingConnection";
			case TRANSITION_RECONNECTING:
				return "Reconnecting";
			case TRANSITION_RESETTING_FROM_DIRTY:
				return "ResettingFromDirty";
			case TRANSITION_RESETTING_FROM_DISCONNECTED:
				return "ResettingFromDisconnected";
			default:
				return super.getDebugStringForTransition(transition);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
