package com.company;

public class Task {
    public final Future future;
    //type 0 - producer, type 1 - consumer
    public final int type;
    public final int count;

    Task(Future future, int type, int count) {
        this.future = future;
        this.type = type;
        this.count = count;
    }
}
