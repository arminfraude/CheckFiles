package ReadWriteHandling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import test.ReentrantRWDemo;

/*
 * Versuch diese Demo auf ReadWriteCmdMenu anzupassen abgebrochen
 * 
 * @see WriterThread
 */
public class ReaderThread implements Runnable {
	
	private static final Logger LOGGER = LogManager.getLogger(ReaderThread.class);	
	private ReentrantRWDemo rwDemo;
	//private ReadWriteCmdMenu rwCmd;
	
	public ReaderThread(ReentrantRWDemo rwDemo) {
		this.rwDemo = rwDemo;
	}
	
	@Override
	public void run() {
		LOGGER.info("Value - " + rwDemo.get("1"));
	}
}
