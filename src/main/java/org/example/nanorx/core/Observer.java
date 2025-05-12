package org.example.nanorx.core;

public interface Observer<T> {
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
}