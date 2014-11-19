/*
 * Muzikant <http://muzikant-android.blogspot.fr/2011/02/how-to-get-root-access-and-execute.html>
 */
package me.shkschneider.dropbearserver2.util;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class ShellUtils {

	public static Boolean execute(String command) {
		CommandCapture commands = new CommandCapture(0, command);
		try {
			RootTools.getShell(true).add(commands);//.waitForFinish();
			return true;
		}
/*
		catch (InterruptedException e) {
			L.e("InterruptedException: " + e.getMessage());
		}
*/
		catch (IOException e) {
			L.e("IOException: " + e.getMessage());
		}
		catch (TimeoutException e) {
			L.e("TimeoutException: " + e.getMessage());
		} catch (RootDeniedException e) {
			L.e("RootDeniedException: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
	}

	public static Boolean mkdir(String path) {
		return execute("mkdir " + path);
	}

	public static Boolean mkdirRecursive(String path) {
		return execute("mkdir -p " + path);
	}

	public static Boolean chown(String path, String owner) {
		return execute("chown " + owner + " " + path);
	}

	public static Boolean chownRecursive(String path, String owner) {
		return execute("chown -R " + owner + " " + path);
	}

	public static Boolean chmod(String path, String chmod) {
		return execute("chmod " + chmod + " " + path);
	}

	public static Boolean chmodRecursive(String path, String chmod) {
		return execute("chmod -R " + chmod + " " + path);
	}

	public static Boolean touch(String path) {
		return execute("echo -n '' > " + path);
	}

	public static Boolean rm(String path) {
		return execute("rm -f " + path);
	}

	public static Boolean rmRecursive(String path) {
		return execute("rm -rf " + path);
	}

	public static Boolean mv(String srcPath, String destPath) {
		return execute("mv " + srcPath + " " + destPath);
	}

	public static Boolean cp(String srcPath, String destPath) {
		return execute("cp " + srcPath + " " + destPath);
	}

	public static Boolean cpRecursive(String srcPath, String destPath) {
		return execute("cp -r " + srcPath + " " + destPath);
	}

	public static Boolean echoToFile(String text, String path) {
		return execute("echo '" + text + "' > " + path);
	}

	public static Boolean echoAppendToFile(String text, String path) {
		return execute("echo '" + text + "' >> " + path);
	}

	public static Boolean lnSymbolic(String srcPath, String destPath) {
		return execute("ln -s " + srcPath + " " + destPath);
	}

	public static Boolean killall(String processName) {
		return execute("killall " + processName);
	}

	public static Boolean remountReadWrite(String path) {
		return RootTools.remount(path, "RW");
	}

	public static Boolean remountReadOnly(String path) {
		return RootTools.remount(path, "RO");
	}
}
