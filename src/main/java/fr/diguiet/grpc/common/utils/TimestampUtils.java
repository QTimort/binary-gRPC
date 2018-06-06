package fr.diguiet.grpc.common.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.Objects;

/**
 * Utils class with static method to simplify the use of timestamp, instant function and object
 * Timestamp and Instant are UTC 0 based
 * @see Timestamp
 * @see Instant
 */
public final class TimestampUtils {

    /**
     * Class is not instantiable and inheritable
     */
    private TimestampUtils() {

    }

    /**
     * Create a new Timestamp from an Instant
     * @param instant The instant to convert
     * @return The new Timestamp
     */
    public static Timestamp toTimestamp(final Instant instant) {
        Objects.requireNonNull(instant);
        return (Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build());
    }

    /**
     * Create a new Timestamp from a byte array representation
     * @param bytes The byte array representation of a Timestamp
     * @return The new Timestamp
     * @throws InvalidProtocolBufferException If unable to convert to Timestamp from the specified byte array
     */
    public static Timestamp toTimestamp(final byte[] bytes) throws InvalidProtocolBufferException {
        Objects.requireNonNull(bytes);
        return (Timestamp.newBuilder().mergeFrom(bytes).build());
    }

    /**
     * Create a new Timestamp representation of now
     * @return A new Timestamp
     */
    public static Timestamp now() {
        return (TimestampUtils.toTimestamp(Instant.now()));
    }

    /**
     * Create a new Timestamp representation of now plus the specified seconds
     * @param seconds The number of seconds to add from "now"
     * @return A new Timestamp
     */
    public static Timestamp nowPlusSeconds(final long seconds) {
        return (TimestampUtils.toTimestamp(Instant.now().plusSeconds(seconds)));
    }

    /**
     * Check if a Timestamp is strictly past another in time
     * @param after The base Timestamp
     * @param date The Timestamp to compare
     * @return True if "date" is after "after" else False
     */
    public static boolean isAfter(final Timestamp after, final Timestamp date) {
        Objects.requireNonNull(after);
        Objects.requireNonNull(date);
        if (date.getSeconds() > after.getSeconds()) {
            return (true);
        } else if (date.getSeconds() == after.getSeconds()) {
            return (date.getNanos() > after.getNanos());
        }
        return (false);
    }

    /**
     * Check if a Timestamp is equal or past another in time
     * @param after The base Timestamp
     * @param date The Timestamp to compare
     * @return True if "date" is equal or after "after" else False
     */
    public static boolean isAfterOrEqual(final Timestamp after, final Timestamp date) {
        Objects.requireNonNull(after);
        Objects.requireNonNull(date);
        if (after.getSeconds() == date.getSeconds() && after.getNanos() == date.getNanos())
            return (true);
        return (TimestampUtils.isAfter(after, date));
    }

    /**
     * Get the difference of seconds between two Timestamp
     * @param a The a timestamp
     * @param b The b timestamp
     * @return a - b seconds
     */
    public static long getSecondsDifference(final Timestamp a, final Timestamp b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return (a.getSeconds() - b.getSeconds());
    }

    /**
     * Get the difference of seconds between the specified Timestamp and now
     * @param a The timestamp to compare
     * @return a - now seconds
     */
    public static long getSecondsDifferenceFromNow(final Timestamp a) {
        Objects.requireNonNull(a);
        return (TimestampUtils.getSecondsDifference(a, TimestampUtils.now()));
    }
}
