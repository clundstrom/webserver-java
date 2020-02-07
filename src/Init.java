import java.net.DatagramSocket;
import java.net.Socket;


/**
 * Main class to run each client.
 *
 * Arguments needed to run: IP PORT BUFFER TRANSFER RATE
 */
public class Init {

    // Enabling debug will show a live feed of sent and received packets and their size. Not suitable for high rates.
    public static boolean DEBUG = true;

    public static void main(String[] args) {
        AbstractNetworkLayer<Socket> tcp = new TCPEchoClient(args, DEBUG); // Comment out this or the one below!
        //AbstractNetworkLayer<DatagramSocket> udp = new UDPEchoClient(args, DEBUG);
    }

}
