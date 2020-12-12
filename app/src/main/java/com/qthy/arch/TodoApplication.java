package com.qthy.arch;

import android.app.Application;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

/**
 * @author zhaohw
 * @date 2020/12/8
 */
@HiltAndroidApp
public class TodoApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree() {
			@Override
			protected void log(int priority, String tag, @NotNull String message, Throwable t) {
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				int index = 7;
				String className = stackTrace[index].getFileName();
				int lineNumber = stackTrace[index].getLineNumber();
				StringBuilder sbMsg = new StringBuilder();
				sbMsg.append("[")
						.append("-zhw-")
						.append("] ")
						.append("(")
						.append(className)
						.append(":")
						.append(lineNumber)
						.append(") ")
						.append(message);
				
				super.log(priority, tag, sbMsg.toString(), t);
			}
		});
		Timber.i("app start");
	}
}
