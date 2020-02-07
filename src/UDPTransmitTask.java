import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Class that represents a task which will send and receive packets at a specified TRANSFER RATE.
 * Includes a logger which keeps track of amount of packages sent and received, also if packages are lost.
 */
public class UDPTransmitTask implements IEchoTaskAdapter {

    private DatagramSocket socket;
    private DatagramPacket sent;
    private DatagramPacket received;
    private int nrOfPackets;
    private Logger logger;
    private ScheduledExecutorService es;

    public UDPTransmitTask(DatagramSocket socket, DatagramPacket sent, DatagramPacket received, int nrOfPackets, Logger logger) {
        this.socket = socket;
        this.sent = sent;
        this.received = received;
        this.nrOfPackets = nrOfPackets;
        this.logger = logger;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long total;

        // Process packages
        for (int i = 0; i < nrOfPackets; i++) {
            try {
                // Send and receive packets.
                socket.send(sent);
                socket.receive(received);

                // Update total packets to logger.
                logger.setSent(logger.getSent() + 1);
                logger.setReceived(logger.getReceived() + 1);

            } catch (IOException e) {
                System.err.println("There was an error while sending or receiving packets.");
                stopScheduleAndExit();
            }

            // Compare sent and received messages
            logger.compare(received, sent);

            total = System.currentTimeMillis() - start;

            if (total >= 999) {
                logger.setRemaining(nrOfPackets - i - 1);
                System.out.println(logger.toString());
                return;
            }

            logger.setRemaining(nrOfPackets - i - 1);
        }
        System.out.println(logger);
        total = System.currentTimeMillis();


        // Wait until the full second has passed
        sleepTask(total, start);
    }


    @Override
    public void setNrOfPackets(int i) {
        this.nrOfPackets = i;
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
     * Stops continuous scheduling of transmission tasks.
     */
    @Override
    public void stopSchedule() {
        this.es.shutdownNow();
    }


    /**
     * Stops all tasks and exits.
     */
    public void stopScheduleAndExit() {
        this.es.shutdownNow();
        System.exit(1);
    }


    @Override
    public void sleepTask(long total, long start) {
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            System.err.println("Task timeout.");
        }
    }
}