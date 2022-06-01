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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

public class DirectoryStructureFormatter implements FileVisitor<Path> {
    
	private static final Logger LOGGER = LogManager.getLogger(DirectoryStructureFormatter.class);
    private static Path startPath = null;
    private static Node<Path> rootNode = null;
    private static Node<Path> currentlyPointedTo = null; 
    private static List<Node<Path>> fileStructureTree = null; 
    private static int levelOfRoot = 0;
    private static int levelOfCurrent = 0;
    private static int maximumLevel = 0;
    private static int cntTotal = 0;
    private static int cntDir = 0;
    private static int cntFile = 0;
    private static int cntFailed = 0;   
    
    
    public DirectoryStructureFormatter(Path startPath) {
    	DirectoryStructureFormatter.startPath = startPath;
    	
    	// Startpunkt des Traversals == rootNode
    	rootNode = new Node<>(startPath);
    	currentlyPointedTo = rootNode;    	
    	levelOfRoot = startPath.getNameCount();
    	levelOfCurrent = levelOfRoot;
    	fileStructureTree = new ArrayList<Node<Path>>();
    	fileStructureTree.add(rootNode);    	
    	rootNode.setSpecialOne(true);
    	rootNode.setSpecialTwo(false);
    	
    	if(levelOfCurrent > maximumLevel) {
    		maximumLevel = levelOfCurrent;
    	}
    }
    
    /*
     * Invoked for a DIRECTORY before entries in the directory are visited. 
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        //LOGGER.debug("About to visit directory: " + dir);
        
        if(dir.getNameCount() > levelOfRoot) {
        	currentlyPointedTo = new Node<>(dir);
        	
        	//LOGGER.debug("currentlyPointedTo: " + currentlyPointedTo);
        	
        	levelOfCurrent = dir.getNameCount();
        	currentlyPointedTo.setSpecialOne(true);
        	currentlyPointedTo.setSpecialTwo(false);
        	if(levelOfCurrent > maximumLevel) {
        		maximumLevel = levelOfCurrent;
        	}
        	
        	Node<Path> directParent = searchDirectoryNodeByLevel(--levelOfCurrent, currentlyPointedTo);    
        	if(directParent == null) {        		
        		LOGGER.error("Kein Vaterknoten zu " + dir + " gefunden, daher Abbruch Traversal!");
        		LOGGER.error("Bitte Überprüfen ...");
        		return FileVisitResult.TERMINATE;
        	}
        	
        	directParent.addChild(currentlyPointedTo);
        	fileStructureTree.add(currentlyPointedTo);
        	//LOGGER.debug("found parent: " + directParent + " of " + currentlyPointedTo);
        }        
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
        //LOGGER.debug("Visiting file: " + file.toString());
        cntTotal++;
        cntFile++;       
        
        if(!Files.isReadable(file)) {
        	LOGGER.error("Visiting but found to be nonReadable: " + file.toString());
        	cntFailed++;
        }
        
        if(file.getNameCount() > levelOfRoot) {
        	currentlyPointedTo = new Node<>(file);
        	
        	//LOGGER.debug("currentlyPointedTo: " + currentlyPointedTo);
        	
        	levelOfCurrent = file.getNameCount();
        	currentlyPointedTo.setSpecialTwo(true);
        	currentlyPointedTo.setSpecialOne(false);
        	if(levelOfCurrent > maximumLevel) {
        		maximumLevel = levelOfCurrent;
        	}
        	
        	Node<Path> directParent = searchDirectoryNodeByLevel(--levelOfCurrent, currentlyPointedTo);         	
        	if(directParent == null) {
        		LOGGER.error("Kein Vaterknoten zu " + file + " gefunden, daher Abbruch Traversal!");
        		LOGGER.error("Bitte Überprüfen ...");
        		return FileVisitResult.TERMINATE;
        	}
        	
        	directParent.addChild(currentlyPointedTo);
        	fileStructureTree.add(currentlyPointedTo);        	
        	//LOGGER.debug("found parent: " + directParent + " of " + currentlyPointedTo);
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
        cntTotal++;
        cntFailed++;   
        
        currentlyPointedTo = new Node<>(file);
       // LOGGER.debug("currentlyPointedTo: " + currentlyPointedTo);    	
    	levelOfCurrent = file.getNameCount();    
    	if(levelOfCurrent > maximumLevel) {
    		maximumLevel = levelOfCurrent;
    	}
    	if(attrs.isDirectory()) {
    		cntDir++;
    		currentlyPointedTo.setSpecialOne(true);
        	currentlyPointedTo.setSpecialTwo(false);
        }           	
    	
    	Node<Path> directParent = searchDirectoryNodeByLevel(--levelOfCurrent, currentlyPointedTo);         	
    	if(directParent == null) {
    		LOGGER.error("Kein Vaterknoten zu " + file + " gefunden, daher Abbruch Traversal!");
    		LOGGER.error("Bitte Überprüfen ...");
    		return FileVisitResult.TERMINATE;
    	}
    	
    	directParent.addChild(currentlyPointedTo);
    	fileStructureTree.add(currentlyPointedTo);        	
    	//LOGGER.debug("found parent: " + directParent	+ " of " + currentlyPointedTo);
             
        return FileVisitResult.CONTINUE; // <=> GENAU WIE SKIP_SUBTREE, WOHL WEIL JA EH NICHT REINKANN
        //return FileVisitResult.SKIP_SUBTREE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    	
    	try {
	    	boolean finishedSearch = Files.isSameFile(dir, startPath);
	        if (finishedSearch) {
	        	LOGGER.info("Traversal beendet!");
	            return FileVisitResult.TERMINATE;
	        }
    		//return FileVisitResult.CONTINUE; // GLEICHES ERGEBNIS WIE BEI FileVisitResult.TERMINATE !
    	}
    	catch(Exception e) {
    		LOGGER.error(Utils.getExceptionInfos(e));
    	}
        return FileVisitResult.CONTINUE;
    }
    
    public Node<Path> getRootNode() {
    	return rootNode;
    }
    
    public int getDepthOfRoot() {
    	return levelOfRoot;
    }
    
    public int getMaximumDepth() {
    	return maximumLevel;
    }
    
    public List<Node<Path>> getFileStructureTree() {
    	return fileStructureTree;
    }
    
    /*
     * Aus historischen Gründen so belassen, aber eig würde oben current.getData().getParent() ausreichen 
     */
    public Node<Path> searchDirectoryNodeByLevel(int level, Node<Path> current) {
    	return fileStructureTree.stream()
    			  .filter(node -> node.getData().getNameCount() == level)
    			  .filter(node -> Files.isDirectory(node.getData())) 
    			  //.findAny() // FALSCH ! LIEFERT IEINEN PARENTORDNER IN HÖHEREM LEVEL, ABER NICHT DIREKTEN !
    			  .filter(node -> node.getData().equals(current.getData().getParent()))
    			  .collect(Collectors.toList())
    			  .get(0);
    }
    
    public List<Node<Path>> getImmediateChildrenByLevel(int level) {
    	return fileStructureTree.stream()
  			  .filter(node -> node.getData().getNameCount() == level)
  			  .collect(Collectors.toList());
    }
    
    /*
     * Gegenüberstellung:
     * Node -- Parent -- Children
     */
    public void getRelationInformations() {    	
    	
    	List<Triplet<Node<Path>, Node<Path>, List<Node<Path>>>> tripletList = 
    		    new ArrayList<Triplet<Node<Path>, Node<Path>, List<Node<Path>>>>();
    	
    	// iterate over nodes in order to inspect relations
    	for(Node<Path> act : getFileStructureTree()) {
    		Node<Path> parent = act.getParent();
    		List<Node<Path>> children = act.getChildren();
    		
    		Triplet<Node<Path>, Node<Path>, List<Node<Path>>> triplet 
    			= Triplet.with(act, parent, children);
    		tripletList.add(triplet);
    	}
    	
    	System.out.println("\nNode, Parent, Children"); 
    	for(Triplet t : tripletList) {
    		System.out.println(t);
    	}
    	System.out.println("");
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
    
    public void reset() {
        startPath = null;
        rootNode = null;
        currentlyPointedTo = null; 
        fileStructureTree = null;
        levelOfRoot = 0;
        levelOfCurrent = 0;
        cntTotal = 0;
        cntDir = 0;
        cntFile = 0;
        cntFailed = 0;                  
        LOGGER.debug("DirectoryTraversal object cleared");
    }
}
