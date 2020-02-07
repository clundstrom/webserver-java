For intelliJ setup

1. Mark src folder as source
2. Pick an output-folder
3. Set project SDK to Java 11.0.1

-------------------------------------------------

How to run CLIENTS:

1. Choose which client to use by comment out the other in the Init.java file.
2. Run Init class with arguments:   IP REMOTE_PORT BUFFER_SIZE TRANSFER_RATE

Example:
192.168.1.113 6000 1024 1

To change between UDP/TCP client: comment out TCP or UDP client in init main function.
-------------------------------------------------

How to run Server:
1. Local ports are set in UDPEchoServer and TCPEchoServer.
2. Simply run the TCPEchoServer or UDPEchoServer with specified buffer rate.



