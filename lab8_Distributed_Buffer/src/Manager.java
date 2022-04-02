import org.jcsp.lang.*;

import java.util.Arrays;

public class Manager implements CSProcess {
    private final AltingChannelOutputInt[] consChannels;
    private final AltingChannelOutputInt[] prodChannels;
    private int insertLap = 0;
    private int withdrawLap = 0;
    private final int[] buffers;
    private final int[] buffersWithdrawIndex;
    private final int[] buffersInsertIndex;
    private int insertPosition = 0;
    private int withdrawPosition = 0;
    private final int bufferSize;


    public Manager(One2OneChannelSymmetricInt[] prodChannels, One2OneChannelSymmetricInt[] consChannels, int[] buffers) {
        this.consChannels = new AltingChannelOutputInt[consChannels.length];
        for (int i = 0; i < consChannels.length; i++)
            this.consChannels[i] = consChannels[i].out();
        this.prodChannels = new AltingChannelOutputInt[prodChannels.length];
        for (int i = 0; i < prodChannels.length; i++)
            this.prodChannels[i] = prodChannels[i].out();
        this.buffers = buffers;
        this.bufferSize = buffers.length;
        this.buffersWithdrawIndex = new int[bufferSize];
        System.arraycopy(buffers, 0, buffersWithdrawIndex, 0, bufferSize);
        this.buffersInsertIndex = new int[bufferSize];
        System.arraycopy(buffers, 0, buffersInsertIndex, 0, bufferSize);
    }

    @Override
    public void run() {
        Guard[] prodGuards = new Guard[prodChannels.length];
        System.arraycopy(prodChannels, 0, prodGuards, 0, prodChannels.length);
        Guard[] consGuards = new Guard[consChannels.length];
        System.arraycopy(consChannels, 0, consGuards, 0, consChannels.length);
        Guard[] allGuards = new Guard[prodChannels.length + consChannels.length];
        System.arraycopy(prodChannels, 0, allGuards, 0, prodChannels.length);
        System.arraycopy(consChannels, 0, allGuards, prodChannels.length, consChannels.length);

        final Alternative prodAlt = new Alternative(prodGuards);
        final Alternative consAlt = new Alternative(consGuards);
        final Alternative allAlt = new Alternative(allGuards);
        Alternative alt;
        while (true) {
            if (!insertGuard())
                alt = consAlt;
            else if (!withdrawGuard())
                alt = prodAlt;
            else alt = allAlt;
            int index = alt.fairSelect();

            if (alt == prodAlt) {
                prodChannels[index].write(insert());
            } else if (alt == consAlt) {
                consChannels[index].write(withdraw());
            } else {
                if (index < prodChannels.length) {
                    prodChannels[index].write(insert());
                } else {
                    consChannels[index - prodChannels.length].write(withdraw());
                }
            }
        }
    }

    private boolean withdrawGuard() {
        if (Arrays.equals(buffersInsertIndex, buffersWithdrawIndex) && insertLap == withdrawLap) {
            return false;
        } else return true;
    }

    private boolean insertGuard() {
        if (Arrays.equals(buffersInsertIndex, buffersWithdrawIndex) && insertLap == withdrawLap + 1) {
            return false;
        } else return true;
    }

    private int insert() {
        boolean isEmpty = true;
        for (int i = 0; i < bufferSize; i++) {
            if (buffersInsertIndex[i] != 0) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            System.arraycopy(buffers, 0, buffersInsertIndex, 0, bufferSize);
            insertLap++;
        }
        int buffer;
        while (true) {
            if (buffersInsertIndex[insertPosition] > 0) {
                buffer = insertPosition;
                buffersInsertIndex[insertPosition]--;
                insertPosition++;
                insertPosition = insertPosition % bufferSize;
                break;
            }
            insertPosition++;
            insertPosition = insertPosition % bufferSize;
        }
        return buffer;
    }

    private int withdraw() {
        boolean isEmpty = true;
        for (int i = 0; i < bufferSize; i++) {
            if (buffersWithdrawIndex[i] != 0) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            System.arraycopy(buffers, 0, buffersWithdrawIndex, 0, bufferSize);
            withdrawLap++;
        }
        int buffer;
        while (true) {
            if (buffersWithdrawIndex[withdrawPosition] > 0) {
                buffer = withdrawPosition;
                buffersWithdrawIndex[withdrawPosition]--;
                withdrawPosition++;
                withdrawPosition = withdrawPosition % bufferSize;
                break;
            }
            withdrawPosition++;
            withdrawPosition = withdrawPosition % bufferSize;
        }
        return buffer;
    }
}
