import java.util.*;

/**
 * Schedules jobs with FIFO or Priority scheduling policy.
 */
public class Scheduler {

    public static scheduler policy;
    
    /**
     * Load 30 jobs into an array
     */
    public static final ArrayList<PCB> jobs = new ArrayList<>(30);

    /**
     * Load instruction set into an array
     */
    public static ArrayList<CPU> instructions = new ArrayList<>();

    private static final PriorityQueue<PCB> priority_queue = new PriorityQueue<>();
    private static final LinkedList<PCB> fifo_queue = new LinkedList<>();

    //Long-term scheduler that adds jobs to a policy
    //and sets its added time to real time. 
    //Also makes sure each job is assigned to its assigned policy
    //And adds the job to the queue accordingly. 
    static void addJob(PCB job) {
        job.setAddedTime(System.currentTimeMillis());
        jobs.add(job); //takes job from job pool/thread pool.
        if (policy == scheduler.PRIORITY) {
            priority_queue.add(job);
        } else {
            fifo_queue.add(job);
        }
    }

    //Add a CPU to the list of CPUs
    static void addCpu(CPU cpu) {
        instructions.add(cpu);
    }

    //Checks if there is remaining job in the queue
    //and retrieves the job that's in ready state at the top of the list. 
    static synchronized boolean hasNext() {
        PCB nextJob;
        if (policy == scheduler.PRIORITY) {
            nextJob = priority_queue.peek();
        } else {
            nextJob = fifo_queue.peek();
        }
        return nextJob == null;
    }

    //Short-term schedule that retrives the job from the top of the list with hasNext() and then removes it. 
    //Syncronized method provides protection from a race condition or a deadlock between CPUs.
    static synchronized PCB nextJob() {
        PCB nextJob;
        if (policy == scheduler.PRIORITY) {
            nextJob = priority_queue.poll();
        } else {
            nextJob = fifo_queue.poll();
        }

        if (nextJob != null) {
            nextJob.setJobState(PCB.JobState.RUNNING); //sets the status from ready to running. 
        }
        return nextJob;
    }

    //keeps a hold of our two policy types. 
    public enum scheduler {
        FIFO,
        PRIORITY
    }
}
