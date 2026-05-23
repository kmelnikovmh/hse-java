package hse.java.lectures.lesson7.limiter;

import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

public class RateLimiter {

    private long windowNanos;
    private int maxRequests;
    private LinkedList<Long> stamps = new LinkedList<>();
    private int currentSize = 0;

    public RateLimiter(ChronoUnit unit, int maxRequests) {
        if (unit != ChronoUnit.SECONDS && unit != ChronoUnit.MINUTES) {
            throw new IllegalArgumentException("bad unit");
        }
        if (1 > maxRequests) {
            throw new IllegalArgumentException("bad max");
        }
        this.windowNanos = unit.getDuration().toNanos();
        this.maxRequests = maxRequests;
    }

    public synchronized boolean check() {
        long now = System.nanoTime();
        long border = now - windowNanos;
        int removed = 0;
        while (!stamps.isEmpty() && stamps.getFirst() <= border) {
            stamps.removeFirst();
            ++removed;
        }
        currentSize = currentSize - removed;
        if (currentSize < maxRequests) {
            stamps.addLast(now);
            ++currentSize;
            return true;
        }
        return false;
    }
}
