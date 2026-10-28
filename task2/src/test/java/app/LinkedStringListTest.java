package app;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class LinkedStringListTest {

    @Test
    void addFirstAndIterate() {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("c");
        list.addFirst("b");
        list.addFirst("a");

        List<String> result = new ArrayList<>();
        for (String s : list) result.add(s);

        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void emptyListIterable() {
        LinkedStringList list = new LinkedStringList();
        assertFalse(list.iterator().hasNext());
    }

    @Test
    void concurrentAddDoesNotLoseElements() throws InterruptedException {
        LinkedStringList list = new LinkedStringList();
        int perThread = 100;
        int threads = 4;

        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int idx = i;
            Thread t = new Thread(() -> {
                for (int j = 0; j < perThread; j++) {
                    list.addFirst("t" + idx + "-" + j);
                }
            });
            workers.add(t);
            t.start();
        }
        for (Thread t : workers) t.join();

        int count = 0;
        for (String ignored : list) count++;
        assertEquals(perThread * threads, count);
    }
}
