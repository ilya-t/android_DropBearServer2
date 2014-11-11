package me.shkschneider.dropbearserver2.task;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import me.shkschneider.dropbearserver2.util.L;
import me.shkschneider.dropbearserver2.util.RootUtils;
import me.shkschneider.dropbearserver2.util.ServerUtils;

public abstract class Task extends AsyncTask<Void, String, Boolean> {

	protected Context mContext = null;
	protected Callback<Boolean> mCallback = null;

	private Boolean mStartInBackground = false;

	public Task(Context context, Callback<Boolean> callback, Boolean startInBackground) {
		mContext = context;
		mCallback = callback;
		mStartInBackground = startInBackground;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		int step = 0;
		int steps = 5;

		// root
		publishProgress("" + step++, "" + steps, "Root access");
		RootUtils.checkRootAccess();

		// busybox
		publishProgress("" + step++, "" + steps, "Busybox");
		RootUtils.checkBusybox();

		// dropbear
		publishProgress("" + step++, "" + steps, "DropBear");
		RootUtils.checkDropbear(mContext);

		ServerUtils.isDropbearRunning(mContext);

		ServerUtils.getIpAddresses(mContext);

		ServerUtils.getDropbearVersion(mContext);

		return (RootUtils.hasRootAccess && RootUtils.hasBusybox && RootUtils.hasDropbear && ServerUtils.dropbearRunning);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (mCallback != null) {
			mCallback.onTaskComplete(Callback.TASK_CHECK, result);
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCancelled(Boolean result) {
		super.onCancelled(result);
	}

	protected Boolean falseWithError(String error) {
		L.d(error);
		return false;
	}

	// Callback

	public interface Callback<T> {

		public static final int TASK_CHECK = 0;
		public static final int TASK_INSTALL = 1;
		public static final int TASK_START = 2;
		public static final int TASK_STOP = 3;
		public static final int TASK_REMOVE = 4;

		public void onTaskComplete(int id, T result);
	}
}