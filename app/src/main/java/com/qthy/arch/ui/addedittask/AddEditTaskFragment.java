package com.qthy.arch.ui.addedittask;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qthy.arch.Event;
import com.qthy.arch.R;
import com.qthy.arch.ScrollChildSwipeRefreshLayout;
import com.qthy.arch.base.BaseFragment;
import com.qthy.arch.util.SnackbarUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddEditTaskFragment extends BaseFragment {
	
	private AddEditTaskViewModel mViewModel;
	private ScrollChildSwipeRefreshLayout mScrollRefreshLayout;
	private View mLayoutEdit;
	private EditText mEtTitle;
	private EditText mEtDesc;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.add_edit_task_fragment, container, false);
		setHasOptionsMenu(true);
		setRetainInstance(false);
		initView(root);
		return root;
	}
	
	private void initView(View root) {
		
		mLayoutEdit = root.findViewById(R.id.layout_edit);
		mEtTitle = root.findViewById(R.id.add_task_title_edit_text);
		mEtDesc = root.findViewById(R.id.add_task_description_edit_text);
		
		FloatingActionButton fab = root.findViewById(R.id.save_task_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewModel.saveTask(mEtTitle.getText().toString(), mEtDesc.getText().toString());
			}
		});
		
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
		mViewModel = ViewModelProviders.of(this).get(AddEditTaskViewModel.class);
		initViewModel();
		
		String taskId = null;
		String title;
		Bundle arguments = getArguments();
		if (arguments != null) {
			AddEditTaskFragmentArgs args = AddEditTaskFragmentArgs.fromBundle(getArguments());
			taskId = args.getTaskId();
			title = args.getTitle();
			mViewModel.title.setValue(title);
		}
		mViewModel.start(taskId);
	}
	
	private void initViewModel() {
		mViewModel.title.observe(getViewLifecycleOwner(), new Observer<String>() {
			@Override
			public void onChanged(String s) {
				if (s != null) {
					mEtTitle.setText(s);
				}
			}
		});
		mViewModel.description.observe(getViewLifecycleOwner(), new Observer<String>() {
			@Override
			public void onChanged(String s) {
				if (s != null) {
					mEtDesc.setText(s);
				}
			}
		});
		mViewModel.dataLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean aBoolean) {
				if (aBoolean != null) {
					mLayoutEdit.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
					mScrollRefreshLayout.setRefreshing(aBoolean);
				}
			}
		});
		mViewModel.mTaskUpdated.observe(getViewLifecycleOwner(), new Event.EventObserver<Integer>() {
			@Override
			public void onEventChanged(@NonNull Integer integer) {
				AddEditTaskFragmentDirections.ActionAddEditTaskFragmentToTasksFragment action =
						AddEditTaskFragmentDirections.actionAddEditTaskFragmentToTasksFragment();
				action.setUserMessage(integer);
				Navigation.findNavController(requireView()).navigate(action);
			}
		});
		mViewModel.mSnackbarText.observe(getViewLifecycleOwner(), new Event.EventObserver<Integer>() {
			@Override
			public void onEventChanged(@NonNull Integer integer) {
				SnackbarUtils.showSnackbar(getView(), getString(integer));
			}
		});
	}
	
	@Override
	public void onStop() {
		super.onStop();
		hideKeyboard();
	}
	
	private void hideKeyboard() {
		View view = getView();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
			}
		}
	}
}