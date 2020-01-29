import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    @Override
    public void run() {
        try {
            // Read input
            InputStream is = socket.getInputStream();

            byte[] buffer = new byte[buffSize];
            int len = is.read(buffer);

            OutputStream pw = socket.getOutputStream();

            while (len != -1) {
                pw.write(buffer, 0, len);
                len = is.read(buffer);
            }
            // Transfer input to output
            //is.transferTo(socket.getOutputStream());


        } catch (SocketException e) {
            System.err.println("Connection reset by client.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
