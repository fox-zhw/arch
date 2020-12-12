package com.qthy.arch.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qthy.arch.Event;
import com.qthy.arch.R;
import com.qthy.arch.ScrollChildSwipeRefreshLayout;
import com.qthy.arch.base.BaseFragment;
import com.qthy.arch.data.Task;
import com.qthy.arch.util.SnackbarUtils;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;
@AndroidEntryPoint
public class TasksFragment extends BaseFragment {
	private TasksViewModel mViewModel;
	private ScrollChildSwipeRefreshLayout mScrollRefreshLayout;
	private FloatingActionButton mFloatingActionButton;
	private View mLayoutTasks;
	private View mLayoutNoTask;
	private RecyclerView mTaskList;
	private TextView mTvFilterLabel;
	private ImageView mIvNoTask;
	private TextView mTvNoTask;
	private TasksAdapter mTasksAdapter;
	
	final List<Task> mTasks = new ArrayList<>();
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		Timber.i("onCreateView%s", this);
		View mRootView = inflater.inflate(R.layout.tasks_fragment, container, false);
		setHasOptionsMenu(true);
		initView(mRootView);
		return mRootView;
	}
	
	private void initView(View mRootView) {
		Timber.i("initView");
		mRootView.findViewById(R.id.coordinator_layout);
		mRootView.findViewById(R.id.tasks_container_layout);
		mLayoutTasks = mRootView.findViewById(R.id.tasks_linear_layout);
		mTvFilterLabel = mRootView.findViewById(R.id.filtering_text);
		mTaskList = mRootView.findViewById(R.id.tasks_list);
		mLayoutNoTask = mRootView.findViewById(R.id.no_tasks_layout);
		mIvNoTask = mRootView.findViewById(R.id.no_tasks_icon);
		mTvNoTask = mRootView.findViewById(R.id.no_tasks_text);
		
		// fab
		mFloatingActionButton = mRootView.findViewById(R.id.add_task_fab);
		mFloatingActionButton.setImageResource(R.drawable.ic_add);
		mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.addNewTask();
			}
		});
		
		// scroll refresh
		mScrollRefreshLayout = mRootView.findViewById(R.id.refresh_layout);
		mScrollRefreshLayout.setColorSchemeColors(
				ContextCompat.getColor(getActivity(), R.color.colorPrimary),
				ContextCompat.getColor(getActivity(), R.color.colorAccent),
				ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
		);
		mScrollRefreshLayout.setScrollUpChild(mTaskList);
		mScrollRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mViewModel.refresh();
			}
		});
		
		
		mTasksAdapter = new TasksAdapter(mTasks, new TasksAdapter.TaskItemListener() {
			@Override
			public void onTaskClick(Task clickedTask) {
				mViewModel.openTask(clickedTask.getId());
			}
			
			@Override
			public void onCompleteTaskClick(Task completedTask) {
				mViewModel.completeTask(completedTask);
			}
			
			@Override
			public void onActivateTaskClick(Task activatedTask) {
				mViewModel.activateTask(activatedTask);
			}
		});
		mTaskList.setAdapter(mTasksAdapter);
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Timber.i("onActivityCreated");
		// TODO: Use the ViewModel
		mViewModel = ViewModelProviders.of(this).get(TasksViewModel.class);
		initViewModel();
		mViewModel.start();
		
		
		Bundle bundle = requireArguments();
		int userMessage = TasksFragmentArgs.fromBundle(bundle).getUserMessage();
		bundle.clear();
		
		mViewModel.showEditResultMessage(userMessage);
		Timber.i("userMessage = " + userMessage);
	}
	
	private void initViewModel() {
		Timber.i("initViewModel");
		// 数据是否为空
		mViewModel.empty.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean != null) {
					mLayoutTasks.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
					mLayoutNoTask.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
				}
			}
		});
		// filter label
		mViewModel.mCurrentFilteringLabel.observe(getViewLifecycleOwner(), new Observer<Integer>() {
			@Override
			public void onChanged(Integer integer) {
				if (integer != null) {
					mTvFilterLabel.setText(integer);
				}
			}
		});
		// tasks
		mViewModel.mItems.observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
			@Override
			public void onChanged(List<Task> tasks) {
				if (tasks != null) {
					mTasks.clear();
					mTasks.addAll(tasks);
					mTasksAdapter.notifyDataSetChanged();
				}
			}
		});
		// no task icon
		mViewModel.mNoTaskIconRes.observe(getViewLifecycleOwner(), new Observer<Integer>() {
			@Override
			public void onChanged(Integer integer) {
				if (integer != null) {
					mIvNoTask.setImageResource(integer);
				}
			}
		});
		// no task label
		mViewModel.mNoTasksLabel.observe(getViewLifecycleOwner(), new Observer<Integer>() {
			@Override
			public void onChanged(Integer integer) {
				if (integer != null) {
					mTvNoTask.setText(integer);
				}
			}
		});
		// add new task
		mViewModel.mNewTaskEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<Object>() {
			@Override
			public void onEventChanged(@NonNull Object o) {
				TasksFragmentDirections.ActionTasksFragmentToAddEditTaskFragment action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(null, getString(R.string.add_task));
				Navigation.findNavController(requireView()).navigate(action);
			}
		});
		// snackbar
		mViewModel.mSnackbarText.observe(getViewLifecycleOwner(), new Event.EventObserver<Integer>() {
			@Override
			public void onEventChanged(@NonNull Integer integer) {
				SnackbarUtils.showSnackbar(getView(), getString(integer));
			}
		});
		// open task
		mViewModel.mOpenTaskEvent.observe(getViewLifecycleOwner(), new Event.EventObserver<String>() {
			@Override
			public void onEventChanged(@NonNull String s) {
				TasksFragmentDirections.ActionTasksFragmentToTaskDetailFragment action = TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment(s);
				Navigation.findNavController(requireView()).navigate(action);
			}
		});

		mViewModel.mDataLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean != null) {
					mScrollRefreshLayout.setRefreshing(aBoolean);
				}
			}
		});
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		inflater.inflate(R.menu.tasks_fragment_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_clear:
				mViewModel.clearCompletedTasks();
				return true;
			case R.id.menu_filter:
				showFilteringPopUpMenu();
				return true;
			case R.id.menu_refresh:
				mViewModel.loadTasks(true);
				return true;
		}
		return false;
	}
	
	private void showFilteringPopUpMenu() {
		PopupMenu popup = new PopupMenu(getActivity(), getActivity().findViewById(R.id.menu_filter));
		popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.active:
						mViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);
						break;
					case R.id.completed:
						mViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);
						break;
					default:
						mViewModel.setFiltering(TasksFilterType.ALL_TASKS);
						break;
				}
				mViewModel.loadTasks(false);
				return true;
			}
		});
		
		popup.show();
	}
}