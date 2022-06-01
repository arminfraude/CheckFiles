package ReadWriteHandling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import test.ReentrantRWDemo;

/*
 * Versuch diese Demo auf ReadWriteCmdMenu anzupassen abgebrochen
 * 
 * @see ReaderThread
 */
public class WriterThread implements Runnable {
	
	private static final Logger LOGGER = LogManager.getLogger(WriterThread.class);	
	private ReentrantRWDemo rwDemo;
	//private ReadWriteCmdMenu rwCmd;
		
	public WriterThread(ReentrantRWDemo rwDemo) {
		this.rwDemo = rwDemo;
	}
	
	@Override
	public void run() {
		rwDemo.put("4", "Four");
	}
}


	

