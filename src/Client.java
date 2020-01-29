import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client {


    public static void main(String[] args) throws IOException {
        byte[] buff = new byte[1024];

        // Create socket
        Socket socket = new Socket();

        // Create local endpoint and bind to socket
        SocketAddress local = new InetSocketAddress(6000);
        socket.bind(local);

        // Create endpoint
        SocketAddress remote = new InetSocketAddress("192.168.1.113", 6000);

        socket.connect(remote, 100);

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        for(int i=0; i < 10; i++){
            pw.println("Ping!");
        }

        int read;

        InputStream in = socket.getInputStream();

        while((read = in.read(buff)) != -1){
            System.out.println(new String(buff, 0, read));
            read = in.read(buff);
        }

        pw.close();
        in.close();
        socket.close();
    }

}
