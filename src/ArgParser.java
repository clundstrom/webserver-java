public class ArgParser {


    public static int tryParse(String i){
        try{
            return Integer.parseInt(i);
        }
        catch (NumberFormatException e){
            System.err.print("There was an error parsing command line arguments.");
            System.exit(1);
        }

        return 0;
    }
}
