package classes;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;

import utils.Utils;

/*
 * Kann Datei oder Ordner sein
 * 
 * Achtung: Wenn Ordner, gem. Logik wird nicht festgehalten wieviele Dateien noch enthalten sind
 * 
 */
public class Data implements Cloneable {

	private static final Logger LOGGER = LogManager.getLogger(Data.class);
	private String path;
	private FileTime lastModifiedTime;
	private boolean isDir = false;
	private boolean isRegularFile = false;
	private boolean isSymbolicLink = false;
	private boolean isReadable = false;
	private boolean isHidden = false;
	private Dummy dummy = new Dummy();
	
	// optional, erst nach Kreierung zu setzen, da sonst zu viele Änderungen an Bestehendem nötig wären
	private Long size = null; 
	
	
	public Data() {
		path = "/DEFAULT";			
		
		try {
			String date = "01.01.1970T00:00:00.000Z";
			LocalDateTime ldt = LocalDateTime.parse(date, Utils.TIMESTAMP_PARSER_DE);	
			
			// "Europe/Berlin" FüHRT DAZU, DASS SPäTER BEI PRINT DIESES DATUM-1h! 
			//ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Berlin"));
			ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));			
			lastModifiedTime = FileTime.fromMillis(zdt.toInstant().toEpochMilli());

		}
		catch(Exception e) {
			LOGGER.error("Error creating Data object:" + Utils.LINE_SEPARATOR 
					+ Utils.getExceptionInfos(e));
		}
	}	
	
	/*
	 * Ggf. etwas unglücklich, da lastModifiedTime nicht automatisch bestimmt wird
	 * und als String übergeben werden soll
	 * 
	 * @see Verbesserung in Data(String path) 
	 */
	public Data(String path, String lastModifiedTime) {
		this.path = path;		
		//System.out.println("Given lastModifiedTime: " + lastModifiedTime.toString());
		
		try {
			String date = lastModifiedTime;
			
			LocalDateTime ldt = null;
			try {
				ldt = LocalDateTime.parse(date, Utils.TIMESTAMP_PARSER_DE);
			}
			catch(DateTimeParseException de) {
				//LOGGER.info(de.getMessage());
				//LOGGER.info("Pattern switch during format date necessary, try with: " + Utils.TIMESTAMP_PARSER_ENG);
				ldt = LocalDateTime.parse(date, Utils.TIMESTAMP_PARSER_ENG);
			}
			
			ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));			
			this.lastModifiedTime = FileTime.fromMillis(zdt.toInstant().toEpochMilli());			
			isDir = Files.isDirectory(Paths.get(path), LinkOption.NOFOLLOW_LINKS);
			isRegularFile = Files.isRegularFile(Paths.get(path), LinkOption.NOFOLLOW_LINKS);
			isSymbolicLink = Files.isSymbolicLink(Paths.get(path));
			isReadable = Files.isReadable(Paths.get(path));			
			
			// Wirft ggf Exception, da immer symbolic Links followed und kann sein dass nicht vorhanden
			// ... jedoch soll ja gar nicht follow, aber keine Möglichkeit umzustellen
			//isHidden = Files.isHidden(Paths.get(path));			
			DosFileAttributes dosFileAttributes = Files.readAttributes(Paths.get(path), 
					DosFileAttributes.class, 
					LinkOption.NOFOLLOW_LINKS);
			isHidden = dosFileAttributes.isHidden();
			
		}
		catch(Exception e) {
			LOGGER.error("Error creating Data object with arguments: " + path + " -- " + lastModifiedTime
					+ Utils.LINE_SEPARATOR 
					+ Utils.getExceptionInfos(e));
		}
	}
	
	/*
	 * Neu: Automatische Bestimmung der lastModifiedTime
	 */
	public Data(String path) throws IOException {
		this(path, Files.getLastModifiedTime(Paths.get(path), LinkOption.NOFOLLOW_LINKS).toString());
	}
	
	public Data(String path, FileTime lastModifiedTime) {
		this(path, lastModifiedTime.toString());
	}
	
	public Data(Data other) {
		this.path = other.path;
		this.lastModifiedTime = other.lastModifiedTime;
		this.isDir = other.isDir;
		this.isRegularFile = other.isRegularFile;
		this.isSymbolicLink = other.isSymbolicLink;
		this.isReadable = other.isReadable;
		this.isHidden = other.isHidden;
		this.size = other.size;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public void setPath(String newPath) {
		this.path = newPath;
	}
	
	public void setLastModifiedTime(FileTime newModifiedTime) {
		this.lastModifiedTime = newModifiedTime;
	}

	public FileTime getLastModifiedTime() {
		return this.lastModifiedTime;
	}
	
	public boolean isDir() {
		return isDir;
	}
	
	public boolean isSymbolicLink() {
		return isSymbolicLink;
	}

	public boolean isRegularFile() {
		return isRegularFile;
	}
	
	public boolean isReadable() {
		return isReadable;
	}
	
	public boolean isHidden() {
		return isHidden;
	}
	
	public Long getSizeInByte() {
		return size;
	}
	
	public Double getSizeInKB() {		
		if(this.size != null) {		
			Double sizeExact = (double)size / 1024;
						
			DecimalFormat twoDForm = new DecimalFormat("#.##");
		    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		    dfs.setDecimalSeparator('.');
		    twoDForm.setDecimalFormatSymbols(dfs);
		    twoDForm.setRoundingMode(RoundingMode.UP);	
		    
		    return Double.valueOf(twoDForm.format(sizeExact));					
		}
		return null;
	}
	
	public static Double getSizeInKB(Double size) {		
		if(size != null) {		
			Double sizeExact = (double)size / 1024;
						
			DecimalFormat twoDForm = new DecimalFormat("#.##");
		    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		    dfs.setDecimalSeparator('.');
		    twoDForm.setDecimalFormatSymbols(dfs);
		    twoDForm.setRoundingMode(RoundingMode.UP);	
		    
		    return Double.valueOf(twoDForm.format(sizeExact));					
		}
		return null;
	}
	
	public void setSize(Long size) {
		this.size = size;
	}
	
	public void calcSize(Long size) {
		try {
			this.size = Files.size(Paths.get(path));
		}
		catch(IOException e) {
			LOGGER.error(Utils.getExceptionInfos(e));
			LOGGER.error("Fehler beim Berechnen der Dateigrösse");
		}
	}
	
	public boolean checkIfValidPath() {
		boolean exists = Files.exists(Paths.get(path));
		if(!exists) {
			LOGGER.warn("Datei / Pfad: \"" + path + "\" nicht existent!");
		}
		return Files.exists(Paths.get(path));
	}
	
	@Override
	public String toString() {		
		String sizeFormat = "";
		if(this.getSizeInKB() != null) {
			sizeFormat = this.getSizeInKB() + " KB";
		}		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastModifiedTimeFormatted = df.format(getLastModifiedTime().toMillis());
		
		return this.path + ";" 
				+ lastModifiedTimeFormatted + ";"
				+ this.isRegularFile + ";" 
				+ this.isDir + ";"
				+ this.isSymbolicLink  + ";"
				+ this.isReadable + ";"
				+ this.isHidden + ";"
				+ sizeFormat;
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
	    Data data = (Data) o;
	    // field comparison
	    return Objects.equals(path, data.path)
	            && Objects.equals(lastModifiedTime, data.lastModifiedTime)
	    		&& Objects.equals(isSymbolicLink, data.isSymbolicLink)
	    		&& Objects.equals(isRegularFile, data.isRegularFile)
	    		&& Objects.equals(isReadable, data.isReadable)
	    		&& Objects.equals(isDir, data.isDir)
	    		&& Objects.equals(isHidden, data.isHidden)
	    		&& (size == null ? data.size == null : size.equals(data.size));
	}
	
	@Override
	public int hashCode() {
        int hash = 7;        
        hash = 31 * hash + (path == null ? 0 : path.hashCode());
        hash = 31 * hash + (lastModifiedTime == null ? 0 : lastModifiedTime.toString().hashCode());
        hash = 31 * hash + (isDir ? 1 : 0);
        hash = 31 * hash + (isRegularFile ? 1 : 0);
        hash = 31 * hash + (isSymbolicLink ? 1 : 0);
        hash = 31 * hash + (isReadable ? 1 : 0);
        hash = 31 * hash + (isHidden ? 1 : 0);
        hash = 31 * hash + (size == null ? 0 : size.hashCode());
        return hash;
    }

	@Override
    protected Object clone() throws CloneNotSupportedException {
		// Assign the shallow copy to
        // new reference variable t
        Data t = (Data)super.clone();
        
        // Creating a deep copy for dummy
        t.dummy = new Dummy();  
        
        // Create a new object for the field dummy
        // and assign it to shallow copy obtained,
        // to make it a deep copy
        return t;	    
    }
	
}
