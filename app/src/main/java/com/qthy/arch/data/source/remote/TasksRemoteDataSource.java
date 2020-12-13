package com.qthy.arch.data.source.remote;

import android.os.Handler;

import androidx.annotation.NonNull;


import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
		return Flowable.timer(2, TimeUnit.SECONDS).compose(new FlowableTransformer<Long, List<Task>>() {
			@Override
			public Publisher<List<Task>> apply(Flowable<Long> upstream) {
				return upstream.map(new Function<Long, List<Task>>() {
					@Override
					public List<Task> apply(Long aLong) throws Exception {
						return new ArrayList<>(TASKS_SERVICE_DATA.values());
					}
				}).subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread());
			}
		});
	}
	
	/**
	 * Note: {@link GetTaskCallback#onDataNotAvailable()} is never fired. In a real remote data
	 * source implementation, this would be fired if the server can't be contacted or the server
	 * returns an error.
	 */
	@Override
	public void getTask(@NonNull String taskId, final @NonNull GetTaskCallback callback) {
		final Task task = TASKS_SERVICE_DATA.get(taskId);
		
		// Simulate network by delaying the execution.
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				callback.onTaskLoaded(task);
			}
		}, SERVICE_LATENCY_IN_MILLIS);
	}
	
	@Override
	public void saveTask(@NonNull Task task) {
		TASKS_SERVICE_DATA.put(task.getId(), task);
	}
	
	@Override
	public void completeTask(@NonNull Task task) {
		Task completedTask = new Task(task.getTitle(), task.getDescription(), true, task.getId());
		TASKS_SERVICE_DATA.put(task.getId(), completedTask);
	}
	
	@Override
	public void completeTask(@NonNull String taskId) {
		// Not required for the remote data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public void activateTask(@NonNull Task task) {
		Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
		TASKS_SERVICE_DATA.put(task.getId(), activeTask);
	}
	
	@Override
	public void activateTask(@NonNull String taskId) {
		// Not required for the remote data source because the {@link TasksRepository} handles
		// converting from a {@code taskId} to a {@link task} using its cached data.
	}
	
	@Override
	public void clearCompletedTasks() {
		Iterator<Map.Entry<String, Task>> it = TASKS_SERVICE_DATA.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Task> entry = it.next();
			if (entry.getValue().isCompleted()) {
				it.remove();
			}
		}
	}
	
	@Override
	public void refreshTasks() {
		// Not required because the {@link TasksRepository} handles the logic of refreshing the
		// tasks from all the available data sources.
	}
	
	@Override
	public void deleteAllTasks() {
		TASKS_SERVICE_DATA.clear();
	}
	
	@Override
	public void deleteTask(@NonNull String taskId) {
		TASKS_SERVICE_DATA.remove(taskId);
	}
}
