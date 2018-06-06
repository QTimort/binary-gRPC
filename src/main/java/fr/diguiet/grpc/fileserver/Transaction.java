package fr.diguiet.grpc.fileserver;

import fr.diguiet.grpc.fileserver.exception.transaction.ClosedException;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * Encapsulation class of a LMDB transaction
 */
public class Transaction implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private final Txn<ByteBuffer> transaction;
    private final boolean readOnly;
    private boolean isOpen = true;

    /**
     * Create a single use transaction
     * @param env the LMDB env
     * @see Env
     * @return a new Transaction
     */
    public static Transaction newInstance(final Env<ByteBuffer> env) {
        return (new Transaction(env, true));
    }

    /**
     * Create a single use transaction
     * @param env the LMDB env
     * @param readOnly specifiy if the transaction is in read only or not
     * @see Env
     * @return a new Transaction
     */
    public static Transaction newInstance(final Env<ByteBuffer> env, boolean readOnly) {
        return (new Transaction(env, readOnly));
    }

    /**
     * Create a new transaction instance
     * @param env LMDB env
     * @param readOnly is read only
     * @see Env
     */
    private Transaction(final Env<ByteBuffer> env, boolean readOnly) {
        this.transaction = (readOnly) ? env.txnRead() : env.txnWrite();
        this.readOnly = readOnly;
        Transaction.logger.debug("[" + this.transaction.getId() + "]" + " Begin of new transaction, readOnly=" + this.readOnly);
    }

    /**
     * Close the transaction, must be idempotent because of Closeable
     * @see Closeable
     */
    @Override
    public void close() {
        if (this.isOpen) {
            if (!this.readOnly) {
                this.transaction.commit();
            }
            this.transaction.close();
            Transaction.logger.debug("[" + this.transaction.getId() + "]" + " End of transaction, readOnly=" + this.readOnly);
            this.isOpen = false;
        }
    }

    /**
     * Just in case the transaction is not closed properly
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        if (this.isOpen) {
            Transaction.logger.warn("Transaction has not been closed properly");
            this.close();
        }
        super.finalize();
    }

    /**
     * Get the LMDB transaction instance
     * @return the LMDB transaction instance
     */
    public Txn<ByteBuffer> get() {
        this.mustBeOpenOrThrow("Tried to get a closed transaction");
        return (this.transaction);
    }

    /**
     * Predicate that tell whether or not the transaction is open
     * @return If the transaction is open or not
     */
    public boolean isOpen() {
        return (this.isOpen);
    }

    /**
     * Throw an exception if the file server is open
     * @param errorLogMessage the exception error message
     * @throws ClosedException
     */
    private void mustBeOpenOrThrow(@Nullable final String errorLogMessage) {
        if (!this.isOpen) {
            if (errorLogMessage != null)
                Transaction.logger.error(errorLogMessage);
            throw new ClosedException();
        }
    }

    /**
     * Throw an exception if the file server is closed
     * @throws ClosedException
     */
    private void mustBeOpenOrThrow() {
        this.mustBeOpenOrThrow(null);
    }

    /**
     * To string representation of the instance
     * @return the string representation of the instance
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "transaction=" + this.transaction +
                ", readOnly=" + this.readOnly +
                ", isOpen=" + this.isOpen +
                '}';
    }
}
