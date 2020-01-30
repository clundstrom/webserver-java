import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Class which represents a TCP transmission Task.
 */
public class TCPTransmitTask implements Runnable {

    private Socket socket;
    private int nrOfPackets;
    private Logger logger;
    private String message;
    private int buffSize;
    ScheduledExecutorService es;

    public TCPTransmitTask(Socket socket, int nrOfPackets, String message, Logger logger, int buffSize) {
        this.socket = socket;
        this.nrOfPackets = nrOfPackets;
        this.logger = logger;
        this.message = message;
        this.buffSize = buffSize;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long total;
        try {
            byte[] buf = new byte[buffSize];
            // Create a Writer to the output-stream
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);


            // Process messages
            for (int i = 0; i < nrOfPackets; i++) {
                // Write to output
                output.println(message);
                logger.setSent(logger.getSent() + 1);
            }
            output.flush();

            // Keep track of time spent
            total = System.currentTimeMillis() - start;

            // If the time exceeds 1 second abort immediately
            if (total >= 999) {
                System.out.println(logger.toString());
                System.exit(0);
                Thread.currentThread().interrupt();
            }
        }
        catch (SocketException e){
            System.err.println("Connection to server lost.");
            stopSchedule();
        }
        catch (IOException e) {
            System.err.println("There was an error writing to or reading from stream.");
            stopSchedule();
        }

        /* Wait until the full second has passed before terminating thread*/
        total = System.currentTimeMillis();
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            stopSchedule();
        }
    }

    /**
     * Attaches scheduler to task for termination.
     * @param es ScheduledExecutorService
     */
    public void attachScheduler(ScheduledExecutorService es) {
        this.es = es;
    }

    /**
     * Stops continuous scheduling of transmission tasks if server connection is lost.
     */
    public void stopSchedule(){
        this.es.shutdownNow();
    }
}
