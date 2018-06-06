package fr.diguiet.grpc.fileserver.exception.databasefile;


import fr.diguiet.grpc.fileserver.DatabaseFileMetadata;

/**
 * Represent a database metadata serializer out of found exception
 * It can occur when the length of the serialized metadata object is too long
 */
public class SerializerOutOfBound extends MetadataException {
    public SerializerOutOfBound(final long size) {
        super("The serialized metadata size must be below " + DatabaseFileMetadata.getMaxSerializedLength() + " bytes, current size is " + size);
    }

    public SerializerOutOfBound(final String msg) {
        super(msg);
    }

    public SerializerOutOfBound(final Throwable cause) {
        super(cause);
    }

    public SerializerOutOfBound(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
