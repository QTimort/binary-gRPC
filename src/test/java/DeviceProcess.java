import fr.diguiet.grpc.fileserver.exception.FileServerException;
import fr.diguiet.grpc.rpc.device.Device;

import java.io.IOException;

public class DeviceProcess {
    public static void main(final String[] args) throws IOException, FileServerException, InterruptedException {
        if (args.length > 0) {
            final int port = Integer.parseInt(args[0]);
            final Device device = new Device();
            Runtime.getRuntime().addShutdownHook(new Thread(device::stop));
            device.start(port);
            device.blockUntilShutdown();
        }
    }
}
