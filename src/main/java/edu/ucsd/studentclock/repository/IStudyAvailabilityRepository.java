package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.StudyAvailability;

import java.util.Optional;

/**
 * Abstraction for study availability persistence.
 */
public interface IStudyAvailabilityRepository {

    void save(StudyAvailability availability);

    Optional<StudyAvailability> load();
}
