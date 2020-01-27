import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class PacketTask implements Runnable {

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
        int nrOutPackets = 0;
        int nrInPackets = 0;
        long start = System.currentTimeMillis();
        long end;

        /* Process packages */
        for (int i = 0; i < nrOfPackets; i++) {
            try {
                socket.send(sent);
                nrOutPackets++;
                socket.receive(received);
                nrInPackets++;
            } catch (IOException e) {
                System.err.println("There was an error while sending or receiving packets.");
            }

            /* Compare sent and received message */
            String receivedString =
                    new String(received.getData(),
                            received.getOffset(),
                            received.getLength());
            if (receivedString.compareTo(message) == 0){
                System.out.printf("%d bytes sent and received\n", received.getLength());
            }
            else{
                System.out.print("Sent and received msg not equal!\n");
            }

            end = System.currentTimeMillis();
            if(end-start >= 1000) return;
        }
        end = System.currentTimeMillis();

        /* Update logger */
        logger.setSent(logger.getSent() + nrOutPackets);
        logger.setReceived(logger.getReceived() + nrInPackets);
        System.out.println(logger.toString());


        /* Wait until the full second has passed */
        try {
            Thread.sleep(1000 - (end - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}