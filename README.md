# NanoRx

**NanoRx** — это простая библиотека на Java, похожая на RxJava, только гораздо легче. Я сделал её, чтобы самому лучше понять, как работает реактивное программирование, и заодно показать это другим.

### Что здесь есть:

1. **Observer** — это как подписчик. Он «слушает» поток данных. Есть 3 метода:
   - `onNext()` — вызывается, когда приходит новое значение.
   - `onError()` — если произошла ошибка.
   - `onComplete()` — когда поток заканчивается.

2. **Observable** — это источник данных. С ним можно:
   - создать поток (`create`);
   - подписаться на него (`subscribe`);
   - изменить данные с помощью операторов (`map`, `filter`, `flatMap`);
   - управлять потоками (`subscribeOn`, `observeOn`).

3. **Scheduler** — помогает управлять, в каком потоке будет выполняться код:
   - `SingleThreadScheduler` — один поток, всё последовательно.
   - `ComputationScheduler` — несколько потоков для вычислений.
   - `IOThreadScheduler` — для ввода-вывода, например, работы с файлами или сетью.

4. **Disposable** — чтобы можно было «отписаться» от потока и не засорять память. Есть и `CompositeDisposable` — для управления сразу несколькими подписками.

5. **Операторы**:
   - `map` — меняет данные, например, число → строка;
   - `filter` — пропускает только нужные данные;
   - `flatMap` — превращает элемент в другой поток (типа «поток в потоке»).

### Пример кода:

```java
Observable<Integer> source = Observable.<Integer>create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onNext(3);
    observer.onComplete();
});

Observable<String> result = source
    .filter(item -> item % 2 == 0) // Оставляем только чётные
    .map(String::valueOf);         // Превращаем в строки

result.subscribe(new TestObserver<>());
```

### Пример с потоками:

```java
Observable<String> observable = Observable.<String>create(observer -> {
    observer.onComplete();
});

observable
    .observeOn(new IOThreadScheduler()) // Работаем в другом потоке
    .subscribe(new TestObserver<>());
```

### Тесты

Я написал тесты на JUnit 5 для всех важных штук: `map`, `filter`, `flatMap`, `Disposable`, `Schedulers` и ошибок. Всё находится в `src/test/`.

Чтобы запустить тесты:

```bash
mvn test
```

### Как начать:

1. Склонируйте репозиторий:

```bash
git clone https://github.com/leprekon13/NanoRx.git
cd NanoRx
```

2. Убедитесь, что у вас есть Maven и JDK 17+.

3. Запустите тесты:

```bash
mvn test
```

### Зачем всё это?

NanoRx — это просто способ лучше понять, как работает RxJava. Тут только основа — без сложностей. Можно использовать как учебный проект или основу для своей библиотеки.

