package fr.diguiet.grpc.rpc.service.provider;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import fr.diguiet.grpc.fileserver.DatabaseFileMetadata;
import fr.diguiet.grpc.fileserver.IFileServer;
import fr.diguiet.grpc.rpc.common.*;
import fr.diguiet.grpc.rpc.common.Error;
import fr.diguiet.grpc.rpc.service.provider.download.DownloadManager;
import fr.diguiet.grpc.rpc.service.provider.interceptor.ResponseStatusInterceptor;
import fr.diguiet.grpc.rpc.utils.MessageUtils;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * Binary download service provider class
 */
public class BinaryDownload extends BinaryDownloadGrpc.BinaryDownloadImplBase implements ResponseStatusInterceptor.ICallBack {
    private static final Logger logger = LoggerFactory.getLogger(BinaryDownload.class);
    private final ServerInterceptor responseStatusInterceptor = ResponseStatusInterceptor.newInterceptor(this);
    private final DownloadManager downloadManager = DownloadManager.newInstance();
    private final IFileServer fileServer;

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    public static ServerServiceDefinition newServiceProvider(final IFileServer fileServer) {
        final BinaryDownload binaryDownload = new BinaryDownload(fileServer);
        return (ServerInterceptors.intercept(binaryDownload, binaryDownload.responseStatusInterceptor));
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    private BinaryDownload(final IFileServer fileServer) {
        Objects.requireNonNull(fileServer);
        this.fileServer = fileServer;
    }

    /**
     * Send the request chunk
     * @param request the Request
     * @param responseObserver the response observer
     */
    @Override
    public void getChunk(GetBlobChunkRequest request, StreamObserver<GetBlobChunkResponse> responseObserver) {
        GetBlobChunkResponse.Builder builder = GetBlobChunkResponse.newBuilder();
        final UUID blobId = MessageUtils.getUUID(request.getBlobId());
        final byte[] data = this.fileServer.download(blobId);
        final int startOffset = request.getStartOffset();
        final int length = request.getLength();
        ByteString chunk = ByteString.copyFrom(data, startOffset, length);
        builder.setChunk(BlobChunk.newBuilder().setPayload(chunk));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * Send the blob information
     * @param request The request
     * @param responseObserver The response observer
     */
    @Override
    public void getBlobInfo(GetBlobInfoRequest request, StreamObserver<GetBlobInfoResponse> responseObserver) {
        final GetBlobInfoResponse.Builder Response = GetBlobInfoResponse.newBuilder();
        final UUID blobId = MessageUtils.getUUID(request.getBlobId());
        final DatabaseFileMetadata databaseFileMetadata = this.fileServer.getFileMetaData(blobId);
        if (databaseFileMetadata != null) {
            Response.setInfo(BlobDownloadInfo.newBuilder().setBlobLength(databaseFileMetadata.getDataLength()).build());
        } else {
            Response.setError(Error.newBuilder().setMessage("Unable to get blob metadata info!").build());
        }
        responseObserver.onNext(Response.build());
        responseObserver.onCompleted();
    }

    /**
     * Delete the specified blob
     * @param request the request
     * @param responseObserver the response observer
     */
    @Override
    public void deleteBlob(DeleteBlobRequest request, StreamObserver<DeleteBlobResponse> responseObserver) {
        final UUID blobId = MessageUtils.getUUID(request.getBlobId());
        this.fileServer.delete(blobId);
        DeleteBlobResponse Response = DeleteBlobResponse.newBuilder().build();
        responseObserver.onNext(Response);
        responseObserver.onCompleted();
    }

    /**
     * When response successfully received
     * @param clientIp the client ip
     * @param message the request message
     */
    @Override
    public void onResponseReceive(final SocketAddress clientIp, final GeneratedMessageV3 message) {
        if (message instanceof GetBlobChunkRequest) {
            GetBlobChunkRequest chunkRequest = (GetBlobChunkRequest) message;
            if (chunkRequest.hasBlobId()) {
                final UUID blobId = MessageUtils.getUUID(chunkRequest.getBlobId());
                final DatabaseFileMetadata databaseFileMetadata = this.fileServer.getFileMetaData(blobId);
                if (databaseFileMetadata == null) {
                    this.downloadManager.deleteBlob(blobId);
                    BinaryDownload.logger.info("Removing blob " + blobId + " because it has been removed from fileserver");
                } else {
                    final int startOffset = chunkRequest.getStartOffset();
                    final int length = chunkRequest.getLength();
                    boolean isComplete = this.downloadManager.getBlob(blobId, databaseFileMetadata.getDataLength(), databaseFileMetadata.getExpirationDate())
                            .getClient(clientIp.toString())
                            .addChunk(startOffset, length);
                    if (isComplete) {
                        BinaryDownload.logger.info("Download of " + blobId + " complete for " + clientIp);
                        BinaryDownload.logger.debug("Metadata " + databaseFileMetadata);
                        this.fileServer.delete(blobId);
                    }
                }
            }
        }
    }

    /**
     * On response cancel
     * @param clientIp the client ip
     * @param message the request message
     */
    @Override
    public void onResponseCancel(final SocketAddress clientIp, final GeneratedMessageV3 message) {

    }
}
