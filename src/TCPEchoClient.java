import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPEchoClient {

    public static final int MYPORT = 6000;
    public static final String MSG = "Ping!";
    public static int BUFSIZE = 1024;


    public static void main(String[] args) {
        // Handle mandatory arguments
        if (args.length < 2) {
            System.err.println("Error: Specify arguments server_name port (buffer-size) (transfer-rate) ");
            System.exit(1);
        }

        // Parse buffer-size
        if (args.length >= 3) {
            BUFSIZE = ArgParser.tryParse(args[2]);
        }


        try {
            byte[] buf = new byte[BUFSIZE];

            // Create socket
            Socket socket = new Socket();

            // Create local endpoint and bind to socket
            SocketAddress local = new InetSocketAddress(MYPORT);
            socket.bind(local);

            // Create endpoint
            SocketAddress remote = new InetSocketAddress(args[0], Integer.parseInt(args[1]));

            // Create packet


            // Connect to remote, set conn timeout to 10sec
            socket.connect(remote, 10);
            // Open input stream
            InputStream is = socket.getInputStream();

            // Read results
            int res = is.read(buf);

            // Print results
            System.out.println(new String(buf, 0, res));
            socket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
