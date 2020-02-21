package assign2;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
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
    private String defaultPath;
    private String[] protectedRoutes = {
            "protected.html"
    };

    private Map<String, String> redirectedRoutes = new HashMap<>();
    private byte[] data;
    private String defaultPassword = "1dv701";
    private OutputStream out;
    private InputStream is;
    private HttpResponse hr;
    private BufferedInputStream isr;


    public ServeTask(Socket socket, String path) {
        this.socket = socket;
        this.defaultPath = path;
    }


    @Override
    public void run() {
        redirectedRoutes.put("bee.png", "a/b/bee.png");
        // todo: fix infinite /////// to path
        try {
            is = socket.getInputStream();
            out = socket.getOutputStream();

            ParsedHeader header = new ParsedHeader();

            isr = new BufferedInputStream(is);

            // Read raw header
            String raw = readStringToCRLF(isr);

            // Fetch information about the content requested
            header = ArgParser.parseHeader(raw, defaultPath);

            assert header != null;
            switch (header.getRequestType()) {
                case "GET":
                    ProcessGet(header);
                    break;
                case "POST":
                    ProcessPost(header);
                    break;
                case "PUT":
                    ProcessPut();
                    break;
                default:
                    break;
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

    private String readStringToCRLF(BufferedInputStream isr) {
        StringBuilder sb = new StringBuilder();

        char fourth = ' ';
        char third = ' ';
        char second = ' ';
        char first = ' ';

        // Read request
        int read = 0;
        try {
            while (((read = isr.read()) != -1)) {
                first = (char) read;
                sb.append(first);

                // 2x CRLF read
                if (fourth == '\r' && third == '\n' && second == '\r' && first == '\n') {
                    System.out.println("Header read.");
                    // Save position in buffer
                    isr.mark(200);
                    break;
                }
                // Reorder last 4 bytes.
                fourth = third;
                third = second;
                second = first;
            }
        } catch (IOException e) {
            System.err.println("Could not read from stream.");
        }
        return sb.toString();
    }

    private void ProcessPut() {

    }

    private void ProcessPost(ParsedHeader header) throws IOException {

        // Check if there is a payload to read
        if (header.getContentLength() > 0 && header.getContentBoundary() != null) {

            // Read file details
            String[] fileInfo = readStringToCRLF(isr).split("\r\n");
            String name = findName(fileInfo);
            byte[] temp = readBytesToEOF(isr);
            byte[] imageData = new byte[temp.length-4];
            System.arraycopy(temp, 0, imageData,0, imageData.length-4);
            
            // Writes data to file
            if (imageData != null) {
                File file = new File("static/uploads/"+name);
                OutputStream fos = new FileOutputStream(file);
                fos.write(imageData);
                fos.close();
                System.out.println("File written");
                String success = "<h2>File successfully uploaded.</h2>";
                out.write(new HttpResponse("200 OK", success.length(), "text/html").build());
                out.write(success.getBytes());
                out.close();
            } else {
                out.write(new HttpResponse("500 Internal Server Error", 0, "text/html").build());
                out.close();
            }
        }

    }

    private String findName(String[] fileInfo) {
        String match = "filename=";
        int length = fileInfo[1].lastIndexOf(match);
        String name = fileInfo[1].substring(length+match.length());
        char c = '\"';
        name = name.replaceAll( String.valueOf(c), "");
        return name;
    }

    private byte[] readBytesToEOF(BufferedInputStream isr) {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();

        char third = ' ';
        char second = ' ';
        char first = ' ';

        // Read request
        int read = 0;
        try {
            while (((read = isr.read()) != -1)) {
                first = (char) read;

                // 2x CRLF read
                if (third == '-' && second == '-' && first == '-') {
                    System.out.println("Header read.");
                    break;
                }
                aos.write(read);

                // Reorder last 3 bytes.
                third = second;
                second = first;
            }
            return aos.toByteArray();
        } catch (IOException e) {
            System.err.println("Could not read from stream.");
        }
        return null;
    }


    /**
     * Processing steps of Get Request
     *
     * @param header Header information needed.
     * @throws IOException Handle IOException in caller.
     */
    private void ProcessGet(ParsedHeader header) throws IOException {
        // Verify that content exists
        isContent(header);

        // Verify that content is not protected
        isRouteProtected(header);

        // Verify that content is not moved
        isContentMoved(header);

        // Write response-header to stream
        out.write(hr.build());

        // Write data to stream
        if (data != null)
            out.write(data);
    }

    /**
     * Returns a URL to the new content if it has been moved.
     *
     * @param header parsedHeader
     * @return Outgoing url
     */
    private boolean isContentMoved(ParsedHeader header) {
        for (Map.Entry i : redirectedRoutes.entrySet()) {
            String compare = defaultPath + "/" + i.getKey();
            if (header.getPath().equalsIgnoreCase(compare)) {
                hr = new HttpResponse("302 found", 0, header.getContentType());
                hr.extras = new String[]{"Location: " + i.getValue().toString()};
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies that content actually exists.
     *
     * @param header parsed header information.
     * @return
     */
    private boolean isContent(ParsedHeader header) {
        try {
            boolean isValid = Paths.get(header.getPath()).toFile().isFile();

            // If path is a valid file
            if (isValid) {
                data = composeData(header);
                if (data != null) {
                    hr = new HttpResponse("200 OK", data.length, header.getContentType());
                }
            } else {
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
     * @param header parsed header info.
     * @return
     */
    private boolean isRouteProtected(ParsedHeader header) {
        for (String i : protectedRoutes) {
            String compare = defaultPath + "/" + i;
            if (compare.equalsIgnoreCase(header.getPath())) {
                if (header.getQueryParams().containsKey("pass") && header.getQueryParams().get("pass").equals(defaultPassword)) {
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
     * @param header parsed header information.
     * @return A byte array.
     */
    private byte[] composeData(ParsedHeader header) {
        try {
            return Files.readAllBytes(Paths.get(header.getPath()));
        } catch (NullPointerException e) {
            System.err.println("Could not find a path.");
        } catch (IOException e) {
            System.err.println("There was an error while reading or writing to contents of path. Return 500");
            hr = new HttpResponse("500 Internal Server Error", 0, "text/html");
        }
        return null;
    }
}
