class Dispatcher {

    /**
     * Dipatches a job onto a CPU.
     * @param job The job to be loaded on to a CPU.
     * @param cpu specifies which CPU the job will be loaded to.
     */

     //Syncronized method allows the resources to be protected as they are running concurrently. 
    static synchronized void loadJob(PCB job, CPU cpu) {
        int totalSize = job.getTotalSize();
        int diskStartPoint = job.getDiskStart();
        int ramStartPoint = accessRam(totalSize, diskStartPoint);
        int ramEndPoint = ramStartPoint + totalSize;
        job.setCurrrentCPU(cpu);
        job.setRamStart(ramStartPoint);
        job.setRamEnd(ramEndPoint);

        // Load Instructions into memory
        cpu.setRegisters(job.getRegisters());
        cpu.resetProgramCounter();
        cpu.setCurrent_Job(job);
        job.setStartTime(System.currentTimeMillis());
        System.out.println(job);
    }

    //syncronized method to hide memory resources as they are run concurrently.
    static synchronized int accessRam(int totalSize, int diskStartPoint) {
        int ramStartPoint = MMU.left(totalSize);
        for(int i = ramStartPoint; i < ramStartPoint + totalSize; i++) {
            MMU.store_ram(i, MMU.load_disk(diskStartPoint + i - ramStartPoint));
        }
        return ramStartPoint;
    }

    //clear when job is completed
    static void unloadJob(PCB job, CPU cpu) {
        job.setRegisters(cpu.getRegisters());
        job.setCompletionTime(System.currentTimeMillis());
        cpu.setCurrent_Job(null);
    }
}
