/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bestom.CameraQR.camera.callback;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

/**
 * 由于对焦不是一次性完成的任务（手抖），而系统提供的对焦仅有Camera.autoFocus()方法，
 * 因此需要一个线程来不断调用Camera.autoFocus()直到用户满意按下快门为止
 */
public final class AutoFocusManager implements Camera.AutoFocusCallback {

	private static final String TAG = AutoFocusManager.class.getSimpleName();

	private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;
	private static final Collection<String> FOCUS_MODES_CALLING_AF;
	static {
		FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
		FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
		FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
	}

	private boolean active;
	private final boolean useAutoFocus= true;
	private final Camera camera;
	private AsyncTask<?, ?, ?> outstandingTask;

	public AutoFocusManager(Context context, Camera camera) {
		this.camera = camera;
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		Camera.Parameters mParams = camera.getParameters();
		List<String> focusModes = mParams.getSupportedFocusModes();
//		if (focusModes.contains(focusMode)) {
//			mParams.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE );
//		}
		for (String str:focusModes){
			Log.i(TAG,"camera support FocusModes"+str);
		}

		start();
	}

	@Override
	public synchronized void onAutoFocus(boolean success, Camera theCamera) {
		if (active) {
			AutoFocusTask newTask = new AutoFocusTask();
			try {
				if (Build.VERSION.SDK_INT >= 11) {
					newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					newTask.execute();
				}
				outstandingTask = newTask;
			} catch (RejectedExecutionException ree) {
				Log.w(TAG, "Could not request auto focus", ree);
			}
		}
		if (success){
			Log.i(TAG,"对焦成功....");
		}
	}

	private synchronized void start() {
		if (useAutoFocus) {
			active = true;
			try {
				camera.cancelAutoFocus();
				camera.getParameters().setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
				camera.autoFocus(this);
				String currentFocusMode = camera.getParameters().getFocusMode();
				Log.i(TAG, "Current focus mode '" + currentFocusMode);
			}
			catch (RuntimeException re) {
				// Have heard RuntimeException reported in Android 4.0.x+;
				// continue?
				Log.w(TAG, "Unexpected exception while focusing", re);
			}
		}
	}

	public synchronized void stop() {
		if (useAutoFocus) {
			try {
				camera.cancelAutoFocus();
			}
			catch (RuntimeException re) {
				// Have heard RuntimeException reported in Android 4.0.x+;
				// continue?
				Log.w(TAG, "Unexpected exception while cancelling focusing", re);
			}
		}
		if (outstandingTask != null) {
			outstandingTask.cancel(true);
			outstandingTask = null;
		}
		active = false;
	}

	private final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... voids) {
			try {
				Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
			}
			catch (InterruptedException e) {
				// continue
			}
			synchronized (AutoFocusManager.this) {
				if (active) {
					Log.i(TAG,"对焦开始.....");
					start();
				}
			}
			return null;
		}
	}

}
