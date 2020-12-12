package com.qthy.arch.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

/**
 * @author zhaohw
 * @date 2020/12/2
 */
@Entity(tableName = "tasks")
public class Task {
	@ColumnInfo(name = "title")
	String title = "";
	@ColumnInfo(name = "description")
	String description = "";
	@ColumnInfo(name = "completed")
	boolean isCompleted = false;
	@PrimaryKey
	@NonNull
	@ColumnInfo(name = "entryid")
	String id;
	
	@Ignore
	public Task(String title, String description, boolean isCompleted) {
		this.title = title;
		this.description = description;
		this.isCompleted = isCompleted;
		id = UUID.randomUUID().toString();
	}
	
	public Task(String title, String description, boolean isCompleted, @NonNull String id) {
		this.title = title;
		this.description = description;
		this.isCompleted = isCompleted;
		this.id = id;
	}
	
	@Ignore
	public Task(String title, String description, String id) {
		this.title = title;
		this.description = description;
		this.isCompleted = false;
		this.id = id;
	}
	
	@Ignore
	public Task(String title, String description) {
		this.title = title;
		this.description = description;
		this.isCompleted = false;
		this.id = UUID.randomUUID().toString();
	}
	
	public void setId(@NonNull String id) {
		this.id = id;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setCompleted(boolean completed) {
		isCompleted = completed;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
	public String getTitleForList() {
		if (title != null && !title.isEmpty())
			return title;
		else
			return description;
	}
	
	public boolean isActive(){
		return !isCompleted;
	}
	
	public boolean isEmpty(){
		return title == null || title.isEmpty() || description == null || description.isEmpty();
	}
	
	@Override
	public String toString() {
		return "Task{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				", isCompleted=" + isCompleted +
				", id='" + id + '\'' +
				'}';
	}
}
