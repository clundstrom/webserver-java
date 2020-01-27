import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UDPEchoClient {
    public static final int MYPORT = 6000;
    public static final String MSG = "An Echo Message!";
    public static int BUFSIZE = 1024;
    public static int TRANSFER_RATE = 1;


    // Enabling debug will show individual packets and their size. Not suitable for high rates.
    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        // Handle mandatory arguments
        if (args.length < 2) {
            System.err.println("Error: Specify arguments server_name port (buffer-size) (transfer-rate) ");
            System.exit(1);
        }

        // Parse buffer-size
        if (args.length >= 3) {
            BUFSIZE = ArgParser.tryParse(args[2]);
        }

        // Parse transfer rate
        if (args.length >= 4) {
            TRANSFER_RATE = ArgParser.tryParse(args[3]);
        }

        try {
            byte[] buf = new byte[BUFSIZE];

            // Create socket
            DatagramSocket socket = new DatagramSocket(null);

            // Create local endpoint using bind()
            SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
            socket.bind(localBindPoint);

            // Create remote endpoint
            //TODO: Kolla så parseInt inte throwar
            SocketAddress remoteBindPoint = new InetSocketAddress(args[0], Integer.parseInt(args[1]));

            // Create datagram packet for sending message
            DatagramPacket sendPacket = new DatagramPacket(MSG.getBytes(),
                    MSG.length(),
                    remoteBindPoint);

            // Create datagram packet for receiving echoed message
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

            // Create packet logger
            Logger logger = new Logger();

            // Create packet-task
            PacketTask task = new PacketTask(socket, sendPacket, receivePacket, MSG, TRANSFER_RATE, logger);
            task.setDebug(DEBUG);

            // Send and receive message
            sendReceive(task);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("There was an error establishing a connection.");
            System.exit(1);
        }
    }


    /**
     * Function which handles package scheduling for the client.
     * @param task Custom task which sends and receives packages.
     */
    private static void sendReceive(PacketTask task) {
        // Special case, run once.
        if (TRANSFER_RATE == 0) {
            task.run();
        } else {
            ScheduledExecutorService es = new ScheduledThreadPoolExecutor(1);
            es.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        }
    }
}