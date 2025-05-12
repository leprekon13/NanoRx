package org.example.nanorx.core.scheduler;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    @Test
    void testSingleThreadScheduler() throws InterruptedException {
        Scheduler scheduler = new SingleThreadScheduler();
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        CountDownLatch latch = new CountDownLatch(1);
        scheduler.execute(() -> {
            taskExecuted.set(true);
            latch.countDown();
        });

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        assertTrue(completed, "Task in SingleThreadScheduler should complete");
        assertTrue(taskExecuted.get(), "Task in SingleThreadScheduler should be executed");
    }

    @Test
    void testComputationScheduler() throws InterruptedException {
        Scheduler scheduler = new ComputationScheduler();
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        CountDownLatch latch = new CountDownLatch(1);
        scheduler.execute(() -> {
            taskExecuted.set(true);
            latch.countDown();
        });

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        assertTrue(completed, "Task in ComputationScheduler should complete");
        assertTrue(taskExecuted.get(), "Task in ComputationScheduler should be executed");
    }

    @Test
    void testIOThreadScheduler() throws InterruptedException {
        Scheduler scheduler = new IOThreadScheduler();
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        CountDownLatch latch = new CountDownLatch(1);
        scheduler.execute(() -> {
            taskExecuted.set(true);
            latch.countDown();
        });

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        assertTrue(completed, "Task in IOThreadScheduler should complete");
        assertTrue(taskExecuted.get(), "Task in IOThreadScheduler should be executed");
    }

    @Test
    void testMultipleTasksInSchedulers() throws InterruptedException {
        Scheduler singleThreadScheduler = new SingleThreadScheduler();
        Scheduler computationScheduler = new ComputationScheduler();
        Scheduler ioThreadScheduler = new IOThreadScheduler();

        // Test multiple tasks
        CountDownLatch latch = new CountDownLatch(6);

        Runnable task = latch::countDown;

        singleThreadScheduler.execute(task);
        singleThreadScheduler.execute(task);

        computationScheduler.execute(task);
        computationScheduler.execute(task);

        ioThreadScheduler.execute(task);
        ioThreadScheduler.execute(task);

        boolean completed = latch.await(2, TimeUnit.SECONDS);

        assertTrue(completed, "All tasks across schedulers should complete");
    }
}