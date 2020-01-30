import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TCPEchoClient {

    public static final int MYPORT = 6000;
    public static final String MSG = "Ping!";
    public static int TRANSFER_RATE = 2;
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

        // Parse transfer-rate
        if (args.length >= 4) {
            TRANSFER_RATE = ArgParser.tryParse(args[3]);
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
            socket.connect(remote, 10000);

            // Create transmission task
            TCPTransmitTask task = new TCPTransmitTask(socket, TRANSFER_RATE, MSG, logger, BUFSIZE);

            // Transmit task
            scheduleTask(task);

            // Open input socket
            InputStream in = socket.getInputStream();

            // Continuously read from input stream
            int read;
            while((read = in.read(buf)) != -1){

                logger.setTotalReceived(logger.getTotalReceived() + 1);
                // Print current state of logger.
                System.out.println(logger);
            }

            // Close streams and socket when there is nothing to read or server terminates.
            in.close();
            socket.close();
        }
        catch (ConnectException e){
            System.err.println("Could not connect to host.");
        }
        catch (BindException e){
            System.err.println("Could not bind to port " + MYPORT);
        }
        catch (UnknownHostException e){
            System.err.println("Could not resolve host.");
        }
        catch (NullPointerException e){
            System.err.println("Address can not be null.");
        }
        catch (IllegalArgumentException e){
            System.err.println("Illegal argument. Allowed port range 0-65535");
        }
        catch (IOException e) {
            System.err.println("There was an error reading or writing to stream.");
        }
    }

    /**
     * Function which handles package scheduling for the client.
     * @param task Custom task which sends and receives packages.
     */
    private static void scheduleTask(TCPTransmitTask task) {

        // Create continuous execution of the task.
        ScheduledExecutorService es = new ScheduledThreadPoolExecutor(1);
        task.attachScheduler(es);

        // Special case, run once.
        if (TRANSFER_RATE == 0) {
            es.submit(task);
        } else {
            es.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        }
    }
}
