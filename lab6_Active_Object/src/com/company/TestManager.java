package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;

public class TestManager {
    private FileWriter writer;
    private double startTime;
    private int currentTest;
    private List<Producer> producers;
    private List<Consumer> consumers;
    private final int firstThreshold = 10000;
    private final int secondThreshold = 100000;
    private final int thirdThreshold = 1000000;
    private final int forthThreshold = 4000000;
    private double firstThresholdTime;
    private double secondThresholdTime;
    private double thirdThresholdTime;
    private double forthThresholdTime;
    private int nrOfTests;
    private double firstThresholdAcc;
    private double secondThresholdAcc;
    private double thirdThresholdAcc;
    private double forthThresholdAcc;
    private double totalCpuAcc;
    private long numOfTasksDoneAcc;
    private long maxNumOfTasksAcc;
    private final String absolutePath;
    private final String fileName;

    public TestManager(String fileName, String absolutePath, int nrOfTests){
        this.nrOfTests = nrOfTests;
        this.absolutePath = absolutePath;
        this.fileName = fileName;
        writer = null;
        try {
            this.writer = new FileWriter(fileName);
            this.writer.write("Conf;1st_mark;2nd_mark;3rd_mark;tasksDone;maxTasks;totalCpuTime;\n");
            this.writer.flush();
        } catch (IOException e) {
            System.out.println("An error occurred while creating " + fileName);
        }
    }

    public void startNewTest(double startTime, int currentTest, List producers, List consumers) {
        this.startTime = startTime;
        this.currentTest = currentTest;
        this.producers = producers;
        this.consumers = consumers;
    }

    public void startNewConf(){
        this.firstThresholdAcc = 0;
        this.secondThresholdAcc = 0;
        this.thirdThresholdAcc = 0;
        this.forthThresholdAcc = 0;
        this.totalCpuAcc = 0;
        this.numOfTasksDoneAcc = 0;
        this.maxNumOfTasksAcc = 0;
    }

    private static long cpuTime(Thread thr) {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        if (mxBean.isThreadCpuTimeSupported()) {
            try {
                return mxBean.getThreadCpuTime(thr.getId());
            } catch (UnsupportedOperationException e) {
                System.out.println(e.toString());
            }
        } else {
            System.out.println("Not supported");
        }
        return 0;
    }

    public void report(int step) throws InterruptedException {
        switch (step) {
            case firstThreshold -> {
                firstThresholdTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
                System.out.println("First threshold of " + firstThreshold + " has been reached in: " + firstThresholdTime + " sec");
                firstThresholdAcc += firstThresholdTime;
            }
            case secondThreshold -> {
                secondThresholdTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
                System.out.println("Second threshold of " + secondThreshold + " has been reached in: " + secondThresholdTime + " sec");
                secondThresholdAcc += secondThresholdTime;
            }
            case thirdThreshold -> {
                thirdThresholdTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
                System.out.println("Third threshold of " + thirdThreshold + " has been reached in: " + thirdThresholdTime + " sec");
                thirdThresholdAcc += thirdThresholdTime;
                finish();
            }
            case forthThreshold -> {
                forthThresholdTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
                System.out.println("Third threshold of " + forthThreshold + " has been reached in: " + forthThresholdTime + " sec");
                forthThresholdAcc += forthThresholdTime;

            }
        }
    }

    private void saveRecord(String toWrite) {
        try {
            writer.write(toWrite);
            writer.flush();
        } catch (Exception e) {
            System.out.println("An error occurred (" + fileName + ")");
            e.printStackTrace();
        }
    }

    private void finish() throws InterruptedException {
        double threadTime;
        double cpuTimeAcc = 0;
        Consumer currentConsumer;
        for (int i = 0; i < consumers.size(); i++) {
            currentConsumer = consumers.get(i);
            threadTime = cpuTime(currentConsumer) / Math.pow(10, 9);
            cpuTimeAcc += threadTime;
            currentConsumer.interrupt();
            System.out.println(currentConsumer.getName() + "'s cpu time: " + threadTime + " sec");
            numOfTasksDoneAcc += currentConsumer.getNumberOfTaskDone();
            maxNumOfTasksAcc += currentConsumer.getMaxNumOfTasks();
        }
        Producer currentProducer;
        for (int i = 0; i < producers.size(); i++) {
            currentProducer = producers.get(i);
            threadTime = cpuTime(currentProducer) / Math.pow(10, 9);
            cpuTimeAcc += threadTime;
            currentProducer.interrupt();
            System.out.println(currentProducer.getName() + "'s cpu time: " + threadTime + " sec");
            numOfTasksDoneAcc += currentProducer.getNumberOfTaskDone();
            maxNumOfTasksAcc += currentProducer.getMaxNumOfTasks();
        }
        cpuTimeAcc += cpuTime(Thread.currentThread()) / Math.pow(10, 9);
        totalCpuAcc += cpuTimeAcc;
        System.out.println("Cpu total time: " + cpuTimeAcc);
        if(currentTest == nrOfTests){
            saveRecord(((double)producers.size()/(double)consumers.size()) + ";" + firstThresholdAcc/nrOfTests + ";" + secondThresholdAcc/nrOfTests + ";" + thirdThresholdAcc/nrOfTests + ";" + numOfTasksDoneAcc/nrOfTests + ";" + maxNumOfTasksAcc/nrOfTests + ";"  + totalCpuAcc/nrOfTests + ";\n");
        }
        throw new InterruptedException();
    }


}

