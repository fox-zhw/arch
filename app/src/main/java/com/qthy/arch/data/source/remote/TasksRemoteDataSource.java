package com.qthy.arch.data.source.remote;

import androidx.annotation.NonNull;

import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
public class TasksRemoteDataSource implements TasksDataSource {
	
	private static final int SERVICE_LATENCY_IN_MILLIS = 2000;
	
	private final static Map<String, Task> TASKS_SERVICE_DATA;
	
	static {
		TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
		addTask("Build tower in Pisa", "Ground looks good, no foundation work required.", "0");
		addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "1");
		addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "2");
		addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "3");
		addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!", "4");
	}
	
	// Prevent direct instantiation.
	@Inject
	public TasksRemoteDataSource() {
		Timber.i("TasksRemoteDataSource: ");
	}
	
	private static void addTask(String title, String description, String id) {
		Task newTask = new Task(title, description, id);
		TASKS_SERVICE_DATA.put(newTask.getId(), newTask);
	}
	
	/**
	 * Note: {@link LoadTasksCallback#onDataNotAvailable()} is never fired. In a real remote data
	 * source implementation, this would be fired if the server can't be contacted or the server
	 * returns an error.
	 */
	@Override
	public Flowable<List<Task>> getTasks() {
		Timber.i("getTasks: ");
		return Flowable.timer(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS).compose(new FlowableTransformer<Long, List<Task>>() {
			@Override
			public Publisher<List<Task>> apply(Flowable<Long> upstream) {
				return upstream.map(new Function<Long, List<Task>>() {
					@Override
					public List<Task> apply(Long aLong) throws Exception {
						return new ArrayList<>(TASKS_SERVICE_DATA.values());
					}
				});
			}
		});
	}
	
	/**
	 * Note: {@link GetTaskCallback#onDataNotAvailable()} is never fired. In a real remote data
	 * source implementation, this would be fired if the server can't be contacted or the server
	 * returns an error.
	 */
	@Override
	public Flowable<Task> getTask(@NonNull String taskId) {
		Timber.i("getTask: %s", taskId);
		final Task task = TASKS_SERVICE_DATA.get(taskId);
		// Simulate network by delaying the execution.
		return Flowable.timer(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS).compose(new FlowableTransformer<Long, Task>() {
			@Override
			public Publisher<Task> apply(Flowable<Long> upstream) {
				return upstream.map(new Function<Long, Task>() {
					@Override
					public Task apply(Long aLong) throws Exception {
						return task;
					}
				});
			}
		});
	}
	
	@Override
	public Completable saveTask(@NonNull Task task) {
		Timber.i("saveTask: %s", task);
		return Completable.create(new CompletableOnSubscribe() {
			@Override
			public void subscribe(CompletableEmitter emitter) throws Exception {
				TASKS_SERVICE_DATA.put(task.getId(), task);
				emitter.onComplete();
			}
		});
	}
	
	@Override
	public Completable completeTask(@NonNull Task task) {
		Timber.i("completeTask: %s", task);
		return Completable.fromRunnable(new Runnable() {
			@Override
			public void run() {
				Task completedTask = new Task(task.getTitle(), task.getDescription(), true, task.getId());
				TASKS_SERVICE_DATA.put(task.getId(), completedTask);
			}
		});
	}
	
	@Override
	public void completeTask(@NonNull String taskId) {
		// Not required for the remote data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public Completable activateTask(@NonNull Task task) {
		Timber.i("activateTask: %s", task);
		return Completable.fromRunnable(new Runnable() {
			@Override
			public void run() {
				Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
				TASKS_SERVICE_DATA.put(task.getId(), activeTask);
			}
		});
	}
	
	@Override
	public void activateTask(@NonNull String taskId) {
		// Not required for the remote data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public Flowable<Integer> clearCompletedTasks() {
		Timber.i("clearCompletedTasks: ");
		return Flowable.fromCallable(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				int count = 0;
				Iterator<Map.Entry<String, Task>> it = TASKS_SERVICE_DATA.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Task> entry = it.next();
					if (entry.getValue().isCompleted()) {
						it.remove();
						count++;
					}
				}
				return count;
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
		return Completable.fromRunnable(new Runnable() {
			@Override
			public void run() {
				TASKS_SERVICE_DATA.clear();
			}
		});
	}
	
	@Override
	public Flowable<Integer> deleteTask(@NonNull String taskId) {
		Timber.i("deleteTask: %s", taskId);
		return Flowable.fromCallable(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				TASKS_SERVICE_DATA.remove(taskId);
				return 1;
			}
		});
	}
}
