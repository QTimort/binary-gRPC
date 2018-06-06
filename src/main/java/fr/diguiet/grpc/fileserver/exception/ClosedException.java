package fr.diguiet.grpc.fileserver.exception;

/**
 * Represent a file server close exception
 * It can occur when you tried to perform an illegal operation with a closed file server
 */
public class ClosedException extends RunTimeFileServerException {
    public ClosedException() {
        super("You cannot perform this action because the file server is closed!");
    }

    public ClosedException(String msg) {
        super(msg);
    }

    public ClosedException(Throwable cause) {
        super(cause);
    }

    public ClosedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
