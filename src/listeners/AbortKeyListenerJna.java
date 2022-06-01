package listeners;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.*;

import classes.CheckFilesCmdMenu;
import utils.Utils;

/*
 * "Es ist generell problematisch, Hintergrundprozessen zu erlauben, auf die Standardein- oder -ausgabe zuzugreifen, 
 *	die ja vorwiegend vom Vordergrund-Thread verwendet wird. Ein- und Ausgaben könnten durcheinander geraten 
 *	und es könnte zu Synchronisationsproblemen kommen, die die Ausgabe verfälschen. "
 *
 * Aktuelle Probleme bei Verwendung in Main:
 * Auf der anderen Seite Abbruch nach <ESC> meist (noch) nicht unmittelbar nach Keystroke (möglich)
 * da readLine() aus Main dann erstmal noch weiterblockiert bis weitere User-Eingabe mit ENTER folgt
 * Jedoch auch nur wenn auf oberster Ebene des Menüs ist, wenn bereits beim Startpfad eingeben oder noch weiter,
 * dann bricht Main auch nicht mehr nach weiterer User-Eingabe mit Enter ab sondern verbleibt ewig in Schleife !
 */


@Deprecated
public class AbortKeyListenerJna implements Runnable {	
	
	private static final Logger LOGGER = LogManager.getLogger(AbortKeyListenerJna.class);
	private static AbortKeyListenerJna INSTANCE;	
	private static Terminal TERMINAL;
	private static NonBlockingReader NON_BLOCKING_READER; 
	private static PrintWriter WRITER; 
	
	// WENN USER ESC DRÜCKT == true UND ENDE DER MAIN
	//public static volatile Boolean RUN_ABORT_KEY_LISTENER = null;
	// A: NICHT PRAKTIKABEL, VERSUCH MIT THREAD other TO BE INTERRUPTED (ALSO MAIN)
	private static Thread OTHER = null;
	// JETZT NUR FÜR INNERHALB KLASSE, NICHT MEHR SHARED
	private static Boolean RUN_DAEMON = null;
	
	
    private AbortKeyListenerJna() {
    	try {		
    		LOGGER.info("Create new AbortKeyListener ...");
    		
    		// FÜR: Calling Native.load() causes an UnsatisfiedLinkError
    		//System.setProperty("jna.debug_load", "true"); // HIER OK ODER IN static Block DER CLASS, da sonst zu spät geladen ???
    		
			TERMINAL = TerminalBuilder.builder()
	        		.streams(System.in, System.out) 
		            .system(true)
		            .dumb(false)
		            .encoding(Charset.forName("UTF-8"))
		            .name("Terminal")
		            .jna(true)
		            .jansi(false)
		            .build();
			NON_BLOCKING_READER = TERMINAL.reader();	
			WRITER = TERMINAL.writer();
		}
		catch(Exception e) {
			LOGGER.error("Unable to build AbortKeyListener");
			LOGGER.error(Utils.getExceptionInfos(e));
			LOGGER.error("------------------------" + Utils.FILE_SEPARATOR);
			LOGGER.error("PLEASE USE CTRL+C FOR EXITING THE RUNNING PROGRAM ..." + Utils.FILE_SEPARATOR);
		}
    }
    
    public static synchronized AbortKeyListenerJna getInstance() {
        if(INSTANCE == null){
        	INSTANCE = new AbortKeyListenerJna();
        }
        return INSTANCE;
    }
    
    public static void assignThreadToBeInterrupted(Thread other) {
    	OTHER = other;
    }

    
	@Override
	public void run() {	
		LOGGER.info("AbortKeyListener Daemon start ... ");
		RUN_DAEMON = true;
		while(RUN_DAEMON) {				
			try {
				LOGGER.info("AbortKeyListener Daemon exec ... ");
				if(NON_BLOCKING_READER.read() == 27) {
					LOGGER.info("Programm wird auf Anforderung des Benutzers beendet ....");
					if(OTHER != null) {
						LOGGER.info("Sende Interrupt an MAIN ....");
						OTHER.interrupt();
					}
					else {
						LOGGER.warn("Kein aufzuhaltender Thread spezifiziert!");
						// WENN HIER WEITERLAIUFEN WÜRDE, WÄRE PROGRAMM NICHT AUFZUHALTEN
						// SO WIRD ZWAR KEIN INTERRUPT GESENDET, JEDOCH DAEMON ZUMINDEST...
					}
					RUN_DAEMON = false;
				}
			    else {	
				  char input = (char) NON_BLOCKING_READER.read(); // TEST
				  LOGGER.info("Daemon read: " + input); // TEST
				}				
			}
			catch(Exception e) {
				LOGGER.error(Utils.getExceptionInfos(e));
						
				if(e.getClass().equals(InterruptedIOException.class)) {
					LOGGER.error("byteTransferred before Interrupt: " 
							+ ( (InterruptedIOException) e).bytesTransferred);	
				}
				
				LOGGER.error("BEENDE AbortKeyListener nach Fehler !");
				RUN_DAEMON = false;		
				
				//LOGGER.error("Neustart vom Horchen auf <ESCAPE> nach Fehler ...");
				//continue;					
			}
		}		
		
		try {
			LOGGER.info("Try closing READER / TERMINAL ... ");
			NON_BLOCKING_READER.close();
			TERMINAL.close();
		}
		catch(IOException e) { 
			LOGGER.error("IOException während shutdown TERMINAL / READER ... ");
			LOGGER.error(Utils.getExceptionInfos(e));
		}
		finally {
			//System.exit(0); // PRÜFEN OB HIER LASSEN, DENN WÜRDE JA HEISSEN, for IN MAIN KÄSE ...
		}
	
	}	
	
}
