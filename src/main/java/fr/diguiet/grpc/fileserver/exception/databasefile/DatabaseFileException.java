package fr.diguiet.grpc.fileserver.exception.databasefile;

/**
 * Represent a database file exception
 */
public class DatabaseFileException extends RuntimeException {

    public DatabaseFileException() {

    }

    public DatabaseFileException(final String msg) {
        super(msg);
    }

    public DatabaseFileException(final Throwable cause) {
        super(cause);
    }

    public DatabaseFileException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
