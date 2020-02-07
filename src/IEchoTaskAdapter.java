import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface to wrap Runnable and extend
 * with functions to deal with Scheduling of
 * tasks and termination of those.
 */
public interface IEchoTaskAdapter extends Runnable {
    void setNrOfPackets(int i);
    void run();
    void attachScheduler(ScheduledExecutorService es);
    void stopSchedule();
    void sleepTask(long end, long start);
}
