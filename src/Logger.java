/*
    Logger class which handles counting of packages. Supports use by multiple threads.
 */
public class Logger {

    private volatile long totalSent = 0;
    private volatile long totalReceived = 0;
    private volatile long remaining = 0;

    @Override
    public String toString() {
        return "Logger{" + "sent=" + totalSent + ", received=" + totalReceived + ", remaining="+remaining + '}';

    }

    public long getSent() {
        return totalSent;
    }

    public synchronized void setSent(long sent) {
        this.totalSent = sent;
    }

    public long getTotalReceived() {
        return totalReceived;
    }

    public synchronized void setTotalReceived(long totalReceived) {
        this.totalReceived = totalReceived;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public long getRemaining() {
        return remaining;
    }
}