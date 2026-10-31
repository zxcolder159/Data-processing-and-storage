package app;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class BubbleSorterTest {

    private List<String> toList(LinkedStringList list) {
        List<String> result = new ArrayList<>();
        for (String s : list) result.add(s);
        return result;
    }

    private void waitForExpected(LinkedStringList list, List<String> expected, long maxWaitMs) throws InterruptedException {
        long end = System.currentTimeMillis() + maxWaitMs;
        while (System.currentTimeMillis() < end) {
            if (expected.equals(toList(list))) {
                return;
            }
            Thread.sleep(10);
        }
    }

    @Test
    void sortsTwoElements() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("b");
        list.addFirst("a");

        AtomicLong counter = new AtomicLong();
        Thread t = new Thread(new BubbleSorter(list, 10, counter));
        t.setDaemon(true);
        t.start();

        Thread.sleep(300);
        t.interrupt();
        t.join(500);

        List<String> result = toList(list);
        assertEquals(List.of("a", "b"), result);
        assertTrue(counter.get() > 0);
    }

    @Test
    void sortsMultipleElements() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("delta");
        list.addFirst("alpha");
        list.addFirst("gamma");
        list.addFirst("beta");

        AtomicLong counter = new AtomicLong();
        Thread t = new Thread(new BubbleSorter(list, 10, counter));
        t.setDaemon(true);
        t.start();

        Thread.sleep(1000);
        t.interrupt();
        t.join(500);

        List<String> result = toList(list);
        assertEquals(List.of("alpha", "beta", "delta", "gamma"), result);
    }

    @Test
    void sortsEmptyListWithoutErrors() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        AtomicLong counter = new AtomicLong();
        Thread t = new Thread(new BubbleSorter(list, 1, counter));
        t.setDaemon(true);
        t.start();

        Thread.sleep(50);
        t.interrupt();
        t.join(500);

        assertFalse(t.isAlive());
        assertEquals(List.of(), toList(list));
        assertEquals(0, counter.get());
    }

    @Test
    void sortsSingleElementList() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("solo");

        AtomicLong counter = new AtomicLong();
        Thread t = new Thread(new BubbleSorter(list, 1, counter));
        t.setDaemon(true);
        t.start();

        Thread.sleep(50);
        t.interrupt();
        t.join(500);

        assertFalse(t.isAlive());
        assertEquals(List.of("solo"), toList(list));
        assertEquals(0, counter.get());
    }

    @Test
    void alreadySortedListRemainsSorted() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("cherry");
        list.addFirst("banana");
        list.addFirst("apple");

        AtomicLong counter = new AtomicLong();
        Thread t = new Thread(new BubbleSorter(list, 1, counter));
        t.setDaemon(true);
        t.start();

        Thread.sleep(50);
        t.interrupt();
        t.join(500);

        assertEquals(List.of("apple", "banana", "cherry"), toList(list));
        assertTrue(counter.get() > 0);
    }

    @Test
    void handlesDuplicatesAndReverseOrder() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("apple");
        list.addFirst("banana");
        list.addFirst("apple");
        list.addFirst("banana");

        AtomicLong counter = new AtomicLong();
        Thread t = new Thread(new BubbleSorter(list, 1, counter));
        t.setDaemon(true);
        t.start();

        waitForExpected(list, List.of("apple", "apple", "banana", "banana"), 2000);
        t.interrupt();
        t.join(500);

        assertEquals(List.of("apple", "apple", "banana", "banana"), toList(list));
    }

    @Test
    void multipleThreadsSortConcurrently() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("f");
        list.addFirst("a");
        list.addFirst("e");
        list.addFirst("b");
        list.addFirst("d");
        list.addFirst("c");

        AtomicLong counter = new AtomicLong();
        List<Thread> sorters = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new BubbleSorter(list, 1, counter), "sorter-" + i);
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

        assertEquals(expected, toList(list));
        assertTrue(counter.get() > 0);
    }
}
