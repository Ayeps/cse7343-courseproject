/*
    Memory.java
    @author Yong Joseph Bakos

    TODO
*/

package edu.smu.cse7343.bakos.os;

public class Memory {

    int[] registers;

    public Memory(int size) {
        registers = new int[size];
    }

    public int totalSize() {
        return registers.length;
    }

}