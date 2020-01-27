import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPRespondTask implements Runnable {

    private Socket socket;
    private PrintWriter output;
    private String message;

    public TCPRespondTask(Socket socket, String message) throws IOException {
        this.socket = socket;
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.message = message;
    }


    @Override
    public void run() {
        try {
            respond(this.message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void respond(String message) throws IOException {
        output.println(message);
        output.close();
        socket.close();
    }
}
