package com.qthy.arch.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

/**
 * @author 52512
 * @date 2020/12/12
 */
public abstract class BaseActivity extends AppCompatActivity {
	
	protected NavController navController;
	
	protected abstract int getNavCtrlResId();
	
	protected abstract int getLayoutResId();
	
	protected int getToolbarResId() {
		return 0;
	}
	
	protected abstract void initView(Bundle savedInstanceState);
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		
		if (getToolbarResId() != 0) {
			Toolbar toolbar = findViewById(getToolbarResId());
			setSupportActionBar(toolbar);
		}
		
		navController = Navigation.findNavController(this, getNavCtrlResId());
		
		initView(savedInstanceState);
	}
	
	@Override
	public boolean onSupportNavigateUp() {
		return super.onSupportNavigateUp();
	}
}
