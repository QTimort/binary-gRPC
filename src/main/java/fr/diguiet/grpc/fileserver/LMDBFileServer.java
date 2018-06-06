package fr.diguiet.grpc.fileserver;

import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.fileserver.exception.FileServerException;
import fr.diguiet.grpc.fileserver.exception.AlreadyOpenException;
import fr.diguiet.grpc.fileserver.exception.ClosedException;
import fr.diguiet.grpc.common.utils.BytesUtils;
import fr.diguiet.grpc.common.utils.FileUtils;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import fr.diguiet.grpc.common.utils.UUIDUtils;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.lmdbjava.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Represent an implementation of file server using the LMDB embedded NoSql database
 * @see IFileServer
 * @see org.lmdbjava.Library.Lmdb
 */
public class LMDBFileServer implements IFileServer {
    private static final int MIN_READERS = LMDBFileServer.DB_COUNT;
    private static final long DB_MIN_BYTE_SIZE = 4096;
    private static final int DB_COUNT = 2;
    private static final Logger logger = LoggerFactory.getLogger(LMDBFileServer.class);
    private final Timer scheduler = new Timer();
    private final Settings settings;
    private boolean isOpen = false;
    private Env<ByteBuffer> env;
    private Dbi<ByteBuffer> dataDb;
    private Dbi<ByteBuffer> metaDb;

    /**
     * Create a new instance from a builder
     * @param builder
     */
    private LMDBFileServer(final LMDBFileServer.Builder builder) {
        this.settings = new Settings(builder);
        final long checkEveryMs = this.settings.expirationCheckSecondsDelay * 1000;
        this.scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (LMDBFileServer.this.isOpen) {
                    LMDBFileServer.logger.info("Cleaning expired entries...");
                    LMDBFileServer.this.removeExpired();
                }
            }
        }, checkEveryMs, checkEveryMs);
    }

    /**
     * Create a new Builder instance
     * @return
     */
    public static Builder newInstance() {
        return (LMDBFileServer.Builder.newInstance());
    }

    /**
     * File server settings class representation
     */
    @Immutable
    public static class Settings {
        private final String dataDbName;
        private final String metaDbName;
        private final File dbDirectory;
        private final long dbMaxByteSize;
        private final int maxNbReaders;
        private final int expirationCheckSecondsDelay;

        /**
         * Create a new Settings instance from the Builder
         * @param builder
         */
        private Settings(final Builder builder) {
            this.dataDbName = builder.dataDbName;
            this.metaDbName = builder.metaDbName;
            this.dbDirectory = new File(builder.parentDbDirectory, builder.dbDirectoryName);
            this.dbMaxByteSize = builder.dbMaxByteSize;
            this.maxNbReaders = builder.maxReaders;
            this.expirationCheckSecondsDelay =  builder.expirationCheckSecondsDelay;
        }

        /**
         * Get the data database name
         * @return the data database name
         */
        public String getDataDbName() {
            return (this.dataDbName);
        }

        /**
         * Get the metadata database name
         * @return the metadata database name
         */
        public String getMetaDbName() {
            return (this.metaDbName);
        }

        /**
         * Get the database directory
         * @return the database directory
         */
        public File getDbDirectory() {
            return (this.dbDirectory);
        }

        /**
         * Get the database maximum byte size
         * @return the database maximum byte size
         */
        public long getDbMaxByteSize() {
            return (this.dbMaxByteSize);
        }

        /**
         * Get the maximum number of readers
         * @return the maximum number of readers
         */
        public int getMaxNbReaders() {
            return (this.maxNbReaders);
        }

        /**
         * Get the LMDB env builder
         * @return the lMDB env builder
         * @see Env.Builder
         */
        public Env.Builder<ByteBuffer> getEnvBuilder() {
            return (Env.create()
                    .setMaxDbs(LMDBFileServer.DB_COUNT)
                    .setMapSize(this.dbMaxByteSize)
                    .setMaxReaders(this.maxNbReaders));
        }

        /**
         * Get the string representation of the settings
         * @return the string representation of the settings
         */
        @Override
        public String toString() {
            return "Settings{" +
                    "dataDbName='" + this.dataDbName + '\'' +
                    ", metaDbName='" + this.metaDbName + '\'' +
                    ", dbDirectory=" + this.dbDirectory +
                    ", dbMaxByteSize=" + this.dbMaxByteSize +
                    ", maxNbReaders=" + this.maxNbReaders +
                    ", expirationCheckSecondsDelay=" + this.expirationCheckSecondsDelay +
                    '}';
        }
    }

    /**
     * The file server builder class
     */
    public static class Builder {
        private String dataDbName = "data";
        private String metaDbName = "meta";
        private long dbMaxByteSize = 1024 * 1024 * 256;  // 256 mo
        private int maxReaders = 128;
        private File parentDbDirectory = FileUtils.getCurrentWorkingDirectory();
        private String dbDirectoryName = "db";
        private int expirationCheckSecondsDelay = 60; // 1 minute

        /**
         * Create a new instance of the builder
         * @return
         */
        public static Builder newInstance() {
            return (new Builder());
        }

        /**
         * Instantiate a new builder
         */
        private Builder() {

        }

        /**
         * Build a new file server
         * @return a new file server
         */
        public IFileServer build() {
            return (new LMDBFileServer(this));
        }

        /**
         * Set the maximum number of readers
         * @param maxReaders the maximum of readers
         * @return The builder
         * @throws IllegalArgumentException if the number of max readers is below {@value #MIN_READERS}
         */
        public Builder setMaxReaders(final int maxReaders) {
            if (maxReaders < LMDBFileServer.MIN_READERS)
                throw new IllegalArgumentException("Number of max readers must be greater than or equal to " + LMDBFileServer.MIN_READERS);
            this.maxReaders = maxReaders;
            return (this);
        }

        /**
         * Set the maximum database byte size
         * @param dbMaxSize the maximum database byte sizeof readers
         * @return The builder
         * @throws IllegalArgumentException if the maximum database size is below {@value #DB_MIN_BYTE_SIZE}
         */
        public Builder setDbMaxByteSize(final long dbMaxSize) {
            if (dbMaxSize < LMDBFileServer.DB_MIN_BYTE_SIZE)
                throw new IllegalArgumentException("The max database size me be atleast " + LMDBFileServer.DB_MIN_BYTE_SIZE);
            this.dbMaxByteSize = dbMaxSize;
            return (this);
        }

        /**
         * Set the parent database directory
         * @param parentDbDirectory the parent database directory
         * @return The builder
         * @throws IllegalArgumentException if the directory is invalid or not writable
         */
        public Builder setParentDbDirectory(final File parentDbDirectory) {
            Objects.requireNonNull(parentDbDirectory);
            if (!Files.isDirectory(parentDbDirectory.toPath()))
                throw new IllegalArgumentException("The given path must be a directory");
            if (!Files.isWritable(parentDbDirectory.toPath()))
                throw new IllegalArgumentException("The given path must exist and be writable");
            this.parentDbDirectory = parentDbDirectory;
            return (this);
        }

        /**
         * Set the database directory name
         * @param dbDirectoryName the database directory name
         * @return The builder
         * @throws IllegalArgumentException if the database directory name is empty
         */
        public Builder setDbDirectoryName(final String dbDirectoryName) {
            Objects.requireNonNull(dbDirectoryName);
            if (dbDirectoryName.isEmpty())
                throw new IllegalArgumentException("Db directory name must not be empty");
            this.dbDirectoryName = dbDirectoryName;
            return (this);
        }

        /**
         * Set the data database name
         * @param dataDbName the data database name
         * @return The builder
         * @throws IllegalArgumentException if name is empty or is identical to the metadata database name
         */
        public Builder setDataDbName(final String dataDbName) {
            Objects.requireNonNull(dataDbName);
            if (dataDbName.length() < 1)
                throw new IllegalArgumentException("The name of the database must not be empty!");
            if (dataDbName.equals(this.metaDbName))
                throw new IllegalArgumentException("Data and meta database cannot have he same name!");
            this.dataDbName = dataDbName;
            return (this);
        }

        /**
         * Set the metadata database name
         * @param metaDbName the data database name
         * @return The builder
         * @throws IllegalArgumentException if name is empty or is identical to the data database name
         */
        public Builder setMetaDbName(final String metaDbName) {
            Objects.requireNonNull(metaDbName);
            if (metaDbName.length() < 1)
                throw new IllegalArgumentException("The name of the database must not be empty!");
            if (metaDbName.equals(this.metaDbName))
                throw new IllegalArgumentException("Data and meta database cannot have he same name!");
            this.metaDbName = metaDbName;
            return (this);
        }

        /**
         * Set the number of seconds between each check to remove expired entries
         * @param expirationCheckSecondsDelay the number of seconds between each check
         * @return The builder
         * @throws IllegalArgumentException if number of seconds below 1
         */
        public Builder setExpirationCheckSecondsDelay(final int expirationCheckSecondsDelay) {
            if (expirationCheckSecondsDelay < 1)
                throw new IllegalArgumentException("The delay to check for expired file must be greater than 0");
            this.expirationCheckSecondsDelay = expirationCheckSecondsDelay;
            return (this);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void openOrCreate() throws FileServerException {
        this.mustBeCloseOrThrow("Tried to open already opened db");
        try {
            FileUtils.createFolderIfNotPresent(this.settings.getDbDirectory());
        } catch (IOException e) {
            LMDBFileServer.logger.error("Tried to open invalid path: " + e.getMessage());
            LMDBFileServer.logger.debug("path: " + this.settings.getDbDirectory());
            throw (new FileServerException(e));
        }
        this.env = this.settings.getEnvBuilder().open(this.settings.getDbDirectory(), EnvFlags.MDB_NOTLS);
        LMDBFileServer.logger.info("Opening database at " + this.settings.getDbDirectory().toString());
        this.metaDb = this.env.openDbi(this.settings.getMetaDbName(), DbiFlags.MDB_CREATE);
        this.dataDb = this.env.openDbi(this.settings.getDataDbName(), DbiFlags.MDB_CREATE);
        this.isOpen = true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isOpen() {
        return (this.isOpen);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void close() {
        if (this.isOpen) {
            LMDBFileServer.logger.info("Closing database at " + this.settings.getDbDirectory().toString());
            this.dataDb.close();
            this.metaDb.close();
            this.env.close();
            this.isOpen = false;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void upload(final UUID fileId, final Timestamp expirationDate, byte[] data) {
        Objects.requireNonNull(fileId);
        Objects.requireNonNull(expirationDate);
        Objects.requireNonNull(data);
        this.mustBeOpenOrThrow("Tried to upload data with closed connection! fileId:" + fileId);
        DatabaseFileMetadata databaseFileMetadata = this.getFileMetaData(fileId);
        if (databaseFileMetadata == null) {
            databaseFileMetadata = DatabaseFileMetadata.Builder.newBuilder()
                    .id(fileId)
                    .dataLength(data.length)
                    .checksum(BytesUtils.getCheckSum(data))
                    .creationDate(TimestampUtils.now())
                    .expirationDate(expirationDate)
                    .lastModificationDate(TimestampUtils.now())
                    .build();
        } else {
            byte existingData[] = this.download(fileId);
            if (existingData != null)
                data = BytesUtils.merge(existingData, data);
            databaseFileMetadata = DatabaseFileMetadata.Builder.fromPrototype(databaseFileMetadata)
                    .dataLength(data.length)
                    .checksum(BytesUtils.getCheckSum(data))
                    .creationDate(databaseFileMetadata.getCreationDate())
                    .expirationDate(expirationDate)
                    .lastModificationDate(TimestampUtils.now())
                    .build();
        }
        final ByteBuffer key = BytesUtils.allocateAndPutFlip(UUIDUtils.toBytes(fileId));
        final String jsonMetaData = databaseFileMetadata.toJsonString();
        final ByteBuffer metaData = BytesUtils.toByteBuffer(jsonMetaData);

        this.metaDb.put(key, metaData);
        this.dataDb.put(key, BytesUtils.allocateAndPutFlip(data));

        LMDBFileServer.logger.debug("Uploaded metadata: " + jsonMetaData);
        LMDBFileServer.logger.info("Uploaded file: " + fileId);
    }

    /**
     * @inheritDoc
     */
    @Override
    public @Nullable byte[] download(final UUID fileId) {
        Objects.requireNonNull(fileId);
        this.mustBeOpenOrThrow("Tried to download file: " + fileId + " with closed connection!");
        final ByteBuffer key = BytesUtils.allocateAndPutFlip(UUIDUtils.toBytes(fileId));

        if (this.isExpired(fileId)) {
            LMDBFileServer.logger.info("Tried to download expired file: " + fileId);
            return (null);
        }
        try (final Transaction dataTransac = Transaction.newInstance(this.env)) {
            final ByteBuffer data = this.dataDb.get(dataTransac.get(), key); // Temporary data, must be copied if we want to keep it
            if (data == null) {
                LMDBFileServer.logger.info("Tried to download non present file: " + fileId);
                return (null);
            }
            LMDBFileServer.logger.info("Downloaded file: " + fileId);
            final byte[] bytes = BytesUtils.toByteArray(data);
            LMDBFileServer.logger.debug("Data length: " + bytes.length);
            return (bytes);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public @Nullable DatabaseFileMetadata getFileMetaData(final UUID fileId) {
        Objects.requireNonNull(fileId);
        this.mustBeOpenOrThrow("Tried to get metadata of " + fileId + " with closed connection!");
        final ByteBuffer key = BytesUtils.allocateAndPutFlip(UUIDUtils.toBytes(fileId));
        if (this.isExpired(fileId)) {
            LMDBFileServer.logger.info("Tried to get expired metadata of file: " + fileId);
            return (null);
        }
        try (final Transaction metaTransac = Transaction.newInstance(this.env)) {
            final ByteBuffer metadata = this.metaDb.get(metaTransac.get(), key);
            if (metadata == null) {
                LMDBFileServer.logger.info("Unable to get metadata of file " + fileId);
                return (null);
            }
            final DatabaseFileMetadata databaseFileMetadata = this.getFileMetaDataFrom(metadata);
            if (databaseFileMetadata == null) {
                LMDBFileServer.logger.error("Unable to convert metadata of file" + fileId);
            }
            return (databaseFileMetadata);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public @Nullable DatabaseFile getFile(UUID fileId) {
        Objects.requireNonNull(fileId);
        this.mustBeOpenOrThrow("Tried to download file : " + fileId + " with closed connection!");
        final byte[] data = this.download(fileId);
        final DatabaseFileMetadata databaseFileMetadata = this.getFileMetaData(fileId);

        if (data == null || databaseFileMetadata == null) {
            if (data != null) {
                LMDBFileServer.logger.warn("File without metadata, removing file " + fileId);
                LMDBFileServer.logger.debug("file length: " + data.length);
                this.delete(fileId);
            } else if (databaseFileMetadata != null) {
                LMDBFileServer.logger.warn("Metadata without file, removing metdata " + fileId);
                LMDBFileServer.logger.debug("metadata: " + databaseFileMetadata);
                this.delete(fileId);
            }
            return (null);
        }
        if (this.isExpired(fileId, databaseFileMetadata, ByteBuffer.wrap(data))) {
            LMDBFileServer.logger.info("Tried to get expired file: " + fileId);
            return (null);
        }
        return DatabaseFile.newInstance(databaseFileMetadata, data);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void delete(final UUID fileId) {
        this.mustBeOpenOrThrow("Tried to delete file: " + fileId + " with closed connection!");
        final ByteBuffer key = BytesUtils.allocateAndPutFlip(UUIDUtils.toBytes(fileId));

        LMDBFileServer.logger.info("Removing entries with id: " + fileId);
        if (!this.dataDb.delete(key))
            LMDBFileServer.logger.debug("Unable to remove file data with id: " + fileId + " maybe it has already been deleted");
        if (!this.metaDb.delete(key))
            LMDBFileServer.logger.debug("Unable to remove metadata with id: " + fileId + " maybe it has already been deleted");
    }

    /**
     * @inheritDoc
     */
    @Override
    public void deleteAll() {
        try (final Transaction dataTransac = Transaction.newInstance(this.env, false)) {
            this.dataDb.drop(dataTransac.get());
        }
        try (final Transaction metaTransac = Transaction.newInstance(this.env, false)) {
            this.metaDb.drop(metaTransac.get());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void removeExpired() {
        try (final Transaction dataTransac = Transaction.newInstance(this.env)) {
            final Cursor<ByteBuffer> cursor = this.dataDb.openCursor(dataTransac.get()); // A cursor always belongs to a particular Dbi.
            if (cursor.seek(SeekOp.MDB_FIRST)) {
                try (final Transaction metaTransac = Transaction.newInstance(this.env)) {
                    boolean hasNext;
                    do {
                        final ByteBuffer key = cursor.key();
                        final ByteBuffer metadata = this.metaDb.get(metaTransac.get(), key);
                        final ByteBuffer data = this.dataDb.get(dataTransac.get(), key);
                        final UUID fileId = UUIDUtils.fromBytes(BytesUtils.toByteArray(key));
                        hasNext = cursor.seek(SeekOp.MDB_NEXT);
                        if (this.isExpired(fileId, metadata, data)) {
                            LMDBFileServer.logger.info("File " + fileId + " has expired");
                            LMDBFileServer.logger.debug("Metadata: " + metadata);
                            LMDBFileServer.logger.debug("Now: " + TimestampUtils.now());
                            this.delete(fileId);
                        }
                    } while (hasNext);
                }
            }
        }
    }

    /**
     * Tell whether or not the specified id is expired.
     * Transactions must be open before
     * @inheritDoc
     * @return False If the specified id is present and has a valid metadata date and data that isn't expired yet in any other case it will return True
     */
    public boolean isExpired(final UUID fileId) {
        this.mustBeOpenOrThrow("Tried to check if id: " + fileId + " expired with closed connection!");
        final ByteBuffer key = BytesUtils.allocateAndPutFlip(UUIDUtils.toBytes(fileId));
        final ByteBuffer metadata;
        final ByteBuffer data;

        try (final Transaction dataTransac = Transaction.newInstance(this.env);
             final Transaction metaTransac = Transaction.newInstance(this.env)) {
            metadata = this.metaDb.get(metaTransac.get(), key);
            data = this.dataDb.get(dataTransac.get(), key);
        }
        return (this.isExpired(fileId, metadata, data));
    }

    /**
     * @inheritDoc
     */
    @Override
    public long getAvailableUsableByte() {
        final long metaUsedSpace = this.getMetaDataUsedSpace();
        final long dataUsedSpace = this.getDataUsedSpace();
        final long mapSize = this.env.info().mapSize;
        final int pageSize = this.env.stat().pageSize;
        final long metaReservedPages = (long)Math.ceil((double) metaUsedSpace / pageSize);
        final long dataReservedPages = (long)Math.ceil((double) dataUsedSpace / pageSize);

        return (mapSize - pageSize * (metaReservedPages + dataReservedPages));
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean hasEnoughSpaceFor(final int nbByte) {
        final int pageSize = this.env.stat().pageSize;
        final long neededSpace = ((long)Math.ceil((double) (nbByte + DatabaseFileMetadata.getMaxSerializedLength()) / pageSize)) * pageSize;
        return (this.getAvailableUsableByte() > neededSpace);
    }

    /**
     * Just in case the file server wasn't closed properly
     * @inheritDoc
     */
    @Override
    protected void finalize() throws Throwable {
        if (this.isOpen) {
            LMDBFileServer.logger.warn("Database " + this.settings.getDbDirectory().toString() + " has not been closed properly");
            this.close();
        }
        super.finalize();
    }

    /**
     * Get the used byte space
     * @param stat the LMDB stat
     * @see Stat
     * @return the used space in byte
     */
    private long getUsedSpace(final Stat stat) {
        Objects.requireNonNull(stat);
        return (stat.pageSize * (stat.leafPages + stat.branchPages + stat.overflowPages));
    }

    /**
     * Get the used byte space by the metadata database
     * @return the used metadata space in byte
     */
    public long getMetaDataUsedSpace() {
        this.mustBeOpenOrThrow("Tried to get used space with closed connection!");
        try (final Transaction metaTransac = Transaction.newInstance(this.env)) {
            return (this.getUsedSpace(this.metaDb.stat(metaTransac.get())));
        }
    }

    /**
     * Get the used byte space by the data database
     * @return the used data space in byte
     */
    public long getDataUsedSpace() {
        this.mustBeOpenOrThrow("Tried to get used space with closed connection!");
        try (final Transaction dataTransac = Transaction.newInstance(this.env)) {
            return (this.getUsedSpace(this.dataDb.stat(dataTransac.get())));
        }
    }

    /**
     * Predicate that tell whether or not a file is expired
     * @param fileId the file id
     * @param metadata the metadata associated to the file
     * @param data the data associated to the file
     * @return if the file is expired
     */
    private boolean isExpired(final UUID fileId, final ByteBuffer metadata, final ByteBuffer data) {
        if (metadata != null) {
            return (this.isExpired(fileId, this.getFileMetaDataFrom(metadata), data));
        } else {
            return (this.isExpired(fileId, (DatabaseFileMetadata) null, data));
        }
    }

    /**
     * Predicate that tell whether or not a file is expired
     * @param fileId the file id
     * @param metadata the metadata associated to the file
     * @param data the data associated to the file
     * @return if the file is expired
     */
    private boolean isExpired(final UUID fileId, final DatabaseFileMetadata metadata, final ByteBuffer data) {
        if (metadata == null || data == null) {
            LMDBFileServer.logger.debug("Id: " + fileId + " is considered expired because metadata or data is null");
            LMDBFileServer.logger.debug("Data: " + data);
            LMDBFileServer.logger.debug("Metadata: " + metadata);
            return (true);
        }
        final Timestamp timeStampNow = TimestampUtils.now();
        return (TimestampUtils.isAfter(metadata.getExpirationDate(), timeStampNow));
    }

    /**
     * Create a new database file metadata from its byte representation
     * @param metadata the metadata to convert
     * @return the file metadata instance
     */
    private DatabaseFileMetadata getFileMetaDataFrom(final ByteBuffer metadata) {
        Objects.requireNonNull(metadata);
        final String jsonMetadata = BytesUtils.toString(metadata);
        final DatabaseFileMetadata.Builder builder = DatabaseFileMetadata.Builder.fromJson(jsonMetadata);
        if (builder == null) {
            LMDBFileServer.logger.error("Unable to convert json metadata to builder!");
            LMDBFileServer.logger.debug("Json: " + jsonMetadata);
        } else {
            return (builder.build());
        }
        return (null);
    }

    /**
     * Throw an exception if the file server is closed
     * @param errorLogMessage the exception error message
     * @throws ClosedException
     */
    private void mustBeOpenOrThrow(@Nullable final String errorLogMessage) {
        if (!this.isOpen) {
            if (errorLogMessage != null)
                LMDBFileServer.logger.error(errorLogMessage);
            throw new ClosedException();
        }
    }

    /**
     * Throw an exception if the file server is open
     * @param errorLogMessage the exception error message
     * @throws AlreadyOpenException
     */
    private void mustBeCloseOrThrow(@Nullable final String errorLogMessage) {
        if (this.isOpen) {
            if (errorLogMessage != null)
                LMDBFileServer.logger.error(errorLogMessage);
            throw new AlreadyOpenException();
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
     * Throw an exception if the file server is open
     * @throws AlreadyOpenException
     */
    private void mustBeCloseOrThrow() {
        this.mustBeCloseOrThrow(null);
    }

    /**
     * String representation of the instance
     */
    @Override
    public String toString() {
        return "LMDBFileServer{" +
                "settings=" + this.settings +
                ", isOpen=" + this.isOpen +
                ", env=" + this.env +
                ", dataDb=" + this.dataDb +
                ", metaDb=" + this.metaDb +
                '}';
    }
}
