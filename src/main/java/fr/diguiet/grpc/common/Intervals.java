package fr.diguiet.grpc.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class that encapsulate a list of interval
 * @see Interval
 */
public class Intervals {
    private List<Interval> intervals = new ArrayList<>();

    /**
     * Get a new instance
     * @return a new Intervals
     */
    public static Intervals newInstance() {
        return (new Intervals());
    }

    /**
     * Instantiate a new Intervals
     */
    private Intervals() {

    }

    /**
     * Add a new interval in the list
     * The internal list is simplified if it contains sub-interval
     * @param interval the interval to add
     */
    public void add(Interval interval) {
        Objects.requireNonNull(interval);
        final List<Interval> merge = new ArrayList<>();

        for (final Interval current : this.intervals) {
            if (current.notIn(interval)) {
                merge.add(current);
            } else {
                interval = current.merge(interval);
            }
        }
        merge.add(interval);
        this.intervals = merge;
    }

    /**
     * Clear the whole list
     */
    public void clear() {
        this.intervals.clear();
    }

    /**
     * Get the intervals
     * @return a copy of the interval list
     */
    public List<Interval> getIntervals() {
        return (new ArrayList<>(this.intervals));
    }

    /**
     * String representation of the instance
     * @return representation of the instance
     */
    @Override
    public String toString() {
        return "Intervals{" +
                "intervals=" + this.intervals.stream().map(Object::toString).collect(Collectors.joining(", ")) +
                '}';
    }
}
