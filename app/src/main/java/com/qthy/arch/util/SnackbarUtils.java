package com.qthy.arch.util;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * @author zhaohw
 * @date 2020/12/10
 */
public class SnackbarUtils {
	
	public static void showSnackbar(View v, String snackbarText) {
		if (v == null || snackbarText == null) {
			return;
		}
		Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG).show();
	}
}
