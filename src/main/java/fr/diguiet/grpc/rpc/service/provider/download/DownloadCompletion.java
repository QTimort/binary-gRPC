package fr.diguiet.grpc.rpc.service.provider.download;

import fr.diguiet.grpc.common.Interval;
import fr.diguiet.grpc.common.Intervals;

import java.util.List;

public class DownloadCompletion {
    private final Intervals downloadPart = Intervals.newInstance();
    private final int totalLength;

    public static DownloadCompletion newInstance(final int totalLength) {
        return (new DownloadCompletion(totalLength));
    }

    private DownloadCompletion(final int totalLength) {
        this.totalLength = totalLength;
    }

    public boolean addChunk(final int offset, final int length) {
        this.downloadPart.add(Interval.valueOf(offset, offset + length));
        return (this.isCompleted());
    }

    public boolean isCompleted() {
        final List<Interval> intervals = this.downloadPart.getIntervals();
        if (intervals.size() == 1) {
            final Interval interval = intervals.get(0);
            return (interval.getBegin() == 0 && interval.getEnd() == totalLength);
        }
        return (false);
    }
}
