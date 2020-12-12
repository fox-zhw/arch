package com.qthy.arch.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.qthy.arch.R;
import com.qthy.arch.data.Task;

import java.util.List;

/**
 * @author zhaohw
 * @date 2020/12/3
 */
class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.MyHolder> {
	
	private final List<Task> mTasks;
	
	private final TaskItemListener mItemListener;
	
	public TasksAdapter(List<Task> tasks, TaskItemListener itemListener) {
		mTasks = tasks;
		mItemListener = itemListener;
	}
	
	@NonNull
	@Override
	public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
		return new MyHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyHolder holder, int position) {
		holder.bind(mTasks.get(position));
	}
	
	@Override
	public int getItemCount() {
		return mTasks.size();
	}
	
	class MyHolder extends RecyclerView.ViewHolder {
		
		private final View rowView;
		private final CheckBox mCheckBox;
		private final TextView mTitle;
		
		public MyHolder(@NonNull View itemView) {
			super(itemView);
			rowView = itemView;
			mCheckBox = itemView.findViewById(R.id.complete_checkbox);
			mTitle = itemView.findViewById(R.id.title_text);
		}
		
		public void bind(Task task) {
			mTitle.setText(task.getTitleForList());
			mCheckBox.setChecked(task.isCompleted());
			updateCompleted(task);
			
			mCheckBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mItemListener != null) {
						if (!task.isCompleted()) {
							mItemListener.onCompleteTaskClick(task);
						} else {
							mItemListener.onActivateTaskClick(task);
						}
						task.setCompleted(!task.isCompleted());
						updateCompleted(task);
					}
				}
			});
			
			rowView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mItemListener != null) {
						mItemListener.onTaskClick(task);
					}
				}
			});
			
		}
		
		private void updateCompleted(Task task) {
			if (task.isCompleted()) {
				rowView.setBackgroundResource(R.drawable.list_completed_touch_feedback);
			} else {
				rowView.setBackgroundResource(R.drawable.touch_feedback);
			}
		}
	}
	
	public interface TaskItemListener {
		
		void onTaskClick(Task clickedTask);
		
		void onCompleteTaskClick(Task completedTask);
		
		void onActivateTaskClick(Task activatedTask);
	}
}
