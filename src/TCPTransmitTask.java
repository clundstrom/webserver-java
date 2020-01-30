import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;


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
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            StringBuilder sr = new StringBuilder();

            // Process messages
            for (int i = 0; i < nrOfPackets; i++) {
                output.println(message);
                logger.setSent(logger.getSent() + 1);
            }
                // Write to output
                //output.println(sr.toString());
                output.flush();

                // Shutdown output to allow for input
                socket.shutdownOutput();




                // Open input socket
                InputStream in = socket.getInputStream();

                int read;
                while((read = in.read(buf)) != -1){
                    System.out.println((new String(buf, 0, read)));
                    read = in.read(buf);
                }
                socket.shutdownInput();

                logger.setTotalReceived(logger.getTotalReceived() + 1);

                // Keep track of time spent
                total = System.currentTimeMillis() - start;

                // If the time exceeds 1 second abort immediately
                if (total >= 999) {
                    System.out.println(logger.toString());
                    System.exit(0);
                    Thread.currentThread().interrupt();
                }

            System.out.println(logger);

            // Close streams and socket
            output.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        /* Wait until the full second has passed before terminating thread*/
        total = System.currentTimeMillis();
        try {
            Thread.sleep(1000 - (total - start));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

}
