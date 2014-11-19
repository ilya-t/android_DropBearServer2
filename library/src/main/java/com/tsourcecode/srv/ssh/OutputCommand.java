package com.tsourcecode.srv.ssh;

import com.stericson.RootTools.execution.CommandCapture;

public class OutputCommand extends CommandCapture {
    public OutputCommand(int id, String... command) {
        super(id, command);
    }

    public OutputCommand(int id, boolean handlerEnabled, String... command) {
        super(id, handlerEnabled, command);
    }

    @Override
    public void commandCompleted(int id, int exitcode) {
        super.commandCompleted(id, exitcode);
    }

    @Override
    public void commandOutput(int id, String line) {
        super.commandOutput(id, line);
    }

    public OutputCommand(int id, int timeout, String... command) {
        super(id, timeout, command);
    }
}
