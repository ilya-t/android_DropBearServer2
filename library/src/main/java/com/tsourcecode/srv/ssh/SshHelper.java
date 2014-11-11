package com.tsourcecode.srv.ssh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import me.shkschneider.dropbearserver2.task.Checker;
import me.shkschneider.dropbearserver2.task.Installer;
import me.shkschneider.dropbearserver2.task.Remover;
import me.shkschneider.dropbearserver2.task.Starter;
import me.shkschneider.dropbearserver2.task.Stopper;
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
        STOPPED
    }
    private Status status = Status.UNKNOWN;


    private final Context context;
    private boolean isChecked;

    public SshHelper(Context context) {
        this.context = context.getApplicationContext();
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


    public void install(final TaskCompleteListener completeListener){
        new Installer(context, new Task.Callback<Boolean>() {
            @Override
            public void onTaskComplete(int id, Boolean result) {
                if (completeListener != null){
                    completeListener.onTaskCompleted(null, result, null);
                }
            }
        }).execute();
    }

    public String getVerion(){
        return ServerUtils.dropbearVersion;
    }

    public void start(final TaskCompleteListener completeListener){
        new Starter(context, new Task.Callback<Boolean>() {
            @Override
            public void onTaskComplete(int id, Boolean result) {
                if (completeListener != null){
                    completeListener.onTaskCompleted(null, result, null);
                }
            }
        }, false).execute();
    }

    public void stop(final TaskCompleteListener completeListener){
        new Stopper(context, new Task.Callback<Boolean>() {
            @Override
            public void onTaskComplete(int id, Boolean result) {
                if (completeListener != null){
                    completeListener.onTaskCompleted(null, result,null);
                }
            }
        }, false).execute();
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

    public void addPublicKey(){
        //TODO implement later
    }

    public void removePublicKey(){
        final List<String> pubKeys = ServerUtils.getPublicKeys(ServerUtils.getLocalDir(context) + "/authorized_keys");
        if (pubKeys.size() > 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(context).setSingleChoiceItems(pubKeys.toArray(new String[pubKeys.size()]), 0, null).create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setIcon(android.R.drawable.ic_dialog_info);
            alertDialog.setTitle("Remove a pubkey");
            alertDialog.setMessage(null);
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                //@Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

                //@Override
                public void onClick(DialogInterface dialog, int which) {
                    ListView listView = ((AlertDialog) dialog).getListView();
                    ServerUtils.removePublicKey(pubKeys.get((int) listView.getSelectedItemId()), ServerUtils.getLocalDir(context) + "/authorized_keys");
                    Toast.makeText(context, "Pubkey removed", Toast.LENGTH_SHORT).show();
                }
            });
            alertDialog.show();
        }else {
            Toast.makeText(context, "No pubkeys to remove", Toast.LENGTH_SHORT).show();
        }
    }
}
