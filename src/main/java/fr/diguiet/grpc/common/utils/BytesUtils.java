package fr.diguiet.grpc.common.utils;

import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Utils class with static method to simplify the use of bytes related function and object
 * @see ByteBuffer
 */
public final class BytesUtils {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    private static final String DEFAULT_CHECKSUM_ALGO = "SHA-1";

    /**
     * Class is not instantiable and inheritable
     */
    private BytesUtils() {

    }

    /**
     * Allocate a direct ByteBuffer and copy the bytes into it
     * @param bytes The bytes to copy
     * @return a new ByteBuffer containing the specified bytes
     */
    public static ByteBuffer allocateAndPut(final byte[] bytes) {
        Objects.requireNonNull(bytes);
        return (ByteBuffer.allocateDirect(bytes.length).put(bytes));
    }

    /**
     * Allocate a direct ByteBuffer, copy the bytes into it and flip it
     * @param bytes The bytes to copy
     * @return a new ByteBuffer containing the specified bytes
     */
    public static ByteBuffer allocateAndPutFlip(final byte[] bytes) {
        Objects.requireNonNull(bytes);
        ByteBuffer bb = BytesUtils.allocateAndPut(bytes);
        bb.flip();
        return (bb);
    }

    /**
     * Allocate a direct ByteBuffer with random bytes
     * @param nbBytes The number of random bytes to allocate
     * @return a new ByteBuffer containing random bytes
     * @throws IllegalArgumentException if the number of bytes is incorrect
     */
    public static ByteBuffer getRandom(final int nbBytes) {
        if (nbBytes < 1)
            throw new IllegalArgumentException("Number of bytes must be greater than or equal to 0");
        final byte random[] = new byte[nbBytes];
        new Random().nextBytes(random);
        return (BytesUtils.allocateAndPutFlip(random));
    }

    /**
     * Copy the whole buffer from position 0 to remaining into a new byte array
     * @param bb the ByteBuffer to copy
     * @return a new byte array containing the ByteBuffer payload
     */
    public static byte[] toByteArray(final ByteBuffer bb) {
        Objects.requireNonNull(bb);
        final int prevPos = bb.position();
        bb.position(0);
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        bb.position(prevPos);
        return (bytes);
    }

    /**
     * Convert a string encoded payload using the default charset into a ByteBuffer
     * @see BytesUtils#DEFAULT_CHARSET
     * @param str The encoded string to convert
     * @return A new direct ByteBuffer containing the decoded string payload
     */
    public static ByteBuffer toByteBuffer(final String str) {
        Objects.requireNonNull(str);
        return (BytesUtils.toByteBuffer(str, BytesUtils.DEFAULT_CHARSET));
    }

    /**
     * Convert a ByteBuffer into an encoded string using the default charset
     * It convert the whole ByteBuffer from position 0 to remaining
     * @see BytesUtils#DEFAULT_CHARSET
     * @param bb The ByteBuffer to encode
     * @return An encoded string containing the ByteBuffer payload
     */
    public static String toString(final ByteBuffer bb) {
        Objects.requireNonNull(bb);
        return (BytesUtils.toString(bb, BytesUtils.DEFAULT_CHARSET));
    }

    /**
     * Convert a string encoded payload using a charset into a ByteBuffer
     * @param str The encoded string to convert
     * @param charset The charset to use to decode the string
     * @return A new direct ByteBuffer containing the decoded string payload
     */
    private static ByteBuffer toByteBuffer(final String str, final Charset charset) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(charset);
        return (BytesUtils.allocateAndPutFlip(charset.encode(str).array()));
    }

    /**
     * Convert a ByteBuffer into an encoded string using a charset
     * It convert the whole ByteBuffer from position 0 to remaining
     * @param bb The ByteBuffer to encode
     * @param charset The Charset to use to encode the string
     * @return An encoded string containing the ByteBuffer payload
     */
    public static String toString(final ByteBuffer bb, final Charset charset) {
        Objects.requireNonNull(bb);
        Objects.requireNonNull(charset);
        final int prevPosition = bb.position();
        bb.position(0);
        final String s = charset.decode(bb).toString();
        bb.position(prevPosition);
        return (s);
    }

    /**
     * Compute and return the checksum of a ByteBuffer using the {@value #DEFAULT_CHECKSUM_ALGO} algorithm
     * The checksum is made for the whole ByteBuffer payload from position 0 to remaining
     * @param data The ByteBuffer to compute
     * @return The checksum of the ByteBuffer payload
     */
    public static byte[] getCheckSum(final ByteBuffer data) {
        Objects.requireNonNull(data);
        final byte[] bytes = BytesUtils.toByteArray(data);
        return (BytesUtils.getCheckSum(bytes));
    }

    /**
     * Compute and return the checksum of a byte array using the {@value #DEFAULT_CHECKSUM_ALGO} algorithm
     * @param data The byte array to compute
     * @return The checksum of the byte array Or Null if unable to get the checksum algorithm
     */
    public static byte[] getCheckSum(final byte[] data) {
        Objects.requireNonNull(data);
        final MessageDigest md;

        try {
            md = MessageDigest.getInstance(BytesUtils.DEFAULT_CHECKSUM_ALGO);
            return (md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            final Logger logger = Logger.getLogger(BytesUtils.class);
            logger.warn("Unable to get " + BytesUtils.DEFAULT_CHECKSUM_ALGO + " checksum algorithm! Exception:" + e.getMessage());
        }
        return (null);
    }

    /**
     * Merge two byte array into a new byte array
     * @param begin The byte array that will copied at the begin
     * @param end The byte array that will be copied at the end
     * @return A new byte array containing the merged byte array
     */
    public static byte[] merge(final byte[] begin, final byte[] end) {
        Objects.requireNonNull(begin);
        Objects.requireNonNull(end);
        final byte[] result = new byte[begin.length + end.length];
        System.arraycopy(begin, 0, result, 0, begin.length);
        System.arraycopy(end, 0, result, begin.length, end.length);
        return (result);
    }

    /**
     * Split a byte array into nbSplit byte array
     * All the chunk will have the same length, except for the last one if the number of split
     * isn't a multiple of the byte array length
     * @param bytes The byte array to split
     * @param nbSplit The number of split to be made
     * @return A new two dimensional byte array
     * @throws IllegalArgumentException if the number of split is invalid
     */
    public static byte[][] split(final byte[] bytes, final int nbSplit) {
        Objects.requireNonNull(bytes);
        if (nbSplit < 1)
            throw new IllegalArgumentException("The number of split must be greater than 0!");
        final int length = bytes.length;
        final float bytesPerChunkRatio = (float) length / nbSplit;
        if (bytesPerChunkRatio < 1.0f) {
            throw new IllegalArgumentException("Cannot split a byte, either your data is too small or your number of split too big!");
        }
        final int bytesPerChunk = (int) Math.floor(bytesPerChunkRatio);
        final byte[] copy = bytes.clone();
        final byte[][] chunks = new byte[nbSplit][];
        for (int i = 0; i < nbSplit; ++i) {
            final int beginPos = Math.toIntExact(bytesPerChunk * i);
            final byte[] chunkBytes;
            if (i == (nbSplit - 1)) {
                // copy to the end of the array in case dataLength divided by nbSplit isn't an integer
                chunkBytes = Arrays.copyOfRange(copy, beginPos, copy.length);
            } else {
                chunkBytes = Arrays.copyOfRange(copy, beginPos, beginPos + bytesPerChunk);
            }
            chunks[i] = chunkBytes;
        }
        return (chunks);
    }

    /**
     * Convert a byte array into a base 64 encoded string
     * @param bytes The byte array to encode
     * @return A base 64 encoded string
     */
    public static String toBase64String(final byte[] bytes) {
        Objects.requireNonNull(bytes);
        return (DatatypeConverter.printBase64Binary(bytes));
    }
}
