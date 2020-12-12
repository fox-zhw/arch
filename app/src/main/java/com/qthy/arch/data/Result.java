package com.qthy.arch.data;

import androidx.annotation.NonNull;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
public class Result<R> {
	
	public static class Success<T> extends Result<T> {
		T data;
		
		public Success(T data) {
			this.data = data;
		}
	}
	
	public static class Error<T> extends Result<T> {
		Exception exception;
		
		public Error(Exception exception) {
			this.exception = exception;
		}
	}
	
	public class Loading<T> extends Result<T> {
	
	}
	
	@NonNull
	@Override
	public String toString() {
		if (this instanceof Success) {
			return String.format("Success[%s]", ((Success)this).data);
		} else if (this instanceof Error) {
			return String.format("Error[%s]", ((Error)this).exception);
		} else if (this instanceof Loading) {
			return "Loading";
		}
		return "unknown";
	}
}
