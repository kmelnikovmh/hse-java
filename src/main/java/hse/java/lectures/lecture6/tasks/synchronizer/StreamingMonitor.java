package hse.java.lectures.lecture6.tasks.synchronizer;

import java.util.List;

public class StreamingMonitor {

    private final List<StreamWriter> writers;
    private final int totalTicks;
    private int nextIndex = 0;
    private int ticks = 0;

    public StreamingMonitor(List<StreamWriter> writers, int ticksPerWriter) {
        this.writers = writers.stream()
                              .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                              .toList();
        this.totalTicks = writers.size() * ticksPerWriter;
    }

    public synchronized boolean acquire(StreamWriter writer) {
        while (ticks < totalTicks && writers.get(nextIndex) != writer) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return ticks < totalTicks;
    }

    public synchronized void release(StreamWriter writer) {
        ++ticks;
        nextIndex = (nextIndex + 1) % writers.size();
        notifyAll();
    }

    public synchronized void awaitCompletion() {
        while (ticks < totalTicks) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
