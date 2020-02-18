package assign2;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class ServeTask implements Runnable {

    private Socket socket;
    private int buffSize;

    public ServeTask(Socket socket, int buffSize) {
        this.socket = socket;
        this.buffSize = buffSize;
    }
    String response = "";
    @Override
    public void run() {
        try {
            // Create buffer
            var buf = new byte[buffSize];

            InputStream is = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Read input
            int read;
            String incomingHeader = "";
            // Read get Request
            while ((read = is.read(buf)) != -1) {
                // Add to get request
                incomingHeader += new String(buf, 0, read);

                String[] info = ArgParser.getStaticContent(incomingHeader);
                byte[] data = composeData(Paths.get(info[0]));
                composeResponse(data, info[1]);
                out.write(response.getBytes());
                out.write(data);
            }

            out.flush();
            out.close();

        } catch (SocketException e) {
            System.err.println("Connection reset by client. Terminating thread " + Thread.currentThread().getName());
        } catch (IOException e) {
            System.err.println("There was an error writing or reading from the stream.");
        } finally {
            Thread.currentThread().interrupt();
        }
    }

    private byte[] composeData(Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            return data;
        }
        catch (IOException e){
            System.err.println("There was an error");
            // call 404
        }
        catch (NullPointerException e){
            System.err.println("Could not find specified path.");
            // call 404
        }
        return null;
    }


    void composeResponse(byte[] data, String contentType){
        int contentLength = 0;

        if(data != null){
            contentLength = data.length;
        }

        response += "HTTP/1.1 200 OK\n";
        response += "Server: Webservice\n";
        response += "Date:" + LocalDateTime.now() + "\n";
        response += "Content-Length:" + contentLength + "\n";
        response += "Connection: close \n";
        response += "Content-Type:" + contentType +"\n";
        response += "\r\n";
    }
}
