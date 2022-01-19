import java.math.BigInteger;
import java.util.Arrays;


public class CPU extends Thread {

    private PCB Current_Job;
    private int cpuId;
    private CpuState cpuState;

    private String bin;
    private String address;
    private int reg1_index;
    private int reg2_index;
    private int reg3_index;
    private int Address_Index;

    private Register[] register = new Register[16];
    private final String[] cache = new String[Driver.cache_size];

    private int pc;
    private boolean continueExec = true;

    private final long start;
    private long completion;
    private int ioProcesses = 0;
    private int jobCount;

    private final String[] opcodeArray = {"RD", "WR", "ST", "LW", "MOV", "ADD", "SUB", "MUL", "DIV", "AND",
    "OR", "MOVI", "ADDI", "MULI", "DIVI", "LDI", "SLT", "SLTI", "HLT", "NOP", "JMP", "BEQ",
    "BNE", "BEZ", "BNZ", "BGZ", "BLZ"};

    //initialize start time, id, and cpu state
    public CPU (int id) {
        this.start = System.currentTimeMillis();
        this.cpuId = id;
        this.cpuState = CpuState.FREE;
    }

   // This method loads an instruction at a specified address from RAM.
    private String fetch(int index) {
        return cache[index].substring(0, 8);
    }

    private void decode(String hex) {
        StringBuilder binString = new StringBuilder();
        binString.append(new BigInteger(hex, 16).toString(2));
        // Adds leading zeros if bin string is less than 32 chars long
        while (binString.toString().length() < 32) {
            binString.insert(0, "0");
        }
        bin = binString.toString();

        // Chars 0-1 indicate type of instruction (arithmetic, conditional, etc)
        instructionFormat(bin.substring(0, 2));

        // Chars 2-7 specify opcode of action. This is converted to decimal and used to index opcodeArray
        String opcodeBinary = bin.substring(2, 8);
        evaluate(opcodeArray[Integer.parseInt(opcodeBinary, 2)]);
    }

     // Main thread execution of the CPU class. Each CPU will independently check for remaining jobs and execute them accordingly.
       @Override
    public void run() {
        // Checking if job is completed successfully
        while (!Scheduler.hasNext()) {
            PCB nextJob = Scheduler.nextJob();
            if (nextJob != null) {
                jobCount++;
                cpuState = CpuState.EXECUTING;
                Dispatcher.loadJob(nextJob, this);
                nextJob.setRamUsage(MMU.ram_usage());
                loadInstructionsToCache();
                nextJob.setCacheUsage(getCacheUsage());
                while (continueExec && pc < Current_Job.getNumberofInstructions()) {
                    // Creating an artificial execution time for each instruction
                    try {
                        Thread.sleep(Driver.thread_delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace(); //prints error message for throawble object
                    }
                    String hex = fetch(pc);
                    pc++;
                    Current_Job.incrementProgramCounter();
                    decode(hex);
                }
                MMU.clear_all(Current_Job.getRamStart(), Current_Job.getRamEnd());
                Dispatcher.unloadJob(Current_Job, this);
                cpuState = CpuState.FREE;
                clearCache();
            }
        }

        completion = System.currentTimeMillis();
    }

    // Getter/setter methods
    void resetProgramCounter() {
        this.pc = 0;
        this.continueExec = true;
    }
    void setCurrent_Job(PCB job) {
        this.Current_Job = job;
    }
    PCB getCurrent_Job() {
        return Current_Job;
    }
    void setRegisters(Register[] register) {
        this.register = register;
    }
    Register[] getRegisters() {
        return register;
    }
    public int getCpuId() {
        return cpuId;
    }
    public long getCompletionTime() {
        return completion - start;
    }
    public int getIOProcesses() {
        return ioProcesses;
    }
    public int getJobCount() {
        return jobCount;
    }

     // Clear cache array
    void clearCache() {
        Arrays.fill(cache, "");
    }

     // Reads info for the current job from RAM into cache.
    void loadInstructionsToCache() {
        for (int i = 0; i < Current_Job.getTotalSize(); i++) {
            cache[i] = MMU.load_ram(Current_Job.getRamStart() + i);
        }
    }

     // Get the number of currently loaded instructions in the cache.
    public int getCacheUsage() {
        int usage = 0;
        for (String s : cache) {
            if (s != null && !s.equals("")) {
                usage++;
            }
        }
        return usage;
    }

    // Initializes register indexes based on instruction format.

    private void instructionFormat(String format) {
        switch (format) {
            case "00": { // ARITHMETIC
                String reg1 = bin.substring(8, 12);
                reg1_index = Integer.parseInt(reg1, 2);

                String reg2 = bin.substring(12, 16);
                reg2_index = Integer.parseInt(reg2, 2);

                String reg3 = bin.substring(16, 20);
                reg3_index = Integer.parseInt(reg3, 2);
                break;
            }
            case "01": // CONDITIONAL
            case "11": { // INPUT/OUTPUT
                    String reg1 = bin.substring(8, 12);
                reg1_index = Integer.parseInt(reg1, 2);

                String reg2 = bin.substring(12, 16);
                reg2_index = Integer.parseInt(reg2, 2);

                address = bin.substring(16, 32);
                Address_Index = Integer.parseInt(address, 2) / 4; //returns corresponding absolute/physical address
                break;
            }
            case "10": { // UNCONDITIONAL
                address = bin.substring(8, 32);
                Address_Index = Integer.parseInt(address, 2) / 4;
                break;
            }
        }
    }

     // Method to provide operations for each opcode.
    private void evaluate(String opcode) {
        switch (opcode) {
            case "RD": {
                if (Address_Index == 0) {
                    register[reg1_index].data = Integer.parseInt(cache[register[reg2_index].data], 16);
                } else {
                    register[reg1_index].data = Integer.parseInt(cache[Address_Index], 16);
                }
                ioProcesses++;
                Current_Job.incrementIoProcesses();
                break;
            }
            case "WR": {
                cache[Address_Index] = Integer.toHexString(register[reg1_index].data);
                ioProcesses++;
                Current_Job.incrementIoProcesses();
                break;
            }
            case "ST": {
                if (Address_Index == 0) {
                    cache[register[reg2_index].data] = Integer.toHexString(register[reg1_index].data);
                } else {
                    cache[Address_Index] = Integer.toHexString(register[reg1_index].data);
                }
                break;
            }
            case "LW": {
                if (Address_Index == 0) {
                    register[reg2_index].data = Integer.parseInt(cache[register[reg1_index].data], 16);
                } else {
                    register[reg2_index].data = Integer.parseInt(cache[Address_Index], 16);
                }
                break;
            }
            case "MOV": {
                register[reg3_index].data = register[reg1_index].data;
                break;
            }
            case "ADD": {
                register[reg3_index].data = register[reg1_index].data + register[reg2_index].data;
                break;
            }
            case "SUB": {
                register[reg3_index].data = register[reg1_index].data - register[reg2_index].data;
                break;
            }
            case "MUL": {
                register[reg3_index].data = register[reg1_index].data * register[reg2_index].data;
                break;
            }
            case "DIV": {
                if (register[reg2_index].data != 0) {
                    register[reg3_index].data = register[reg1_index].data / register[reg2_index].data;
                }
                break;
            }
            case "AND": {
                if (register[reg1_index].data != 0 && register[reg2_index].data != 0) {
                    register[reg3_index].data = 1;
                } else {
                    register[reg3_index].data = 0;
                }
                break;
            }
            case "OR": {
                if (register[reg1_index].data == 1 || register[reg2_index].data == 1) {
                    register[reg3_index].data = 1;
                } else {
                    register[reg3_index].data = 0;
                }
                break;
            }
            case "MOVI": {
                register[reg2_index].data = Integer.parseInt(address, 2);
                break;
            }
            case "ADDI": {
                register[reg2_index].data++;
                break;
            }
            case "MULI": {
                register[reg2_index].data = register[reg2_index].data * Address_Index;
                break;
            }
            case "DIVI": {
                if (Address_Index != 0) {
                    register[reg2_index].data = register[reg2_index].data / Address_Index;
                }
                break;
            }
            case "LDI": {
                register[reg2_index].data = Address_Index;
                break;
            }
            case "SLT": {
                if (register[reg1_index].data < register[reg2_index].data) {
                    register[reg3_index].data = 1;
                } else {
                    register[reg3_index].data = 0;
                }
                break;
            }
            case "SLTI": {
                if (register[reg1_index].data < Address_Index) {
                    register[reg2_index].data = 1;
                } else {
                    register[reg2_index].data = 0;
                }
                break;
            }
            case "HLT": {
                continueExec = false;
                break;
            }
            case "NOP": {
                pc++;
                break;
            }
            case "JMP": {
                pc = Address_Index;
                break;
            }
            case "BEQ": {
                if (register[reg1_index].data == register[reg2_index].data) {
                    pc = Address_Index;
                }
                break;
            }
            case "BNE": {
                if (register[reg1_index].data != register[reg2_index].data) {
                    pc = Address_Index;
                }
                break;
            }
            case "BEZ": {
                if (register[reg2_index].data == 0) {
                    pc = Address_Index;
                }
                break;
            }
            case "BNZ": {
                if (register[reg1_index].data != 0) {
                    pc = Address_Index;
                }
                break;
            }
            case "BGZ": {
                if (register[reg1_index].data > 0) {
                    pc = Address_Index;
                }
                break;
            }
            case "BLZ": {
                if (register[reg1_index].data < 0) {
                    pc = Address_Index;
                }
                break;
            }
        }
    }

     // enum that depicts the current state of the CPU.

    public enum CpuState {
        FREE,
        EXECUTING
    }

    @Override
    public String toString() {
        return "ID: " + cpuId + " | State: " + cpuState;
    }
}
