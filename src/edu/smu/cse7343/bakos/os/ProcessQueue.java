/*
    Simulator.java
    @author Yong Joseph Bakos

    This class represents an abstract main entry point of the simulation, and
    prepares all of the necessary structures and instances. Processing's `PApplet.main`
    function will call `setup`, and then call `draw` over and over again.

*/

package edu.smu.cse7343.bakos.os;

import java.util.*;
import processing.core.*;

public class ProcessQueue {
    
    private LinkedList<ProcessControlBlock> queue = new LinkedList<ProcessControlBlock>();
    private ProcessState managedState;
    private String title;
    private int x;
    private int y;

    public ProcessQueue(ProcessState managedState, String title, int x, int y) {
        this.managedState = managedState;
        this.title = title;
        this.x = x;
        this.y = y;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void add(ProcessControlBlock pcb) {
        pcb.state = ProcessState.READY;
        queue.add(pcb);
    }

    public ProcessControlBlock remove() {
        return queue.remove();
    }

    public void draw(HashMap<Integer, ProcessView> processViews, PApplet p) {
        p.pushStyle();
        p.pushMatrix();
        p.stroke(150);
        p.translate(x, y);
        p.textSize(32);
        p.text(title, 250, -50);
        p.textSize(18);
        p.text("Head", -50, 5);
        p.line(0, 0, 500, 0);
        float sizeOffset = 0.0f;
        for (ProcessControlBlock pcb : queue) {
            //if (pcb.state == managedState) {
                ProcessView view = processViews.get(new Integer(pcb.pid));
                p.fill(view.color);
                p.ellipse(sizeOffset + (view.size / 2.0f), 0, view.size, view.size);
                sizeOffset += (view.size / 2.0f);
            //}
        }
        p.popMatrix();
        p.popStyle();
    }

}