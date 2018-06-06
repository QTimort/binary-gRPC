import fr.diguiet.grpc.common.utils.BytesUtils;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import fr.diguiet.grpc.fileserver.DatabaseFile;
import fr.diguiet.grpc.fileserver.DatabaseFileMetadata;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

public class Json {
    @Test
    public void serialize() {
        final UUID id = UUID.randomUUID();
        final byte[] randomBytes = BytesUtils.toByteArray(BytesUtils.getRandom(1024));
        final DatabaseFile databaseFile = DatabaseFile.newInstance(DatabaseFileMetadata.Builder.newBuilder()
                .id(id)
                .dataLength(randomBytes.length)
                .checksum(BytesUtils.getCheckSum(randomBytes))
                .creationDate(TimestampUtils.now())
                .expirationDate(TimestampUtils.nowPlusSeconds(60 * 60))
                .lastModificationDate(TimestampUtils.now())
                .build(), randomBytes);
        System.out.println(databaseFile.getDatabaseFileMetadata().toJsonString());
    }

    @Test
    public void deserialize() throws IOException {
        final String json = "{\"id\":\"f3bb2968-bd5f-481d-b9dd-001576491f56\",\"dataLength\":1024,\"creationDate\":{\"seconds\":1527454567,\"nanos\":791000000},\"lastModificationDate\":{\"seconds\":1527454567,\"nanos\":862000000},\"expirationDate\":{\"seconds\":1527458167,\"nanos\":862000000},\"checksum\":\"8T1XeRYf+gc19Tfh5plvVQHdllE=\"}\n";

        DatabaseFileMetadata.Builder builder = DatabaseFileMetadata.Builder.fromJson(json);
        final DatabaseFileMetadata databaseFileMetadata = builder.build();
        System.out.println(databaseFileMetadata.toString());
    }
}
