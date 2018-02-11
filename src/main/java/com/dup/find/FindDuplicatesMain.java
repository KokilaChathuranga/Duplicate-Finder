/*This is used to test the application without GUI
* run configurations:
* main class : com.dup.find.FindDuplicatesMain
* program arguments : <path to directory>
*     */
package com.dup.find;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class FindDuplicatesMain {
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final String RESULT_FILE_NAME = "results.txt";
    private static final int SEARCH_THREAD_COUNT = 5;
    private static final Logger LOGGER = Logger.getLogger(FindDuplicatesMain.class.getName());

    public static void main(String[] args) throws Exception {
        BlockingQueue queue = new LinkedBlockingQueue();
        //noinspection unchecked
        queue.put("");

        //if args length is zero, then path is not specified
        if (args.length < 1) {
            System.out.println("Please supply a path to directory to find duplicate files in.");
            return;
        }
        File dir = new File(args[0]);
        //check if path name refers  to an actual directory
        if (!dir.isDirectory()) {
            System.out.println("Supplied directory does not exist.");
            return;
        }
        initiate(queue, dir);
    }

    private static void initiate(BlockingQueue queue, File dir) throws Exception {
        PausableThreadPoolExecutor executorPool = new PausableThreadPoolExecutor(
                SEARCH_THREAD_COUNT,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TIME_UNIT,
                new ArrayBlockingQueue<Runnable>(4));
        LOGGER.info("Starting findDuplicates service..");
        FindDuplicates.find(dir, queue, executorPool);
        LOGGER.info("Starting write service..");
        Write write = new Write(queue, RESULT_FILE_NAME);
        Thread writeThread = new Thread(write);
        writeThread.start();

        //test Pause
        executorPool.pause();
        System.out.println("Thread pool is paused :" + executorPool.isPaused());
        System.out.println("Thread pool is resumed :" + executorPool.isRunning());

        //test Resume
        executorPool.resume();
        System.out.println("Thread pool is resumed :" + executorPool.isRunning());
        System.out.println("Thread pool is paused :" + executorPool.isPaused());

        Thread.sleep(10000);
        //writer thread is interrupted and pool is shutdown after 10 seconds.
        writeThread.interrupt();
        executorPool.shutdown();
    }
}