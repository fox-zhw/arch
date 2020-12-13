package com.qthy.arch.data.source;

import androidx.annotation.NonNull;


import com.qthy.arch.data.Task;

import java.util.List;

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
	
	void getTask(@NonNull String taskId, @NonNull GetTaskCallback callback);
	
	void saveTask(@NonNull Task task);
	
	void completeTask(@NonNull Task task);
	
	void completeTask(@NonNull String taskId);
	
	void activateTask(@NonNull Task task);
	
	void activateTask(@NonNull String taskId);
	
	void clearCompletedTasks();
	
	void refreshTasks();
	
	void deleteAllTasks();
	
	void deleteTask(@NonNull String taskId);
}
