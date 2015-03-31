package org.lsm.bluetoothmesh.btxfr;

import org.lsm.bluetoothmesh.database.DataPacket;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by pralav on 3/30/15.
 */
public class TaskQueue {


    private static final ArrayList<LinkedBlockingQueue<DataPacket>> senderQueues = new ArrayList<LinkedBlockingQueue<DataPacket>>(Constants.MAX_THREADS);

    private static final LinkedBlockingQueue<String> schedulerQueue = new LinkedBlockingQueue<String>();
//    private static final LinkedBlockingQueue<UrlCategory> siteToBeProcessedQueue = new LinkedBlockingQueue<UrlCategory>();
//    private static ExecutorService feedExtractorExcecutorService = Executors.newFixedThreadPool(Constants.DOWNLOAD_THREAD_COUNT.getNumericValue());
//    private static ExecutorService contentExtractorExcecutorService = Executors.newFixedThreadPool(Constants.DOWNLOAD_THREAD_COUNT.getNumericValue());
//    private static ExecutorService keywordExtractorExecutorService = Executors.newFixedThreadPool(Constants.DOWNLOAD_THREAD_COUNT.getNumericValue());
//    private static ExecutorService wikiExtractorExecutorService = Executors.newFixedThreadPool(Constants.DOWNLOAD_THREAD_COUNT.getNumericValue());
//    private static ExecutorService entityExtractorExecutorService = Executors.newFixedThreadPool(Constants.DOWNLOAD_THREAD_COUNT.getNumericValue());
    private static volatile TaskQueue instance;
    private static int size;


    public static TaskQueue getInstance() {
        if (instance == null) {
            synchronized (TaskQueue.class) {
                if (instance == null)
                    instance = new TaskQueue();
                    for(int i=0;i<Constants.MAX_THREADS;i++){
                        instance.senderQueues.add(new LinkedBlockingQueue<DataPacket>());
                    }
            }
        }
        return instance;
    }

    public void enqueueForSenderQueue(DataPacket feed,int queueId) throws InterruptedException {
        LinkedBlockingQueue<DataPacket> senderQueue=senderQueues.get(queueId);
        senderQueue.put(feed);
    }

    public DataPacket dequeueFromSenderQueue(int queueId) throws InterruptedException {
        LinkedBlockingQueue<DataPacket> senderQueue=senderQueues.get(queueId);
        return senderQueue.poll();
    }
    public void enqueueForTaskQueue(String address) throws InterruptedException {
        schedulerQueue.put(address);
    }

    public String dequeueFromTaskQueue() throws InterruptedException {

        return schedulerQueue.poll();
    }


    public static int getSize(int queueId) {
        LinkedBlockingQueue<DataPacket> senderQueue=senderQueues.get(queueId);
        return senderQueue.size();
    }

    public static void setSize(int size) {
        TaskQueue.size = size;
    }
}