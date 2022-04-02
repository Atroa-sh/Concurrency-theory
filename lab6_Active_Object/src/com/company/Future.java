package com.company;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Future implements FutureReceiver {
    private int result;
    private boolean isReady = false;
    public final Lock futureLock = new ReentrantLock();
    public final Condition condition = futureLock.newCondition();

    @Override
    public boolean getIsReady() {
        return isReady;
    }

    @Override
    public int getResult() {
        return result;
    }

    @Override
    public int waiting(Callable<Void> ownTask,int nrOfChecks) throws Exception  {
        int result = -1;
        for (int i = 0 ; i < nrOfChecks ; i++){
            ownTask.call();
            futureLock.lock();
            try {
                if (getIsReady()) {
                    result = getResult();
                }
            } finally {
                futureLock.unlock();
            }
        }
        futureLock.lock();
        try {
            while (!getIsReady()) condition.await();
        }
        finally {
            result = getResult();
            futureLock.unlock();
        }
        return result;
    }

    public void setIsReady(boolean state) {
        isReady = state;
    }


    public void setResult(int result) {
        this.result = result;
    }
}