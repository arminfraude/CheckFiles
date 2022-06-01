package test;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ReadWriteHandling.ReaderThread;
import ReadWriteHandling.WriterThread;
import utils.Utils;

public class ReentrantRWDemo {
	
	private static final Logger LOGGER = LogManager.getLogger(ReentrantRWDemo.class);	
	private final Map<String, String> m = new TreeMap<String, String>();
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	    
	// get method for getting values from map
	// it can be used by many read threads simultaneously
	public String get(String key) {
		LOGGER.info("In get method waiting to acquire lock");
	    rwl.readLock().lock();
	    LOGGER.info("In get method acquired read lock");
	    try { 
	      try {
	        Thread.sleep(1500);
	      } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      }
	      return m.get(key); 
	    }
	    finally { 
	      rwl.readLock().unlock(); 
	      LOGGER.info("In get method released read lock");
	    }
	}
	    
	// Put method to store  key, value in a map
	// it acquires a write lock so only one thread at a time
	public String put(String key, String value) {
		LOGGER.info("In put method waiting to acquire lock");
	    rwl.writeLock().lock();
	    LOGGER.info("In put method acquired write lock");
	    try { 
	      try {
	        Thread.sleep(1000);
	      } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      }
	      return m.put(key, value); 
	    }
	    finally { 
	      rwl.writeLock().unlock(); 
	      LOGGER.info("In put method released write lock");
	    }
	}
	    
	public void display(){
		m.entrySet().forEach(System.out::println);
	}
	    
	public static void main(String... args) {
	
	/*	ReentrantRWDemo rwDemo = new ReentrantRWDemo();
	    // Putting some values in the map
	    rwDemo.put("1", "One");
	    rwDemo.put("2", "Two");
	    rwDemo.put("3", "Three");
	    
	    // Starting two read threads and one write thread
	    Thread rThread1 = new Thread(new ReaderThread(rwDemo));
	    Thread wThread = new Thread(new WriterThread(rwDemo));
	    Thread rThread2 = new Thread(new ReaderThread(rwDemo));
	    rThread1.start();
	    wThread.start();
	    rThread2.start();
	    
	    // Wait for the threads to finish, then only go for display method
	    try {
	    	rThread1.join();
	    	wThread.join();
	    	rThread2.join();
	    } catch (InterruptedException e) {
	    	LOGGER.info(Utils.getExceptionInfos(e));
	    }    
	    rwDemo.display();     
	    */   
	}
}

/*
class ReadThread implements Runnable {
		
	private static final Logger LOGGER = LogManager.getLogger(ReadThread.class);	
	ReentrantRWDemo rwDemo;
		
	ReadThread(ReentrantRWDemo rwDemo) {
		this.rwDemo = rwDemo;
	}
	
	public void run() {
		LOGGER.info("Value - " + rwDemo.get("1"));
	}
}

	
class WriterThread implements Runnable {
		
	private static final Logger LOGGER = LogManager.getLogger(WriterThread.class);	
	ReentrantRWDemo rwDemo;
		
	WriterThread(ReentrantRWDemo rwDemo) {
		this.rwDemo = rwDemo;
	}
	
	public void run() {
		rwDemo.put("4", "Four");
	}
}
*/