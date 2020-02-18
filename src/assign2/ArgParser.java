package assign2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Helper class which handles some common argument parsing.
 */
public class ArgParser {


    /**
     * Parses arguments to integer values.
     * @param i String("123")
     * @return Int(123)
     */
    static int tryParse(String i){
        try{
            return Integer.parseInt(i);
        }
        catch (NumberFormatException e){
            System.err.println("There was an error parsing command line arguments.");
            System.exit(1);
        }

        return 0;
    }

    static void verifyArguments(String[] args){
        // Handle mandatory arguments
        if (args.length < 2) {
            System.err.println("Error: Specify arguments (PORT) (RELATIVE PATH)\nExample: 8080 static ");
            System.exit(1);
        }
    }

    /**
     * Fetches content from static path.
     * @param item item to be fetched.
     * @return content file path and file extension
     */
    static String[] getStaticContentInfo(String item, String defaultPath) {
        String[] parsedHeader = item.split("\r\n");
        String[] parsedGet = parsedHeader[0].split( " ");
        String contentDir = Paths.get("").toString() + defaultPath;
        String url = parsedGet[1];


        // If ends with / or no extension
        if(url.endsWith("/")){
            url += "/index.html";
        }

        // Get the file extension if there is one.
        String extension = "";
        if(url.contains(".") && url.lastIndexOf(".") != 0) {
            extension = url.substring(url.lastIndexOf(".") + 1);
        }
        else{
            url += "/index.html";
        }

        // Append url
        contentDir += url;

        return new String[]{contentDir, determineContentType(extension)};
    }


    /**
     * Determines the content type of the header by matching in a predefined list of supported types.
     * @param extension Extension matched against the database.
     * @return Returns correct content type.
     */
    static String determineContentType(String extension){
        try {
            List<String> supported = Files.readAllLines(Paths.get("SupportedContentTypes.txt").toAbsolutePath());

            for(String item : supported){
                if(item.contains(extension)){
                    return item;
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read supported content types.");
        }
        return "text/html";
    }


}
