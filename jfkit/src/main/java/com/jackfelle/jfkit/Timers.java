//
//	The MIT License (MIT)
//
//	Copyright © 2021-2024 Jacopo Filié
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

package com.jackfelle.jfkit;

import android.os.CountDownTimer;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

@MainThread
public abstract class Timers
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Countdown timers
	
	public static @NonNull CountDownTimer newCountDownTimer(long timeInterval, @NonNull OnFinishBlock onFinishBlock) {
		return new CountDownTimer(timeInterval, timeInterval)
		{
			@Override public void onFinish() {
				onFinishBlock.execute(this);
			}
			
			@Override public void onTick(long millisUntilFinished) {}
		};
	}
	
	public static @NonNull CountDownTimer newCountDownTimer(long timeInterval, @NonNull OnFinishBlock onFinishBlock, long tickInterval, @NonNull OnTickBlock onTickBlock) {
		return new CountDownTimer(timeInterval, tickInterval)
		{
			@Override public void onFinish() {
				onFinishBlock.execute(this);
			}
			
			@Override public void onTick(long millisUntilFinished) {
				onTickBlock.execute(this, millisUntilFinished);
			}
		};
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Scheduled timers
	
	public static @NonNull Timer newScheduledTimer(long timeInterval, @NonNull TaskBlock taskBlock) {
		return Timers.newScheduledTimer(timeInterval, 0, false, taskBlock);
	}
	
	public static @NonNull Timer newScheduledTimer(long timeInterval, boolean repeats, @NonNull TaskBlock taskBlock) {
		return Timers.newScheduledTimer(timeInterval, 0, repeats, taskBlock);
	}
	
	public static @NonNull Timer newScheduledTimer(long timeInterval, long delay, boolean repeats, @NonNull TaskBlock taskBlock) {
		Timer retObj = new Timer();
		WeakReference<Timer> weakTimer = new WeakReference<>(retObj);
		
		TimerTask task = new TimerTask()
		{
			@Override public void run() {
				Timer strongTimer = weakTimer.get();
				if(strongTimer != null) {
					taskBlock.execute(strongTimer);
				}
			}
		};
		
		if(repeats) {
			retObj.scheduleAtFixedRate(task, delay, timeInterval);
		} else {
			retObj.schedule(task, delay + timeInterval);
		}
		
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	@FunctionalInterface
	public interface OnFinishBlock
	{
		void execute(@NonNull CountDownTimer timer);
	}
	
	@FunctionalInterface
	public interface OnTickBlock
	{
		void execute(@NonNull CountDownTimer timer, long millisUntilFinished);
	}
	
	@FunctionalInterface
	public interface TaskBlock
	{
		void execute(@NonNull Timer timer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
