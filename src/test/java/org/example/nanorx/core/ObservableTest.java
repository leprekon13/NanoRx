package org.example.nanorx.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void testObservableEmitsOnNextAndOnComplete() {
        Observable<String> observable = Observable.create(observer -> {
            observer.onNext("Hello");
            observer.onComplete();
        });

        TestObserver<String> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);

        assertEquals(List.of("Hello"), testObserver.receivedItems);
        assertTrue(testObserver.isCompleted);
        assertNull(testObserver.error);
    }

    @Test
    void testObservableHandlesError() {
        Observable<String> observable = Observable.create(observer -> {
            throw new RuntimeException("Test Error");
        });

        TestObserver<String> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);

        assertNotNull(testObserver.error);
        assertEquals("Test Error", testObserver.error.getMessage());
    }

    @Test
    void testFilterOperator() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onNext(4);
            observer.onComplete();
        });

        Observable<Integer> filtered = source.filter(item -> item % 2 == 0);

        TestObserver<Integer> testObserver = new TestObserver<>();
        filtered.subscribe(testObserver);

        assertEquals(List.of(2, 4), testObserver.receivedItems);
        assertTrue(testObserver.isCompleted);
        assertNull(testObserver.error);
    }

    @Test
    void testMapOperator() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Observable<String> mapped = source.map(String::valueOf);

        TestObserver<String> testObserver = new TestObserver<>();
        mapped.subscribe(testObserver);

        assertEquals(List.of("1", "2", "3"), testObserver.receivedItems);
        assertTrue(testObserver.isCompleted);
        assertNull(testObserver.error);
    }

    @Test
    void testFilterHandlesPredicateException() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        Observable<Integer> filtered = source.filter(item -> {
            if (item == 2) {
                throw new RuntimeException("Predicate failed");
            }
            return true;
        });

        TestObserver<Integer> testObserver = new TestObserver<>();
        filtered.subscribe(testObserver);

        assertEquals(List.of(1), testObserver.receivedItems);
        assertNotNull(testObserver.error);
        assertEquals("Predicate failed", testObserver.error.getMessage());
        assertFalse(testObserver.isCompleted);
    }
}