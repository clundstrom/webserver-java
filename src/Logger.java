import java.net.DatagramPacket;

/*
    Logger class which handles counting of packages. Supports use by multiple threads.
 */
public class Logger {

    private volatile long totalSent = 0;
    private volatile long totalReceived = 0;
    private volatile long remaining = 0;
    private boolean DEBUG;

    Logger(boolean debug){
        this.DEBUG = debug;
    }

    @Override
    public String toString() {
        return "Logger{" + "sent=" + totalSent + ", received=" + totalReceived + ", remaining="+remaining + '}';

    }

    public synchronized long getSent() {
        return totalSent;
    }

    public synchronized void setSent(long sent) {
        this.totalSent = sent;
    }

    public synchronized long getReceived() {
        return totalReceived;
    }

    public synchronized void setReceived(long totalReceived) {
        this.totalReceived = totalReceived;
    }

    public synchronized void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    /**
     * Prints a comparison between two packets if DEBUG is enabled for logger.
     */
    public void compare(DatagramPacket received, DatagramPacket sent){
        String receivedString = new String(received.getData(), received.getOffset(), received.getLength());

        if (receivedString.compareTo(new String(sent.getData(), sent.getOffset(), sent.getLength())) == 0) {
            if (DEBUG)
                System.out.printf("Sent/Rec: %d, %d bytes\n", sent.getLength(), received.getLength());
        } else {
            System.err.printf("Warning(Packet mismatch): Sent/Rec: %d, %d bytes\n", sent.getLength(), received.getLength());
        }
    }
}