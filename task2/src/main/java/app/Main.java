package app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    private static final int MAX_LINE_LENGTH = 80;

    public static void main(String[] args) throws Exception {
        int numThreads = args.length > 0 ? Integer.parseInt(args[0]) : 2;
        long delayMs = args.length > 1 ? Long.parseLong(args[1]) : 1000;
        boolean useStdLib = args.length > 2 && args[2].equals("--stdlib");

        System.out.println("Потоков сортировки: " + numThreads + ", задержка: " + delayMs + "мс, режим: " + (useStdLib ? "ArrayList" : "LinkedList"));

        AtomicLong stepCounter = new AtomicLong(0);
        List<Thread> sorterThreads = new ArrayList<>();

        if (useStdLib) {
            runWithStdLib(numThreads, delayMs, stepCounter, sorterThreads);
        } else {
            runWithCustomList(numThreads, delayMs, stepCounter, sorterThreads);
        }
    }

    private static void runWithCustomList(int numThreads, long delayMs, AtomicLong stepCounter, List<Thread> sorterThreads) throws Exception {
        LinkedStringList list = new LinkedStringList();

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(new BubbleSorter(list, delayMs, stepCounter), "Sorter-" + i);
            t.setDaemon(true);
            t.start();
            sorterThreads.add(t);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                printList(list, stepCounter);
            } else {
                addSplitLines(line, list);
            }
        }

        for (Thread t : sorterThreads) t.interrupt();
    }

    private static void runWithStdLib(int numThreads, long delayMs, AtomicLong stepCounter, List<Thread> sorterThreads) throws Exception {
        List<String> list = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(new SyncListBubbleSorter(list, delayMs, stepCounter), "Sorter-" + i);
            t.setDaemon(true);
            t.start();
            sorterThreads.add(t);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                synchronized (list) {
                    System.out.println("[Шагов: " + stepCounter.get() + "] Список (" + list.size() + " эл.):");
                    for (String s : list) {
                        System.out.println("  " + s);
                    }
                }
            } else {
                addSplitLinesStdLib(line, list);
            }
        }

        for (Thread t : sorterThreads) t.interrupt();
    }

    private static void addSplitLines(String line, LinkedStringList list) {
        for (int i = 0; i < line.length(); i += MAX_LINE_LENGTH) {
            String chunk = line.substring(i, Math.min(i + MAX_LINE_LENGTH, line.length()));
            list.addFirst(chunk);
        }
    }

    private static void addSplitLinesStdLib(String line, List<String> list) {
        for (int i = 0; i < line.length(); i += MAX_LINE_LENGTH) {
            String chunk = line.substring(i, Math.min(i + MAX_LINE_LENGTH, line.length()));
            list.add(0, chunk);
        }
    }

    private static void printList(LinkedStringList list, AtomicLong stepCounter) {
        System.out.println("[Шагов: " + stepCounter.get() + "] Список:");
        for (String s : list) {
            System.out.println("  " + s);
        }
    }
}
