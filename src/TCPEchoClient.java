import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TCPEchoClient {

    public static final int MYPORT = 6000;
    public static final String MSG = "Ping!";
    public static int TRANSFER_RATE = 1;
    public static int BUFSIZE = 1024;


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


        try {
            byte[] buf = new byte[BUFSIZE];

            // Create socket
            Socket socket = new Socket();

            // Create local endpoint and bind to socket
            SocketAddress local = new InetSocketAddress(MYPORT);
            socket.bind(local);

            // Create endpoint
            SocketAddress remote = new InetSocketAddress(args[0], Integer.parseInt(args[1]));

            // Create logger
            Logger logger = new Logger();

            // Connect to remote, set conn timeout to 10sec
            socket.connect(remote, 10);

            // Create packet
            TCPTransmitTask task = new TCPTransmitTask(socket,TRANSFER_RATE, "Pong!", logger);

            sendReceive(task);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Function which handles package scheduling for the client.
     * @param task Custom task which sends and receives packages.
     */
    private static void sendReceive(TCPTransmitTask task) {
        // Special case, run once.
        if (TRANSFER_RATE == 0) {
            task.run();
        } else {
            ScheduledExecutorService es = new ScheduledThreadPoolExecutor(1);
            es.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        }
    }
}
