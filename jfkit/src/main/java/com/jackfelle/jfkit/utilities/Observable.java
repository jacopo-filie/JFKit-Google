//
//	The MIT License (MIT)
//
//	Copyright © 2020-2024 Jacopo Filié
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

package com.jackfelle.jfkit.utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Observable <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Fields - Data
	
	private final @NonNull ParameterizedLazy<Observable.Implementation<T>, Observable<T>> implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	public @NonNull T get() {
		return this.getImplementation().getValue();
	}
	
	public void set(@NonNull T value) {
		this.getImplementation().setValue(value);
	}
	
	private @NonNull Observable.Implementation<T> getImplementation() {
		return this.implementation.get(this);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public static <T> Observable<T> newInstance(@NonNull T value) {
		return new Observable<>(ParameterizedLazy.newInstance(param -> new Implementation<>(param, value)));
	}
	
	public static <T> Observable<T> newSynchronizedInstance(@NonNull T value) {
		return new Observable<>(ParameterizedLazy.newSynchronizedInstance(param -> new SynchronizedImplementation<>(param, value)));
	}
	
	private Observable(@NonNull ParameterizedLazy<Observable.Implementation<T>, Observable<T>> implementation) {
		super();
		
		this.implementation = implementation;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers
	
	public void addNotificationBlock(@NonNull NotificationBlock<T> notificationBlock) {
		this.getImplementation().getNotificationBlocks().add(notificationBlock);
	}
	
	public void addObserver(@NonNull Observer<T> observer) {
		this.getImplementation().getObservers().addObserver(observer);
	}
	
	public void removeNotificationBlock(@NonNull NotificationBlock<T> notificationBlock) {
		this.getImplementation().getNotificationBlocks().remove(notificationBlock);
	}
	
	public void removeObserver(@NonNull Observer<T> observer) {
		this.getImplementation().getObservers().removeObserver(observer);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface NotificationBlock <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		void onValueChanged(@NonNull T newVal, @NonNull T oldVal);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public interface Observer <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		void onValueChanged(@NonNull Observable<T> sender, @NonNull T newVal, @NonNull T oldVal);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	private static class Implementation <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Data
		
		protected final @NonNull WeakReference<Observable<T>> owner;
		protected @NonNull T value;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Observers
		
		private final @NonNull Lazy<List<NotificationBlock<T>>> notificationBlocks;
		private final @NonNull Lazy<ObserversController<Observer<T>>> observers;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Data
		
		public @Nullable Observable<T> getOwner() {
			return this.owner.get();
		}
		
		public @NonNull T getValue() {
			return this.value;
		}
		
		public void setValue(@NonNull T value) {
			T old = this.value;
			if(Utilities.areObjectsEqual(old, value)) {
				return;
			}
			
			this.value = value;
			
			this.executeNotificationBlocks(this.getNotificationBlocks(), value, old);
			this.notifyObservers(value, old);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Observers
		
		public @NonNull List<NotificationBlock<T>> getNotificationBlocks() {
			return this.notificationBlocks.get();
		}
		
		public @NonNull ObserversController<Observer<T>> getObservers() {
			return this.observers.get();
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public Implementation(@NonNull Observable<T> owner, @NonNull T value) {
			this(owner, value, Lazy.newInstance(ArrayList::new), Lazy.newInstance(ObserversController::new));
		}
		
		protected Implementation(@NonNull Observable<T> owner, @NonNull T value, @NonNull Lazy<List<NotificationBlock<T>>> notificationBlocks, @NonNull Lazy<ObserversController<Observer<T>>> observers) {
			super();
			
			this.notificationBlocks = notificationBlocks;
			this.observers = observers;
			this.owner = new WeakReference<>(owner);
			this.value = value;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Observers
		
		protected void executeNotificationBlocks(@NonNull List<NotificationBlock<T>> notificationBlocks, @NonNull T newVal, @NonNull T oldVal) {
			for(NotificationBlock<T> block : notificationBlocks) {
				block.onValueChanged(newVal, oldVal);
			}
		}
		
		protected void notifyObservers(@NonNull T newVal, @NonNull T oldVal) {
			Observable<T> owner = this.getOwner();
			if(owner != null) {
				this.getObservers().notifyObservers(observer -> observer.onValueChanged(owner, newVal, oldVal), false);
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T> extends Observable.Implementation<T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Data
		
		@Override public synchronized @NonNull T getValue() {
			return super.getValue();
		}
		
		@Override public synchronized void setValue(@NonNull T value) {
			super.setValue(value);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public SynchronizedImplementation(@NonNull Observable<T> owner, @NonNull T value) {
			super(owner, value, Lazy.newSynchronizedInstance(() -> Collections.synchronizedList(new ArrayList<>())), Lazy.newSynchronizedInstance(ObserversController::new));
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Observers
		
		@Override protected void executeNotificationBlocks(@NonNull List<NotificationBlock<T>> notificationBlocks, @NonNull T newVal, @NonNull T oldVal) {
			synchronized(notificationBlocks) {
				super.executeNotificationBlocks(notificationBlocks, newVal, oldVal);
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
