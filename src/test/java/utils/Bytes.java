package utils;

import fr.diguiet.grpc.common.utils.BytesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Bytes {
    private static final byte EMPTY[] = new byte[0];
    private static final byte SOME_BYTES[] = {1, 3, 4, 6, 6, 42, 13, 37, 101, 7};
    private static final int RANDOM_LENGTH = 1024;
    private static final byte RANDOM[] = BytesUtils.toByteArray(BytesUtils.getRandom(Bytes.RANDOM_LENGTH));
    private static final int SHA_ONE_LENGTH = 20;
    private static final String NEO = "You take the blue pill - the story ends, you wake up in your bed and" +
            "believe whatever you want to believe. You take the red pill - you stay in Wonderland and" +
            "I show you how deep the rabbit-hole goes.";

    private void testAllocationEquality(final byte[] bytes) {
        Assertions.assertTrue(Arrays.equals(bytes, BytesUtils.toByteArray(BytesUtils.allocateAndPut(bytes))));
        Assertions.assertTrue(Arrays.equals(
                BytesUtils.toByteArray(BytesUtils.allocateAndPut(bytes)),
                BytesUtils.toByteArray(BytesUtils.allocateAndPutFlip(bytes))));
        Assertions.assertTrue(Arrays.equals(bytes, BytesUtils.toByteArray(BytesUtils.allocateAndPutFlip(bytes))));
    }

    private void testAllocationDirectness(final byte[] bytes) {
        Assertions.assertTrue(BytesUtils.allocateAndPut(bytes).isDirect());
        Assertions.assertTrue(BytesUtils.allocateAndPutFlip(bytes).isDirect());
    }

    private void testAllocationHasArray(final byte[] bytes) {
        Assertions.assertFalse(BytesUtils.allocateAndPut(bytes).hasArray());
        Assertions.assertFalse(BytesUtils.allocateAndPutFlip(bytes).hasArray());
    }

    @Test
    public void allocation() {
        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.allocateAndPutFlip(null));
        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.allocateAndPut(null));
        testAllocationEquality(Bytes.EMPTY);
        testAllocationDirectness(Bytes.EMPTY);
        testAllocationHasArray(Bytes.EMPTY);
        testAllocationEquality(Bytes.EMPTY);
        testAllocationDirectness(Bytes.EMPTY);
        testAllocationHasArray(Bytes.EMPTY);
    }

    @Test
    void random() {
        final int length = 1024;
        final ByteBuffer random = BytesUtils.getRandom(length);
        final ByteBuffer random1 = BytesUtils.getRandom(length);

        Assertions.assertEquals(length, random.limit());
        Assertions.assertEquals(length, random1.limit());

        Assertions.assertNotEquals(random, random1);
        Assertions.assertFalse(Arrays.equals(BytesUtils.toByteArray(random), BytesUtils.toByteArray(random1)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> BytesUtils.getRandom(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BytesUtils.getRandom(0));
        BytesUtils.getRandom(1);
    }

    @Test
    void toBytesArray() {
        final ByteBuffer empty = ByteBuffer.wrap(Bytes.EMPTY);
        final ByteBuffer some = ByteBuffer.wrap(Bytes.SOME_BYTES);
        final ByteBuffer random = ByteBuffer.wrap(Bytes.RANDOM);
        final ByteBuffer randomDirect = BytesUtils.getRandom(Bytes.RANDOM_LENGTH);
        final byte[] emptyBytes = BytesUtils.toByteArray(empty);
        final byte[] someBytes = BytesUtils.toByteArray(some);
        final byte[] randomBytes = BytesUtils.toByteArray(random);
        final byte[] randomDirectBytes = BytesUtils.toByteArray(randomDirect);
        Assertions.assertEquals(Bytes.EMPTY.length, emptyBytes.length);
        Assertions.assertEquals(Bytes.SOME_BYTES.length, someBytes.length);
        Assertions.assertEquals(Bytes.RANDOM.length, randomBytes.length);
        Assertions.assertEquals(Bytes.RANDOM_LENGTH, randomDirectBytes.length);
        Assertions.assertTrue(Arrays.equals(Bytes.EMPTY, emptyBytes));
        Assertions.assertTrue(Arrays.equals(Bytes.SOME_BYTES, someBytes));
        Assertions.assertTrue(Arrays.equals(Bytes.RANDOM, randomBytes));
        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.toByteArray(null));
        BytesUtils.toByteArray(ByteBuffer.wrap(new byte[0]));
    }

    @Test
    void toByteBuffer() {
        final ByteBuffer empty = ByteBuffer.wrap(Bytes.EMPTY);
        final ByteBuffer some = ByteBuffer.wrap(Bytes.SOME_BYTES);
        final ByteBuffer random = ByteBuffer.wrap(Bytes.RANDOM);
        final ByteBuffer randomDirect = BytesUtils.getRandom(Bytes.RANDOM_LENGTH);

        final String emptyStr = BytesUtils.toString(empty);
        final String someStr = BytesUtils.toString(some);
        final String randomStr = BytesUtils.toString(random);
        final String randomDirectStr = BytesUtils.toString(randomDirect);

        final ByteBuffer emptyBb = BytesUtils.toByteBuffer(emptyStr);
        final ByteBuffer someBb = BytesUtils.toByteBuffer(someStr);
        final ByteBuffer randomBb = BytesUtils.toByteBuffer(randomStr);
        final ByteBuffer randomDirectBb = BytesUtils.toByteBuffer(randomDirectStr);

        Assertions.assertTrue(emptyBb.isDirect());
        Assertions.assertTrue(someBb.isDirect());
        Assertions.assertTrue(randomBb.isDirect());
        Assertions.assertTrue(randomDirectBb.isDirect());

        Assertions.assertEquals(empty, emptyBb);
        Assertions.assertEquals(some, someBb);
        Assertions.assertEquals(random, randomBb);
        Assertions.assertEquals(randomDirect, randomDirectBb);

        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.toByteBuffer(null));
        BytesUtils.toByteBuffer("");
    }

    @Test
    public void byteToString() {
        final ByteBuffer empty = ByteBuffer.wrap(Bytes.EMPTY);
        final ByteBuffer some = ByteBuffer.wrap(Bytes.SOME_BYTES);
        final ByteBuffer random = ByteBuffer.wrap(Bytes.RANDOM);
        final ByteBuffer randomDirect = BytesUtils.getRandom(Bytes.RANDOM_LENGTH);

        final String emptyStr = BytesUtils.toString(empty);
        final String someStr = BytesUtils.toString(some);
        final String randomStr = BytesUtils.toString(random);
        final String randomDirectStr = BytesUtils.toString(randomDirect);

        Assertions.assertNotNull(emptyStr);
        Assertions.assertNotNull(someStr);
        Assertions.assertNotNull(randomDirect);
        Assertions.assertNotNull(randomDirectStr);

        final ByteBuffer emptyBb = BytesUtils.toByteBuffer(emptyStr);
        final ByteBuffer someBb = BytesUtils.toByteBuffer(someStr);
        final ByteBuffer randomBb = BytesUtils.toByteBuffer(randomStr);
        final ByteBuffer randomDirectBb = BytesUtils.toByteBuffer(randomDirectStr);

        Assertions.assertTrue(emptyBb.isDirect());
        Assertions.assertTrue(someBb.isDirect());
        Assertions.assertTrue(randomBb.isDirect());
        Assertions.assertTrue(randomDirectBb.isDirect());

        Assertions.assertEquals(empty, emptyBb);
        Assertions.assertEquals(some, someBb);
        Assertions.assertEquals(random, randomBb);

        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.toString(null));
    }

    @Test
    public void checksum() {
        final ByteBuffer empty = ByteBuffer.wrap(Bytes.EMPTY);
        final ByteBuffer some = ByteBuffer.wrap(Bytes.SOME_BYTES);
        final ByteBuffer random = ByteBuffer.wrap(Bytes.RANDOM);
        final ByteBuffer randomDirect = BytesUtils.getRandom(Bytes.RANDOM_LENGTH);
        final String expectedBase64Checksum = "mGkWHO9m3BnGXDrbVPYOPAYfzFg=";

        Assertions.assertTrue(Arrays.equals(BytesUtils.getCheckSum(Bytes.EMPTY), BytesUtils.getCheckSum(empty)));
        Assertions.assertTrue(Arrays.equals(BytesUtils.getCheckSum(Bytes.SOME_BYTES), BytesUtils.getCheckSum(some)));
        Assertions.assertTrue(Arrays.equals(BytesUtils.getCheckSum(Bytes.RANDOM), BytesUtils.getCheckSum(random)));
        Assertions.assertNotNull(BytesUtils.getCheckSum(randomDirect));
        final byte[] checkSum = BytesUtils.getCheckSum(NEO.getBytes());
        Assertions.assertNotNull(checkSum);
        Assertions.assertEquals(checkSum.length, Bytes.SHA_ONE_LENGTH, "Not using sha-1 ?");
        Assertions.assertEquals(expectedBase64Checksum, BytesUtils.toBase64String(checkSum));

        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.getCheckSum((byte[]) null));
        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.getCheckSum((ByteBuffer) null));
    }

    @Test
    public void merge() {
        final byte[] empty = BytesUtils.merge(Bytes.EMPTY, Bytes.EMPTY);
        final byte[] someEmpty = BytesUtils.merge(Bytes.SOME_BYTES, Bytes.EMPTY);
        final byte[] someRandom = BytesUtils.merge(Bytes.SOME_BYTES, Bytes.RANDOM);
        final byte[] emptySome = BytesUtils.merge(Bytes.EMPTY, Bytes.SOME_BYTES);
        final byte[] randomSome = BytesUtils.merge(Bytes.RANDOM, Bytes.SOME_BYTES);

        Assertions.assertTrue(Arrays.equals(Bytes.EMPTY, empty));
        Assertions.assertEquals(Bytes.SOME_BYTES.length + Bytes.EMPTY.length, someEmpty.length);
        Assertions.assertEquals(Bytes.SOME_BYTES.length + Bytes.RANDOM.length, someRandom.length);
        Assertions.assertEquals(Bytes.EMPTY.length + Bytes.SOME_BYTES.length, emptySome.length);
        Assertions.assertEquals(Bytes.RANDOM.length + Bytes.SOME_BYTES.length, randomSome.length);
        Assertions.assertTrue(Arrays.equals(someEmpty, emptySome));
        Assertions.assertFalse(Arrays.equals(randomSome, someRandom));

        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.merge(Bytes.SOME_BYTES, null));
        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.merge(null, Bytes.SOME_BYTES));
        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.merge(null, null));
    }

    @Test
    public void split() {
        final int nbSplit = 42;

        final byte[][] split = BytesUtils.split(Bytes.RANDOM, nbSplit);
        Assertions.assertEquals(nbSplit, split.length);
        byte[] original = new byte[0];
        for (int i = 0 ; i < nbSplit; ++i) {
            original = BytesUtils.merge(original, split[i]);
        }
        Assertions.assertTrue(Arrays.equals(Bytes.RANDOM, original));

        BytesUtils.split(Bytes.SOME_BYTES, Bytes.SOME_BYTES.length);
        final byte[][] noSplit = BytesUtils.split(Bytes.SOME_BYTES, 1);
        Assertions.assertEquals(1, noSplit.length);
        Assertions.assertTrue(Arrays.equals(Bytes.SOME_BYTES, noSplit[0]));

        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.split(null, 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BytesUtils.split(Bytes.SOME_BYTES, -1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BytesUtils.split(Bytes.SOME_BYTES, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> BytesUtils.split(Bytes.SOME_BYTES, Bytes.SOME_BYTES.length + 1));
    }

    @Test
    public void base64() {
        final String base64 = BytesUtils.toBase64String(NEO.getBytes());
        final String expectedBase64 = "WW91IHRha2UgdGhlIGJsdWUgcGlsbCAtIHRoZSBzdG9yeSBlbmRzLCB5b3Ugd2FrZSB1cCBpbiB5b3VyIGJlZCBhbmRiZWxpZXZlIHdoYXRldmVyIHlvdSB3YW50IHRvIGJlbGlldmUuIFlvdSB0YWtlIHRoZSByZWQgcGlsbCAtIHlvdSBzdGF5IGluIFdvbmRlcmxhbmQgYW5kSSBzaG93IHlvdSBob3cgZGVlcCB0aGUgcmFiYml0LWhvbGUgZ29lcy4=";
        Assertions.assertEquals(expectedBase64, base64);

        Assertions.assertThrows(NullPointerException.class, () -> BytesUtils.toBase64String( null));
    }
}
