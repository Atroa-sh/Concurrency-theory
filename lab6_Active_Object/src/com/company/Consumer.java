package com.company;

import java.util.Random;
import java.util.concurrent.Callable;

public class Consumer extends Thread {
    private final Proxy proxy;
    private final int m;
    private final int nrOfChecks;
    private final int checkEvery;
    private FutureReceiver future;
    private final int consumerNumber;
    private long numberOfTaskDone = 0;
    private long maxNumOfTasks = 0;
    Random RNG = new Random();
    Random RNGSin = new Random(123);
    Consumer(int m, Proxy proxy, int nrOfChecks, int consumerNumber, int checkEvery) {
        this.proxy = proxy;
        this.m = m;
        this.nrOfChecks = nrOfChecks;
        this.consumerNumber = consumerNumber;
        this.checkEvery = checkEvery;
    }


    @Override
    public void run() {
        Thread.currentThread().setName("C" + consumerNumber);
        RNG.setSeed(Thread.currentThread().getName().hashCode());
        while (true) {
            //send a command to proxy - get a future - then do sth in the meantime - check if finished
            int getCount = RNG.nextInt(m);
            int result;
            getCount += 1; //so he won't consume 0
            future = proxy.addConsumerTask(getCount);
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
