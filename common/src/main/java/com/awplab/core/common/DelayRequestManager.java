package com.awplab.core.common;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by andyphillips404 on 6/20/17.
 */
public class DelayRequestManager {
    private DelayQueue<DelayQueueItem> delayQueue = new DelayQueue<>();

    private TimeUnit timeUnit;
    private long delay;

    public DelayRequestManager(long delay, TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        this.delay = delay;
    }

    public void waitForClear() throws InterruptedException {
        delayQueue.add(new DelayQueueItem(delay, timeUnit));
        delayQueue.take();
    }

    public void clear() {
        delayQueue.clear();
    }

    private static class DelayQueueItem implements Delayed {
        private long startTime;

        public DelayQueueItem(long delay, TimeUnit timeUnit) {
            this.startTime = System.currentTimeMillis() + timeUnit.toMillis(delay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (this.startTime < ((DelayQueueItem) o).startTime) {
                return -1;
            }
            if (this.startTime > ((DelayQueueItem) o).startTime) {
                return 1;
            }
            return 0;
        }

    }


}
