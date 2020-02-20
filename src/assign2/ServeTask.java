package assign2;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ServeTask implements Runnable {

    private Socket socket;
    private int buffSize = 1024;
    private String defaultPath;
    private String[] protectedRoutes = {
            "protected.html"
    };

    private Map<String, String> redirectedRoutes = new HashMap<>();
    private byte[] data;
    private String defaultPassword = "1dv701";
    private OutputStream out;
    private HttpResponse hr;

    public ServeTask(Socket socket, String path) {
        this.socket = socket;
        this.defaultPath = path;
    }


    @Override
    public void run() {
        redirectedRoutes.put("bee.png", "a/b/bee.png");
        // todo: fix infinite /////// to path
        try {
            // Create buffer
            var buf = new byte[buffSize];

            InputStream is = socket.getInputStream();
            out = socket.getOutputStream();

            // Read input
            int read;
            String incomingHeader = "";

            // Read request
            while ((read = is.read(buf)) != -1) {

                // Read incoming header
                incomingHeader += new String(buf, 0, read);

                // Fetch information about the content requested
                String[] info = ArgParser.getStaticContentInfo(incomingHeader, defaultPath);

                assert info != null;
                switch (info[3]){
                    case "GET": 
                        ProcessGet(info);
                    case "PUT":
                        ProcessPost();
                    case "POST":
                        ProcessPut();
                }
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

    private void ProcessPut() {

    }

    private void ProcessPost() {
        
    }


    /**
     * Processing steps of Get Request
     * @param info Header information needed.
     * @throws IOException Handle IOException in caller.
     */
    private void ProcessGet(String[] info) throws IOException {
        // Verify that content exists
        isContent(info);

        // Verify that content is not protected
        isRouteProtected(info);

        // Verify that content is not moved
        isContentMoved(info);

        // Write header to stream
        out.write(hr.build());

        // Write data to stream
        if (data != null)
            out.write(data);
    }

    /**
     * Returns a URL to the new content if it has been moved.
     *
     * @param info Incoming url
     * @return Outgoing url
     */
    private boolean isContentMoved(String[] info) {
        if (info != null) {
            for (Map.Entry i : redirectedRoutes.entrySet()) {
                String compare = defaultPath + "/" + i.getKey();
                if (info[0].equalsIgnoreCase(compare)) {
                    hr = new HttpResponse("302 found", 0, info[1]);
                    hr.extras = new String[]{"Location: " + i.getValue().toString()};
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifies that content actually exists.
     * @param info
     * @return
     */
    private boolean isContent(String[] info) {
        try {
            boolean isValid = Paths.get(info[0]).toFile().isFile();

            // If path is a valid file
            if(isValid){
                data = composeData(info);
                if(data != null){
                    hr = new HttpResponse("200 OK", data.length, info[1]);
                }
            }
            else{
                hr = new HttpResponse("404 not found", 0, "text/html");
            }
            return isValid;
        } catch (InvalidPathException e) {
            System.err.println("Could not process path. Return 404.");

        } catch (NullPointerException e) {
            System.err.println("No specified path. Return 404.");
            hr = new HttpResponse("404 not found", 0, "text/html");
        }
        return false;
    }


    /**
     * Verifies whether the user is accessing protected data.
     *
     * @param info
     * @return
     */
    private boolean isRouteProtected(String[] info) {
        for (String i : protectedRoutes) {
            String compare = defaultPath + "/" + i;
            if (compare.equalsIgnoreCase(info[0])) {
                if (defaultPassword.equals(info[2])) {
                    return false;
                }
                hr = new HttpResponse("403 forbidden", 0, "text/html");
                return true;
            }
        }
        return false;
    }


    /**
     * Prepares outgoing data by reading it into a byte array.
     *
     * @param path Path of the data.
     * @return A byte array.
     */
    private byte[] composeData(String[] path) {
        try {
            return Files.readAllBytes(Paths.get(path[0]));
        } catch (NullPointerException e) {
            System.err.println("Could not find a path.");
        } catch (IOException e) {
            System.err.println("There was an error while reading or writing to contents of path. Return 500");
            hr = new HttpResponse("500 Internal Server Error", 0, "text/html");
        }
        return null;
    }
}
