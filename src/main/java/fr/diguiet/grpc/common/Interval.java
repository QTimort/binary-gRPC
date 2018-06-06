package fr.diguiet.grpc.common;

import javax.annotation.concurrent.Immutable;

/**
 * Object representation of an interval between a begin and end value
 * Instance is immutable
 */
@Immutable
public class Interval {
    private final int begin;
    private final int end;

    /**
     * Interval object factory tha return a representation of begin and end
     * @param begin The begin interval value
     * @param end The end interval value
     * @return an Interval object representation
     */
    public static Interval valueOf(final int begin, final int end) {
        return (new Interval(begin, end));
    }

    /**
     * Create a new instance of an Interval
     * @param begin The begin interval value
     * @param end The end interval value
     */
    private Interval(final int begin, final int end) {
        if (begin > end)
            throw new IllegalArgumentException("The end of the interval cannot be before its beginning!");
        this.begin = begin;
        this.end = end;
    }

    /**
     * Get the begin interval
     * @return the begin interval
     */
    public int getBegin() {
        return (this.begin);
    }

    /**
     * Get the end interval
     * @return the end interval
     */
    public int getEnd() {
        return (this.end);
    }

    /**
     * Predicate that tell whether or not this interval is not in the specified one
     * @param interval the interval to check
     * @return wether or not this interval is not in the one specified
     */
    public boolean notIn(final Interval interval) {
        return (this.getEnd() < interval.getBegin() || interval.getEnd() < this.getBegin());
    }

    /**
     * Merge this interval and the specified one
     * @param interval the interval to merge
     * @return Return the largest interval that include both this and the specified interval
     */
    public Interval merge(final Interval interval) {
        final int mergedBegin = this.getBegin() < interval.getBegin() ? this.getBegin() : interval.getBegin();
        final int mergedEnd = this.getEnd() < interval.getEnd() ? interval.getEnd() : this.getEnd();
        return (Interval.valueOf(mergedBegin, mergedEnd));
    }

    /**
     * A string representation of the instance
     * @return a string representation of the instance
     */
    @Override
    public String toString() {
        return "[" + this.begin + ";" + this.end + "]";
    }
}
