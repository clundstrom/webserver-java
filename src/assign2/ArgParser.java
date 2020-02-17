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
    static String[] getStaticContent(String item){
        String[] parsedHeader = item.split("\r\n");
        String[] parsedGet = parsedHeader[0].split( " ");
        if (parsedGet[1].equals("/")) {
            parsedGet[1] = "\\index.html";
        }
        String path = Paths.get("").toAbsolutePath().toString();
        path += "\\static" + parsedGet[1];
        System.out.println("Serving: " + Paths.get(path).getFileName());

        String extension = "";
        // Parse file extension
        if(parsedGet[1].contains(".") && parsedGet[1].lastIndexOf(".") != 0){
            extension = parsedGet[1].substring(parsedGet[1].lastIndexOf(".")+1);
        }

        String[] contentInfo = {path, determineContentType(extension)};

        return contentInfo;
    }


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
