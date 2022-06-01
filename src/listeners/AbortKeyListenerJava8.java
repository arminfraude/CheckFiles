package listeners;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/*
 * Grundlegendes Problem ist der gemeinsame Zugriff lesend wie schreibend von System.in
 * Daher nicht wirklich einsetzbar
 * 
 * @see AbortkeyListenerJna, AbortKeyListenerJava8Pool, AbortKeyListenerJava8Service
 *  
 */
@Deprecated
public interface AbortKeyListenerJava8 {
	
	public ScheduledFuture<?> listen(int initialDelayMillis, int delayMillis);
	
	// PROBLEMATISCH: WENN RETURN TYPE IN IMPLEMENTIERENDER KLASSE EINE ERBENDE KLASSE, IST VALIDE
	// ... ABER NAT�RLICH DIE NUR VOM SUBTYP UNTERST�TZTEN METHODEN DANN NICHT ANWENDBAR !
	// KONKRET HIER: getQueue() F�R ScheduledThreadPoolExecutor, ABER NICHT F�R ScheduledExecutorService !
	public ScheduledExecutorService getExecutorService();
	
	public void shutdownSoft();
	public void shutdownHard();
	public Runnable getTask();
	public List<Future<Object>> invokeAll() throws InterruptedException;
}
