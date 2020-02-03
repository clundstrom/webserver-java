For intelliJ setup

1. Mark src folder as source
2. Pick an out-folder
3. Set project SDK to Java 11.0.1


How to run CLIENTS:

Run Init class with arguments:

IP PORT BUFFERSIZE TRANSFERRATE

Example:
192.168.1.113 6000 1024 1

To change between UDP/TCP client: comment out TCP or UDP client in init main function.


How to run Server:

Simply run the TCPEchoServer or UDPEchoServer with previously mentioned arguments.




