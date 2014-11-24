package com.tsourcecode.srv.ssh;

import com.stericson.RootTools.execution.CommandCapture;
import com.tsourcecode.srv.ssh.util.AsyncCommandResult;

public class OutputCommand extends CommandCapture {
    private AsyncCommandResult asyncResult;

    public OutputCommand(AsyncCommandResult asyncResult, int id, String... command) {
        super(id, command);
        this.asyncResult = asyncResult;
    }

    public OutputCommand(int id, String... command) {
        super(id, command);
    }

    public OutputCommand(int id, boolean handlerEnabled, String... command) {
        super(id, handlerEnabled, command);
    }

    @Override
    public void commandCompleted(int id, int exitcode) {
        super.commandCompleted(id, exitcode);
        if (asyncResult != null){
            asyncResult.onResult(id, exitcode, this);
        }
    }

    @Override
    public void commandTerminated(int id, String reason) {
        super.commandTerminated(id, reason);
    }

    @Override
    public void commandOutput(int id, String line) {
        super.commandOutput(id, line);
    }

    public OutputCommand(int id, int timeout, String... command) {
        super(id, timeout, command);
    }
}
