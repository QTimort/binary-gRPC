package fr.diguiet.grpc.rpc.service.consumer;

import fr.diguiet.grpc.rpc.common.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Image analysis service consumer class
 */
public class ImageAnalysis {
    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysis.class);
    private final ImageAnalysisGrpc.ImageAnalysisBlockingStub imageAnalysisStub;

    /**
     * Create a new service consumer
     * @param channel The channel to register on
     * @return the service
     */
    public static ImageAnalysis newServiceConsumer(final ManagedChannel channel) {
        return (new ImageAnalysis(channel));
    }

    /**
     * Instantiate a new ImageAnalysis consumer
     * @param channel The channel to register on
     * @throws IllegalArgumentException if channel is closed or unavailable
     */
    private ImageAnalysis(final ManagedChannel channel) {
        Objects.requireNonNull(channel);
        if (channel.isShutdown() ||channel.isTerminated()) {
            throw new IllegalArgumentException("Channel must be open and available");
        }
        this.imageAnalysisStub = ImageAnalysisGrpc.newBlockingStub(channel);
    }


    /**
     * Get the image levels
     * @param blobId the blobId of the image
     * @return the image levels or null if expired or error
     */
    public long[] getImageLevels(final UUID blobId) {
        final GetImageLevelsRequest request = GetImageLevelsRequest.newBuilder().setBlobId(blobId).build();
        try {
            final GetImageLevelsResponse response = this.imageAnalysisStub.getImageLevels(request);
            if (response.hasError()) {
                ImageAnalysis.logger.error("get image level error " + response.getError().getMessage());
            } else {
                return (response.getLevels().getLevelsList()
                        .stream()
                        .mapToLong(i -> i)
                        .toArray());
            }
        } catch (StatusRuntimeException e) {
            ImageAnalysis.logger.warn("RPC failed: {}", e.getStatus());
        }
        return (null);
    }
}
