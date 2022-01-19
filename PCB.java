import java.util.ArrayList;

// Process Control Block //

public class PCB implements Comparable<PCB> {

    // Job information
    private final int jobId;
    private int ProgramCounter;
    private final int priority;

    // Job Status
    private JobState state;
    private CPU CurrrentCPU;
    private Register[] registers = new Register[16];

    // Instructor Information
    private final int NumberofInstructions;
    private int inputBufferSize;
    private int outputBufferSize;
    private int tempBufferSize;

    // RAM Pointers
    private int RamStart;
    private int RamEnd;

    // Disk Pointers
    private final int diskStart;

    // Metric Values
    private long AddedTime;
    private long startTime;
    private long completionTime;
    private int ramUsage;
    private int cacheUsage;
    private int numIoProcesses = 0;

    PCB(String jobId, String NumberofInstructions, String priority, int diskStart) {
        this.jobId = Integer.parseInt(jobId, 16);
        this.ProgramCounter = 0;
        this.NumberofInstructions = Integer.parseInt(NumberofInstructions, 16);
        this.priority = Integer.parseInt(priority, 16);
        this.diskStart = diskStart;
        this.state = JobState.NEW;

        // Initialize Registers
        for (int i = 0; i < this.registers.length; i++) {
            this.registers[i] = new Register();
        }
    }
    // Metric Values
    void setAddedTime(long time) {
        this.AddedTime = time;
    }
    void setStartTime(long time) {
        this.startTime = time;
    }
    void setCompletionTime(long time) {
        this.completionTime = time;
    }
    long getAddedTime() {
        return AddedTime;
    }
    long getStartTime() {
        return startTime;
    }
    long getCompletionTime() {
        return completionTime - startTime;
    }
    void setRamUsage(int usage) {
        this.ramUsage = usage;
    }
    void setCacheUsage(int usage) {
        this.cacheUsage = usage;
    }
    int getRamUsage() {
        return ramUsage;
    }
    int getCacheUsage() {
        return cacheUsage;
    }
    int getNumIoProcesses () {
        return numIoProcesses;
    }
    void incrementIoProcesses() {
        numIoProcesses++;
    }

    // Getters & setters
    int getJobId() {
        return jobId;
    }

    int getProgramCounter(){
        return ProgramCounter;
    }
    int getPriority() {
        return priority;
    }
    JobState getJobState() {
        return state;
    }
    void setJobState(JobState state) {
        this.state = state;
    }
    // An enum for all possible job states.
    public enum JobState {
        BLOCKED,
        READY,
        NEW,
        RUNNING;
    }
    void incrementProgramCounter() {
        ProgramCounter++;
    }

    void setCurrrentCPU(CPU cpu) {
        this.CurrrentCPU = cpu;
    }
    CPU getCurrrentCPU() {
        return CurrrentCPU;
    }
    Register[] getRegisters() {
        return registers;
    }
    void setRegisters(Register[] registers) {
        this.registers = registers;
    }
    int getNumberofInstructions() {
        return NumberofInstructions;
    }

    //Buffers
    void setInputBufferSize(int inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
    }
    void setOutputBufferSize(int outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
    }
    void setTempBufferSize(int tempBufferSize) {
        this.tempBufferSize = tempBufferSize;
    }
    int getTotalSize() {
        return NumberofInstructions + inputBufferSize + outputBufferSize + tempBufferSize;
    }
    int getDiskStart() {
        return diskStart;
    }
    int getRamStart() {
        return RamStart;
    }
    void setRamStart(int index) {
        this.RamStart = index;
    }
    int getRamEnd() {
        return RamEnd;
    }
    void setRamEnd(int index) {
        this.RamEnd = index;
    }


    @Override
    public int compareTo(PCB pcb) {
        int stateCompare = state.compareTo(pcb.getJobState());
        int priorityCompare = Integer.compare(priority, pcb.getPriority());
        int NumberCompare = Integer.compare(NumberofInstructions, pcb.NumberofInstructions);

        // Order of Scheduler's priority queue
        //1. state
        //2. priority
        //3. number of instructions

        if (stateCompare == 0) {
            if (priorityCompare == 0) {
                return NumberCompare;
            } else {
                return priorityCompare;
            }
        } else {
            return stateCompare;
        }
    }

    @Override
    public String toString() {
        ArrayList<String> jobInfo = new ArrayList<>();
        jobInfo.add("Job ID: " + jobId);
        jobInfo.add("Job Priority: " + priority);
        jobInfo.add("Number of instructions: " + NumberofInstructions);
        jobInfo.add("Job State: " + state);
        jobInfo.add("RAM: " + RamStart + "-" + RamEnd);
        StringBuilder finalOutput = new StringBuilder();
        for (String s : jobInfo) {
            while (s.length() < 15) {
                s = s + " ";
            }
            finalOutput.append(s).append(" | ");
        }

        return finalOutput.toString();
    }
}