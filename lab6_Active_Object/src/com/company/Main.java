package com.company;

//scheduler on one thread
//get command from smwhr
//do command
//give back Future

//proxy to contact Threads of P,K
//producer wants a confirmation of already produced (FUTURE) and then wants to make next
//consumer wants and gets in future

//weighting code later (sleep, sin/cos in loop)

//PK waits on the future after giving an order in proxy and has AN ADDITIONAL TASK IN THE MEANTIME (count 3 sins?)
//when to stop doing "own" task?

//future "locking" (synchronized) and not (give me value after some time)

//parameters!

//scheduler - in place of condition, whether a given producer/consumer can happen
//if not? - guard elsewhere? - throw back to the end
//takes another action from queue
//when queue is empty - beginning and end in same place so synchro problems
//don't use a Java ReentrantBlockingQueue! - a synchronized queue so no problem with Scheduler and (proxy?) BUT NO, need a custom queue with 2 lists to prevent starvation
//NOT TO THE END DROPPING COMMANDS, JUST CHECKING ON THE BEGINNING OF BOTH LISTS
//two lists "inside" and schedule changing - one for P, one for K
//time of arrival best - pairs of (time, command)
//if there is no task of another type?
//scheduler CAN "hang" with nothing to do and has to wait for new commands
//scheduler needs a .wait(), NO ACTIVE WAITING without tasks to realize!
//no task - he has to hang, and wait for the task to appear
//waiting for - consumer | producer | anything!
//addProducerTask(), addConsumerTask() on proxy, we wait for the scheduler to let us add anything
//scheduler with giveNextTask()
//we ignore lack of memory on queue

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // write your code here
        //Double[] conf = {0.05,0.1,0.2,0.5,1.0,2.0,5.0,10.0,20.0}; //producer/consumer ratio
        //Integer[] conf = {5,10,15,20,25,30,35,40,45,50,55,60}; //prod/con number
        Integer[] conf = {50,100,150,200,250,300,350,400,450,500,550,600}; //m
        //Integer[] conf = {1500,2000};
        String absolutePath = new File(".").getAbsolutePath();
        String fileName = "resultAO.csv";
        int nrOfTests = 5;
        int nrOfChecks = 5;
        TestManager testManager = new TestManager(fileName, absolutePath, nrOfTests);
        for(int k = 0 ; k < conf.length ; k++){
            int m = conf[k];
            int checkEvery = 10;
            int nrOfProducers =10;
            int nrOfConsumers = 10;
            testManager.startNewConf();
            for (int j = 1; j <= nrOfTests; j++) {
                System.out.println("New test " + j + " starts");
                Locks locks = new Locks();
                TasksQueues queues = new TasksQueues();
                List<Thread> consumers = new LinkedList<>();
                List<Thread> producers = new LinkedList<>();
                testManager.startNewTest(System.nanoTime(), j, producers, consumers);
                Proxy proxy = new Proxy(locks, queues, m, testManager);
                for (int i = 0; i < nrOfConsumers; i++) {
                    consumers.add(new Consumer(m, proxy, nrOfChecks, i + j * nrOfConsumers, checkEvery));
                }
                for (int i = 0; i < nrOfProducers; i++) {
                    producers.add(new Producer(m, proxy, nrOfChecks, i + j * nrOfProducers, checkEvery));
                }
                proxy.scheduler.start();
                for (int i = 0; i < nrOfProducers; i++) {
                    producers.get(i).start();
                }
                for (int i = 0; i < nrOfConsumers; i++) {
                    consumers.get(i).start();
                }

                for (int i = 0; i < nrOfProducers; i++) {
                    try {
                        producers.get(i).join();
                    } catch (InterruptedException e) {

                    }
                }
                for (int i = 0; i < nrOfConsumers; i++) {
                    try {
                        consumers.get(i).join();
                    } catch (InterruptedException e) {

                    }
                }
                try {
                    proxy.scheduler.join();

                } catch (InterruptedException e) {

                }
                System.out.println("Finished");
            }
        }
    }
}
