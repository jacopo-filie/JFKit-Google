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

public class ObservableNullable <T>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Fields - Data
	
	private final @NonNull ParameterizedLazy<ObservableNullable.Implementation<T>, ObservableNullable<T>> implementation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	public @Nullable T get() {
		return this.getImplementation().getValue();
	}
	
	public void set(@Nullable T value) {
		this.getImplementation().setValue(value);
	}
	
	private @NonNull ObservableNullable.Implementation<T> getImplementation() {
		return this.implementation.get(this);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory
	
	public static <T> ObservableNullable<T> newInstance() {
		return ObservableNullable.newInstance(null);
	}
	
	public static <T> ObservableNullable<T> newInstance(@Nullable T value) {
		return new ObservableNullable<>(ParameterizedLazy.newInstance(param -> new Implementation<>(param, value)));
	}
	
	public static <T> ObservableNullable<T> newSynchronizedInstance() {
		return ObservableNullable.newSynchronizedInstance(null);
	}
	
	public static <T> ObservableNullable<T> newSynchronizedInstance(@Nullable T value) {
		return new ObservableNullable<>(ParameterizedLazy.newSynchronizedInstance(param -> new SynchronizedImplementation<>(param, value)));
	}
	
	private ObservableNullable(@NonNull ParameterizedLazy<ObservableNullable.Implementation<T>, ObservableNullable<T>> implementation) {
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
		
		void onValueChanged(@Nullable T newVal, @Nullable T oldVal);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public interface Observer <T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Data
		
		void onValueChanged(@NonNull ObservableNullable<T> sender, @Nullable T newVal, @Nullable T oldVal);
		
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
		
		protected final @NonNull WeakReference<ObservableNullable<T>> owner;
		protected @Nullable T value;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Observers
		
		private final @NonNull Lazy<List<NotificationBlock<T>>> notificationBlocks;
		private final @NonNull Lazy<ObserversController<Observer<T>>> observers;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Data
		
		public @Nullable ObservableNullable<T> getOwner() {
			return this.owner.get();
		}
		
		public @Nullable T getValue() {
			return this.value;
		}
		
		public void setValue(@Nullable T value) {
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
		
		public Implementation(@NonNull ObservableNullable<T> owner, @Nullable T value) {
			this(owner, value, Lazy.newInstance(ArrayList::new), Lazy.newInstance(ObserversController::new));
		}
		
		protected Implementation(@NonNull ObservableNullable<T> owner, @Nullable T value, @NonNull Lazy<List<NotificationBlock<T>>> notificationBlocks, @NonNull Lazy<ObserversController<Observer<T>>> observers) {
			super();
			
			this.notificationBlocks = notificationBlocks;
			this.observers = observers;
			this.owner = new WeakReference<>(owner);
			this.value = value;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Observers
		
		protected void executeNotificationBlocks(@NonNull List<NotificationBlock<T>> notificationBlocks, @Nullable T newVal, @Nullable T oldVal) {
			for(NotificationBlock<T> block : notificationBlocks) {
				block.onValueChanged(newVal, oldVal);
			}
		}
		
		protected void notifyObservers(@Nullable T newVal, @Nullable T oldVal) {
			ObservableNullable<T> owner = this.getOwner();
			if(owner != null) {
				this.getObservers().notifyObservers(observer -> observer.onValueChanged(owner, newVal, oldVal), false);
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private static class SynchronizedImplementation <T> extends ObservableNullable.Implementation<T>
	{
		////////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Data
		
		@Override public synchronized @Nullable T getValue() {
			return super.getValue();
		}
		
		@Override public synchronized void setValue(@Nullable T value) {
			super.setValue(value);
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Memory
		
		public SynchronizedImplementation(@NonNull ObservableNullable<T> owner, @Nullable T value) {
			super(owner, value, Lazy.newSynchronizedInstance(() -> Collections.synchronizedList(new ArrayList<>())), Lazy.newSynchronizedInstance(ObserversController::new));
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Observers
		
		@Override protected void executeNotificationBlocks(@NonNull List<NotificationBlock<T>> notificationBlocks, @Nullable T newVal, @Nullable T oldVal) {
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
