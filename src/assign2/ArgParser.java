package assign2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

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
    static String[] getStaticContentInfo(String item, String defaultPath) {
        try {
            // Split at each CRLF
            String[] parsedHeader = item.split("\r\n");

            // Split at each space
            String[] parsedGet = parsedHeader[0].split(" ");

            // Append the default relative path to the content dir
            String contentDir = Paths.get("").toString() + defaultPath;

            // Extract additional params if there are any after "?".
            String[] queries = parsedGet[1].split("\\?");

            // Initialize queryParam for eventual token
            String token = "";


            if (queries.length > 1) {
                // Again split at additional queryParameters "&"
                String[] splitQueries = queries[1].split("&");

                // Iterate over parameters to support multiple.
                for (int i = 0; i < splitQueries.length; i++) {
                    String[] splitQueryParams = splitQueries[i].split("=");

                    if (splitQueryParams[0].equalsIgnoreCase("pass")) {
                        token = splitQueryParams[1];
                    }
                }
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

            return new String[]{contentDir, determineContentType(extension), token ,parsedGet[0]};
        } catch (IndexOutOfBoundsException e ) {
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
}
