package utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import fr.diguiet.grpc.common.utils.TimestampUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class Timestamps {
    @Test
    public void instant() {
        final Instant instantNow = Instant.now();
        final Timestamp convertedNow = TimestampUtils.toTimestamp(instantNow);

        Assertions.assertEquals(instantNow.getEpochSecond(), convertedNow.getSeconds());
        Assertions.assertEquals(instantNow.getNano(), convertedNow.getNanos());
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.toTimestamp((Instant) null));
    }

    @Test
    public void bytes() throws InvalidProtocolBufferException {
        final Timestamp now = TimestampUtils.now();
        final byte[] bytes = now.toByteArray();
        final Timestamp fromBytes = TimestampUtils.toTimestamp(bytes);
        Assertions.assertEquals(now, fromBytes);
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.toTimestamp((byte[]) null));
    }

    @Test
    public void now() {
        final Timestamp now = TimestampUtils.now();
        final Timestamp timestamp = TimestampUtils.nowPlusSeconds(1);
        Assertions.assertTrue(timestamp.getSeconds() > now.getSeconds());
        final long secondsDifference = TimestampUtils.getSecondsDifference(timestamp, now);
        Assertions.assertTrue(secondsDifference >= 1 && secondsDifference <= 2); // 1 second tolerance
    }

    @Test
    public void isAfter() throws InterruptedException {
        final Timestamp now = TimestampUtils.now();
        Thread.sleep(1);
        final Timestamp now2 = TimestampUtils.now();
        final Timestamp nowPlusSeconds = TimestampUtils.nowPlusSeconds(1);

        Assertions.assertTrue(TimestampUtils.isAfter(now, now2));
        Assertions.assertTrue(TimestampUtils.isAfter(now, nowPlusSeconds));
        Assertions.assertTrue(TimestampUtils.isAfter(now2, nowPlusSeconds));

        Assertions.assertFalse(TimestampUtils.isAfter(nowPlusSeconds, now));
        Assertions.assertFalse(TimestampUtils.isAfter(nowPlusSeconds, now2));
        Assertions.assertFalse(TimestampUtils.isAfter(nowPlusSeconds, nowPlusSeconds));

        Assertions.assertTrue(TimestampUtils.isAfterOrEqual(now, now2));
        Assertions.assertTrue(TimestampUtils.isAfterOrEqual(now, nowPlusSeconds));
        Assertions.assertTrue(TimestampUtils.isAfterOrEqual(now2, nowPlusSeconds));

        Assertions.assertFalse(TimestampUtils.isAfterOrEqual(nowPlusSeconds, now));
        Assertions.assertFalse(TimestampUtils.isAfterOrEqual(nowPlusSeconds, now2));
        Assertions.assertTrue(TimestampUtils.isAfterOrEqual(nowPlusSeconds, nowPlusSeconds));

        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.isAfter(now, null));
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.isAfter(null, now));
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.isAfter(null, null));

        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.isAfterOrEqual(now, null));
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.isAfterOrEqual(null, now));
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.isAfterOrEqual(null, null));
    }

    @Test
    public void difference() {
        final int plusSeconds = 2;
        final Timestamp now = TimestampUtils.now();
        final Timestamp now2 = TimestampUtils.now();
        final Timestamp nowPlusSeconds = TimestampUtils.nowPlusSeconds(plusSeconds);

        final long secondsDifference = TimestampUtils.getSecondsDifference(now2, now);
        final long secondsDifferenceFromNow = TimestampUtils.getSecondsDifferenceFromNow(now2);
        Assertions.assertTrue(secondsDifference >= 0 && secondsDifference <= 1); // 1 second tolerance
        Assertions.assertTrue(secondsDifferenceFromNow >= 0 && secondsDifferenceFromNow <= 1); // 1 second tolerance
        Assertions.assertEquals(plusSeconds, TimestampUtils.getSecondsDifference(nowPlusSeconds, now));
        Assertions.assertEquals(- plusSeconds, TimestampUtils.getSecondsDifference(now, nowPlusSeconds));

        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.getSecondsDifference(now, null));
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.getSecondsDifference(null, now));
        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.getSecondsDifference(null, null));

        Assertions.assertThrows(NullPointerException.class, () -> TimestampUtils.getSecondsDifferenceFromNow(null));
    }

    @Test void timestampComparison() {
        final Timestamp timeStampNow = TimestampUtils.now();
        final Timestamp timeStampAfter = TimestampUtils.nowPlusSeconds(60);
        final Timestamp timeStampBefore = TimestampUtils.nowPlusSeconds(-60);
        final Timestamp timestampNow1nano = Timestamp.newBuilder(timeStampNow).setNanos(timeStampNow.getNanos() + 1).build();
        Assertions.assertFalse(TimestampUtils.isAfter(timeStampNow, timeStampBefore));
        Assertions.assertFalse(TimestampUtils.isAfter(timeStampNow, timeStampNow));
        Assertions.assertTrue(TimestampUtils.isAfter(timeStampNow, timeStampAfter));
        Assertions.assertFalse(TimestampUtils.isAfter(timeStampAfter, timeStampBefore));
        Assertions.assertTrue(TimestampUtils.isAfter(timeStampBefore, timeStampNow));
        Assertions.assertFalse(TimestampUtils.isAfter(timeStampAfter, timeStampNow));

        Assertions.assertFalse(TimestampUtils.isAfter(timestampNow1nano, timeStampBefore));
        Assertions.assertFalse(TimestampUtils.isAfter(timestampNow1nano, timestampNow1nano));
        Assertions.assertTrue(TimestampUtils.isAfter(timestampNow1nano, timeStampAfter));
        Assertions.assertFalse(TimestampUtils.isAfter(timeStampAfter, timeStampBefore));
        Assertions.assertTrue(TimestampUtils.isAfter(timeStampBefore, timestampNow1nano));
        Assertions.assertFalse(TimestampUtils.isAfter(timeStampAfter, timestampNow1nano));

        Assertions.assertFalse(TimestampUtils.isAfter(timestampNow1nano, timeStampNow));
        Assertions.assertTrue(TimestampUtils.isAfter(timeStampNow, timestampNow1nano));
    }
}
