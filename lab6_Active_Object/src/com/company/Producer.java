package com.company;

import java.util.Random;
import java.util.concurrent.Callable;

public class Producer extends Thread {
    private final Proxy proxy;
    private final int m;
    private final int checkEvery;
    private final int nrOfChecks;
    private FutureReceiver future;
    private final int producerNumber;
    private long numberOfTaskDone = 0;
    private long maxNumOfTasks = 0;
    Random RNG = new Random();
    Random RNGSin = new Random(123);
    Producer(int m, Proxy proxy, int nrOfChecks, int producerNumber, int checkEvery) {
        this.proxy = proxy;
        this.m = m;
        this.producerNumber = producerNumber;
        this.checkEvery = checkEvery;
        this.nrOfChecks = nrOfChecks;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("P" + producerNumber);
        RNG.setSeed(Thread.currentThread().getName().hashCode());
        while (true) {
            int makeCount = RNG.nextInt(m);
            int result;
            makeCount += 1; //so he won't produce 0
            future = proxy.addProducerTask(makeCount);
            maxNumOfTasks += nrOfChecks*checkEvery;
            try {
                result = future.waiting(new Callable<>() {
                    @Override
                    public Void call() throws Exception{
                        return ownTask();
                    }
                }, nrOfChecks);
            } catch (Exception e){
                return;
            }
        }
    }

    private Void ownTask() throws InterruptedException{
        for (int i = 0; i < checkEvery; i++) {
            Math.sin(RNGSin.nextDouble());
            numberOfTaskDone ++;
        }
        if (Thread.interrupted()){
            throw new InterruptedException();
        }
        return null;
    }
    public long getMaxNumOfTasks() {
        return maxNumOfTasks;
    }

    public long getNumberOfTaskDone() {
        return numberOfTaskDone;
    }

}

