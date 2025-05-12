package org.example.nanorx.core;

import org.example.nanorx.core.scheduler.IOThreadScheduler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void testObservableEmitsOnNextAndOnComplete() {
        Observable<String> observable = Observable.<String>create(observer -> {
            observer.onNext("Hello");
            observer.onComplete();
        });

        TestObserver<String> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);

        assertEquals(1, testObserver.receivedItems.size(), "Expected 1 emitted item");
        assertEquals("Hello", testObserver.receivedItems.get(0), "Unexpected emitted value");
        assertTrue(testObserver.isCompleted, "Observable should complete");
        assertNull(testObserver.error, "No error expected");
    }

    @Test
    void testObservableHandlesError() {
        Observable<String> observable = Observable.<String>create(observer -> {
            throw new RuntimeException("Test Error");
        });

        TestObserver<String> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);

        assertNotNull(testObserver.error, "Error should not be null");
        assertEquals("Test Error", testObserver.error.getMessage(), "Unexpected error message");
    }

    @Test
    void testFilterOperator() {
        Observable<Integer> source = Observable.<Integer>create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Observable<Integer> filtered = source.filter(item -> item % 2 == 0);

        TestObserver<Integer> testObserver = new TestObserver<>();
        filtered.subscribe(testObserver);

        assertEquals(1, testObserver.receivedItems.size(), "Expected 1 emitted item");
        assertEquals(2, testObserver.receivedItems.get(0), "Unexpected value after filter");
        assertTrue(testObserver.isCompleted, "Observable should complete");
        assertNull(testObserver.error, "No error expected");
    }

    @Test
    void testMapOperator() {
        Observable<Integer> source = Observable.<Integer>create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        Observable<String> mapped = source.map(String::valueOf);

        TestObserver<String> testObserver = new TestObserver<>();
        mapped.subscribe(testObserver);

        assertEquals(2, testObserver.receivedItems.size(), "Expected 2 emitted items");
        assertEquals("1", testObserver.receivedItems.get(0), "Unexpected value at index 0");
        assertEquals("2", testObserver.receivedItems.get(1), "Unexpected value at index 1");
        assertTrue(testObserver.isCompleted, "Observable should complete");
        assertNull(testObserver.error, "No error expected");
    }

    @Test
    void testFlatMapOperator() {
        Observable<Integer> source = Observable.<Integer>create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        Observable<String> flattened = source.flatMap(item ->
            Observable.<String>create(innerObserver -> {
                innerObserver.onNext(item + "A");
                innerObserver.onNext(item + "B");
                innerObserver.onComplete();
            })
        );

        TestObserver<String> testObserver = new TestObserver<>();
        flattened.subscribe(testObserver);

        assertEquals(4, testObserver.receivedItems.size(), "Expected 4 emitted items");
        assertEquals(List.of("1A", "1B", "2A", "2B"), testObserver.receivedItems, "Unexpected flatMapped values");
        assertTrue(testObserver.isCompleted, "Observable should complete");
        assertNull(testObserver.error, "No error expected");
    }

    @Test
    void testObserveOnWithScheduler() throws InterruptedException {
        Observable<String> observable = Observable.<String>create(observer -> {
            observer.onNext("Hello");
            observer.onComplete();
        }).observeOn(new IOThreadScheduler());

        TestObserver<String> testObserver = new TestObserver<>();

        CountDownLatch latch = new CountDownLatch(2);
        testObserver.setOnComplete(() -> latch.countDown());
        testObserver.setOnNext(() -> latch.countDown());

        observable.subscribe(testObserver);

        boolean completed = latch.await(1, TimeUnit.SECONDS);

        assertTrue(completed, "TestObserver should complete");
        assertEquals(List.of("Hello"), testObserver.receivedItems, "Unexpected emitted items");
        assertTrue(testObserver.isCompleted, "Observable should complete");
        assertNull(testObserver.error, "No error expected");
    }

    @Test
    void testSubscribeReturnsDisposable() {
        Observable<Integer> observable = Observable.<Integer>create(observer -> {
            observer.onNext(1);
            observer.onComplete();
        });

        Disposable disposable = observable.subscribe(new TestObserver<>());

        assertNotNull(disposable, "Disposable object should not be null");
        assertFalse(disposable.isDisposed(), "Disposable should not be disposed immediately");

        disposable.dispose();

        assertTrue(disposable.isDisposed(), "Disposable should be disposed after calling dispose()");
    }
}