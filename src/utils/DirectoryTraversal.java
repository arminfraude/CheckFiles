package utils;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DirectoryTraversal implements FileVisitor<Path> {
    
	private static final Logger LOGGER = LogManager.getLogger(DirectoryTraversal.class);
    private Path startPath = null;
    private int cntTotal = 0;
    private int cntDir = 0;
    private int cntFile = 0;
    private int cntFailed = 0;   
    private Map<Path, Long> resultMapFileAndSize = new HashMap<Path, Long>();
    
    
    public DirectoryTraversal(Path startPath) {
    	this.startPath = startPath;
    }
    
    /*
     * Invoked for a DIRECTORY before entries in the directory are visited. 
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        //LOGGER.info("About to visit directory: " + dir);
        resultMapFileAndSize.put(dir, attrs.size());
    	cntTotal++;
    	cntDir++;    	    	
        return FileVisitResult.CONTINUE;
    } 
    
    /*
     * Achtung: Wenn Datei nicht lesbar ist, geht trotzdem hier rein und
     * nicht in visitFileFailed weswegen für korrektes cntFile hier Abprüfung erfolgen muss
     * 
     * Nur nicht lesbare Ordner führen zu exec von visitFileFailed
     * (wie es scheint bzw bisher so festgestellt)
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      //  LOGGER.debug("Visiting file: " + file.toString());
        resultMapFileAndSize.put(file, attrs.size());
        cntTotal++;
        cntFile++;       
        
        if(!Files.isReadable(file)) {
        	LOGGER.error("Visiting but found to be nonReadable: " + file.toString());
        	cntFailed++;
        }
 
        return FileVisitResult.CONTINUE;
    }
    
    /*
     * Wenn Verzeichnis nicht lesbar erfolgt kein Hochzählen in preVisitDirectory
     * daher muss hier abgeprüft werden
     * 
     * Abprüfung gem. FileVisitor<T> via BasicFileAttributes
     * 
     * Achtung: Wenn Datei nicht lesbar geht NICHT hier rein, sondern bleibt trotzdem bei visited !
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        LOGGER.error("Failed to access file: " + file.toString());
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        resultMapFileAndSize.put(file, attrs.size());
        cntTotal++;
        cntFailed++;   
        
        if(attrs.isDirectory()) {
        	cntDir++;
        }        
        return FileVisitResult.CONTINUE; // <=> GENAU WIE SKIP_SUBTREE, WOHL WEIL JA EH NICHT REINKANN
        //return FileVisitResult.SKIP_SUBTREE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    	
    	try {
	    	boolean finishedSearch = Files.isSameFile(dir, startPath);
	        if (finishedSearch) {
	        	LOGGER.debug("Traversal beendet!");
	            return FileVisitResult.TERMINATE;
	        }
    		//return FileVisitResult.CONTINUE; // GLEICHES ERGEBNIS WIE BEI FileVisitResult.TERMINATE !
    	}
    	catch(Exception e) {
    		LOGGER.error(Utils.getExceptionInfos(e));
    		LOGGER.error("\nAbbruch, da keine Aussage gemacht werden kann zu Position in der Suche...\n");
    	}
        return FileVisitResult.CONTINUE;
    }
    
    public int getCntFailed() {
    	return cntFailed;
    }
    
    public int getCntDir() {
		return cntDir;
	}

	public int getCntFile() {
		return cntFile;
	}

	public int getCntTotal() {
		return cntTotal;
	}
    
    public Map<Path, Long> getResultMapFileAndSize() {
    	return resultMapFileAndSize;
    }

    public void reset() {
    	cntDir= 0;
        cntFailed = 0;
        cntFile = 0;
        cntTotal = 0;
        resultMapFileAndSize.clear();
        LOGGER.debug("DirectoryTraversal object cleared");
    }

    
    public static void main (String... args) {
    	
    	
    }
}
