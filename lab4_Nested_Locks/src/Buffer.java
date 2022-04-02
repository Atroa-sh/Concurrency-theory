

public class Buffer {
    private final int maxSize;
    private int empty;
    private int filled;


    Buffer(int m) {
        this.maxSize = 2 * m;
        empty = maxSize;
        filled = 0;
    }


    public boolean canProduce(int n) {
        if (maxSize - filled >= n) return true;
        else return false;
    }

    public boolean canConsume(int n) {
        if (filled >= n) return true;
        else return false;
    }

    public int produceN(int n) {
        filled += n;
        empty -= n;
        return n;
    }

    public int consumeN(int n) {
        filled -= n;
        empty += n;
        return n;
    }

    public boolean lessThanHalf() {
        return filled < maxSize / 2;
    }


}
