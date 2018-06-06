package fr.diguiet.grpc.rpc.service.provider;

import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.fileserver.IFileServer;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import fr.diguiet.grpc.rpc.common.*;
import fr.diguiet.grpc.rpc.common.Error;
import fr.diguiet.grpc.rpc.service.provider.upload.UploadCompletion;
import fr.diguiet.grpc.rpc.service.provider.upload.UploadManager;
import fr.diguiet.grpc.rpc.utils.MessageUtils;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Binary upload service provider class
 */
public class BinaryUpload extends BinaryUploadGrpc.BinaryUploadImplBase {
    private static final int DEFAULT_FILE_EXPIRATION_SECONDS = 60 * 10; // 10 minutes
    private static final Logger logger = LoggerFactory.getLogger(BinaryUpload.class);
    private final UploadManager uploadManager = UploadManager.newInstance();
    private final IFileServer fileServer;
    private final int blobExpirationSeconds;

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    public static BinaryUpload newServiceProvider(final IFileServer fileServer) {
        return (new BinaryUpload(fileServer));
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @param fileExpirationSeconds the number of seconds becore a file expire
     * @return the service
     */
    public static BinaryUpload newServiceProvider(final IFileServer fileServer, final int fileExpirationSeconds) {
        return (new BinaryUpload(fileServer, fileExpirationSeconds));
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    private BinaryUpload(final IFileServer fileServer) {
        this(fileServer, BinaryUpload.DEFAULT_FILE_EXPIRATION_SECONDS);
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @param blobExpirationSeconds the number of seconds becore a file expire
     * @return the service
     */
    private BinaryUpload(final IFileServer fileServer, final int blobExpirationSeconds) {
        Objects.requireNonNull(fileServer);
        if (blobExpirationSeconds < 1) {
            throw new IllegalArgumentException("The number of seconds before the file expire must be greater than 0");
        }
        this.fileServer = fileServer;
        this.blobExpirationSeconds = blobExpirationSeconds;
    }

    /**
     * Check if the file server have enough space to store the specified blob
     * @param request the request
     * @param responseObserver the response observer
     */
    @Override
    public void createBlob(CreateBlobRequest request, StreamObserver<CreateBlobResponse> responseObserver) {
        final ServerCallStreamObserver<CreateBlobResponse> serverCallStreamObserver = (ServerCallStreamObserver<CreateBlobResponse>) responseObserver;

        final CreateBlobResponse.Builder builder = CreateBlobResponse.newBuilder();
        if (!this.fileServer.hasEnoughSpaceFor(request.getBlobSize())) {
            builder.setError(Error.newBuilder().setMessage("Not enough space available!")).build();
        } else {
            final java.util.UUID uuid = java.util.UUID.randomUUID();
            final UUID uuidMessage = MessageUtils.toUUIDMessage(uuid);
            final Timestamp expirationDate = TimestampUtils.nowPlusSeconds(this.blobExpirationSeconds);
            builder.setInfo(BlobCreationInfo.newBuilder()
                    .setBlobId(uuidMessage)
                    .setExpirationDate(expirationDate)).build();
            this.uploadManager.addNewUpload(uuid, request.getBlobSize(), request.getChunkCount(), expirationDate);
            serverCallStreamObserver.setOnCancelHandler(() -> this.uploadManager.deleteBlob(uuid));
        }
        serverCallStreamObserver.onNext(builder.build());
        serverCallStreamObserver.onCompleted();
    }

    /**
     * Save the uploaded chunk
     * @param request the request
     * @param responseObserver  the response observer
     */
    @Override
    public void uploadChunk(UploadBlobRequest request, StreamObserver<UploadBlobResponse> responseObserver) {
        final java.util.UUID blobId = MessageUtils.getUUID(request.getBlobId());
        final UploadCompletion blobCompletion = this.uploadManager.getBlobCompletion(blobId);
        final Timestamp expirationDate;
        if (blobCompletion != null) {
            expirationDate = blobCompletion.getExpiration();
        } else {
            expirationDate = TimestampUtils.nowPlusSeconds(this.blobExpirationSeconds);
        }
        BinaryUpload.logger.debug("upload chunk blob " + blobId);
        this.fileServer.upload(blobId, expirationDate, request.getPayload().toByteArray());
        UploadBlobResponse Response;
        try {
            if (blobCompletion != null) {
                blobCompletion.updateUploadCompletion(request.getPayload().size(), request.getChunkIndex());
            } else {
                throw new IllegalArgumentException("Not allowed to upload this blob");
            }
            Response = UploadBlobResponse.newBuilder().setExpirationDate(expirationDate).build();
        } catch (IllegalArgumentException e) {
            Response = UploadBlobResponse.newBuilder().setError(Error.newBuilder().setMessage(e.getMessage()).build()).build();
        }
        responseObserver.onNext(Response);
        responseObserver.onCompleted();
    }

    /**
     * Delete the requested uploaded blob id
     * @param request the request
     * @param responseObserver the response observer
     */
    @Override
    public void deleteBlob(DeleteBlobRequest request, StreamObserver<DeleteBlobResponse> responseObserver) {
        final java.util.UUID blobId = MessageUtils.getUUID(request.getBlobId());
        this.fileServer.delete(blobId);
        DeleteBlobResponse Response = DeleteBlobResponse.newBuilder().build();
        responseObserver.onNext(Response);
        responseObserver.onCompleted();
    }

    /**
     * Get the number of seconds before a blob expire
     * @return the number of seconds before a blob expire
     */
    public int getBlobExpirationSeconds() {
        return (this.blobExpirationSeconds);
    }
}
