import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Is used to represent a Network Layer and the properties it possesses such as send and receive.
 *
 */
public abstract class AbstractNetworkLayer<T> {

    protected static int BUFSIZE = 1024;
    protected static int MYPORT = 25650;
    protected T socket;

    public static String MSG = "An echo message!";
    public static int TRANSFER_RATE = 0;
    public static volatile boolean shutdownEarly = false;

    // Enabling debug will show a live feed of sent and received packets and their size. Not suitable for high rates.
    public static boolean DEBUG = true;

    public AbstractNetworkLayer(){
        if(MSG == null){
            System.err.println("You must provide a message.");
        }
    }


    /**
     * Function which handles task scheduling for the client.
     * @param task Custom task which sends and receives packages.
     */
    protected static void scheduleTask(IEchoTaskAdapter task){
        ScheduledExecutorService es = new ScheduledThreadPoolExecutor(1);
        task.attachScheduler(es);

        // Special case, send once.
        if (TRANSFER_RATE == 0) {
            task.setNrOfPackets(1);
            shutdownEarly = true;
            task.run();
        } else {
            // Run task each second
            es.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        }
    }


    /**
     * Parses arguments and makes sure they are valid.
     * @param args Input arguments
     */
    protected void verifyArguments(String[] args){
        // Handle mandatory arguments
        if (args.length < 2) {
            System.err.println("Error: Specify arguments server_name port (buffer-size) (transfer-rate) ");
            System.exit(1);
        }

        // Parse buffer-size
        if (args.length >= 3) {
            BUFSIZE = ArgParser.tryParse(args[2]);
            if(BUFSIZE < 1) {
                System.err.println("Buffer size not allowed. Exiting..");
                System.exit(1);
            }
            if (BUFSIZE < MSG.getBytes().length)
                System.err.println("Warning: Buffer size less than message.");
        }

        // Parse transfer-rate
        if (args.length >= 4) {
            TRANSFER_RATE = ArgParser.tryParse(args[3]);

            if(TRANSFER_RATE < 0)
                System.err.println("Invalid transfer rate.");
        }
    }
}
