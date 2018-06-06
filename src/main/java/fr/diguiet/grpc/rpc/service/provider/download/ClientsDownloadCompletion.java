package fr.diguiet.grpc.rpc.service.provider.download;

import java.util.HashMap;

public class ClientsDownloadCompletion {
    private final HashMap<String, DownloadCompletion> clientsDownload = new HashMap<>();
    private final int totalLength;

    public static ClientsDownloadCompletion newInstance(final int totalLength) {
        return (new ClientsDownloadCompletion(totalLength));
    }

    private ClientsDownloadCompletion(final int totalLength) {
        this.totalLength = totalLength;
    }

    /**
     *
     * @param clientIp
     *
     */
    private void addClientIfAbsent(final String clientIp) {
        if (!this.clientsDownload.containsKey(clientIp)) {
            this.clientsDownload.put(clientIp, DownloadCompletion.newInstance(this.totalLength));
        }
    }

    public DownloadCompletion getClient(final String clientIp) {
        this.addClientIfAbsent(clientIp);
        return (this.clientsDownload.get(clientIp));
    }
}
