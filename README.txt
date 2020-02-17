For intelliJ setup

1. Mark src folder as source
2. Pick an output-folder
3. Set project SDK to Java 11.0.1

-------------------------------------------------

How to run CLIENTS:

1. Choose which client to use by comment out the other in the assign1.MainClient.java file.
2. Run assign1.MainClient class with arguments:   IP REMOTE_PORT BUFFER_SIZE TRANSFER_RATE

Example:
192.168.1.113 6000 1024 1

To change between UDP/TCP client: comment out TCP or UDP client in main function.
-------------------------------------------------

How to run Server:
1. Local ports are set in assign1.UDPEchoServer and assign1.TCPEchoServer.
2. Simply run the assign1.TCPEchoServer or assign1.UDPEchoServer with specified buffer rate.



