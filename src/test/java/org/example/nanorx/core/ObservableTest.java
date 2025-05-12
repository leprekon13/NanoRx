package org.example.nanorx.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;


class ObservableTest {

    @Test
    void testObservableEmitsOnNextAndOnComplete() {
        // Создаем Observable, которое отправляет элемент "Hello" и завершает поток
        Observable<String> observable = Observable.create(observer -> {
            observer.onNext("Hello");
            observer.onComplete();
        });

        // Создаем нашего тестового наблюдателя
        TestObserver<String> testObserver = new TestObserver<>();
        // Подписываем наблюдателя на Observable
        observable.subscribe(testObserver);

        // Проверяем, что метод onNext получил элемент "Hello"
        assertEquals("Hello", testObserver.item);
        // Проверяем, что метод onComplete был вызван
        assertTrue(testObserver.isCompleted);
        // Проверяем, что ошибок не было
        assertNull(testObserver.error);
    }

    @Test
    void testObservableHandlesError() {
        // Создаем Observable, которое выбрасывает ошибку
        Observable<String> observable = Observable.create(observer -> {
            throw new RuntimeException("Test Error");
        });

        // Создаем нашего тестового наблюдателя
        TestObserver<String> testObserver = new TestObserver<>();
        // Подписываем наблюдателя на Observable
        observable.subscribe(testObserver);

        // Проверяем, что метод onError получил ошибку
        assertNotNull(testObserver.error);
        // Убеждаемся, что сообщение ошибки корректное
        assertEquals("Test Error", testObserver.error.getMessage());
    }

    // Тестовый наблюдатель для проверки работы Observable
    static class TestObserver<T> implements Observer<T> {
        T item; // Элемент, полученный через onNext
        Throwable error; // Ошибка, полученная через onError
        boolean isCompleted = false; // Флаг, показывающий, был ли вызван onComplete

        @Override
        public void onNext(T item) {
            // Сохраняем полученный элемент
            this.item = item;
        }

        @Override
        public void onError(Throwable throwable) {
            // Сохраняем полученную ошибку
            this.error = throwable;
        }

        @Override
        public void onComplete() {
            // Устанавливаем флаг завершения потока
            this.isCompleted = true;
        }
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

        List<String> results = new ArrayList<>();
        TestObserver<String> testObserver = new TestObserver<>() {
            @Override
            public void onNext(String item) {
                results.add(item);
            }
        };

        mapped.subscribe(testObserver);

        assertEquals(List.of("1", "2", "3"), results);
        assertTrue(testObserver.isCompleted);
        assertNull(testObserver.error);
    }

}