package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persists and retrieves courses using JDBC with SQLite.
 */
public class CourseRepository {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS courses (id TEXT PRIMARY KEY, name TEXT NOT NULL)";
    private static final String INSERT_SQL =
            "INSERT OR REPLACE INTO courses (id, name) VALUES (?, ?)";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, name FROM courses WHERE id = ?";
    private static final String SELECT_ALL_SQL =
            "SELECT id, name FROM courses";

    private final Connection connection;

    /**
     * Creates a repository that uses the given connection for all operations.
     * The caller is responsible for closing the connection when appropriate (e.g. app shutdown).
     *
     * @param connection a valid JDBC connection (e.g. to SQLite)
     */
    public CourseRepository(Connection connection) {
        if (connection == null) {
            throw new NullPointerException("connection must not be null");
        }
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create courses table", e);
        }
    }

    /**
     * Stores a course. If a course with the same id already exists, it is replaced.
     *
     * @param course the course to add (must not be null)
     */
    public void addCourse(Course course) {
        if (course == null) {
            throw new NullPointerException("course must not be null");
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, course.getId());
            statement.setString(2, course.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add course", e);
        }
    }

    /**
     * Returns the course with the given id, or empty if not found.
     *
     * @param id the course id (null is treated as absent, returns Optional.empty())
     * @return Optional containing the course if found, otherwise empty
     */
    public Optional<Course> getCourse(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String courseId = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    return Optional.of(new Course(courseId, name));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get course", e);
        }
    }

    /**
     * Returns all stored courses in an unmodifiable list.
     *
     * @return list of all courses (never null, may be empty)
     */
    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {
            while (resultSet.next()) {
                String courseId = resultSet.getString("id");
                String name = resultSet.getString("name");
                courseList.add(new Course(courseId, name));
            }
            return List.copyOf(courseList);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all courses", e);
        }
    }
}
