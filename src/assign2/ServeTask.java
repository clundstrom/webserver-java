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
    private String[] protectedRoutes = {"protected.html"};
    private byte[] data;
    private String defaultPassword = "1dv701";

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
                HttpResponse hr = new HttpResponse();

                // Add to get request
                incomingHeader += new String(buf, 0, read);

                // Fetch information about the content requested
                String[] info = ArgParser.getStaticContentInfo(incomingHeader, defaultPath);

                // Verify that content exists
                if(isContent(Paths.get(info[0]))){
                    data = composeData(Paths.get(info[0]));
                    hr = new HttpResponse("200 OK", data.length ,info[1]);
                }
                else{
                    hr = new HttpResponse("404 not found", 0, "text/html");
                }

                // Verify that content is not protected
                if(isRouteProtected(info)){
                    hr = new HttpResponse("403 forbidden",0 , "text/html");
                }

                // Verify that password is correct
                if(!defaultPassword.equals(info[2])){
                    hr.setStatusCode("200 OK");
                }


                out.write(hr.build());

                if(data != null)
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

    private boolean isContent(Path info) {
        return info.toFile().isFile();
    }

    /**
     * Verifies whether the user is accessing protected data.
     * @param info
     * @return
     */
    private boolean isRouteProtected(String[] info) {
        for (String i : protectedRoutes) {
            if (i.equalsIgnoreCase(info[0] + info[1])) {
                return true;
            }
        }
        return false;
    }

    private byte[] composeData(Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            return data;
        } catch (IOException e) {
            System.err.println("There was an error");
            // call 500
        } catch (NullPointerException e) {
            System.err.println("Could not find specified path.");
        }
        return null;
    }
}
