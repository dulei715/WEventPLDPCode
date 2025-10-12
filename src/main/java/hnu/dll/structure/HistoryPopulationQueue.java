package hnu.dll.structure;

import java.util.*;

public class HistoryPopulationQueue<T> {
    private Integer maxWindowSize;
    private ArrayDeque<Set<T>> keyQueue;

    public HistoryPopulationQueue(Integer maxWindowSize) {
        this.maxWindowSize = maxWindowSize;
        this.keyQueue = new ArrayDeque<>();
    }

    public void offer(Set<T> value) {
        if (this.maxWindowSize <= 0) {
            return;
        }
        if (this.keyQueue.size() >= this.maxWindowSize) {
            this.keyQueue.poll();
        }
        this.keyQueue.addLast(value);
    }

    public Integer getReverseSizeSum(Integer len) {
        Iterator<Set<T>> descendingIterator = this.keyQueue.descendingIterator();
        Integer totalSize = 0;
        int index = 0;
        while (descendingIterator.hasNext() && index < len) {
            totalSize += descendingIterator.next().size();
            ++index;
        }
        return totalSize;
    }

    public Set<T> getFirst() {
        return this.keyQueue.peek();
    }


    public boolean isEmpty() {
        return keyQueue.isEmpty();
    }

    public int size() {
        return keyQueue.size();
    }

    public void clear() {
        keyQueue.clear();
    }
}

