package com.qthy.arch.ui.statistics;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.data.source.TasksRepository;

import java.util.List;

public class StatisticsViewModel extends ViewModel {
	
	final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();
	
	final MutableLiveData<Boolean> mError = new MutableLiveData<>();
	
	final MutableLiveData<Integer> mActiveTasks = new MutableLiveData<>();
	
	final MutableLiveData<Integer> mCompletedTasks = new MutableLiveData<>();
	
	final MutableLiveData<Boolean> mEmpty = new MutableLiveData<>();
	
	private int mNumberOfActiveTasks = 0;
	
	private int mNumberOfCompletedTasks = 0;
	
	private final TasksRepository mTasksRepository;
	
	@ViewModelInject
	public StatisticsViewModel(TasksRepository tasksRepository) {
		mTasksRepository = tasksRepository;
	}
	
	public void start() {
		loadStatistics();
	}
	
	public void loadStatistics() {
		mDataLoading.setValue(true);
		
		mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
			@Override
			public void onTasksLoaded(List<Task> tasks) {
				mError.setValue(false);
				computeStats(tasks);
			}
			
			@Override
			public void onDataNotAvailable() {
				mError.setValue(true);
				mNumberOfActiveTasks = 0;
				mNumberOfCompletedTasks = 0;
				updateDataBindingObservables();
			}
		});
	}
	
	/**
	 * Called when new data is ready.
	 */
	private void computeStats(List<Task> tasks) {
		int completed = 0;
		int active = 0;
		
		for (Task task : tasks) {
			if (task.isCompleted()) {
				completed += 1;
			} else {
				active += 1;
			}
		}
		mNumberOfActiveTasks = active;
		mNumberOfCompletedTasks = completed;
		
		updateDataBindingObservables();
	}
	
	private void updateDataBindingObservables() {
		mCompletedTasks.setValue(mNumberOfCompletedTasks);
		mActiveTasks.setValue(mNumberOfActiveTasks);
		mEmpty.setValue(mNumberOfActiveTasks + mNumberOfCompletedTasks == 0);
		mDataLoading.setValue(false);
		
	}
}