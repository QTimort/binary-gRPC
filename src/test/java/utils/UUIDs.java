package utils;

import fr.diguiet.grpc.common.utils.UUIDUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class UUIDs {
    private static final String UUID_STR = "21d02569-ca99-4421-bc9f-4156f69e30ab";

    @Test
    public void convert() {
        final UUID uuid = UUID.fromString(UUIDs.UUID_STR);
        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(UUIDs.UUID_STR, uuid.toString());
        final byte[] bytes = UUIDUtils.toBytes(uuid);
        Assertions.assertNotNull(bytes);
        final UUID uuidFromBytes = UUIDUtils.fromBytes(bytes);
        Assertions.assertEquals(uuid, uuidFromBytes);
        Assertions.assertEquals(UUIDs.UUID_STR, uuidFromBytes.toString());

        Assertions.assertThrows(NullPointerException.class, () -> UUIDUtils.fromBytes(null));
        Assertions.assertThrows(NullPointerException.class, () -> UUIDUtils.toBytes(null));
    }
}
