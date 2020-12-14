package com.qthy.arch.ui.taskdetail;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.qthy.arch.Event;
import com.qthy.arch.R;
import com.qthy.arch.base.BaseViewModel;
import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksRepository;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class TaskDetailViewModel extends BaseViewModel {
	
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
			addDisposable(mTasksRepository.deleteTask(mTask.getValue().getId()), new Consumer<Integer>() {
				@Override
				public void accept(Integer integer) throws Exception {
					Timber.i("deleteTask: success");
					mDeleteTaskCommand.setValue(new Event<>(new Object()));
				}
			});
		}
	}
	
	public void editTask() {
		mEditTaskCommand.setValue(new Event<>(new Object()));
	}
	
	public void setCompleted(boolean completed) {
		if (mDataLoading.getValue()) {
			return;
		}
		Task task = this.mTask.getValue();
		if (completed) {
			addDisposable(mTasksRepository.completeTask(task), new Action() {
				@Override
				public void run() throws Exception {
					Timber.i("setCompleted: completeTask success");
					showSnackbarMessage(R.string.task_marked_complete);
				}
			});
		} else {
			addDisposable(mTasksRepository.activateTask(task), new Action() {
				@Override
				public void run() throws Exception {
					Timber.i("setCompleted: activateTask success");
					showSnackbarMessage(R.string.task_marked_active);
				}
			});
		}
	}
	
	public void start(String taskId) {
		if (taskId != null) {
			mDataLoading.setValue(true);
			
			addDisposable(mTasksRepository.getTask(taskId), new Consumer<Task>() {
				@Override
				public void accept(Task task) throws Exception {
					if (task != null) {
						setTask(task);
						mDataLoading.setValue(false);
					} else {
						mTask.setValue(null);
						mDataLoading.setValue(false);
						mIsDataAvailable.setValue(false);
					}
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