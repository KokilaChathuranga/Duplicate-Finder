package com.dup.find;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class Write implements Runnable {
    private final Logger LOGGER = Logger.getLogger(FindDuplicatesMain.class.getName());
    private final String resultFileName;
    private final BlockingQueue queue;

    public Write(BlockingQueue queue, String resultFileName) {
        this.queue = queue;
        this.resultFileName = resultFileName;
    }

    public void run() {
        try {
            LOGGER.info("Starts writing to the file..");
            int i = 1;
            //stopping the blocking queue is done by interrupt
            while (!Thread.interrupted()) {
                //avoid empty values
                if (!queue.take().toString().equals("")) {
                    //print the duplicates one by one - not all at once
                    File file = new File(resultFileName);
                    FileWriter fileWriter = new FileWriter(file, true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    PrintWriter printWriter = new PrintWriter(bufferedWriter);
                    printWriter.println(i + "." + queue.take());
                    printWriter.close();
                    i++;
                }
                else if(queue.take().toString().equalsIgnoreCase("end")){
                    LOGGER.info("Search completed.");
                }
            }
        } catch (InterruptedException | UnsupportedEncodingException | FileNotFoundException e) {
            //stop the write thread
            LOGGER.info("thread is interrupted");
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}