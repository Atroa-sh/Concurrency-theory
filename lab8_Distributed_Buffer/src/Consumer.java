import org.jcsp.lang.*;


public class Consumer implements CSProcess, Stats {
    private final AltingChannelInputInt[] bufferChannels;
    private final AltingChannelInputInt managerChannel;
    private int receivedItems = 0;
    private final int number;

    public Consumer(final One2OneChannelSymmetricInt[] bufferChannels, One2OneChannelSymmetricInt managerChannel, int number) {
        this.bufferChannels = new AltingChannelInputInt[bufferChannels.length];
        for (int i = 0; i < bufferChannels.length; i++)
            this.bufferChannels[i] = bufferChannels[i].in();
        this.managerChannel = managerChannel.in();
        this.number = number;
    }

    @Override
    public void run() {
        int buffer;
        while (true) {
            buffer = managerChannel.read();
            bufferChannels[buffer].read();
            receivedItems++;
        }
    }

    public String printStats() {
        return "C" + number + " consumed " + receivedItems;
    }
}