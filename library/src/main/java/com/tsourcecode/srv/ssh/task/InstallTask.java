package com.tsourcecode.srv.ssh.task;

import android.content.Context;

import com.stericson.RootTools.execution.CommandCapture;
import com.tsourcecode.srv.ssh.OutputCommand;
import com.tsourcecode.srv.ssh.R;
import com.tsourcecode.srv.ssh.util.AsyncCommandResult;
import com.tsourcecode.srv.ssh.util.AsyncResult;
import com.tsourcecode.srv.ssh.util.ShellUtilsExt;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import me.shkschneider.dropbearserver2.util.ServerUtils;
import me.shkschneider.dropbearserver2.util.Utils;

public class InstallTask implements Runnable {
    private final Context context;
    private final AsyncResult installResult;
    private final String tmp;
    private AtomicInteger cmdCount = new AtomicInteger();
    private ArrayList<String> log = new ArrayList<>();
    private volatile boolean scriptSent;

    private final String ssh = "/system/xbin/ssh";
    private final String scp = "/system/xbin/scp";
    private final String dbclient = "/system/xbin/dbclient";
    private final String sftp_server = "system/xbin/sftp-server";
    private final String dropbear;
    private final String dropbearkey;
    private final String banner;
    private final String host_rsa;
    private final String host_dss;
    private final String authorized_keys;

    private volatile boolean isFailed;

    private AsyncCommandResult onCommandResult = new AsyncCommandResult() {
            @Override
            public void onResult(int id, int exitcode, CommandCapture command) {
                if (exitcode == 0){
                    log.add(String.valueOf(cmdCount.intValue())+" - "+"(OK) "+command.getCommand());
                }else{
                    log.add(String.valueOf(cmdCount.intValue())+" - "+"("+exitcode+") "+command.getCommand() + " : "+ command.toString());
                }
                if ((cmdCount.decrementAndGet() == 0 && scriptSent) || exitcode != 0){
                    installResult.onResult(exitcode == 0, getLog());
                    isFailed = true;
                }
            }
    };

    private String getLog() {
        String result = "";
        for (String line : log){
            result += line + "\n";
        }
        return result;
    }

    public InstallTask(AsyncResult installResult, Context context) {
        this.context = context;
        this.installResult = installResult;
        tmp = ServerUtils.getLocalDir(context) + "/tmp";
        dropbear = ServerUtils.getLocalDir(context) + "/dropbear";
        dropbearkey = ServerUtils.getLocalDir(context) + "/dropbearkey";
        banner = ServerUtils.getLocalDir(context) + "/banner";
        host_rsa = ServerUtils.getLocalDir(context) + "/host_rsa";
        host_dss = ServerUtils.getLocalDir(context) + "/host_dss";
        authorized_keys = ServerUtils.getLocalDir(context) + "/authorized_keys";
        Executors.newSingleThreadExecutor().submit(this);
    }

    private boolean install() {
        log.add("dropbear");
        if (!installFromRaw(dropbear, R.raw.dropbear)) return false;
        log.add("dropbear-key");
        if (!installFromRaw(dropbearkey, R.raw.dropbearkey)) return false;

        if (waitForCommands()) return true;

        // Read-Write
        log.add("Remount Read-Write");
        if (!Utils.remountReadWrite("/system")) {
            log.add("ERROR:"+"/system RW");
            return false;
        }


        log.add("SSH binary");
        if (!copyFromRaw(ssh, R.raw.ssh)) return false;
        log.add("SCP binary");
        if (!copyFromRaw(scp, R.raw.scp)) return false;
        log.add("DBClient binary");
        if (!copyFromRaw(dbclient, R.raw.dbclient)) return false;
        log.add("SFTP binary");
        if (!copyFromRaw(sftp_server, R.raw.sftp_server)) return false;

        if (waitForCommands()) return true;

        // Read-Only
        log.add("Remount Read-Only");
        if (!Utils.remountReadOnly("/system")) {
            log.add("ERROR:"+"/system RO");
            return false;
        }

        // banner
        log.add("Banner");
        if (new File(banner).exists()){
            execute("rm -f " + banner);
        }

        if (!Utils.copyRawFile(context, R.raw.banner, banner)) {
            log.add("ERROR:"+banner);
            return false;
        }else{
            log.add("new file " + banner);
        }

        execute("chmod 644 " + banner);

        // authorized_keys
        log.add("Authorized keys");
        if (new File(authorized_keys).exists()){
            execute("rm -f " + authorized_keys);
        }

        if (ServerUtils.createIfNeeded(authorized_keys)){
            log.add("new file " + authorized_keys);
        }

        execute("chmod 644 " + authorized_keys);

        // host_rsa
        log.add("Host RSA key");
        if (new File(host_rsa).exists()){
            execute("rm -f " + host_rsa);
        }

        if (waitForCommands()) return true;

        execute(ServerUtils.getLocalDir(null) + "/dropbearkey -t rsa -f " + host_rsa);

        if (waitForCommands()) return true;

        execute("chown 0:0 " + host_rsa);

        // host_dss
        log.add("Host DSS key");
        if (new File(host_dss).exists()){
            execute("rm -f " + host_dss);
        }

        if (waitForCommands()) return true;

        execute(ServerUtils.getLocalDir(null) + "/dropbearkey -t dss -f " + host_dss);

        if (waitForCommands()) return true;

        execute("chown 0:0 " + host_dss);

        if (waitForCommands()) return true;

        // /data/local
        log.add("Permissions");
        execute("chmod 755 /data/local");

        scriptSent = true;
        return true;
    }

    private void execute(String command) {
        cmdCount.incrementAndGet();
        ShellUtilsExt.execute(new OutputCommand(onCommandResult, 1, command));

        if (command.startsWith("rm -f")){
            waitForCommands();
        }
    }

    private boolean waitForCommands() {
        while (cmdCount.intValue() > 0){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isFailed){
                return true;
            }
        }
        return false;
    }

    private boolean copyFromRaw(String fileName, int resId) {
        if (!Utils.copyRawFile(context, resId, tmp)) {
            log.add("ERROR:"+tmp);
            return false;
        }

        execute("rm -f " + fileName);
        execute("cp " + tmp + " " + fileName);
        execute("rm -f " + tmp);
        execute("chmod 755 " + fileName);

        return true;
    }

    private boolean installFromRaw(String destination, int sourceResId) {
        if (new File(destination).exists()){
            execute("rm -f " + destination);
            waitForCommands();
        }

        if (!Utils.copyRawFile(context, sourceResId, destination)) {
            installResult.onResult(false, "failed to copy raw file: "+destination);
            return false;
        }

        execute("chmod 755 " + destination);
        return true;
    }

    @Override
    public void run() {
        if (!install()){
            installResult.onResult(false, getLog());
        }
    }
}
