package com.qthy.arch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * @author zhaohw
 * @date 2020/8/17
 */
public class Event<T> {
	
	private T mContent;
	
	private boolean hasBeenHandled = false;
	
	public Event(T content) {
		if (content == null) {
			throw new IllegalArgumentException("null values in Event are not allowed.");
		}
		mContent = content;
	}
	
	public T getContentIfNotHandled() {
		if (hasBeenHandled) {
			return null;
		} else {
			hasBeenHandled = true;
			return mContent;
		}
	}
	
	public boolean hasBeenHandled() {
		return hasBeenHandled;
	}
	
	/**
	 * Used as a wrapper for Observer that is exposed via a LiveData that represents an event.
	 *
	 * @param <T>
	 */
	public static abstract class EventObserver<T> implements Observer<Event<T>> {
		
		public abstract void onEventChanged(@NonNull T t);
		
		@Override
		public void onChanged(@Nullable Event<T> tEvent) {
			if (tEvent != null) {
				T t = tEvent.getContentIfNotHandled();
				if (t != null) onEventChanged(t);
			}
		}
	}
}
