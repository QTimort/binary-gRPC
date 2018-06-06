package fr.diguiet.grpc.rpc.client;

import fr.diguiet.grpc.rpc.service.consumer.BinaryDownload;
import fr.diguiet.grpc.rpc.service.consumer.BinaryUpload;
import fr.diguiet.grpc.rpc.service.consumer.ImageAnalysis;
import fr.diguiet.grpc.rpc.service.consumer.ImageGenerator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Class representation of a java-gRPC client
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final ManagedChannel channel;
    private final BinaryUpload binaryUpload;
    private final BinaryDownload binaryDownload;
    private final ImageAnalysis imageAnalysis;
    private final ImageGenerator imageGenerator;

    /**
     * Construct client connecting to BinaryDataService server at {@code host:port}.
     */
    public Client(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // add ssl support ?
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    Client(final ManagedChannel channel) {
        this.channel = channel;
        this.binaryUpload = BinaryUpload.newServiceConsumer(channel);
        this.binaryDownload = BinaryDownload.newServiceConsumer(channel);
        this.imageAnalysis = ImageAnalysis.newServiceConsumer(channel);
        this.imageGenerator = ImageGenerator.newServiceConsumer(channel);
    }

    /**
     * Shutdown the client
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public BinaryUpload getBinaryUpload() {
        return (this.binaryUpload);
    }

    public BinaryDownload getBinaryDownload() {
        return (this.binaryDownload);
    }

    public ImageAnalysis getImageAnalysis() {
        return (this.imageAnalysis);
    }

    public ImageGenerator getImageGenerator() {
        return (this.imageGenerator);
    }

    /**
     * Start the client
     */
    public void start() {

    }

}