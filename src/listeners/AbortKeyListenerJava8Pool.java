package listeners;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jna.CLibrary;
import utils.Utils;

/*
 * Grundlegendes Problem ist der gemeinsame Zugriff lesend wie schreibend von System.in
 * Daher nicht wirklich einsetzbar
 * 
 * @see AbortkeyListenerJna, AbortKeyListenerJava8Service, AbortKeyListenerJava8
 *  
 */
@Deprecated
public class AbortKeyListenerJava8Pool implements AbortKeyListenerJava8 {

	private static final Logger LOGGER = LogManager.getLogger(AbortKeyListenerJava8Pool.class);
	private static ScheduledThreadPoolExecutor EXECUTOR;
	private static Task TASK;
	private static CLibrary clib = CLibrary.INSTANCE;
	private static ScheduledFuture<?> SCHEDULED_FUTURE; 
	
	public AbortKeyListenerJava8Pool() {
		EXECUTOR = new ScheduledThreadPoolExecutor(1);
		EXECUTOR.setRemoveOnCancelPolicy(true);
		EXECUTOR.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		EXECUTOR.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		TASK = new Task("task1");		
	}
	
	@Override
	public ScheduledFuture<?> listen(int initialDelayMillis, int delayMillis) {
		SCHEDULED_FUTURE = 
				EXECUTOR.scheduleAtFixedRate(TASK, initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS);
		
		return SCHEDULED_FUTURE;
	}
	
	@Override
	public ScheduledThreadPoolExecutor getExecutorService() {
		return EXECUTOR;
	}
	
	@Override
	public void shutdownSoft() {
		EXECUTOR.shutdown();
	}
	
	@Override
	public void shutdownHard() {
		EXECUTOR.shutdownNow();
	}
	
	@Override
	public Runnable getTask() {
		return TASK;
	}
	
	// Es ist ein Versuch ....
	@Override
	public List<Future<Object>> invokeAll() throws InterruptedException {
		Callable<Object> c = Executors.callable(TASK);
		//return EXECUTOR.invokeAll((Collection<? extends Callable<Object>>) c);
		return null;
	}
	
	private class Task implements Runnable {
		
	    String taskName;
	    
	    public Task(String taskName) {
	        this.taskName = taskName;
	    }	    
	    
	    public void run() {
	    	
	    	/*
	    	 * Zu shutdownNow:
	    	 * There are no guarantees beyond best-effort attempts to stop 
	    	 * processing actively executing tasks. 
	    	 * This implementation cancels tasks via Thread.interrupt(), 
	    	 * so any task that fails to respond to interrupts 
	    	 * may never terminate.
	    	 * 
	    	 * Daher der Gedanke hier, dass vll helfen könnte ...
	    	 */
	    	if(!Thread.currentThread().isInterrupted()) {
		    	try {
			    	LOGGER.info("Scheduling: " + System.nanoTime());	
			    	int read = clib._getwch();
			    	if(read == Utils.ESCAPE_CODE) {
						LOGGER.info("Stop Listener after <ESCAPE> ...");
						EXECUTOR.shutdown();
					}
		    	}
		    	catch (Exception e) {
			    	LOGGER.error("task interrupted");
			    	if (!EXECUTOR.isTerminated()) {
			    		LOGGER.error("cancel non-finished tasks");
			        }
			    	List<Runnable> list = EXECUTOR.shutdownNow();
			    	for(Runnable r : list) {
			    		LOGGER.info("Runnable r: " + r.toString());
			    	}
			        LOGGER.error("shutdown finished");
			    }
		    }
	    }
	}
}
