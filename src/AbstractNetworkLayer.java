import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Is used to represent a Network Layer and the properties it possesses such as send and receive.
 *
 */
public abstract class AbstractNetworkLayer {


    public static int MYPORT = 6000;
    public static String MSG = "An echo message!";
    public static int BUFSIZE = 1024;
    public static int TRANSFER_RATE = 0;

    // Enabling debug will show a live feed of sent and received packets and their size. Not suitable for high rates.
    public static boolean DEBUG;


    public AbstractNetworkLayer(){
        if(MSG == null){
            System.err.println("You must provide a message.");
        }
    }


    /**
     * Function which handles task scheduling for the client.
     * @param task Custom task which sends and receives packages.
     */
    static void scheduleTask(IEchoTask task){
        ScheduledExecutorService es = new ScheduledThreadPoolExecutor(1);
        task.attachScheduler(es);

        // Special case, send once.
        if (TRANSFER_RATE == 0) {
            task.setNrOfPackets(1);
            es.execute(task);
        } else {
            // Run task each second
            es.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        }
    }



}
