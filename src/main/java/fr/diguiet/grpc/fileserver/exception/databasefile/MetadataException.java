package fr.diguiet.grpc.fileserver.exception.databasefile;

/**
 * Represent a database file metadata exception
 */
public class MetadataException extends DatabaseFileException {

    public MetadataException() {

    }

    public MetadataException(final String msg) {
        super(msg);
    }

    public MetadataException(final Throwable cause) {
        super(cause);
    }

    public MetadataException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
