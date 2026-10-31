package app;

import app.LinkedStringList.Node;

public class BubbleSorter implements Runnable {

    private final LinkedStringList list;
    private final long delayMs;
    private final java.util.concurrent.atomic.AtomicLong stepCounter;

    public BubbleSorter(LinkedStringList list, long delayMs, java.util.concurrent.atomic.AtomicLong stepCounter) {
        this.list = list;
        this.delayMs = delayMs;
        this.stepCounter = stepCounter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                bubblePass();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void bubblePass() throws InterruptedException {
        Node prev = list.getHead();

        while (true) {
            Thread.sleep(delayMs);

            Node first;
            Node second;

            synchronized (prev.lock) {
                first = prev.next;
                if (first == null) break;
                synchronized (first.lock) {
                    second = first.next;
                    if (second == null) {
                        prev = first;
                        continue;
                    }
                    stepCounter.incrementAndGet();
                    if (first.value.compareTo(second.value) > 0) {
                        first.next = second.next;
                        prev.next = second;
                        second.next = first;
                        prev = second;
                    } else {
                        prev = first;
                    }
                }
            }

            Thread.sleep(delayMs);
        }
    }
}
