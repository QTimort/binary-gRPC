package fr.diguiet.grpc.fileserver;

import fr.diguiet.grpc.common.utils.BytesUtils;
import fr.diguiet.grpc.fileserver.exception.databasefile.DatabaseFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Class representation of a database file
 * @see DatabaseFileMetadata
 */
@Immutable
public class DatabaseFile {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFile.class);
    private final byte data[];
    private final DatabaseFileMetadata databaseFileMetadata;

    /**
     * Create a new instance
     * @param databaseFileMetadata the metadata associated with the file
     * @param data the payload of the file
     * @return a new DatabaseFile instance
     */
    public static DatabaseFile newInstance(final DatabaseFileMetadata databaseFileMetadata, final byte[] data) {
        Objects.requireNonNull(databaseFileMetadata);
        Objects.requireNonNull(data);
        if (databaseFileMetadata.getDataLength() != data.length) {
            DatabaseFile.logger.debug("File and metadata length don't match " + data.length + databaseFileMetadata);
            throw new DatabaseFileException("File data length and metadata length doesn't match (" + data.length + ", " + databaseFileMetadata.getDataLength() + ")");
        }
        return (new DatabaseFile(databaseFileMetadata, data));
    }

    private DatabaseFile(final DatabaseFileMetadata databaseFileMetadata, final byte[] data) {
        this.data = data;
        this.databaseFileMetadata = databaseFileMetadata;
    }

    /**
     * Get the file data
     * @return the data
     */
    public byte[] getData() {
        return (this.data.clone());
    }

    /**
     * Get the file metadata
     * @return the metadata
     * @see DatabaseFileMetadata
     */
    public DatabaseFileMetadata getDatabaseFileMetadata() {
        return (this.databaseFileMetadata);
    }

    /**
     * String representation of the instance
     * @return a string representation of the instance
     */
    @Override
    public String toString() {
        return "DatabaseFile{" +
                "data checksum=" + BytesUtils.toBase64String(BytesUtils.getCheckSum(this.data)) +
                ", databaseFileMetadata=" + this.databaseFileMetadata +
                '}';
    }
}
