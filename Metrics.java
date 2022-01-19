import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Metrics {

    static double RealStartTime;
    static BufferedWriter bufferedWriter;

    static void Initialize(String filename) throws IOException {
        File metrics = new File("./metrics/" + filename);
        bufferedWriter = new BufferedWriter(new PrintWriter(metrics));
    }

    static void list_metrics() throws IOException {
        bufferedWriter.write("# Job Metrics\n");
        bufferedWriter.write("Job ID, CPU ID, Waiting Time, Completion Time, I/O Processes, MMU RAM %, Job RAM %, Job Cache %\n");
        for (PCB job : Scheduler.jobs) {
            double waiting_time = job.getStartTime() - RealStartTime;
            double job_ram = (double) Math.round((double) job.getTotalSize() / Driver.ram_size * 1000) / 1000;
            double jobPercentCache = (double) Math.round((double) job.getCacheUsage() / Driver.cache_size * 1000) / 1000;
            double memoryPercRam = (double) Math.round((double) job.getRamUsage() / Driver.ram_size * 1000) / 1000;
            bufferedWriter.write(job.getJobId() + "," + job.getCurrrentCPU().getCpuId() + "," + waiting_time + "," +
                    job.getCompletionTime() + "," + job.getNumIoProcesses() + "," + memoryPercRam + "," + job_ram
                    + "," + jobPercentCache + "\n");
        }
    }


    static void listCpuMetrics() throws IOException {
        bufferedWriter.write("# CPU COMPLETION METRICS\n");
        bufferedWriter.write("CPU ID, Completion Time, I/O Processes, Number of Jobs, % of Jobs\n");
        for (CPU cpu : Scheduler.instructions) {
            double percentJobs = (double) Math.round((double) cpu.getJobCount() / Driver.job_count * 1000) / 1000;
            bufferedWriter.write(cpu.getCpuId() + "," + cpu.getCompletionTime() + "," + cpu.getIOProcesses()
                    + "," + cpu.getJobCount() + "," + percentJobs + "\n");
        }
    }


    static void print_metrics() throws IOException {
        list_metrics();
        bufferedWriter.newLine();
        listCpuMetrics();
        close();
    }

    static void start_time(long time) {
        RealStartTime = time;
    }

    static void close() throws IOException {
        bufferedWriter.close();
    }

    static void cacheDump() throws IOException {
        for(String str : MMU.ram) {
            bufferedWriter.write(str + "\n");
        }
    }
}
