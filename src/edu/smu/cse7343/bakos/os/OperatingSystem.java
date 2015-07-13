/*
    OperatingSystem.java
    @author Yong Joseph Bakos

    TODO
*/

package edu.smu.cse7343.bakos.os;

public class OperatingSystem {

    public static final int ROUND_ROBIN_CYCLE_LIMIT = 30;
    private static final int FAUX_INITIAL_USERSPACE_PID = 10;

    private CPU cpu;
    private int currentPid;
    public int nextAvailablePid;
    public ProcessQueue readyQueue;
    public ProcessQueue waitQueue;

    public OperatingSystem(CPU cpu) {
        this.cpu = cpu;
        currentPid = 0;
        nextAvailablePid = FAUX_INITIAL_USERSPACE_PID;
        readyQueue = new ProcessQueue(ProcessState.READY);
        waitQueue = new ProcessQueue(ProcessState.WAITING);
    }

    public void manageProcesses() {
        if (!readyQueue.isEmpty() && (cpu.isIdle || roundRobinCycleLimitReached())) {
            switchContext();
        } // else idle
        System.out.println("Ready queue has: " + readyQueue.queue.size());
    }

    // Simulates an exec system call, and adds a PCB for a new process to the tail of
    // the ready queue.
    public void exec() {
        readyQueue.add(new ProcessControlBlock(nextAvailablePid++));
    }

    // Place the currently executing process' PCB at the tail of the ready queue,
    // and restore the execution of the process represented by the PCB at the head
    // of the queue.
    private void switchContext() {
        System.out.println("Dispatch!");
        if (readyQueue.isEmpty()) return;
        if (cpuIsExecutingAUserspaceProcess()) {
            ProcessControlBlock pcb = new ProcessControlBlock(currentPid);
            pcb.state = ProcessState.READY;
            pcb.programCounter = cpu.programCounter;
            pcb.registers = cpu.registers.clone();
            readyQueue.add(pcb);
        }
        dispatch(readyQueue.remove());
    }

    private void dispatch(ProcessControlBlock pcb) {
        pcb.state = ProcessState.RUNNING;
        cpu.registers = pcb.registers.clone();
        cpu.programCounter = pcb.programCounter;
        currentPid = pcb.pid;
        if (cpu.isIdle) cpu.isIdle = false;
    }

    // Simulates the self-blocking of a process, as if it is waiting for a resource.
    // Once the process is blocked, it is placed on the waiting queue, a context switch
    // occurs, and the corresponding view is dimmed to indicate that the process is blocked.
    public void blockCurrentProcess() {
        // if (currentProcess == null) return;
        // currentProcess.state = ProcessState.WAITING;
        // waitQueue.add(currentProcess);
        // // TODO: extract ProcessView view = processViews.get(new Integer(currentProcess.pid));
        // // if (view != null) view.dim();
        // currentProcess = null;
        // switchContext();
    }

    // Simulates the availability of a resource and an interrupt that allows the process
    // corresponding with the PCB at the head of the queue to be placed back in the ready
    // queue.
    // private void interruptAndUnblock(int pid) {
    //     ProcessControlBlock waitHead = os.waitQueue.peek();
    //     if (waitHead != null && pid == waitHead.pid) {
    //         ProcessControlBlock pcb = os.waitQueue.remove();
    //         pcb.state = ProcessState.READY;
    //         os.readyQueue.add(waitHead);
    //         processViews.get(new Integer(pid)).light();
    //     }
    // }

    // A naive simulation of determining if a process has received enough CPU time.
    private boolean roundRobinCycleLimitReached() {
        return cpu.cycleCount % ROUND_ROBIN_CYCLE_LIMIT == 0;
    }

    private boolean cpuIsExecutingAUserspaceProcess() {
        return currentPid > 0;
    }

}