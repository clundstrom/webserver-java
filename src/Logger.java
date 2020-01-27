/*
    Logger class which handles counting of packages. Supports use by multiple threads.
 */
public class Logger {

    private volatile long sent = 0;
    private volatile long received = 0;

    @Override
    public String toString() {
        return "Logger{" + "sent=" + sent + ", received=" + received + '}';
    }

    public long getSent() {
        return sent;
    }

    public synchronized void setSent(long sent) {
        this.sent = sent;
    }

    public long getReceived() {
        return received;
    }

    public synchronized void setReceived(long received) {
        this.received = received;
    }
}