package com.qthy.arch.ui.tasks;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.qthy.arch.Event;
import com.qthy.arch.MainActivity;
import com.qthy.arch.R;
import com.qthy.arch.base.BaseViewModel;
import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

public class TasksViewModel extends BaseViewModel {
	
	final MutableLiveData<List<Task>> mItems = new MutableLiveData<>();
	
	final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();
	
	final MutableLiveData<Integer> mCurrentFilteringLabel = new MutableLiveData<>();
	
	final MutableLiveData<Integer> mNoTasksLabel = new MutableLiveData<>();
	
	final MutableLiveData<Integer> mNoTaskIconRes = new MutableLiveData<>();
	
	final MutableLiveData<Boolean> mTasksAddViewVisible = new MutableLiveData<>();
	
	final MutableLiveData<Event<Integer>> mSnackbarText = new MutableLiveData<>();
	
	private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;
	
	
	private final TasksRepository mTasksRepository;
	
	final MutableLiveData<Boolean> mIsDataLoadingError = new MutableLiveData<>();
	
	final MutableLiveData<Event<String>> mOpenTaskEvent = new MutableLiveData<>();
	
	final MutableLiveData<Event<Object>> mNewTaskEvent = new MutableLiveData<>();
	
	public final LiveData<Boolean> empty = Transformations.map(mItems, new Function<List<Task>, Boolean>() {
		@Override
		public Boolean apply(List<Task> input) {
			return input.isEmpty();
		}
	});
	
	@ViewModelInject
	public TasksViewModel(TasksRepository tasksRepository) {
		mTasksRepository = tasksRepository;
		setFiltering(TasksFilterType.ALL_TASKS);
	}
	
	public void start() {
		loadTasks(false);
	}
	
	public void loadTasks(boolean forceUpdate) {
		loadTasks(forceUpdate, true);
	}
	
	public void setFiltering(TasksFilterType requestType) {
		mCurrentFiltering = requestType;
		
		switch (requestType) {
			case ALL_TASKS:
				setFilter(R.string.label_all, R.string.no_tasks_all,
						R.drawable.logo_no_fill, true);
				break;
			case ACTIVE_TASKS:
				setFilter(R.string.label_active, R.string.no_tasks_active,
						R.drawable.ic_check_circle_96dp, false);
				break;
			case COMPLETED_TASKS:
				setFilter(R.string.label_completed, R.string.no_tasks_completed,
						R.drawable.ic_verified_user_96dp, false);
				break;
		}
	}
	
	public void clearCompletedTasks() {
		mTasksRepository.clearCompletedTasks();
		mSnackbarText.setValue(new Event<>(R.string.completed_tasks_cleared));
		loadTasks(false, false);
	}
	
	public void completeTask(Task task) {
		mTasksRepository.completeTask(task);
		mSnackbarText.setValue(new Event<>(R.string.task_marked_complete));
		
	}
	
	public void activateTask(Task task) {
		mTasksRepository.activateTask(task);
		mSnackbarText.setValue(new Event<>(R.string.task_marked_active));
	}
	
	/**
	 * Called by the Data Binding library and the FAB's click listener.
	 */
	public void addNewTask() {
		mNewTaskEvent.setValue(new Event<>(new Object()));
	}
	
	/**
	 * Called by the {@link TasksAdapter}.
	 */
	void openTask(String taskId) {
		mOpenTaskEvent.setValue(new Event<>(taskId));
	}
	
	private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {
		if (showLoadingUI) {
			mDataLoading.setValue(true);
		}
		if (forceUpdate) {
			mTasksRepository.refreshTasks();
		}
		
		addDisposable(mTasksRepository.getTasks(), new Consumer<List<Task>>() {
			@Override
			public void accept(List<Task> tasks) throws Exception {
				if (tasks.isEmpty()) {
					mDataLoading.setValue(false);
					mIsDataLoadingError.setValue(true);
				} else {
					List<Task> tasksToShow = new ArrayList<>();
					
					// We filter the tasks based on the requestType
					for (Task task : tasks) {
						switch (mCurrentFiltering) {
							case ALL_TASKS:
								tasksToShow.add(task);
								break;
							case ACTIVE_TASKS:
								if (task.isActive()) {
									tasksToShow.add(task);
								}
								break;
							case COMPLETED_TASKS:
								if (task.isCompleted()) {
									tasksToShow.add(task);
								}
								break;
							default:
								tasksToShow.add(task);
								break;
						}
					}
					if (showLoadingUI) {
						mDataLoading.setValue(false);
					}
					mIsDataLoadingError.setValue(false);
					
					List<Task> itemsValue = new ArrayList<>(tasksToShow);
					mItems.setValue(itemsValue);
					mDataLoading.setValue(false);
				}
			}
		});
	}
	
	private void setFilter(@StringRes int filteringLabelString,
	                       @StringRes int noTasksLabelString,
	                       @DrawableRes int noTaskIconDrawable,
	                       boolean tasksAddVisible) {
		mCurrentFilteringLabel.postValue(filteringLabelString);
		mNoTasksLabel.postValue(noTasksLabelString);
		mNoTaskIconRes.postValue(noTaskIconDrawable);
		mTasksAddViewVisible.postValue(tasksAddVisible);
	}
	
	public void refresh() {
		mTasksRepository.refreshTasks();
		setFiltering(mCurrentFiltering);
		loadTasks(true);
	}
	
	public void showEditResultMessage(int userMessage) {
		switch (userMessage) {
			case MainActivity.ADD_EDIT_RESULT_OK:
				mSnackbarText.setValue(new Event<>(R.string.successfully_added_task_message));
				break;
			case MainActivity.DELETE_RESULT_OK:
				mSnackbarText.setValue(new Event<>(R.string.successfully_deleted_task_message));
				break;
			case MainActivity.EDIT_RESULT_OK:
				mSnackbarText.setValue(new Event<>(R.string.successfully_saved_task_message));
				break;
		}
	}
}