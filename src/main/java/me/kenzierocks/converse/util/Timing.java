package me.kenzierocks.converse.util;

public class Timing {

    public static Timing now() {
        return at(System.currentTimeMillis());
    }

    public static Timing at(long time) {
        return new Timing(time);
    }

    private final long time;

    private Timing(long time) {
        this.time = time;
    }

    public long getStartTime() {
        return this.time;
    }

    public long getDifference(long end) {
        return end - this.time;
    }

    public long getDifference(Timing end) {
        // the end.time is upon us
        return getDifference(end.time);
    }

    public long getDifferenceNow() {
        return getDifference(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return String.valueOf(this.time);
    }

}
