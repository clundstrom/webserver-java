import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class TCPResponseTask implements Runnable {

    private final int buffSize;
    private Socket socket;

    public TCPResponseTask(Socket socket, int buffSize) {
        this.socket = socket;
        this.buffSize = buffSize;
    }

    @Override
    public void run() {
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
            while(bytesRead != 0){
                output.println(buf);
                bytesRead = is.read(buf);
            }

        } catch (Exception e) {
            System.err.println("Connection reset by client.");
        }
    }

}
