package org.example.nanorx.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TestObserver<T> implements Observer<T> {
    public final List<T> receivedItems = new ArrayList<>();
    public Throwable error = null;
    public boolean isCompleted = false;

    private Runnable onNextCallback = null;
    private Runnable onCompleteCallback = null;

    @Override
    public void onNext(T item) {
        if (error == null && !isCompleted) {
            receivedItems.add(item);
            if (onNextCallback != null) {
                onNextCallback.run();
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (!isCompleted) {
            this.error = throwable;
            this.isCompleted = false;
        }
    }

    @Override
    public void onComplete() {
        if (error == null) {
            this.isCompleted = true;
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
            }
        }
    }

    // Устанавливаем callback для обработки события onNext
    public void setOnNext(Runnable onNextCallback) {
        this.onNextCallback = onNextCallback;
    }

    // Устанавливаем callback для обработки события onComplete
    public void setOnComplete(Runnable onCompleteCallback) {
        this.onCompleteCallback = onCompleteCallback;
    }
}