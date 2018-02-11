package com.dup.find;

import sun.swing.ImageIconUIResource;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class FindDuplicatesGUI extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(FindDuplicatesMain.class.getName());
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int KEEP_ALIVE_TIME = 5000;
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private static String searchDirectory;
    private static String resultFileName;
    private static String searchThreadCount;


    private JPanel rootPanel;
    private JPanel labelPanel;
    private JPanel textFieldPanel;
    private JPanel buttonPanel;
    private JLabel searchDirectoryLabel;
    private JLabel resultFileNameLabel;
    private JLabel searchThreadCountLabel;
    private JTextField searchDirectoryTextField;
    private JTextField resultFileNameTextField;
    private JTextField searchThreadCountTextField;
    private JButton searchButton;
    private JButton pauseButton;
    private JButton continueButton;
    private JButton stopButton;
    private JTextArea textMsgField;
    private JButton browseButton;
    private static Write write;
    private static Thread writeThread;
    private static PausableThreadPoolExecutor executorPool;
    public static final ImageIcon IMAGE_ICON = new ImageIcon("resources/icon.ico");

    public FindDuplicatesGUI() throws HeadlessException {
        super("Duplicate Finder");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //call the initiate method to run threads
                searchDirectory = searchDirectoryTextField.getText();
                resultFileName = resultFileNameTextField.getText();
                searchThreadCount = searchThreadCountTextField.getText();
                //check if all the fields are provided
                if (searchDirectory.isEmpty() || resultFileName.isEmpty() || searchThreadCount.isEmpty()) {
                    textMsgField.setText("Please provide all details.");
                    //check if the directory exists
                } else if (!new File(searchDirectory).isDirectory()) {
                    textMsgField.setText("Supplied directory does not exist.");
                } else {
                    LOGGER.info("Starting the findDuplicates service..");
                    initiate();
                    textMsgField.setText("Search started! Press Stop to stop searching..");
                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //pause the thread
                executorPool.pause();
                LOGGER.info("Thread pool executor paused.");
                textMsgField.setText("Search paused!");
            }
        });
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //continue thread execution
                executorPool.resume();
                LOGGER.info("Thread pool executor resumed.");
                textMsgField.setText("Search resumed!");
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executorPool.shutdown();
                writeThread.interrupt();
                textMsgField.setText("Search stopped. Please Look into the " + resultFileName + " for results.");
            }
        });
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browse();
            }
        });

        textMsgField.setPreferredSize(new Dimension(20,40));
        textMsgField.setLineWrap(true);
        textMsgField.setWrapStyleWord(true);
    }

    public static void main(String[] args) {
        FindDuplicatesGUI findDuplicatesGUI = new FindDuplicatesGUI();
        JFrame frame = new JFrame("Duplicate Finder");
        frame.setContentPane(findDuplicatesGUI.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setSize(new Dimension(500,300));
        frame.setIconImage(IMAGE_ICON.getImage());
        frame.setVisible(true);
        LOGGER.info("Starting the GUI..");
    }

    private static void initiate() {
        BlockingQueue queue = new LinkedBlockingQueue();
        try {
            //noinspection unchecked
            queue.put("");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorPool = new PausableThreadPoolExecutor(
                Integer.parseInt(searchThreadCount),
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TIME_UNIT,
                new ArrayBlockingQueue<Runnable>(4));
        File dir = new File(searchDirectory);
        try {
            FindDuplicates.find(dir, queue, executorPool);
        } catch (Exception e) {
            e.printStackTrace();
        }
        write = new Write(queue, resultFileName);
        writeThread = new Thread(write);
        writeThread.start();
        LOGGER.info("Starting the write thread.");
    }

    private void browse() {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Choose a directory to search for duplicates:");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isDirectory()) {
                searchDirectoryTextField.setText(fileChooser.getSelectedFile().toString());
            }
        }
    }
}