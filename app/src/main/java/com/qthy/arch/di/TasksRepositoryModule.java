package com.qthy.arch.di;

import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.data.source.TasksRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;

/**
 * @author zhaohw
 * @date 2020/12/11
 */
@Module
@InstallIn(ApplicationComponent.class)
public class TasksRepositoryModule {
	
	@Singleton
	@Provides
	TasksRepository provideTasksRepository(
			@AppModule.BindRemoteDataSource TasksDataSource tasksRemoteDataSource,
			@AppModule.BindLocalDataSource TasksDataSource tasksLocalDataSource) {
		return new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource);
	}
}
