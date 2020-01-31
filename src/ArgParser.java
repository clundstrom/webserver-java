public class ArgParser {


    static int tryParse(String i){
        try{
            return Integer.parseInt(i);
        }
        catch (NumberFormatException e){
            System.err.print("There was an error parsing command line arguments.");
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


}
