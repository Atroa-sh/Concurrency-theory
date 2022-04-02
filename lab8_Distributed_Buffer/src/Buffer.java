import org.jcsp.lang.*;

import java.util.ArrayList;

public class Buffer implements CSProcess, Stats {
    private final AltingChannelInputInt[] prodChannel;
    private final AltingChannelOutputInt[] consChannel;
    private final ArrayList<Integer> data = new ArrayList<>();
    private final int maxCapacity;
    private int received = 0;
    private int passed = 0;
    private final int number;

    public Buffer(One2OneChannelSymmetricInt[] prodChannel, One2OneChannelSymmetricInt[] consChannel, int capacity, int number) {
        this.prodChannel = new AltingChannelInputInt[prodChannel.length];
        for (int i = 0; i < prodChannel.length; i++)
            this.prodChannel[i] = prodChannel[i].in();
        this.consChannel = new AltingChannelOutputInt[consChannel.length];
        for (int i = 0; i < consChannel.length; i++)
            this.consChannel[i] = consChannel[i].out();
        this.maxCapacity = capacity;
        this.number = number;
    }

    @Override
    public void run() {
        Guard[] guardsProds = new Guard[prodChannel.length];
        System.arraycopy(prodChannel, 0, guardsProds, 0, prodChannel.length);
        Guard[] guardsCons = new Guard[consChannel.length];
        System.arraycopy(consChannel, 0, guardsCons, 0, consChannel.length);
        Guard[] guardsAll = new Guard[prodChannel.length + consChannel.length];
        System.arraycopy(prodChannel, 0, guardsAll, 0, prodChannel.length);
        System.arraycopy(consChannel, 0, guardsAll, prodChannel.length, consChannel.length);

        Alternative alt;
        final Alternative altProds = new Alternative(guardsProds);
        final Alternative altCons = new Alternative(guardsCons);
        final Alternative altAll = new Alternative(guardsAll);

        while (true) {
            if (data.size() == 0)
                alt = altProds;
            else if (data.size() == maxCapacity)
                alt = altCons;
            else alt = altAll;

            int index = alt.fairSelect();

            if (alt == altProds) {
                data.add(prodChannel[index].read());
                received++;
            } else if (alt == altCons) {
                consChannel[index].write(data.remove(0));
                passed++;
            } else {
                if (index < prodChannel.length) {
                    data.add(prodChannel[index].read());
                    received++;
                } else {
                    consChannel[index - prodChannel.length].write(data.remove(0));
                    passed++;
                }
            }
        }
    }

    public String printStats() {
        return "B" + number + " received " + received + "\n" + "B" + number + " passed " + passed;
    }
}
