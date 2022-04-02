package com.company;

import java.util.LinkedList;
import java.util.List;

public class TasksQueues {
    public final List<Task> producerQueue = new LinkedList<>();
    public final List<Task> consumerQueue = new LinkedList<>();
    public boolean newProdRequest = false;
    public boolean newConsRequest = false;
}
