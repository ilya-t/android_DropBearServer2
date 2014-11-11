package me.shkschneider.dropbearserver2.task;

import android.content.Context;

import me.shkschneider.dropbearserver2.util.RootUtils;
import me.shkschneider.dropbearserver2.util.ServerUtils;

public class Checker extends Task {

	public Checker(Context context, Callback<Boolean> callback) {
		super(context, callback, false);
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
}