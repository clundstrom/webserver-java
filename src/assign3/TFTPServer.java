package assign3;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

enum Result {
    DATA_SENT, DATA_RECEIVED, ACK_SENT, ACK_RECEIVED, ERR, STOP
}


public class TFTPServer {
    public static final int TFTPPORT = 69;
    public static final int BUFSIZE = 516;
    public static final String READDIR = "download/"; //custom address at your PC
    public static final String WRITEDIR = "upload/"; //custom address at your PC
    // OP codes
    public static final short OP_RRQ = 1;
    public static final short OP_WRQ = 2;
    public static final short OP_DAT = 3;
    public static final short OP_ACK = 4;
    public static final short OP_ERR = 5;

    private static int blockNumber = 0;
    private boolean receiving = true;

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
                    } catch (FileNotFoundException e) {
                        System.err.println("File not found.");
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
            // Open file and input stream
            File file = new File(requestedFile);
            InputStream is = new FileInputStream(file);

            // Allocate 512 byte buffer
            byte[] dataBuf = new byte[512];

            int readBytes = 0;
            int blockNum = 1;
            int lastBlock = 0;

            // Process bytes from stream
            while ((readBytes = is.read(dataBuf)) != -1) {
                int finalBlockNum = blockNum;
                lastBlock = readBytes;

                // Copy the amount of bytes read up to a maximum of 512
                byte[] sendData = Arrays.copyOfRange(dataBuf, 0, readBytes);

                // Start timeout state-handler for sending data and receiving acks
                if(!await(() -> sendData(sendSocket, sendData, finalBlockNum), () -> processAck(sendSocket, opcode, finalBlockNum), 1000)){
                    System.err.println("Connection timed out..");
                    return;
                }
                blockNum++;
            }

            // Send an extra empty packet to acknowledge end of transfer in EDGE CASE where packet size matches buffer size
            if (lastBlock % 512 == 0) {
                byte[] empty = new byte[0];
                sendData(sendSocket, empty, blockNum);
            }

        } else if (opcode == OP_WRQ) {
            // Open file and output stream
            File file = new File(requestedFile);
            OutputStream fos = new FileOutputStream(file);

            // Send initial ack to start data transfer
            processAck(sendSocket, opcode, blockNumber);

            while (receiving) {
                // Start timeout state-handler for sending data and receiving acks
                if(!await(() -> receiveData(sendSocket, fos), () -> processAck(sendSocket, opcode, blockNumber), 1000)){
                    System.err.println("Connection timed out..");
                    blockNumber = 0;
                    return;
                }
            }
            // Reset receiving and close output stream
            receiving = true;
            fos.close();
        } else {
            System.err.println("Invalid request. Sending an error packet.");
            //send_ERR(params);
            return;
        }
    }


    /**
     * Improvised state machine used as a timeout handler.
     * Uses Callable so functions can return results.
     * Handles:
     * <p>
     * SEND DATA - RECEIVE ACK
     * RECEIVE DATA - SEND ACK
     * <p>
     * @param dataAction  Response to be sent.
     * @param ackAction       Action to be awaited.
     * @param timeoutMillis Timeout in milliseconds.
     */
    public boolean await(Callable<Result> dataAction, Callable<Result> ackAction, int timeoutMillis) {
        try {
            // Start timer when sending
            long start = System.currentTimeMillis();
            final int MAX_TRIES = 3;
            int numTries = 0;

            // SEND / RECEIVE DATA
            dataAction.call();

            // Receive ack / SEND ACK
            Result res = ackAction.call();

            while (res == Result.ERR || System.currentTimeMillis() - start > timeoutMillis) {
                numTries++;
                System.out.println("Request timeout.." + numTries);

                if(numTries == MAX_TRIES) throw new TimeoutException();

                // Reset timer
                start = System.currentTimeMillis();

                // Send/receive DATA
                dataAction.call();

                // Receive/send ACK
                res = ackAction.call();
            }


            // Print results
            switch (res){
                case ACK_RECEIVED:
                    System.out.println("Ack received..");
                    break;
                case DATA_RECEIVED:
                    System.out.println("Data received..");
                    break;
                case DATA_SENT:
                    System.out.println("Data sent..");
                    break;
                default:
                    break;
            }

        } catch (TimeoutException e){
            return false;
        }
        catch (Exception e) {
            System.err.println("There was an error retrieving results from callable.");
            return false;
        }
        return true;
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
     * Handles sending of data to the socket.
     *
     * @param sendSocket Socket in use.
     * @param data       Data that is requested
     * @return result Result of action
     */
    Result sendData(DatagramSocket sendSocket, byte[] data, int blockNr) {
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

            if (data.length == 0) {
                System.out.println("Sending " + data.length + " bytes (edge case). Block num " + blockNr);
            } else {
                System.out.println("Sending " + data.length + " bytes. Block num " + blockNr);
            }
        } catch (IOException e) {
            System.err.println("Unexpected IO Error");
            return Result.ERR;
        }
        return Result.DATA_SENT;
    }


    /**
     * Function which receives data from socket.
     *
     * @param sendSocket
     * @return
     */
    Result receiveData(DatagramSocket sendSocket, OutputStream fos) {
        try {
            byte[] dataBuf = new byte[516]; // max data buffer

            // Create packet with buffer of 516 bytes ( 4 for header, 512 data)
            DatagramPacket receive = new DatagramPacket(dataBuf, dataBuf.length);
            sendSocket.receive(receive);

            // Copy date
            byte[] data = Arrays.copyOfRange(dataBuf, 4, receive.getLength());

            // Write data to stream
            fos.write(data);
            System.out.println("Receiving " + data.length + " bytes.");
            blockNumber++;

            // If data is less than 512 stop receiving-loop.
            if (data.length < 512) {
                receiving = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.DATA_RECEIVED;
    }


    /**
     * Send or receive ack depending on opcode.
     *
     * @param sendSocket Socket to use.
     * @param opcode     Opcode
     * @return Success/fail.
     */
    Result processAck(DatagramSocket sendSocket, short opcode, int block) {
        try {
            // Allocate bytes for header
            byte[] ackBuf = new byte[4];
            ByteBuffer ack = ByteBuffer.wrap(ackBuf);

            if (opcode == OP_RRQ) {
                DatagramPacket receive = new DatagramPacket(ackBuf, 4);
                sendSocket.receive(receive);
                System.out.println("Awaiting ack for block " + block);

                // Only return ack received if block numbers match
                if(ack.getShort(2) == block){
                    return Result.ACK_RECEIVED;
                }
            } else {
                ack.putShort(OP_ACK);
                ack.putShort((short) block);
                System.out.println("Sending ack for block " + block);
                sendSocket.send(new DatagramPacket(ackBuf, 4));
                return Result.ACK_SENT;
            }
        } catch (IOException e) {
            System.err.println("There was an error.");
            return Result.ERR;
        }
        return Result.ERR;
    }
}



