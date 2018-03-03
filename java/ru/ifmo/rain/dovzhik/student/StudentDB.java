package ru.ifmo.rain.dovzhik.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map.Entry;

public class StudentDB implements StudentGroupQuery {

    private Comparator<Student> nameComparator = Comparator.comparing(Student::getLastName, String::compareTo)
                                                 .thenComparing(Student::getFirstName, String::compareTo)
                                                 .thenComparingInt(Student::getId);

    private List<String> mappedStudents(List<Student> students, Function<Student, String> mapper) {
        return students.stream().map(mapper).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mappedStudents(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mappedStudents(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mappedStudents(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mappedStudents(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new HashSet<>(getFirstNames(students));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Comparator.comparingInt(Student::getId)).get().getFirstName();
    }

    private List<Student> sortedStudents(Stream<Student> studentStream, Comparator<Student> cmp) {
        return studentStream.sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedStudents(students.stream(), Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedStudents(students.stream(), nameComparator);
    }

    private Predicate<Student> getFirstNamePredicate(String firstName) {
        return student -> firstName.equals(student.getFirstName());
    }

    private Predicate<Student> getLastNamePredicate(String lastName) {
        return student -> lastName.equals(student.getLastName());
    }

    private Predicate<Student> getGroupPredicate(String group) {
        return student -> group.equals(student.getGroup());
    }

    private Stream<Student> filteredStudentsStream(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream().filter(predicate);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return sortedStudents(filteredStudentsStream(students, getFirstNamePredicate(name)), nameComparator);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return sortedStudents(filteredStudentsStream(students, getLastNamePredicate(name)), nameComparator);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return sortedStudents(filteredStudentsStream(students, getGroupPredicate(group)), nameComparator);
    }

    private static String minString(final String s1, final String s2) {
        return s1.compareTo(s2) < 0 ? s1 : s2;
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return filteredStudentsStream(students, getGroupPredicate(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, StudentDB::minString));
    }

    private Stream<Entry<String, List<Student>>> getGroupsStream(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet().stream();
    }

    private List<Group> getSortedGroups(Stream<Entry<String, List<Student>>> groupsStream, UnaryOperator<List<Student>> sorter) {
        return groupsStream.map(elem -> new Group(elem.getKey(), sorter.apply(elem.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getSortedGroups(getGroupsStream(students), this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getSortedGroups(getGroupsStream(students), this::sortStudentsById);
    }

    private String getFilteredLargestGroup(Stream<Entry<String, List<Student>>> groupsStream, ToIntFunction<List<Student>> filter) {
        return groupsStream
                .max(Comparator.comparingInt((Entry<String, List<Student>> group) -> filter.applyAsInt(group.getValue()))
                        .thenComparing(Entry::getKey, Collections.reverseOrder(String::compareTo)))
                .get().getKey();
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        /*
        Set<Entry<String, List<Student>>> tmp = students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet();

        students.forEach(student -> System.out.println(student.toString()));
        tmp.forEach(elem -> System.out.println(elem.getKey() + " " + elem.getValue().size()));
        System.out.println();
        */

        return getFilteredLargestGroup(getGroupsStream(students), List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getFilteredLargestGroup(getGroupsStream(students), studentsList -> getDistinctFirstNames(studentsList).size());
    }
}