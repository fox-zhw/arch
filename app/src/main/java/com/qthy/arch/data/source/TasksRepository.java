package com.qthy.arch.data.source;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qthy.arch.data.Task;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
public class TasksRepository implements TasksDataSource {
	
	private final TasksDataSource mTasksRemoteDataSource;
	
	private final TasksDataSource mTasksLocalDataSource;
	
	/**
	 * This variable has package local visibility so it can be accessed from tests.
	 */
	Map<String, Task> mCachedTasks;
	
	/**
	 * Marks the cache as invalid, to force an update the next time data is requested. This variable
	 * has package local visibility so it can be accessed from tests.
	 */
	private boolean mCacheIsDirty = false;
	
	// Prevent direct instantiation.
	@Inject
	public TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
	                        @NonNull TasksDataSource tasksLocalDataSource) {
		Timber.i("TasksRepository: ");
		mTasksRemoteDataSource = tasksRemoteDataSource;
		mTasksLocalDataSource = tasksLocalDataSource;
	}
	
	/**
	 * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
	 * available first.
	 * <p>
	 * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if all data sources fail to
	 * get the data.
	 */
	@Override
	public Flowable<List<Task>> getTasks() {
		
		// Respond immediately with cache if available and not dirty
		if (mCachedTasks != null && !mCacheIsDirty) {
			return Flowable.just(new ArrayList<>(mCachedTasks.values()));
		}
		
		if (mCacheIsDirty) {
			// If the cache is dirty we need to fetch new data from the network.
			return mTasksRemoteDataSource.getTasks().map(new Function<List<Task>, List<Task>>() {
				@Override
				public List<Task> apply(List<Task> tasks) throws Exception {
					refreshCache(tasks);
					refreshLocalDataSource(tasks);
					return new ArrayList<>(mCachedTasks.values());
				}
			});
		} else {
			// Query the local storage if available. If not, query the network.
			return mTasksLocalDataSource.getTasks().switchMap(new Function<List<Task>, Publisher<List<Task>>>() {
				@Override
				public Publisher<List<Task>> apply(List<Task> tasks) throws Exception {
					if (tasks.isEmpty()) {
						return mTasksRemoteDataSource.getTasks();
					} else {
						refreshCache(tasks);
						return Flowable.just(new ArrayList<>(mCachedTasks.values()));
					}
				}
			});
		}
	}
	
	@Override
	public void saveTask(@NonNull Task task) {
		mTasksRemoteDataSource.saveTask(task);
		mTasksLocalDataSource.saveTask(task);
		
		// Do in memory cache update to keep the app UI up to date
		if (mCachedTasks == null) {
			mCachedTasks = new LinkedHashMap<>();
		}
		mCachedTasks.put(task.getId(), task);
	}
	
	@Override
	public void completeTask(@NonNull Task task) {
		mTasksRemoteDataSource.completeTask(task);
		mTasksLocalDataSource.completeTask(task);
		
		Task completedTask = new Task(task.getTitle(), task.getDescription(), true, task.getId());
		
		// Do in memory cache update to keep the app UI up to date
		if (mCachedTasks == null) {
			mCachedTasks = new LinkedHashMap<>();
		}
		mCachedTasks.put(task.getId(), completedTask);
	}
	
	@Override
	public void completeTask(@NonNull String taskId) {
		completeTask(getTaskWithId(taskId));
	}
	
	@Override
	public void activateTask(@NonNull Task task) {
		mTasksRemoteDataSource.activateTask(task);
		mTasksLocalDataSource.activateTask(task);
		
		Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
		
		// Do in memory cache update to keep the app UI up to date
		if (mCachedTasks == null) {
			mCachedTasks = new LinkedHashMap<>();
		}
		mCachedTasks.put(task.getId(), activeTask);
	}
	
	@Override
	public void activateTask(@NonNull String taskId) {
		activateTask(getTaskWithId(taskId));
	}
	
	@Override
	public void clearCompletedTasks() {
		mTasksRemoteDataSource.clearCompletedTasks();
		mTasksLocalDataSource.clearCompletedTasks();
		
		// Do in memory cache update to keep the app UI up to date
		if (mCachedTasks == null) {
			mCachedTasks = new LinkedHashMap<>();
		}
		Iterator<Map.Entry<String, Task>> it = mCachedTasks.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Task> entry = it.next();
			if (entry.getValue().isCompleted()) {
				it.remove();
			}
		}
	}
	
	/**
	 * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
	 * uses the network data source. This is done to simplify the sample.
	 * <p>
	 * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if both data sources fail to
	 * get the data.
	 */
	@Override
	public void getTask(@NonNull final String taskId, @NonNull final GetTaskCallback callback) {
		
		Task cachedTask = getTaskWithId(taskId);
		
		// Respond immediately with cache if available
		if (cachedTask != null) {
			callback.onTaskLoaded(cachedTask);
			return;
		}
		
		// Load from server/persisted if needed.
		
		// Is the task in the local data source? If not, query the network.
		mTasksLocalDataSource.getTask(taskId, new GetTaskCallback() {
			@Override
			public void onTaskLoaded(Task task) {
				// Do in memory cache update to keep the app UI up to date
				if (mCachedTasks == null) {
					mCachedTasks = new LinkedHashMap<>();
				}
				mCachedTasks.put(task.getId(), task);
				
				callback.onTaskLoaded(task);
			}
			
			@Override
			public void onDataNotAvailable() {
				mTasksRemoteDataSource.getTask(taskId, new GetTaskCallback() {
					@Override
					public void onTaskLoaded(Task task) {
						if (task == null) {
							onDataNotAvailable();
							return;
						}
						// Do in memory cache update to keep the app UI up to date
						if (mCachedTasks == null) {
							mCachedTasks = new LinkedHashMap<>();
						}
						mCachedTasks.put(task.getId(), task);
						
						callback.onTaskLoaded(task);
					}
					
					@Override
					public void onDataNotAvailable() {
						
						callback.onDataNotAvailable();
					}
				});
			}
		});
	}
	
	@Override
	public void refreshTasks() {
		mCacheIsDirty = true;
	}
	
	@Override
	public void deleteAllTasks() {
		mTasksRemoteDataSource.deleteAllTasks();
		mTasksLocalDataSource.deleteAllTasks();
		
		if (mCachedTasks == null) {
			mCachedTasks = new LinkedHashMap<>();
		}
		mCachedTasks.clear();
	}
	
	@Override
	public void deleteTask(@NonNull String taskId) {
		mTasksRemoteDataSource.deleteTask(taskId);
		mTasksLocalDataSource.deleteTask(taskId);
		
		mCachedTasks.remove(taskId);
	}
	
	private void refreshCache(List<Task> tasks) {
		if (mCachedTasks == null) {
			mCachedTasks = new LinkedHashMap<>();
		}
		mCachedTasks.clear();
		for (Task task : tasks) {
			mCachedTasks.put(task.getId(), task);
		}
		mCacheIsDirty = false;
	}
	
	private void refreshLocalDataSource(List<Task> tasks) {
		mTasksLocalDataSource.deleteAllTasks();
		for (Task task : tasks) {
			mTasksLocalDataSource.saveTask(task);
		}
	}
	
	@Nullable
	private Task getTaskWithId(@NonNull String id) {
		if (mCachedTasks == null || mCachedTasks.isEmpty()) {
			return null;
		} else {
			return mCachedTasks.get(id);
		}
	}
}
