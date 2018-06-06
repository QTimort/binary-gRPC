package fr.diguiet.grpc.rpc.service.consumer;

import fr.diguiet.grpc.rpc.common.ImageGeneratorGrpc;
import fr.diguiet.grpc.rpc.common.TakeImageRequest;
import fr.diguiet.grpc.rpc.common.TakeImageResponse;
import fr.diguiet.grpc.rpc.common.UUID;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Image generator service consumer class
 */
public class ImageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerator.class);
    private final ImageGeneratorGrpc.ImageGeneratorBlockingStub imageGeneratorStub;

    /**
     * Create a new service consumer
     * @param channel The channel to register on
     * @return the service
     */
    public static ImageGenerator newServiceConsumer(final ManagedChannel channel) {
        return (new ImageGenerator(channel));
    }

    /**
     * Instantiate a new ImageGenerator consumer
     * @param channel The channel to register on
     * @throws IllegalArgumentException if channel is closed or unavailable
     */
    private ImageGenerator(final ManagedChannel channel) {
        Objects.requireNonNull(channel);
        if (channel.isShutdown() ||channel.isTerminated()) {
            throw new IllegalArgumentException("Channel must be open and available");
        }
        this.imageGeneratorStub = ImageGeneratorGrpc.newBlockingStub(channel);
    }

    /**
     * Request to take an image
     * @return the blob id to the generated image or null if error
     */
    public UUID takeImage() {
        final TakeImageRequest request = TakeImageRequest.newBuilder().build();
        try {
            final TakeImageResponse response = this.imageGeneratorStub.takeImage(request);
            if (response.hasError()) {
                ImageGenerator.logger.error("take image error " + response.getError().getMessage());
            } else {
                return (response.getBlobId());
            }
        } catch (StatusRuntimeException e) {
            ImageGenerator.logger.warn("RPC failed: {}", e.getStatus());
        }
        return (null);
    }
}
