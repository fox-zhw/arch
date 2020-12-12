package com.qthy.arch.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * @author zhaohw
 * @date 2020/12/3
 */
public class AppExecutors {
	private static final int THREAD_COUNT = 3;
	
	private final Executor diskIO;
	
	private final Executor networkIO;
	
	private final Executor mainThread;
	
	AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
		this.diskIO = diskIO;
		this.networkIO = networkIO;
		this.mainThread = mainThread;
	}
	
	@Inject
	public AppExecutors() {
		this(new DiskIOThreadExecutor(), Executors.newFixedThreadPool(THREAD_COUNT),
				new MainThreadExecutor());
		Timber.i("AppExecutors: ");
	}
	
	public Executor diskIO() {
		return diskIO;
	}
	
	public Executor networkIO() {
		return networkIO;
	}
	
	public Executor mainThread() {
		return mainThread;
	}
	
	private static class MainThreadExecutor implements Executor {
		private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
		
		@Override
		public void execute(@NonNull Runnable command) {
			mainThreadHandler.post(command);
		}
	}
	
	private static class DiskIOThreadExecutor implements Executor {
		
		private final Executor mDiskIO;
		
		public DiskIOThreadExecutor() {
			mDiskIO = Executors.newSingleThreadExecutor();
		}
		
		@Override
		public void execute(@NonNull Runnable command) {
			mDiskIO.execute(command);
		}
	}
}
