package com.qthy.arch.data.source.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.qthy.arch.data.Task;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
@Database(entities = {Task.class}, version = 1, exportSchema = false)
public abstract class ToDoDatabase extends RoomDatabase {

	public abstract TasksDao taskDao();
	
}
