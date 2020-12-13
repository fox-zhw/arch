package com.qthy.arch.util;

import org.reactivestreams.Publisher;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author 52512
 * @date 2020/12/13
 */
public class RxUtils {
	
	/**
	 * 统一线程处理
	 * @param <T> 指定的泛型类型
	 * @return FlowableTransformer
	 */
	public static <T> FlowableTransformer<T, T> FlowableTransformer() {
		return new FlowableTransformer<T, T>() {
			@Override
			public Publisher<T> apply(Flowable<T> upstream) {
				return upstream.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread());
			}
		};
	}
	
	public static CompletableTransformer CompletableTransformer() {
		return new CompletableTransformer() {
			@Override
			public CompletableSource apply(Completable upstream) {
				return upstream.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread());
			}
		};
	}
	
	/**
	 * 统一线程处理
	 * @param <T> 指定的泛型类型
	 * @return ObservableTransformer
	 */
	public static <T> ObservableTransformer<T, T> ObservableTransformer() {
		return new ObservableTransformer<T, T>() {
			@Override
			public ObservableSource<T> apply(Observable<T> upstream) {
				return upstream.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread());
			}
		};
	}
}
