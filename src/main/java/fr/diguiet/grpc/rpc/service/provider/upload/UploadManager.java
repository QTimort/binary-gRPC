package fr.diguiet.grpc.rpc.service.provider.upload;

import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UploadManager {
    private final ExpiringMap<UUID, UploadCompletion> blobPartUploaded = ExpiringMap.builder()
            .variableExpiration()
            .build();

    private UploadManager() {

    }

    public static UploadManager newInstance() {
        return (new UploadManager());
    }

    public void deleteBlob(final UUID blobId) {
        this.blobPartUploaded.remove(blobId);
    }

    public UploadCompletion getBlobCompletion(final UUID blobId) {
        if (this.blobPartUploaded.containsKey(blobId))
            return (this.blobPartUploaded.get(blobId));
        return (null);
    }

    public void addNewUpload(final UUID blobId, final int totalLength, final int nbChunk, final Timestamp expiration) {
        if (!this.blobPartUploaded.containsKey(blobId)) {
            this.blobPartUploaded.put(blobId,
                    UploadCompletion.newInstance(totalLength, nbChunk, expiration),
                    ExpirationPolicy.CREATED,
                    TimestampUtils.getSecondsDifferenceFromNow(expiration),
                    TimeUnit.SECONDS);
        } else {
            throw new IllegalArgumentException("Only one client is allowed to upload chunk to a specific blob");
        }
    }
}
