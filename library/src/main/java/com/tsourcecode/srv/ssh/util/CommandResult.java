package com.tsourcecode.srv.ssh.util;

public interface CommandResult {
    void onCommandResult(boolean result, int commandId, String output);
}
