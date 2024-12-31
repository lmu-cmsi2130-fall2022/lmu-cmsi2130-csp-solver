# Calendar Satisfaction Problem (CSP) Solver

## Overview

The **CSP Solver** is a Java-based utility designed to solve scheduling problems involving a set number of meetings within a defined date range, while satisfying specified unary and binary constraints. It employs a backtracking algorithm with pre-processing techniques (node and arc consistency) to optimize the solution search.

---

## Features

1. **Meeting Scheduling:**
   - Schedules `n` meetings within a specified date range (`rangeStart` to `rangeEnd`)
   - Each meeting is represented as a variable with a domain of allowable dates

2. **Constraints:**
   - Supports **unary constraints** (restrictions on a single meeting's date)
   - Supports **binary constraints** (relationships between dates of two meetings)

3. **Pre-Processing:**
   - **Node Consistency:** Eliminates dates from a meeting's domain that violate unary constraints
   - **Arc Consistency:** Refines domains using the AC-3 algorithm to remove values that violate binary constraints

4. **Backtracking:**
   - Searches for an assignment of dates that satisfies all constraints
   - Prunes the search space dynamically to optimize performance

---

## Public Interface

### `solve`
Schedules meetings with given constraints

- **Parameters:**
  - `nMeetings`: Number of meetings to schedule
  - `rangeStart`: Start date (inclusive) of the scheduling range
  - `rangeEnd`: End date (inclusive) of the scheduling range
  - `constraints`: Set of `DateConstraint` objects defining scheduling rules
  
- **Returns:**
  - A `List<LocalDate>` with the scheduled dates for each meeting, or `null` if no solution exists

---

## Key Components

1. **Backtracking Algorithm:**
   - Recursively assigns dates to meetings
   - Ensures consistency with constraints after each assignment

2. **Pre-Processing:**
   - `nodeConsistency`: Filters domains based on unary constraints
   - `arcConsistency`: Applies the AC-3 algorithm for binary constraints, ensuring consistent relationships between meeting dates

3. **Helper Classes:**
   - `MeetingDomain`: Defines the domain (allowable dates) for a meeting
   - `Arc`: Represents binary constraints between two meetings for arc consistency processing

4. **Constraint Checking:**
   - Validates that assignments satisfy all constraints using `checkAssignment`

---

## Usage Example

```java
LocalDate startDate = LocalDate.of(2024, 1, 1);
LocalDate endDate = LocalDate.of(2024, 1, 31);
Set<DateConstraint> constraints = new HashSet<>();

// Add constraints (UnaryDateConstraint, BinaryDateConstraint)
// Example: constraints.add(new UnaryDateConstraint(...));

List<LocalDate> schedule = CSPSolver.solve(5, startDate, endDate, constraints);

if (schedule != null) {
    System.out.println("Meeting Schedule: " + schedule);
} else {
    System.out.println("No feasible schedule found.");
}
