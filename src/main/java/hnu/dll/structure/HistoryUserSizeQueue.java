package hnu.dll.structure;

import java.util.LinkedList;
import java.util.Queue;

@Deprecated
public class HistoryUserSizeQueue {
    private Integer maxWindowSize;
    private Queue<Integer> historyQueue;

    public HistoryUserSizeQueue(Integer maxWindowSize) {
        if (maxWindowSize < 0) {
            throw new RuntimeException("The maximum window size needs to be no less than 0!");
        }
        this.maxWindowSize = maxWindowSize;
        this.historyQueue = new LinkedList<>();
    }

    public void addElement(Integer element) {
        if (this.maxWindowSize <= 0) {
            return;
        }
        if (this.historyQueue.size() >= this.maxWindowSize) {
            this.historyQueue.poll();
        }
        this.historyQueue.offer(element);
    }

    public Integer getSum() {
        Integer sum = 0;
        for (Integer element : this.historyQueue) {
            sum += element;
        }
        return sum;
    }

    public Integer getLength() {
        return this.historyQueue.size();
    }

    public Integer getMaxWindowSize() {
        return maxWindowSize;
    }

    @Override
    public String toString() {
        return "HistoryUserSizeQueue{" +
                "maxWindowSize=" + maxWindowSize +
                ", historyQueue=" + historyQueue +
                '}';
    }
}
