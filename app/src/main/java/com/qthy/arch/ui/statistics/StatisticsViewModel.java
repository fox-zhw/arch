package com.qthy.arch.ui.statistics;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qthy.arch.base.BaseViewModel;
import com.qthy.arch.data.Task;
import com.qthy.arch.data.source.TasksDataSource;
import com.qthy.arch.data.source.TasksRepository;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class StatisticsViewModel extends BaseViewModel {
	
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
		super();
		mTasksRepository = tasksRepository;
	}
	
	public void start() {
		loadStatistics();
	}
	
	public void loadStatistics() {
		mDataLoading.setValue(true);
		
		addDisposable(mTasksRepository.getTasks(), new Consumer<List<Task>>() {
			@Override
			public void accept(List<Task> tasks) throws Exception {
				Timber.i("loadStatistics: success");
				if (tasks.isEmpty()) {
					mError.setValue(true);
					mNumberOfActiveTasks = 0;
					mNumberOfCompletedTasks = 0;
					updateDataBindingObservables();
				} else {
					mError.setValue(false);
					computeStats(tasks);
				}
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