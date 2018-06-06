package fr.diguiet.grpc.fileserver.exception;

/**
 * Exception class used to describe a file server exception
 */
public class FileServerException extends Exception {
    public FileServerException() {

    }

    public FileServerException(final String msg) {
        super(msg);
    }

    public FileServerException(final Throwable cause) {
        super(cause);
    }

    public FileServerException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
