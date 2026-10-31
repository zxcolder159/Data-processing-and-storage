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

    @Test
    void iteratorThrowsNoSuchElementExceptionWhenExhausted() {
        LinkedStringList list = new LinkedStringList();
        Iterator<String> emptyIt = list.iterator();
        assertThrows(java.util.NoSuchElementException.class, emptyIt::next);

        list.addFirst("item");
        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("item", it.next());
        assertFalse(it.hasNext());
        assertThrows(java.util.NoSuchElementException.class, it::next);
    }

    @Test
    void singleElementList() {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("lone");

        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("lone", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void getHeadAndNodeStructure() {
        LinkedStringList list = new LinkedStringList();
        LinkedStringList.Node head = list.getHead();
        assertNotNull(head);
        assertNull(head.value);
        assertNotNull(head.lock);
        assertNull(head.next);

        list.addFirst("first");
        assertSame(head, list.getHead());
        assertNotNull(head.next);
        assertEquals("first", head.next.value);
        assertNotNull(head.next.lock);
        assertNull(head.next.next);

        list.addFirst("second");
        assertEquals("second", head.next.value);
        assertEquals("first", head.next.next.value);
    }

    @Test
    void independentIterators() {
        LinkedStringList list = new LinkedStringList();
        list.addFirst("two");
        list.addFirst("one");

        Iterator<String> it1 = list.iterator();
        Iterator<String> it2 = list.iterator();

        assertEquals("one", it1.next());
        assertEquals("one", it2.next());
        assertEquals("two", it1.next());
        assertEquals("two", it2.next());
        assertFalse(it1.hasNext());
        assertFalse(it2.hasNext());
    }
}
