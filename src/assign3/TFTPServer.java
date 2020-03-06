package assign3;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class TFTPServer {
    public static final int TFTPPORT = 69;
    public static final int BUFSIZE = 516;
    public static final String READDIR = "static/"; //custom address at your PC
    public static final String WRITEDIR = "static/"; //custom address at your PC
    // OP codes
    public static final short OP_RRQ = 1; // read request
    public static final short OP_WRQ = 2; // write request
    public static final short OP_DAT = 3; // data
    public static final short OP_ACK = 4; // ack
    public static final short OP_ERR = 5; // error

    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
            System.exit(1);
        }
        //Starting the server
        try {
            TFTPServer server = new TFTPServer();
            server.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void start() throws SocketException {
        byte[] buf = new byte[BUFSIZE];

        // Create socket
        DatagramSocket socket = new DatagramSocket(null);

        // Create local bind point
        SocketAddress localBindPoint = new InetSocketAddress(TFTPPORT);
        socket.bind(localBindPoint);

        System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

        // Loop to handle client requests
        while (true) {

            // Get address and read packet into buffer
            final InetSocketAddress clientAddress = receiveFrom(socket, buf);

            // If clientAddress is null, an error occurred in receiveFrom()
            if (clientAddress == null)
                continue;

            //
            final StringBuffer requestedFile = new StringBuffer();
            final int reqtype = ParseRQ(buf, requestedFile);

            new Thread() {
                public void run() {
                    try {
                        DatagramSocket sendSocket = new DatagramSocket(0);

                        // Connect to client
                        sendSocket.connect(clientAddress);

                        System.out.printf("%s request for %s from %s using port %d \n",
                                (reqtype == OP_RRQ) ? "Read" : "Write", requestedFile.toString(), clientAddress.getHostName(), clientAddress.getPort());

                        // Read request
                        if (reqtype == OP_RRQ) {
                            requestedFile.insert(0, READDIR);
                            HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);
                        }
                        // Write request
                        else {
                            requestedFile.insert(0, WRITEDIR);
                            HandleRQ(sendSocket, requestedFile.toString(), OP_WRQ);
                        }
                        sendSocket.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.err.println("There was an error reading or writing to file.");
                    }
                }
            }.start();
        }
    }

    /**
     * Reads the first block of data, i.e., the request for an action (read or write).
     *
     * @param socket (socket to read from)
     * @param buf    (where to store the read data)
     * @return socketAddress (the socket address of the client)
     */
    private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) {
        // Create datagram packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        // Receive packet
        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.err.println("There was an error receiving packets.");
        }

        // Get address of client
        InetSocketAddress addr = new InetSocketAddress(socket.getInetAddress(), 69);
        return addr;
    }

    /**
     * Parses the request in buf to retrieve the type of request and requestedFile
     *
     * @param buf           (received request)
     * @param requestedFile (name of file to read/write)
     * @return opcode (request type: RRQ or WRQ)
     */
    private int ParseRQ(byte[] buf, StringBuffer requestedFile) {
        requestedFile.append(parseToNullTermination(buf, 2));

        // Get short from request
        ByteBuffer wrap = ByteBuffer.wrap(buf);
        short opcode = wrap.getShort();

        return opcode;
    }

    /**
     * Handles RRQ and WRQ requests
     *
     * @param sendSocket    (socket used to send/receive packets)
     * @param requestedFile (name of file to read/write)
     * @param opcode        (RRQ or WRQ)
     */
    private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode) throws IOException {
        if (opcode == OP_RRQ) {
            // See "TFTP Formats" in TFTP specification for the DATA and ACK packet contents
            //boolean result = send_DATA_receive_ACK(params);
            //sendFile(socket, file);
            File file = new File(requestedFile);

            // Create packet - set block number and opcode_data

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fi = new FileInputStream(file);


            // Read all bytes
            baos.write(fi.readAllBytes());


            byte[] test = new byte[12];

            ByteBuffer bb = ByteBuffer.allocate(512);

            byte[] data = baos.toByteArray();

            // Calculate
            int numBlocks = (data.length / 512) + 1;
            int remainingBlock = data.length % 512;

            //for(int i= 1; i < numBlocks; i++){

            // Put OPCODE
            bb.putShort(OP_DAT);

            // Put BLOCK nr
            bb.putShort((short) 1);

            // Read data to Bytebuffer
            bb.put(baos.toByteArray());

            byte[] embed = bb.array();


            SocketAddress remote = new InetSocketAddress(sendSocket.getLocalAddress(), sendSocket.getPort());
            DatagramPacket send = new DatagramPacket(embed, embed.length, remote);

            // Send packet
            System.out.println("Port: " + sendSocket.getPort());
            System.out.println(sendSocket.getLocalPort());

            sendSocket.send(send);


            // Ack is 4 bytes
            byte[] ackBuf = new byte[4];
            DatagramPacket receive = new DatagramPacket(ackBuf, 4);
            // Await response
            sendSocket.receive(receive);
            //}

            awaitAck();
        } else if (opcode == OP_WRQ) {
            //boolean result = receive_DATA_send_ACK(params);
        } else {
            System.err.println("Invalid request. Sending an error packet.");
            // See "TFTP Formats" in TFTP specification for the ERROR packet contents
            //send_ERR(params);
            return;
        }
    }

    /**
     * To be implemented
     */
//    private boolean send_DATA_receive_ACK(params) {
//        return true;
//    }
//
//    private boolean receive_DATA_send_ACK(params) {
//        return true;
//    }
//
//    private void send_ERR(params) {
//    }


    String parseToNullTermination(byte[] buf, int offset) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < buf.length; i++) {
            if ((char) buf[i] == '\0') break;

            sb.append((char) buf[i]);
        }
        return sb.toString();
    }

    boolean sendFile(DatagramSocket dest, String file) throws IOException {
        //DatagramPacket packet = new DatagramPacket(null);
        // Read file to buffer

        // Send buffer as packet


        //dest.send(packet);
        return true;
    }

    boolean awaitAck() {

        return true;
    }
}



