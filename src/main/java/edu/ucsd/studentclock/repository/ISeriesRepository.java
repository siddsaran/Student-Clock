package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.Series;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for series persistence.
 */
public interface ISeriesRepository {

    void addSeries(Series series);

    Optional<Series> getSeries(String id);

    List<Series> getSeriesByCourse(String courseId);
}
