/*
 * Pawel Nadolski <http://stackoverflow.com/questions/10319471/android-is-the-groupid-of-sdcard-rw-always-1015/>
 */
package me.shkschneider.dropbearserver2.task;

import android.content.Context;

import com.tsourcecode.srv.ssh.SshConfig;

import me.shkschneider.dropbearserver2.util.L;
import me.shkschneider.dropbearserver2.util.ServerUtils;
import me.shkschneider.dropbearserver2.util.ShellUtils;

public class Starter extends Task {

	private static final int ID_ROOT = 0;

	public Starter(Context context, Callback<Boolean> callback, Boolean startInBackground) {
		super(context, callback, startInBackground);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		String login = "root";
        SshConfig config = new SshConfig(mContext);
		String banner = ServerUtils.getLocalDir(mContext) + "/banner";
		String hostRsa = ServerUtils.getLocalDir(mContext) + "/host_rsa";
		String hostDss = ServerUtils.getLocalDir(mContext) + "/host_dss";
		String authorizedKeys = ServerUtils.getLocalDir(mContext) + "/authorized_keys";
		Integer listeningPort = 22;
		String pidFile = ServerUtils.getLocalDir(mContext) + "/pid";

		String command = ServerUtils.getLocalDir(mContext) + "/dropbear";
		command = command.concat(" -A -N " + login);
		if (config.isAllowPassword()) {
			command = command.concat(" -C " + config.getPassword());
		}else{
			command = command.concat(" -s");
		}
		command = command.concat(" -r " + hostRsa + " -d " + hostDss);
		command = command.concat(" -R " + authorizedKeys);
		command = command.concat(" -U " + ID_ROOT + " -G " + ID_ROOT);
		command = command.concat(" -p " + listeningPort);
		command = command.concat(" -P " + pidFile);
		command = command.concat(" -b " + banner);

		L.d("Command: " + command);
		if (!ShellUtils.execute(command)) {
			return falseWithError("execute(" + command + ")");
		}

		return true;
	}
}