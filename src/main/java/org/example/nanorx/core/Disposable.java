package org.example.nanorx.core;

public interface Disposable {
    void dispose();
    boolean isDisposed();
}