package com.qthy.arch.data.source.local;

import androidx.annotation.NonNull;

import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.util.RxUtils;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
public class TasksLocalDataSource implements TasksDataSource {
	
	private TasksDao mTasksDao;
	
	// Prevent direct instantiation.
	@Inject
	public TasksLocalDataSource(@NonNull TasksDao tasksDao) {
		Timber.i("TasksLocalDataSource: ");
		mTasksDao = tasksDao;
	}
	
	/**
	 * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if the database doesn't exist
	 * or the table is empty.
	 */
	@Override
	public Flowable<List<Task>> getTasks() {
		return mTasksDao.getTasks();
	}
	
	/**
	 * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if the {@link Task} isn't
	 * found.
	 */
	@Override
	public void getTask(@NonNull final String taskId, @NonNull final GetTaskCallback callback) {
		Disposable subscribe = mTasksDao.getTaskById(taskId)
				.subscribe(new Consumer<Task>() {
					@Override
					public void accept(Task task) throws Exception {
						if (task != null) {
							callback.onTaskLoaded(task);
						} else {
							callback.onDataNotAvailable();
						}
					}
				});
	}
	
	@Override
	public void saveTask(@NonNull final Task task) {
		Disposable subscribe = mTasksDao.insertTask(task)
				.subscribe(new Action() {
					@Override
					public void run() throws Exception {
						// complete
						Timber.i("saveTask complete");
					}
				});
	}
	
	@Override
	public void completeTask(@NonNull final Task task) {
		Disposable subscribe = mTasksDao.updateCompleted(task.getId(), true)
				.subscribe(new Action() {
					@Override
					public void run() throws Exception {
						// complete
						Timber.i("completeTask complete");
					}
				});
	}
	
	@Override
	public void completeTask(@NonNull String taskId) {
		// Not required for the local data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public void activateTask(@NonNull final Task task) {
		Disposable subscribe = mTasksDao.updateCompleted(task.getId(), false)
				.subscribe(new Action() {
					@Override
					public void run() throws Exception {
						// complete
						Timber.i("activateTask complete");
					}
				});
	}
	
	@Override
	public void activateTask(@NonNull String taskId) {
		// Not required for the local data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public void clearCompletedTasks() {
		Disposable subscribe = Observable.create(new ObservableOnSubscribe<Integer>() {
			@Override
			public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
				int i = mTasksDao.deleteCompletedTasks();
				emitter.onNext(i);
			}
		}).subscribe(new Consumer<Integer>() {
					@Override
					public void accept(Integer integer) throws Exception {
						Timber.i("clearCompletedTasks count = %s", integer);
					}
				});
	}
	
	@Override
	public void refreshTasks() {
		// Not required because the {@link TasksRepository} handles the logic of refreshing the
		// tasks from all the available data sources.
	}
	
	@Override
	public void deleteAllTasks() {
		Disposable deleteAllTasks_complete = mTasksDao.deleteTasks()
				.subscribe(new Action() {
					@Override
					public void run() throws Exception {
						Timber.i("deleteAllTasks complete");
					}
				});
	}
	
	@Override
	public void deleteTask(@NonNull final String taskId) {
		Disposable subscribe = Observable.create(new ObservableOnSubscribe<Integer>() {
			@Override
			public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
				int i = mTasksDao.deleteTaskById(taskId);
				emitter.onNext(i);
			}
		}).subscribe(new Consumer<Integer>() {
					@Override
					public void accept(Integer integer) throws Exception {
						Timber.i("deleteTask count = %s", integer);
					}
				});
	}
}
