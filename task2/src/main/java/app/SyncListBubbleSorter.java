package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SyncListBubbleSorter implements Runnable {

    private final List<String> list;
    private final long delayMs;
    private final AtomicLong stepCounter;

    public SyncListBubbleSorter(List<String> list, long delayMs, AtomicLong stepCounter) {
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
        synchronized (list) {
            int size = list.size();
            for (int i = 0; i < size - 1; i++) {
                Thread.sleep(delayMs);
                stepCounter.incrementAndGet();
                if (list.get(i).compareTo(list.get(i + 1)) > 0) {
                    Collections.swap(list, i, i + 1);
                }
                Thread.sleep(delayMs);
            }
        }
    }
}
