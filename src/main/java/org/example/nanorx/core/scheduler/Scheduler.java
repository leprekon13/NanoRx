package org.example.nanorx.core.scheduler;

public interface Scheduler {
    void execute(Runnable task);
}