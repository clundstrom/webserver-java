import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {


    public static void main(String[] args) throws IOException, InterruptedException {
        // Create server socket and bind to port.
        ServerSocket serverSocket = new ServerSocket(6000);
        byte[] buff = new byte[1024];


        while(true){

            Socket incoming = serverSocket.accept();
            System.out.println("Creating thread for incoming request: " + incoming.getInetAddress() + " " + incoming.getPort());

            InputStream in = incoming.getInputStream();
            int read;

            StringBuilder sr = new StringBuilder();
            while((read = in.read(buff)) != -1){
                sr.append(new String(buff, 0, read));
                read = in.read(buff);
            }
            incoming.shutdownInput();

            System.out.println("Responding to client: ");
            System.out.println(sr.toString());
            System.out.println("End of stream.");


            PrintWriter pw = new PrintWriter(incoming.getOutputStream());
            pw.println(sr.toString());


            pw.close();
            in.close();
            incoming.close();
        }
    }
}