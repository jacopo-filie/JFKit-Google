package com.jackfelle.jfkit;

import com.jackfelle.jfkit.operations.Operation;

import junit.framework.TestCase;

import java.util.Locale;

import androidx.annotation.NonNull;

public class OperationTest extends TestCase
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Tests
	
	public void testAsynchronousOperation() {
		boolean[] flag = new boolean[] {false};
		AsynchronousOperation operation = new AsynchronousOperation(() -> {
			flag[0] = true;
		});
		this.assertExpectedStates(operation, true, false, false, false, "Asynchronous operation is %s before it's started?");
		TestCase.assertFalse("Test flag is true before operation is started?", flag[0]);
		operation.start();
		this.assertExpectedStates(operation, false, true, false, false, "Asynchronous operation is %s after it's started?");
		TestCase.assertTrue("Test flag is false after operation is finished?", flag[0]);
		operation.finish();
		this.assertExpectedStates(operation, false, false, true, false, "Asynchronous operation is %s before it's finished?");
	}
	
	public void testCanceledOperation() {
		boolean[] flag = new boolean[] {false};
		Operation operation = new SynchronousOperation(() -> {
			flag[0] = true;
		});
		this.assertExpectedStates(operation, true, false, false, false, "Synchronous operation is %s before it's started?");
		TestCase.assertFalse("Test flag is true before operation is started?", flag[0]);
		operation.cancel();
		this.assertExpectedStates(operation, true, false, false, true, "Synchronous operation is %s after it's canceled, before it's started?");
		operation.start();
		this.assertExpectedStates(operation, false, false, true, true, "Synchronous operation is %s after it's canceled and started?");
		TestCase.assertFalse("Test flag is true after operation is canceled and finished?", flag[0]);
	}
	
	public void testCompletion() {
		boolean[] flag = new boolean[] {false, false};
		AsynchronousOperation operation = new AsynchronousOperation(() -> {
			flag[0] = true;
		});
		operation.setCompletion(() -> {
			flag[1] = true;
		});
		TestCase.assertFalse("Test flag[0] is true before operation is started?", flag[0]);
		operation.start();
		TestCase.assertTrue("Test flag[0] is false after operation is started?", flag[0]);
		TestCase.assertFalse("Test flag[1] is true before operation is finished?", flag[1]);
		operation.finish();
		TestCase.assertTrue("Test flag[1] is false after operation is finished?", flag[1]);
	}
	
	public void testDependencies() {
		boolean[] flag = new boolean[] {false, false};
		Operation operation1 = new SynchronousOperation(() -> {
			flag[0] = true;
		});
		Operation operation2 = new SynchronousOperation(() -> {
			flag[1] = true;
		});
		operation2.addDependency(operation1);
		this.assertExpectedStates(operation2, false, false, false, false, "Operation2 is %s before it's started?");
		operation2.start();
		this.assertExpectedStates(operation2, false, false, false, false, "Operation2 is %s after it's started, before operation1 is finished?");
		TestCase.assertFalse("Test flag[0] is true before operation1 is started?", flag[0]);
		operation1.start();
		TestCase.assertTrue("Test flag[0] is false after operation1 is finished?", flag[0]);
		this.assertExpectedStates(operation2, true, false, false, false, "Operation2 is %s before it's started, after operation1 is finished?");
		TestCase.assertFalse("Test flag[1] is true before operation2 is started?", flag[1]);
		operation2.start();
		this.assertExpectedStates(operation2, false, false, true, false, "Operation2 is %s after it's finished?");
		TestCase.assertTrue("Test flag[1] is false after operation2 is finished?", flag[1]);
	}
	
	public void testName() {
		Operation operation = new SynchronousOperation(() -> {});
		TestCase.assertNull("Name is not null before set?", operation.getName());
		String name = "Test name";
		operation.setName(name);
		TestCase.assertNotNull("Name is null before set?", operation.getName());
		TestCase.assertEquals("Name is different?", name, operation.getName());
	}
	
	public void testSynchronousOperation() {
		boolean[] flag = new boolean[] {false};
		Operation operation = new SynchronousOperation(() -> {
			flag[0] = true;
		});
		this.assertExpectedStates(operation, true, false, false, false, "Synchronous operation is %s before it's started?");
		TestCase.assertFalse("Test flag is true before operation is started?", flag[0]);
		operation.start();
		this.assertExpectedStates(operation, false, false, true, false, "Synchronous operation is %s after it's started?");
		TestCase.assertTrue("Test flag is false after operation is finished?", flag[0]);
	}
	
	public void testWaitUntilFinished() {
		boolean[] flag = new boolean[] {false};
		Operation operation = new SynchronousOperation(() -> {
			flag[0] = true;
		});
		Thread thread = new Thread(operation::waitUntilFinished);
		thread.start();
		TestCase.assertFalse("Test flag is true before operation is started?", flag[0]);
		TestCase.assertTrue("Test thread is not alive?", thread.isAlive());
		operation.start();
		TestCase.assertTrue("Test flag is false after operation is finished?", flag[0]);
		try {
			thread.join(1000);
		} catch(InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
		TestCase.assertFalse("Test thread is still alive?", thread.isAlive());
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods
	
	private void assertExpectedStates(@NonNull Operation operation, boolean isReady, boolean isExecuting, boolean isFinished, boolean isCanceled, @NonNull String format) {
		TestCase.assertEquals(String.format(Locale.US, format, isReady ? "not ready" : "ready"), isReady, operation.isReady());
		TestCase.assertEquals(String.format(Locale.US, format, isExecuting ? "not executing" : "executing"), isExecuting, operation.isExecuting());
		TestCase.assertEquals(String.format(Locale.US, format, isFinished ? "not finished" : "finished"), isFinished, operation.isFinished());
		TestCase.assertEquals(String.format(Locale.US, format, isCanceled ? "not canceled" : "canceled"), isCanceled, operation.isCanceled());
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	public static class AsynchronousOperation extends SynchronousOperation
	{
		@Override public boolean isAsynchronous() {
			return true;
		}
		
		public AsynchronousOperation(@NonNull Runnable runnable) {
			super(runnable);
		}
		
		@Override public void finish() {
			super.finish();
		}
	}
	
	public static class SynchronousOperation extends Operation
	{
		public final @NonNull Runnable runnable;
		
		public SynchronousOperation(@NonNull Runnable runnable) {
			super();
			this.runnable = runnable;
		}
		
		@Override protected void main() {
			this.runnable.run();
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}