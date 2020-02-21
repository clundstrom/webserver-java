package assign2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class which handles some common argument parsing.
 */
public class ArgParser {


    /**
     * Parses arguments to integer values.
     *
     * @param i String("123")
     * @return Int(123)
     */
    static int tryParse(String i) {
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException e) {
            System.err.println("There was an error parsing command line arguments.");
            System.exit(1);
        }

        return 0;
    }

    static void verifyArguments(String[] args) {
        // Handle mandatory arguments
        if (args.length < 2) {
            System.err.println("Error: Specify arguments (PORT) (RELATIVE PATH)\nExample: 8080 static ");
            System.exit(1);
        }
    }

    /**
     * Fetches content from static path.
     *
     * @param item item to be fetched.
     * @return content file path and file extension
     */
    static ParsedHeader parseHeader(String item, String defaultPath) {
        try {
            //
            ParsedHeader parsed = new ParsedHeader();

            // Split at each CRLF
            String[] parsedHeader = item.split("\r\n");

            // Split at data
            int contentLength = findContentLength(parsedHeader);

            // Split at each space
            String[] parsedGet = parsedHeader[0].split(" ");

            // Append the default relative path to the content dir
            String contentDir = Paths.get("").toString() + defaultPath;

            // Extract additional params if there are any after "?".
            String[] queries = parsedGet[1].split("\\?");


            // Parse data


            // Map Queries
            Map map = new HashMap();
            if (queries.length > 1) {
                map = mapQueries(queries[1]);
            }

            // Append with index.html if query ends with /
            if (queries[0].endsWith("/")) {
                queries[0] += "index.html";
            }

            // Get the file extension if there is one.
            String extension = "";
            if (queries[0].contains(".") && queries[0].lastIndexOf(".") != 0) {
                extension = queries[0].substring(queries[0].lastIndexOf(".") + 1);
            } else {
                queries[0] += "/index.html";
            }

            // Append url
            contentDir += queries[0];


            parsed = new ParsedHeader(parsedGet[0], contentDir, determineContentType(extension), map, contentLength);


            return parsed;
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Could not process header.");
        }
        return null;
    }


    /**
     * Determines the content type of the header by matching in a predefined list of supported types.
     *
     * @param extension Extension matched against the database.
     * @return Returns correct content type.
     */
    static String determineContentType(String extension) {
        try {
            List<String> supported = Files.readAllLines(Paths.get("SupportedContentTypes.txt").toAbsolutePath());

            for (String item : supported) {
                if (item.contains(extension)) {
                    return item;
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read supported content types.");
        }
        return "text/html";
    }

    static byte[] parseData(String incomingHeader, InputStream is) throws IOException {


        return null;
    }


    // Map Queries
    static Map<String, String> mapQueries(String queries){
        HashMap<String, String> map = new HashMap();

        if (!queries.isEmpty()) {
            // Again split at additional queryParameters "&"
            String[] splitQueries = queries.split("&");

            // Iterate over parameters to support multiple.
            for (int i = 0; i < splitQueries.length; i++) {
                String[] splitQueryParams = splitQueries[i].split("=");
                map.put(splitQueryParams[0], splitQueryParams[1]);
            }
        }
        return map;
    }

    static int findContentLength(String[] header){
        int length = 0;
        String match ="Content-Length: ";
        // Get content length
        for (String i : header) {
            if (i.startsWith(match)) {
                length = Integer.parseInt(i.substring(match.length()));
            }
        }
        return length;
    }
}
