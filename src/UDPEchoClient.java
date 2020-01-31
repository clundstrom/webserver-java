import java.net.*;

public class UDPEchoClient extends AbstractNetworkLayer {

    public UDPEchoClient(int myport, String message, int bufferSize, int transferRate, boolean debug){
        MYPORT = myport;
        MSG = message;
        BUFSIZE = bufferSize;
        TRANSFER_RATE = transferRate;
        DEBUG = debug;
    }

    public static void main(String[] args) {
        // Handle mandatory arguments
        if (args.length < 2) {
            System.err.println("Error: Specify arguments server_name port (buffer-size) (transfer-rate)");
            System.exit(1);
        }

        // Parse buffer-size
        if (args.length >= 3) {
            BUFSIZE = ArgParser.tryParse(args[2]);
            if (BUFSIZE < 1) {
                System.err.println("Buffer size not allowed. Exiting..");
                System.exit(1);
            }

            if (BUFSIZE < MSG.getBytes().length) {
                System.err.println("Warning: Buffer size less than message.");
            }
        }

        // Parse transfer rate
        if (args.length >= 4) {
            TRANSFER_RATE = ArgParser.tryParse(args[3]);
        }

        try {
            byte[] buf = ArgParser.parseToBuffer(MSG, BUFSIZE);

            // Create socket
            DatagramSocket socket = new DatagramSocket(null);

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
            UDPSendReceiveTask task = new UDPSendReceiveTask(socket, sendPacket, receivePacket, MSG, TRANSFER_RATE, logger);

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