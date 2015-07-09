/*
    Simulator.java
    @author Yong Joseph Bakos

    This class represents an abstract main entry point of the simulation, and
    prepares all of the necessary structures and instances. Processing's `PApplet.main`
    function will call `setup`, and then call `draw` over and over again.

    For the sake of this simulation, you can consider the repetitive calls to draw a
    complete fetch-execute cycle (or tick-tock) of the CPU.

    This simulation uses a very naive, implicit round-robin scheduler that lets each
    active process execute for 30 cycles before being placed at the back of the ready
    queue.

    The screen itself displays instructions for the user (see README.md) for details.
*/

package edu.smu.cse7343.bakos.os;

import processing.core.*;
import java.util.*;

public class Simulator extends PApplet {

    private final static int ROUND_ROBIN_CYCLE_LIMIT = 30;

    private ProcessQueue readyQueue;
    private ProcessQueue waitQueue;
    private ProcessControlBlock currentProcess;
    private int nextPid = 0;
    // Visual representations of each running process, each corresponding to a PCB in a queue.
    private HashMap<Integer, ProcessView> processViews = new HashMap<Integer, ProcessView>();

    public void setup() {
        size(displayWidth, displayHeight);
        readyQueue = new ProcessQueue(ProcessState.READY, "Ready Queue", 200, height - 300);
        waitQueue = new ProcessQueue(ProcessState.WAITING, "Wait Queue", 200, height - 100);
    }

    // Invoked automatically and over and over again by Processing. This provides an implicit
    // fetch/execute cycle or tick-tock of the CPU.
    // At the highest level of abstraciton, this is all the whole program really does.
    public void draw() {
        background(0);
        drawTitle();
        tickTock();
        drawProcessViews();
        drawQueues();
    }

    // This really is a true interrupt handler, and I use it to simulate interrupts. Pressing
    // the space bar will spawn a new process, adding it to the ready queue. Pressing the B
    // key will cause the currently executing process to self-block, to "fake" waiting for a
    // resource such as some abstract I/O.
    public void keyPressed() {
        if (key == ' ') {
            createNewProcess(nextPid++);
        } else if (key == 'b') {
            blockProcess(currentProcess);
        }
    }

    // This really is a true interrupt handler, and I use it to simulate an interrupt that
    // signals a process in the wait queue that its resource is ready and that the process
    // can be put back on the ready queue.
    public void mousePressed() {
        for (Map.Entry<Integer, ProcessView> entry : processViews.entrySet()) {
            if (entry.getValue().isClicked(mouseX, mouseY)) {
                interruptAndUnblock(entry.getKey().intValue());
            }
        }
    }

    private void drawTitle() {
        fill(50);
        textSize(72);
        textAlign(CENTER);
        text("Press space to create a process.", width / 2, 100);
        text("Press B to cause the executing process to self-block.", width / 2, 180);
        textSize(32);
        textAlign(CENTER);
        text("Click on a blocked process to interrupt and place it back in the ready queue.", width / 2, 230);
    }

    // Executes the current process. If it has been given enough CPU time, then conduct a
    // context switch by placing the current PCB at the tail of the ready queue, and use the
    // restore execution of the process represented by the PCB at the head of the queue.
    private void tickTock() {
        if (!readyQueue.isEmpty() && roundRobinCycleLimitReached()) {
            switchContext();
        } else if (currentProcess != null) {
            execute(currentProcess);
        }
    }

    // A naive simulation of determining if a process has received enough CPU time.
    private boolean roundRobinCycleLimitReached() {
        return frameCount % ROUND_ROBIN_CYCLE_LIMIT == 0;
    }

    // Adds a new PCB to the tail of the ready queue, and also creates a ProcessView
    // to visually represent the process for that PCB.
    private void createNewProcess(int pid) {
        readyQueue.add(new ProcessControlBlock(pid));
        processViews.put(new Integer(pid), new ProcessView(this));
    }

    // Place the currently executing process' PCB at the tail of the ready queue,
    // and restore the execution of the process represented by the PCB at the head
    // of the queue.
    private void switchContext() {
        if (currentProcess != null) {
            readyQueue.add(currentProcess);
            currentProcess.state = ProcessState.READY;
        }
        if (!readyQueue.isEmpty()) {
            currentProcess = readyQueue.remove();
            currentProcess.state = ProcessState.RUNNING;
        }
    }

    // Simulates the execution of a process represented by the particular PCB,
    // and updates the corresponding ProcessView that visually represents the
    // process associated with that PCB.
    private void execute(ProcessControlBlock pcb) {
        pcb.programCounter++;
        ProcessView view = processViews.get(new Integer(pcb.pid));
        if (view != null) view.update();
    }

    // Simulates the self-blocking of a process, as if it is waiting for a resource.
    // Once the process is blocked, it is placed on the waiting queue, a context switch
    // occurs, and the corresponding view is dimmed to indicate that the process is blocked.
    private void blockProcess(ProcessControlBlock pcb) {
        if (pcb == null) return;
        pcb.state = ProcessState.WAITING;
        waitQueue.add(pcb);
        currentProcess = null;
        switchContext();
        ProcessView view = processViews.get(new Integer(pcb.pid));
        if (view != null) view.dim();
    }

    // Simulates the availability of a resource and an interrupt that allows the process
    // corresponding with the PCB at the head of the queue to be placed back in the ready
    // queue.
    private void interruptAndUnblock(int pid) {
        ProcessControlBlock waitHead = waitQueue.peek();
        if (waitHead != null && pid == waitHead.pid) {
            ProcessControlBlock pcb = waitQueue.remove();
            pcb.state = ProcessState.READY;
            readyQueue.add(waitHead);
            processViews.get(new Integer(pid)).light();
        }
    }

    private void drawProcessViews() {
        for (ProcessView v : processViews.values()) {
            v.draw();
        }
    }

    private void drawQueues() {
        readyQueue.draw(processViews, this);
        waitQueue.draw(processViews, this);
    }

}