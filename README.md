Тесты к курсу «Технологии Java»
====

Домашнее задание 1. Обход файлов
----
Для того, чтобы протестировать программу:

 * Скачайте тесты ([WalkTest.jar](artifacts/WalkTest.jar)) и библиотеки к ним:
    [junit-4.11.jar](lib/junit-4.11.jar) [hamcrest-core-1.3.jar](lib/hamcrest-core-1.3.jar)
 * Откомпилируйте решение домашнего задания
 * Запуск простого варианта

		info.kgeorgiy.java.advanced.walk.Tester Walk <полное имя класса>

 * Запуск сложного варианта

        info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk <полное имя класса>

 * Обратите внимание, что все скачанные `.jar` файлы должны быть указаны в `CLASSPATH`.

Исходный код тестов:

* [Простой вариант](java/info/kgeorgiy/java/advanced/walk/WalkTest.java)
* [Сложный вариант](java/info/kgeorgiy/java/advanced/walk/RecursiveWalkTest.java)
