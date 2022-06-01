package test;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyFileVisitor implements FileVisitor<Path>
{
   private static final Logger LOGGER = LogManager.getLogger(MyFileVisitor.class);
   private int fileCount = 0;
   private int dirCount = 0;
   private int failedCount = 0;

   /**
    * Hier ist Path immer ein Directory der Gr��e 0
    */
   @Override
   public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes bfa) throws IOException {
	  LOGGER.info("preVisitDirectory: " + path + " size = " + bfa.size() + " bytes");
      dirCount++;
      return FileVisitResult.CONTINUE;
   }

   /**
    * Hier ist Path immer ein Directory der Gr��e 0
    * ex = null wenn keine Exception aufgetreten ist
    */
   @Override
   public FileVisitResult postVisitDirectory(Path path, IOException ex) throws IOException {
	  LOGGER.info("postVisitDirectory: " + path + " Exception = " + ex);
      return FileVisitResult.CONTINUE;
   }

   /**
    */
   @Override
   public FileVisitResult visitFile(Path path, BasicFileAttributes bfa) throws IOException {
	  LOGGER.info("visitFile: " + path + " size = " + bfa.size() + " bytes");
      fileCount++;
      return FileVisitResult.CONTINUE;
   }

   /**
    */
   @Override
   public FileVisitResult visitFileFailed(Path path, IOException ex) throws IOException {
	  LOGGER.info("visitFileFailed " + " Exception = " + ex);
	  failedCount++;
      return FileVisitResult.CONTINUE;
   }

   public int getFileCount() {
      return fileCount;
   }

   public int getDirCount() {
      return dirCount;
   }
   
   public int getFailedCount() {
	   return failedCount;
   }
}