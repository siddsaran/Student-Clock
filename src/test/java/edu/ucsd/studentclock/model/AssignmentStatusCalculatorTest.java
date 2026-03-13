package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AssignmentStatusCalculator")
public class AssignmentStatusCalculatorTest {

        private Assignment makeAssignment(
                        LocalDateTime start,
                        LocalDateTime deadline,
                        double estimated,
                        double remaining) {
                Assignment a = new AssignmentBuilder()
                                .setName("Test")
                                .setCourseId("CSE110")
                                .setStart(start)
                                .setDeadline(deadline)
                                .setLateDaysAllowed(0)
                                .setEstimatedHours(estimated)
                                .build();

                a.setRemainingHours(remaining);
                return a;
        }

        @Test
        @DisplayName("Assignment due within 24 hours is marked urgent")
        void dueTomorrowIsUrgent() {
                LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

                Assignment a = makeAssignment(
                                now.minusDays(1),
                                now.plusHours(20),
                                5,
                                5);

                assertTrue(AssignmentStatusCalculator.isUrgent(a, now));
        }

        @Test
        @DisplayName("Assignment due more than a day away is not urgent")
        void dueLaterIsNotUrgent() {
                LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

                Assignment a = makeAssignment(
                                now.minusDays(1),
                                now.plusDays(3),
                                5,
                                5);

                assertFalse(AssignmentStatusCalculator.isUrgent(a, now));
        }

        @Test
        @DisplayName("Assignment ahead of schedule returns NONE status")
        void aheadOfScheduleIsNone() {
                LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

                Assignment a = makeAssignment(
                                now.minusHours(5),
                                now.plusHours(5),
                                10,
                                2);

                assertEquals(AssignmentStatus.NONE,
                                AssignmentStatusCalculator.behindStatus(a, now));
        }

        @Test
        @DisplayName("Slightly behind schedule returns YELLOW status")
        void slightlyBehindIsYellow() {
                LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

                Assignment a = makeAssignment(
                                now.minusHours(3),
                                now.plusHours(5),
                                8,
                                8);

                assertEquals(AssignmentStatus.YELLOW,
                                AssignmentStatusCalculator.behindStatus(a, now));
        }

        @Test
        @DisplayName("Moderately behind schedule returns ORANGE status")
        void moderatelyBehindIsOrange() {
                LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

                Assignment a = makeAssignment(
                                now.minusHours(5),
                                now.plusHours(3),
                                8,
                                8);

                assertEquals(AssignmentStatus.ORANGE,
                                AssignmentStatusCalculator.behindStatus(a, now));
        }

        @Test
        @DisplayName("Severely behind schedule returns RED status")
        void veryBehindIsRed() {
                LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

                Assignment a = makeAssignment(
                                now.minusHours(10),
                                now.plusHours(1),
                                10,
                                10);

                assertEquals(AssignmentStatus.RED,
                                AssignmentStatusCalculator.behindStatus(a, now));
        }

        @Test
        @DisplayName("Completed assignment always returns NONE status")
        void finishedAssignmentIsNone() {
                LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

                Assignment a = makeAssignment(
                                now.minusDays(2),
                                now.plusDays(1),
                                5,
                                0);

                a.setDone(true);

                assertEquals(AssignmentStatus.NONE,
                                AssignmentStatusCalculator.behindStatus(a, now));
        }
}