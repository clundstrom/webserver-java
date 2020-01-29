import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {


    public static void main(String[] args) throws IOException {
        // Create server socket and bind to port.
        ServerSocket socket = new ServerSocket(6000);



        while(true){

            Socket incoming = socket.accept();
            System.out.println("Creating thread for incoming request: " + incoming.getInetAddress() + " " + incoming.getPort());

            // Transer input stream to output stream
            incoming.getInputStream().transferTo(incoming.getOutputStream());


        }



    }
}
