package fr.diguiet.grpc.rpc.service.consumer;

import fr.diguiet.grpc.common.utils.BytesUtils;
import fr.diguiet.grpc.rpc.common.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Binary download service consumer class
 */
public class BinaryDownload {
    private static final Logger logger = LoggerFactory.getLogger(BinaryDownload.class);
    private final BinaryDownloadGrpc.BinaryDownloadBlockingStub downloadStub;

    /**
     * Create a new service consumer
     * @param channel The channel to register on
     * @return the service
     */
    public static BinaryDownload newServiceConsumer(final ManagedChannel channel) {
        return (new BinaryDownload(channel));
    }

    /**
     * Instantiate a new BinaryDownload consumer
     * @param channel The channel to register on
     * @throws IllegalArgumentException if channel is closed or unavailable
     */
    private BinaryDownload(final ManagedChannel channel) {
        Objects.requireNonNull(channel);
        if (channel.isShutdown() ||channel.isTerminated()) {
            throw new IllegalArgumentException("Channel must be open and available");
        }
        this.downloadStub = BinaryDownloadGrpc.newBlockingStub(channel);
    }

    /**
     * Download a chunk
     * @param blobId the blob id
     * @param offset the chunk offset
     * @param length the chunk length
     * @return the downloaded chunk or null if not found or error
     */
    public byte[] getChunk(final UUID blobId, final int offset, final int length) {
        final GetBlobChunkRequest request = GetBlobChunkRequest.newBuilder()
                .setBlobId(blobId)
                .setLength(length)
                .setStartOffset(offset)
                .build();
        try {
            final GetBlobChunkResponse response = this.downloadStub.getChunk(request);
            if (response.hasError()) {
                BinaryDownload.logger.error("get blob chunk error " + response.getError().getMessage());
            } else {
                return (response.getChunk().getPayload().toByteArray());
            }
        } catch (StatusRuntimeException e) {
            BinaryDownload.logger.warn("RPC failed: {}", e.getStatus());
        }
        return (null);
    }

    /**
     * Get a blob info
     * @param blobId the blob id
     * @return the blob info or null if not found or error
     */
    public BlobDownloadInfo getBlobInfo(final UUID blobId) {
        final GetBlobInfoRequest request = GetBlobInfoRequest.newBuilder().setBlobId(blobId).build();
        try {
            final GetBlobInfoResponse response = this.downloadStub.getBlobInfo(request);
            if (response.hasError()) {
                BinaryDownload.logger.error("get blob info error " + response.getError().getMessage());
            } else {
                return (response.getInfo());
            }
        } catch (StatusRuntimeException e) {
            BinaryDownload.logger.warn("RPC failed: {}", e.getStatus());
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
            final DeleteBlobResponse response = this.downloadStub.deleteBlob(request);
            if (response.hasError()) {
                BinaryDownload.logger.error("delete downloaded blob error " + response.getError().getMessage());
            }
        } catch (StatusRuntimeException e) {
            BinaryDownload.logger.warn("RPC failed: {0}", e.getStatus());
        }
    }

    /**
     * Download a blob
     * @param blobId the blob id to download
     * @param nbChunk the number of chunk to download the whole blob
     * @return the downloaded blob data or null if not found or error
     * @throws IllegalArgumentException if invalid number of chunk
     */
    public byte[] downloadBlob(final UUID blobId, final int nbChunk) {
        if (nbChunk < 1)
            throw new IllegalArgumentException("Number of chunk must be greater than 0");

        final BlobDownloadInfo blobInfo = this.getBlobInfo(blobId);
        final int blobLength = blobInfo.getBlobLength();
        if (blobLength < nbChunk)
            throw new IllegalArgumentException("The number of chunk must be smaller than the length of the blob to download");
        final int lengthPerChunk = blobInfo.getBlobLength() / nbChunk;
        byte[] imageData = new byte[0];

        for (int i = 0 ; i < nbChunk ; i++) {
            final byte[] dataChunk;
            final int offset = lengthPerChunk * i;
            if (i == (nbChunk - 1)) {
                dataChunk = this.getChunk(blobId, offset, blobLength - offset);
            } else {
                dataChunk = this.getChunk(blobId, offset, lengthPerChunk);
            }
            imageData = BytesUtils.merge(imageData, dataChunk);
        }
        BinaryDownload.logger.debug("original length {} vs downloaded length {}", blobInfo.getBlobLength(), imageData.length);
        return (imageData);
    }
}
