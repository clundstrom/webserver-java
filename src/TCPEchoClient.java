import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class TCPEchoClient extends AbstractNetworkLayer<Socket> {


    public TCPEchoClient(String[] args, boolean debug) {
        DEBUG = debug;
        verifyArguments(args);
        initialize(args);
    }


    public void initialize(String[] args) {
        try {
            byte[] buf = new byte[BUFSIZE];

            // Create socket
            socket = new Socket();

            // Create local endpoint and bind to socket
            SocketAddress local = new InetSocketAddress(MYPORT);
            socket.bind(local);

            // Create endpoint
            SocketAddress remote = new InetSocketAddress(args[0], Integer.parseInt(args[1]));

            // Create logger
            Logger logger = new Logger(DEBUG);

            // Connect to remote, set conn timeout to 10sec
            socket.connect(remote, 10000);

            // Create transmission task
            TCPTransmitTask task = new TCPTransmitTask(socket, TRANSFER_RATE, MSG, logger);

            // Transmit task
            scheduleTask(task);

            // Open input stream
            InputStream in = socket.getInputStream();

            // Continuously read from input stream
            int read;
            StringBuilder sb = new StringBuilder();
            while((read = in.read(buf)) != -1){

                // Append read bytes to string
                sb.append(new String(buf, 0 ,read));

                if(DEBUG && sb.length() == MSG.length()){
                    System.out.println(sb.toString());
                    // Reset StringBuilder
                    sb.setLength(0);
                }
                
                if(shutdownEarly){
                    break;
                }
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
