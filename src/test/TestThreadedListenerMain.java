package test;

import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import listeners.AbortKeyListenerJava8Pool;
import utils.ThreadAnalyzer;
import utils.Utils;

public class TestThreadedListenerMain {
	
	private static final Logger LOGGER = LogManager.getLogger(TestThreadedListenerMain.class);
	private static final Scanner SCANNER = new Scanner(System.in);

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		Scanner scan = new Scanner(System.in);
		
		//AbortKeyListenerJava8Service listener = new AbortKeyListenerJava8Service();
		AbortKeyListenerJava8Pool listener = new AbortKeyListenerJava8Pool();
		
		LOGGER.info("TASK: " + listener.getTask().toString());
		ScheduledFuture<?> sf = listener.listen(5000, 1000);
		
		ThreadClass threadClass = new ThreadClass();
		Thread t1 = new Thread(threadClass);
		t1.setName("ThreadClass");
		t1.start();
		    
		ThreadAnalyzer.actualizeCurrentThreadMap();
		ThreadAnalyzer.printSomeGenerals();
		
		try {			
			for(;;) {				
				//LOGGER.info("ScheduledFuture isDone: " + sf.isDone());
				//LOGGER.info("ScheduledFuture isCancelled: " + sf.isCancelled());
						
				ThreadAnalyzer.actualizeCurrentThreadMap();
				ThreadAnalyzer.printThreadInfos();
				
				LOGGER.info("Type: ");
				String line = scan.nextLine();
		        LOGGER.info("You typed: " + line);
		        if(line.equals("exit")) {
		        	LOGGER.info("Break von Main nach: " + line); 	
		        	break;
		        }
			}
			
			/*
        	LOGGER.info("Beende Executor");
        	listener.shutdownSoft();        	
        	*/
        	
			// HÄNGT DANN AB HIER, ALSO KEINE LSG ....
			//LOGGER.info("Call get() on ScheduledFuture ....");
			//sf.get();
			
			// Soll so künftige Iterationen verhindern, scheint aber dass nicht ...
			LOGGER.info("Cancel Future");			
        	sf.cancel(true);
        	LOGGER.info("Versuche Remove der Tasks aus Queue ... ");
			for (Runnable task : listener.getExecutorService().getQueue()) {
				listener.getExecutorService().remove(task);
		    }
			
			int mSecWait = 2000;
			LOGGER.info("Warte " + mSecWait + " mSec...");
			Thread.sleep(2000);
			
			if(listener.getExecutorService().isTerminated()) {
				LOGGER.info("Executor terminated !");
			}
			else {
				LOGGER.warn("Executor NICHT terminated !");				
				LOGGER.info("Versuche Clear Queue ....");
				listener.getExecutorService().getQueue().clear();
			}		
			
			LOGGER.info("Call shutdownSoft on listener ....");
			listener.shutdownSoft();
			
			int waitmSecToJoin = 10000;
			Thread.sleep(waitmSecToJoin);
			boolean b = listener.getExecutorService().awaitTermination(waitmSecToJoin, TimeUnit.MILLISECONDS);
        	if(b) {
        		LOGGER.warn("Executor soft shutdown und NACH " + waitmSecToJoin + " mSec awaitTermination nicht terminated !");
        		LOGGER.info("Therefore try hard Shutdown ...");
        		listener.shutdownHard();        		
        	}
        	else {
        		LOGGER.info("Executor terminated");
        	}			
        	LOGGER.info("Executor shutdown: " + listener.getExecutorService().isShutdown());
        	LOGGER.info("Executor terminated: " + listener.getExecutorService().isTerminated());
        	
        	int waitmSecAfterInterrupt = 30000;
        	LOGGER.info("Send interrupt to ThreadClass and wait " + waitmSecAfterInterrupt + " mSec ...");
        	t1.interrupt();         	
        	Thread.sleep(waitmSecAfterInterrupt);
        	
        	// Führt anscheinend zu Endlosschleife ...
        	// Da also keine Möglichkeit den Thread zu beenden, Versuche close des scanners auf gut Glück ...
        	/*while(t1.isAlive()) {
        		LOGGER.info("ThreadClass still alive, therefore wait (another) " + waitmSecToJoin + " msec !");
        		LOGGER.info("ThreadClass interrupted flag: " + t1.isInterrupted());
        		t1.join(waitmSecToJoin);
        	}  
        	*/        	
        	
        	// DA PROGRAMM ENDE UND ES SICH ZUSÄTZLICH UM System.in HANDELT
        	// ... VERZICHTE AUF PROBLEMBEHAFTETES close()
			//LOGGER.info("Scanner close");
        	//scan.close();
			
		}
		catch(Exception e) {
			LOGGER.error(Utils.getExceptionInfos(e));
			
			/* ACHTUNG:
			 * Es kann sein, dass in diesem Fall nicht alle Ressourcen closed sind
			 * Da aber weder hier noch in finally diese sichtbar müssten diese oberhalb von try
			 * definiert werden, worauf hier jedoch verzichtet wird;
			 */
		}
		finally {

			ThreadAnalyzer.actualizeCurrentThreadMap();
			ThreadAnalyzer.printSomeGenerals();
			ThreadAnalyzer.printThreadInfos();
		}
	}		
}
