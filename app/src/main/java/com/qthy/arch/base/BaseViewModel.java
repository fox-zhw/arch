package com.qthy.arch.base;

import androidx.lifecycle.ViewModel;

import com.qthy.arch.util.RxUtils;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author 52512
 * @date 2020/12/13
 */
public class BaseViewModel extends ViewModel {
	final CompositeDisposable compositeDisposable = new CompositeDisposable();
	
	public BaseViewModel() {
	}
	
	public <T> void addDisposable(Flowable<T> flowable, Consumer<T> consumer) {
		try {
			compositeDisposable.add(flowable
					.compose(RxUtils.FlowableTransformer())
					.subscribe(consumer));
		} catch (Exception e) {
			Timber.e(e);
		}
	}
	
	public <T> void addDisposable(Completable completable, Action action) {
		compositeDisposable.add(completable
				.compose(RxUtils.CompletableTransformer())
				.subscribe(action));
	}
	
	@Override
	protected void onCleared() {
		compositeDisposable.clear();
		super.onCleared();
	}
}
