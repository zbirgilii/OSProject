import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


//Run this as main//

public class Driver {
    static int disk_size = 2048;
    static int ram_size = 1024;
    static int cache_size = 128;
    static int job_count = 30;
    static int thread_delay = 0;
    

    public static void main(String[] args) throws InterruptedException, IOException, BrokenBarrierException {
        Execute(1, Scheduler.scheduler.FIFO); //1 core
        Execute(1, Scheduler.scheduler.PRIORITY);
        Execute(4, Scheduler.scheduler.FIFO); //4 core
        Execute(4, Scheduler.scheduler.PRIORITY);
    }

    static void Execute(int CPUcores, Scheduler.scheduler policy) throws IOException, InterruptedException {
        ResetDriver(policy);
        Metrics.Initialize(policy.toString() + +CPUcores + ".csv");
        //have each cpu do a job
        //cpu number is determined by core rnumber
        for (int i = 0; i < CPUcores; i++) {
            CPU cpu = new CPU(i);
            cpu.setName("CPU " + i);
            Scheduler.addCpu(cpu);
        }
        
        //executes threads at the same time and ends them.
        ExecutorService executorService = Executors.newCachedThreadPool();
        //used currentTimeMilis to get real system time. 
        Metrics.start_time(System.currentTimeMillis());
        //as long as CPU has instructions, execute them. 
        for (CPU cpu : Scheduler.instructions) {
            executorService.execute(cpu);
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);
        Metrics.print_metrics();
    }

    //Reset to run anohter Schedule mode.
    static void ResetDriver(Scheduler.scheduler policy) {
        Scheduler.instructions.clear();
        Scheduler.jobs.clear();
        Scheduler.policy = policy;
        Loader.load();
        MMU.Initialize();
    }

}
