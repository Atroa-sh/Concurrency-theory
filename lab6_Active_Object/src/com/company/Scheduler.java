package com.company;

public class Scheduler extends Thread {
    private final Buffer buffer;
    private final Executioner executioner;
    private final Locks locks;
    private final TasksQueues queues;
    private final TestManager manager;
    private int taskCount = 0;
    private boolean finished = false;
    private boolean noConsumers = false;
    private boolean noProducers = false;
    private boolean cantConsume = false;
    private boolean cantProduce = false;

    Scheduler(int m, TasksQueues queues, Locks locks, TestManager manager) {
        this.buffer = new Buffer(m);
        this.queues = queues;
        this.locks = locks;
        this.executioner = new Executioner(buffer);
        this.manager = manager;
    }

    @Override
    public void run() {

        while (true) {
            finished = false;
            try {
                if (buffer.lessThanHalf()) {
                    tryProducer();
                    if (!finished) {
                        tryConsumer();
                    }
                } else {
                    tryConsumer();
                    if (!finished) {
                        tryProducer();
                    }
                }
                if (!finished) {
                    waitForNewTask();
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void tryConsumer() throws InterruptedException {
        try {
            locks.consumerQueueLock.lock();
            if (!queues.consumerQueue.isEmpty()) {
                noConsumers = false;
                if (executioner.guard(queues.consumerQueue.get(0))) {
                    cantConsume = false;
                    finished = true;
                    executioner.executeTask(queues.consumerQueue.remove(0));
                    taskCount++;
                    manager.report(taskCount);
                } else cantConsume = true;
            } else noConsumers = true;
        } finally {
            queues.newConsRequest = false;
            locks.consumerQueueLock.unlock();
        }

    }

    private void tryProducer() throws InterruptedException {
        try {
            locks.producerQueueLock.lock();
            if (!queues.producerQueue.isEmpty()) {
                noProducers = false;
                if (executioner.guard(queues.producerQueue.get(0))) {
                    cantProduce = false;
                    finished = true;
                    executioner.executeTask(queues.producerQueue.remove(0));
                    taskCount++;
                    manager.report(taskCount);
                } else cantProduce = true;
            } else noProducers = true;
        } finally {
            queues.newProdRequest = false;
            locks.producerQueueLock.unlock();
        }
    }

    private void waitForNewTask() throws InterruptedException{
        try {
            locks.schedulerLock.lock();
            if (!queues.newConsRequest && noConsumers && cantProduce) {
                locks.awaitingConsumer.await();
                noConsumers = false;
                cantProduce = false;
            } else if (!queues.newProdRequest && noProducers && cantConsume) {
                locks.awaitingProducer.await();
                noProducers = false;
                cantConsume = false;
            } else if (!queues.newProdRequest && !queues.newConsRequest && noProducers && noConsumers) {
                locks.awaitingAnyone.await();
                noProducers = false;
                noConsumers = false;
            }

        } finally {
            locks.schedulerLock.unlock();
        }
    }
}



