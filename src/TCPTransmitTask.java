import java.io.*;
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
    StringBuilder responseString = new StringBuilder();

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
        long total = processMessages(start);




        try {
            receiveMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Wait until the full second has passed before terminating thread*/
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    private void receiveMessages() throws IOException {
        byte[] buff = new byte[buffSize];
        int read;
        StringBuilder sr = new StringBuilder();
        InputStream in = this.socket.getInputStream();
        while((read = in.read(buff)) != -1){
            System.out.println((new String(buff, 0, read)));
            read = in.read(buff);
        }
    }

    long processMessages(long start) {
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

                // check input
                String str;
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
        return total;
    }
}
