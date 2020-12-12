package com.qthy.arch.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.qthy.arch.R;
import com.qthy.arch.ScrollChildSwipeRefreshLayout;
import com.qthy.arch.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;
@AndroidEntryPoint
public class StatisticsFragment extends BaseFragment {
	
	private StatisticsViewModel mViewModel;
	private View mLayoutStatistics;
	private TextView mTvNoTasks;
	private TextView mTvActive;
	private TextView mTvCompleted;
	private ScrollChildSwipeRefreshLayout mScrollRefreshLayout;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.statistics_fragment, container, false);
		initView(root);
		return root;
	}
	
	private void initView(View root) {
		mLayoutStatistics = root.findViewById(R.id.statistics_layout);
		mTvNoTasks = root.findViewById(R.id.tv_no_tasks);
		mTvActive = root.findViewById(R.id.stats_active_text);
		mTvCompleted = root.findViewById(R.id.stats_completed_text);
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
		mViewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
		// TODO: Use the ViewModel
		initViewModel();
		mViewModel.start();
	}
	
	private void initViewModel() {
		mViewModel.mDataLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean != null) {
					mLayoutStatistics.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
					mScrollRefreshLayout.setRefreshing(aBoolean);
				}
			}
		});
		mViewModel.mActiveTasks.observe(getViewLifecycleOwner(), new Observer<Integer>() {
			@Override
			public void onChanged(Integer integer) {
				if (integer != null) {
					mTvActive.setText(getString(R.string.statistics_active_tasks, integer + 0.0));
				}
			}
		});
		mViewModel.mCompletedTasks.observe(getViewLifecycleOwner(), new Observer<Integer>() {
			@Override
			public void onChanged(Integer integer) {
				if (integer != null) {
					mTvCompleted.setText(getString(R.string.statistics_completed_tasks, integer + 0.0));
				}
			}
		});
		mViewModel.mEmpty.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean != null) {
					mTvNoTasks.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
					mTvActive.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
					mTvCompleted.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
				}
			}
		});
		mViewModel.mError.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
			
			}
		});
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Timber.i("onDestroyView");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Timber.i("onDestroy");
	}
}