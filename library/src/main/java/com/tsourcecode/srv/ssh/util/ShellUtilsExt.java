package com.tsourcecode.srv.ssh.util;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import me.shkschneider.dropbearserver2.util.ShellUtils;

public class ShellUtilsExt extends ShellUtils {

    public static void isCommandAvailable(String command, final AsyncResult result){
        execute(new CommandCapture(0, command){
            @Override
            public void commandTerminated(int id, String reason) {
                super.commandTerminated(id, reason);
            }

            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
                result.onResult(exitcode != 127, toString());
/*
                        exitcode == 0 &&
                                this.toString().length() < 50 &&
                                !this.toString().toLowerCase().contains("not found"), toString());
*/
            }
        });
    }

    public static void execute(CommandCapture command) {
        try {
            RootTools.getShell(true).add(command);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    public static void killall(final String processName, final AsyncResult result) {
        isCommandAvailable("killall", new AsyncResult() {
            @Override
            public void onResult(boolean commandAvailable, String message) {
                if (!commandAvailable){
                   execute(new CommandCapture(0,"ps | grep "+processName){
                       @Override
                       public void commandCompleted(int id, int exitcode) {
                           super.commandCompleted(id, exitcode);

                           if (exitcode == 0){//lines found
                               String[] split = this.toString().split(" ");
                               final ArrayList<Integer> numberList = new ArrayList<>();

                               for (String part : split){
                                   if (part.length() > 0){
                                       try {
                                           int intVal = Integer.parseInt(part);
                                           numberList.add(intVal);
                                       } catch (NumberFormatException e) {
                                           e.printStackTrace();
                                       }
                                   }
                               }

                               if (numberList.size() > 0){
                                   for (int pid : numberList){
                                       execute(new CommandCapture(pid, "ps -p "+String.valueOf(pid)){
                                           @Override
                                           public void commandCompleted(int id, int exitcode) {
                                               super.commandCompleted(id, exitcode);

                                               if (exitcode == 0 && this.toString().contains(processName)){
                                                   result.onResult(execute("kill "+id), "killed pid "+id);
                                               }
                                           }
                                       });
                                   }
                               }
                           }else if (exitcode == 1){//lines not found
                               result.onResult(true, "process not found");
                           }else{
                               result.onResult(false, this.toString());
                           }
                       }
                   });

                }else{
                    result.onResult(execute("killall " + processName), "Command: killall " + processName);
                }
            }
        });
    }

}
