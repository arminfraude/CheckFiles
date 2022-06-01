package ReadWriteHandling;

import java.io.Console;
import java.io.File;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import classes.Data;
import classes.Dummy;
import classes.FileStructure;
import classes.FileStructure.FileStructureBuilder;
import comparators.DataPathComparator;
import comparators.DataPathReverseComparator;
import comparators.DataModifyDateComparator;
import comparators.DataModifyDateReverseComparator;
import utils.Utils;

/*
 * Versuch auf diese Weise das Lesen von der Console zu koordinieren mit Integration von Horchen auf _getwch() abgebrochen
 * Zum einen wohl riesen Overhead und zum andere Frage ob an sich überhaupt vernünftig realisierbar wäre
 * 
 * Daher die Fertiggstellung hiervon mit Integration der Thread-Klassen mit vollständiger Anpassung an Threadkontext nicht zu Ende realisiert
 * @see ReaderThread, WriterThread
 */
@Deprecated
public class ReadWriteCmdMenu {

	private static final Logger LOGGER = LogManager.getLogger(ReadWriteCmdMenu.class);	
	Console console = System.console();
	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
	
	public void writeStartMenu() {
		console.printf("In writeStartMenu method waiting to acquire lock" + Utils.FILE_SEPARATOR);
	    rwl.writeLock().lock();
	    console.printf("In writeStartMenu method acquired write lock" + Utils.FILE_SEPARATOR);
	    console.printf("--------------------------------------------------" + Utils.FILE_SEPARATOR);
	   	    
	    console.printf("Willkommen beim FileCheck" + Utils.FILE_SEPARATOR);
	    console.printf("Verlassen des Programms mit <ESCAPE>" + Utils.FILE_SEPARATOR);
	    console.printf("--------------------------------------------------" + Utils.FILE_SEPARATOR);
	    console.printf("Optionen: " + Utils.FILE_SEPARATOR);
	    console.printf("1 - Initiiere FileCheck" + Utils.FILE_SEPARATOR);
	    console.flush();
	    
	    rwl.writeLock().unlock(); 
	    console.printf("In put method released write lock" + Utils.FILE_SEPARATOR);	    
	}
	
	public String readFromStartMenu() {
		
		console.printf("In readFromStartMenu method waiting to acquire lock" + Utils.FILE_SEPARATOR);
		rwl.readLock().lock();
		console.printf("In readFromStartMenu method acquired read lock" + Utils.FILE_SEPARATOR);	    
	    
		String selection = console.readLine();	
		rwl.readLock().unlock(); 
		console.printf("In readFromStartMenu method released read lock before exec writeResponseToStartMenuSelection in while" + Utils.FILE_SEPARATOR);
		
		console.flush();
		while(!validateSelectionInput(selection)) {				
			
			writeResponseToStartMenuSelection(selection);
			//TODO: ZUSÄTZLICH AUCH HIER NOCH INTEGRIEREN GGF DASS 1 EBENE ZURÜCK FÜR BREAK;
		}
	
		return selection;
	}
	
	private boolean validateSelectionInput(String input) {		
		try {
			int parsed = Integer.parseInt(input);
			//if(parsed == 1 || parsed == 2) {
			if(parsed == 1) {
				return true;
			}
			return false;
		}
		catch(NumberFormatException e){  
		    return false;  
		}  
	}
	
	private void writeResponseToStartMenuSelection(String selection) {
		console.printf("In writeResponseToStartMenuSelection method waiting to acquire lock" + Utils.FILE_SEPARATOR);
	    rwl.writeLock().lock();
	    console.printf("In writeResponseToStartMenuSelection method acquired write lock" + Utils.FILE_SEPARATOR);
	    
		console.printf("Ungültige Eingabe: " + selection);
		console.printf("Bitte Wiederholen ...");
		console.flush();
		
		rwl.writeLock().unlock(); 
	    console.printf("In writeResponseToStartMenuSelection method released write lock" + Utils.FILE_SEPARATOR);	   
	}
	
	public void askUserAndValidateForInitFileCheck() {
		console.printf("In askUserForInitFileCheck method waiting to acquire lock" + Utils.FILE_SEPARATOR);
	    rwl.writeLock().lock();
	    console.printf("In askUserForInitFileCheck method acquired write lock" + Utils.FILE_SEPARATOR);
		
		String start_path = null;
		boolean valid = false;
		console.printf("Bitte zu untersuchenden Startpfad eingeben:" + Utils.FILE_SEPARATOR);
		console.flush();
		while(!valid) {
			
			rwl.writeLock().unlock(); 
		    console.printf("In askUserForInitFileCheck method released write lock" + Utils.FILE_SEPARATOR);	   
		    
		    //TODO: ZUSÄTZLICH AUCH HIER NOCH INTEGRIEREN GGF DASS 1 EBENE ZURÜCK FÜR BREAK;
		    
		    valid = readUserInputToInitFileCheck();
		    
		    console.printf("In askUserForInitFileCheck method waiting to acquire lock" + Utils.FILE_SEPARATOR);
		    rwl.writeLock().lock();
		    console.printf("In askUserForInitFileCheck method acquired write lock" + Utils.FILE_SEPARATOR);
		    if(valid == false) {
		    	console.printf("Pfad existiert nicht!" + Utils.FILE_SEPARATOR);
		    	console.flush();
			}
		}		
		buildAndShowFilestructureResult(start_path);		
	}
	
	public boolean readUserInputToInitFileCheck() {
		console.printf("In readUserInputToInitFileCheck method waiting to acquire lock" + Utils.FILE_SEPARATOR);
		rwl.readLock().lock();
		console.printf("In readUserInputToInitFileCheck method acquired read lock" + Utils.FILE_SEPARATOR);
		
		String start_path = console.readLine();
		boolean valid = FileStructureBuilder.validatePath(start_path);
		
		rwl.readLock().unlock(); 
		console.printf("In readUserInputToInitFileCheck method released read lock" + Utils.FILE_SEPARATOR);
		
		return valid;		
	}
	
	public void buildAndShowFilestructureResult(String start_path) {
		// HIER SOLLTE ALSO DER WRITELOCK NOCH GESETZT SEIN VON askUserAndValidateForInitFileCheck
		// ... DIREKT VORHER ...
		
		FileStructure result = readFileStructure(start_path);
		console.printf("--------------------------------------------------" + Utils.FILE_SEPARATOR);
		console.printf("Ergebnis der Berechnung: " + result.toString());	
		console.printf("--------------------------------------------------" + Utils.FILE_SEPARATOR);
		console.printf(result.getFileTypeCounts() );
		console.printf("--------------------------------------------------" + Utils.FILE_SEPARATOR);
		console.flush();
		exportToCsvCheck(result);
	}
	
	private FileStructure readFileStructure(String start_path) {		
		return new FileStructureBuilder(start_path)
				.dummy(new Dummy("ini", "ini"))
				.build();			
	}
	
	private String exportToCsvCheck(FileStructure result) {		
		// HIER SOLLTE ALSO DER WRITELOCK IMMER NOCH GESETZT SEIN VON buildAndShowFilestructureResult
		// ... DIREKT VORHER ...
	
		console.printf("Ergebnis im CSV-Format exportieren? J/N" + Utils.FILE_SEPARATOR);
		console.flush();
		rwl.writeLock().unlock(); 
	    console.printf("In exportToCsvCheck method released write lock" + Utils.FILE_SEPARATOR);	   
	   
	    String export = null;
		boolean valid = false;
	    while(!valid) {
			export = evaluateUserInputToCsvCheck();		
			
			console.printf("In exportToCsvCheck method waiting to acquire lock" + Utils.FILE_SEPARATOR);
			rwl.readLock().lock();
			console.printf("In exportToCsvCheck method acquired read lock" + Utils.FILE_SEPARATOR);
			
			if(export == null) {
				LOGGER.info("Bitte Eingabe wiederholen... J/N ?");
			}
			else if(export.equals("J")) {
				valid = true;
			}
			else if(export.equals("N")) {
				valid = true;
			}
		}
	    return export;
	}
	
	private String evaluateUserInputToCsvCheck() {
		console.printf("In evaluateUserInputToCsvCheck method waiting to acquire lock" + Utils.FILE_SEPARATOR);
		rwl.readLock().lock();
		console.printf("In evaluateUserInputToCsvCheck method acquired read lock" + Utils.FILE_SEPARATOR);
		
		String selection = console.readLine();	
		
		rwl.readLock().unlock(); 
		console.printf("In evaluateUserInputToCsvCheck method released read lock" + Utils.FILE_SEPARATOR);
		
		if(selection.equals("J")) {
			return "J";
		}
		else if(selection.equals("N")) {
			return "N";
		}
		else {
			return null;
		}	
	}
	
	/*
	private void determineSortingForCsv(FileStructure result) {
		Comparator<Data> comparator = null;
		int parsed;
		for (boolean valid = false; !valid;) {
			LOGGER.info("Bitte Sortierung der CSV wählen:" 
					+ "\n1: Ohne Sortierung"
					+ "\n2: Modified Date aufsteigend" 
					+ "\n3: Modified Date absteigend" 
					+ "\n4: Dateiname alphabetisch aufsteigend"
					+ "\n5: Dateiname alphabetisch absteigend ?" + Utils.FILE_SEPARATOR);
			String selection = SCANNER.nextLine();		
			
			try {
				parsed = Integer.parseInt(selection);
			}
			catch(NumberFormatException nfe) {
				parsed = 0;
			}
			
			//LOGGER.info("parsed: " + parsed);
				// WIESO ERKENNT KORREKTE EINGABE NICHT??
				// A: WAR WEGEN DEFAULT !!!!!
				// AUS IEINEM GRUND AUCH WENN 1-5 EINGEGEBEN GING IMMER IN default, WAS DANN FEHLER WAR.. ABER WARUM ?!!!
				// A: WAR VLL WEIL break, WAS GEFEHLT ABER GEFORDERT WIRD, SOLLTE NATüRLICH AM ENDE VON JEDEM CASE, ABER JETZT EH GUT ...
			switch(parsed) {
				case 0:
					LOGGER.info("Eingabe ungültig: \"" + selection +  "\" -- bitte wiederholen");
					break;
				case 1:
					valid = true;
					break;
				case 2:
					comparator = new DataModifyDateComparator();
					valid = true;
					break;					
				case 3:
					comparator = new DataModifyDateReverseComparator();
					valid = true;
					break;
				case 4:
					comparator = new DataFilenameComparator();
					valid = true;
					break;
				case 5:
					comparator = new DataFilenameReverseComparator();
					valid = true;		
					break;
			}		
		}
		setDestinationPathForCSVExport(result, comparator);
	}
	
	private void setDestinationPathForCSVExport(FileStructure result, Comparator<Data> comparator) {		
		
		String selectionPath = null;		
		for (boolean valid = false; !valid;) {
			LOGGER.info("Bitte Pfad für den CSV-Export angeben: ");
			selectionPath = SCANNER.nextLine();			
			if(selectionPath != null
					&& new File(selectionPath).exists() 
					&& new File(selectionPath).isDirectory()) {
				
				valid = true;		
			}
			else {
				LOGGER.info("Ungültige Eingabe - bitte Wiederholen ...");
			}
		}
		setDestinationNameForCSVExport(result, selectionPath, comparator);
	}
	
	private void setDestinationNameForCSVExport(FileStructure result, String selectionPath, Comparator<Data> comparator) {
		
		String selectionName = null;	
		for (boolean valid = false; !valid;) {
			LOGGER.info("Bitte Dateinamen für den CSV-Export angeben: ");
			selectionName = SCANNER.nextLine();
			if(selectionName != null) {
				valid = true;
			}
			else {
				LOGGER.info("Ungültige Eingabe - bitte Wiederholen ...");
			}
		}		
		
		// Wenn hier also keine gültigen Angaben gemacht werden, wird dann
		// ... in writeToCSV DEFAULT genommen
		result.setCustomCsvPathAndName(selectionPath, selectionName);
		result.writeToCSV(comparator, false);
	}
	*/
}
