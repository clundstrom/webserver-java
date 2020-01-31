import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class TCPEchoClient extends AbstractNetworkLayer {

    public static void main(String[] args) {

        // Handle mandatory arguments
        if (args.length < 2) {
            System.err.println("Error: Specify arguments server_name port (buffer-size) (transfer-rate) ");
            System.exit(1);
        }

        // Parse buffer-size
        if (args.length >= 3) {
            BUFSIZE = ArgParser.tryParse(args[2]);
            if(BUFSIZE < 1) {
                System.err.println("Buffer size not allowed. Exiting..");
                System.exit(1);
            }
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
            Logger logger = new Logger(DEBUG);
            // TODO: Abstract

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
                System.out.println(new String(buf, 0, read));
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
}
