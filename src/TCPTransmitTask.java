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

            // Create buffer
            byte[] buf = parseToBuffer(message);

            // Create a Writer to the output-stream
            OutputStream output = socket.getOutputStream();

            // Process messages
            for (int i = 0; i < nrOfPackets; i++) {
                // Write to output
                output.write(buf, 0 , buf.length);
                logger.setSent(logger.getSent() + 1);
                logger.setReceived(logger.getReceived() + 1);
            }
            output.flush();

            // Keep track of time spent
            total = System.currentTimeMillis() - start;

            // If the time exceeds 1 second abort immediately
            if (total >= 999) {
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

        // Print current state of logger.
        System.out.println(logger);

        /* Wait until the full second has passed before terminating thread*/
        total = System.currentTimeMillis();
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            // Interrupt thread
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Parses message to specified buffer size.
     * @param message String to parse.
     * @return Byte array with parsed message.
     */
    private byte[] parseToBuffer(String message) {
        byte[] bytes = message.getBytes();
        byte[] buff = new byte[buffSize];

        for(int i=0; i < message.length(); i++){
            if(i == buff.length){
                System.err.println("Could not parse the complete message. (Buff size: " + buff.length + " Message: " + message.length() + ")");
                return buff;
            }
            buff[i] = bytes[i];
        }
        return buff;
    }

    /**
     * Attaches scheduler to task for termination.
     * @param es ScheduledExecutorService
     */
    public void attachScheduler(ScheduledExecutorService es) {
        this.es = es;
    }

    /**
     * Stops continuous scheduling of transmission tasks.
     */
    public void stopSchedule(){
        this.es.shutdownNow();
    }
}
