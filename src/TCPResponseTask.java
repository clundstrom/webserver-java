import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
            socket.setKeepAlive(true);
            // Make sure a buffer size is specified
            if (buffSize == 0) {
                this.buffSize = socket.getReceiveBufferSize();
            }
            var buf = new byte[buffSize];

            // Read input
            InputStream is = socket.getInputStream();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            int read;
            while((read = is.read(buf)) != -1){
                out.println(new String(buf, 0, read));
                System.out.println("Response: " + new String(buf, 0, read));
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
