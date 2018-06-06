package fr.diguiet.grpc.fileserver.exception;

/**
 * Represent a file server already open exception
 * It can occur when you tried open an already open file server
 */
public class AlreadyOpenException extends RunTimeFileServerException {
    public AlreadyOpenException() {
        super("The file server is already open!");
    }

    public AlreadyOpenException(String msg) {
        super(msg);
    }

    public AlreadyOpenException(Throwable cause) {
        super(cause);
    }

    public AlreadyOpenException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
