package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import classes.CheckFilesCmdMenu;

public class Utils {
	
	private static final Logger LOGGER = LogManager.getLogger(Utils.class);
	public static final String LINE_SEPARATOR = System.lineSeparator();	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");  // "\" WIN, "/" UNIX
	
	//private static final String INI_FILE = "src/resources/config.ini";
	//private static final String WINI_FILE = "./resources/config.ini";
	private static final String INI_FILE = "resources/config.ini";
	private static final String WINI_FILE = "resources/config.ini";
	
	private static Ini INI = null;		
	private static String LOG_NAME_PREFIX;
	public static final String DATA_PRINT_HEADER = "path;lastModifiedTime;isRegularFile;isDir;isSymbolicLink;isReadable;isHidden;size";
	private static final char[] ILLEGAL_CHARACTERS = { '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '/', '\n',
            '\r', '\t', '\0', '\f' };
	private static final String[] ILLEGAL_FILENAMES = { "CON", "PRN", "AUX", "CLOCK$", "NUL",
			"COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
			"LPT0", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
	
	public static final int ESCAPE_CODE = 27;
	public static final int GO_BACK = 90;	
	
	public static final String APPENDER_PRETTY_PRINT = "---";
	public static final Integer INITIAL_IDENT = 1;
	public static final Integer IDENT_STEP = 4;
	
	static {
		try {
			buildFromIniFile();
		}
		catch(Exception e) {
			LOGGER.error(Utils.getExceptionInfos(e));
		}
	}	
	
	public static void buildFromIniFile() throws InvalidFileFormatException, IOException {
		try {			
			
			//INI = new Ini(new File(INI_FILE)); // FÜR .jar so nicht möglich
			INI = new Ini(getFileFromResourceAsStream(INI_FILE));
			LOG_NAME_PREFIX = INI.get("log_name_prefix", "prefix");
		}
		catch(Exception e) {
			LOGGER.error(Utils.getExceptionInfos(e));
			LOGGER.error("Ini konnte nicht gebuildet werden !\nVersuche Wini ...");
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream inputStream = classloader.getResourceAsStream(WINI_FILE);
			INI = new Wini(inputStream);
		}
	}
	
	public static InputStream getFileFromResourceAsStream(String fileName) {

		// The class loader that loaded the class
		ClassLoader classLoader = Utils.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		// the stream holding the file content
		if (inputStream == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		}
		else {
			return inputStream;
		}
	}
	
	public static Ini getIni() {
		return INI;
	}
	
	public static final DateTimeFormatter TIMESTAMP_PARSER_DE = new DateTimeFormatterBuilder()
			   .parseCaseInsensitive()
			   .append(DateTimeFormatter.ofPattern("dd.MM.uuuu'T'HH:mm:ss", Locale.GERMANY))
				// optional decimal point followed by 1 to 9 digits
			   .optionalStart()
			   .appendPattern(".")
			   .appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
			   .optionalEnd()
			   .appendPattern("X") // WENN INNERHALB optional TEIL GEHT NUR WENN ALLE OPTIONALEN TEILE VORHANDEN WIE ES SCHEINT
			   .toFormatter()	// ... ALSO ZB GINGE GEHT NICHT: 'X' ZWAR VORHANDEN IN STRING ABER KEINE millisec, DIESE KOMBI WüRDE ALSO ZU EXCEPTION FüHREN
			   .withResolverStyle(ResolverStyle.STRICT);
	
	public static final DateTimeFormatter TIMESTAMP_PARSER_ENG = new DateTimeFormatterBuilder()
			   .parseCaseInsensitive()
			   .append(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"))
				// optional decimal point followed by 1 to 9 digits
			   .optionalStart()
			   .appendPattern(".")
			   .appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false)
			   .optionalEnd()
			   .appendPattern("X")
			   .toFormatter()
			   .withResolverStyle(ResolverStyle.STRICT);
	
	public static final DateTimeFormatter TIMESTAMP_OUTPUT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
	
	public static final String getExceptionInfos(Exception e) {
		String errorStr = "Exception: " + Utils.LINE_SEPARATOR;
		errorStr += e.getMessage() + Utils.LINE_SEPARATOR;
		errorStr += e.getCause() + Utils.LINE_SEPARATOR;
		
		Throwable[] sup = e.getSuppressed();
        for (Throwable i: sup) {	  
            errorStr += i.toString() + Utils.LINE_SEPARATOR;
        }
        
        errorStr += ExceptionUtils.getStackTrace(e) + Utils.LINE_SEPARATOR;
        return errorStr;
	}
	
	public static void printCurrentThreadStatus(Thread t) {		
		LOGGER.info(formatCurrentThreadStatus(t));
	}
	
	private static String formatCurrentThreadStatus(Thread t) {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: " + t.getId() + Utils.LINE_SEPARATOR);
		sb.append("Name: " + t.getName() + Utils.LINE_SEPARATOR);
		sb.append("State: " + t.getState() + Utils.LINE_SEPARATOR);
		sb.append("Priority: " + t.getPriority() + Utils.LINE_SEPARATOR);	

		for(StackTraceElement ste: Thread.currentThread().getStackTrace())
			sb.append("StackTrace: " + ste.toString() + Utils.LINE_SEPARATOR);
		
		return sb.toString();
	}

	public static void printCurrentThreadStatus() {		
		LOGGER.info(formatCurrentThreadStatus());
	}
	
	private static String formatCurrentThreadStatus() {
        StringBuilder sb = new StringBuilder();
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Iterator<Entry<Thread, StackTraceElement[]>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
        	Entry<Thread, StackTraceElement[]> entry = iter.next();
            sb.append(entry.getKey().getName() + ":" + Utils.LINE_SEPARATOR);
            
            for(StackTraceElement ste: entry.getValue())
            	sb.append("StackTrace: " + ste.toString() + Utils.LINE_SEPARATOR);
        
        }
        return sb.toString();
    }
	
	public static void deleteLogFiles() {

		try {
			if(LOG_NAME_PREFIX == null) {
				throw new RuntimeException("LOG_NAME_PREFIX is NULL!\nAbort deleteLogFiles() ...");
			}
			
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			Configuration config = ctx.getConfiguration();
			FileAppender fileAppender = (FileAppender) config.getAppender("FileAppender");
			String currentLogFile = fileAppender.getFileName();

			
			//TEST
			System.out.println("\ncurrentLogFile: "+currentLogFile+"\n");
			
			
			
			File logDir = new File(fileAppender.getFileName()
					.replaceAll("[0-9]", "")
					.replaceAll("-", "")
					.replaceAll("_", "")					
					.replaceAll(".log", "")
					.replaceAll(LOG_NAME_PREFIX, "")
					);
			Path path = Paths.get(logDir.getAbsolutePath());				
			
			Stream<Path> list = Files.list(path);     
	        list.filter(p -> {
	            if (!Files.isDirectory(p)) {
	                return true;
	            }
	            return false;            
	            
	        }).forEach(p -> {
	        	
	        	//TEST
	        	System.out.println("\nLogFile to inspect: "+p+"\n");
	        	
	        	String currentExtenson = FilenameUtils.getExtension(p.toAbsolutePath().toString());
	        	
	        	//TEST
	        	System.out.println(p.getFileName().toString());
	        	System.out.println("equals");
	        	System.out.println(currentLogFile);
	        	
	        	
	        	if(currentExtenson.equals("log") && !(p.getFileName().toString().equals(currentLogFile))) {
	        		
	        		//TEST
		        	System.out.println("LogFile to delete, found not to be actual: "+p+"\n");
	        		
	        		try {
						Files.delete(p);
					} 
	        		catch (IOException e) {
						LOGGER.error(Utils.getExceptionInfos(e));
					}
	        	}
	        });	   
	        list.close(); 
	        
		}
		catch(Exception e) {
			LOGGER.error(Utils.getExceptionInfos(e));
		}        
	}
	
	private static void printPathInfo(Path path) {
        System.out.printf("Path: %s, isDir: %s%n", path,
                Files.isDirectory(path));
    }
	
    public static String isValidFileName(String fileName, String suffixToCheckForSet) {
        
    	if (fileName == null || fileName.isBlank()) {
        	LOGGER.error("Dateiname leer!");
            return null;
        }
        
        if(suffixToCheckForSet == null || suffixToCheckForSet.isBlank()) {
        	LOGGER.error("Kein Suffix für die Prüfung angegeben!");
        	return null;
        }

        for (char c : ILLEGAL_CHARACTERS) {
            if (fileName.indexOf(c) != -1) {
            	LOGGER.error("Unzulässige Zeichen im Dateinamen!" + Utils.LINE_SEPARATOR
            			+ "Nicht enthalten sein dürfen:" + Utils.LINE_SEPARATOR
            			+ "'`', '?', '*', '\\', '<', '>', '|', '\"', ':', '/'");
            	
                return null;
            }
        }
        
        for(String s : ILLEGAL_FILENAMES) {
        	if(fileName.trim().equals(s)) {
        		LOGGER.error("Unzulässige Zeichen im Dateinamen!" + Utils.LINE_SEPARATOR
        				+ "Unzulässig sind:" + Utils.LINE_SEPARATOR
        				+ "CON, PRN, AUX, CLOCK$, NUL" + Utils.LINE_SEPARATOR
        				+ "COM0, COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8, COM9" + Utils.LINE_SEPARATOR
        				+ "LPT0, LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, LPT9");
        				
        		return null;
        	}
        }
        
        String fileNameWithCheckedSuffix = checkSuffixAndSetIfNot(fileName, suffixToCheckForSet);
        if(fileNameWithCheckedSuffix != null) {
        	return fileNameWithCheckedSuffix;
        }
        return null;
    }
	
     public static String checkSuffixAndSetIfNot(String fileName, String suffixToCheckForSet) {
    	 
    	 // SELBE PRÜFUNGEN WIE IN isValidFileName, DA JA FORMAL BEIDE METHODEN UNABH AUFGERUFEN KÖNNEN WERDEN SOLLTEN
    	 if(suffixToCheckForSet == null || suffixToCheckForSet.isBlank()) {
    		 LOGGER.error("Kein Suffix zur Prüfung angegeben!");
    		 return null;
    	 }
    	 if(fileName == null || fileName.isBlank()) {
    		 LOGGER.error("Dateiname leer!");
    		 return null;
    	 }
    	 
    	 if(FilenameUtils.getExtension(fileName).equals(suffixToCheckForSet)) {
    		 return fileName;
    	 }
    	 else {
    		 String splitted[] = fileName.split("\\.");
    		 return splitted[0] + "." + suffixToCheckForSet;
    	 }			
     } 
	
	public static String replaceLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos > -1) {
	        return string.substring(0, pos)
	             + replacement
	             + string.substring(pos + toReplace.length());
	    } else {
	        return string;
	    }
	}	
	
	public static List<Path> listFiles(Path path, int depth) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path, depth)) {
            result = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return result;
    }

	public static List<Path> listDirectories(Path path, int depth) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path, depth)) {
            result = walk.filter(Files::isDirectory)
                    .collect(Collectors.toList());
        }
        return result;
    }
	
	public static Optional<Path> findDeepest(Path p) throws IOException {
        try (Stream<Path> s = Files.walk(p)) {
            return s.filter(Files::isRegularFile).map(p::relativize).max(Comparator.comparing(Path::getNameCount));
        }
    }
	
	public static String padLeftSpaces(String str, int n) {
		return String.format("%1$" + n + "s", str);
	}
	
	public static String padLeftCustom(String str, int n) {
		return String.format("%1$" + n + "s", str).replace(' ', '-');
	}
}
