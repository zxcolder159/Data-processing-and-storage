package app;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedStringList implements Iterable<String> {

    public static class Node {
        volatile String value;
        volatile Node next;
        final Object lock = new Object();

        Node(String value) {
            this.value = value;
        }
    }

    private final Node head = new Node(null);

    public void addFirst(String value) {
        Node newNode = new Node(value);
        synchronized (head.lock) {
            newNode.next = head.next;
            head.next = newNode;
        }
    }

    public Node getHead() {
        return head;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            private Node current = head.next;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public String next() {
                if (current == null) throw new NoSuchElementException();
                String val = current.value;
                current = current.next;
                return val;
            }
        };
    }
}
