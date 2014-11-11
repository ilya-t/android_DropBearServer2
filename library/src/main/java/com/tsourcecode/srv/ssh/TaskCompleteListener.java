package com.tsourcecode.srv.ssh;

import me.shkschneider.dropbearserver2.task.Task;

public interface TaskCompleteListener {
    void onTaskCompleted(Task task, boolean result, String resultMessage);
}
