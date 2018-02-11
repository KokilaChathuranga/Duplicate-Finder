package com.dup.find;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;


public class Read implements Runnable {
    private final Logger LOGGER = Logger.getLogger(FindDuplicatesMain.class.getName());
    private final ThreadPoolExecutor executorPool;
    private BlockingQueue queue = null;
    private final File dir;

    public Read(BlockingQueue queue, File dir, ThreadPoolExecutor executorPool) {
        this.queue = queue;
        this.dir = dir;
        this.executorPool = executorPool;
    }

    public void run() {
        try {
            FindDuplicates.find(dir, queue, executorPool);
            LOGGER.info("executing the find duplicate service..");
            //stopping the blocking queue is done through "poisoning" the queue by adding "end"
            //queue.put("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}