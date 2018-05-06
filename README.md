# Тесты к курсу «Технологии Java»

[Условия домашних заданий](http://www.kgeorgiy.info/courses/java-advanced/homeworks.html)

## Домашнее задание 10. HelloUDP

Тестирование

 * простой вариант:
	* клиент:
    	```info.kgeorgiy.java.advanced.hello.Tester client <полное имя класса>```
	* сервер:
    	```info.kgeorgiy.java.advanced.hello.Tester server <полное имя класса>```
 * сложный вариант:
	* клиент:
    	```info.kgeorgiy.java.advanced.hello.Tester client-i18n <полное имя класса>```
	* сервер:
    	```info.kgeorgiy.java.advanced.hello.Tester server-i18n <полное имя класса>```

Исходный код тестов:

* [Клиент](java/info/kgeorgiy/java/advanced/hello/HelloClientTest.java)
* [Сервер](java/info/kgeorgiy/java/advanced/hello/HelloServerTest.java)


## Домашнее задание 9. Web Crawler

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.crawler.Tester easy <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.crawler.Tester hard <полное имя класса>```

* *Модификация для 38-39*.
    * Получить с сайта `https://e.lanbook.com` информацию о
    книгах, изданных за последние 5 лет.
    * Разделы:
        * Математика
        * Физика
        * Информатика
    * Пример ссылки:
        ```
        Алексеев, А.И. Сборник задач по классической электродинамике.
        [Электронный ресурс] — Электрон. дан. — СПб. : Лань, 2008. — 320 с. —
        Режим доступа: http://e.lanbook.com/book/100 — Загл. с экрана.
        ```

Исходный код тестов:

* [интерфейсы и вспомогательные классы](java/info/kgeorgiy/java/advanced/crawler/)
* [простой вариант](java/info/kgeorgiy/java/advanced/crawler/CrawlerEasyTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/crawler/CrawlerHardTest.java)



## Домашнее задание 8. Параллельный запуск

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.mapper.Tester scalar <ParallelMapperImpl>,<IterativeParallelism>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.mapper.Tester list <ParallelMapperImpl>,<IterativeParallelism>```

Внимание! Между полными именами классов `ParallelMapperImpl` и `IterativeParallelism`
должна быть запятая и не должно быть пробелов.

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/mapper/ScalarMapperTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/mapper/ListMapperTest.java)


## Домашнее задание 7. Итеративный параллелизм

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.concurrent.Tester scalar <полное имя класса>```

  Класс должен реализовывать интерфейс
  [ScalarIP](java/info/kgeorgiy/java/advanced/concurrent/ScalarIP.java).

 * сложный вариант:
    ```info.kgeorgiy.java.advanced.concurrent.Tester list <полное имя класса>```

  Класс должен реализовывать интерфейс
  [ListIP](java/info/kgeorgiy/java/advanced/concurrent/ListIP.java).

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/concurrent/ScalarIPTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/concurrent/ListIPTest.java)


## Домашнее задание 5. JarImplementor

Класс должен реализовывать интерфейс
[JarImpler](java/info/kgeorgiy/java/advanced/implementor/JarImpler.java).

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.implementor.Tester jar-interface <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.implementor.Tester jar-class <полное имя класса>```

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/implementor/InterfaceJarImplementorTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/implementor/ClassJarImplementorTest.java)


## Домашнее задание 4. Implementor

Класс должен реализовывать интерфейс
[Impler](java/info/kgeorgiy/java/advanced/implementor/Impler.java).

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.implementor.Tester interface <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.implementor.Tester class <полное имя класса>```

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/implementor/InterfaceImplementorTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/implementor/ClassImplementorTest.java)


## Домашнее задание 3. Студенты

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.student.Tester StudentQuery <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.student.Tester StudentGroupQuery <полное имя класса>```

Исходный код

 * простой вариант:
    [интерфейс](java/info/kgeorgiy/java/advanced/student/StudentQuery.java),
    [тесты](java/info/kgeorgiy/java/advanced/student/StudentQueryFullTest.java)
 * сложный вариант:
    [интерфейс](java/info/kgeorgiy/java/advanced/student/StudentGroupQuery.java),
    [тесты](java/info/kgeorgiy/java/advanced/student/StudentGroupQueryFullTest.java)


## Домашнее задание 2. ArraySortedSet

Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.arrayset.Tester SortedSet <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet <полное имя класса>```

Исходный код тестов:

 * [простой вариант](java/info/kgeorgiy/java/advanced/arrayset/SortedSetTest.java)
 * [сложный вариант](java/info/kgeorgiy/java/advanced/arrayset/NavigableSetTest.java)


## Домашнее задание 1. Обход файлов

Для того, чтобы протестировать программу:

 * Скачайте тесты ([WalkTest.jar](artifacts/WalkTest.jar)) и библиотеки к ним:
    [junit-4.11.jar](lib/junit-4.11.jar), [hamcrest-core-1.3.jar](lib/hamcrest-core-1.3.jar)
 * Откомпилируйте решение домашнего задания
 * Протестируйте домашнее задание
    * простой вариант:
        ```info.kgeorgiy.java.advanced.walk.Tester Walk <полное имя класса>```
    * сложный вариант:
        ```info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk <полное имя класса>```
 * Обратите внимание, что все скачанные `.jar` файлы должны быть указаны в `CLASSPATH`.

Исходный код тестов:

 * [простой вариант](java/info/kgeorgiy/java/advanced/walk/WalkTest.java)
 * [сложный вариант](java/info/kgeorgiy/java/advanced/walk/RecursiveWalkTest.java)
