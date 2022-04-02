package com.company;


import java.util.concurrent.Callable;

public interface FutureReceiver {

    boolean getIsReady();

    int getResult();

    int waiting(Callable<Void> ownTask, int nrOfChecks) throws Exception;
}
