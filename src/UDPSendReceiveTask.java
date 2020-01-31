import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Class that represents a task which will send and receive packets at a specified TRANSFER RATE.
 * Includes a logger which keeps track of amount of packages sent and received, also if packages are lost.
 */
public class UDPSendReceiveTask implements Runnable {

    private static boolean DEBUG = false;

    private DatagramSocket socket;
    private DatagramPacket sent;
    private DatagramPacket received;
    private int nrOfPackets;
    private String message;
    private Logger logger;
    private ScheduledExecutorService es;

    public UDPSendReceiveTask(DatagramSocket socket, DatagramPacket sent, DatagramPacket received, String message, int nrOfPackets, Logger logger) {
        this.socket = socket;
        this.sent = sent;
        this.received = received;
        this.nrOfPackets = nrOfPackets;
        this.message = message;
        this.logger = logger;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long total;

        /* Process packages */
        for (int i = 0; i < nrOfPackets; i++) {
            try {
                // Send and receive packets.
                socket.send(sent);
                socket.receive(received);

                // Update total packets to logger.
                logger.setSent(logger.getSent() + 1);
                logger.setReceived(logger.getReceived() + 1);

            } catch (SocketTimeoutException e) {
                System.err.println("Address can not be null.");
                stopScheduleAndExit();
            } catch (PortUnreachableException e) {
                System.err.println("Port unreachable. Aborting..");
                stopScheduleAndExit();

            } catch (IOException e) {
                System.err.println("There was an error while sending or receiving packets.");
                stopScheduleAndExit();
            }

            /* Compare sent and received message */
            String receivedString = new String(received.getData(), received.getOffset(), received.getLength());

            if (receivedString.compareTo(new String(sent.getData(), sent.getOffset(), sent.getLength())) == 0) {
                if (DEBUG)
                    System.out.printf("Sent/Rec: %d, %d bytes\n", sent.getLength(), received.getLength());
            } else {
                    System.err.printf("Warning(Packet mismatch): Sent/Rec: %d, %d bytes\n", sent.getLength(), received.getLength());
            }

            total = System.currentTimeMillis() - start;

            if (total >= 1000) {
                logger.setRemaining(nrOfPackets - i - 1);
                System.out.println(logger.toString());
                System.exit(0);
            }
            logger.setRemaining(nrOfPackets - i - 1);
        }
        System.out.println(logger);
        total = System.currentTimeMillis();


        /* Wait until the full second has passed */
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            System.err.println("Task timeout.");
        }
    }

    public void setNrOfPackets(int i) {
        this.nrOfPackets = i;
    }

    public void setDebug(boolean debug) {
        this.DEBUG = debug;
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
    public void stopSchedule() {
        this.es.shutdownNow();
    }

    /**
     * Stops all tasks and exits.
     */
    public void stopScheduleAndExit(){
        this.es.shutdownNow();
        System.exit(1);
    }
}