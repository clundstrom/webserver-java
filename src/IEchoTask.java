import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface to wrap Runnable and extends with functions to deal with Scheduling of tasks.
 */
public interface IEchoTask extends Runnable{

    void setNrOfPackets(int i);
    void run();
    void attachScheduler(ScheduledExecutorService es);
    void stopSchedule();
}
