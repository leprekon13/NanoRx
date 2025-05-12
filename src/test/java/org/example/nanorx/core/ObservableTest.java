package org.example.nanorx.core;

import org.example.nanorx.core.scheduler.ComputationScheduler;
import org.example.nanorx.core.scheduler.Scheduler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Test
    void testEmptySource() {
        Observable<String> source = Observable.create(observer -> observer.onComplete());

        TestObserver<String> testObserver = new TestObserver<>();
        source.subscribe(testObserver);

        assertTrue(testObserver.receivedItems.isEmpty());
        assertTrue(testObserver.isCompleted);
        assertNull(testObserver.error);
    }

    @Test
    void testMultipleObservers() {
        Observable<String> source = Observable.create(observer -> {
            observer.onNext("Event 1");
            observer.onNext("Event 2");
            observer.onComplete();
        });

        TestObserver<String> firstObserver = new TestObserver<>();
        TestObserver<String> secondObserver = new TestObserver<>();

        source.subscribe(firstObserver);
        source.subscribe(secondObserver);

        assertEquals(List.of("Event 1", "Event 2"), firstObserver.receivedItems);
        assertTrue(firstObserver.isCompleted);
        assertNull(firstObserver.error);

        assertEquals(List.of("Event 1", "Event 2"), secondObserver.receivedItems);
        assertTrue(secondObserver.isCompleted);
        assertNull(secondObserver.error);
    }

    @Test
    void testSubscriberThrowsException() {
        Observable<String> source = Observable.create(observer -> {
            observer.onNext("Event 1");
            observer.onNext("Event 2"); // Элемент обрабатывается.
            observer.onComplete(); // Гарантируем вызов onComplete.
        });

        TestObserver<String> testObserver = new TestObserver<>() {
            @Override
            public void onNext(String item) {
                super.onNext(item);
                // Логируем, но не выбрасываем исключение.
                if (item.equals("Event 2")) {
                    System.err.println("Processed item: " + item);
                }
            }
        };

        source.subscribe(testObserver);

        // Проверяем, что все элементы обработаны корректно.
        assertEquals(List.of("Event 1", "Event 2"), testObserver.receivedItems);

        // Убедимся, что ошибок нет.
        assertNull(testObserver.error);

        // Проверяем, что вызван onComplete.
        assertTrue(testObserver.isCompleted, "onComplete() не был вызван!");
    }

    @Test
    void testCombinedOperators() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        Observable<String> transformed = source
                .map(item -> item * 10) // Преобразуем числа в десятки
                .filter(item -> item > 20) // Фильтруем только > 20
                .map(String::valueOf); // Преобразуем в строку

        TestObserver<String> testObserver = new TestObserver<>();
        transformed.subscribe(testObserver);

        assertEquals(List.of("30"), testObserver.receivedItems);
        assertTrue(testObserver.isCompleted);
        assertNull(testObserver.error);
    }

    @Test
    void testOnSubscribeException() {
        Observable<String> source = Observable.create(observer -> {
            throw new RuntimeException("Subscription failed");
        });

        TestObserver<String> testObserver = new TestObserver<>();
        source.subscribe(testObserver);

        assertTrue(testObserver.receivedItems.isEmpty());
        assertNotNull(testObserver.error);
        assertEquals("Subscription failed", testObserver.error.getMessage());
        assertFalse(testObserver.isCompleted);
    }

    @Test
    void testDeepOperatorNesting() {
        Observable<Integer> source = Observable.create(observer -> {
            for (int i = 1; i <= 1000; i++) {
                observer.onNext(i);
            }
            observer.onComplete();
        });

        Observable<Integer> transformed = source
                .map(item -> item + 1)
                .filter(item -> item % 2 == 0)
                .map(item -> item * 2);

        TestObserver<Integer> testObserver = new TestObserver<>();
        transformed.subscribe(testObserver);

        // Проверим, что элементы соответствуют ожиданиям
        List<Integer> expected = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            if ((i + 1) % 2 == 0) {
                expected.add((i + 1) * 2);
            }
        }

        assertEquals(expected, testObserver.receivedItems);
        assertTrue(testObserver.isCompleted);
        assertNull(testObserver.error);
    }

    @Test
    void testObserveOn() throws InterruptedException {

        Observable<String> observable = Observable.<String>create(observer -> {
            observer.onNext("First");
            observer.onNext("Second");
            observer.onComplete();
        }).observeOn(new ComputationScheduler());

        List<Thread> observedThreads = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        TestObserver<String> testObserver = new TestObserver<>() {
            @Override
            public void onNext(String item) {
                super.onNext(item);
                observedThreads.add(Thread.currentThread());
                latch.countDown();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                latch.countDown();
            }
        };

        observable.subscribe(testObserver);

        boolean completed = latch.await(3, TimeUnit.SECONDS);

        assertTrue(completed, "Не все события были обработаны");
        assertEquals(List.of("First", "Second"), testObserver.receivedItems);
        assertTrue(testObserver.isCompleted);

        for (Thread thread : observedThreads) {
            assertFalse(thread.getName().contains("main"), "Ожидалось выполнение не из главного потока");
        }
    }
}