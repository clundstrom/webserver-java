import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client {


    public static void main(String[] args) throws IOException {

        // Create socket
        Socket socket = new Socket();

        // Create local endpoint and bind to socket
        SocketAddress local = new InetSocketAddress(6000);
        socket.bind(local);

        // Create endpoint
        SocketAddress remote = new InetSocketAddress("192.168.1.113", 6000);

        socket.connect(remote, 100);

        InputStream is = socket.getInputStream();
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("Ping!");

        var bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println(bufferedReader.readLine());
    }

}
