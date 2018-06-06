package fr.diguiet.grpc.rpc.service.provider.upload;

import com.google.protobuf.Timestamp;

public class UploadCompletion {
    private final Timestamp expiration;
    private final int totalLength;
    private final int nbChunk;
    private int nextExpectedChunk;
    private int receivedLength;
    private boolean isComplete;

    public static UploadCompletion newInstance(final int totalLength, final int nbChunk, final Timestamp expiration) {
        return (new UploadCompletion(totalLength, nbChunk, expiration));
    }

    private UploadCompletion(final int totalLength, final int nbChunk, final Timestamp expiration) {
        this.expiration = expiration;
        this.totalLength = totalLength;
        this.nbChunk = nbChunk;
        this.nextExpectedChunk = 0;
        this.receivedLength = 0;
        this.isComplete = false;
    }

    public boolean updateUploadCompletion(final int uploadLength, final int currentChunkIndex) {
        final int currentLength = this.receivedLength + uploadLength;
        if (this.isComplete) {
            throw new IllegalArgumentException("This upload is already complete");
        }
        if (this.nextExpectedChunk != currentChunkIndex) {
            throw new IllegalArgumentException("Invalid chunk index, must be sent in ascending order");
        }
        if (this.receivedLength == currentLength) {
            throw new IllegalArgumentException("Cannot receive empty chunk");
        }
        if (this.receivedLength > currentLength) {
            throw new IllegalArgumentException("The previous received length cannot be greater than the current received length");
        }
        if (this.receivedLength > this.totalLength) {
            throw new IllegalArgumentException("Received too much data");
        }
        this.receivedLength = currentLength;
        if (this.receivedLength == this.totalLength) {
            if (this.nbChunk != (this.nextExpectedChunk + 1)) {
                throw new IllegalArgumentException("Invalid number of chunk received");
            }
            this.isComplete = true;
        }
        ++this.nextExpectedChunk;
        return (this.isComplete);
    }

    public Timestamp getExpiration() {
        return (this.expiration);
    }
}
