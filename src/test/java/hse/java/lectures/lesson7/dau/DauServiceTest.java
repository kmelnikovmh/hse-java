package hse.java.lectures.lesson7.dau;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("dauservice")
class BaseImplDauServiceTest {

    static class MutableClock extends Clock {
        private Instant instant;

        MutableClock() {
            this.instant = LocalDateTime.of(2026, 1, 1, 10, 0)
                                        .toInstant(ZoneOffset.UTC);
        }
        void nextDay() {
            instant = instant.plusSeconds(24 * 60 * 60);
        }
        @Override public Instant instant() { return instant; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    @Test
    void TestNoEventsForAuthor() {
        MutableClock clock = new MutableClock();

        DauService service = new DauService.BaseImpl(clock);

        assertEquals(0, service.getAuthorDauStatistics(9));
    }

    @Test
    void TestSingleAuthorMultipleUsersSingleDay() {
        MutableClock clock = new MutableClock();
        DauService service = new DauService.BaseImpl(clock);

        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(2, 100));

        clock.nextDay();

        assertEquals(2, service.getAuthorDauStatistics(100));
    }

    @Test
    void TestMultipleAuthorsShareUsers() {
        MutableClock clock = new MutableClock();
        DauService service = new DauService.BaseImpl(clock);

        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(1, 200));
        service.postEvent(new Event(2, 200));

        clock.nextDay();

        Map<Integer, Long> DataDay = service.getDauStatistics(List.of(100, 200));
        assertEquals(1, DataDay.get(100));
        assertEquals(2, DataDay.get(200));
    }

    @Test
    void TestNextDayClearsOldData() {
        MutableClock clock = new MutableClock();
        DauService service = new DauService.BaseImpl(clock);

        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(2, 100));

        clock.nextDay();
        assertEquals(2, service.getAuthorDauStatistics(100));

        clock.nextDay();
        assertEquals(0, service.getAuthorDauStatistics(100));
    }

    @Test
    void TestMultipleAuthorsMultipleUsersMultipleDays() {
        MutableClock clock = new MutableClock();
        DauService service = new DauService.BaseImpl(clock);

        service.postEvent(new Event(1, 100));
        service.postEvent(new Event(2, 200));
        service.postEvent(new Event(3, 200));

        clock.nextDay();

        service.postEvent(new Event(4, 100));
        service.postEvent(new Event(5, 200));

        Map<Integer, Long> DataDayOne = service.getDauStatistics(List.of(100, 200));
        assertEquals(1, DataDayOne.get(100));
        assertEquals(2, DataDayOne.get(200));

        clock.nextDay();

        Map<Integer, Long> DataDayTwo = service.getDauStatistics(List.of(100, 200));
        assertEquals(1, DataDayTwo.get(100));
        assertEquals(1, DataDayTwo.get(200));
    }

    @Test
    void TestManyAuthorsManyUsers() {
        MutableClock clock = new MutableClock();
        DauService service = new DauService.BaseImpl(clock);

        for (int user = 1; user <= 5; ++user) {
            for (int author = 100; author <= 102; ++author) {
                service.postEvent(new Event(user, author));
            }
        }

        clock.nextDay();

        Map<Integer, Long> stats = service.getDauStatistics(List.of(100, 101, 102, 103));
        assertEquals(5, stats.get(100));
        assertEquals(5, stats.get(101));
        assertEquals(5, stats.get(102));
        assertEquals(0, stats.get(103));
    }

    @Test
    void TestConcurrentPosts() throws InterruptedException {
    MutableClock clock = new MutableClock();
    DauService service = new DauService.BaseImpl(clock);

    int threads = 10;
    int usersPerThread = 100;
    int authorId = 500;
    Thread[] ts = IntStream.range(0, threads)
            .mapToObj(threadId -> new Thread(() -> {
                for (int user = 1; user <= usersPerThread; ++user) {
                    int userId = threadId * usersPerThread + user;
                    service.postEvent(new Event(userId, authorId));
                }
            }))
            .toArray(Thread[]::new);


    for (Thread t : ts) t.start();
    for (Thread t : ts) t.join();

    clock.nextDay();

    assertEquals((threads * usersPerThread), service.getAuthorDauStatistics(authorId));
    }
}
