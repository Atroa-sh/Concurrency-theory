package com.company;

public class Proxy {
    public final Scheduler scheduler;
    private final Locks locks;
    private final TasksQueues queues;

    Proxy(Locks locks, TasksQueues queues, int m, TestManager manager) {
        this.locks = locks;
        this.queues = queues;
        this.scheduler = new Scheduler(m, queues, locks, manager);
    }

    Future addConsumerTask(int count) {
        Future future = new Future();
        Task newTask = new Task(future, 1, count);
        try {
            locks.consumerQueueLock.lock();
            queues.consumerQueue.add(newTask);
        } finally {
            locks.schedulerLock.lock();
            queues.newConsRequest = true;
            locks.consumerQueueLock.unlock();
            locks.awaitingConsumer.signal();
            locks.awaitingAnyone.signal();
            locks.schedulerLock.unlock();
        }

        return future;
    }

    Future addProducerTask(int count) {
        Future future = new Future();
        Task newTask = new Task(future, 0, count);
        try {
            locks.producerQueueLock.lock();
            queues.producerQueue.add(newTask);
        } finally {
            locks.schedulerLock.lock();
            queues.newProdRequest = true;
            locks.producerQueueLock.unlock();
            locks.awaitingProducer.signal();
            locks.awaitingAnyone.signal();
            locks.schedulerLock.unlock();
        }

        return future;
    }


}
