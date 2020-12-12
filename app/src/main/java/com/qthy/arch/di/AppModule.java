package com.qthy.arch.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.data.source.local.TasksDao;
import com.qthy.arch.data.source.local.TasksLocalDataSource;
import com.qthy.arch.data.source.local.ToDoDatabase;
import com.qthy.arch.data.source.remote.TasksRemoteDataSource;
import com.qthy.arch.util.AppExecutors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * @author zhaohw
 * @date 2020/12/11
 */
@Module
@InstallIn(ApplicationComponent.class)
public class AppModule {
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface BindLocalDataSource {
	}
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface BindRemoteDataSource {
	}
	
	@Singleton
	@BindRemoteDataSource
	@Provides
	TasksDataSource provideTasksRemoteDataSource() {
		return new TasksRemoteDataSource();
	}
	
	@Singleton
	@BindLocalDataSource
	@Provides
	TasksDataSource provideTasksRepository(AppExecutors appExecutors,
	                                       @NonNull TasksDao tasksDao) {
		return new TasksLocalDataSource(appExecutors, tasksDao);
	}
	
	@Singleton
	@Provides
	TasksDao provideDataBase(@ApplicationContext Context context) {
		return Room.databaseBuilder(context.getApplicationContext(),
				ToDoDatabase.class, "Tasks.db")
				.build().taskDao();
	}
	
	@Provides
	AppExecutors provideAppExecutors() {
		return new AppExecutors();
	}
}
