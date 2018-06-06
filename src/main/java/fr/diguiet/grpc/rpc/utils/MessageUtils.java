package fr.diguiet.grpc.rpc.utils;

import fr.diguiet.grpc.rpc.common.UUID;

/**
 * Utils class with static method to simplify the use of message related function and object
 * @see com.google.protobuf.GeneratedMessageV3
 */
public final class MessageUtils {

    /**
     * Class is not instantiable and inheritable
     */
    private MessageUtils() {

    }

    /**
     * Convert an UUID message into an UUID Java object
     * @param uuidMessage the UUID message
     * @return a new UUID Java equivalent
     */
    public static java.util.UUID getUUID(final UUID uuidMessage) {
        return (java.util.UUID.fromString(uuidMessage.getId()));
    }

    /**
     * Convert a Java UUID instance into a new UUID Message
     * @param uuid the Java UUID
     * @return a new UUID message
     */
    public static UUID toUUIDMessage(final java.util.UUID uuid) {
        return (UUID.newBuilder().setId(uuid.toString()).build());
    }
}
