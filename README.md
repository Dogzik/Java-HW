# Тесты к курсу «Технологии Java»

[Условия домашних заданий](http://www.kgeorgiy.info/courses/java-advanced/homeworks.html)


## Домашнее задание 6. Итеративный параллелизм

Тестирование

 * простой вериант:
    ```info.kgeorgiy.java.advanced.concurrent.Tester list <полное имя класса>```

  Класс должен реализовывать интерфейс
  [ListIP](java/info/kgeorgiy/java/advanced/concurrent/ListIP.java).

 * сложный вериант:
    ```info.kgeorgiy.java.advanced.concurrent.Tester scalar <полное имя класса>```

  Класс должен реализовывать интерфейс
  [ScalarIP](java/info/kgeorgiy/java/advanced/concurrent/ScalarIP.java).

Исходный код тестов:

* [простой вариант](java/info/kgeorgiy/java/advanced/concurrent/ScalarIPTest.java)
* [сложный вариант](java/info/kgeorgiy/java/advanced/concurrent/ListIPTest.java)


# Домашнее задание 5. JarImplementor

Класс должен реализовывать интерфейс
[JarImpler](java/info/kgeorgiy/java/advanced/implementor/JarImpler.java).

Тестирование

 * простой вериант:
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

 * простой вериант:
    ```info.kgeorgiy.java.advanced.implementor.Tester interface <полное имя класса>```
 * сложный вериант:
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
