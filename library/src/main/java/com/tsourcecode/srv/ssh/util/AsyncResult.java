package com.tsourcecode.srv.ssh.util;

public interface AsyncResult {
    void onResult(boolean result, String message);
}
