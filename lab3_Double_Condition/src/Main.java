import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args){
        int nrOfProducers = 4;
        int nrOfConsumers = 4;
        LinkedList<Thread> producers = new LinkedList<>();
        LinkedList<Thread> consumers = new LinkedList<>();
        ProCon producersAndConsumers = new ProCon();
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
            t.setName("P"+i);
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
            t.setName("C"+i);
            consumers.add(t);
        }
        for(int i = 0; i < nrOfConsumers; i++){
            producers.get(i).start();
        }
        for(int i = 0; i < nrOfConsumers; i++){
            consumers.get(i).start();
        }
    }
    public static class ProCon{
        int pauseTime = 0;
        int bufferMaxSize = 10;
        int update = 10;
        LinkedList<Integer> buffer = new LinkedList<>();
        HashMap<String, String> waitingMap = new HashMap<>();
        ReentrantLock lock = new ReentrantLock();
        Condition producers = lock.newCondition();
        Condition consumers = lock.newCondition();
        FileSaver fileSaver = new FileSaver("resultsTwoConditions.csv");
        Random RNG = new Random();
        ArrayList<Integer> timesList = new ArrayList<>();
        static int steps = 0; //one step == one production or one consumption
        static int avgTimeAcc = 0;
        static int val = 0;
        public void pro() throws InterruptedException{
            try {
                lock.lock();
                int randomAmount = RNG.nextInt((bufferMaxSize /2)) + 1;
                waitingMap.put(Thread.currentThread().getName(), Integer.toString(steps));
                while (buffer.size() + randomAmount > bufferMaxSize){
                    producers.await();
                }
                for (int i = 0 ; i < randomAmount ; i++){
                    val++;
                    buffer.add(val);
                }
                int waitingTime = steps - Integer.parseInt(waitingMap.remove(Thread.currentThread().getName()));
                System.out.println("[" + Thread.currentThread().getName() + "] Producer produced " + randomAmount + " values");
                System.out.println("[" + Thread.currentThread().getName() + "] Producer waited for " + waitingTime + " steps");
                updateStats(waitingTime);
                consumers.signal();
                Thread.sleep(pauseTime);
            }
            finally {
                lock.unlock();
            }


        }
        public void con() throws InterruptedException{
            try {
                lock.lock();
                int randomAmount = RNG.nextInt((bufferMaxSize /2)) + 1;
                waitingMap.put(Thread.currentThread().getName(), Integer.toString(steps));
                while (buffer.size() - randomAmount < 0){
                    consumers.await();
                }
                for (int i = 0 ; i < randomAmount ; i++){
                    buffer.removeFirst();
                }
                int waitingTime = steps - Integer.parseInt(waitingMap.remove(Thread.currentThread().getName()));
                System.out.println("[" + Thread.currentThread().getName() + "] Consumer consumed " + randomAmount + " values");
                System.out.println("[" + Thread.currentThread().getName() + "] Consumer waited for " + waitingTime + " steps");
                System.out.println("Buffer: " + buffer.size());
                updateStats(waitingTime);
                producers.signal();
                Thread.sleep(pauseTime);
            }
            finally {
                lock.unlock();
            }

        }
        public void updateStats(int waitingTime){
            avgTimeAcc += waitingTime;
            steps++;
            int index = Collections.binarySearch(timesList, waitingTime);
            if(index < 0){
                timesList.add(-(index+1), waitingTime);
            }
            else {
                timesList.add(index, waitingTime);
            }
            if(steps%update==0){
                int median = (timesList.get(timesList.size()/2) + timesList.get(timesList.size()/2 - 1))/2;
                int nineTenQuantile = (timesList.get(9*timesList.size()/10) + timesList.get(9*timesList.size()/10 - 1))/2;
                fileSaver.saveRecord(steps + ";" + avgTimeAcc/steps + ";" + median + ";" + nineTenQuantile + ";" +timesList.get(timesList.size()-1) +";\n");
            }
        }
    }
}