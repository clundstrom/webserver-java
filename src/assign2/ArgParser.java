package assign2;

import java.io.File;
import java.nio.file.Paths;

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


    /**
     * Parses message to specified buffer size.
     * @param message String to parse.
     * @return Byte array with parsed message.
     */
    static byte[] parseToBuffer(String message, int buffSize) {
        byte[] bytes = message.getBytes();
        byte[] buff = new byte[buffSize];

        for(int i=0; i < message.length(); i++){
            if(i == buff.length){
                System.err.println("Could not parse the complete message. (Buff size: " + buff.length + " Message: " + message.length() + ")");
                return buff;
            }
            buff[i] = bytes[i];
        }
        return buff;
    }


    static String getStaticContent(String item){
        String[] parsedHeader = item.split("\r\n");
        String[] parsedGet = parsedHeader[0].split( " ");
        if (parsedGet[1].equals("/")) {
            parsedGet[1] = "\\index.html";
        }
        String path = Paths.get("").toAbsolutePath().toString();
        path += "\\static" + parsedGet[1];
        System.out.println("Serving: " + Paths.get(path).getFileName());
        return path;
    }
}
