import org.jcsp.lang.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.ArrayList;

public final class PCMain {
    private final static int nrOfConsumers = 5;
    private final static int nrOfProducers = 5;
    private final static int[] buffers = {5, 5, 5, 5, 5};
    private final static int bufferSize = buffers.length;

    public static void main(String[] args) {
        CSProcess[] procList = new CSProcess[nrOfConsumers + nrOfProducers + bufferSize + 1];

        //signal handler
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal sig) {
                for (int i = 0; i < procList.length - 1; i++) {
                    Stats process = (Stats) procList[i];
                    System.out.println(process.printStats());
                }
                System.exit(1);
            }
        });

        //creating channels
        One2OneChannelSymmetricInt[] prodManChannels = new One2OneChannelSymmetricInt[nrOfProducers];
        One2OneChannelSymmetricInt[] consManChannels = new One2OneChannelSymmetricInt[nrOfConsumers];

        One2OneChannelSymmetricInt[][] consBuffChannels = new One2OneChannelSymmetricInt[nrOfConsumers][bufferSize];
        One2OneChannelSymmetricInt[][] prodBuffChannels = new One2OneChannelSymmetricInt[nrOfProducers][bufferSize];

        One2OneChannelSymmetricInt[][] buffConsChannels = new One2OneChannelSymmetricInt[bufferSize][nrOfConsumers];
        One2OneChannelSymmetricInt[][] buffProdChannels = new One2OneChannelSymmetricInt[bufferSize][nrOfProducers];

        for (int i = 0; i < nrOfProducers; i++)
            prodManChannels[i] = Channel.one2oneSymmetricInt();

        for (int i = 0; i < nrOfConsumers; i++)
            consManChannels[i] = Channel.one2oneSymmetricInt();

        for (int i = 0; i < nrOfProducers; i++)
            for (int j = 0; j < bufferSize; j++)
                prodBuffChannels[i][j] = Channel.one2oneSymmetricInt();


        for (int i = 0; i < nrOfConsumers; i++)
            for (int j = 0; j < bufferSize; j++)
                consBuffChannels[i][j] = Channel.one2oneSymmetricInt();



        for (int i = 0; i < bufferSize; i++)
            for (int j = 0; j < nrOfProducers; j++)
                buffProdChannels[i][j] = prodBuffChannels[j][i];



        for (int i = 0; i < bufferSize; i++)
            for (int j = 0; j < nrOfConsumers; j++)
                buffConsChannels[i][j] = consBuffChannels[j][i];




        //creating objects
        Manager manager = new Manager(prodManChannels, consManChannels, buffers);

        int procListIndex = 0;
        for (int i = 0; i < nrOfProducers; i++) {
            procList[procListIndex] = new Producer(prodBuffChannels[i], prodManChannels[i], i);
            procListIndex++;
        }

        for (int i = 0; i < nrOfConsumers; i++) {
            procList[procListIndex] = new Consumer(consBuffChannels[i], consManChannels[i], i);
            procListIndex++;
        }


        for (int i = 0; i < bufferSize; i++) {
            procList[procListIndex] = new Buffer(buffProdChannels[i], buffConsChannels[i], buffers[i], i);
            procListIndex++;
        }
        procList[procListIndex] = manager;


        //fire up the processes
        Parallel par = new Parallel(procList);
        par.run();

    }

}

