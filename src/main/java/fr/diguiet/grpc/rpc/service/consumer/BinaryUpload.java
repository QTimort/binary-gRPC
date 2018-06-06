package fr.diguiet.grpc.rpc.service.consumer;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.common.utils.BytesUtils;
import fr.diguiet.grpc.rpc.common.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Binary upload service consumer class
 */
public class BinaryUpload {
    private static final Logger logger = LoggerFactory.getLogger(BinaryUpload.class);
    private final BinaryUploadGrpc.BinaryUploadBlockingStub uploadStub;

    /**
     * Create a new service consumer
     * @param channel The channel to register on
     * @return the service
     */
    public static BinaryUpload newServiceConsumer(final ManagedChannel channel) {
        return (new BinaryUpload(channel));
    }

    /**
     * Instantiate a new BinaryUpload consumer
     * @param channel The channel to register on
     * @throws IllegalArgumentException if channel is closed or unavailable
     */
    private BinaryUpload(final ManagedChannel channel) {
        Objects.requireNonNull(channel);
        if (channel.isShutdown() || channel.isTerminated()) {
            throw new IllegalArgumentException("Channel must be open and available");
        }
        this.uploadStub = BinaryUploadGrpc.newBlockingStub(channel);
    }

    /**
     * Request the creation of a blob
     * @param dataLength the length of the data
     * @param nbChunk the number of chunk
     * @return a BlobCreationInfo or null if not enough space or error
     */
    public BlobCreationInfo createBlob(final int dataLength, final int nbChunk) {
        final CreateBlobRequest request = CreateBlobRequest.newBuilder().setBlobSize(dataLength).setChunkCount(nbChunk).build();
        try {
            final CreateBlobResponse response = this.uploadStub.createBlob(request);
            BinaryUpload.logger.debug("response: " + response.toString());
            if (response.hasError()) {
                BinaryUpload.logger.error("create blob error " + response.getError().getMessage());
            } else if (response.hasInfo()) {
                return (response.getInfo());
            }
        } catch (StatusRuntimeException e) {
            BinaryUpload.logger.warn("RPC failed: {}", e.getStatus());
        }
        return (null);
    }

    /**
     * Upload a chunk to a specific blob id
     * Chunks must be sent in ascending chunk index
     * @param uuid the blobId
     * @param chunkIndex the chunk index
     * @param payload the chunk data
     * @return The blob id expiration date or null if error
     */
    public Timestamp uploadChunk(final UUID uuid, final int chunkIndex, final byte[] payload) {
        final UploadBlobRequest request = UploadBlobRequest.newBuilder()
                .setBlobId(uuid)
                .setChunkIndex(chunkIndex)
                .setPayload(ByteString.copyFrom(payload))
                .build();
        try {
            final UploadBlobResponse response = this.uploadStub.withCompression("gzip").uploadChunk(request);
            if (response.hasError()) {
                BinaryUpload.logger.error("upload blob chunk error " + response.getError().getMessage());
            } else if (response.hasExpirationDate()) {
                return (response.getExpirationDate());
            }
        } catch (StatusRuntimeException e) {
            BinaryUpload.logger.warn("RPC failed: {}", e.getStatus());
        }
        return (null);
    }

    /**
     * Delete a blob
     * @param blobId the blod id to delete
     */
    public void deleteBlob(final UUID blobId) {
        final DeleteBlobRequest request = DeleteBlobRequest.newBuilder().setBlobId(blobId).build();
        try {
            final DeleteBlobResponse response = this.uploadStub.deleteBlob(request);
            if (response.hasError()) {
                BinaryUpload.logger.error("delete uploaded blob error " + response.getError().getMessage());
            }
        } catch (StatusRuntimeException e) {
            BinaryUpload.logger.warn("RPC failed: {}", e.getStatus());
        }
    }

    /**
     * Upload a blob
     * @param data  the blob data
     * @param nbChunk the number of chunk to make to send the data
     * @return The blobCreationInfo or null if not enough space or error
     */
    public BlobCreationInfo uploadBlob(final byte data[], final int nbChunk) {
        final byte chunks[][] = BytesUtils.split(data, nbChunk);

        BinaryUpload.logger.debug("data checksum " + BytesUtils.toBase64String(BytesUtils.getCheckSum(data)));
        final BlobCreationInfo blob = this.createBlob(data.length, nbChunk);
        if (blob == null) {
            BinaryUpload.logger.error("Failed to create blob!");
            return (null);
        }
        final UUID uuid = blob.getBlobId();
        Timestamp expiration = null;
        for (int i = 0; i < nbChunk; ++i) {
            expiration = this.uploadChunk(uuid, i, chunks[i]);
            if (expiration == null) {
                BinaryUpload.logger.error("Failed to upload blob with id " + uuid);
                return (null);
            }
        }
        return (BlobCreationInfo.newBuilder().setBlobId(uuid).setExpirationDate(expiration).build());
    }
}
