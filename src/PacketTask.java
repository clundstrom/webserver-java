import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

public class PacketTask extends TimerTask {

    private DatagramSocket socket;
    private DatagramPacket sent;
    private DatagramPacket received;
    private int nrOfPackets;
    private String message;

    public PacketTask(DatagramSocket socket, DatagramPacket sent, DatagramPacket received, String message, int nrOfPackets) {
        this.socket = socket;
        this.sent = sent;
        this.received = received;
        this.nrOfPackets = nrOfPackets;
        this.message = message;
    }

    public void run() {
        try {
            for (int i = 0; i < nrOfPackets; i++) {
                socket.send(sent);
                socket.receive(received);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
