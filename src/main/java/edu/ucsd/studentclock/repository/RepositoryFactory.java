package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.IDataSource;

public class RepositoryFactory {

    private final IDataSource dataSource;

    public RepositoryFactory(IDataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource must not be null");
        }
        this.dataSource = dataSource;
    }

    public ICourseRepository createCourseRepository() {
        return new CourseRepository(dataSource);
    }

    public ISeriesRepository createSeriesRepository() {
        return new SeriesRepository(dataSource);
    }

    public IAssignmentRepository createAssignmentRepository() {
        return new AssignmentRepository(dataSource);
    }

    public IStudyAvailabilityRepository createStudyAvailabilityRepository() {
        return new StudyAvailabilityRepository(dataSource);
    }

    public WorkLogRepository createWorkLogRepository() {
        return new WorkLogRepository(dataSource);
    }

    public AssignmentWorkLogRepository createAssignmentWorkLogRepository() {
        return new AssignmentWorkLogRepository(dataSource);
    }
}