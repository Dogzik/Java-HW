# Тесты к курсу «Технологии Java»

[Условия домашних заданий](http://www.kgeorgiy.info/courses/java-advanced/homeworks.html)


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
1. Разработайте класс `StudentDB`, осуществляющий поиск по базе данных студентов.
    * Класс `StudentDB` должен реализовывать интерфейс `StudentQuery` (простая версия) или `StudentGroupQuery` (сложная версия).
    * Каждый методы должен состоять из ровного одного оператора. При этом длинные операторы надо разбивать на несколько строк.
2. При выполнении задания следует обратить внимание на:
    * Применение лямбда-выражений и поток.
    * Избавление от повторяющегося кода.
    
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
1. Разработайте класс `ArraySet`, реализующие неизменяемое упорядоченное множество.
    * Класс `ArraySet` должен реализовывать интерфейс `SortedSet` (упрощенная версия) или `NavigableSet` (усложненная версия).
    * Все операции над множествами должны производиться с максимально возможной асимптотической эффективностью.
2. При выполнении задания следует обратить внимание на:
    * Применение стандартных коллекций.
    * Избавление от повторяющегося кода.
    
Тестирование

 * простой вариант:
    ```info.kgeorgiy.java.advanced.arrayset.Tester SortedSet <полное имя класса>```
 * сложный вариант:
    ```info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet <полное имя класса>```

Исходный код тестов:

 * [простой вариант](java/info/kgeorgiy/java/advanced/arrayset/SortedSetTest.java)
 * [сложный вариант](java/info/kgeorgiy/java/advanced/arrayset/NavigableSetTest.java)


## Домашнее задание 1. Обход файлов
1. Разработайте класс Walk, осуществляющий подсчет хеш-сумм файлов.
    * Формат запуска:  
  `java Walk <входной файл> <выходной файл>`
    * Входной файл содержит список файлов, которые требуется обойти.
    * Выходной файл должен содержать по одной строке для каждого файла. Формат строки:  
  `<шестнадцатеричная хеш-сумма> <путь к файлу>`
    * Для подсчета хеш-суммы используйте алгоритм FNV.
    * Если при чтении файла возникают ошибки, укажите в качестве его хеш-суммы 00000000.
    * Кодировка входного и выходного файлов — UTF-8.
    * Размеры файлов могут превышать размер оперативной памяти.
    * Пример  
  Входной файл  
                        `java/info/kgeorgiy/java/advanced/walk/samples/156`    
                        `java/info/kgeorgiy/java/advanced/walk/samples/127`  
                        `java/info/kgeorgiy/java/advanced/walk/samples/123`  
                        `java/info/kgeorgiy/java/advanced/walk/samples/1234`  
                        `java/info/kgeorgiy/java/advanced/walk/samples/1`  
                        `java/info/kgeorgiy/java/advanced/walk/samples/binary`  
                        `java/info/kgeorgiy/java/advanced/walk/samples/no-such-file`  
  Выходной файл  
                        `050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/156`  
                        `2076af58 java/info/kgeorgiy/java/advanced/walk/samples/127`  
                        `72d607bb java/info/kgeorgiy/java/advanced/walk/samples/123`  
                        `81ee2b55 java/info/kgeorgiy/java/advanced/walk/samples/1234`  
                        `050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1`   
                        `8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary`  
                        `00000000 java/info/kgeorgiy/java/advanced/walk/samples/no-such-file`  
                    
2. Усложненная версия:
    * Разработайте класс RecursiveWalk, осуществляющий подсчет хеш-сумм файлов в директориях
    * Входной файл содержит список файлов и директорий, которые требуется обойти. Обход директорий осуществляется рекурсивно.
    * Пример  
  Входной файл  
                        `java/info/kgeorgiy/java/advanced/walk/samples/binary  
                        java/info/kgeorgiy/java/advanced/walk/samples`    
  Выходной файл  
                        `8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary  
                        050c5d2e java/info/kgeorgiy/java/advanced/walk/samples/1  
                        2076af58 java/info/kgeorgiy/java/advanced/walk/samples/12  
                        72d607bb java/info/kgeorgiy/java/advanced/walk/samples/123  
                        81ee2b55 java/info/kgeorgiy/java/advanced/walk/samples/1234  
                        8e8881c5 java/info/kgeorgiy/java/advanced/walk/samples/binary`  
                    
3. При выполнении задания следует обратить внимание на:
    * Дизайн и обработку исключений, диагностику ошибок.
    * Программа должна корректно завершаться даже в случае ошибки.
    * Корректная работа с вводом-выводом.
    * Отсутствие утечки ресурсов.
4. Требования к оформлению задания.
    * Проверяется исходный код задания.
    * Весь код должен находиться в пакете `ru.ifmo.rain.фамилия.walk.`

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
