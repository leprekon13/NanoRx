package org.example.nanorx.core;

public class Observable<T> {
    private final OnSubscribe<T> onSubscribe;

    public Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public void subscribe(Observer<? super T> observer) {
        try {
            onSubscribe.subscribe(observer);
        } catch (Throwable t) {
            observer.onError(t);
        }
    }

    public interface OnSubscribe<T> {
        void subscribe(Observer<? super T> observer);
    }
}