package com.company;

public class Executioner {
    private final Buffer buffer;

    Executioner(Buffer buffer) {
        this.buffer = buffer;
    }

    public void executeTask(Task t) {
        switch (t.type) {
            case 0 -> {
                t.future.futureLock.lock();
                try {
                    int result = buffer.produceN(t.count);
                    t.future.setResult(result);
                    t.future.setIsReady(true);
                    t.future.condition.signal();
                } finally {
                    t.future.futureLock.unlock();
                }


            }
            case 1 -> {
                t.future.futureLock.lock();
                try {
                    int result = buffer.consumeN(t.count);
                    t.future.setResult(result);
                    t.future.setIsReady(true);
                    t.future.condition.signal();
                } finally {
                    t.future.futureLock.unlock();
                }


            }
            default -> throw new IllegalArgumentException("Invalid type of task");
        }
    }

    public boolean guard(Task t) {
        if (t.type == 0 && buffer.canProduce(t.count)) return true;
        else if (t.type == 1 && buffer.canConsume(t.count)) return true;
        else return false;
    }
}
