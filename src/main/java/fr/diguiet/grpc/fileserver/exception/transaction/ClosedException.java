package fr.diguiet.grpc.fileserver.exception.transaction;

/**
 * Represent a transaction close exception
 * It can occur when you tried to perform an illegal operation with a closed transaction
 */
public class ClosedException extends TransactionException {
    public ClosedException() {
        super("You cannot perform this action because the transaction is closed!");
    }
}
