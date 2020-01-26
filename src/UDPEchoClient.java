/*
  UDPEchoClient.java
  A simple echo client with no error handling
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPEchoClient {
    public static final int BUFSIZE= 1024;
    public static final int MYPORT= 0;
    public static final String MSG= "An Echo Message!";

    public static void main(String[] args) throws IOException {
	byte[] buf= new byte[BUFSIZE];
	if (args.length != 2) {
	    System.err.printf("usage: %s server_name port\n", args[1]);
	    System.exit(1);
	}
	
	/* Create socket */
	DatagramSocket socket= new DatagramSocket(null);
	
	/* Create local endpoint using bind() */
	SocketAddress localBindPoint= new InetSocketAddress(MYPORT);
	socket.bind(localBindPoint);
	
	/* Create remote endpoint */
	SocketAddress remoteBindPoint=
	    new InetSocketAddress(args[0],
				  Integer.valueOf(args[1]));
	
	/* Create datagram packet for sending message */
	DatagramPacket sendPacket=
	    new DatagramPacket(MSG.getBytes(),
			       MSG.length(),
			       remoteBindPoint);
	
	/* Create datagram packet for receiving echoed message */
	DatagramPacket receivePacket= new DatagramPacket(buf, buf.length);
	
	/* Send and receive message*/
	socket.send(sendPacket);
	socket.receive(receivePacket);
	
	/* Compare sent and received message */
	String receivedString=
	    new String(receivePacket.getData(),
		       receivePacket.getOffset(),
		       receivePacket.getLength());
	if (receivedString.compareTo(MSG) == 0)
	    System.out.printf("%d bytes sent and received\n", receivePacket.getLength());
	else
	    System.out.printf("Sent and received msg not equal!\n");
	socket.close();
    }
}