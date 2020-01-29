import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPResponseTask implements Runnable {

    private final int buffSize;
    private Socket socket;

    public TCPResponseTask(Socket socket, int buffSize) {
        this.socket = socket;
        this.buffSize = buffSize;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long total;
        try {

            // Read input
            InputStream is = socket.getInputStream();
            byte[] buf = new byte[buffSize];

            // Read into buffer
            int bytesRead = is.read(buf);
            System.out.println("Message received: " + new String(buf, 0 , bytesRead));

            // Create a Writer to the output-stream
            PrintWriter output = new PrintWriter(socket.getOutputStream());


            // Write to output while buffer is not empty

            while(bytesRead != -1){
                output.println(buf);
                bytesRead = is.read(buf);
            }

            // Keep track of time spent
            total = System.currentTimeMillis() - start;

            // If the time exceeds 1 second abort immediately
            if (total >= 1000) {
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        total = System.currentTimeMillis();

        /* Wait until the full second has passed */
        try {
            Thread.sleep(999 - (total - start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
