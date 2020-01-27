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
        int out = 0;
        int in = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < nrOfPackets; i++) {
            try {
                socket.send(sent);
                out++;
                socket.receive(received);
                in++;
            } catch (IOException e) {
                System.out.println("There was an error.");
            }

            /* Compare sent and received message */
            String receivedString =
                    new String(received.getData(),
                            received.getOffset(),
                            received.getLength());
            if (receivedString.compareTo(message) == 0)
                System.out.printf("%d bytes sent and received\n", received.getLength());
            else
                System.out.print("Sent and received msg not equal!\n");
        }
        long end = System.currentTimeMillis();

        /* Wait  */
        while (end - start <= 1000){
            if(end-start > 1000){
                break;

            }
            end = System.currentTimeMillis();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.setSent(logger.getSent()+out);
        logger.setReceived(logger.getReceived()+in);
        System.out.println("Packages(sent/received): " + logger.getSent() + ", " + logger.getReceived());
    }
}