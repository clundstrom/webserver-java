import java.net.*;

public class UDPEchoClient extends AbstractNetworkLayer<DatagramSocket> {

    public UDPEchoClient(String[] args, boolean debug) {
        DEBUG = debug;
        verifyArguments(args);
        initialize(args);
    }

    public UDPEchoClient(String[] args) {
        verifyArguments(args);
        initialize(args);
    }

    public void initialize(String[] args) {
        try {
            byte[] buf = ArgParser.parseToBuffer(MSG, BUFSIZE);

            // Create UDP socket
            socket = new DatagramSocket(null);

            // Create local endpoint using bind()
            SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
            socket.bind(localBindPoint);

            // Create remote endpoint
            SocketAddress remoteBindPoint = new InetSocketAddress(args[0], ArgParser.tryParse(args[1]));

            // Create datagram packet for sending message
            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, remoteBindPoint);

            // Create datagram packet for receiving echoed message
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

            // Create packet logger
            Logger logger = new Logger(DEBUG);

            // Create packet-task
            UDPTransmitTask task = new UDPTransmitTask(socket, sendPacket, receivePacket, TRANSFER_RATE, logger);

            // Send and receive message
            scheduleTask(task);

        } catch (BindException e) {
            System.err.println("Could not bind to port " + MYPORT);
        } catch (NullPointerException e) {
            System.err.println("Address can not be null.");
        }
        catch (IllegalArgumentException e) {
            System.err.println("Illegal argument. Is IP correctly formatted? Allowed port range 0-65535");
        }
        catch (SocketException e){
            System.err.println("There was an error establishing a connection.");
            System.exit(1);
        }
    }
}