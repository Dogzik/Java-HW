package info.kgeorgiy.java.advanced.student;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StudentGroupQueryTest extends StudentQueryTest {
    private final StudentGroupQuery db = createCUT();

    @Test
    public void test21_testGetGroupsByName() {
        testGroups(
                db::getGroupsByName,
                new int[]{0}, new int[]{1, 0}, new int[]{2, 1, 0}
        );
    }

    @Test
    public void test22_testGetGroupsById() {
        testGroups(
                db::getGroupsById,
                new int[]{0}, new int[]{0, 1}, new int[]{2, 1, 0}
        );
    }

    @Test
    public void test23_testGetLargestGroup() {
        testString(
                db::getLargestGroup,
                "M3138", "M3138", "M3139"
        );
    }


    @Test
    public void test24_testGetLargestGroupByFirstName() {
        testString(
                db::getLargestGroupFirstName,
                "M3138", "M3138", "M3138"
        );
    }

    private void testGroups(final Function<List<Student>, List<Group>> query, final int[]... answers) {
        this.test(query, this::groups, answers);
    }

    private List<Group> groups(final List<Student> students, final int[] answer) {
        String group = null;
        List<Student> groupStudents = new ArrayList<>();
        final List<Group> groups = new ArrayList<>();

        for (final Student student : getStudents(students, answer)) {
            if (group != null && !group.equals(student.getGroup())) {
                groups.add(new Group(group, groupStudents));
                groupStudents = new ArrayList<>();
            }
            group = student.getGroup();
            groupStudents.add(student);
        }
        if (!groupStudents.isEmpty()) {
            groups.add(new Group(group, groupStudents));
        }
        return groups;
    }
}
