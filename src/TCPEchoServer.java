import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPEchoServer {

    public static int MYPORT = 25650;
    public static int BUFSIZE = 1024;

    public static void main(String[] args) {
        try {
            // Parse buffer-size
            if (args.length >= 1) {
                BUFSIZE = ArgParser.tryParse(args[0]);
                if(BUFSIZE < 1) {
                    System.err.println("Buffer size not allowed. Exiting..");
                    System.exit(1);
                }
            }

            // Create server socket and bind to port.
            ServerSocket socket = new ServerSocket(MYPORT);

            // Create ExecutorService with expandable thread-pool
            ExecutorService es = Executors.newCachedThreadPool();
            System.out.println("Server online. Awaiting incoming connections..");

            while (true) {
                // Accept incoming requests
                Socket incoming = socket.accept();

                // Create task to respond to client
                System.out.println("Creating thread for incoming request: " + incoming.getInetAddress() + " " + incoming.getPort());

                // Create separate thread for the task
                TCPEchoTask task = new TCPEchoTask(incoming, BUFSIZE);

                // Submit task to executor service
                es.submit(task);
            }
        }
        catch (BindException e){
            System.err.println("Could not bind to port " + MYPORT);
        }
        catch (IllegalArgumentException e){
            System.err.println("Illegal argument. Allowed port range 0-65535");
        }
        catch (SecurityException e ){
            System.err.println("Security: Action not allowed.");
        }
        catch (IOException e) {
            System.err.println("Could not bind to port.");
        }
    }
}
