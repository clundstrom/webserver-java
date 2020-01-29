import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class TCPResponseTask implements Runnable {

    private Socket socket;
    private int buffSize;

    public TCPResponseTask(Socket socket, int buffSize) {
        this.socket = socket;
        this.buffSize = buffSize;
    }

    public TCPResponseTask(Socket socket) {
        this.socket = socket;
        this.buffSize = 0;
    }

    @Override
    public void run() {
        try {

            // Make sure a buffer size is specified
            if (buffSize == 0) {
                this.buffSize = socket.getReceiveBufferSize();
            }

            // Read input
            InputStream is = socket.getInputStream();

            // Create buffer
            byte[] buf = new byte[buffSize];

            // Read input
            int bytesRead = is.read(buf);

            // Create output-stream
            OutputStream os = socket.getOutputStream();

            // Write to output as long as there are bytes to read
            while (bytesRead != -1) {
                os.write(buf, 0, bytesRead);
                bytesRead = is.read(buf);
            }

        } catch (SocketException e) {
            System.err.println("Connection reset by client. Terminating thread.");
        } catch (IOException e) {
            System.err.println("There was an error writing or reading from the stream.");
        }
        finally {
            Thread.currentThread().interrupt();
        }
    }
}
