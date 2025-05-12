package org.example.nanorx.core;

import java.util.function.Function;

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

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new Observable<>(observer ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            R result = mapper.apply(item);
                            observer.onNext(result);
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        observer.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public interface OnSubscribe<T> {
        void subscribe(Observer<? super T> observer);
    }
}