package fr.diguiet.grpc.rpc.service.provider;


import com.google.protobuf.GeneratedMessageV3;
import fr.diguiet.grpc.fileserver.DatabaseFile;
import fr.diguiet.grpc.fileserver.IFileServer;
import fr.diguiet.grpc.common.utils.ImageUtils;
import fr.diguiet.grpc.rpc.common.Error;
import fr.diguiet.grpc.rpc.common.GetImageLevelsResponse;
import fr.diguiet.grpc.rpc.common.GetImageLevelsRequest;
import fr.diguiet.grpc.rpc.common.ImageAnalysisGrpc;
import fr.diguiet.grpc.rpc.common.ImageLevels;
import fr.diguiet.grpc.rpc.service.provider.interceptor.ResponseStatusInterceptor;
import fr.diguiet.grpc.rpc.utils.MessageUtils;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Objects;

/**
 * Image analysis service consumer class
 */
public class ImageAnalysis extends ImageAnalysisGrpc.ImageAnalysisImplBase implements ResponseStatusInterceptor.ICallBack {
    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysis.class);
    private final IFileServer fileServer;
    private final ServerInterceptor responseStatusInterceptor = ResponseStatusInterceptor.newInterceptor(this);

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    public static ServerServiceDefinition newServiceProvider(final IFileServer fileServer) {
        final ImageAnalysis imageAnalysis = new ImageAnalysis(fileServer);
        return (ServerInterceptors.intercept(imageAnalysis, imageAnalysis.responseStatusInterceptor));
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    private ImageAnalysis(final IFileServer fileServer) {
        Objects.requireNonNull(fileServer);
        this.fileServer = fileServer;
    }

    /**
     * Compute the specified image levels
     * @param request the request
     * @param responseObserver the response observer
     */
    @Override
    public void getImageLevels(GetImageLevelsRequest request, StreamObserver<GetImageLevelsResponse> responseObserver) {
        final GetImageLevelsResponse.Builder builder = GetImageLevelsResponse.newBuilder();
        final java.util.UUID blobId = MessageUtils.getUUID(request.getBlobId());
        final DatabaseFile image = this.fileServer.getFile(blobId);
        if (image == null) {
            builder.setError(Error.newBuilder().setMessage("Invalid blobId"));
        } else {
            try {
                final BufferedImage bufferedImage = ImageUtils.fromBytes(image.getData());
                final long[] imageLevels = ImageUtils.getLevels(bufferedImage);
                final ImageLevels.Builder levelsBuilder = ImageLevels.newBuilder();
                Arrays.stream(imageLevels).forEach(levelsBuilder::addLevels);
                builder.setLevels(levelsBuilder);
            } catch (IOException e) {
                ImageAnalysis.logger.warn("Unable to get image " + blobId + " + from bytes ");
                ImageAnalysis.logger.debug("Message: " + e.getMessage());
                builder.setError(Error.newBuilder().setMessage(e.getMessage()));
            }
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * When response successfully received
     * @param clientIp the client ip
     * @param message the request message
     */
    @Override
    public void onResponseReceive(SocketAddress clientIp, GeneratedMessageV3 message) {
        if (message instanceof GetImageLevelsRequest) {
            GetImageLevelsRequest request = (GetImageLevelsRequest) message;
            if (request.hasBlobId()) {
                final java.util.UUID blobId = MessageUtils.getUUID(request.getBlobId());
                this.fileServer.delete(blobId);
                ImageAnalysis.logger.info("Command successfully received by the client, removing associated blob: " + blobId);
            }
        }
    }

    /**
     * On response cancel
     * @param clientIp the client ip
     * @param message the request message
     */
    @Override
    public void onResponseCancel(SocketAddress clientIp, GeneratedMessageV3 message) {

    }
}
