import java.io.IOException;
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

    public TCPTransmitTask(Socket socket, int nrOfPackets, String message, Logger logger) {
        this.socket = socket;
        this.nrOfPackets = nrOfPackets;
        this.logger = logger;
        this.message = message;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long total;
        try {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Process packages
            for (int i = 0; i < nrOfPackets; i++) {

                // Write incoming to output
                output.println(message);

                // Update total packets to logger.
                logger.setSent(logger.getSent() + 1);

                total = System.currentTimeMillis() - start;

                if (total >= 1000) {
                    logger.setRemaining(nrOfPackets - i - 1);
                    System.out.println(logger.toString());
                    output.close();
                    socket.close();
                    System.exit(0);
                }

                logger.setRemaining(nrOfPackets - i - 1);
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(logger);
        total = System.currentTimeMillis();

        /* Wait until the full second has passed */
        try {
            Thread.sleep(999 - (total - start));

            socket.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }
}
