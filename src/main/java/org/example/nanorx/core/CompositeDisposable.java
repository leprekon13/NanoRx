package org.example.nanorx.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CompositeDisposable implements Disposable {

    private final Set<Disposable> disposables = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean disposed = false;

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            synchronized (disposables) {
                for (Disposable d : disposables) {
                    d.dispose();
                }
                disposables.clear();
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void add(Disposable disposable) {
        if (!disposed) {
            disposables.add(disposable);
        } else {
            disposable.dispose();
        }
    }

    public void remove(Disposable disposable) {
        if (!disposed) {
            disposables.remove(disposable);
        }
    }
}