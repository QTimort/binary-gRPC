package fr.diguiet.grpc.fileserver;

import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.fileserver.exception.FileServerException;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.UUID;

/**
 * Interface that represent a File server
 * @see Closeable
 */
public interface IFileServer extends Closeable {

    /**
     * Open or create the file server
     * @throws FileServerException
     */
    void openOrCreate() throws FileServerException;

    /**
     * Close the file server, must be idempotent because of Closeable
     * @see Closeable
     */
    void close();

    /**
     * Upload data to a file with a expiration date
     * Multiple call to an existing fileId will result in the data being append
     * @param fileId The file id
     * @param expirationDate The expiration date
     * @param data The data to save
     */
    void upload(final UUID fileId, final Timestamp expirationDate, final byte[] data);

    /**
     * Download a file data with the specified id
     * @param fileId the file id
     * @return the file data or Null if the file wasn't found or is expired
     */
    @Nullable byte[] download(final UUID fileId);

    /**
     * Get the file metadata related to the specified file id
     * @param fileId the file id
     * @return The file metadata Or Null if not found or is expired
     * @see DatabaseFileMetadata
     */
    @Nullable DatabaseFileMetadata getFileMetaData(final UUID fileId);

    /**
     * Get a file with his data and metadata
     * @param fileId the file id to get
     * @return The file
     * @see DatabaseFile
     */
    @Nullable DatabaseFile getFile(final UUID fileId);

    /**
     * Delete a file with the specified id
     * @param fileId the file id to delete
     */
    void delete(final UUID fileId);

    /**
     * Delete all entries
     */
    void deleteAll();

    /**
     * Predicate that tell whether or not a file is expired
     * @param fileId the file to check
     * @return if expired or not
     */
    boolean isExpired(final UUID fileId);

    /**
     * Remove all expired entries
     */
    void removeExpired();

    /**
     * Get the available usable byte on the file server
     * @return the available usable byte
     */
    long getAvailableUsableByte();

    /**
     * Predicate that tell whether or not the file server has enough space to star nbByte
     * @param nbByte The number of byte
     * @return if has enough space for nbByte or not
     */
    boolean hasEnoughSpaceFor(final int nbByte);

    /**
     * Predicate that tell whether or not the file server is open
     * @return If the file server is open or not
     */
    boolean isOpen();
}