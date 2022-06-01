package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilePermission;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.ini4j.Ini;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import classes.Data;
import jna.CLibrary;
import jna.Kernel32;
import listeners.AbortKeyListenerJava8;
import listeners.AbortKeyListenerJava8Pool;
import listeners.AbortKeyListenerJava8Service;
import listeners.AbortKeyListenerJna;
import utils.DirectoryTraversal;
import utils.ThreadAnalyzer;
//import jline.Terminal;
//import jline.console.ConsoleReader;
//import jline.console.KeyMap;
//import jline.console.completer.Completer;
//import jline.console.history.History.Entry;
import utils.Utils;

public class TestMain {
	
	private static final Logger LOGGER = LogManager.getLogger(TestMain.class);
	private static final Scanner SCANNER = new Scanner(System.in);
	private static volatile boolean escCondition = false;
	
	static {		
		// SO NICHT NÖTIG FÜR CMD UND IN ECLIPSE GEHT ANSCHEINEND PER SE NICHT !
		/*String absolutePath = new File(System.getProperty("user.dir") 
				+ Utils.FILE_SEPARATOR
				+ "src"
				+ Utils.FILE_SEPARATOR
				+ "resources")
				.getAbsolutePath();
		System.out.println("absolutePath: " + absolutePath);
		System.out.println("SET jna.library.path ...");
		System.setProperty("jna.library.path", absolutePath);
		*/
	}
	
	private static void checkTest() {
		List<String> str = Arrays.asList("Geeks", "for", "Geeks");	  
	       
		// Convert the character list into String
        // using Collectors.joining() method
        String chString = str.stream()
                              .map(String::valueOf)
                              .collect(Collectors.joining(", ", "{", "}"));
  
        // Print the concatenated String
        System.out.println(chString);
	}		
	
	private static void checkCollector() {
		
		Stream<String> words = Arrays.asList("A", "B", "C", "D").stream();        
        String joinedString = words.collect(Collectors.joining()); //ABCD         
        System.out.println( joinedString );  
        joinedString = words.collect(Collectors.joining(",")); //A,B,C,D         
        System.out.println( joinedString ); 
        joinedString = words.collect(Collectors.joining(",", "{", "}")); //{A,B,C,D}         
        System.out.println( joinedString );
	}
	
	private static void checkPredicate() {
		
		List<String> cities = Arrays.asList("New York", "Tokyo", "New Delhi");
		 
        Predicate<String> predicate = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                // filter cities that start with `N`
                return s.startsWith("N");
            }
        };
 
        cities.stream()
                .filter(predicate)
                .forEach(System.out::println);
	}
	
	private static List<Path> readFilesFromPathAsStream(String start_path_1) {
		
		Path path_one = Paths.get(start_path_1);
		LOGGER.info("path_one: " + path_one + Utils.LINE_SEPARATOR + Utils.LINE_SEPARATOR + Utils.LINE_SEPARATOR);
		Stream<Path> stream = null;
		//List<String> collect = null;
		List<Path> collect2 = null;
		
		//try (Stream<Path> stream = Files.walk(path_one, Integer.MAX_VALUE)) {
	    try {		
	    		stream = Files.walk(path_one, Integer.MAX_VALUE);
	    		/* collect = stream
	    		        .map(String::valueOf)
	    		        .sorted()
	    		        .collect(Collectors.toList());
	        	*/
	        	collect2 = stream
	    		        .sorted()
	    		        .collect(Collectors.toList());
	        	
	        	List<List<String>> selectedFileList = new ArrayList<>();
	        	List<List<String>> dirList = new ArrayList<>();
	        	
	        	//collect.forEach(System.out::println);       
	        	//collect.forEach(name -> System.out.println(name));        	
				
	        	collect2.forEach(p -> 
	        	{	        				
	        		//File[] files = directory.listFiles();
	        		//Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());	     
	        				
	        		List<String> selectedInner = new ArrayList<>();
	        		List<String> dirInner = new ArrayList<>();
//	        		List<FileTime> lastModified = new ArrayList<>();
	        				
	        		String tmp_output = "Datei " + p + Utils.LINE_SEPARATOR;
					try {
						boolean hidden = Files.isHidden(p);
						tmp_output += " hidden: " + hidden + Utils.LINE_SEPARATOR;
										
						boolean dir = Files.isDirectory(path_one);
						tmp_output += " directory: " + dir + Utils.LINE_SEPARATOR;
						if(dir) {
							dirInner.add(p.toString());									
						}
						else {
							selectedInner.add(p.toString());
						}
								
						boolean readable = Files.isReadable(p);
						tmp_output += " readable: " + readable + Utils.LINE_SEPARATOR;
							
						boolean regular = Files.isRegularFile(p);
						tmp_output += " regular: " + regular + Utils.LINE_SEPARATOR;
								
						boolean symbolicLink = Files.isSymbolicLink(p);
						tmp_output += " symbolicLink: " + symbolicLink + Utils.LINE_SEPARATOR;
								
						boolean executable = Files.isExecutable(p);
						tmp_output += " executable: " + executable + Utils.LINE_SEPARATOR;	
								
						FileTime lastModifiedTime = Files.getLastModifiedTime(p);
						tmp_output += " lastModifiedTime: " + lastModifiedTime + Utils.LINE_SEPARATOR;
						if(dir) {
							dirInner.add(lastModifiedTime.toString());
									
						}
						else {
							selectedInner.add(lastModifiedTime.toString());
						}								
								
						tmp_output += "-----------------------------------------------" + Utils.LINE_SEPARATOR;								
						LOGGER.info(tmp_output);
								
						dirList.add(dirInner);
						selectedFileList.add(selectedInner);
								
								
					} catch (IOException e) {
						e.printStackTrace();
						LOGGER.error("IOException reading: " + Utils.LINE_SEPARATOR 
								+ Utils.getExceptionInfos(e));
					}
				});	        	
	        	
		}
		catch (Exception e) {
			LOGGER.error(Utils.getExceptionInfos(e));
		}
	    finally {
	    	stream.close();
	    }
	    return collect2;
		
	}
	
	private static void sortByLastModifiedTimeAndPrint1(List<Path> collect) {
		
		Collections.sort(collect, (p1, p2) -> {		        		
			try {
				return (-1)*Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
			} 
			catch (IOException e) {
				LOGGER.error("IOException comparing: " + Utils.LINE_SEPARATOR
						+ Utils.getExceptionInfos(e));
			}
			return 0;
		});
		collect.forEach(s -> {
	    	try {
				LOGGER.info(s + " -- " + Files.getLastModifiedTime(s));
			} catch (IOException e) {
				LOGGER.error("Exception writing: " + Utils.LINE_SEPARATOR
						+ Utils.getExceptionInfos(e));
			}
		});
	}
	
	private static void sortByLastModifiedTimeAndPrint2(List<Path> collect) {
		
		List<Path> sortedList = collect.stream()
	            .sorted(Collections.reverseOrder(Comparator.comparingLong(s -> {
					try {
						return Files.getLastModifiedTime(s).toMillis();
					} catch (IOException e) {
						LOGGER.error("Exception converting LastModifiedTime: " + Utils.LINE_SEPARATOR
								+ Utils.getExceptionInfos(e));
					}
					return 0L;
				})))
	            .collect(Collectors.toList());
	
		sortedList.forEach(s -> {
			try {
				LOGGER.info(s + " -- " + Files.getLastModifiedTime(s));
			} catch (IOException e) {
				LOGGER.error("Exception writing: " + Utils.LINE_SEPARATOR
						+ Utils.getExceptionInfos(e));
			}
		});
	}


	private static List<String> readAllLinesFromFile(String path) {

		List<String> resultList = new ArrayList<>();
		
		try {			
		    FileReader fileReader = new FileReader(path);
		    BufferedReader bufferedReader = new BufferedReader(fileReader);
		    String line = null;
		    while( (line = bufferedReader.readLine()) != null) {
		    	
		    	if(line.trim().length() > 0) 
		    		resultList.add(line);
		    	else
		    		LOGGER.warn("Empty non null line reading CSV");
		    }
		    bufferedReader.close();
		}
		catch(Exception e) {
			LOGGER.error("Exception reading CSV: " + Utils.LINE_SEPARATOR
					+ Utils.getExceptionInfos(e));
		}
		
	    return resultList;

	}	
	
	private static String writeToCSVFromPathList(String path, List<Path> pathList, boolean append) throws IOException {		
		
		List<String> collect = pathList.stream().map(x -> {
			try {
				return x.toAbsolutePath() + ";" + Files.getLastModifiedTime(x) + "\n";
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error("IOException reading: " + Utils.LINE_SEPARATOR
						+ Utils.getExceptionInfos(e));
			}
			return "dummy";			
		}).collect(Collectors.toList());
        System.out.println(collect);         
        
		FileWriter writer = new FileWriter(path, append);	    
		collect.forEach(s -> 
    		{
    			try {
					writer.write(s);							
				} 
    			catch (IOException e) {
					e.printStackTrace();
					LOGGER.error("IOException reading: " + Utils.LINE_SEPARATOR
							+ Utils.getExceptionInfos(e));
				}
		});	    
	    writer.close();		
	    
	    return "success";
	}
	
	
	public static void main(String[] args) throws Exception {
		
		//try {
			/*
			String s = "hello";
			String t = "hello2";
			System.out.println("s: " + s.hashCode());
			System.out.println("t: " + t.hashCode());
			System.out.println(s == t);
			System.out.println(s.equals(t) + Utils.LINE_SEPARATOR);
			
			String f = "hello";
			System.out.println("f: " + f.hashCode());
			System.out.println("f == s: " + f == s);
			System.out.println("f.equals(s): " + f.equals(s) + Utils.LINE_SEPARATOR);
			
			HashSet<String> hm = new HashSet<>();
			hm.add(s);
			hm.add(t);
			hm.add(f);
			System.out.println(hm);
			System.out.println("hm.contains(s): " + hm.contains(s));
			System.out.println("hm.contains(t): " + hm.contains(t));
			System.out.println("hm.contains(f): " + hm.contains(f) + Utils.LINE_SEPARATOR);
			*/
			
			/*
			String s = "C:/Users/Armin/Desktop/Test1/Ebay Sicherheitsfragen 16022020.png";
			File file1 = new File(s);
			String t = "C:/Users/Armin/Desktop/Test1/";
			File file2 = new File(t);
			System.out.println(file1.getAbsolutePath());
			System.out.println(file1.getCanonicalPath());
			System.out.println(file1.getFreeSpace());
			System.out.println(file1.getName());
			System.out.println(file1.getPath());
			System.out.println(file1.getTotalSpace());
			System.out.println(file1.getUsableSpace());
			System.out.println(file1.getParentFile()  + Utils.LINE_SEPARATOR);
			
			String file1Name = file1.getName();
			String file2Name = file2.getName();
			File file3 = new File(file1.getPath());
			String file3Name = file1.getName();
			System.out.println("file1Name: " + file1Name);
			System.out.println("file2Name: " + file2Name);
			System.out.println("file3Name: " + file3Name);
			System.out.println("file1Name.compareTo(file2Name): " + file1Name.compareTo(file2Name));
			System.out.println("file2Name.compareTo(file3Name): " + file2Name.compareTo(file3Name));
			System.out.println("file3Name.compareTo(file1Name): " + file3Name.compareTo(file1Name));
			System.out.println(Utils.LINE_SEPARATOR);
			
			Data data1 = new Data(file1.getPath(), "01.01.1970T00:00:00.000Z");
			Data data2 = new Data(file2.getPath(), "01.01.1970T00:00:00.000Z");
			Data data3 = new Data(file3.getPath(), "01.01.1970T00:00:00.000Z");
			System.out.println("data1: " + data1);
			System.out.println("data1.getPath(): " + data1.getPath());
			
			System.out.println(Utils.LINE_SEPARATOR);
			String dat = "C:/Users/Armin/Desktop/test";
			System.out.println(FilenameUtils.getExtension(s)); 
			System.out.println(FilenameUtils.getExtension(t) == "");
			System.out.println(FilenameUtils.getExtension(dat)); 
			System.out.println(FilenameUtils.getExtension(dat) == null);
			System.out.println(FilenameUtils.getExtension(dat) == "");
			String dat2 = "C:/Users/Armin/Desktop/test.csv";
			System.out.println(FilenameUtils.getExtension(dat2));
			
			System.out.println(Utils.LINE_SEPARATOR);
			String h = null;
			//File f3 = new File(h);
			//System.out.println(f3.isDirectory());
			  */
			
			/*				
			AbortKeyListenerAlternative abortKeyListener = new Alternative();
			abortKeyListener.submit();
			LOGGER.info("Thread.currentThread(): " + Thread.currentThread().getName());
			LOGGER.info("Thread.activeCount(): " + Thread.activeCount());		
			LOGGER.info("abortKeyListener: " + abortKeyListener);	
			
			synchronized(abortKeyListener) {
				LOGGER.info("Call to wait on " + Thread.currentThread().getName() + " ...");
				abortKeyListener.wait();
			}
			
			LOGGER.info("Future done? " + abortKeyListener.getIfFutureIsDone());
			if(abortKeyListener.getIfFutureIsDone()) {
				LOGGER.info("Future: " + abortKeyListener.getFutureValue());
			}	
			
			
			Boolean done = abortKeyListener.getIfFutureIsDone();		
			for(escCondition = false; !escCondition;) {			
				if(done) {
					LOGGER.info("Future done - check value... ");
					String value = abortKeyListener.getFutureValue();
					if(value != null) {
						LOGGER.info("value true !");
						escCondition = true;
						LOGGER.info("Abort ...");
					}
					else {
						LOGGER.info("value false, continue ... ");
					}
				}
			}
			Thread.sleep(15000);
			LOGGER.info("abortKeyListener.getFuture().isCancelled(): " + abortKeyListener.getFuture().isCancelled());
			LOGGER.info("abortKeyListener.getExecutorService().isTerminated(): " + abortKeyListener.getExecutorService().isTerminated());
			LOGGER.info("abortKeyListener.getExecutorService().isShutdown(): " + abortKeyListener.getExecutorService().isShutdown());
			
			
			//LOGGER.info("ShutdownHard ... ");		
			//abortKeyListener.shutdownHard();
			
			//LOGGER.info("Cancel Future ... ");	
			//abortKeyListener.getFuture().cancel(true);
			
			Thread.sleep(15000);
			
			LOGGER.info("ShutdownSoft ... ");		
			abortKeyListener.shutdownSoft();		
			
			LOGGER.info("abortKeyListener.getFuture().isCancelled(): " + abortKeyListener.getFuture().isCancelled());
			LOGGER.info("abortKeyListener.getExecutorService().isTerminated(): " + abortKeyListener.getExecutorService().isTerminated());
			LOGGER.info("abortKeyListener.getExecutorService().isShutdown(): " + abortKeyListener.getExecutorService().isShutdown());
		*/
			
			/* EIGENTLICH GEHT AN NUTZEN VORBEI, ANSCHEINEND KEIN SINAL FüR ESC VORHANDEN
			 * ... UND GEHT AUCH NICHT IN ECLIPSE
			Signal.handle(new Signal("ESC"), new SignalHandler () {
			      public void handle(Signal sig) {
			        System.out.println(
			          "Aaarggh, a user is trying to interrupt me!!");
			        System.out.println(
			          "(throw garlic at user, say `shoo, go away')");
			      }
			    });
			    for(int i=0; i<100; i++) {
			      Thread.sleep(1000);
			      System.out.print('.');
			    }
			    System.exit(0);
			*/
		
			
			//String INI_FILE = "C:/Users/Armin/eclipse-workspace/CheckFiles/config.ini";
			/*ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream inputStream = classloader.getResourceAsStream(INI_FILE);
			Wini ini = new Wini(inputStream);		
			String s = ini.get("log_name_prefix", "prefix", String.class);
			System.out.println(s);
			*/
			
			//Get file from resources folder
	       /* ClassLoader classloader = (new TestMain()).getClass().getClassLoader();
			String INI_FILE = "src/resources/config.ini";
			File f = new File(INI_FILE);
			System.out.println(f.exists());
			System.out.println(f.canRead());
			System.out.println(f.getAbsolutePath());
			System.out.println(f.getCanonicalPath());
			System.out.println(f.getName());
					
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
	        System.out.println(classloader.getName());
	        System.out.println(classloader.getDefinedPackages().length);
	        for (Package p: classloader.getDefinedPackages())
	        	System.out.println(p.getName());
	        
	        System.out.println(classloader.getParent().getName());
	        //InputStream stream = classloader.getResourceAsStream(INI_FILE);
	        InputStream stream = classloader.getResourceAsStream(f.getAbsolutePath());
	        System.out.println(stream.available());
	        System.out.println(stream.toString()); 
	        				
			Ini ini = new Ini(new File("src/resources/config.ini"));
			System.out.println(ini.get("start_path_default", "start_path"));
	        */

		
			//Utils.deleteLogFiles(); 
			
			//Ini ini  = Utils.getIni();
			
			
			/* FOLGENDES MIT CONSOLE READER NUR FÜR jline2
			 * jline2 TESTS START	--------------------------> 
			//ConsoleReader consoleReader = new ConsoleReader(System.in, System.out, terminal);
			ConsoleReader consoleReader = new ConsoleReader(System.in, System.out);
//			WindowsTerminal terminal = new WindowsTerminal();
			Terminal terminal = consoleReader.getTerminal();		
			terminal.init();
			LOGGER.info("terminal.isSupported: " + terminal.isSupported());
			terminal.setEchoEnabled(true);				
			consoleReader.setBellEnabled(true);
			
			//LOGGER.info("beep...");
			//consoleReader.beep(); // GEHT NICHT IN ECLIPSE ABER AUF CMD !
			
			//LOGGER.info("getDirectConsole: " + terminal.getDirectConsole());
			LOGGER.info("getHeight: " + terminal.getHeight());
			LOGGER.info("getWidth: " + terminal.getWidth());
			LOGGER.info("getOutputEncoding: " + terminal.getOutputEncoding());
			
			//LOGGER.info("reset... ");
			//terminal.reset();
			//LOGGER.info("restore... ");
			//terminal.restore();		
			
			LOGGER.info("beep...");
			consoleReader.beep();
			LOGGER.info("drawLine...");
			consoleReader.drawLine();
			
			LOGGER.info("getAutoprintThreshold: " + consoleReader.getAutoprintThreshold());
			LOGGER.info("getCommentBegin: " + consoleReader.getCommentBegin() );
			
			Collection<Completer> lc = consoleReader.getCompleters();
			LOGGER.info("CompleterListSize: " + lc.size());
			for (Completer s : lc) {
				LOGGER.info("Completer: " + s.toString());
		    }
			
			/*
			 * DIES HIER IST QUASI readLine() UND WARTET AUF USER EINGABE IN CONSOLE
			 * ... JEDOCH WIRD NIE BEENDET UND WARTET IMMER AUF EINGABE, UND WENN GETIPPT
			 * ... ABER NICHT SICHTBAR IN CONSOLE JEDOCH NACH ENTER DANN SIEHT MAN WIE ECHO UND WIEDER VON VORNE 
			 * ALSO ANSCHEINEND DIE VERBINDUNG MIT System.in/out KORREKT
			 * ... ES WIRD HALT NICHT GHLEICH SICHTBAR, ERST NACH ENTER
			 * 		 
			 
			InputStream inputStream = consoleReader.getInput();
			Scanner s = new Scanner(inputStream);
			int cnt = 0;
			LOGGER.info("Type Input ...");
			while(s.hasNext() && cnt < 2) {
				LOGGER.info("Input next: " + s.next());
				cnt++;
				LOGGER.info("flush...");
				consoleReader.flush();
			} 
			LOGGER.info("cnt fertig - RESUME also");
			s.close(); 
			
			
					
			LOGGER.info("getPrompt: " + consoleReader.getPrompt());
			LOGGER.info("backspace: " + consoleReader.backspace());		
			LOGGER.info("keyMap: " + consoleReader.getKeyMap());
			
			KeyMap km = consoleReader.getKeys();
			LOGGER.info("km.getName: " + km.getName());
			
			LOGGER.info("KeyMap.keyMaps().entrySet() ... ");
			for (Map.Entry<String, KeyMap> entry : KeyMap.keyMaps().entrySet()) {
			    System.out.println(entry.getKey() + "/" + entry.getValue().getName());
			}
				
			LOGGER.info("print... ");
			consoleReader.print("\"TEST PRINT\"" + Utils.LINE_SEPARATOR);
//			LOGGER.info("flush...");
//			consoleReader.flush();
			
			LOGGER.info("putString... ");
			consoleReader.putString("\" TEST PUTSTRING \"" + Utils.LINE_SEPARATOR);
//			LOGGER.info("flush...");
//			consoleReader.flush();
			
			
			LOGGER.info("FIELDS: ");
			LOGGER.info("--------------------------------------------------");
			Field[] fields = java.awt.event.KeyEvent.class.getDeclaredFields();
			for (Field f : fields) {
			    if (Modifier.isStatic(f.getModifiers())) {
			    	LOGGER.info(f.getName());
			    	consoleReader.flush();
			    } 
			}
			LOGGER.info("--------------------------------------------------");

			

			LOGGER.info("getOutput + bind new Writer and try put text ...");
			Writer stringWriter = consoleReader.getOutput();
			stringWriter.write("TEXT WRITTEN TO stringWriter BOUNT TO consoleReader.getOutput()\n\n");
			stringWriter.flush();
			//consoleReader.flush();
			//stringWriter.close(); // WENN DIES HIER AUSGEFüHRT WIRD NIX MEHR IN CMD ANGEZEIGT, WOHL ABER AUF ESC REAGIERT !!!!!
			LOGGER.info(Utils.LINE_SEPARATOR);
			
			
			
					
			// KLAPPT SO ---> FüR MAIN + ESCAPE DANN !!!!!!!!!!!!!!!!!!!
			LOGGER.info("As Char 27 (ESC): " + (char)27); // <=> keyCode 27 == ESCAPE TASTE !!
			InputStream inputStream2 = consoleReader.getInput();
			Scanner s2 = new Scanner(inputStream2);
			LOGGER.info("Type Input ...");
			LOGGER.info("readCharacter until escape from loop with <ESCAPE>... ");
			while(true) {
				//consoleReader.flush();
				//if(Character.toString(consoleReader.readCharacter()).matches("C")) {
				if(consoleReader.readCharacter() == 27) {
					LOGGER.info("BREAK FROM LOOP AFTER PRESSING <ESCAPE>... ");
					break;
				}

			}
			s2.close();

		
			LOGGER.info("redrawLine ... ");
			consoleReader.redrawLine();
			
			LOGGER.info("searchBackwards(s) ");
			LOGGER.info(consoleReader.searchBackwards("s"));
			
			//LOGGER.info("searchForwards(s) ");
			//consoleReader.searchForwards("s");
			
			
			
			LOGGER.info("yankPop ");
			consoleReader.yankPop();
			
			LOGGER.info("yank ");
			consoleReader.yank();
			
			
			LOGGER.info("flush...");
			consoleReader.flush();
			
			
			//LOGGER.info("setCursorPosition(55) ");
			//consoleReader.setCursorPosition(55);
			
			//LOGGER.info("moveCursor 100 ... ");
			//consoleReader.moveCursor(100);
			
			//LOGGER.info("println... ");
			//consoleReader.println();	-> NEWLINE
			
			//LOGGER.info("paste: " + consoleReader.paste());
			LOGGER.info("paste ...");
			consoleReader.paste(); // AUSGABE DES GERADE VORHANDENEN AUS CLIPBOARD!!!
							
			
			stringWriter.close();
			consoleReader.close();	
			
			}
			catch(Exception e) {
				LOGGER.error(Utils.getExceptionInfos(e));
			}
		 <-------------------------- jline2 TEST ENDE */	

			
			
			
			/*CLibrary.INSTANCE.printf("Hello, World\n");
	        for (int i=0;i < args.length;i++) {
	            CLibrary.INSTANCE.printf("Argument %d: %s\n", i, args[i]);
	        }	        
	        CLibrary.INSTANCE.printf("Erg cosh: %d", CLibrary.INSTANCE.cosh(4.0));
	        CLibrary.INSTANCE.printf("\nErg atol: %s", CLibrary.INSTANCE.atol("hello"));
	        */
			
	        /*Kernel32 lib = Kernel32.INSTANCE;
	        Kernel32.SYSTEMTIME time = new Kernel32.SYSTEMTIME();
	        lib.GetSystemTime(time);
	        LOGGER.info("Current time (UTC): " + time.wHour + ":"+ time.wMinute + ":" +
	        		+ time.wSecond + "."+ time.wMilliseconds);*/
	        
	       // LOGGER.info(System.getProperties());
	      //  LOGGER.info("java.library.path: " + System.getProperty("java.library.path"));	        
	     //   LOGGER.info("org.jline.terminal.type: " + System.getProperty("org.jline.terminal.type"));
	        
	        
	        /*
	        Terminal TERMINAL = TerminalBuilder.builder()
	        		.streams(System.in, System.out) 
		            .system(true)
		            .dumb(false)
		            .encoding(Charset.forName("UTF-8"))
		            .name("Terminal")
		            .jna(true)
		            .jansi(false)
		            .build();
			NonBlockingReader NON_BLOCKING_READER = TERMINAL.reader();
			PrintWriter WRITER = TERMINAL.writer();
			
			while(true) {
				WRITER.write("PLEASE INPUT...");
				
				if(NON_BLOCKING_READER.read() == 27) {
					LOGGER.info(Utils.LINE_SEPARATOR);
					LOGGER.info("BREAK FROM LOOP AFTER PRESSING <ESCAPE>... ");
					break;
				}				
				else {
					char input = (char) NON_BLOCKING_READER.read(); 
					LOGGER.info("Daemon read: " + input); 
				}
			}
			NON_BLOCKING_READER.shutdown();
			TERMINAL.close();
			*/

		/*	CLibrary clib = CLibrary.INSTANCE;
			boolean run = true;
			while(run) {
				LOGGER.info("Bitte Zeichen eingeben: ");
				int read = clib._getwch();
				LOGGER.info("read with _getwch: " + read);
				LOGGER.info("read with _getwch conv: " + (char)read);
				
				LOGGER.info("Bitte Zeile: ");				
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		        String name = reader.readLine();
		        LOGGER.info("gelesen: " + name);
				
				
				if(read == Utils.ESCAPE_CODE) {
					LOGGER.info("Break loop after ESCAPE...");
					break;
				}
			}
	        
	  	}
		 
		*/
			
			/*ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> {
			    String threadName = Thread.currentThread().getName();
			    System.out.println("Hello " + threadName);
			});*/
			
			
			
		/*	ExecutorService executor = Executors.newWorkStealingPool();

			List<Callable<String>> callables = Arrays.asList(
			        () -> "task1",
			        () -> "task2",
			        () -> "task3");

			executor.invokeAll(callables)
			    .stream()
			    .map(future -> {
			        try {
			        	String s = future.get();
			            return s;
			        }
			        catch (Exception e) {
			            throw new IllegalStateException(e);
			        }
			    })
			    //.forEach(System.out::println);
			    .forEach(s -> LOGGER.info(s));
			
			executor.shutdownNow(); */
			
			
			
			/*
			ScheduledExecutorService scExecutor = Executors.newScheduledThreadPool(1);

			Runnable task = () -> System.out.println("Scheduling: " + System.nanoTime());
			ScheduledFuture<?> future = scExecutor.schedule(task, 3, TimeUnit.SECONDS);

			TimeUnit.MILLISECONDS.sleep(1337);

			long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
			System.out.printf("Remaining Delay: %sms", remainingDelay);
			System.out.println("\n");
			scExecutor.shutdownNow(); 
			*/
			
		
		
//		String[] s = "lll.gggg".split("\\.");
//		return;
		
		/*LOGGER.info(FilenameUtils.getExtension("test.csv"));
		LOGGER.info(FilenameUtils.getExtension("test")==null);
		LOGGER.info(FilenameUtils.getExtension("test").isBlank());
		//LOGGER.info(FilenameUtils.getExtension(null).equals("csv"));
		LOGGER.info(FilenameUtils.getExtension("test.csv").equals("csv"));
		LOGGER.info(FilenameUtils.getExtension("test.csv").equals(".csv"));
		LOGGER.info(FilenameUtils.getExtension("test.exe").equals("csv"));
		LOGGER.info(FilenameUtils.getExtension("").equals("csv"));
		LOGGER.info(FilenameUtils.getExtension("test").equals("csv"));
		LOGGER.info(Utils.LINE_SEPARATOR);*/
		
//		LOGGER.info(Utils.isValidFileName("test.csv", "csv"));
//		LOGGER.info(Utils.isValidFileName("test.cse", "csv"));
//		LOGGER.info(Utils.isValidFileName("test.exe", "csv"));
//		LOGGER.info(Utils.isValidFileName("test", "csv"));
//		LOGGER.info(Utils.isValidFileName(null, "csv"));
//		LOGGER.info(Utils.isValidFileName("test", null));
//		LOGGER.info(Utils.isValidFileName("test", ""));
//		LOGGER.info(Utils.isValidFileName("test", "    "));
		
//		 File f = new File("C:/");
//		 LOGGER.info(f.isDirectory());
//		 LOGGER.info(f.canWrite());	 

//		 AccessController.checkPermission(new FilePermission("/", "read,write"));
		 
//		 FilePermission fp = new FilePermission("C:/Users/nikos7/Desktop/output.txt", "read");
//		 try {
//			 AccessController.checkPermission(fp);		 
//		 } 
//		 catch (AccessControlException ex) {
//			 LOGGER.info("Access denied");
//		 }
		
//		FilePermission fp = new FilePermission("C:\\Users\\Armin\\eclipse-workspace\\CheckFiles\\*", "read");
//		 try {
//			 AccessController.checkPermission(fp);		 
//		 } 
//		 catch (AccessControlException ex) {
//			 LOGGER.info("Access denied");
//		 }
		 
		
	/*	try {
			 Path p = Paths.get("C:/");
			 File f = p.toFile();
			 System.out.println(p.toString());
			 System.out.println(Files.isWritable(p)); 
			 System.out.println(f.canWrite());
			 
			 
			 LOGGER.trace("LOGGER.trace");
			 LOGGER.debug("LOGGER.debug");
			 LOGGER.info("LOGGER.info");
			 LOGGER.warn("LOGGER.warn");
			 LOGGER.error("LOGGER.error");
			 System.out.println("System.out.println: äüß"); // TEST
			 LOGGER.error("LOGGER.error: äüß"); // TEST	
			 
			 
			 File f2 = new File("C:/t/");
			 System.out.println(f2.canWrite());
			 
			 LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			 Configuration config = ctx.getConfiguration();
			 System.out.println("config.getAppender(\"FileAppender\"): " + config.getAppender("FileAppender"));
			 System.out.println("config.getAppender(\"Console\"): " + config.getAppender("Console"));
				
			 System.out.println("config.getLoggerConfig(\"FileAppender\"): " + config.getLoggerConfig("FileAppender"));
			 System.out.println("config.getLoggerConfig(\"Console\"): " + config.getLoggerConfig("Console"));
			 			
			 FileAppender fileAppender = (FileAppender) config.getAppender("FileAppender");
			 System.out.println(fileAppender);
				
			 
			 // WENN NICHT IN try catch finally FOLGT PROGRAMMABBRUCH !!!!!
			 assert f2.canWrite()==true : "NICHT TRUE";
		}
		catch(java.lang.AssertionError e) {
			LOGGER.error("Reaching catch block of: " + "\n"
					+ e.getMessage() + "\n" 
					+ e.getCause() + "\n"
					+ e.getSuppressed().toString()); 
		}
		finally {
			Utils.deleteLogFiles();
		} */
		
		
		//Stream<Path> stream = null;
		//List<Data> dataList = null;
		try {
			
			/*Predicate<Path> predicate = isReadablePath();
			Stream<Path> str = Files.walk(Paths.get(start_path))
				.filter(predicate);
			System.out.println(str.collect(Collectors.toList()));
			*/			
						
			/*
			stream = Files.walk(path_one, Integer.MAX_VALUE); // HIER FEHLER
			dataList = stream
				.filter(s -> Files.isReadable(s))
				.map(s -> 
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
			
			for(Data d : dataList)
				System.out.println(d);
			*/
			
			String startPathStr = "C:/";
			Path startPath = Paths.get(startPathStr);
			LOGGER.info("isReadable: " + Files.isReadable(startPath));
			LOGGER.info("canRead: " + new File(startPathStr).canRead() + "\n");
			
			DirectoryTraversal traversal = new DirectoryTraversal(startPath);
			Files.walkFileTree(startPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, traversal);
			LOGGER.info(Utils.LINE_SEPARATOR 
					+ "Visited totally: " + traversal.getCntTotal() + "\n"
					+ "Failed to visit: " + traversal.getCntFailed());
			
			
		}
		catch(Exception e) {
			LOGGER.error(ExceptionUtils.getStackTrace(e));
		}		
		
	}
	
	public static Predicate<Path> isReadablePath() {
	    return p -> Files.isReadable(p);
	}
}
