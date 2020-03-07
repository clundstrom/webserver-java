package assign3;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class TFTPServer {
    public static final int TFTPPORT = 69;
    public static final int BUFSIZE = 516;
    public static final String READDIR = "static/"; //custom address at your PC
    public static final String WRITEDIR = "static/"; //custom address at your PC
    // OP codes
    public static final short OP_RRQ = 1;
    public static final short OP_WRQ = 2;
    public static final short OP_DAT = 3;
    public static final short OP_ACK = 4;
    public static final short OP_ERR = 5;

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
            System.err.println("Unexpected error on Socket.");
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
            final int reqType = ParseRQ(buf, requestedFile);

            new Thread() {
                public void run() {
                    try {
                        DatagramSocket sendSocket = new DatagramSocket(0);

                        // Connect to client
                        sendSocket.connect(clientAddress);

                        System.out.printf("%s request for %s from %s using port %d \n",
                                (reqType == OP_RRQ) ? "Read" : "Write", requestedFile.toString(), clientAddress.getHostName(), clientAddress.getPort());

                        // Read request
                        if (reqType == OP_RRQ) {
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
                        System.err.println("Unexpected error in socket. Is port already bound?");
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
        DatagramPacket incoming = new DatagramPacket(buf, buf.length);

        // Receive packet
        try {
            socket.receive(incoming);
        } catch (IOException e) {
            System.err.println("There was an error receiving packets.");
        }

        // Return address of client
        return new InetSocketAddress(incoming.getAddress(), incoming.getPort());
    }

    /**
     * Parses the request in buf to retrieve the type of request and requestedFile
     *
     * @param buf           (received request)
     * @param requestedFile (name of file to read/write)
     * @return opcode (request type: RRQ or WRQ)
     */
    private int ParseRQ(byte[] buf, StringBuffer requestedFile) {
        requestedFile.append(parseToNullTermination(buf, 2, '\0'));
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
    private void HandleRQ(DatagramSocket sendSocket, String requestedFile, short opcode) throws IOException {
        if (opcode == OP_RRQ) {

            byte[] data = Files.readAllBytes(Paths.get(requestedFile));

            var blockList = blockify(data, 512);

            for(int i =0; i < blockList.size(); i++){
                sendData(sendSocket, blockList.get(i), opcode);
                processAck(sendSocket, opcode, i);
            }

            // Wait for Ack
            // ackReceived false?
            long wait = System.currentTimeMillis();
            boolean ackReceived = processAck(sendSocket, opcode, 1);


        } else if (opcode == OP_WRQ) {

            // Ack + Receive
            processAck(sendSocket, opcode, 1);
            receiveData(sendSocket);

        } else {
            System.err.println("Invalid request. Sending an error packet.");
            //send_ERR(params);
            return;
        }
    }

    public ArrayList<byte[]> blockify(byte[] data, int blockSize){
        ArrayList<byte[]> blockList = new ArrayList<>();
        int numBlocks = (data.length / blockSize);
        int lastBlockSize = data.length % blockSize;


        if(data.length <= blockSize){
            blockList.add(data);
            return blockList;
        }

        for(int i =0; i < numBlocks; i++){
            blockList.add(Arrays.copyOfRange(data,i*blockSize, (i+1)*blockSize));
        }
        blockList.add(Arrays.copyOfRange(data,numBlocks*512, numBlocks*512+lastBlockSize));
        return blockList;
    }


    /**
     * Parses a byte array until terminator char is found.
     *
     * @param buf    Byte array to parse.
     * @param offset Start with an offset.
     * @return A parsed String.
     */
    String parseToNullTermination(byte[] buf, int offset, char terminator) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < buf.length; i++) {
            if ((char) buf[i] == terminator) break;
            sb.append((char) buf[i]);
        }
        return sb.toString();
    }


    /**
     * Function which parses data. Either it receives or sends data from a socket.
     *
     * @param sendSocket    socket to send and receive from
     * @param data Data that is requested
     * @return result
     */
    boolean sendData(DatagramSocket sendSocket, byte[] data, int blockNr) {
        try {
            // Allocate buffer for data length + 4 byte header
            ByteBuffer bb = ByteBuffer.allocate(data.length + 4);

            // Put OPCODE
            bb.putShort(OP_DAT);

            // Put BLOCK nr
            bb.putShort((short) blockNr);

            // Read data to byte buffer
            bb.put(data);

            byte[] embed = bb.array();

            DatagramPacket send = new DatagramPacket(embed, embed.length);
            sendSocket.send(send);

        } catch (FileNotFoundException e) {
            System.err.println("File not found.");
            return false;
        } catch (IOException e) {
            System.err.println("Unexpected IO Error");
            return false;
        }
        return true;
    }

    boolean receiveData(DatagramSocket sendSocket) {
        try {
            // Receive data
            byte[] dataBuf = new byte[1]; // max data buffer

            byte[] b_ack = new byte[4];
            var buff = ByteBuffer.wrap(b_ack);
            buff.putShort((short) 4);
            buff.putShort((short) 1);
            DatagramPacket ack = new DatagramPacket(buff.array(), buff.array().length);
            sendSocket.send(ack);

            // dont know size of incoming packet
            DatagramPacket receive = new DatagramPacket(dataBuf, dataBuf.length);
            sendSocket.receive(receive);

            byte[] incoming = receive.getData();

            System.out.println(incoming.length);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Send or receive ack depending on opcode.
     *
     * @param sendSocket Socket to use.
     * @param opcode     Opcode
     * @return Success/fail.
     * @throws IOException
     */
    boolean processAck(DatagramSocket sendSocket, short opcode, int block) throws IOException {
        byte[] ackBuf = new byte[4];
        ByteBuffer ack = ByteBuffer.wrap(ackBuf);

        if (opcode == OP_RRQ) {
            DatagramPacket receive = new DatagramPacket(ackBuf, 4);
            // Await response
            sendSocket.receive(receive);

            // TODO: Edge case if payload is exactly 512 bytes

            short op = ack.getShort(0);
            short numBlock = ack.getShort(2);
            return true;
        } else {

            ack.putShort(OP_ACK);
            ack.putShort((short) block);


        }
        return true;
    }
}



