package hse.java.lectures.lesson7.dau;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface DauService {

    void postEvent(Event event);

    Map<Integer, Long> getDauStatistics(List<Integer> authorIds);

    Long getAuthorDauStatistics(int authorId);

    class BaseImpl implements DauService {

        private final Clock clock;
        private LocalDate currDay;
        private final ConcurrentHashMap<Integer, Set<Integer>> currData = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Integer, Set<Integer>> prevData = new ConcurrentHashMap<>();

        public BaseImpl(Clock clock) {
            this.clock = clock;
            this.currDay = LocalDate.now(clock);
        }

        private void rotateIfNeeded() {
            LocalDate now = LocalDate.now(clock);
            synchronized (this) {
                if (!now.equals(currDay)) {
                    prevData.clear();
                    prevData.putAll(currData);
                    currData.clear();
                    currDay = now;
                }
            }
        }

        @Override
        public void postEvent(Event event) {
            rotateIfNeeded();
            currData.computeIfAbsent(event.authorId(),
                                     k -> ConcurrentHashMap.newKeySet())
                    .add(event.userId());
        }

        @Override
        public Map<Integer, Long> getDauStatistics(List<Integer> authorIds) {
            rotateIfNeeded();
            Map<Integer, Long> result = new HashMap<>();
            for (Integer authorId : authorIds) {
                result.put(authorId, getAuthorDauStatistics(authorId));
            }
            return result;
        }

        @Override
        public Long getAuthorDauStatistics(int authorId) {
            rotateIfNeeded();
            Set<Integer> users = prevData.get(authorId);
            return (users == null) ? 0L : users.size();
        }
    }
}
