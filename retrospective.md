# MS2 Velocity Retrospective

*This retrospective calculates I1’s velocity and describes how that is applied to planning (re)planning Iteration 2.*

## Calculation of Iteration 1's Velocity

### Stories completed (Iteration 1)

Iteration 1 includes US1, DS1, **DS2**, DS3, DS4, and **DS6** (DS2 and DS6 are in I1; they are not in I2). Only story-level completed work is recorded below. Estimates match GitHub board values; totals are in hours and sum to ~30.

| Story | Estimate | Total |
|-------|----------|-------|
| US1 Fix bugs with big picture | 2 | 3.5 |
| DS1 Fix all known and discovered bugs | 3 | 5 |
| DS2 | 3 | 5 |
| DS3 Standardize formatting, documentation and naming | 2 | 3.5 |
| DS4 Refactor for SRP | 5 | 8.5 |
| DS6 | 3 | 4.5 |
| **Total** | **18** | **30** |

**Iteration 1 delivered 30 hours of work** (story-level totals).

### Velocity (based on completed work)

- **Total available hours per iteration:** 2 hours/day × 4 days × 6 members = **48 hours**
- **Iteration 1 completed:** **30 hours** → **I1 velocity = 30 hours/iteration**
- 30 / 48 ≈ **0.625**

---

## MS1 learnings that informed MS2 planning

From Milestone 1:

- Planning was conservative (~50% of estimated capacity), yielding velocity ~0.5.
- All planned stories were completed.
- Challenges included: minor merge delays, uneven contribution (quizzes/midterms), more bugs discovered during integration, and only 5 of 6 members active in the first two iterations.


---

## Application to Iteration 2 (re)planning

Using **I1’s actual velocity** (30 hours), the team (re)plans Iteration 2 as follows:

- **I1 velocity to use for planning:** **30 hours/iteration** (30/48 ≈ 0.625 of capacity).
- **I2 scope:** Planned at or below I1 velocity.
- **Rationale:** I1 delivered 30 hours. I2 is planned with the same capacity in mind; refactoring and coordination overhead are accounted for in the story/task breakdown below.

### Iteration 2: Stories planned with estimates and tasks

Estimates below total ~30 hours of work.

| Title | Estimate |
|-------|----------|
| US2 Improve UI/UX Navigation and Layout | 2 |
| US2-1 Audit navigation and layout inconsistencies | 1 |
| US2-2 Create a consistent navigation layout pattern | 1 |
| US2-3 Add active-screen indication | 0.5 |
| US2-4 Adjust layout spacing and alignment | 1 |
| US2-5 Manual verification of navigation flows | 0.5 |
| DS5 Remove duplication (DRY) | 2.5 |
| DS5-1 Identify duplicated logic and repeated constants | 0.5 |
| DS5-2 Centralize repeated business logic | 1 |
| DS5-3 Extract repeated constants and UI values | 1 |
| DS5-4 Refactor repeated UI/list-building patterns | 1 |
| DS5-5 Regression check after consolidation | 0.5 |
| DS7 Expand test coverage across code | 3 |
| DS7-1 Identify uncovered non-trivial methods | 0.5 |
| DS7-2 Add unit tests for domain and model logic | 1 |
| DS7-3 Add presenter tests for core user actions | 1 |
| DS7-4 Add regression tests for known bugs | 1 |
| DS7-5 Review for redundant tests and naming consistency | 0.5 |
| DS8 Final Code Review | 5.5 |
| DS8-1 Review bug fixes and regression protection | 1 |
| DS8-2 Review dead code, formatting, and naming consistency | 1 |
| DS8-3 Review self-documentation quality | 0.5 |
| DS8-4 Review architecture against MS2 requirements | 1 |
| DS8-5 Run full build and test verification on clean state | 1 |
| DS8-6 Final team walkthrough and merge readiness check | 0.5 |
| **Total** | **30** |
