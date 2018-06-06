package fr.diguiet.grpc.fileserver.exception;

/**
 * Exception class used to describe RunTime exception usually caused by an incorrect usage of an instantiated object or a function
 */
public class RunTimeFileServerException extends RuntimeException {

    public RunTimeFileServerException() {

    }

    public RunTimeFileServerException(final String msg) {
        super(msg);
    }

    public RunTimeFileServerException(final Throwable cause) {
        super(cause);
    }

    public RunTimeFileServerException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
