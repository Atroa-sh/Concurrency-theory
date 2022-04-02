import org.jcsp.lang.*;

import java.util.ArrayList;
import java.util.Random;

public class Producer implements CSProcess, Stats {
    private final AltingChannelOutputInt[] bufferChannels;
    private final AltingChannelInputInt managerChannel;
    private int producedItems = 0;
    private final int number;
    private final Random RNG = new Random();

    public Producer(One2OneChannelSymmetricInt[] bufferChannels, One2OneChannelSymmetricInt managerChannel, int number) {
        this.bufferChannels = new AltingChannelOutputInt[bufferChannels.length];
        for (int i = 0; i < bufferChannels.length; i++)
            this.bufferChannels[i] = bufferChannels[i].out();
        this.managerChannel = managerChannel.in();
        this.number = number;
    }

    @Override
    public void run() {
        int buffer;
        while (true) {
            buffer = managerChannel.read();
            bufferChannels[buffer].write(RNG.nextInt());
            producedItems++;
        }
    }

    public String printStats() {
        return "P" + number + " produced " + producedItems;
    }
}
