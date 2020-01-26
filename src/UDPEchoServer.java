/*
  UDPEchoServer.java
  A simple echo server with no error handling
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPEchoServer {
    public static final int BUFSIZE = 1024;
    public static final int MYPORT = 6000;
    public static int TRANSFER_RATE = 0;

    public static void main(String[] args) throws IOException {
        byte[] buf = new byte[BUFSIZE];

        // Parse buffer-size
        if (args.length == 3) {
            buf = new byte[tryParse(args[0])];
        }

        // Parse transfer rate
        if (args.length == 4) {
            TRANSFER_RATE = tryParse(args[1]);
        }


        /* Create socket */
        DatagramSocket socket = new DatagramSocket(null);

        /* Create local bind point */
        SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
        socket.bind(localBindPoint);
        while (true) {
            /* Create datagram packet for receiving message */
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

            /* Receiving message */
            socket.receive(receivePacket);

            /* Create datagram packet for sending message */
            DatagramPacket sendPacket =
                    new DatagramPacket(receivePacket.getData(),
                            receivePacket.getLength(),
                            receivePacket.getAddress(),
                            receivePacket.getPort());

            /* Send message*/
            socket.send(sendPacket);
            System.out.printf("UDP echo request from %s", receivePacket.getAddress().getHostAddress());
            System.out.printf(" using port %d\n", receivePacket.getPort());
        }
    }

    private static int tryParse(String i){
        try{
            return Integer.parseInt(i);
        }
        catch (NumberFormatException e){
            System.err.print("There was an error parsing command line arguments.");
        }
        System.exit(1);
        return 0;
    }
}