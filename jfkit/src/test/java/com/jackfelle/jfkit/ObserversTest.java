package com.jackfelle.jfkit;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;

public class ObserversTest extends TestCase
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private @Nullable Observers<Observer> observers;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	public void setUp() throws Exception {
		super.setUp();
		this.observers = new Observers<>();
	}
	
	public void tearDown() throws Exception {
		this.observers = null;
		super.tearDown();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Tests
	
	public void testAddAndRemove() {
		Observers<Observer> observers = Objects.requireNonNull(this.observers);
		TestCase.assertEquals("List not empty at start?", 0, observers.size());
		Observer observer = new Observer() {};
		observers.add(observer);
		TestCase.assertEquals("List empty after adding one item?", 1, observers.size());
		observers.add(observer);
		TestCase.assertEquals("Adding the same item twice should not change the list size!", 1, observers.size());
		observers.remove(observer);
		TestCase.assertEquals("List not empty after removing the last item?", 0, observers.size());
		observers.remove(observer);
		TestCase.assertEquals("Removing the same item twice should not change the list size!", 0, observers.size());
	}
	
	public void testNotify() {
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection") List<Observer> holder = new ArrayList<>();
		Observers<Observer> observers = Objects.requireNonNull(this.observers);
		int[] count = new int[] {0};
		int size = 10;
		for(int i = 0; i < size; i++) {
			Observer observer = new Observer()
			{
				@Override public void onNotified() {
					count[0]++;
				}
			};
			holder.add(observer);
			observers.add(observer);
		}
		observers.notify(Observer::onNotified);
		TestCase.assertEquals("Not all observers have been notified!", size, count[0]);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Observer
	{
		default void onNotified() {}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}