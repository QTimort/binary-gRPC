package fr.diguiet.grpc.rpc.device;

import fr.diguiet.grpc.fileserver.exception.FileServerException;
import fr.diguiet.grpc.fileserver.IFileServer;
import fr.diguiet.grpc.fileserver.LMDBFileServer;
import fr.diguiet.grpc.rpc.service.provider.BinaryDownload;
import fr.diguiet.grpc.rpc.service.provider.BinaryUpload;
import fr.diguiet.grpc.rpc.service.provider.ImageAnalysis;
import fr.diguiet.grpc.rpc.service.provider.ImageGenerator;
import fr.diguiet.grpc.rpc.service.provider.interceptor.EnableCompressionInterceptor;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Device that manage binary services.
 */
public class Device {
    private static final Logger logger = LoggerFactory.getLogger(Device.class);
    private static final int FILE_EXPIRATION_SECONDS = 60 * 10; // 10 minutes
    private IFileServer fileServer;
    private Server server;

    /**
     * Start the device
     * @param port the port to listen on
     * @throws IOException
     * @throws FileServerException
     */
    public void start(final int port) throws IOException, FileServerException {
        this.fileServer = LMDBFileServer.newInstance().build();
        this.fileServer.openOrCreate();
        this.fileServer.deleteAll();
        this.server = ServerBuilder.forPort(port)
                .addService(BinaryUpload.newServiceProvider(this.fileServer, Device.FILE_EXPIRATION_SECONDS))
                .addService(ImageAnalysis.newServiceProvider(this.fileServer))
                .addService(ImageGenerator.newServiceProvider(this.fileServer, Device.FILE_EXPIRATION_SECONDS))
                .addService(BinaryDownload.newServiceProvider(this.fileServer))
                .intercept(EnableCompressionInterceptor.newInterceptor())
                .build()
                .start();
        Device.logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            Device.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    /**
     * Stop the device
     */
    public void stop() {
        if (this.server != null) {
            this.server.shutdown();
        }
        if (this.fileServer != null) {
            this.fileServer.close();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }
}
