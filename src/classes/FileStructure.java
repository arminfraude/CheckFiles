package classes;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.Wini;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import comparators.DataFilenameAndSizeComparator;
import comparators.DataFilenameComparator;
import comparators.DataPathComparator;
import comparators.DataPathReverseComparator;
import test.MyFileVisitor;
import utils.DirectoryStructureFormatter;
import utils.DirectoryTraversal;
import utils.Node;
import utils.Utils;

public class FileStructure implements Cloneable {

	private static final Logger LOGGER = LogManager.getLogger(FileStructure.class);
	private static String CSV_PATH_DEFAULT;
	private static String CSV_NAME_DEFAULT;
	private static String START_PATH_DEFAULT;
	private static String CSV_COLUMNS_FILES_LIST;
	private static String CSV_COLUMNS_FILES_EQUALS;
	
	private String csvPath;
	private String csvName;
	private String start_path; // FRAGE OB ÜBERHAUPT SO SINNVOLL NUN
	private Data startPathData; // STARTPFAD WIRD ABER NICHT ALS EIGENE FILE MITGEZÄHLT 
	private List<Data> dataList; 	
	private int fileTotal;
	private int fileCount;
	private int dirCount;
	private int readable;
	private int nonReadable;
	private Dummy dummy;	
	private int possiblyMoreFiles;
	
	static {
		try {
			Ini ini = Utils.getIni();	
			
			CSV_PATH_DEFAULT = ini.get("csv_path_default", "csv_path", String.class);	   
			if(CSV_PATH_DEFAULT == null) {
				LOGGER.error("Kein CSV_PATH_DEFAULT IN ini-File definiert!");	
			}
			else if(!CSV_PATH_DEFAULT.endsWith("/") && !CSV_PATH_DEFAULT.endsWith("\\")) {
				CSV_PATH_DEFAULT += Utils.FILE_SEPARATOR;
			}			
			LOGGER.debug("csv_path_default: " + CSV_PATH_DEFAULT);	
			
			CSV_NAME_DEFAULT = ini.get("csv_name_default", "csv_name", String.class);	        
			if(CSV_NAME_DEFAULT == null) {
				LOGGER.error("Kein CSV_NAME_DEFAULT IN ini-File definiert!");	
			}
			LOGGER.debug("csv_name_default: " + CSV_NAME_DEFAULT);			
			
			START_PATH_DEFAULT = ini.get("start_path_default", "start_path", String.class);
			if(START_PATH_DEFAULT == null) {
				LOGGER.error("Kein START_PATH_DEFAULT IN ini-File definiert!");	
			}
			else if(!START_PATH_DEFAULT.endsWith("/") && !START_PATH_DEFAULT.endsWith("\\")) {
				START_PATH_DEFAULT += Utils.FILE_SEPARATOR;
			}
			LOGGER.debug("start_path_default: " + START_PATH_DEFAULT);
			
			CSV_COLUMNS_FILES_LIST = ini.get("csv_columns_files_list", "columns", String.class);
			if(CSV_COLUMNS_FILES_LIST == null) {
				LOGGER.error("Keine CSV_COLUMNS_FILES_LIST IN ini-File definiert!");	
			}
			LOGGER.debug("csv_columns_files_list: " + CSV_COLUMNS_FILES_LIST);
			
			CSV_COLUMNS_FILES_EQUALS = ini.get("csv_columns_files_equals", "columns", String.class);
			if(CSV_COLUMNS_FILES_EQUALS == null) {
				LOGGER.error("Keine CSV_COLUMNS_FILES_EQUALS IN ini-File definiert!");	
			}
			LOGGER.debug("csv_columns_files_equals: " + CSV_COLUMNS_FILES_EQUALS);		
		}
		catch (Exception e) {
			LOGGER.error("Error creating FileStructure static variables: " 
					+ Utils.getExceptionInfos(e));
			
			
		}
	}
	
	public static class FileStructureBuilder {
		
		private String csvPath;
		private String csvName;
		private String start_path; // obligatorisches Start-Argument also + darauf aufbauend ggf. die anderen Methoden
		private Data startPathData; // NEU und auch obligatorisch
		private List<Data> dataList; 	
		private int fileTotal;
		private int fileCount;
		private int dirCount;
		private int readable;
		private int nonReadable;
		private Dummy dummy;		
		private int possiblyMoreFiles;
		
		// obligatorisch - als 1. Aufruf
		public FileStructureBuilder(String start_path) {
			if(!start_path.endsWith("/") && !start_path.endsWith("\\")) {
				start_path += Utils.FILE_SEPARATOR;
			}
            this.start_path = start_path;              
            
            try {
            	startPathData = new Data(start_path);
            }
            catch(IOException e) {
            	startPathData = null; // DANN IN build() DARAUF GECHECKT VGL. ANDERE PRÜFUNGEN OB ERFOLGREICH ..
            }
            dataList = new ArrayList<Data>();
            fileTotal = 0;
            fileCount = 0;
            dirCount = 0;
            readable = 0;
            nonReadable = 0;
            possiblyMoreFiles = 0;
        }
		
		/* BEIDE METHODEN MACHEN SO KEINEN SINN WÄHREND BUILD DA ALLES AUF FileStructure(start_path)
			... DELEGIERT WIRD -> AUSLAGERN IN FileStructure In SETTER...
		public FileStructureBuilder fromDataList(List<Data> dataList) {
			this.dataList = dataList.stream().map(item -> new Data(item)).collect(Collectors.toList());	
			return this; ---> ALSO KEIN EIGENER CTOR MEHR HIERZU, NUR NOCH EH BEREITS EXISTIERENDER SETTER
		}			
		
		... WOBEI DIES HIER ALS SETTER WIRKLICH UNNöTIG, DA EINFACH NEU KREIERTES OBJ ZUGEWIESEN WERDEN KANN VIA BUILDER
	    public FileStructureBuilder fromOther(FileStructure other) {
	    	return fromDataList(other.getDataList());
	    	
	    	//start_path = other.start_path; 
	    	//return this;	
	    }
		*/
		
        public FileStructureBuilder csvPath(String csvPath) {
        	if(!csvPath.endsWith("/") && !csvPath.endsWith("\\")) {
        		csvPath += Utils.FILE_SEPARATOR;
        	}
            this.csvPath = csvPath;
            return this;
        }
        
        public FileStructureBuilder csvName(String csvName) {
            this.csvName = csvName;
            return this;
        }
        
        public FileStructureBuilder dummy(Dummy dummy) {
        	this.dummy = dummy;        	
            return this;
        }
                
        //Return the final constructed User object
        public FileStructure build() {
        	// FÜR startPathData KEINE GESONDERTE VALIDIERUNG NÖTIG DA HIER MITENTHALTEN
        	if(!validatePath(start_path)) {
            	LOGGER.error("FileStructure Objekt konnte nicht gebuildet werden!");
            	LOGGER.error("Bitte start_path überprüfen");
            	return null;
            }
        	if(!validateAssignedCSVPath()) {
        		LOGGER.error("FileStructure Objekt konnte nicht gebuildet werden!");
            	LOGGER.error("Bitte csv_path überprüfen");
            	return null;
        	}       	
        	if(startPathData == null) {
        		LOGGER.error("FileStructure Objekt konnte nicht gebuildet werden!");
            	LOGGER.error("Verarbeitung des Startpfad fehlgeschlagen");
            	return null;
        	}
        	
        	FileStructure structure =  new FileStructure(this);
        	//FileStructure.setFileTypeCounts(structure); // GEHT AUCH HIER BZW WIESO HIER UND NICHT EINFACH INNERHALB VON CTOR FileStructure??            
            return structure;
        }       
	
		public static boolean validatePath(String start_path) {
	     	File f = new File(start_path);               	
	     	if (!f.exists()) {
	     		LOGGER.error("Pfad: " + start_path + " existiert nicht!");
	     		return false;
	     	}
	     	else if(!f.isDirectory()) {
	     		LOGGER.error("Pfad: " + start_path + " ist kein Verzeichnis!");
	     		return false;
	     	}
	     	//else if(!f.canRead()) { // LIEFERT KEINE ZUVERLÄSSIGEN ERGEBNISSE !
	     	else if(!Files.isReadable(f.toPath())) {
	     		LOGGER.error("Von Pfad: " + start_path + " kann nicht gelesen werden!");
	     		LOGGER.error("Ggf. fehlt Berechtigung");
	     		return false;
	     	}
	     	return true;
	     }
        
        private boolean validateAssignedCSVPath() {
        	if(csvPath != null) {
        		File f = new File(csvPath);
            	if (!f.isDirectory()) {
            		LOGGER.error("Angegebener Pfad für CSV-Export: " + csvPath + " existiert nicht!");
            		return false;
            	} 
            	else if (!Files.isReadable(f.toPath())) {
            		LOGGER.error("Angegebener Pfad für CSV-Export: " + csvPath + " kann nicht gelesen werden !");
            		return false;
            	}
            	else if (!f.canWrite()) {
            		LOGGER.error("Unter angegebener Pfad für CSV-Export: " + csvPath + " kann nicht geschrieben werden !");
            		return false;
            	}
            	else {
            		if(csvName == null) {
            			LOGGER.warn("Kein Name für CSV-Exportdatei angegeben - verwende Default: " + CSV_NAME_DEFAULT);
            		}
            		else {
            			String checked = Utils.isValidFileName(csvName, "csv");
            			if(checked == null) {
            				return false;
            			}
            		}
            		return true;
            	}        		
        	}
        	else {
        		LOGGER.warn("Momentan kein eigener CSV-Pfad angegeben - verwende Default: " + CSV_PATH_DEFAULT);
        		return true;
        	}
        }       
	}
	
	/* 
	 * Wenn keine custom CSV-Angaben in Filebuilder angegeben, wird Objekt ja trotzdem gebuildet
	 * und hier dann null übergeben -> in writeToCSV dann in diesem Fall Default-Werte verwendet !
	 */
	private FileStructure(FileStructureBuilder builder) {
		
		if(builder == null) {
			throw new RuntimeException("wrong arguments passed");
		}

		this.start_path = builder.start_path;
		LOGGER.info("set start_path: " + this.start_path);		
		this.csvPath = builder.csvPath;
		LOGGER.info("set csvPath: " + this.csvPath);		
		this.csvName = builder.csvName;
		LOGGER.info("set csvName: " + this.csvName);
        this.dataList = builder.dataList; 
		
        try {
        	this.startPathData = (Data)builder.startPathData.clone();
        }
        catch(CloneNotSupportedException e) {
        	LOGGER.error("Error creating startPathData: " + Utils.getExceptionInfos(e));
        }
        
		try {
    		this.dummy = (Dummy)builder.dummy.clone(); // OK SO??
    	}
    	catch(CloneNotSupportedException e) {
    		LOGGER.error("Error creating Dummy: " + Utils.getExceptionInfos(e));
    	}
		
		//NEU: adjustCounts=true JETZT MITGELIEFERT UM UNNÖTIGEN ERNEUTEN AUFRUF HIER EINZUSPAREN!
		generateDataListFromPathAdvanced(this.start_path, true);
	}	
	
	/*
	 * Trotz ggf. AccessDeniedException möglich Struktur (dataList) ohne Abbruch aufzubauen
	 * 
	 * @see DirectoryTraversal, FileVisitor<Path>
	 */
	private void generateDataListFromPathAdvanced(String start_path, boolean adjustCounts) {
	
		Path path_one = null;
		try {
			path_one = Paths.get(this.start_path);
			DirectoryTraversal traversal = new DirectoryTraversal(path_one);
			
			// <=> ALSO FOLLOW_LINKS option NICHT AKTIV
			Files.walkFileTree(path_one, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, traversal);
			Map<Path, Long> resultMapFileAndSize = traversal.getResultMapFileAndSize();		
		    for (Map.Entry<Path, Long> entry : resultMapFileAndSize.entrySet()) {
		    	try {
		    		Data d = new Data(entry.getKey().toString());
		    		d.setSize(entry.getValue());
					dataList.add(d);	
		    	}
		    	catch(Exception e) {
		    		LOGGER.error(Utils.getExceptionInfos(e));
		    		LOGGER.error("Fehler bei Verarbeitung der Datei " + entry.getKey());
		    		LOGGER.error("Überspringe und fahre mit Schleife fort ...\n");		    			
		    		continue;
		    	}
		    }
			
			if(adjustCounts) {
				setFileTypeCounts(this, traversal);
			}
			
			dataList.forEach( data -> {
				if(data.isDir()) {
					try {
						Long sizeThis = generateDataSizeCompleteForSubfolder(data.getPath());
						
						if(data.getSizeInByte() > 0L) {
							LOGGER.warn("Ordnergrösse gesamt bereits mit Grösse > 0 vorhanden:");
							LOGGER.warn("Überschreibe für " + data.getPath() + " alten Wert " + data.getSizeInKB() 
								+ "mit: " + sizeThis + "\n");
						}
						data.setSize(sizeThis);
					}
					catch(RuntimeException e) {
						LOGGER.error("Überspringe ...\n");
						
						// Foreach() cannot use the two keywords break and continue. 
						// It can achieve the break effect by throwing an exception, 
						//and the continue effect can be directly used by return.
						//return;
						
						// JEDOCH: INNERHALB WIE ES SCHEINT NICHT return NÖTIG, forEach MACHT SO ODER SO WEITER
					}
				}
			});			
		}
		catch(Exception e) {
			LOGGER.error("(Neu-)Generieren der Dateiliste zu " + path_one + " nicht möglich!\n");
			LOGGER.error(Utils.getExceptionInfos(e));
			throw new RuntimeException();
		}
	}

	/*
	 * Beachte: Im Moment würden Dateiobjekte, die weder Ordner noch regular File noch Symlink sind
	 * nicht vom fileCount erfasst werden
	 * 
	 * LinkOption.NOFOLLOW_LINKS aktiv sofern es geht, da Sinn nicht ist die Sourcedatei zu analysieren
	 * 
	 */
	private static void setFileTypeCounts(FileStructure fs, DirectoryTraversal traversal) {	
		
		// TEST
		List<String> notRecognizedFiles = new ArrayList<>();
		int cnt = 0;
		
		
		fs.fileTotal = fs.dataList.size();
		for(Data d: fs.getDataList()) {		
			
			// TEST
			int fileCntBefore = fs.fileCount;
			int dirCountBefore = fs.dirCount;
			
			
			if(Files.isDirectory(Paths.get(d.getPath()), LinkOption.NOFOLLOW_LINKS)) {
				fs.dirCount++;
			}
			else if(Files.isRegularFile(Paths.get(d.getPath()), LinkOption.NOFOLLOW_LINKS)) {
				fs.fileCount++;
			}
			else if(Files.isSymbolicLink(Paths.get(d.getPath()))) {
				fs.fileCount++;	
			}
			
			if(Files.isReadable(Paths.get(d.getPath()))) {
				fs.readable++;
			}
			else {
				fs.nonReadable++;
				
				// NEU: Jetzt zusätzlich um ggf. in toString() bzw CSV-Export anzeigen zu können, 
				// ... dass ggf. noch weitere Dateien vorhanden sind, die jedoch nicht erreicht werden können
				if(Files.isDirectory(Paths.get(d.getPath()), LinkOption.NOFOLLOW_LINKS)) {
					fs.possiblyMoreFiles++;
				}
			}
			
			
			
			// TEST
			int fileCntAfter = fs.fileCount;
			int dirCountAfter = fs.dirCount;			
			if( (dirCountAfter-dirCountBefore)==0
					&& (fileCntAfter-fileCntBefore)==0 ) {
				LOGGER.error("dirCount und auch fileCount NICHT hochgezählt !");
				LOGGER.error("Datei:");
				LOGGER.error(Utils.DATA_PRINT_HEADER);
				LOGGER.error(d.toString() + "\n");							
				notRecognizedFiles.add(d.toString());
				cnt++;
			}			
		}
		
		if(!(fs.fileTotal==traversal.getCntTotal())) {
			LOGGER.error("Diskrepanzen bei Bestimmung der Anzahl der insgesamt vorhandenen Dateiobjekte: "
				+ fs.fileTotal + " <> " + traversal.getCntTotal());
		}
		if(!(fs.dirCount==traversal.getCntDir())) {
			LOGGER.error("Diskrepanzen bei Bestimmung der Anzahl der Verzeichnisse "
					+ fs.dirCount + " <> " + traversal.getCntDir());
		}
		if(!(fs.fileCount==traversal.getCntFile())) {
			LOGGER.error("Diskrepanzen bei der Bestimmung der Anzahl der Dateien "
					+ fs.fileCount + " <> " + traversal.getCntFile());
		}
		if(!(fs.readable==(traversal.getCntTotal() - traversal.getCntFailed()))) {
			LOGGER.error("Diskrepanzen bei der Bestimmung der lesbaren Dateiobjekte "
					+ fs.readable + " <> " + (traversal.getCntTotal() - traversal.getCntFailed()));
		}
		if(!(fs.nonReadable==traversal.getCntFailed())) {
			LOGGER.error("Diskrepanzen bei der Bestimmung der nicht lesbaren Dateiobjekte "
					+ fs.nonReadable + " <> " + traversal.getCntFailed());
		}

		
		// Test
		//exportNotRecognizedFilesByFsCounter(Utils.DATA_PRINT_HEADER, notRecognizedFiles, cnt, fs);			
		
		// Test
		//exportTraversalFileListToCSV(fs, traversal);	
	}
	
	/*
	 * Test-Methode zur Analyse
	 */
	@SuppressWarnings("unused")
	private static void exportNotRecognizedFilesByFsCounter(String header, 
			List<String> notRecognizedFiles, 
			int counter,
			FileStructure fs) {
		
		String resultFullPath = checkAndGetCSVFullPath("C:\\Users\\Armin\\eclipse-workspace\\CheckFilesCmd", 
				"NotRecognizedFilesByFsCounter.csv");				
		try {
			FileWriter writer = new FileWriter(resultFullPath, false);
			writer.write(header);
			writer.write("\n");			
			notRecognizedFiles.forEach( e -> {
				try {
					writer.write(e);
					writer.write("\n");
				} catch (IOException ex) {
					LOGGER.error("Fehler beim Schreiben der NotRecognized-CSV" + Utils.LINE_SEPARATOR
							+ Utils.getExceptionInfos(ex));
				}
			});
			
			writer.write("Anzahl gesamt nicht von FS-Countern erfassten Dateien: " + counter);
			writer.write("\n");	
			writer.write(fs.getFileTypeCounts());
			writer.close();
		}
		catch(Exception ex) {
			LOGGER.error("Fehler beim Schreiben der NotRecognized-CSV" + Utils.LINE_SEPARATOR
					+ Utils.getExceptionInfos(ex));
		}
	}
	
	/*
	 * Test-Methode zum Abgleich
	 */
	@SuppressWarnings("unused")
	private static void exportTraversalFileListToCSV(FileStructure fs, DirectoryTraversal traversal) {
		try {
			
			// ACHTUNG: HIER WÄRE csvPath NUR DER DEFAULT, DA EIGENTLICHER EXPORT VON USER ERST NACHHER ANGEWIESEN WERDEN KANN
			String resultFullPath = checkAndGetCSVFullPath("C:\\Users\\Armin\\eclipse-workspace\\CheckFilesCmd",
					"Traversal Dateiliste.csv");			
			Map<Path, Long> traversalMap = traversal.getResultMapFileAndSize();
		
			FileWriter writer = new FileWriter(resultFullPath, false);
			writer.write("path;size");
			writer.write("\n");
			traversalMap.forEach(
					(k, v) -> {
						try {
							writer.write(k + ";" + v);
							writer.write("\n");
						} catch (IOException e) {
							LOGGER.error("Fehler beim Schreiben der Traversal Vergleichs-CSV" + Utils.LINE_SEPARATOR
									+ Utils.getExceptionInfos(e));
						}
			  			
					}
				);		
			writer.write("\n");
			
			String counts = "Anzahl Dateiobjekte gesamt: " + traversal.getCntTotal() + Utils.LINE_SEPARATOR
					+ "Anzahl Dateien: " + traversal.getCntFile() + Utils.LINE_SEPARATOR
					+ "Anzahl Ordner: " + traversal.getCntDir() + Utils.LINE_SEPARATOR
					+ "Lesbare Dateiobjekte: " + (traversal.getCntTotal() - traversal.getCntFailed()) + Utils.LINE_SEPARATOR
					+ "Nicht lesbare Dateiobjekte gesamt: " + traversal.getCntFailed();
			writer.write(counts);
			writer.close();
			
		}
		catch(Exception e) {
			LOGGER.error("Fehler beim Schreiben der Traversal Vergleichs-CSV" + Utils.LINE_SEPARATOR
					+ Utils.getExceptionInfos(e));
		}
	}
	
	private static void resetFileCounts(FileStructure fs) {
		fs.fileTotal = 0;
		fs.fileCount = 0;
		fs.dirCount = 0;
		fs.readable = 0;
		fs.nonReadable = 0;
		fs.possiblyMoreFiles = 0;
	}	
	
	private static void printTreeAdvancedFilesHelper(List<Node<Path>> children, int ident) {	
		
		String s = "";
		if(children.size() == 0) {
			s = String.format(Utils.padLeftSpaces("", ident) 
					+ "%1$s%2$s", Utils.APPENDER_PRETTY_PRINT, "[]");	
			System.out.println(s);
		}
		else {
			for(int i = 0; i < children.size(); i++) {			
				if(children.get(i).getSpecialTwo()) { // <=> FILE
					
					// Durch diesen Trick möglich den ident wie gewollt zu setzen, was iwie nicht rein durch String.format ging komischerweise
					s = String.format(Utils.padLeftSpaces("", ident) 
							+ "%1$s%2$s", Utils.APPENDER_PRETTY_PRINT, children.get(i).getData());	
					System.out.println(s);
					// WG PatternLayout DEF JEDER ZEILE IMMER VORANGESTELLT MACHT KEINEN SINN IN LOG-FILE
					//LOGGER.debug(s);										
				}
			}
		}
	}
		// BEACHTE: .children() ÜBERGIBT ALLE, ALSO NICHT NUR DIREKTE !
	private static void printTreeAdvancedFolderHelper(List<Node<Path>> nodes, int depthOfRoot,
			Integer identInitial, Integer identStep) {
	
		for(int i = 0; i < nodes.size(); i++) { // IM ERSTEN SCHRITT AUCH root HIER DRIN 			
			if(nodes.get(i).getSpecialOne()) { // <=> DIRECTORY				

				int actualLevel = nodes.get(i).getData().getNameCount();				
				int ident = actualLevel-depthOfRoot==0 ? identInitial : (actualLevel-depthOfRoot)*identStep+1;
	
				String subfoldersFormatted = String.format(Utils.padLeftSpaces("", ident) 
						+ "%1$s%2$s", Utils.APPENDER_PRETTY_PRINT, nodes.get(i).getData());
				System.out.println(subfoldersFormatted); 
				subfoldersFormatted = String.format(Utils.padLeftSpaces("", ident) + "%1$" + identStep + "s|", "");		
				System.out.println(subfoldersFormatted); 				
				subfoldersFormatted = String.format(Utils.padLeftSpaces("", ident) + "%1$" + identStep + "s|", "");				
				System.out.println(subfoldersFormatted); 

				ident += identStep;
				//System.out.println(subfoldersFormatted); 
				// WG PatternLayout DEF JEDER ZEILE IMMER VORANGESTELLT MACHT KEINEN SINN IN LOG-FILE
				//LOGGER.debug(subfoldersFormatted);	
			
				printTreeAdvancedFilesHelper(nodes.get(i).getChildren(), ident);
			}
		}		
	}
	
	public static void printTreeAdvanced(DirectoryStructureFormatter traversal, Integer initialIdent, 
			Integer identStep) {
		
		if(identStep == null || identStep <= 0) {
			LOGGER.warn("Übergebener Ident ungültig: " + identStep==null ? "null" : identStep);
			LOGGER.warn("Setze IDENT_STEP = " + Utils.IDENT_STEP);
			identStep = Utils.IDENT_STEP;
		}		
		if(initialIdent == null || initialIdent <= 0) {
			LOGGER.warn("Übergebener initialIdent ungültig: " + initialIdent==null ? "null" : initialIdent);
			LOGGER.warn("Setze INITIAL_IDENT = 1");
			initialIdent = 1;
		}

		printTreeAdvancedFolderHelper(traversal.getFileStructureTree(), traversal.getDepthOfRoot(), initialIdent, identStep);		
	}	
	
	/*
	 * Trotz Builder-Pattern lässt sich anders keine sinnvollere Lösung für
	 * Gebrauch in CheckFilrsCmd finden als Kreierung dieses Setters
	 */
	public void setCsvPath(String csvPath) {
		if(!csvPath.endsWith("/") && !csvPath.endsWith("\\")) {
			csvPath += Utils.FILE_SEPARATOR;
		}
		this.csvPath = csvPath;
	}
	
	/*
	 * Trotz Builder-Pattern lässt sich anders keine sinnvollere Lösung für
	 * Gebrauch in CheckFilesCmd finden als Kreierung dieses Setters
	 */
	public void setCsvName(String csvName) {
		this.csvName = csvName;
	}
	
	public static String getCSVPathDefault() {
		return CSV_PATH_DEFAULT;
	}
	
	public static String getCSVNameDefault() {
		return CSV_NAME_DEFAULT;
	}
	
	public static String getStartPathDefault() {
		return START_PATH_DEFAULT;
	}
	
	public List<Data> getDataList() {
		return this.dataList;
	}	
	
	public String getStartPathString() {
		return start_path;
	}
	
	public Data getStartPathData() {
		return startPathData;
	}
	
	/*
	 * Muss bzw. soll jetzt sowohl für String als auch Data-Objektvariable in 1! Methode geschehen
	 */
	public void setStartPath(String start_path) {
		LOGGER.warn("StartPath auf Anforderung geändert!");
		LOGGER.warn("Daher neu Auslesen der Dateiliste nötig ... ");
		
		if(!start_path.endsWith("/") && !start_path.endsWith("\\")) {
			start_path += Utils.FILE_SEPARATOR;
		}
		this.start_path = start_path;
		
		try {
			startPathData = new Data(start_path);
		}
		catch(IOException e) {
			LOGGER.error("Data-Objekt aus Startpfad konnte nicht gebuildet werden!");
			LOGGER.error("Bitte erneut versuchen");
			return;
		}
		
		generateDataListFromPathAdvanced(start_path, true);
	}
	
	/*
	 * Durch Def. der div. Comparator nur 1! Methode nötig
	 * 
	 * Es ist also möglich, dass je nach Comparatorübergabe entweder nur der name
	 * oder name && size wichtig ist 
	 * Momentan ist als Default in CheckFilesCmdMenu#initiateFolderComparison()
	 * fest die Übergabe eines DataFilenameAndSizeComparator als Vergleichskriterium codiert
	 * 
	 * Beachte unsortierte und ggf unübersichtliche Ausgabe
	 * 
	 * Beachte Grenzen momentan: Wenn mehrere Ordner den gleichen Namen haben und aber
	 * unterschiedlichen Inhalt, also Grösse stellt sie dann trotzdem gegenüber und es scheint
	 * als ob dann DataFilenameAndSizeComparator widerspricht ...
	 * 
	 *  
	 */
	public List<Triplet<String, Double, Double>> getEqualDataObjectsWithOther(FileStructure other, 
			Comparator<Data> comparator) {
		
		// LÖSUNG DERART NICHT MÖGLICH, DA REINER NAME KEINE OBJEKTVARIABLE UND HIER
		// ... WÜRDE DANN IMMER DEN GANZEN PFAD VERGLEICHEN, ALSO SO ODER SO
		// ... WÄRE SCHLEIFE NÖTIG (ODER ANPASSUNG VON Data)
		//otherDataListCopy.removeIf(data -> !this.getDataList().contains(data));		
		// Predicate<Data> condition = data -> !this.getDataList().contains(data);

         
		 List<Triplet<String, Double, Double>> resultDataList = new ArrayList<Triplet<String, Double, Double>>();
		 for(int i = 0; i < this.getDataList().size(); i++) {
		     for(int j = 0; j < other.getDataList().size(); j++) {
		    	 
		    	 if(comparator.compare(this.getDataList().get(i), 
		    			 other.getDataList().get(j)) == 0) {
		    		 
		    		 Data thisData = this.getDataList().get(i);
		    		 Data otherData = other.getDataList().get(j);		    		 
		    		 
		    		 Double sizeThis = null;
		    		 Double sizeOther = null;
		    		 
		    		// Dateiname in beiden Fällen gleich
		    		 String fileName = new File(thisData.getPath()).getName();
		    		 
		    		 if(thisData.isDir()) {
		    			 try {
		    				 sizeThis = (double) generateDataSizeCompleteForSubfolder(thisData.getPath());
		    				 sizeThis = Data.getSizeInKB(sizeThis);
		    			 }
		    			 catch(RuntimeException r) {
		    				 LOGGER.error("Überspringe ...\n");
		    			 }		    			 
		    		 }
		    		 else {
		    			 sizeThis = thisData.getSizeInKB();
		    		 }
		    		 
		    		 if(otherData.isDir()) {
		    			 try {
		    				 sizeOther = (double) generateDataSizeCompleteForSubfolder(otherData.getPath());
		    				 sizeOther = Data.getSizeInKB(sizeThis);
		    			 }
		    			 catch(RuntimeException r) {
		    				 LOGGER.error("Überspringe ...\n");
		    			 }
		    		 }
		    		 else {		    			 
				    	 sizeOther = otherData.getSizeInKB();
		    		 }
		    		 
		    		 Triplet<String, Double, Double> tripletThis = Triplet.with(fileName, sizeThis, sizeOther);
		    		 resultDataList.add(tripletThis);
		    	 }
		     }
		 }	 
         return resultDataList;
	}	
	
	private static Long generateDataSizeCompleteForSubfolder(String start_path) {
		
		Path path_one = null;
		Long sizeComplete = 0L;
		try {
			path_one = Paths.get(start_path);
			DirectoryTraversal traversal = new DirectoryTraversal(path_one);
			
			Files.walkFileTree(path_one, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, traversal);
			Map<Path, Long> resultMapFileAndSize = traversal.getResultMapFileAndSize();			
		    for (Map.Entry<Path, Long> entry : resultMapFileAndSize.entrySet()) {		    	
		    	sizeComplete += entry.getValue();		    	
		    }
		}
		catch(Exception e) {
			LOGGER.error("Generieren der Dateiliste zu " + path_one + " für Bestimmung der Dateigrösse gesamt nicht möglich!\n");
			LOGGER.error(Utils.getExceptionInfos(e));
			throw new RuntimeException();
		}
		return sizeComplete;
	}
	
	
	public static List<List<String>> printAndReturnFolderComparison(List<Triplet<String, Double, Double>> tripletList) {
		
		// Durch diesen Trick ist es trotzdem möglich die Zuweisung unten im Lambda-Ausdruck zu realisieren !
		final List<List<String>>[] resultListToReturn = (ArrayList<List<String>>[]) new ArrayList[1];
		resultListToReturn[0] = new ArrayList<List<String>>();		
		
		try {		
			Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");			
			printHeaderWithStrideAccordingHeaderListSize(CSV_COLUMNS_FILES_EQUALS);			
			tripletList.stream().forEach(triplet -> {
				List<Object> tripletCollection = triplet.toList();
				List<String> tripletCollectionStrings = 
						tripletCollection.stream()
							.map(element -> {
								if(numericPattern.matcher(element+"").matches()) {
									return element + " KB";
								}
								return element.toString();
							})
							.collect(Collectors.toCollection(ArrayList<String>::new));	
				
				// Somit also Zuweisung für Weitergabe an Export möglich
				List<String> inner = new ArrayList<String>(tripletCollectionStrings);
				resultListToReturn[0].add(inner); 
			}); 			
		
			Collections.sort(resultListToReturn[0], new Comparator<List<String>>(){
					@Override
				    public int compare(List<String> inner1, List<String> inner2) {		
						
				    // Es wird also vorausgesetzt, dass das 0-te Element der Name ist
				    return inner1.get(0).toLowerCase().compareTo(inner2.get(0).toLowerCase());
				}
			});
			for(List<String> tripletRow : resultListToReturn[0]) {
				formatResultTripletRow(tripletRow);
			}
			
			return resultListToReturn[0];			
		}
		catch(Exception e) {
			LOGGER.error("Fehler bei Ausgabe der Vergleichsliste" + Utils.LINE_SEPARATOR
					+ Utils.getExceptionInfos(e));
		}
		return null;
	}
	
	private static void printHeaderWithStrideAccordingHeaderListSize(String header) {
		//int stride = tripletRow.size() / 3;
		
		List<String> headerList = Arrays.asList(CSV_COLUMNS_FILES_EQUALS.split(";"));	
		int stride = headerList.size() / 3;
		LOGGER.info(String.format("%-20s %-20s %-12s", headerList.get(0),
				headerList.get(stride), headerList.get(stride*2)));	
	}	
	
	private static void formatResultTripletRow(List<String> tripletRow) {		
		int stride = tripletRow.size() / 3;
		for (int row = 0; row < tripletRow.size() / 3; row++) {
			LOGGER.info(String.format("%-20s %-20s %-12s", tripletRow.get(row),
		    		tripletRow.get(row + stride), tripletRow.get(row + stride * 2)));
		}            
	}
	
	public String getFileTypeCounts() {
		return "Anzahl Dateiobjekte gesamt: " + fileTotal + Utils.LINE_SEPARATOR
				+ "Anzahl Dateien: " + fileCount + Utils.LINE_SEPARATOR
				+ "Anzahl Ordner: " + dirCount + Utils.LINE_SEPARATOR
				+ "Lesbare Dateiobjekte: " + readable + Utils.LINE_SEPARATOR
				+ "Nicht lesbare Dateiobjekte gesamt: " + nonReadable + Utils.LINE_SEPARATOR
				+ "Nicht lesbare Ordner mit ggf. weiteren enthaltenen Dateien: " + possiblyMoreFiles;
	}
	
	public void refreshDataList(Data d) {
		LOGGER.info("DataList wird aktualisiert ...");
		generateDataListFromPathAdvanced(start_path, true);
	}
	
	public void clearDataList() {
		dataList.clear();
		LOGGER.warn("DataList geleert - Justiere FileCounts ... ");
		resetFileCounts(this);		
	}
	
	@Override
	public String toString() {				
		dataList.sort(new DataPathComparator());
		
		String formatted = Utils.LINE_SEPARATOR;
		formatted += "--------------------------------------------------" + Utils.LINE_SEPARATOR;
		//formatted += "Start: " + getStartPathData().toString() + Utils.LINE_SEPARATOR; 		
		for(Data d: dataList) {
			formatted += d.toString() + Utils.LINE_SEPARATOR;
		}
		return formatted;
		
		
	}
	
	@Override
    protected Object clone() throws CloneNotSupportedException {
		// Assign the shallow copy to
        // new reference variable t
        FileStructure t = (FileStructure)super.clone();
 
        // Creating a deep copy for dummy
        t.dummy = new Dummy();
        // WENN DIESE IF NICHT GEMACHT; ARBEITET clone() bzgl Dummy Instanzvar nicht korrekt !
        if(this.dummy.getX() != null) {
        	t.dummy.setX(this.dummy.getX());
        }
        if(this.dummy.getY() != null) {
        	t.dummy.setY(this.dummy.getY());
        }
        
        t.startPathData = (Data)this.startPathData.clone();
        
        // Creating a deep copy for dataList
        t.dataList = new ArrayList<Data>(); // WENN DIES NICHT, ConcurrentModException, DA SONST AUF DERSELBEN REFERENZ, ALSO LISTE ADD UND READ !!!
        for(Data d: dataList) { // ... ALSO VORHER MIT new FüR deepCopy NöTIG !!!
        	t.dataList.add(d);
        }
        
        
        // Create a new object for the field dummy
        // and assign it to shallow copy obtained,
        // to make it a deep copy
        return t;	    
    }
	
	@Override
	public int hashCode() {
        int hash = 7;        
        hash = 31 * hash + (csvName == null ? 0 : csvName.hashCode());
        hash = 31 * hash + (csvPath == null ? 0 : csvPath.hashCode());
        hash = 31 * hash + (start_path == null ? 0 : start_path.hashCode());       
        hash = 31 * hash + (startPathData == null ? 0 : startPathData.hashCode()); 
        hash = 31 * hash + (dataList == null ? 0 : dataList.hashCode());
        hash = 31 * hash + fileTotal;
        hash = 31 * hash + fileCount;
        hash = 31 * hash + dirCount;
        hash = 31 * hash + readable;
        hash = 31 * hash + nonReadable;
        hash = 31 * hash + possiblyMoreFiles;
        hash = 31 * hash + (dummy == null ? 0: dummy.hashCode());
        return hash;
    }
	
	@Override
	public boolean equals(Object o) {
	    // self check
	    if (this == o)
	        return true;
	    // null check
	    if (o == null)
	        return false;
	    // type check and cast
	    if (getClass() != o.getClass())
	        return false;
	    FileStructure data = (FileStructure) o;
	    // field comparison
	    return Objects.equals(start_path, data.start_path)
	    		&& Objects.equals(startPathData, data.startPathData)
	            && Objects.equals(fileTotal, data.fileTotal)
	    		&& Objects.equals(dirCount, data.dirCount)
	    		&& Objects.equals(fileCount, data.fileCount)
	    		&& Objects.equals(readable, data.readable)
	    		&& Objects.equals(nonReadable, data.nonReadable)
	    		&& Objects.equals(possiblyMoreFiles, data.possiblyMoreFiles)
	    		&& dataList.equals(data.dataList);
	}	
		
	/* 
	 * Wenn in anderer Main nach Build doch noch Exportiert werden soll muss ja Option separat auch vorhanden sein
	 * 
	 * Benutzen der Default CSV-Werte so an sich nicht gegeben!
	 * Erfordert noch Zuarbeit in main
	 * 
	 */
	public boolean setCustomCsvPathAndName(String dir, String name) {		
				
		boolean successfully = validateAndSetCsvPath(dir, name);
		if(!successfully) {
			LOGGER.error("CSV Angaben konnten nicht verarbeitet werden - Bitte überprüfen!");
			return false;
		}
		return true;
	}	
	
	/*
	 * Keine Prüfung auf "/" bzw "\\" für path nötig, da bereits in setCustomCsvPathAndName
	 */
	private boolean validateAndSetCsvPath(String path, String name) {	
		if(path == null) {
			LOGGER.error("Kein CSV-Pfad angegeben!");
			return false;
		}
		
		if(path != null && !path.endsWith("/") && !path.endsWith("\\")) {
			path += Utils.FILE_SEPARATOR;
		}
		
		File f = new File(path);
    	if (!f.isDirectory()) {
    		LOGGER.error("Angegebener Pfad für CSV-Export: " + path + " existiert nicht!");
    		return false;
    	} 
    	else if (!Files.isReadable(f.toPath())) {
    		LOGGER.error("Angegebener Pfad für CSV-Export: " + path + " kann nicht gelesen werden !");
    		return false;
    	}
    	else if (!f.canWrite()) {
    		LOGGER.error("Unter angegebenem Pfad für CSV-Export: " + path + " kann nicht geschrieben werden !");
    		return false;
    	}    	

    	String check = Utils.isValidFileName(name, "csv");
    	if(check == null) {
    		LOGGER.error("Kein gültiger CSV-Name: " + name);
    		return false;
    	}
    	else {
    		csvPath = path;
    		csvName = check;    		
    	}    	
    	return true;
	}
	
	/*
	 * Wiederum Prüfung auf "/" im Pfad nötig, da auch in CheckFilesCmd gebraucht wird
	 */
	public String writeToCSV(Comparator<Data> comparator, boolean append) {			
		try {
			String resultFullPath = checkAndGetCSVFullPath(csvPath, csvName);			
			
			if(comparator != null) {			
				Collections.sort(this.dataList, comparator);			
			}			
			List<List<String>> dataListAsStrings = convertDataObjectsToStringList(this.dataList);
			FileWriter writer = new FileWriter(resultFullPath, append);
			writer.write(CSV_COLUMNS_FILES_LIST);
			writer.write("\n");

			dataListAsStrings.forEach(inner -> 
				{
					String collect = inner.stream().collect(Collectors.joining(";"));	
					try {
						writer.write(collect);					
						writer.write("\n");
					}
					catch (Exception e) {
						LOGGER.error("Error writing to CSV" + Utils.LINE_SEPARATOR
								+ Utils.getExceptionInfos(e));
					}
			});	    	
			LOGGER.info("CSV erstellt");	
			
			writer.write("\n");
			writer.write(getFileTypeCounts());
			writer.close();
		}
		catch(Exception e) {
			LOGGER.error("Fehler beim Schreiben der CSV" + Utils.LINE_SEPARATOR
					+ Utils.getExceptionInfos(e));
			throw new RuntimeException();
		}
	    return "success";
	}
	
	private static String checkAndGetCSVFullPath(String csvPath, String csvName) {
		String resultFullPath = null;
		String resultPath = null;
		String resultName = null;
		if(csvPath == null) {
			LOGGER.info("Kein Custom Exportpfad angegeben");	
			LOGGER.info("Daher verwende Default: " + CSV_PATH_DEFAULT);	
			resultPath = CSV_PATH_DEFAULT; // HIER ABER KEINE ANDERE MÖGLICHKEIT ALS DAS "/" GESETZT IST
		}
		else {
			if(!csvPath.endsWith("/") && !csvPath.endsWith("\\")) {
				csvPath += Utils.FILE_SEPARATOR;
			}
			resultPath = csvPath;
		}
		
		if(csvName == null) {
			LOGGER.info("Kein Custom Exportname angegeben");	
			LOGGER.info("Daher verwende Default: " + CSV_NAME_DEFAULT);	
			resultName = CSV_NAME_DEFAULT;
		}
		else {			
			if(!FilenameUtils.getExtension(csvName).equals("csv")) {
				csvName += ".csv";
			}
			resultName = csvName;
		}
		resultFullPath = resultPath + resultName;
		return resultFullPath;
	}
	
	private static List<List<String>> convertDataObjectsToStringList(List<Data> dataList) {
		List<List<String>> outer = new ArrayList<List<String>>();
		dataList.stream().forEach(data -> {
			List<String> inner = new ArrayList<String>();
			inner.add(data.toString());
			outer.add(inner);
		});		
	    return outer;
	}		
	
	/*
	 * Streng genommen wäre fast logischer wenn statisch, aber immerhin enstand aus Vgl mit this FS
	 */
	public boolean exportEqualDataObjectsWithOtherList(List<List<String>> equalsList,
			String dir, 
			String name) {	
		
		// ALSO VERWENDUNG VON GLEICHEN OBJEKTVARIABLEN FÜR CSV-ANGABEN
		// ALSO HIER WERDEN USEREINGABE ÜBERPRÜFT UND NUR GESETZT WENN OK !
		// HIER ALSO KEINE DEFAULT PFADE GESETZT
		boolean setSuccess = setCustomCsvPathAndName(dir, name);
		if(setSuccess) {
			return writeEqualDataListToCSVAdvanced(equalsList, false);
		}
		return false;
		
	}
	
	private boolean writeEqualDataListToCSVAdvanced(List<List<String>> resultList, boolean append) {
		try {
			String resultFullPath = csvPath + csvName;		
			final FileWriter writer = new FileWriter(resultFullPath, append);
			writer.write(CSV_COLUMNS_FILES_EQUALS);
			writer.write("\n");
				
			// ALSO EINFACH NACH name PER DEFAULT				
			Collections.sort(resultList, new Comparator<List<String>>(){
	            @Override
	            public int compare(List<String> inner1, List<String> inner2) {		  
	            	// Es wird also vorausgesetzt, dass das 0-te Element der Name ist
	            	return inner1.get(0).toLowerCase().compareTo(inner2.get(0).toLowerCase());
	            }
	        });	
				
			resultList.stream().forEach(line -> {
			String csvLine = String.join(";", line);							
				try {
					writer.write(csvLine);					
					writer.write("\n");	
				}
				catch(IOException e) {
					LOGGER.error("Fehler beim Schreiben der CSV" + Utils.LINE_SEPARATOR
							+ Utils.getExceptionInfos(e));
				}
			});				
			LOGGER.info("CSV erstellt");
			writer.close();	
		}
		catch(Exception e) {
			LOGGER.error("Fehler beim Schreiben der CSV" + Utils.LINE_SEPARATOR
					+ Utils.getExceptionInfos(e));
			throw new RuntimeException(); 
		}
		return true;
	}	
	
	/*
	 * Bzgl. java.nio.file.AccessDeniedException:
	 * 
	 * "No, this exception cannot be avoided.
	 * The exception itself occurs inside the the lazy fetch of Files.walk(), 
	 * hence why you are not seeing it early and why 
	 * there is no way to circumvent it (...)"
	 * 
	 * @see Verbesserte Alternative: generateDataListFromPathAdvanced
	 */
	@Deprecated
	private void generateDataListFromPath(String start_path, boolean adjustCounts) {
		try {
			Path path_one = Paths.get(this.start_path);
			Stream<Path> stream = null;
			
			try {
				stream = Files.walk(path_one, Integer.MAX_VALUE);
				dataList = stream.map(s -> 
					{
						try {
							 return new Data(s.toString(), Files.getLastModifiedTime(s));
						} 
						catch (IOException e) {
							e.printStackTrace();
							LOGGER.error("IOException creating FileStructure object by path: " + Utils.LINE_SEPARATOR
									+ Utils.getExceptionInfos(e));
						}
						return new Data();
					})
					.collect(Collectors.toList());
				
				stream.close();
			}
			catch(IOException e) {
				
			}

				
			if(adjustCounts) {
				setFileTypeCounts(this);
			}
		}
		catch(Exception e) {
			LOGGER.error("Generieren der Dateiliste nicht möglich!\n");
			LOGGER.error(Utils.getExceptionInfos(e));
			throw new RuntimeException();
		}
	}
	
	@Deprecated
	private static void setFileTypeCounts(FileStructure fs) {				
		resetFileCounts(fs); // WENN NICHT, WIRD ALLES DUPLIZIERT (ABER WIESO??) !
		fs.fileTotal = fs.dataList.size();
		for(Data d: fs.getDataList()) {		
			if(Files.isDirectory(Paths.get(d.getPath()))) {
				fs.dirCount++;
			}
			else {
				fs.fileCount++;
			}
			
			if(Files.isReadable(Paths.get(d.getPath()))) {
				fs.readable++;
			}
			else {
				fs.nonReadable++;
			}
		}
	}
	
	public static void main(String[] args) {
		
		try {				
			
		
			
//			System.out.println("#" + Utils.padLeftSpaces("mystring", 10) + "@");
//		    System.out.println("#" + Utils.padLeftSpaces("mystring", 15) + "@");
//		    System.out.println("#" + Utils.padLeftSpaces("mystring", 20) + "@");
//		    System.out.println("#" + Utils.padLeftSpaces("Dies", 15) + "@");
			
//			%[argument_index$][flags][width][.precision]conversion
//			String greetings = String.format("Hello %1$15s, welcome to %2$17s !", "Baeldung", "Folks");
//			System.out.println(greetings);			
//			String str = "DiesisteinTest";
//			String pretty = String.format("Without left justified flag: %1$5d %2$77s", 25, str);			
//			System.out.println(pretty);
			
			
//			String pathString = "C:/Users/Armin/Desktop/Test1";		
//			Path path = Paths.get(pathString);			
			
//			DirectoryStructureFormatter traversal = new DirectoryStructureFormatter(Paths.get(pathString));
			// HIERMIT WERDEN static KLASSENVARIABLEN VON TraversalKlasse GESETZT UND STRUKTUR AUFGEBAUT
//			Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, traversal);
			// DA IN walkFileTree DIE NODE-STRUKTUR GESETZT, LIEFERN NUN ALSO DIE ABFRAGE
			// ... DER KLASSENVARIABLEN VON traversal DIE AKTUELL GESETZTE STRUKTUR 
			// ACHTUNG: MUSS GGF. ZUR LAUFZEIUT DANN DIREKT RESETTET WERDEN, SONST BLEIBEN WERTE
			
			// TEST
			//traversal.getRelationInformations();			
			
			// NACH UMSTELLUNG REKURSIV JETZT AUCH DIE LOGIK MIT directChildren UMSTELLEN (?)...
			//FileStructure.printTreeAdvanced(traversal, 1, null);
			
			
//			String symlinkString = "C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesX64\\Microsoft Office\\Office16\\AppvIsvStream64.dll";
//			symlinkString = "C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\Source Engine\\AppvIsvStream32.dll";
//			Path symlinkPath = Paths.get(symlinkString);		
//			FileTime ft = Files.getLastModifiedTime(symlinkPath, LinkOption.NOFOLLOW_LINKS);			
//			Data d = new Data(symlinkString, Files.getLastModifiedTime(symlinkPath, LinkOption.NOFOLLOW_LINKS));
	
			
//			boolean isHidden = Files.isHidden(Paths.get(symlinkString));
//			LOGGER.info(isHidden);
			
//			DosFileAttributes dosFileAttributes = Files.readAttributes(Paths.get(symlinkString), 
//					DosFileAttributes.class, 
//					LinkOption.NOFOLLOW_LINKS);
//			
//			StringBuilder sb = new StringBuilder();
//			sb.append(dosFileAttributes.isReadOnly() ? "r" : "-");
//			sb.append(dosFileAttributes.isHidden() ? "h" : "-");
//			sb.append(dosFileAttributes.isArchive() ? "a" : "-");
//			sb.append(dosFileAttributes.isSystem() ? "s" : "-");
//			LOGGER.info(sb.toString());			
//			LOGGER.info(dosFileAttributes.isHidden());
			
			
			
//			String pathString = "C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesX64\\Microsoft Office\\Office16\\";		
//			FileStructure fs = new FileStructureBuilder(pathString)
//					.dummy(new Dummy("ini", "ini"))
//					.build();			
//			fs.generateDataListFromPathAdvanced(pathString, true);
			
			
//			String pathString ="C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\Source Engine\\AppvIsvStream32.dll";
//			Path p = Paths.get(pathString);
//			Data d = new Data(pathString, Files.getLastModifiedTime(p, LinkOption.NOFOLLOW_LINKS));
//			LOGGER.info(d.toString());
//			FileStructure fs = new FileStructureBuilder(pathString)
//			.dummy(new Dummy("ini", "ini"))
//			.build();	

//			List<String> problemPathList = new ArrayList<>();
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\Source Engine\\AppvIsvStream32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX64\\Microsoft Shared\\OFFICE16\\AppvIsvSubsystems64.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\Source Engine\\AppvIsvSubsystems32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\OFFICE16\\C2R32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\DW\\C2R32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\EQUATION\\C2R32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\EQUATION\\AppvIsvStream32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\DW\\AppvIsvStream32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\DW\\AppvIsvSubsystems32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\OFFICE16\\AppvIsvStream32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX64\\Microsoft Shared\\OFFICE16\\AppvIsvStream64.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX64\\Microsoft Shared\\OFFICE16\\C2R64.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesX64\\Microsoft Office\\Office16\\AppvIsvStream64.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesX64\\Microsoft Office\\Office16\\C2R64.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\Source Engine\\C2R32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\EQUATION\\AppvIsvSubsystems32.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesX64\\Microsoft Office\\Office16\\AppvIsvSubsystems64.dll");
//			problemPathList.add("C:\\Program Files (x86)\\Microsoft Office\\root\\VFS\\ProgramFilesCommonX86\\Microsoft Shared\\OFFICE16\\AppvIsvSubsystems32.dll");
//			
//			int dirCount = 0;
//			int fileCount = 0;
//			int readable = 0;
//			int nonReadable = 0;
//			int possiblyMoreFiles = 0;
//			
//			int otherCount = 0;
//			int regularCount = 0;
//			int symbolicLink = 0;
//			
//			StringBuilder sb = new StringBuilder();			
//			for(String s : problemPathList) {
//				Path p = Paths.get(s);
//				BasicFileAttributes basicAttr = Files.readAttributes(p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
//				
//				if(Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
//					dirCount++;					
//				}
//				else if(Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
//					fileCount++;						
//				}
//				else if(Files.isSymbolicLink(p)) {
//					fileCount++;	
//				}
//				
//				if(Files.isReadable(p)) {
//					readable++;					
//				}
//				else {
//					nonReadable++;					
//					if(Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
//						possiblyMoreFiles++;						
//					}
//				}
//				
//				if(basicAttr.isOther()) {
//					otherCount++;
//				}
//				if(basicAttr.isRegularFile()) {
//					regularCount++;
//				}
//				if(basicAttr.isSymbolicLink()) {
//					symbolicLink++;
//				}
//				
//				
//			}
//			sb.append("dirCount: " + dirCount + "\n");
//			sb.append("fileCount: " + fileCount + "\n");
//			sb.append("readable: " + readable + "\n");
//			sb.append("nonReadable: " + nonReadable + "\n");
//			sb.append("possiblyMoreFiles: " + possiblyMoreFiles + "\n\n");
//			sb.append("otherCount: " + otherCount + "\n");
//			sb.append("regularCount: " + regularCount + "\n");
//			sb.append("symbolicLink: " + symbolicLink + "\n");
//			System.out.println(sb.toString());

			
			List<String> list = Arrays.asList("A", "B", "C", "D", "E");
			list.forEach( data -> {
					try {
						if(data.equals("C")) {
							throw new RuntimeException();
						}
						else {
							System.out.println(data);
						}
					}
					catch(RuntimeException e) {
						System.out.println("RuntimeException catched");
						
						// Foreach() cannot use the two keywords break and continue. 
						// It can achieve the break effect by throwing an exception, 
						//and the continue effect can be directly used by return.
						//return;
					}
			});	
			
			
			
			
		}
		catch(Exception e) {
			LOGGER.error(Utils.getExceptionInfos(e));
		}
		
	}
}
