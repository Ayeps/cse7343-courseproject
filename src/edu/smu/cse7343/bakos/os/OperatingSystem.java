/*
    OperatingSystem.java
    @author Yong Joseph Bakos

    TODO
*/

package edu.smu.cse7343.bakos.os;

import java.util.*;

public class OperatingSystem {

    public static final int ROUND_ROBIN_CYCLE_LIMIT = 30;
    private static final int FAUX_INITIAL_USERSPACE_PID = 10;
    private static final int DEFAULT_HEAP_SIZE = 10;
    private static final int DEFAULT_STACK_SIZE = 10;

    private CPU cpu;
    private Memory memory;
    private int nextAvailableMemoryAddress;
    private int currentPid;
    private int nextAvailablePid;
    public ProcessQueue readyQueue;
    public ProcessQueue waitQueue;

    private Random rand;

    public OperatingSystem(CPU cpu, Memory memory) {
        this.cpu = cpu;
        this.memory = memory;
        nextAvailableMemoryAddress = 0; // Logical. -> 256000
        currentPid = 0; // idle
        nextAvailablePid = FAUX_INITIAL_USERSPACE_PID;
        readyQueue = new ProcessQueue(ProcessState.READY);
        waitQueue = new ProcessQueue(ProcessState.WAITING);
        rand = new Random();
    }

    public void manageProcesses() {
        if (!readyQueue.isEmpty() && (cpu.isIdle || roundRobinCycleLimitReached())) {
            switchContext();
        } // else idle
        System.out.println("Ready queue has: " + readyQueue.queue.size()); // TODO: delete
    }

    // Execute a new process, by adding a PCB for the new process to the tail
    // of the ready queue.
    public void exec() {
        Program program = new Program(); // load program from disk
        // allocate memory
        int memoryNeeded = (int)program.size;
        int base = nextAvailableMemoryAddress;
        nextAvailableMemoryAddress += memoryNeeded;
        // store in memory
        storeInMemory(base, memoryNeeded, program);
        ProcessControlBlock pcb = new ProcessControlBlock(nextAvailablePid++, base, base + memoryNeeded - 1, program);
        readyQueue.add(pcb);
    }

    private void storeInMemory(int baseAddress, int memoryNeeded, Program p) {
        for (int i = baseAddress; i < baseAddress + memoryNeeded; ++i) {
            memory.write(i, Float.intBitsToFloat(p.color));
        }
        memory.write(nextAvailableMemoryAddress, Float.intBitsToFloat(p.p.color(255, 0, 0)));
    }

    // Place the currently executing process' PCB at the tail of the ready queue,
    // and restore the execution of the process represented by the PCB at the head
    // of the queue.
    private void switchContext() {
        System.out.println("Dispatch!");
        if (readyQueue.isEmpty()) return;
        if (cpuIsExecutingAUserspaceProcess()) {
            ProcessControlBlock pcb = new ProcessControlBlock(currentPid, cpu.baseRegister, cpu.limitRegister, cpu.currentProgram);
            pcb.state = ProcessState.READY;
            pcb.programCounter = cpu.programCounter;
            // pcb.registers = cpu.registers.clone();
            readyQueue.add(pcb);
        }
        dispatch(readyQueue.remove());
    }

    private void dispatch(ProcessControlBlock pcb) {
        currentPid = pcb.pid;
        pcb.state = ProcessState.RUNNING;
        cpu.exec(pcb);
    }

    // Simulates the self-blocking of a process, as if it is waiting for a resource.
    // Once the process is blocked, it is placed on the waiting queue, a context switch
    // occurs, and the corresponding view is dimmed to indicate that the process is blocked.
    // public void blockCurrentProcess() {
    //     if (currentProcess == null) return;
    //     currentProcess.state = ProcessState.WAITING;
    //     waitQueue.add(currentProcess);
    //     // TODO: extract ProcessView view = processViews.get(new Integer(currentProcess.pid));
    //     // if (view != null) view.dim();
    //     currentProcess = null;
    //     switchContext();
    // }

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

    // In this simulation, process id 0 is the kernel idle process; all other
    // processes are userspace processes.
    private boolean cpuIsExecutingAUserspaceProcess() {
        return !cpu.isIdle;
    }

}