# Student Clock

## Overview

**Student Clock** is a Java-based productivity application designed to help students manage their academic workload through structured time tracking, scheduling, and task organization. The system enables users to manage courses, assignments, study availability, and work sessions, providing a centralized interface for planning and monitoring productivity.

The application emphasizes **modular architecture, maintainability, and testability**, using object-oriented design principles and layered system design.

---

## Features

* Course and assignment management
* Study session and work hour tracking
* Availability scheduling and time allocation
* Persistent data storage using SQLite
* Interactive desktop UI built with JavaFX

---

## Architecture

The system is structured using a layered architecture to separate concerns and improve extensibility:

* **Model**: Core domain entities (courses, assignments, sessions)
* **Repository**: Data access layer for persistence (SQLite)
* **Service**: Business logic and application workflows
* **Presenter**: Mediates between UI and backend logic
* **View**: JavaFX-based user interface

This structure enables independent development and testing of components while maintaining clear boundaries between system layers.

---

## Design Patterns

The project incorporates several object-oriented design patterns to improve modularity and flexibility:

* **Factory Pattern** – for controlled object creation
* **Builder Pattern** – for constructing complex domain objects
* **Facade Pattern** – for simplifying interactions between subsystems

---

## Testing

A comprehensive testing suite ensures system correctness and reliability:

* **275+ JUnit tests**
* **500+ assertions**
* Coverage across:

  * Models
  * Repositories
  * Services
  * Presenters
  * Parsing and navigation logic

Both **unit tests** and **cross-component interaction tests** are included to validate behavior across system layers.

---

## CI/CD

Continuous integration is implemented using **GitHub Actions**:

* Automated test execution on pull requests and commits
* Maven-based build and test pipeline
* Early detection of regressions before merge

---

## Tech Stack

* **Language:** Java
* **UI:** JavaFX
* **Database:** SQLite
* **Build Tool:** Maven
* **Testing:** JUnit, Mockito
* **CI/CD:** GitHub Actions

---

## Getting Started

### Prerequisites

* Java 11+
* Maven

### Setup

```bash
git clone <repo-url>
cd student-clock
mvn clean install
mvn javafx:run
```

---

## Project Structure

```text
src/
  main/
    java/
      model/
      repository/
      service/
      presenter/
      view/
      datasource/
  test/
    java/
      (unit and integration tests)
```

---

## Contributors

Add your team members here:

* Sidd Saran
* Parth Mehta
* Khang Nguyen
* Tu Nguyen
* Mojo Batey
* Koushik Karthikeyan

---

## Notes

This project was developed in a collaborative team environment, emphasizing clean architecture, testing discipline, and maintainable design practices.

---
