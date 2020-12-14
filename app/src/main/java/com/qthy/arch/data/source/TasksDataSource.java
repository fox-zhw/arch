package com.qthy.arch.data.source;

import androidx.annotation.NonNull;


import com.qthy.arch.data.Task;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
public interface TasksDataSource {
	
	interface LoadTasksCallback {
		
		void onTasksLoaded(List<Task> tasks);
		
		void onDataNotAvailable();
	}
	
	interface GetTaskCallback {
		
		void onTaskLoaded(Task task);
		
		void onDataNotAvailable();
	}
	
	Flowable<List<Task>> getTasks();
	
	Flowable<Task> getTask(@NonNull String taskId);
	
	Completable saveTask(@NonNull Task task);
	
	Completable completeTask(@NonNull Task task);
	
	void completeTask(@NonNull String taskId);
	
	Completable activateTask(@NonNull Task task);
	
	void activateTask(@NonNull String taskId);
	
	Flowable<Integer> clearCompletedTasks();
	
	void refreshTasks();
	
	Completable deleteAllTasks();
	
	Flowable<Integer> deleteTask(@NonNull String taskId);
}
