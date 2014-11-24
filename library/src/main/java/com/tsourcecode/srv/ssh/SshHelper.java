package com.tsourcecode.srv.ssh;

import android.content.Context;

import com.tsourcecode.srv.ssh.task.InstallTask;
import com.tsourcecode.srv.ssh.util.AsyncResult;
import com.tsourcecode.srv.ssh.util.ShellUtilsExt;

import java.util.List;

import me.shkschneider.dropbearserver2.task.Checker;
import me.shkschneider.dropbearserver2.task.Remover;
import me.shkschneider.dropbearserver2.task.Task;
import me.shkschneider.dropbearserver2.util.RootUtils;
import me.shkschneider.dropbearserver2.util.ServerUtils;

public class SshHelper {
    public enum Status{
        UNKNOWN,
        NO_ROOT,
        NO_BUSY_BOX,
        NOT_INSTALLED,
        STARTED,
        STOPPED;
    }
    private Status status = Status.UNKNOWN;
    private SshConfig sshConfig;
    private final Context context;

    private boolean isChecked;
    public SshHelper(Context context) {
        this.context = context.getApplicationContext();
        sshConfig = new SshConfig(context);
        check(null);
    }

    public void check(final TaskCompleteListener completeListener){
        new Checker(context, new Task.Callback<Boolean>() {
            @Override
            public void onTaskComplete(int id, Boolean result) {
                if (!RootUtils.hasDropbear) {
                    status = Status.NOT_INSTALLED;
                    //stdout("Server not installed");
                }
                else if (!RootUtils.hasRootAccess) {
                    status = Status.NO_ROOT;
                    //stdout("Server not ready (root access denied)");
                }
                else if (!RootUtils.hasBusybox) {
                    status = Status.NO_BUSY_BOX;
                    //stdout("Server not ready (busybox missing)");
                }
                else if (ServerUtils.dropbearRunning) {
                    status = Status.STARTED;
                    //stdout("Server started");

                    //stdout("$ ssh <IP>");
                    //stdout("$ sshfs <IP>:/sdcard <...>");
                    //stdout("$ scp <...> <IP>");
                    //stdout("$ scp <IP>:<...> <...>");
                    //stdout("$ sftp -s /system/xbin/sftp-server <IP>");
//                    for (String ipAddress : ServerUtils.ipAddresses) {
//                        stdout("IP: " + ipAddress);
//                    }
                }else {
                    status = Status.STOPPED;
//                    stdout("Server stopped");
                }

                isChecked = true;

                if (completeListener != null){
                    completeListener.onTaskCompleted(null, result, null);
                }
            }
        }).execute();
    }

    public Status getStatus(){
        return status;
    }

    public void install(AsyncResult installResult){
        new InstallTask(installResult, context);
    }

    public String getVerion(){
        return ServerUtils.dropbearVersion;
    }

    public void start(final AsyncResult result){//final TaskCompleteListener completeListener){
        final int ID_ROOT = 0;
        String login = "root";
        SshConfig config = new SshConfig(context);
        String banner = ServerUtils.getLocalDir(context) + "/banner";
        String hostRsa = ServerUtils.getLocalDir(context) + "/host_rsa";
        String hostDss = ServerUtils.getLocalDir(context) + "/host_dss";
        String authorizedKeys = ServerUtils.getLocalDir(context) + "/authorized_keys";
        Integer listeningPort = 22;
        String pidFile = ServerUtils.getLocalDir(context) + "/pid";

        String command = ServerUtils.getLocalDir(context) + "/dropbear";
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

        ShellUtilsExt.execute(new OutputCommand(0, command){
            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                result.onResult(exitcode == 0, toString());
            }
        });
}

    public void stop(AsyncResult result){
        ShellUtilsExt.killall("dropbear", result);
/*
        new Stopper(context, new Task.Callback<Boolean>() {
            @Override
            public void onTaskComplete(int id, Boolean result) {
                if (completeListener != null){
                    completeListener.onTaskCompleted(null, result,null);
                }
            }
        }, false).execute();
*/
    }

    public void remove(final TaskCompleteListener completeListener){
        new Remover(context, new Task.Callback<Boolean>() {
            @Override
            public void onTaskComplete(int id, Boolean result) {
                if (completeListener != null){
                    completeListener.onTaskCompleted(null, result, null);
                }
            }
        }).execute();
    }

    public boolean addPublicKey(String publicKey){
        List<String> publicKeys = ServerUtils.getPublicKeys(ServerUtils.getLocalDir(context) + "/authorized_keys");
        if (!publicKeys.contains(publicKey)) {
            return ServerUtils.addPublicKey(publicKey, ServerUtils.getLocalDir(context) + "/authorized_keys");
        }
        return false;
    }

    public boolean removePublicKey(String publicKey){
        return ServerUtils.removePublicKey(publicKey, ServerUtils.getLocalDir(context) + "/authorized_keys");
    }

    public void setConfig(SshConfig sshConfig) {
        this.sshConfig = sshConfig;
    }

    public boolean isRunning(){
        return false;
    }
}
