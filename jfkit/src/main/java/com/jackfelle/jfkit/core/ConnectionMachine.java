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
		return (this.getCurrentState() == ConnectionMachine.STATE_CONNECTED);
	}
	
	public boolean isDirty() {
		return (this.getCurrentState() == ConnectionMachine.STATE_DIRTY);
	}
	
	public boolean isDisconnected() {
		return (this.getCurrentState() == ConnectionMachine.STATE_DISCONNECTED);
	}
	
	public boolean isLost() {
		return (this.getCurrentState() == ConnectionMachine.STATE_LOST);
	}
	
	public boolean isReady() {
		return (this.getCurrentState() == ConnectionMachine.STATE_READY);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Transitions
	
	public boolean isConnecting() {
		return (this.getCurrentTransition() == ConnectionMachine.TRANSITION_CONNECTING);
	}
	
	public boolean isDisconnecting() {
		int transition = this.getCurrentTransition();
		return ((transition == ConnectionMachine.TRANSITION_DISCONNECTING_FROM_CONNECTED) || (transition == ConnectionMachine.TRANSITION_DISCONNECTING_FROM_LOST));
	}
	
	public boolean isLosingConnection() {
		return (this.getCurrentTransition() == ConnectionMachine.TRANSITION_LOSING_CONNECTION);
	}
	
	public boolean isReconnecting() {
		return (this.getCurrentTransition() == ConnectionMachine.TRANSITION_RECONNECTING);
	}
	
	public boolean isResetting() {
		int transition = this.getCurrentTransition();
		return ((transition == ConnectionMachine.TRANSITION_RESETTING_FROM_DIRTY) || (transition == ConnectionMachine.TRANSITION_RESETTING_FROM_DISCONNECTED));
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Memory management
	
	public ConnectionMachine(@NonNull Delegate delegate) {
		super(ConnectionMachine.STATE_READY, delegate);
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
		this.performTransition(ConnectionMachine.TRANSITION_CONNECTING, context, completion);
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
			case ConnectionMachine.STATE_CONNECTED:
				transition = ConnectionMachine.TRANSITION_DISCONNECTING_FROM_CONNECTED;
				break;
			case ConnectionMachine.STATE_LOST:
				transition = ConnectionMachine.TRANSITION_DISCONNECTING_FROM_LOST;
				break;
			default:
				transition = StateMachine.TRANSITION_NOT_AVAILABLE;
				break;
		}
		this.performTransition(transition, context, completion);
	}
	
	@Override public int getFinalStateForFailedTransition(int transition) {
		switch(transition) {
			case ConnectionMachine.TRANSITION_CONNECTING:
			case ConnectionMachine.TRANSITION_LOSING_CONNECTION:
			case ConnectionMachine.TRANSITION_RECONNECTING:
				return ConnectionMachine.STATE_LOST;
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_CONNECTED:
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_LOST:
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DIRTY:
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DISCONNECTED:
				return ConnectionMachine.STATE_DIRTY;
			default:
				return super.getFinalStateForFailedTransition(transition);
		}
	}
	
	@Override public int getFinalStateForSucceededTransition(int transition) {
		switch(transition) {
			case ConnectionMachine.TRANSITION_CONNECTING:
			case ConnectionMachine.TRANSITION_RECONNECTING:
				return ConnectionMachine.STATE_CONNECTED;
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_CONNECTED:
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_LOST:
				return ConnectionMachine.STATE_DISCONNECTED;
			case ConnectionMachine.TRANSITION_LOSING_CONNECTION:
				return ConnectionMachine.STATE_LOST;
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DIRTY:
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DISCONNECTED:
				return ConnectionMachine.STATE_READY;
			default:
				return super.getFinalStateForSucceededTransition(transition);
		}
	}
	
	@Override public int getInitialStateForTransition(int transition) {
		switch(transition) {
			case ConnectionMachine.TRANSITION_CONNECTING:
				return ConnectionMachine.STATE_READY;
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_CONNECTED:
			case ConnectionMachine.TRANSITION_LOSING_CONNECTION:
				return ConnectionMachine.STATE_CONNECTED;
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_LOST:
			case ConnectionMachine.TRANSITION_RECONNECTING:
				return ConnectionMachine.STATE_LOST;
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DIRTY:
				return ConnectionMachine.STATE_DIRTY;
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DISCONNECTED:
				return ConnectionMachine.STATE_DISCONNECTED;
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
		this.performTransition(ConnectionMachine.TRANSITION_LOSING_CONNECTION, context, completion);
	}
	
	public void reconnect() {
		this.reconnect(null);
	}
	
	public void reconnect(Blocks.SimpleCompletionBlock completion) {
		this.reconnect(null, completion);
	}
	
	public void reconnect(Object context, Blocks.SimpleCompletionBlock completion) {
		this.performTransition(ConnectionMachine.TRANSITION_RECONNECTING, context, completion);
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
			case ConnectionMachine.STATE_DIRTY:
				transition = ConnectionMachine.TRANSITION_RESETTING_FROM_DIRTY;
				break;
			case ConnectionMachine.STATE_DISCONNECTED:
				transition = ConnectionMachine.TRANSITION_RESETTING_FROM_DISCONNECTED;
				break;
			default:
				transition = StateMachine.TRANSITION_NOT_AVAILABLE;
				break;
		}
		this.performTransition(transition, context, completion);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Utilities management
	
	@Override public String getDebugStringForState(int state) {
		switch(state) {
			case ConnectionMachine.STATE_CONNECTED:
				return "Connected";
			case ConnectionMachine.STATE_DIRTY:
				return "Dirty";
			case ConnectionMachine.STATE_DISCONNECTED:
				return "Disconnected";
			case ConnectionMachine.STATE_LOST:
				return "Lost";
			case ConnectionMachine.STATE_READY:
				return "Ready";
			default:
				return super.getDebugStringForState(state);
		}
	}
	
	@Override public String getDebugStringForTransition(int transition) {
		switch(transition) {
			case ConnectionMachine.TRANSITION_CONNECTING:
				return "Connecting";
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_CONNECTED:
				return "DisconnectingFromConnected";
			case ConnectionMachine.TRANSITION_DISCONNECTING_FROM_LOST:
				return "DisconnectingFromLost";
			case ConnectionMachine.TRANSITION_LOSING_CONNECTION:
				return "LosingConnection";
			case ConnectionMachine.TRANSITION_RECONNECTING:
				return "Reconnecting";
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DIRTY:
				return "ResettingFromDirty";
			case ConnectionMachine.TRANSITION_RESETTING_FROM_DISCONNECTED:
				return "ResettingFromDisconnected";
			default:
				return super.getDebugStringForTransition(transition);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
