import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Webservice {

    public static int MYPORT = 8080;
    public static String PATH = "static";

    public static void main(String[] args) {
        try {
            ArgParser.verifyArguments(args);

            // Parse Port
            if (args.length >= 1) {
                MYPORT = ArgParser.tryParse(args[0]);
                if(MYPORT > 65535 || MYPORT < 1) {
                    System.err.println("Port out of range. Exiting..");
                    System.exit(1);
                }
            }

            // Parse Path
            if (args.length >= 2) {
                PATH = args[1];
                if(!Paths.get(PATH).toFile().isDirectory()) {
                    System.err.println("Given path is not a valid directory. Exiting..");
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
                ServeTask task = new ServeTask(incoming, PATH);

                // Submit task to executor service
                es.execute(task);
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
