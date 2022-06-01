package classes;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

import classes.FileStructure.FileStructureBuilder;
import comparators.DataPathComparator;
import comparators.DataPathReverseComparator;
import comparators.DataFilenameAndSizeComparator;
import comparators.DataFilenameComparator;
import comparators.DataModifyDateComparator;
import comparators.DataModifyDateReverseComparator;
import jna.CLibrary;
import listeners.AbortKeyListenerJna;
import main.CmdMain;
import utils.DirectoryStructureFormatter;
import utils.Utils;


public class CheckFilesCmdMenu {	
	
	private static final Logger LOGGER = LogManager.getLogger(CheckFilesCmdMenu.class);	
	private static final Scanner SCANNER = new Scanner(System.in);
	private static CLibrary CLIB = CLibrary.INSTANCE;	
	private static final Console CONSOLE = System.console();
	private static String LOST_CHAR = "";	
	public static AtomicBoolean ABORT = new AtomicBoolean();
	public static AtomicBoolean BACK = new AtomicBoolean();
	
	private static void resetLostChar() {
		LOST_CHAR = "";
	}
	
	private static int readSetAndFlushFirstKeyStrokeToConsole() {
		int read = CLIB._getwch();
		LOST_CHAR = String.valueOf((char)read);
		CONSOLE.printf(LOST_CHAR);
		CONSOLE.flush();
		
		return read;
	}
	
	private static boolean abortExecution() {
		
//		try {
//			assert LOST_CHAR.isEmpty() : "LOST_CHAR not empty entering abortExecution!";
//		}
//		catch(java.lang.AssertionError e) {
//			LOGGER.error("Clear LOST_CHAR ... "); 
//			LOST_CHAR = "";
//		}		
		if(!LOST_CHAR.isEmpty()) {
			LOGGER.error("LOST_CHAR not empty entering abortExecution!");
			LOGGER.error("Clear LOST_CHAR ... "); 
			LOST_CHAR = "";
		}
		
		int read = readSetAndFlushFirstKeyStrokeToConsole();
		if(read == Utils.ESCAPE_CODE) {			
			ABORT.set(true);			
			resetLostChar();
			return true;
		}
		else {
			ABORT.set(false);
			return false;
		}		
	}
	
	private static boolean goBackInMenu() {
		
//		try {
//			assert LOST_CHAR.isEmpty() : "LOST_CHAR not empty entering goBackInMenu!";
//		}
//		catch(java.lang.AssertionError e) {
//			LOGGER.error("Clear LOST_CHAR ... "); 
//			LOST_CHAR = "";
//		}		
		if(!LOST_CHAR.isEmpty()) {
			LOGGER.error("LOST_CHAR not empty entering goBackInMenu!");
			LOGGER.error("Clear LOST_CHAR ... "); 
			LOST_CHAR = "";
		}
		
		int read = readSetAndFlushFirstKeyStrokeToConsole();
		if(read == Utils.ESCAPE_CODE) {
			BACK.set(true);
			resetLostChar();
			return true;
		}			
		else {
			BACK.set(false);
			return false;
		}
	}	
	
	public static String startMenu() {
        String selection = "";

        LOGGER.info("Willkommen beim FileCheck");
        LOGGER.info("Beenden des Programms mit <ESCAPE>");
        LOGGER.info("--------------------------------------------------" + Utils.LINE_SEPARATOR);
        LOGGER.info("Optionen: ");
        LOGGER.info("1 - Initiiere FileCheck");
        LOGGER.info("2 - Vergleich der Dateien 2er Ordner");
        LOGGER.info("3 - Ausgabe der Ordnerstruktur zu Startpfad");
           
        if(!abortExecution()) {
        	selection = LOST_CHAR + SCANNER.nextLine();  
        	resetLostChar();
            return selection;   
        }       
        return null;
    }
	
	public static boolean validateSelectionInput(String input) {		
		try {
			int parsed = Integer.parseInt(input);
			if(parsed == 1 || parsed == 2 || parsed == 3) {
				return true;
			}
			return false;
		}
		catch(NumberFormatException e){  
		    return false;  
		}  
	}
	
	public static void initiateFileCheck() {		
		String start_path = "";
		for (boolean valid = false; !valid;) {
			LOGGER.info("Bitte zu untersuchenden Startpfad eingeben oder <ESCAPE> um ins Hauptmenü zurückzukehren:" 
					+ Utils.LINE_SEPARATOR);

			if(!goBackInMenu()) {
				start_path = LOST_CHAR + SCANNER.nextLine();  
		        resetLostChar();
		        valid = FileStructureBuilder.validatePath(start_path);
				if(!valid) {
					LOGGER.info("Rückkehr zum Hauptmenü ... ");
					CmdMain.main(null);
					return; 
				}  
		    }
			else {
				CmdMain.main(null); 
				return;
			}
		}		 
		
		FileStructure result = null;
		try {
			result = readFileStructure(start_path);
		}
		catch(RuntimeException e) {
			LOGGER.error("Rückkehr ins Hauptmenü ... " + Utils.LINE_SEPARATOR);
			CmdMain.main(null); 
			return;
		}
		LOGGER.info("\n--------------------------------------------------" + Utils.LINE_SEPARATOR);
		LOGGER.info("\nErgebnis der Berechnung: " + result.toString());	
		LOGGER.info("\n--------------------------------------------------" + Utils.LINE_SEPARATOR);
		LOGGER.info(result.getFileTypeCounts());
		LOGGER.info("\n--------------------------------------------------" + Utils.LINE_SEPARATOR);
		exportToCsvCheck(result);
	}
	
	private static FileStructure readFileStructure(String start_path) {		
		return new FileStructureBuilder(start_path)
				.dummy(new Dummy("ini", "ini"))
				.build();			
	}
	
	private static void exportToCsvCheck(FileStructure result) {		
		boolean export = false;
		for (boolean valid = false; !valid;) {
			String selection = "";
			LOGGER.info("E: Export des Ergebnisses im CSV-Format ");
			LOGGER.info("<ESCAPE>: Zurück ins Hauptmenü" + Utils.LINE_SEPARATOR);
			
			if(!goBackInMenu()) {
				 selection = LOST_CHAR + SCANNER.nextLine();  
				 resetLostChar();
				 
				 if(selection.equals("E")) {
					 export = true;		
					 valid = true;
				 }
				 else {
					 LOGGER.info("Bitte Eingabe wiederholen... J/N ?");
				 }
			}	
			else {
				CmdMain.main(null); 
				return; 
				// SIEHE OBEN initiateFileCheck: SO VOM ABLAUF OK BZW LOGISCH?!
				// ALSO HIER DANN WüRDE ZU CALLER initiateFileCheck
				// ... IN ALLERLETZTE ZEILE ZURüCKKEHREN UND DANN EH AUFHöREN ODER WIE?
				
			}
			
		}
		if(export) {
			determineSortingForCsv(result);
		}
	}
	
	private static void determineSortingForCsv(FileStructure result) {
		Comparator<Data> comparator = null;
		int parsed;
		for (boolean valid = false; !valid;) {
			String selection = "";
			LOGGER.info("Bitte Sortierung der CSV wählen:" 
					+ "\n1: Ohne Sortierung"
					+ "\n2: Modified Date aufsteigend" 
					+ "\n3: Modified Date absteigend" 
					+ "\n4: Dateiname alphabetisch aufsteigend"
					+ "\n5: Dateiname alphabetisch absteigend ?" 
					+ "\n<ESCAPE>: Zurück ins Hauptmenü" + Utils.LINE_SEPARATOR);
			
			if(!goBackInMenu()) {
				 selection = LOST_CHAR + SCANNER.nextLine();  
				 resetLostChar();
				 
				 try {
					 parsed = Integer.parseInt(selection);
				 }
				 catch(NumberFormatException nfe) {
					 parsed = 0;
				 }
					
				 switch(parsed) {
				 	case 0:
				 		LOGGER.error("Eingabe ungültig: \"" + selection +  "\" -- bitte wiederholen");
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
				 		comparator = new DataPathComparator();
				 		valid = true;
				 		break;
				 	case 5:
				 		comparator = new DataPathReverseComparator();
				 		valid = true;		
				 		break;
				 }	
			}		
			else {
				CmdMain.main(null); 
				return; 
				// SIEHE OBEN initiateFileCheck: SO VOM ABLAUF OK BZW LOGISCH?!
			}
		}
		setDestinationPathForCSVExport(result, comparator);
	}
	
	private static void setDestinationPathForCSVExport(FileStructure result, Comparator<Data> comparator) {				
		String selectionPath = null;		
		for (boolean valid = false; !valid;) {
			LOGGER.info("Bitte Pfad für den CSV-Export angeben:" + Utils.LINE_SEPARATOR
					+ "<ESCAPE>: Zurück ins Hauptmenü" + Utils.LINE_SEPARATOR);
	
			if(!goBackInMenu()) {
				selectionPath = LOST_CHAR + SCANNER.nextLine();  
		        resetLostChar();
		        
		        if(selectionPath != null
						&& new File(selectionPath).exists() 
						&& new File(selectionPath).isDirectory()
						&& new File(selectionPath).canWrite()) {
					
					valid = true;		
				}
				else {
					LOGGER.info("Ungültige Eingabe - bitte Wiederholen ...");
				}
		    }
			else {
				CmdMain.main(null); 
				return; 
				// SIEHE OBEN initiateFileCheck: SO VOM ABLAUF OK BZW LOGISCH?!
			}			
		}
		setDestinationNameForCSVExport(result, selectionPath, comparator);
	}
	
	private static void setDestinationNameForCSVExport(FileStructure result, String selectionPath, Comparator<Data> comparator) {		
		String selectionName = null;	
		String checked = null;
		for (boolean valid = false; !valid;) {
			LOGGER.info("Bitte Dateinamen für den CSV-Export angeben: " + Utils.LINE_SEPARATOR
					+ "<ESCAPE>: Zurück ins Hauptmenü" + Utils.LINE_SEPARATOR);
			
			if(!goBackInMenu()) {				
	        	selectionName = LOST_CHAR + SCANNER.nextLine();  
	        	resetLostChar();
	        	
	        	checked = Utils.isValidFileName(selectionName, "csv");
	        	if(checked != null) {
	        		valid = true;
	        	}	        	
			}
			else {
				CmdMain.main(null); 
				return; 
				// SIEHE OBEN initiateFileCheck: SO VOM ABLAUF OK BZW LOGISCH?!
				// ODER DAS DER GRUND WENN FALL SO AUFTRAT 2 ENTER ZUM BEENDEN NÖTIG SIND?
			}			
		}		
		
		// DA ALLE PRÜFUNGEN BEREITS GETAN, KÖNNEN HIER SCHLICHT SETTER VERWENDET WERDEN
		result.setCsvPath(selectionPath);
		result.setCsvName(checked);
		try {
			result.writeToCSV(comparator, false);
		}
		catch(RuntimeException e) {
			LOGGER.info(Utils.getExceptionInfos(e));
			LOGGER.info("Rückkehr ins Hauptmenü ... " + Utils.LINE_SEPARATOR);
			CmdMain.main(null); 
			return; 
		}
	}	
	
	public static void initiateFolderComparison() {
		String folderOne = "";
		String folderTwo = "";
		boolean validOne = false;
		boolean validTwo = false;
		while(!validOne && !validTwo) {
			LOGGER.info("Bitte zu untersuchende Ordnerpfade eingeben oder <ESCAPE> um ins Hauptmenü zurückzukehren" 
					+ Utils.LINE_SEPARATOR);

			if(!goBackInMenu()) {
				LOGGER.info("Ordner 1:");
				folderOne = LOST_CHAR + SCANNER.nextLine();  
				resetLostChar();
				LOGGER.info("Ordner 2:");
				folderTwo = LOST_CHAR + SCANNER.nextLine();  
		        resetLostChar();
		        
		        validOne = FileStructureBuilder.validatePath(folderOne);
		        validTwo = FileStructureBuilder.validatePath(folderTwo);
				if(!validOne || !validTwo) {
					LOGGER.info("Rückkehr ins Hauptmenü ... ");
					CmdMain.main(null);
					return; 
				}  
		    }
			else {
				
				// FRAGE OB SO SAUBER IST FüR RüCKKEHR, DA JA JETZT DER AUFRUFER DIESE 
				// ... METHODE IST UND NICHT DIE MAIN ....
				// ... UND DARF HIER JA NICHT MEHR ZURüCKKEHREN ...
				CmdMain.main(null); 
				return; // FRAGE WAS PASSIERT WENN DANN SCHLIESSLICH HIERHERKOMMT ...
					// ... dh: NACH BEENDIGUNG DER MAIN SOLL ER JA NICHT MEHR HERKOMMEN !!
				// WO return ER DENN DANN HIN?
				// IN DIESEM FALL ZUM AUFRUFER VON initiateFIleCHeck == CmdMain#checkFilesCmd ODER?
			}
		}		 
		
		FileStructure fsOne = null;
		FileStructure fsTwo = null;
		try {
			fsOne = readFileStructure(folderOne);
			fsTwo = readFileStructure(folderTwo);
		}
		catch(RuntimeException e) {
			LOGGER.error("Rückkehr ins Hauptmenü ... " + Utils.LINE_SEPARATOR);
			CmdMain.main(null); 
			return;
		}
		List<Triplet<String, Double, Double>> result = null;
		
		// Comparator hier für Gleichheitskriterium
		result = fsOne.getEqualDataObjectsWithOther(fsTwo, 
					new DataFilenameAndSizeComparator(new DataFilenameComparator()));

		
		LOGGER.info("--------------------------------------------------" + Utils.LINE_SEPARATOR);
		LOGGER.info("Ergebnis des Vergleichs:");	
		LOGGER.info("--------------------------------------------------" + Utils.LINE_SEPARATOR);
		List<List<String>> resultList = FileStructure.printAndReturnFolderComparison(result);
		LOGGER.info("--------------------------------------------------" + Utils.LINE_SEPARATOR);
		exportComparisonResultToCsvCheck(fsOne, resultList);
	}
	
	/*
	 * Art der Abprüfen hier kompakter im Gegensatz zu exportToCsvCheck
	 * bzw. setDestinationPathForCSVExport hier Prüfungen der Usereingaben komplett ausgelagert,
	 * da hier Setzen des Pfads + Namen in 1 logischen (Dialog-)schritt
	 */
	private static void exportComparisonResultToCsvCheck(FileStructure fsOne,
			List<List<String>> result) {		
		
		boolean export = false;
		for (boolean valid = false; !valid;) {
			String selection = "";
			LOGGER.info("E: Export des Ergebnisses im CSV-Format ");
			LOGGER.info("<ESCAPE>: Zurück ins Hauptmenü" + Utils.LINE_SEPARATOR);
			
			if(!goBackInMenu()) {
				 selection = LOST_CHAR + SCANNER.nextLine();  
				 resetLostChar();
				 
				 if(selection.equals("E")) {
					 export = true;		
					 valid = true;
				 }
				 else {
					 LOGGER.info("Bitte Eingabe wiederholen... J/N ?");
				 }
			}	
			else {
				CmdMain.main(null); 
				return; 
				// SIEHE OBEN initiateFileCheck: SO VOM ABLAUF OK BZW LOGISCH?!
				// ALSO HIER DANN WüRDE ZU CALLER initiateFileCheck
				// ... IN ALLERLETZTE ZEILE ZURüCKKEHREN UND DANN EH AUFHöREN ODER WIE?
				
			}
			
		}
		if(export) {
			setDestinationForComparisonExport(fsOne, result);
		}
	}
	
	private static void setDestinationForComparisonExport(FileStructure fsOne, 
			List<List<String>> result) {
		
		try {
			String selectionPath = null;	
			String selectionName = null;	
			for (boolean breakLoop = false; !breakLoop;) {
				LOGGER.info("Bitte Pfad für den CSV-Export angeben " + Utils.LINE_SEPARATOR
						+ "<ESCAPE>: Zurück ins Hauptmenü" + Utils.LINE_SEPARATOR);			
				if(!goBackInMenu()) {				
					selectionPath = LOST_CHAR + SCANNER.nextLine();  
		        	resetLostChar();	        	
				}
				else {
					CmdMain.main(null); 
					return; 
					// SIEHE OBEN initiateFileCheck: SO VOM ABLAUF OK BZW LOGISCH?!
					// ODER DAS DER GRUND WENN FALL SO AUFTRAT 2 ENTER ZUM BEENDEN NÖTIG SIND?
				}	
				
				LOGGER.info("Bitte Dateinamen für den CSV-Export angeben " + Utils.LINE_SEPARATOR
						+ "<ESCAPE>: Zurück ins Hauptmenü" + Utils.LINE_SEPARATOR);
				if(!goBackInMenu()) {				
					selectionName = LOST_CHAR + SCANNER.nextLine();  
		        	resetLostChar();
		        	
		        	if(fsOne.exportEqualDataObjectsWithOtherList(result, selectionPath, selectionName)) {
		        		breakLoop = true;
		        	}			        	
				}
				else {
					CmdMain.main(null); 
					return; 
				}			
			}	
		}
		catch(RuntimeException e) {
			LOGGER.info(Utils.getExceptionInfos(e));
			LOGGER.info("Rückkehr ins Hauptmenü ... " + Utils.LINE_SEPARATOR);
			CmdMain.main(null); 
			return; 
		}
	}
	
	public static void printFolderStructure() {		
		String start_path = "";
		for (boolean valid = false; !valid;) {
			LOGGER.info("Bitte Startpfad für Visualisierung der Struktur eingeben oder <ESCAPE> um ins Hauptmenü zurückzukehren:" 
					+ Utils.LINE_SEPARATOR);

			if(!goBackInMenu()) {
				start_path = LOST_CHAR + SCANNER.nextLine();  
		        resetLostChar();
		        valid = FileStructureBuilder.validatePath(start_path);
				if(!valid) {
					LOGGER.info("Rückkehr zum Hauptmenü ... ");
					CmdMain.main(null);
					return; 
				}  
		    }
			else {
				CmdMain.main(null); 
				return;
			}
		}		 
		
		DirectoryStructureFormatter traversal = null;
		try {
			LOGGER.info("Ergebnis der Berechnung:");			
			traversal = new DirectoryStructureFormatter(Paths.get(start_path));
			// HIERMIT WERDEN static KLASSENVARIABLEN VON TraversalKlasse GESETZT UND STRUKTUR AUFGEBAUT
			Files.walkFileTree(Paths.get(start_path), EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, traversal);
			FileStructure.printTreeAdvanced(traversal, 1, null);
		}
		catch(RuntimeException | IOException e) {
			LOGGER.error("Rückkehr ins Hauptmenü ... " + Utils.LINE_SEPARATOR);
			CmdMain.main(null); 
			return;
		}
		finally {
			traversal.reset();
			LOGGER.error("Objekt für Visualisierung der Dateistruktur reset");
		}
	}
}