package fr.diguiet.grpc.rpc.service.provider.download;


import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    private final ExpiringMap<UUID, ClientsDownloadCompletion> blobPartDownloaded = ExpiringMap.builder()
            .variableExpiration()
            .build();

    private DownloadManager() {

    }

    public static DownloadManager newInstance() {
        return (new DownloadManager());
    }

    public void deleteBlob(final UUID blobId) {
        this.blobPartDownloaded.remove(blobId);
    }

    public ClientsDownloadCompletion getBlob(final UUID blobId, final int totalLength, final Timestamp expirationSeconds) {
        this.addBlobIfAbsent(blobId, totalLength, TimestampUtils.getSecondsDifferenceFromNow(expirationSeconds));
        return (this.blobPartDownloaded.get(blobId));
    }

    private void addBlobIfAbsent(final UUID blobId, final int totalLength, long expirationSeconds) {
        if (expirationSeconds < 1)
            throw new IllegalArgumentException("Seconds before expiration must be greater than 0");
        if (!this.blobPartDownloaded.containsKey(blobId)) {
            this.blobPartDownloaded.put(blobId,
                    ClientsDownloadCompletion.newInstance(totalLength),
                    ExpirationPolicy.CREATED,
                    expirationSeconds,
                    TimeUnit.SECONDS);
        }
    }
}
