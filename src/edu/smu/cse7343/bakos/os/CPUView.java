/*
    CPUView.java
    @author Yong Joseph Bakos

    This class encapsulates a visual representation of a CPU object.

*/

package edu.smu.cse7343.bakos.os;

import processing.core.*;

public class CPUView {

    private PApplet p;
    private CPU cpu;
    int x;
    int y;
    final int WIDTH = 200;
    final int HEIGHT = 200;
    final int STROKE_COLOR = 150;
    final int FILL_COLOR = 20;
    final int INTERNAL_LABEL_COLOR = 200;
    final int MAIN_LABEL_COLOR = 50;

    public CPUView(CPU cpu, int x, int y, PApplet p) {
        this.cpu = cpu;
        this.x = x;
        this.y = y;
        this.p = p;
    }

    public void draw() {
        p.pushStyle();
        p.pushMatrix();
        p.translate(x, y);
        // Body
        p.stroke(STROKE_COLOR);
        p.fill(FILL_COLOR);
        p.rectMode(p.CENTER);
        p.rect(0, 0, WIDTH, HEIGHT);
        // Registers
        p.rectMode(p.CORNER);
        for (int i = 0; i < cpu.registers.length; ++i) {
            p.fill(cpu.registers[i]);
            p.rect(-WIDTH / 2 + 70 + (i * 10), -HEIGHT / 2 + 30, 10, 15);
        }
        // Labels
        p.fill(MAIN_LABEL_COLOR);
        p.textSize(32);
        p.textAlign(p.CENTER);
        p.text("CPU", 0, 20);
        p.fill(INTERNAL_LABEL_COLOR);
        p.textSize(12);
        p.textAlign(p.LEFT);
        p.text("PC: " + cpu.programCounter, -WIDTH / 2 + 10, -HEIGHT / 2 + 20);
        if (cpu.isIdle) p.text("(kernel idle process)", -WIDTH / 2 + 50, -HEIGHT / 2 + 20);
        p.text("Registers", -WIDTH / 2 + 10, -HEIGHT / 2 + 40);
        p.popMatrix();
        p.popStyle();
    }

}