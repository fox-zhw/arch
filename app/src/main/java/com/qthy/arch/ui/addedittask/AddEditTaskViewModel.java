package com.qthy.arch.ui.addedittask;

import androidx.annotation.Nullable;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qthy.arch.Event;
import com.qthy.arch.MainActivity;
import com.qthy.arch.R;
import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.data.source.TasksRepository;

public class AddEditTaskViewModel extends ViewModel {
	
	// Two-way databinding, exposing MutableLiveData
	final MutableLiveData<String> title = new MutableLiveData<>();
	
	// Two-way databinding, exposing MutableLiveData
	final MutableLiveData<String> description = new MutableLiveData<>();
	
	final MutableLiveData<Boolean> dataLoading = new MutableLiveData<>();
	
	final MutableLiveData<Event<Integer>> mSnackbarText = new MutableLiveData<>();
	
	final MutableLiveData<Event<Integer>> mTaskUpdated = new MutableLiveData<>();
	
	private final TasksRepository mTasksRepository;
	
	@Nullable
	private String mTaskId;
	
	private boolean mIsNewTask;
	
	private boolean mIsDataLoaded = false;
	
	private boolean mTaskCompleted = false;
	
	@ViewModelInject
	public AddEditTaskViewModel(TasksRepository tasksRepository) {
		mTasksRepository = tasksRepository;
	}
	
	public void start(String taskId) {
		if (dataLoading.getValue() != null && dataLoading.getValue()) {
			// Already loading, ignore.
			return;
		}
		mTaskId = taskId;
		if (taskId == null) {
			// No need to populate, it's a new task
			mIsNewTask = true;
			return;
		}
		if (mIsDataLoaded) {
			// No need to populate, already have data.
			return;
		}
		mIsNewTask = false;
		dataLoading.setValue(true);
		
		mTasksRepository.getTask(taskId, new TasksDataSource.GetTaskCallback() {
			@Override
			public void onTaskLoaded(Task task) {
				title.setValue(task.getTitle());
				description.setValue(task.getDescription());
				mTaskCompleted = task.isCompleted();
				dataLoading.setValue(false);
				mIsDataLoaded = true;
			}
			
			@Override
			public void onDataNotAvailable() {
				dataLoading.setValue(false);
			}
		});
	}
	
	// Called when clicking on fab.
	void saveTask(String title, String description) {
		Task task = new Task(title, description);
		if (task.isEmpty()) {
			mSnackbarText.setValue(new Event<>(R.string.empty_task_message));
			return;
		}
		if (isNewTask() || mTaskId == null) {
			createTask(task);
		} else {
			task = new Task(title, description, mTaskCompleted, mTaskId);
			updateTask(task);
		}
	}
	
	private boolean isNewTask() {
		return mIsNewTask;
	}
	
	private void createTask(Task newTask) {
		mTasksRepository.saveTask(newTask);
		mTaskUpdated.setValue(new Event<>(MainActivity.ADD_EDIT_RESULT_OK));
	}
	
	private void updateTask(Task task) {
		if (isNewTask()) {
			throw new RuntimeException("updateTask() was called but task is new.");
		}
		mTasksRepository.saveTask(task);
		mTaskUpdated.setValue(new Event<>(MainActivity.EDIT_RESULT_OK));
	}
}