package fr.diguiet.grpc.fileserver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.common.IJsonSerialize;
import fr.diguiet.grpc.common.utils.BytesUtils;
import fr.diguiet.grpc.common.utils.StringUtils;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import fr.diguiet.grpc.fileserver.exception.databasefile.MetadataException;
import fr.diguiet.grpc.fileserver.json.TimestampDataBind;
import fr.diguiet.grpc.fileserver.exception.databasefile.SerializerOutOfBound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Represent the metadata of a database file
 * Serializable to json
 * @see DatabaseFileMetadata
 */
@Immutable
public class DatabaseFileMetadata implements IJsonSerialize {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFileMetadata.class);
    private static final int MAX_SERIALIZED_LENGTH = 1024;
    private final UUID id;
    private final int dataLength;
    private final Timestamp creationDate;
    private final Timestamp lastModificationDate;
    private final Timestamp expirationDate;
    private final byte[] checksum;

    /**
     * Create a new instance using a builder
     * @param builder the builder
     */
    private DatabaseFileMetadata(final Builder builder) {
        this.id = builder.id;
        this.dataLength = builder.dataLength;
        this.creationDate = builder.creationDate;
        this.expirationDate = builder.expirationDate;
        this.lastModificationDate = builder.lastModificationDate;
        this.checksum = builder.checksum;
    }

    /**
     * Serialize the instance into a Json string
     * @return a Json string
     * @see IJsonSerialize
     */
    @Override @JsonIgnore
    public String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return (mapper.writeValueAsString(this));
        } catch (JsonProcessingException e) {
            DatabaseFileMetadata.logger.warn("Unable to convert object into json string");
            DatabaseFileMetadata.logger.debug("DatabaseFileMetadata: " + this);
            throw new MetadataException(e);
        }
    }

    /**
     * Step Builder class to build new metadata instance
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements Id, DataLength, ExpirationDate, CreationDate, LastModificationDate, Checksum, Build {
        private UUID id;
        private int dataLength;
        private byte[] checksum;
        private Timestamp creationDate;
        private Timestamp expirationDate;
        private Timestamp lastModificationDate;

        /**
         * Create a new builder
         * @return a new builder
         */
        @JsonIgnore
        public static Id newBuilder() {
            return (new Builder());
        }

        /**
         * Create a new builder from an existing metadata instance
         * @param databaseFileMetadata the prototype
         * @return a new builder
         */
        @JsonIgnore
        public static Builder fromPrototype(final DatabaseFileMetadata databaseFileMetadata) {
            Objects.requireNonNull(databaseFileMetadata);
            return (new Builder(databaseFileMetadata));
        }

        /**
         * Create a new builder from a Json representation of the builder as prototype
         * @param json the seriliazed builder
         * @return A new builder
         */
        @JsonIgnore
        public static DatabaseFileMetadata.Builder fromJson(final String json) {
            Objects.requireNonNull(json);
            ObjectMapper mapper = new ObjectMapper();
            try {
                final Builder builder = mapper.readValue(json, Builder.class);
                // double check since jackson doesn't check required fields
                builder.checkRequiredField();
                return (builder);
            } catch (final IOException | IllegalArgumentException e) {
                DatabaseFileMetadata.logger.warn("Unable to convert json string into builder " + e.getMessage());
                DatabaseFileMetadata.logger.debug("json string: " + json);
                throw new MetadataException(e);
            }
        }

        /**
         * Specify the metadata file id
         * @param id the Id of the file
         * @return The next step
         */
        @Override @JsonIgnore
        public DataLength id(final UUID id) {
            return (this.setId(id));
        }

        /**
         * Specify the data length of the file
         * @param  dataLength data length  of the file
         * @return The next step
         */
        @Override @JsonIgnore
        public Checksum dataLength(int dataLength) {
            return (this.setDataLength(dataLength));
        }

        /**
         * Specify the checksum of the file data
         * @param checksum the checksum of the file data
         * @return The next step
         */
        @Override @JsonIgnore
        public CreationDate checksum(final byte[] checksum) {
            return (this.setChecksum(checksum));
        }

        /**
         * Specify the creation date of the file
         * @param creationDate the creation date of the file
         * @return The next step
         */
        @Override @JsonIgnore
        public ExpirationDate creationDate(final Timestamp creationDate) {
            return (this.setCreationDate(creationDate));
        }

        /**
         * Specify the expiration date of the file
         * @param expirationDate the expiration date of the file
         * @return The next step
         */
        @Override @JsonIgnore
        public LastModificationDate expirationDate(final Timestamp expirationDate) {
            return (this.setExpirationDate(expirationDate));
        }

        /**
         * Specify the last modification date of the file
         * @param lastModificationDate the last modification date of the file
         * @return The next step
         */
        @Override @JsonIgnore
        public Build lastModificationDate(final Timestamp lastModificationDate) {
            return (this.setLastModificationDate(lastModificationDate));
        }

        /**
         * Build the file metadata instance
         * @return a built instance of file metadata
         * @throws SerializerOutOfBound if the serialized length of the built object is longer than {@value #MAX_SERIALIZED_LENGTH}
         */
        @Override @JsonIgnore
        public DatabaseFileMetadata build() {
            final DatabaseFileMetadata databaseFileMetadata = new DatabaseFileMetadata(this);
            final int serializedLength = databaseFileMetadata.toJsonString().length();
            if (serializedLength > DatabaseFileMetadata.MAX_SERIALIZED_LENGTH) {
                DatabaseFileMetadata.logger.error("Serialized length (" + serializedLength + ") greater than max (" + DatabaseFileMetadata.getMaxSerializedLength() + ")");
                DatabaseFileMetadata.logger.debug("DatabaseFileMetadata: " + databaseFileMetadata);
                throw new SerializerOutOfBound(serializedLength);
            }
            return (new DatabaseFileMetadata(this));
        }

        /**
         * Only to allow Jackson to deserialize automatically the builder
         */
        private Builder setId(final UUID id) {
            Objects.requireNonNull(id);
            this.id = id;
            return (this);
        }

        /**
         * Only to allow Jackson to deserialize automatically the builder
         */
        private Builder setDataLength(final int dataLength) {
            if (dataLength < 0)
                throw new IllegalArgumentException("Data length must be a value greater than or equal to 0");
            this.dataLength = dataLength;
            return (this);
        }

        /**
         * Only to allow Jackson to deserialize automatically the builder
         */
        @JsonDeserialize(using = TimestampDataBind.Deserializer.class)
        private Builder setCreationDate(final Timestamp creationDate) {
            Objects.requireNonNull(creationDate);
            this.creationDate = creationDate;
            return (this);
        }

        /**
         * Only to allow Jackson to deserialize automatically the builder
         */
        @JsonDeserialize(using = TimestampDataBind.Deserializer.class)
        private Builder setExpirationDate(final Timestamp expirationDate) {
            Objects.requireNonNull(expirationDate);
            if (!TimestampUtils.isAfter(this.creationDate, expirationDate))
                throw new IllegalArgumentException("Expiration date must be after the creation date");
            this.expirationDate = expirationDate;
            return (this);
        }

        /**
         * Only to allow Jackson to deserialize automatically the builder
         */
        @JsonDeserialize(using = TimestampDataBind.Deserializer.class)
        private Builder setLastModificationDate(final Timestamp lastModificationDate) {
            Objects.requireNonNull(lastModificationDate);
            if (!TimestampUtils.isAfterOrEqual(this.creationDate, lastModificationDate))
                throw new IllegalArgumentException("Last modification date must be after or equal to the creation date");
            this.lastModificationDate = lastModificationDate;
            return (this);
        }

        /**
         * Only to allow Jackson to deserialize automatically the builder
         */
        private Builder setChecksum(final byte[] checksum) {
            Objects.requireNonNull(checksum);
            if (checksum.length < 1)
                throw new IllegalArgumentException("Checksum length must be greater than 0");
            this.checksum = checksum;
            return (this);
        }

        /**
         * Check the required builder fields
         * Since jackson doesn't check required field, we set every required field again just in case there is one missing
         */
        private void checkRequiredField() {
            this.setId(this.id);
            this.setDataLength(this.dataLength);
            this.checksum(this.checksum);
            this.setCreationDate(this.creationDate);
            this.setExpirationDate(this.expirationDate);
            this.setLastModificationDate(this.lastModificationDate);
        }

        /**
         * Disable public builder, use newInstance() to create a new instance
         */
        private Builder() {

        }

        /**
         * Create a new Builder instance from a prototype metadata
         * @param databaseFileMetadata The prototype
         */
        private Builder(final DatabaseFileMetadata databaseFileMetadata) {
            this.id = databaseFileMetadata.id;
            this.dataLength = databaseFileMetadata.dataLength;
            this.checksum = databaseFileMetadata.checksum;
            this.creationDate = databaseFileMetadata.creationDate;
            this.expirationDate = databaseFileMetadata.expirationDate;
            this.lastModificationDate = databaseFileMetadata.lastModificationDate;
        }
    }

    /**
     * Id step, used in the builder
     */
    public interface Id {
        public DataLength id(final UUID id);
    }

    /**
     * Data length step, used in the builder
     */
    public interface DataLength {
        public Checksum dataLength(final int dataLength);
    }

    /**
     * Checksum step, used in the builder
     */
    public interface Checksum {
        public CreationDate checksum(final byte[] checksum);
    }

    /**
     * Creation date step, used in the builder
     */
    public interface CreationDate {
        public ExpirationDate creationDate(final Timestamp creationDate);
    }

    /**
     * Expiration date step, used in the builder
     */
    public interface ExpirationDate {
        public LastModificationDate expirationDate(final Timestamp creationDate);
    }

    /**
     * Last modification date step, used in the builder
     */
    public interface LastModificationDate {
        public Build lastModificationDate(final Timestamp lastModificationDate);
    }

    /**
     * Build step, used in the builder
     */
    public interface Build {
        public DatabaseFileMetadata build();
    }

    /**
     * Get file Id
     * @return the file id
     */
    public UUID getId() {
        return (this.id);
    }

    /**
     * Get the file data length
     * @return the file data length
     */
    public int getDataLength() {
        return (this.dataLength);
    }

    /**
     * Get the file creation date
     * @return the file creation date
     */
    @JsonSerialize(using = TimestampDataBind.Serializer.class)
    public Timestamp getCreationDate() {
        return (this.creationDate);
    }

    /**
     * Get the file expiration date
     * @return the file expiration date
     */
    @JsonSerialize(using = TimestampDataBind.Serializer.class)
    public Timestamp getExpirationDate() {
        return (this.expirationDate);
    }

    /**
     * Get the file last modification date
     * @return the file last modification date
     */
    @JsonSerialize(using = TimestampDataBind.Serializer.class)
    public Timestamp getLastModificationDate() {
        return (this.lastModificationDate);
    }

    /**
     * Get the file data checksum
     * @return the file date checksum
     */
    public byte[] getChecksum() {
        return (this.checksum.clone());
    }

    /**
     * The maximum serialized length of a metadata instance
     * @return the maximum serialized length
     */
    public static int getMaxSerializedLength() {
        return (DatabaseFileMetadata.MAX_SERIALIZED_LENGTH);
    }

    /**
     * A string representation of the instance
     * @return
     */
    @Override
    public String toString() {
        return "DatabaseFileMetadata{" +
                "id=" + this.id +
                ", dataLength=" + this.dataLength +
                ", creationDate=" + StringUtils.replaceNewLine(this.creationDate.toString(), " ") +
                ", lastModificationDate=" + StringUtils.replaceNewLine(this.lastModificationDate.toString(), " ") +
                ", expirationDate=" + StringUtils.replaceNewLine(this.expirationDate.toString(), " ") +
                ", checksum=" + BytesUtils.toBase64String(this.checksum) +
                '}';
    }
}
