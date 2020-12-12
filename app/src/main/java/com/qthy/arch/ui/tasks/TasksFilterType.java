package com.qthy.arch.ui.tasks;

/**
 * @author zhaohw
 * @date 2020/12/3
 */
enum TasksFilterType {
	/**
	 * Do not filter tasks.
	 */
	ALL_TASKS,
	
	/**
	 * Filters only the active (not completed yet) tasks.
	 */
	ACTIVE_TASKS,
	
	/**
	 * Filters only the completed tasks.
	 */
	COMPLETED_TASKS
}
