package com.qthy.arch.data.source.local;

import androidx.annotation.NonNull;


import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.util.AppExecutors;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

import static kotlin.jvm.internal.Intrinsics.checkNotNull;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
public class TasksLocalDataSource implements TasksDataSource {
	
	private static volatile TasksLocalDataSource INSTANCE;
	
	private TasksDao mTasksDao;
	
	private AppExecutors mAppExecutors;
	
	// Prevent direct instantiation.
	@Inject
	public TasksLocalDataSource(@NonNull AppExecutors appExecutors,
	                            @NonNull TasksDao tasksDao) {
		Timber.i("TasksLocalDataSource: ");
		mAppExecutors = appExecutors;
		mTasksDao = tasksDao;
	}
	
	/**
	 * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if the database doesn't exist
	 * or the table is empty.
	 */
	@Override
	public void getTasks(@NonNull final LoadTasksCallback callback) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				final List<Task> tasks = mTasksDao.getTasks();
				mAppExecutors.mainThread().execute(new Runnable() {
					@Override
					public void run() {
						if (tasks.isEmpty()) {
							// This will be called if the table is new or just empty.
							callback.onDataNotAvailable();
						} else {
							callback.onTasksLoaded(tasks);
						}
					}
				});
			}
		};
		
		mAppExecutors.diskIO().execute(runnable);
	}
	
	/**
	 * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if the {@link Task} isn't
	 * found.
	 */
	@Override
	public void getTask(@NonNull final String taskId, @NonNull final GetTaskCallback callback) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				final Task task = mTasksDao.getTaskById(taskId);
				
				mAppExecutors.mainThread().execute(new Runnable() {
					@Override
					public void run() {
						if (task != null) {
							callback.onTaskLoaded(task);
						} else {
							callback.onDataNotAvailable();
						}
					}
				});
			}
		};
		
		mAppExecutors.diskIO().execute(runnable);
	}
	
	@Override
	public void saveTask(@NonNull final Task task) {
		checkNotNull(task);
		Runnable saveRunnable = new Runnable() {
			@Override
			public void run() {
				mTasksDao.insertTask(task);
			}
		};
		mAppExecutors.diskIO().execute(saveRunnable);
	}
	
	@Override
	public void completeTask(@NonNull final Task task) {
		Runnable completeRunnable = new Runnable() {
			@Override
			public void run() {
				mTasksDao.updateCompleted(task.getId(), true);
			}
		};
		
		mAppExecutors.diskIO().execute(completeRunnable);
	}
	
	@Override
	public void completeTask(@NonNull String taskId) {
		// Not required for the local data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public void activateTask(@NonNull final Task task) {
		Runnable activateRunnable = new Runnable() {
			@Override
			public void run() {
				mTasksDao.updateCompleted(task.getId(), false);
			}
		};
		mAppExecutors.diskIO().execute(activateRunnable);
	}
	
	@Override
	public void activateTask(@NonNull String taskId) {
		// Not required for the local data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public void clearCompletedTasks() {
		Runnable clearTasksRunnable = new Runnable() {
			@Override
			public void run() {
				mTasksDao.deleteCompletedTasks();
				
			}
		};
		
		mAppExecutors.diskIO().execute(clearTasksRunnable);
	}
	
	@Override
	public void refreshTasks() {
		// Not required because the {@link TasksRepository} handles the logic of refreshing the
		// tasks from all the available data sources.
	}
	
	@Override
	public void deleteAllTasks() {
		Runnable deleteRunnable = new Runnable() {
			@Override
			public void run() {
				mTasksDao.deleteTasks();
			}
		};
		
		mAppExecutors.diskIO().execute(deleteRunnable);
	}
	
	@Override
	public void deleteTask(@NonNull final String taskId) {
		Runnable deleteRunnable = new Runnable() {
			@Override
			public void run() {
				mTasksDao.deleteTaskById(taskId);
			}
		};
		
		mAppExecutors.diskIO().execute(deleteRunnable);
	}
}
