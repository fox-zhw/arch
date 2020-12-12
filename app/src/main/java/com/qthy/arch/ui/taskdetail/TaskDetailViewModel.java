package com.qthy.arch.ui.taskdetail;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.qthy.arch.Event;
import com.qthy.arch.R;
import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.data.source.TasksRepository;

public class TaskDetailViewModel extends ViewModel {
	
	final MutableLiveData<Task> mTask = new MutableLiveData<>();
	
	final MutableLiveData<Boolean> mIsDataAvailable = new MutableLiveData<>();
	
	final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();
	
	final MutableLiveData<Event<Object>> mEditTaskCommand = new MutableLiveData<>();
	
	final MutableLiveData<Event<Object>> mDeleteTaskCommand = new MutableLiveData<>();
	
	final MutableLiveData<Event<Integer>> mSnackbarText = new MutableLiveData<>();
	
	private final TasksRepository mTasksRepository;
	
	// This LiveData depends on another so we can use a transformation.
	public final LiveData<Boolean> completed = Transformations.map(mTask, new Function<Task, Boolean>() {
		@Override
		public Boolean apply(Task input) {
			return input.isCompleted();
		}
	});
	
	@ViewModelInject
	public TaskDetailViewModel(TasksRepository tasksRepository) {
		mTasksRepository = tasksRepository;
	}
	
	public void deleteTask() {
		if (mTask.getValue() != null) {
			mTasksRepository.deleteTask(mTask.getValue().getId());
			mDeleteTaskCommand.setValue(new Event<>(new Object()));
		}
	}
	
	public void editTask() {
		mEditTaskCommand.setValue(new Event<>(new Object()));
	}
	
	public LiveData<Event<Integer>> getSnackbarMessage() {
		return mSnackbarText;
	}
	
	public MutableLiveData<Event<Object>> getEditTaskCommand() {
		return mEditTaskCommand;
	}
	
	public MutableLiveData<Event<Object>> getDeleteTaskCommand() {
		return mDeleteTaskCommand;
	}
	
	public LiveData<Task> getTask() {
		return mTask;
	}
	
	public LiveData<Boolean> getIsDataAvailable() {
		return mIsDataAvailable;
	}
	
	public LiveData<Boolean> getDataLoading() {
		return mDataLoading;
	}
	
	public void setCompleted(boolean completed) {
		if (mDataLoading.getValue()) {
			return;
		}
		Task task = this.mTask.getValue();
		if (completed) {
			mTasksRepository.completeTask(task);
			showSnackbarMessage(R.string.task_marked_complete);
		} else {
			mTasksRepository.activateTask(task);
			showSnackbarMessage(R.string.task_marked_active);
		}
	}
	
	public void start(String taskId) {
		if (taskId != null) {
			mDataLoading.setValue(true);
			mTasksRepository.getTask(taskId, new TasksDataSource.GetTaskCallback() {
				@Override
				public void onTaskLoaded(Task task) {
					setTask(task);
					mDataLoading.setValue(false);
				}
				
				@Override
				public void onDataNotAvailable() {
					mTask.setValue(null);
					mDataLoading.setValue(false);
					mIsDataAvailable.setValue(false);
				}
			});
		}
	}
	
	public void setTask(Task task) {
		this.mTask.setValue(task);
		mIsDataAvailable.setValue(task != null);
	}
	
	public void onRefresh() {
		if (mTask.getValue() != null) {
			start(mTask.getValue().getId());
		}
	}
	
	@Nullable
	protected String getTaskId() {
		return mTask.getValue().getId();
	}
	
	private void showSnackbarMessage(@StringRes Integer message) {
		mSnackbarText.setValue(new Event<>(message));
	}
}