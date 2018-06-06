import fr.diguiet.grpc.common.utils.ImageUtils;
import fr.diguiet.grpc.image.generator.MandelBrot;
import fr.diguiet.grpc.rpc.client.Client;
import fr.diguiet.grpc.rpc.common.BlobCreationInfo;
import fr.diguiet.grpc.rpc.common.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class Grpc {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static Process newProcessDevice(final int port) throws IOException {
        String classpath = Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                .map(URL::getFile)
                .collect(Collectors.joining(File.pathSeparator));
        return (new ProcessBuilder(
                System.getProperty("java.home") + "/bin/java",
                "-classpath",
                classpath,
                DeviceProcess.class.getCanonicalName(),
                Integer.toString(port))
                .inheritIO()
                .start());
    }

    public static void testClient(final Client client, final boolean deleteImageAfter) throws IOException {
        final MandelBrot mandelBrot = MandelBrot.newInstance().build();
        final BufferedImage bufferedImage = mandelBrot.generateImage();
        final byte data[] = ImageUtils.toBytes(bufferedImage);
        final BlobCreationInfo blobInfo = client.getBinaryUpload().uploadBlob(data, 16);
        final UUID imageId = client.getImageGenerator().takeImage();
        if (imageId != null) {
            final byte[] bytes = client.getBinaryDownload().downloadBlob(blobInfo.getBlobId(), 8);
            final BufferedImage bytesToImage = ImageUtils.fromBytes(bytes);
            final File file = new File("downloadedImage " +
                    new SimpleDateFormat("HH-mm-ss.SSS").format(new Date()) +
                    "." + ImageUtils.getDefaultImgExt());
            ImageUtils.save(bytesToImage, file);
            if (deleteImageAfter)
                file.delete();
        }
    }

    public static Runnable newClientRunnable(final int port) {
        return (() -> {
            final Client client = new Client("localhost", port);
            client.start();
            try {
                testClient(client, false);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        });
    }

    @Test
    public void multipleDeviceAndClient() throws IOException, InterruptedException {
        final Process process = Grpc.newProcessDevice(1337);
        final Process process1 = Grpc.newProcessDevice(1338);
        final Process process2 = Grpc.newProcessDevice(1339);
        final Process process3 = Grpc.newProcessDevice(1340);
        final Process process4 = Grpc.newProcessDevice(1341);
        final Thread client1 = new Thread(newClientRunnable(1337));
        final Thread client2 = new Thread(newClientRunnable(1338));
        final Thread client3 = new Thread(newClientRunnable(1339));
        final Thread client4 = new Thread(newClientRunnable(1340));
        final Thread client5 = new Thread(newClientRunnable(1341));

        Thread.sleep(6000);

        client1.start();
        client2.start();
        client3.start();
        client4.start();
        client5.start();

        client1.join();
        client2.join();
        client3.join();
        client4.join();
        client5.join();

        process.destroy();
        process1.destroy();
        process2.destroy();
        process3.destroy();
        process4.destroy();

        process.waitFor();
        process1.waitFor();
        process2.waitFor();
        process3.waitFor();
        process4.waitFor();
    }

    @Test
    public void oneDeviceAndClients() throws InterruptedException, IOException {
        final Process process = Grpc.newProcessDevice(1337);
        final Thread client1 = new Thread(newClientRunnable(1337));
        final Thread client2 = new Thread(newClientRunnable(1337));
        final Thread client3 = new Thread(newClientRunnable(1337));

        Thread.sleep(6000);

        client1.start();
        client2.start();
        client3.start();

        client1.join();
        client2.join();
        client3.join();

        process.destroy();

        process.waitFor();
    }
}
