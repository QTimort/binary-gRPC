package fr.diguiet.grpc.common.utils;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

/**
 * Utils class with static method to simplify the use of UUID function and object
 * @see UUID
 */
public final class UUIDUtils {
    private static final int UUID_BYTE_SIZE = 16;

    /**
     * Class is not instantiable and inheritable
     */
    private UUIDUtils() {

    }

    /**
     * Get the byte size of an UUID
     * @return the byte size
     */
    public static int getUuidByteSize() {
        return (UUIDUtils.UUID_BYTE_SIZE);
    }

    /**
     * Create a new UUID from the specified byte array representation of an UUID
     * @param bytes The byte array to convert
     * @return the convert byte array
     */
    public static UUID fromBytes(final byte[] bytes) {
        Objects.requireNonNull(bytes);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return (new UUID(firstLong, secondLong));
    }

    /**
     * Convert an UUID into a new byte array
     * @param uuid The UUID to convert
     * @return A new byte array UUID representation
     */
    public static byte[] toBytes(final UUID uuid) {
        Objects.requireNonNull(uuid);
        ByteBuffer bb = ByteBuffer.wrap(new byte[UUIDUtils.UUID_BYTE_SIZE]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return (bb.array());
    }
}
