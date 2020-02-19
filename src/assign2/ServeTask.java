package assign2;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServeTask implements Runnable {

    private Socket socket;
    private int buffSize = 1024;
    private String defaultPath = "";

    public ServeTask(Socket socket, String path) {
        this.socket = socket;
        this.defaultPath = path;
    }


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

                String[] info = ArgParser.getStaticContentInfo(incomingHeader, defaultPath);
                byte[] data = composeData(Paths.get(info[0]));

                HttpResponse hr = new HttpResponse();
                hr.setStatusCode("200 OK");
                hr.setContentLength(data.length);
                hr.setContentType(info[1]);
                out.write(hr.composeResponse());
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
}
