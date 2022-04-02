import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static LinkedList<Thread> producers = new LinkedList<>();
    static LinkedList<Thread> consumers = new LinkedList<>();
    static ProCon producersAndConsumers;
    static FileSaver fileSaver = new FileSaver("resultsNested.csv");

    public static void main(String[] args){
        int nrOfProducers = 3;
        int nrOfConsumers = 3;
        int nrOfTests = 10;

        producersAndConsumers = new ProCon(1);
        for(int j = 1; j <= nrOfTests ; j++){
            producersAndConsumers.reset(j);
            System.out.println("Starting " + j + " test");
            for(int i = 0; i < nrOfProducers; i++){
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true){
                                producersAndConsumers.pro();
                            }
                        }
                        catch (InterruptedException e){

                        }
                    }
                });
                t.setName("P"+ i + nrOfProducers*(j - 1));
                producers.add(t);
            }
            for(int i = 0; i < nrOfConsumers; i++){
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true){
                                producersAndConsumers.con();
                            }

                        }
                        catch (InterruptedException e){

                        }
                    }
                });
                t.setName("C"+i + nrOfConsumers*(j - 1));
                consumers.add(t);
            }
            for(int i = 0; i < nrOfProducers; i++){
                producers.get(i).start();
            }
            for(int i = 0; i < nrOfConsumers; i++){
                consumers.get(i).start();
            }
            for(int i = 0; i < nrOfConsumers; i++){
                try {
                    producers.get(i).join();
                }
                catch (InterruptedException e){

                }
            }
            for(int i = 0; i < nrOfConsumers; i++){
                try {
                    consumers.get(i).join();
                }
                catch (InterruptedException e){

                }
            }
        }
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
    public static class ProCon{
        private final int firstThreshold = 10000;
        private final int secondThreshold = 100000;
        private final int thirdThreshold = 1000000;
        private final int forthThreshold = 10000000;
        private double firstThresholdTime;
        private double secondThresholdTime;
        private double thirdThresholdTime;
        private double forthThresholdTime;
        private double firstThresholdAcc;
        private double secondThresholdAcc;
        private double thirdThresholdAcc;
        private double forthThresholdAcc;
        int nr;
        final int m = 10;
        long startTime = System.nanoTime();
        static int steps = 0;
        int nrOfTasks;
        public ProCon(int nrOfTasks, int m){
            this.nrOfTasks = nrOfTasks;
        }
        Buffer buffe = new Buffer(m);
        ReentrantLock producersLock = new ReentrantLock();
        ReentrantLock consumersLock = new ReentrantLock();
        ReentrantLock action = new ReentrantLock();
        Condition consumerCond = action.newCondition();
        Condition producerCond = action.newCondition();

        Random RNG = new Random();
        public ProCon(int nr){
            this.nr = nr;
            RNG.setSeed(nr);
        }
        public void pro() throws InterruptedException{
            try {
                producersLock.lock();
                int randomAmount;
                if(m ==1)randomAmount=1;
                else  randomAmount = RNG.nextInt((m /2)) + 1;
                action.lock();
                while (!buffe.canProduce(randomAmount)){
                    producerCond.await();
                }
                for (int i = 0 ; i < randomAmount ; i++){
                    buffe.produceN(randomAmount);
                }
                steps++;
                report(steps);
                consumerCond.signal();
            }
            finally {
                action.unlock();
                producersLock.unlock();
            }



        }
        public void con() throws InterruptedException{
            try {
                consumersLock.lock();
                int randomAmount;
                if(m ==1)randomAmount=1;
                else  randomAmount = RNG.nextInt((m /2)) + 1;
                action.lock();
                while (buffe.canConsume(randomAmount)){
                    consumerCond.await();
                }
                for (int i = 0 ; i < randomAmount ; i++){
                    buffe.consumeN(randomAmount);
                }
                steps++;
                report(steps);
                producerCond.signal();
            }
            finally {
                action.unlock();
                consumersLock.unlock();
            }


        }
        public void report(int step){
            switch (step){
                case firstThreshold:
                    System.out.println("First threshold of " + firstThreshold + " has been reached in: " + (System.nanoTime() - startTime)/Math.pow(10, 9) + " sec");
                    firstThresholdTime = (System.nanoTime() - startTime)/Math.pow(10, 9);
                    break;
                case secondThreshold:
                    System.out.println("Second threshold of " + secondThreshold + " has been reached in: " + (System.nanoTime() - startTime)/Math.pow(10, 9) + " sec");
                    secondThresholdTime = (System.nanoTime() - startTime)/Math.pow(10, 9);
                    break;
                case thirdThreshold:
                    System.out.println("Third threshold of " + thirdThreshold + " has been reached in: " + (System.nanoTime() - startTime)/Math.pow(10, 9) + " sec");
                    thirdThresholdTime = (System.nanoTime() - startTime)/Math.pow(10, 9);
                    finish();
                    break;

            }
        }
        public void finish(){
            double cpuTimeAcc = 0;
            for(int i = 0; i < consumers.size(); i++){
                consumers.get(i).interrupt();
                cpuTimeAcc += cpuTime(consumers.get(i))/Math.pow(10, 9);
                System.out.println(consumers.get(i).getName() + "'s cpu time: " + cpuTime(consumers.get(i))/Math.pow(10, 9) + " sec");
            }
            for(int i = 0; i < producers.size(); i++){
                producers.get(i).interrupt();
                cpuTimeAcc += cpuTime(consumers.get(i))/Math.pow(10, 9);
                System.out.println(producers.get(i).getName() + "'s cpu time: " + cpuTime(producers.get(i))/Math.pow(10, 9) + " sec");
            }
            System.out.println("Cpu total time: " + cpuTimeAcc);
            fileSaver.saveRecord(nr + ";" + firstThresholdTime + ";" + secondThresholdTime + ";" + thirdThresholdTime + ";" + cpuTimeAcc +";\n");
        }
        public void reset(int nr){
            this.nr = nr;
            consumers.clear();
            producers.clear();
            firstThresholdTime = 0;
            secondThresholdTime = 0;
            thirdThresholdTime = 0;
            buffer.clear();
            steps = 0;
            val = 0;
            startTime = System.nanoTime();
        }
        private Void ownTask() throws InterruptedException{
            for (int i = 0; i < nrOf; i++) {
                Math.sin(RNG.nextDouble());
            }
            if (Thread.interrupted()){
                throw new InterruptedException();
            }
            return null;
        }
    }
}