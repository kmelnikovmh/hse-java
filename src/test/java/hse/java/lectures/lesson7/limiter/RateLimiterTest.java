package hse.java.lectures.lesson7.limiter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("limiter")
class RateLimiterTest {

    @Test
    void allowsThenRejects() {
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, 3);
        for (int i = 0; i < 3; ++i) {
            assertTrue(limiter.check());
        }
        assertFalse(limiter.check());
    }

    @Test
    void rejectedDontTakeSlots() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, 1);
        assertTrue(limiter.check());
        for (int i = 0; i < 50; ++i) {
            assertFalse(limiter.check());
        }
        Thread.sleep(1100);
        assertTrue(limiter.check());
    }

    @Test
    void threadSafe() throws Exception {
        int limit = 5;
        int threads = 20;
        RateLimiter limiter = new RateLimiter(ChronoUnit.SECONDS, limit);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger(0);
        for (int i = 0; i < threads; ++i) {
            pool.submit(() -> {
                try { latch.await(); } catch (InterruptedException e) { return; }
                if (limiter.check()) { ok.incrementAndGet(); }
            });
        }
        latch.countDown();
        pool.shutdown();
        while (!pool.isTerminated()) { Thread.sleep(50); }
        assertEquals(limit, ok.get());
    }
}
