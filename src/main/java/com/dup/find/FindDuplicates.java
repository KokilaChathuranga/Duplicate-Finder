package com.dup.find;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class FindDuplicates {
    private static final Logger LOGGER = Logger.getLogger(FindDuplicatesMain.class.getName());
    private static MessageDigest md;

    static {
        try {
            //this is used for hashing
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("cannot initialize SHA-512 hash function", e);
        }
    }

    private static ConcurrentHashMap<String, List<String>> hashContentToPathMap = new ConcurrentHashMap<>();

    public static void find(File directory, BlockingQueue queue, ThreadPoolExecutor executorPool) throws Exception {

        LOGGER.info("Finding sub directories and files..");
        String hash;
        //noinspection ConstantConditions
        for (File child : directory.listFiles()) {
            //if child is a directory, find inside that..
            if (child.isDirectory()) {
                executorPool.execute(new Thread(new Read(queue, child, executorPool)));
                //if it's a file,
            } else {
                /*create the hash content
                check for the key in the map
                if not exists create a new map with hash and path and add the new path, Else add the paths to the queue*/
                hash = makeHash(child);
                List<String> paths = hashContentToPathMap.get(hash);
                if (paths == null) {
                    paths = new LinkedList<>();
                    paths.add(child.getAbsolutePath());
                    hashContentToPathMap.put(hash, paths);
                } else {
                    paths.add(child.getAbsolutePath());
                    LOGGER.info("Adding files to the queue..");
                    //noinspection unchecked
                    queue.put(paths);
                }
            }
        }
    }

    //returns the hash code string of given file
    private static String makeHash(File infile) throws Exception {
        LOGGER.info("Creating hashes for file contents..");
        FileInputStream fin = new FileInputStream(infile);//create the file input stream from infile
        byte data[] = new byte[(int) infile.length()];//create the byte array of the contents
        fin.read(data);//read the byte stream
        fin.close();
        return new BigInteger(1, md.digest(data)).toString(16);//create hash using md.digest() and convert to a String
    }
}
