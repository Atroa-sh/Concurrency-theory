package com.company;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Buffer {
    private final int maxSize;
    private int empty;
    private int filled;

    private ArrayList<Integer> buffer = new ArrayList<>();
    Buffer(int m) {
        this.maxSize = 2 * m;
    }


    public boolean canProduce(int n) {
        if (maxSize - buffer.size() >= n) return true;
        else return false;
    }

    public boolean canConsume(int n) {
        if (buffer.size() >= n) return true;
        else return false;
    }

    public int produceN(int n) {
        for(int i = 0 ; i < n ; i++){
            buffer.add(1);
        }

        return n;
    }

    public int consumeN(int n) {
        for(int i = 0 ; i < n ; i++){
            buffer.remove(0);
        }
        return n;
    }

    public boolean lessThanHalf() {
        return buffer.size() < maxSize / 2;
    }


}