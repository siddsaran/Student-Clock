package edu.ucsd.studentclock.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Assignment;

public class AssignmentRepository {

    /**
     * Persists and retrieves assignments using JDBC with SQLite.
     */
    private static final String CREATE_TABLE_SQL =
                "CREATE TABLE IF NOT EXISTS assignments (" + "id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " + "courseID TEXT NOT NULL, " + 
                        "start TEXT NOT NULL, " + "deadline TEXT NOT NULL, " + 
                        "lateDaysAllowed INTEGER)";

    private static final String INSERT_SQL =
            "INSERT OR REPLACE INTO assignments " +
                    "(id, name, courseID, start, deadline, lateDaysAllowed) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_COURSE_SQL =
            "SELECT * FROM assignments WHERE courseID = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT * FROM assignments";

    private final Connection connection;

    /**
     * Creates a repository that uses the given data source for all operations.
     *
     * @param dataSource a valid data source (e.g. SQLite)
     */
    public AssignmentRepository(IDataSource dataSource) {
        if (dataSource == null){
            throw new NullPointerException("dataSource must not be null");
        }
        this.connection = dataSource.getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create assignments table", e);
        }
    }

    /**
     * Stores an assignment. If an assignment with the same id already exists,
     * it is replaced.
     *
     * @param assignment the assignment to add (must not be null)
     */
    public void addAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new NullPointerException("assignment must not be null");
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, assignment.getID());
            statement.setString(2, assignment.getName());
            statement.setString(3, assignment.getCourseID());
            statement.setString(4, assignment.getStart().toString());
            statement.setString(5, assignment.getDeadline().toString());
            statement.setInt(6, assignment.getLateDaysAllowed());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add assignment", e);
        }
    }

    /**
     * Returns all assignments for the given course id.
     *
     * @param courseID the course id
     * @return list of assignments (never null)
     */
    public List<Assignment> getAssignmentsForCourse(String courseID) {
        List<Assignment> assignmentList = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(SELECT_BY_COURSE_SQL)) {

            statement.setString(1, courseID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String cid = resultSet.getString("courseID");
                    LocalDateTime start =
                            LocalDateTime.parse(resultSet.getString("start"));
                    LocalDateTime deadline =
                            LocalDateTime.parse(resultSet.getString("deadline"));
                    int lateDays = resultSet.getInt("lateDaysAllowed");

                    assignmentList.add(
                            new Assignment(name, cid, start, deadline, lateDays, 0)
                    );
                }
            }

            return List.copyOf(assignmentList);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get assignments for course", e);
        }
    }

    /**
     * Returns all assignments from all courses.
     *
     * @return list of all assignments (never null)
     */
    public List<Assignment> getAllAssignments() {
        List<Assignment> assignmentList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String cid = resultSet.getString("courseID");
                LocalDateTime start =
                        LocalDateTime.parse(resultSet.getString("start"));
                LocalDateTime deadline =
                        LocalDateTime.parse(resultSet.getString("deadline"));
                int lateDays = resultSet.getInt("lateDaysAllowed");
                assignmentList.add(
                        new Assignment(name, cid, start, deadline, lateDays, 0)
                );
            }
            return List.copyOf(assignmentList);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all assignments", e);
        }
    }

}