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

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
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
		Timber.i("getTasks: ");
		// Respond immediately with cache if available and not dirty
		if (mCachedTasks != null && !mCacheIsDirty) {
			return Flowable.just(new ArrayList<>(mCachedTasks.values()));
		}
		
		if (mCacheIsDirty) {
			// If the cache is dirty we need to fetch new data from the network.
			return mTasksRemoteDataSource.getTasks().map(new Function<List<Task>, List<Task>>() {
				@Override
				public List<Task> apply(List<Task> tasks) throws Exception {
					Timber.i("getTasks mTasksRemoteDataSource: %s", tasks.size());
					refreshCache(tasks);
					refreshLocalDataSource(tasks);
					return new ArrayList<>(mCachedTasks.values());
				}
			});
		} else {
			// Query the local storage if available. If not, query the network.
			return mTasksLocalDataSource.getTasks().flatMap(new Function<List<Task>, Publisher<List<Task>>>() {
				@Override
				public Publisher<List<Task>> apply(List<Task> tasks) throws Exception {
					Timber.i("getTasks mTasksLocalDataSource: %s", tasks.size());
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
	public Completable saveTask(@NonNull Task task) {
		Timber.i("saveTask: %s", task);
		return mTasksRemoteDataSource.saveTask(task)
				.mergeWith(mTasksLocalDataSource.saveTask(task))
				.mergeWith(new Completable() {
					@Override
					protected void subscribeActual(CompletableObserver observer) {
						// Do in memory cache update to keep the app UI up to date
						if (mCachedTasks == null) {
							mCachedTasks = new LinkedHashMap<>();
						}
						mCachedTasks.put(task.getId(), task);
						observer.onComplete();
					}
				});
	}
	
	@Override
	public Completable completeTask(@NonNull Task task) {
		Timber.i("completeTask: %s", task);
		return mTasksRemoteDataSource.completeTask(task)
				.mergeWith(mTasksLocalDataSource.completeTask(task))
				.mergeWith(new Completable() {
					@Override
					protected void subscribeActual(CompletableObserver observer) {
						Task completedTask = new Task(task.getTitle(), task.getDescription(), true, task.getId());
						
						// Do in memory cache update to keep the app UI up to date
						if (mCachedTasks == null) {
							mCachedTasks = new LinkedHashMap<>();
						}
						mCachedTasks.put(task.getId(), completedTask);
						observer.onComplete();
					}
				});
	}
	
	@Override
	public void completeTask(@NonNull String taskId) {
		Timber.i("completeTask: %s", taskId);
		completeTask(getTaskWithId(taskId));
	}
	
	@Override
	public Completable activateTask(@NonNull Task task) {
		Timber.i("activateTask: %s", task);
		return mTasksRemoteDataSource.activateTask(task)
				.mergeWith(mTasksLocalDataSource.activateTask(task))
				.mergeWith(new Completable() {
					@Override
					protected void subscribeActual(CompletableObserver observer) {
						Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
						
						// Do in memory cache update to keep the app UI up to date
						if (mCachedTasks == null) {
							mCachedTasks = new LinkedHashMap<>();
						}
						mCachedTasks.put(task.getId(), activeTask);
						observer.onComplete();
					}
				});
	}
	
	@Override
	public void activateTask(@NonNull String taskId) {
		Timber.i("activateTask: %s", taskId);
		activateTask(getTaskWithId(taskId));
	}
	
	@Override
	public Flowable<Integer> clearCompletedTasks() {
		Timber.i("clearCompletedTasks: ");
		return mTasksRemoteDataSource.clearCompletedTasks()
				.concatMap(new Function<Integer, Publisher<? extends Integer>>() {
					@Override
					public Publisher<? extends Integer> apply(Integer integer) throws Exception {
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
						return null;
					}
				}).concatMap(new Function<Integer, Publisher<? extends Integer>>() {
					@Override
					public Publisher<? extends Integer> apply(Integer integer) throws Exception {
						return mTasksLocalDataSource.clearCompletedTasks();
					}
				});
	}
	
	/**
	 * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
	 * uses the network data source. This is done to simplify the sample.
	 * <p>
	 * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if both data sources fail to
	 * get the data.
	 */
	@Override
	public Flowable<Task> getTask(@NonNull final String taskId) {
		Timber.i("getTask: %s", taskId);
		Task cachedTask = getTaskWithId(taskId);
		
		// Respond immediately with cache if available
		if (cachedTask != null) {
			return Flowable.just(cachedTask);
		}
		
		// Load from server/persisted if needed.
		
		// Is the task in the local data source? If not, query the network.
		return mTasksLocalDataSource.getTask(taskId).flatMap(new Function<Task, Publisher<Task>>() {
			@Override
			public Publisher<Task> apply(Task task) throws Exception {
				if (task != null) {
					if (mCachedTasks == null) {
						mCachedTasks = new LinkedHashMap<>();
					}
					mCachedTasks.put(task.getId(), task);
					return Flowable.just(task);
				} else {
					return mTasksRemoteDataSource.getTask(taskId).map(new Function<Task, Task>() {
						@Override
						public Task apply(Task task) throws Exception {
							if (task != null) {
								// Do in memory cache update to keep the app UI up to date
								if (mCachedTasks == null) {
									mCachedTasks = new LinkedHashMap<>();
								}
								mCachedTasks.put(task.getId(), task);
							}
							return task;
						}
					});
				}
			}
		});
	}
	
	@Override
	public void refreshTasks() {
		Timber.i("refreshTasks: ");
		mCacheIsDirty = true;
	}
	
	@Override
	public Completable deleteAllTasks() {
		Timber.i("deleteAllTasks: ");
		return mTasksRemoteDataSource.deleteAllTasks()
				.mergeWith(mTasksLocalDataSource.deleteAllTasks())
				.mergeWith(new Completable() {
					@Override
					protected void subscribeActual(CompletableObserver observer) {
						if (mCachedTasks == null) {
							mCachedTasks = new LinkedHashMap<>();
						}
						mCachedTasks.clear();
						observer.onComplete();
					}
				});
	}
	
	@Override
	public Flowable<Integer> deleteTask(@NonNull String taskId) {
		Timber.i("deleteTask: " + taskId);
		return mTasksRemoteDataSource.deleteTask(taskId)
				.concatMap(new Function<Integer, Publisher<? extends Integer>>() {
					@Override
					public Publisher<? extends Integer> apply(Integer integer) throws Exception {
						mCachedTasks.remove(taskId);
						return Flowable.just(1);
					}
				})
				.concatMap(new Function<Integer, Publisher<? extends Integer>>() {
					@Override
					public Publisher<? extends Integer> apply(Integer integer) throws Exception {
						return mTasksLocalDataSource.deleteTask(taskId);
					}
				});
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
		mTasksLocalDataSource.deleteAllTasks().subscribe().dispose();
		for (Task task : tasks) {
			mTasksLocalDataSource.saveTask(task).subscribe().dispose();
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
