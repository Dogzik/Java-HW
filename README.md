# Тесты к курсу «Технологии Java»

[Условия домашних заданий](http://www.kgeorgiy.info/courses/java-advanced/homeworks.html)


## Домашнее задание 3. Студенты

 * Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.student.Tester StudentGroupQuery <полное имя класса>

 * Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.student.Tester StudentQuery <полное имя класса>

Исходный код:

 * Простой вариант: [интерфейс](java/info/kgeorgiy/java/advanced/student/StudentQuery.java), [тесты](java/info/kgeorgiy/java/advanced/student/StudentQueryTest.java)
 * Сложный вариант: [интерфейс](java/info/kgeorgiy/java/advanced/student/StudentGroupQuery.java), [тесты](java/info/kgeorgiy/java/advanced/student/StudentGroupQueryTest.java)


## Домашнее задание 2. ArraySortedSet

 * Протестировать сложную версию задания:

        info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet <полное имя класса>

 * Протестировать простую версию задания:

        info.kgeorgiy.java.advanced.arrayset.Tester SortedSet <полное имя класса>

Исходный код тестов:

 * [Простой вариант](java/info/kgeorgiy/java/advanced/arrayset/SortedSetTest.java)
 * [Сложный вариант](java/info/kgeorgiy/java/advanced/arrayset/NavigableSetTest.java)


## Домашнее задание 1. Обход файлов

Для того, чтобы протестировать программу:

 * Скачайте тесты ([WalkTest.jar](artifacts/WalkTest.jar)) и библиотеки к ним:
    [junit-4.11.jar](lib/junit-4.11.jar), [hamcrest-core-1.3.jar](lib/hamcrest-core-1.3.jar)
 * Откомпилируйте решение домашнего задания
 * Запуск простого варианта

        info.kgeorgiy.java.advanced.walk.Tester Walk <полное имя класса>

 * Запуск сложного варианта

        info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk <полное имя класса>

 * Обратите внимание, что все скачанные `.jar` файлы должны быть указаны в `CLASSPATH`.

Исходный код тестов:

 * [Простой вариант](java/info/kgeorgiy/java/advanced/walk/WalkTest.java)
 * [Сложный вариант](java/info/kgeorgiy/java/advanced/walk/RecursiveWalkTest.java)
