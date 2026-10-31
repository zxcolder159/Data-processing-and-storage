package app;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SyncListBubbleSorterTest {

    private void waitForExpected(List<String> list, List<String> expected, long maxWaitMs) throws InterruptedException {
        long end = System.currentTimeMillis() + maxWaitMs;
        while (System.currentTimeMillis() < end) {
            synchronized (list) {
                if (expected.equals(list)) {
                    return;
                }
            }
            Thread.sleep(10);
        }
    }

    @Test
    void sortsTwoElements() throws InterruptedException {
        List<String> list = Collections.synchronizedList(new ArrayList<>(List.of("b", "a")));
        AtomicLong counter = new AtomicLong();

        Thread t = new Thread(new SyncListBubbleSorter(list, 1, counter));
        t.setDaemon(true);
        t.start();

        waitForExpected(list, List.of("a", "b"), 2000);
        t.interrupt();
        t.join(500);

        assertEquals(List.of("a", "b"), list);
        assertTrue(counter.get() > 0);
    }

    @Test
    void sortsMultipleElementsReverse() throws InterruptedException {
        List<String> list = Collections.synchronizedList(new ArrayList<>(List.of("delta", "gamma", "beta", "alpha")));
        AtomicLong counter = new AtomicLong();

        Thread t = new Thread(new SyncListBubbleSorter(list, 1, counter));
        t.setDaemon(true);
        t.start();

        List<String> expected = List.of("alpha", "beta", "delta", "gamma");
        waitForExpected(list, expected, 2000);
        t.interrupt();
        t.join(500);

        assertEquals(expected, list);
        assertTrue(counter.get() > 0);
    }

    @Test
    void emptyAndSingleElementList() throws InterruptedException {
        List<String> emptyList = Collections.synchronizedList(new ArrayList<>());
        AtomicLong emptyCounter = new AtomicLong();
        Thread t1 = new Thread(new SyncListBubbleSorter(emptyList, 1, emptyCounter));
        t1.setDaemon(true);
        t1.start();

        List<String> singleList = Collections.synchronizedList(new ArrayList<>(List.of("lone")));
        AtomicLong singleCounter = new AtomicLong();
        Thread t2 = new Thread(new SyncListBubbleSorter(singleList, 1, singleCounter));
        t2.setDaemon(true);
        t2.start();

        Thread.sleep(50);
        t1.interrupt();
        t2.interrupt();
        t1.join(500);
        t2.join(500);

        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());
        assertEquals(0, emptyList.size());
        assertEquals(List.of("lone"), singleList);
        assertEquals(0, emptyCounter.get());
        assertEquals(0, singleCounter.get());
    }

    @Test
    void handlesDuplicates() throws InterruptedException {
        List<String> list = Collections.synchronizedList(new ArrayList<>(List.of("banana", "apple", "banana", "apple")));
        AtomicLong counter = new AtomicLong();

        Thread t = new Thread(new SyncListBubbleSorter(list, 1, counter));
        t.setDaemon(true);
        t.start();

        List<String> expected = List.of("apple", "apple", "banana", "banana");
        waitForExpected(list, expected, 2000);
        t.interrupt();
        t.join(500);

        assertEquals(expected, list);
    }

    @Test
    void multipleThreadsSortConcurrently() throws InterruptedException {
        List<String> list = Collections.synchronizedList(new ArrayList<>(List.of("f", "a", "e", "b", "d", "c")));
        AtomicLong counter = new AtomicLong();

        List<Thread> sorters = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Thread t = new Thread(new SyncListBubbleSorter(list, 1, counter), "sync-sorter-" + i);
            t.setDaemon(true);
            sorters.add(t);
            t.start();
        }

        List<String> expected = List.of("a", "b", "c", "d", "e", "f");
        waitForExpected(list, expected, 3000);

        for (Thread t : sorters) {
            t.interrupt();
            t.join(500);
        }

        assertEquals(expected, list);
        assertTrue(counter.get() > 0);
    }
}
