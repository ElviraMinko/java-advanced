package info.kgeorgiy.ja.minko.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {

    private final Comparator<Student> BASE_NAME_COMPARATOR = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparing(Student::getId);

    private <T> List<T> mapStudentsWithFunction(List<Student> students, Function<Student, T> mapper) {
        return students.stream().map(mapper).toList();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapStudentsWithFunction(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapStudentsWithFunction(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapStudentsWithFunction(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapStudentsWithFunction(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Comparator.comparing(Student::getId)).map(Student::getFirstName).orElse("");
    }

    private List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator).toList();
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Comparator.comparing(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, BASE_NAME_COMPARATOR);
    }

    private <T> List<Student> findStudentBy(Collection<Student> students, T value, Function<Student, T> function) {
        return students.stream().filter(student -> function.apply(student).equals(value))
                .sorted(BASE_NAME_COMPARATOR).toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentBy(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentBy(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentBy(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)
                ));
    }
}
