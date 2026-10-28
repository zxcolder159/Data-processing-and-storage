package app;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class BubbleSorterTest {

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

        java.util.List<String> result = new java.util.ArrayList<>();
        for (String s : list) result.add(s);

        assertEquals(java.util.List.of("a", "b"), result);
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

        java.util.List<String> result = new java.util.ArrayList<>();
        for (String s : list) result.add(s);

        assertEquals(java.util.List.of("alpha", "beta", "delta", "gamma"), result);
    }
}
