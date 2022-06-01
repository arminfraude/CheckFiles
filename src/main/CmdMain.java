package main;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import classes.CheckFilesCmdMenu;
import utils.Utils;

public class CmdMain {
	
	private static final Logger LOGGER = LogManager.getLogger(CmdMain.class);	
	private static AtomicBoolean ABORT_CONDITION = CheckFilesCmdMenu.ABORT;
	private static boolean RUN = true;
	
	public static void main(String[] args) {
		//System.out.println("System.out.println: äüß"); // TEST
		//LOGGER.info("LOGGER.info: äüß"); // TEST	
		
		while(RUN) {
			try {				
				checkFilesCmd();			
			}
			catch(Exception e) {
				LOGGER.error(Utils.getExceptionInfos(e));
				LOGGER.info("Beende Programm nach Exception ...");
				RUN = false;
			}
			finally {
				Utils.deleteLogFiles();
			}
		}
	}
	
	private static void checkFilesCmd() {
		
		boolean noInputOrInvalid = true;
		String userSelection = "";
		while(noInputOrInvalid) {					
			userSelection = CheckFilesCmdMenu.startMenu();
			checkAbortProgramExecutionFromTopLevel(userSelection);
			if(!CheckFilesCmdMenu.validateSelectionInput(userSelection)) {
				LOGGER.info("Ungültige Eingabe: " 
						+ userSelection
						+ Utils.LINE_SEPARATOR
						+ "Bitte Wiederholen ...");
			}
			else {
				noInputOrInvalid = false;
			}
        }
		
		int selection = Integer.parseInt(userSelection);
		switch(selection) {
			case 1: 
				CheckFilesCmdMenu.initiateFileCheck();
				break;
			case 2:
				CheckFilesCmdMenu.initiateFolderComparison();
				break;
			case 3:
				CheckFilesCmdMenu.printFolderStructure();
				break;
		}
	}
	
	private static void checkAbortProgramExecutionFromTopLevel(String userSelection) {
		if(ABORT_CONDITION.get()) {
			LOGGER.info("Programmabbruch durch User ...");
			Utils.deleteLogFiles();
			System.exit(0);
		}
	}
	
	/*
	 * Fertigstellung der grundlegenden Klassen hierzu abgebrochen wegen letztendlich Zweifel an Realisierbarkeit
	 * 
	 * @see ReadWriteCmdMenu, ReaderThread, WriterThread
	 */
	@Deprecated
	private static void readerWriterThreadedSolution() {		
		
	}
	
	/*
	 * Fertigstellung hier abgebrochen, da Lösung hiermit letztendlich nicht erreicht werden kann
	 * 
	 * @see AbortkeyListenerJna, AbortKeyListenerJava8Pool, AbortKeyListenerJava8
	 */
	@Deprecated
	private static void execThreadedSolution() {
		
		/*
		try {			
			// Class Console GEHT NICHT IN IDE ABER WäRE SYNCHRONIZED FüR THREADS
			// Class Scanner GEHT IN IDE, ABER IST NICHT SYNCHRONIZED !
	        if (CONSOLE == null) {
	            LOGGER.info("No console: not in interactive mode!");
	            System.exit(0);
	        }
	       			
			
			AbortKeyListenerJna r = AbortKeyListenerJna.getInstance();
			AbortKeyListenerJna.assignThreadToBeInterrupted(Thread.currentThread());
			Thread daemon = new Thread(r);
			daemon.setDaemon(true);
			daemon.start();
			
			while(!Thread.currentThread().isInterrupted()) {				
				
				Utils.printCurrentThreadStatus(Thread.currentThread()); // TEST 
				Utils.printCurrentThreadStatus(daemon); // TEST
			
				String userSelection = CheckFilesCmdMenu.startMenu();
				if(!CheckFilesCmdMenu.validateSelectionInput(userSelection)) {
					LOGGER.info("Ungültige Eingabe: " + userSelection);
					LOGGER.info("Bitte Wiederholen ...");
					continue;
		        }
				int selection = Integer.parseInt(userSelection);
				switch(selection) {
					case 1: 
						CheckFilesCmdMenu.initiateFileCheck();
						break;
				}		        
			}					
			LOGGER.info("Received Interrupt ...");
		}        
	    catch (Exception e) {
			// TODO: HIER WAS FANGEN, WAS DANN TROTZDEM SCHLEIFE WEITER DANN ??
	    	// BISHER IDEE: IN MAIN PROGRAMM DANN AUSGABE DES FEHLERS UND ABBRUCH ...
		}	
			
	}
	*/	
	}
}
