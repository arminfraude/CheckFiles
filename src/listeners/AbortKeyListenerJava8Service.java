package listeners;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jna.CLibrary;
import utils.Utils;

/*
 * Grundlegendes Problem ist der gemeinsame Zugriff lesend wie schreibend von System.in
 * Daher nicht wirklich einsetzbar
 * 
 * @see AbortkeyListenerJna, AbortKeyListenerJava8Pool, AbortKeyListenerJava8
 *  
 */
@Deprecated
public class AbortKeyListenerJava8Service implements AbortKeyListenerJava8 {
	
	private static final Logger LOGGER = LogManager.getLogger(AbortKeyListenerJava8Service.class);
	private static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(0);
	private static CLibrary clib = CLibrary.INSTANCE;
	private static Runnable TASK;
	private static ScheduledFuture<?> SCHEDULED_FUTURE; 
	
	public AbortKeyListenerJava8Service() {
		TASK = () -> {
		    try {
		    	LOGGER.info("Scheduling: " + System.nanoTime());				    	
		    //  LOGGER.info("Bitte Zeichen eingeben: ");
				int read = clib._getwch();
			//	LOGGER.info("read with _getwch: " + read);
			//	LOGGER.info("read with _getwch conv: " + (char)read);				        
		        
		        if(read == Utils.ESCAPE_CODE) {
					LOGGER.info("Stop Listener after <ESCAPE> ...");
					EXECUTOR_SERVICE.shutdown();
				}
		    }
		    catch (Exception e) {
		    	LOGGER.error("task interrupted");
		    	if (!EXECUTOR_SERVICE.isTerminated()) {
		    		LOGGER.error("cancel non-finished tasks");
		        }
		    	List<Runnable> list = EXECUTOR_SERVICE.shutdownNow();
		    	for(Runnable r : list) {
		    		LOGGER.info("Runnable r: " + r.toString());
		    	}
		        LOGGER.error("shutdown finished");
		    }
		};
	}	
	
	@Override
	public ScheduledFuture<?> listen(int initialDelayMillis, int delayMillis) {
		SCHEDULED_FUTURE = EXECUTOR_SERVICE.scheduleWithFixedDelay(TASK, initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS);
		return SCHEDULED_FUTURE;
	}
	
	@Override
	public ScheduledExecutorService getExecutorService() {
		return EXECUTOR_SERVICE;
	}	
	
	@Override
	public void shutdownSoft() {
		EXECUTOR_SERVICE.shutdown();
	}
	
	/*
	 * Problematisch: This means that shutdownNow() will not stop the thread
	 */
	@Override
	public void shutdownHard() {
		EXECUTOR_SERVICE.shutdownNow();
	}
	
	@Override
	public Runnable getTask() {
		return TASK;
	}
	
	// Es ist ein Versuch ....
	@Override
	public List<Future<Object>> invokeAll() throws InterruptedException {
		Callable<Object> c = Executors.callable(TASK);
		return EXECUTOR_SERVICE.invokeAll((Collection<? extends Callable<Object>>) c);
	}
	
	
}