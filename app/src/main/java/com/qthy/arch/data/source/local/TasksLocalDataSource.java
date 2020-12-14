package com.qthy.arch.data.source.local;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.util.RxUtils;

import org.reactivestreams.Publisher;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import kotlinx.coroutines.flow.Flow;
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
		Timber.i("getTasks: ");
		return Flowable.create(new FlowableOnSubscribe<List<Task>>() {
			@SuppressLint("CheckResult")
			@Override
			public void subscribe(FlowableEmitter<List<Task>> emitter) throws Exception {
				List<Task> tasks = mTasksDao.getTasks();
				if (!emitter.isCancelled()) {
					emitter.onNext(tasks);
				}
				emitter.onComplete();
			}
		}, BackpressureStrategy.LATEST);
	}
	
	/**
	 * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if the {@link Task} isn't
	 * found.
	 */
	@Override
	public Flowable<Task> getTask(@NonNull String taskId) {
		Timber.i("getTask: %s", taskId);
		return Flowable.just(taskId).flatMap(new Function<String, Publisher<Task>>() {
			@Override
			public Publisher<Task> apply(String s) throws Exception {
				Task taskById = mTasksDao.getTaskById(s);
				return Flowable.just(taskById);
			}
		});
	}
	
	@Override
	public Completable saveTask(@NonNull final Task task) {
		Timber.i("saveTask: %s", task);
		return Completable.create(new CompletableOnSubscribe() {
			@Override
			public void subscribe(CompletableEmitter emitter) throws Exception {
				mTasksDao.insertTask(task);
				if (!emitter.isDisposed()) {
					emitter.onComplete();
				}
			}
		});
	}
	
	@Override
	public Completable completeTask(@NonNull final Task task) {
		Timber.i("completeTask: %s", task);
		return Completable.create(new CompletableOnSubscribe() {
			@Override
			public void subscribe(CompletableEmitter emitter) throws Exception {
				mTasksDao.updateCompleted(task.getId(), true);
				if (!emitter.isDisposed()) {
					emitter.onComplete();
				}
			}
		});
	}
	
	@Override
	public void completeTask(@NonNull String taskId) {
		// Not required for the local data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public Completable activateTask(@NonNull final Task task) {
		Timber.i("activateTask: %s", task);
		return Completable.create(new CompletableOnSubscribe() {
			@Override
			public void subscribe(CompletableEmitter emitter) throws Exception {
				mTasksDao.updateCompleted(task.getId(), false);
				if (!emitter.isDisposed()) {
					emitter.onComplete();
				}
			}
		});
	}
	
	@Override
	public void activateTask(@NonNull String taskId) {
		// Not required for the local data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public Flowable<Integer> clearCompletedTasks() {
		Timber.i("clearCompletedTasks: ");
		return Flowable.just(0).map(new Function<Integer, Integer>() {
			@Override
			public Integer apply(Integer integer) throws Exception {
				return mTasksDao.deleteCompletedTasks();
			}
		});
	}
	
	@Override
	public void refreshTasks() {
		// Not required because the {@link TasksRepository} handles the logic of refreshing the
		// tasks from all the available data sources.
	}
	
	@Override
	public Completable deleteAllTasks() {
		Timber.i("deleteAllTasks: ");
		return Completable.create(new CompletableOnSubscribe() {
			@Override
			public void subscribe(CompletableEmitter emitter) throws Exception {
				mTasksDao.deleteTasks();
				if (!emitter.isDisposed()) {
					emitter.onComplete();
				}
			}
		});
	}
	
	@Override
	public Flowable<Integer> deleteTask(@NonNull final String taskId) {
		Timber.i("deleteTask: %s", taskId);
		return Flowable.just(0).map(new Function<Integer, Integer>() {
			@Override
			public Integer apply(Integer integer) throws Exception {
				return mTasksDao.deleteTaskById(taskId);
			}
		});
	}
}
