package main.csp;

import java.time.LocalDate;
import java.util.*;

/**
 * CSP: Calendar Satisfaction Problem Solver Provides a solution for scheduling
 * some n meetings in a given period of time and according to some unary and
 * binary constraints on the dates of each meeting.
 */
public class CSPSolver {

	// Backtracking CSP Solver
	// --------------------------------------------------------------------------------------------------------------

	/**
	 * Public interface for the CSP solver in which the number of meetings, range of
	 * allowable dates for each meeting, and constraints on meeting times are
	 * specified.
	 * 
	 * @param nMeetings   The number of meetings that must be scheduled, indexed
	 *                    from 0 to n-1
	 * @param rangeStart  The start date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param rangeEnd    The end date (inclusive) of the domains of each of the n
	 *                    meeting-variables
	 * @param constraints Date constraints on the meeting times (unary and binary
	 *                    for this assignment)
	 * @return A list of dates that satisfies each of the constraints for each of
	 *         the n meetings, indexed by the variable they satisfy, or null if no
	 *         solution exists.
	 */
	public static List<LocalDate> solve(int nMeetings, LocalDate rangeStart, LocalDate rangeEnd,
			Set<DateConstraint> constraints) {
		List<LocalDate> assignment = new ArrayList<LocalDate>();
		List<MeetingDomain> domains = generateDomains(nMeetings, rangeStart, rangeEnd);
		// call pre-processing methods
		nodeConsistency(domains, constraints);
		arcConsistency(domains, constraints);
		return backTracking(assignment, domains, nMeetings, constraints);
	}

	/**
	 * Private method used to recursively find the solution as to which dates can be
	 * assigned to some number and set of meetings. Ex: it will return a list
	 * similar to this [01-10-2022,01-11-2022]
	 * 
	 * @param assignment a list of local dates (List<LocalDate>) that store a date
	 *                   at each index, where the index number represents the
	 *                   meeting number
	 * @param domains    a list of meeting domains (List<MeetingDomain>) that store
	 *                   all valid dates that a meeting can be
	 * 
	 * @param nMeetings  an integer representing the number of meetings that need
	 *                   dates assigned to them
	 * 
	 * @return A list of local dates (List<LocalDate>) called result that stores the
	 *         correct date assigned to each meeting
	 * 
	 */
	private static List<LocalDate> backTracking(List<LocalDate> assignment, List<MeetingDomain> domains, int nMeetings,
			Set<DateConstraint> constraints) {
		// call helper and check that every meeting is assigned and assignments are
		// consistent
		if (checkAssignment(constraints, assignment) && assignment.size() == nMeetings) {
			return assignment;
		}
		// next index is the current size of the list
		int index = assignment.size();
		for (LocalDate date : domains.get(index).domainValues) {
			// add date to meeting
			assignment.add(index, date);

			if (checkAssignment(constraints, assignment)) {
				// recursive call to backTracking
				List<LocalDate> result = backTracking(assignment, domains, nMeetings, constraints);
				if (result != null) {
					return result;
				}
			}
			assignment.remove(index);
		}
		return null;
	}

	/**
	 * A helper method that checks if assignments are consistent with each meeting's
	 * set of constraints and will return a boolean value.
	 * 
	 * @param constraints  a set of DateConstraints (Set<DateConstraint> constraint)
	 *                     that are can be unary or binary meeting constraints
	 * 
	 * @param meetingDates a list of Local Dates (List<LocalDate> which is the list
	 *                     of all possible dates for a meeting
	 * 
	 * @return A boolean value where true means that the assignment is consistent
	 *         and false if it is inconsistent
	 * 
	 */
	private static boolean checkAssignment(Set<DateConstraint> constraints, List<LocalDate> meetingDates) {
		for (DateConstraint constraint : constraints) {
			if (constraint.ARITY == 1) {
				// unary
				UnaryDateConstraint unary = (UnaryDateConstraint) constraint;
				if (unary.L_VAL < meetingDates.size()) {
					// calls DateConstraint's isSatisfied() method
					if (!unary.isSatisfiedBy(meetingDates.get(unary.L_VAL), unary.R_VAL)) {
						// if inconsistent
						return false;
					}
				}
			} else {
				// binary
				BinaryDateConstraint binary = (BinaryDateConstraint) constraint;
				if (binary.L_VAL < meetingDates.size() && binary.R_VAL < meetingDates.size()) {
					if (!binary.isSatisfiedBy(meetingDates.get(binary.L_VAL), meetingDates.get(binary.R_VAL))) {
						// if inconsistent
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Helper method for generating uniform domains.
	 * 
	 * @param n          Number of meeting variables in this CSP.
	 * @param startRange Start date for the range of each variable's domain.
	 * @param endRange   End date for the range of each variable's domain.
	 * @return The List of Meeting-indexed MeetingDomains.
	 */
	private static List<MeetingDomain> generateDomains(int n, LocalDate startRange, LocalDate endRange) {
		List<MeetingDomain> domains = new ArrayList<>();
		while (n > 0) {
			domains.add(new MeetingDomain(startRange, endRange));
			n--;
		}
		return domains;
	}

	// Filtering Operations
	// --------------------------------------------------------------------------------------------------------------

	/**
	 * Enforces node consistency for all variables' domains given in varDomains
	 * based on the given constraints. Meetings' domains correspond to their index
	 * in the varDomains List.
	 * 
	 * @param varDomains  List of MeetingDomains in which index i corresponds to D_i
	 * @param constraints Set of DateConstraints specifying how the domains should
	 *                    be constrained. [!] Note, these may be either unary or
	 *                    binary constraints, but this method should only process
	 *                    the *unary* constraints!
	 */
	public static void nodeConsistency(List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {
		for (DateConstraint constraint : constraints) {
			// only need node consistency pre-processing for unary constraints
			if (constraint.ARITY == 1) {
				UnaryDateConstraint unary = (UnaryDateConstraint) constraint;
				MeetingDomain domain = varDomains.get(constraint.L_VAL);
				// to prevent concurrency issues
				Set<LocalDate> copyOfDomain = new HashSet<>(domain.domainValues);
				for (LocalDate value : domain.domainValues) {
					// remove if inconsistent
					if (!unary.isSatisfiedBy(value, unary.R_VAL)) {
						copyOfDomain.remove(value);
					}
				}
				// set the original to the copy
				domain.domainValues = copyOfDomain;

			}
		}

	}

	/**
	 * Enforces arc consistency for all variables' domains given in varDomains based
	 * on the given constraints. Meetings' domains correspond to their index in the
	 * varDomains List.
	 * 
	 * @param varDomains  List of MeetingDomains in which index i corresponds to D_i
	 * @param constraints Set of DateConstraints specifying how the domains should
	 *                    be constrained. [!] Note, these may be either unary or
	 *                    binary constraints, but this method should only process
	 *                    the *binary* constraints using the AC-3 algorithm!
	 */
	public static void arcConsistency(List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {

		HashSet<Arc> arcSet = new HashSet<>();
		for (DateConstraint constraint : constraints) {
			// only need arc consistency for binary constraints
			if (constraint.ARITY == 2) {
				BinaryDateConstraint binary = (BinaryDateConstraint) constraint;
				// create arc set with meeting indexes and constraints
				Arc forward = new Arc(binary.L_VAL, binary.R_VAL, constraint);
				Arc back = new Arc(binary.R_VAL, binary.L_VAL, binary.getReverse());
				arcSet.add(forward);
				arcSet.add(back);
			}

		}
		// to avoid concurrency issues
		HashSet<Arc> copyOfArcSet = new HashSet<>(arcSet);
		while (!arcSet.isEmpty()) {
			// pop arc
			Arc arcNext = arcSet.iterator().next();
			arcSet.remove(arcNext);

			if (removeInconsistencies(arcNext, varDomains)) {
				for (Arc arc : copyOfArcSet) {
					// find and add neighbors
					if (arcNext.TAIL == arc.HEAD) {
						arcSet.add(arc);
					}
				}
			}
		}
	}

	/**
	 * Helper method that will be used to do AC-3 pre-processing. It will
	 * 
	 * @param an         arc an arc is constructed with an integer representing the
	 *                   tail and head and a DateConstraint called constriant
	 * @param varDomains List of MeetingDomains in which index i corresponds to D_i
	 * 
	 * 
	 */
	private static boolean removeInconsistencies(Arc arc, List<MeetingDomain> varDomains) {
		// get domain of head and tail
		MeetingDomain tailDomain = varDomains.get(arc.TAIL);
		MeetingDomain headDomain = varDomains.get(arc.HEAD);
		boolean removed = false;
		// to avoid concurrency issues
		Set<LocalDate> copyOfTailDomain = new HashSet<>(tailDomain.domainValues);
		for (LocalDate tailDate : tailDomain.domainValues) {
			boolean satisfied = false;
			for (LocalDate headDate : headDomain.domainValues) {
				if (arc.CONSTRAINT.isSatisfiedBy(tailDate, headDate)) {
					// if consistent
					satisfied = true;
					break;
				}
			}

			if (!satisfied) {
				// prune date from the tail domain if assignment is not satisfied
				copyOfTailDomain.remove(tailDate);
				removed = true;
			}
		}
		// set original to copy
		tailDomain.domainValues = copyOfTailDomain;
		return removed;
	}

	/**
	 * Private helper class organizing Arcs as defined by the AC-3 algorithm, useful
	 * for implementing the arcConsistency method. [!] You may modify this class
	 * however you'd like, its basis is just a suggestion that will indeed work.
	 */
	private static class Arc {

		public final DateConstraint CONSTRAINT;
		public final int TAIL, HEAD;

		/**
		 * Constructs a new Arc (tail -> head) where head and tail are the meeting
		 * indexes corresponding with Meeting variables and their associated domains.
		 * 
		 * @param tail Meeting index of the tail
		 * @param head Meeting index of the head
		 * @param c    Constraint represented by this Arc. [!] WARNING: A
		 *             DateConstraint's isSatisfiedBy method is parameterized as:
		 *             isSatisfiedBy (LocalDate leftDate, LocalDate rightDate), meaning
		 *             L_VAL for the first parameter and R_VAL for the second. Be
		 *             careful with this when creating Arcs that reverse direction. You
		 *             may find the BinaryDateConstraint's getReverse method useful
		 *             here.
		 */
		public Arc(int tail, int head, DateConstraint c) {
			this.TAIL = tail;
			this.HEAD = head;
			this.CONSTRAINT = c;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (this.getClass() != other.getClass()) {
				return false;
			}
			Arc otherArc = (Arc) other;
			return this.TAIL == otherArc.TAIL && this.HEAD == otherArc.HEAD
					&& this.CONSTRAINT.equals(otherArc.CONSTRAINT);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.TAIL, this.HEAD, this.CONSTRAINT);
		}

		@Override
		public String toString() {
			return "(" + this.TAIL + " -> " + this.HEAD + ")";
		}

	}

}
