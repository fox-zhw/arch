package com.qthy.arch.ui.taskdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qthy.arch.Event;
import com.qthy.arch.MainActivity;
import com.qthy.arch.R;
import com.qthy.arch.ScrollChildSwipeRefreshLayout;
import com.qthy.arch.base.BaseFragment;
import com.qthy.arch.data.Task;
import com.qthy.arch.util.SnackbarUtils;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class TaskDetailFragment extends BaseFragment {
	private TaskDetailViewModel mViewModel;
	public static final int REQUEST_EDIT_TASK = 1;
	private ScrollChildSwipeRefreshLayout mScrollRefreshLayout;
	private View mLayoutNoData;
	private View mLayoutData;
	private TextView mTvNoData;
	private CheckBox mCbComplete;
	private TextView mTvTitle;
	private TextView mTvDesc;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		Timber.i("onCreateView%s", this);
		
		View root = inflater.inflate(R.layout.task_detail_fragment, container, false);
		setHasOptionsMenu(true);
		
		initView(root);
		return root;
	}
	
	private void initView(View root) {
		Timber.i("initView");
		mLayoutNoData = root.findViewById(R.id.layout_no_data);
		mLayoutData = root.findViewById(R.id.layout_data);
		mTvNoData = root.findViewById(R.id.tv_no_data);
		mCbComplete = root.findViewById(R.id.task_detail_complete_checkbox);
		mTvTitle = root.findViewById(R.id.task_detail_title_text);
		mTvDesc = root.findViewById(R.id.task_detail_description_text);
		
		mCbComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mViewModel.setCompleted(isChecked);
			}
		});
		
		FloatingActionButton fab = root.findViewById(R.id.edit_task_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.editTask();
			}
		});
		
		// scroll refresh
		mScrollRefreshLayout = root.findViewById(R.id.refresh_layout);
		mScrollRefreshLayout.setColorSchemeColors(
				ContextCompat.getColor(getActivity(), R.color.colorPrimary),
				ContextCompat.getColor(getActivity(), R.color.colorAccent),
				ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
		);
		mScrollRefreshLayout.setEnabled(false);
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Timber.i("onActivityCreated");
		mViewModel = ViewModelProviders.of(this).get(TaskDetailViewModel.class);
		// TODO: Use the ViewModel
		initViewModel();
	}
	
	private void initViewModel() {
		Timber.i("initViewModel");
		mViewModel.completed.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				mCbComplete.setChecked(aBoolean);
			}
		});
		mViewModel.mTask.observe(getViewLifecycleOwner(), new Observer<Task>() {
			@Override
			public void onChanged(Task task) {
				if (task != null) {
					mTvTitle.setText(task.getTitle());
					mTvDesc.setText(task.getDescription());
				}
			}
		});
		// 数据有效
		mViewModel.mIsDataAvailable.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean != null) {
					mLayoutNoData.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
					mLayoutData.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
				}
			}
		});
		mViewModel.mDataLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean != null) {
					mTvNoData.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
					mScrollRefreshLayout.setRefreshing(aBoolean);
				}
			}
		});
		mViewModel.mDeleteTaskCommand.observe(getViewLifecycleOwner(), new Event.EventObserver<Object>() {
			@Override
			public void onEventChanged(@NonNull Object o) {
				TaskDetailFragmentDirections.ActionTaskDetailFragmentToTasksFragment action = TaskDetailFragmentDirections.actionTaskDetailFragmentToTasksFragment();
				action.setUserMessage(MainActivity.DELETE_RESULT_OK);
				Navigation.findNavController(requireView()).navigate(action);
			}
		});
		mViewModel.mEditTaskCommand.observe(getViewLifecycleOwner(), new Event.EventObserver<Object>() {
			@Override
			public void onEventChanged(@NonNull Object o) {
				TaskDetailFragmentDirections.ActionTaskDetailFragmentToAddEditTaskFragment action =
						TaskDetailFragmentDirections.actionTaskDetailFragmentToAddEditTaskFragment(TaskDetailFragmentArgs.fromBundle(requireArguments()).getTaskId(), getString(R.string.edit_task));
				Navigation.findNavController(requireView()).navigate(action);
			}
		});
		mViewModel.mSnackbarText.observe(getViewLifecycleOwner(), new Event.EventObserver<Integer>() {
			@Override
			public void onEventChanged(@NonNull Integer integer) {
				SnackbarUtils.showSnackbar(getView(), getString(integer));
			}
		});
		
		Bundle arguments = getArguments();
		if (arguments != null) {
			String taskId = TaskDetailFragmentArgs.fromBundle(arguments).getTaskId();
			mViewModel.start(taskId);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_delete:
				mViewModel.deleteTask();
				return true;
		}
		return false;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
	}
	
}