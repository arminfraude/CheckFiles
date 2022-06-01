package test;

import java.io.*;
import java.net.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utils.Utils;

public class ThreadClass implements Runnable
{
    DataInputStream in;
    private boolean runLoop = true;
    private static final Logger LOGGER = LogManager.getLogger(ThreadClass.class);

	public void run() {
	    BufferedReader into = new BufferedReader(new InputStreamReader(System.in));
	    String read;
	    while(runLoop && !Thread.currentThread().isInterrupted()) {
	        try {
	        	LOGGER.info("ThreadClass running ... ");
	        	
	        	// DAS IST WOHL DER GRUND WARUM ERST NACH WEITERER USER EINGABE MIT <ENTER> BEENDET WIRD
	        	// OBWOHL MAIN BEREITS FERTIG -> DAHER VERSUCH ...
	           // read = into.readLine();
	        	// GEDANKE: FALLS NACH EINTRITT IN while ERST interrupt flag GESETZT, SOLL AUSFÜHRUNG SOFORT
	        	// ... BEENDET WEREN UND DIES SIMULIERT DAS WUASI WENN AUCH UNSCHÖN BZW NICHT 100%-ig SICHER OB SO OK
	        	if(!Thread.currentThread().isInterrupted()) {
	        		LOGGER.info("Type ThreadClass: ");
	        		read = into.readLine();
		            LOGGER.info(read);
		            if(read.equals("bye")) {
		            	runLoop = false;
		            }
		            //Thread.sleep(500);
	        	}
	        }
	        catch(IOException e) {
	        	LOGGER.error(Utils.getExceptionInfos(e));
	        }
//	        catch(InterruptedException ie) {
//	        	LOGGER.error(Utils.getExceptionInfos(ie));
//	        }
	    }
	    String cause = runLoop == false ? "due to runLoop set to false" : "due to Interrupt";
	    LOGGER.info("ThreadClass exit: " + cause);
	    LOGGER.info("Thread.currentThread().isInterrupted(): " + Thread.currentThread().isInterrupted());
	}
	
	public static void main(String[]args) {
	    ThreadClass main = new ThreadClass();
	    Thread t1 = new Thread(main);
	    t1.start();
	}

}
