package org.example.nanorx.core;

import java.util.function.Function;
import java.util.function.Predicate;
import org.example.nanorx.core.scheduler.Scheduler;


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
        onSubscribe.subscribe(new Observer<>() {
            private boolean terminated = false; // Если true, подписка завершена.

            @Override
            public void onNext(T item) {
                if (terminated) return; // Не обрабатывать элементы после завершения.
                try {
                    observer.onNext(item);
                } catch (Throwable t) {
                    onError(t); // Завершить подписку при ошибке.
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (terminated) return;
                terminated = true; // Завершить подписку с ошибкой.
                observer.onError(throwable);
            }

            @Override
            public void onComplete() {
                if (terminated) return;
                terminated = true; // Завершить подписку успешно.
                observer.onComplete();
            }
        });
    } catch (Throwable t) {
        observer.onError(t); // Ловим любые ошибки в процессе подписки.
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

    public Observable<T> filter(Predicate<? super T> predicate) {
        return new Observable<>(observer ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
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

public Observable<T> observeOn(Scheduler scheduler) {
    return new Observable<>(observer -> 
        subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                scheduler.execute(() -> observer.onNext(item));
            }

            @Override
            public void onError(Throwable throwable) {
                scheduler.execute(() -> observer.onError(throwable));
            }

            @Override
            public void onComplete() {
                scheduler.execute(observer::onComplete);
            }
        })
    );
}

    public interface OnSubscribe<T> {
        void subscribe(Observer<? super T> observer);
    }
}