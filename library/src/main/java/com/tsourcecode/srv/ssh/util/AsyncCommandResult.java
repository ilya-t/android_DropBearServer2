package com.tsourcecode.srv.ssh.util;

import com.stericson.RootTools.execution.CommandCapture;

public interface AsyncCommandResult {
    void onResult(int id, int exitcode, CommandCapture command);
}
