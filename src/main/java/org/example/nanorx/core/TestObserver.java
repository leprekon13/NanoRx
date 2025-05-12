package org.example.nanorx.core;

import java.util.ArrayList;
import java.util.List;

public class TestObserver<T> implements Observer<T> {
    public final List<T> receivedItems = new ArrayList<>();
    public Throwable error = null;
    public boolean isCompleted = false;

    @Override
    public void onNext(T item) {
        if (error == null && !isCompleted) {
            receivedItems.add(item);
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
        }
    }
}