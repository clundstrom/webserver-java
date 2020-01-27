/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UDPEchoClient {
    public static final int BUFSIZE = 1024;
    public static final int MYPORT = 6000;
    public static int TRANSFER_RATE = 0;
    public static final String MSG = "An Echo Message!";


    /*
    Change the client and server so that you can specify the buffer size and a message transfer rate as command line parameters.
    The buffer size is given in bytes, and the transfer rate in messages per second. If the transfer rate is 0, the client should send a single message once.


    ip port buffsize transfer rate
     */
    public static void main(String[] args) throws IOException {
        byte[] buf = new byte[BUFSIZE];

        // Mandatory arguments
        if (args.length < 2) {
            System.err.printf("usage: %s server_name port\n", args[0]);
            System.exit(1);
        }

        // Parse buffer-size
        if (args.length == 3) {
            buf = new byte[tryParse(args[2])];
        }

        // Parse transfer rate
        if (args.length == 4) {
            TRANSFER_RATE = tryParse(args[3]);
        }

        /* Create socket */
        DatagramSocket socket = new DatagramSocket(null);

        /* Create local endpoint using bind() */
        SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
        socket.bind(localBindPoint);

        /* Create remote endpoint */
        SocketAddress remoteBindPoint =
                new InetSocketAddress(args[0],
                        Integer.parseInt(args[1]));

        /* Create datagram packet for sending message */
        DatagramPacket sendPacket = new DatagramPacket(MSG.getBytes(),
                MSG.length(),
                remoteBindPoint);

        /* Create datagram packet for receiving echoed message */
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);


        /* Create packet logger */
        Logger logger = new Logger();

        /* Send and receive message*/
        sendReceive(socket, sendPacket, receivePacket, MSG, logger);


        //socket.close();
    }

    private static int tryParse(String i) {
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException e) {
            System.err.print("There was an error parsing command line arguments.");
        }
        System.exit(1);
        return 0;
    }

    private static void sendReceive(DatagramSocket socket, DatagramPacket packet, DatagramPacket receive,  String message, Logger logger) {
        if (TRANSFER_RATE == 0) {
            new PacketTask(socket, packet, receive, message, 1, logger).run();
        } else {
            PacketTask task = new PacketTask(socket, packet, receive, message, 1, logger);
            ScheduledExecutorService es = new ScheduledThreadPoolExecutor(1);
            es.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);

        }
    }
}