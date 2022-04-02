package com.company;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Locks {
    public final Lock consumerQueueLock = new ReentrantLock();
    public final Lock producerQueueLock = new ReentrantLock();
    public final Lock schedulerLock = new ReentrantLock();
    public final Condition awaitingConsumer = schedulerLock.newCondition();
    public final Condition awaitingProducer = schedulerLock.newCondition();
    public final Condition awaitingAnyone = schedulerLock.newCondition();
}
