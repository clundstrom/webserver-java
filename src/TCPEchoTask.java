import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class TCPEchoTask implements Runnable {

    private Socket socket;
    private int buffSize;

    public TCPEchoTask(Socket socket, int buffSize) {
        this.socket = socket;
        this.buffSize = buffSize;
    }


    public TCPEchoTask(Socket socket) {
        this.socket = socket;
        this.buffSize = 0;
    }


    @Override
    public void run() {
        try {

            // Make sure a buffer size is specified
            if (buffSize <= 0) {
                this.buffSize = socket.getReceiveBufferSize();
            }

            // Create buffer
            var buf = new byte[buffSize];

            InputStream is = socket.getInputStream();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Read input
            int read;
            while((read = is.read(buf)) != -1){
                // Echo back what is read
                out.println(new String(buf, 0,read));
            }
            out.flush();

        } catch (SocketException e) {
            System.err.println("Connection reset by client. Terminating thread " + Thread.currentThread().getName());
        } catch (IOException e) {
            System.err.println("There was an error writing or reading from the stream.");
        }
        finally {
            Thread.currentThread().interrupt();
        }
    }
}
