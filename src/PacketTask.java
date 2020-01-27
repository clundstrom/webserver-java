import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * Class that represents a task which will send and receive packets at a specified TRANSFER RATE.
 * Includes a logger which keeps track of amount of packages sent and received, also if packages are lost.
 */
public class PacketTask implements Runnable {

    private static final boolean DEBUG = false;

    private DatagramSocket socket;
    private DatagramPacket sent;
    private DatagramPacket received;
    private int nrOfPackets;
    private String message;
    private Logger logger;

    public PacketTask(DatagramSocket socket, DatagramPacket sent, DatagramPacket received, String message, int nrOfPackets, Logger logger) {
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

                // Update logger.
                logger.setSent(logger.getSent() + 1);
                logger.setReceived(logger.getReceived() + 1);

            } catch (IOException e) {
                System.err.println("There was an error while sending or receiving packets.");
            }

            /* Compare sent and received message */
            String receivedString =
                    new String(received.getData(),
                            received.getOffset(),
                            received.getLength());
            if (receivedString.compareTo(message) == 0){
                if(DEBUG)
                    System.out.printf("%d bytes sent and received\n", received.getLength());
            }
            else{
                System.out.print("Sent and received msg not equal!\n");
            }

            total = System.currentTimeMillis()-start;

            if(total >= 1000){
                logger.setRemaining(nrOfPackets-i-1);
                System.out.println(logger.toString());
                System.exit(0);
            }
            logger.setRemaining(nrOfPackets-i-1);
        }
        System.out.println(logger.toString());
        total = System.currentTimeMillis();


        /* Wait until the full second has passed */
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}