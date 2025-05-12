package org.example.nanorx.core.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {
    private final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void execute(Runnable task) {
        System.out.println("Task submitted to ComputationScheduler: " + Thread.currentThread().getName());
        executor.submit(task);
    }
}