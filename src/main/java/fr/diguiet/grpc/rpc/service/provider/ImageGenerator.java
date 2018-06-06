package fr.diguiet.grpc.rpc.service.provider;

import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.common.utils.ImageUtils;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import fr.diguiet.grpc.fileserver.IFileServer;
import fr.diguiet.grpc.image.generator.MandelBrot;
import fr.diguiet.grpc.rpc.common.Error;
import fr.diguiet.grpc.rpc.common.ImageGeneratorGrpc;
import fr.diguiet.grpc.rpc.common.TakeImageResponse;
import fr.diguiet.grpc.rpc.common.TakeImageRequest;
import fr.diguiet.grpc.rpc.utils.MessageUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Image generator service consumer class
 */
public class ImageGenerator extends ImageGeneratorGrpc.ImageGeneratorImplBase {
    private static final int DEFAULT_FILE_EXPIRATION_SECONDS = 60 * 60; // 1 hour
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerator.class);
    private final IFileServer fileServer;
    private final int fileExpirationSeconds;

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    public static ImageGenerator newServiceProvider(final IFileServer fileServer) {
        return (new ImageGenerator(fileServer));
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @param fileExpirationSeconds the number of seconds before a file expire
     * @return the service
     */
    public static ImageGenerator newServiceProvider(final IFileServer fileServer, final int fileExpirationSeconds) {
        return (new ImageGenerator(fileServer, fileExpirationSeconds));
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @return the service
     */
    private ImageGenerator(final IFileServer fileServer) {
        this(fileServer, ImageGenerator.DEFAULT_FILE_EXPIRATION_SECONDS);
    }

    /**
     * Create a new service provider
     * @param fileServer The file server
     * @param fileExpirationSeconds the number of seconds becore a file expire
     * @return the service
     */
    private ImageGenerator(final IFileServer fileServer, final int fileExpirationSeconds) {
        Objects.requireNonNull(fileServer);
        if (fileExpirationSeconds < 1) {
            throw new IllegalArgumentException("The number of seconds before the file expire must be greater than 0");
        }
        this.fileServer = fileServer;
        this.fileExpirationSeconds = fileExpirationSeconds;
    }

    /**
     * Take an image
     * @param request the request
     * @param responseObserver the response observer
     */
    @Override
    public void takeImage(final TakeImageRequest request, final StreamObserver<TakeImageResponse> responseObserver) {
        final TakeImageResponse.Builder builder = TakeImageResponse.newBuilder();
        final MandelBrot mandelBrot = MandelBrot.newInstance().build();
        final BufferedImage bufferedImage = mandelBrot.generateImage();
        try {
            final byte[] bytes = ImageUtils.toBytes(bufferedImage);
            final UUID blobId = UUID.randomUUID();
            final Timestamp expirationDate = TimestampUtils.nowPlusSeconds(this.fileExpirationSeconds);
            this.fileServer.upload(blobId, expirationDate, bytes);
            builder.setBlobId(MessageUtils.toUUIDMessage(blobId));
        } catch (IOException e) {
            ImageGenerator.logger.warn("Unable to convert image into bytes " + e.getMessage());
            ImageGenerator.logger.debug("Request " + request + " Mandelbrot " + mandelBrot);
            builder.setError(Error.newBuilder().setMessage(e.getMessage()));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
