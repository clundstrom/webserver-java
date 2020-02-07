import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Class which represents a TCP transmission Task.
 */
public class TCPTransmitTask implements IEchoTaskAdapter {

    private Socket socket;
    private int nrOfPackets;
    private Logger logger;
    private String message;
    ScheduledExecutorService es;

    public TCPTransmitTask(Socket socket, int nrOfPackets, String message, Logger logger) {
        this.socket = socket;
        this.nrOfPackets = nrOfPackets;
        this.logger = logger;
        this.message = message;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long total;

        try {

            // Create buffer
            byte[] buf = message.getBytes();

            // Create a Writer to the output-stream
            OutputStream output = socket.getOutputStream();

            // Process messages
            for (int i = 0; i < nrOfPackets; i++) {
                // Write to output
                output.write(buf, 0, buf.length);
                logger.setSent(logger.getSent() + 1);
                logger.setReceived(logger.getReceived() + 1);
            }
            output.flush();

            // Keep track of time spent
            total = System.currentTimeMillis() - start;

            // If the time exceeds 1 second abort immediately
            if (total >= 999) {
                System.exit(0);
                Thread.currentThread().interrupt();
            }
        } catch (SocketException e) {
            System.err.println("Connection to server lost.");
            stopSchedule();
        } catch (IOException e) {
            System.err.println("There was an error writing to or reading from stream.");
            stopSchedule();
        }

        // Print current state of logger.
        System.out.println(logger);

        total = System.currentTimeMillis();

        // Wait until the full second has passed before terminating thread.
        sleepTask(total, start);
    }


    /**
     * Attaches scheduler to task for termination.
     *
     * @param es ScheduledExecutorService
     */
    public void attachScheduler(ScheduledExecutorService es) {
        this.es = es;
    }


    /**
     * Wait until the full second has passed before terminating thread
     *
     * @param total
     * @param start
     */
    @Override
    public void sleepTask(long total, long start) {
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            System.err.println("Task timeout.");
        }
    }


    /**
     * Stops continuous scheduling of transmission tasks.
     */
    public void stopSchedule() {
        this.es.shutdownNow();
    }


    @Override
    public void setNrOfPackets(int i) {
        this.nrOfPackets = i;
    }
}
