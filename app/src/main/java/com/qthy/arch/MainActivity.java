package com.qthy.arch;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.qthy.arch.base.BaseActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BaseActivity {
	// Keys for navigation
	public static final int ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1;
	public static final int DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2;
	public static final int EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3;
	private AppBarConfiguration mAppBarConfiguration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.setStatusBarBackground(R.color.colorPrimaryDark);
		NavigationView navigationView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(
				R.id.tasksFragment, R.id.statisticsFragment)
				.setDrawerLayout(drawer)
				.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);
	}
	
	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}
}