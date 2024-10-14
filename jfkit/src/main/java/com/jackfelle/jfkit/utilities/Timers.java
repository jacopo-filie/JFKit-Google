package com.jackfelle.jfkit.utilities;

import android.os.CountDownTimer;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

public abstract class Timers
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods
	
	@MainThread public static CountDownTimer newCountDownTimer(long timeInterval, @NonNull OnFinishBlock finishBlock) {
		return new CountDownTimer(timeInterval, timeInterval)
		{
			@Override public void onTick(long millisUntilFinished) {
				// Nothing to do.
			}
			
			@Override public void onFinish() {
				finishBlock.execute(this);
			}
		};
	}
	
	@MainThread public static CountDownTimer newCountDownTimer(long timeInterval, @NonNull OnFinishBlock finishBlock, long tickInterval, @NonNull OnTickBlock tickBlock) {
		return new CountDownTimer(timeInterval, tickInterval)
		{
			@Override public void onTick(long millisUntilFinished) {
				tickBlock.execute(this, millisUntilFinished);
			}
			
			@Override public void onFinish() {
				finishBlock.execute(this);
			}
		};
	}
	
	@MainThread public static Timer newScheduledTimer(long timeInterval, @NonNull TaskBlock taskBlock) {
		return Timers.newScheduledTimer(timeInterval, 0, false, taskBlock);
	}
	
	@MainThread public static Timer newScheduledTimer(long timeInterval, boolean repeats, @NonNull TaskBlock taskBlock) {
		return Timers.newScheduledTimer(timeInterval, 0, repeats, taskBlock);
	}
	
	@MainThread public static Timer newScheduledTimer(long timeInterval, long delay, boolean repeats, @NonNull TaskBlock taskBlock) {
		Timer retObj = new Timer();
		WeakReference<Timer> weakTimer = new WeakReference<>(retObj);
		TimerTask task = new TimerTask()
		{
			@Override public void run() {
				Utilities.unwrapObjectAndExecuteBlock(weakTimer, taskBlock::execute);
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
	
	public interface OnFinishBlock
	{
		void execute(@NonNull CountDownTimer timer);
	}
	
	public interface OnTickBlock
	{
		void execute(@NonNull CountDownTimer timer, long millisUntilFinished);
	}
	
	public interface TaskBlock
	{
		void execute(@NonNull Timer timer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
