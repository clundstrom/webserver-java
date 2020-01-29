import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Class which represents a TCP transmission Task.
 */
public class TCPTransmitTask implements Runnable {

    private Socket socket;
    private int nrOfPackets;
    private Logger logger;
    private String message;
    private int buffSize;

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
            PrintWriter output = new PrintWriter(socket.getOutputStream());
            InputStream is = socket.getInputStream();

            // Process packages
            for (int i = 0; i < nrOfPackets; i++) {

                // Write to output
                output.println(message);
                logger.setSent(logger.getSent()+1);

                // Receive input
                int bytes = is.read(buf);
                logger.setTotalReceived(logger.getTotalReceived() + 1);

                // Keep track of time spent
                total = System.currentTimeMillis() - start;

                // If the time exceeds 1 second abort immediately
                if (total >= 999) {
                    logger.setRemaining(nrOfPackets - i - 1);
                    System.out.println(logger.toString());
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(logger);
        total = System.currentTimeMillis();

        /* Wait until the full second has passed before terminating thread*/
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
