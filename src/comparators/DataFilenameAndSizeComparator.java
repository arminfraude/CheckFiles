package comparators;

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import classes.Data;

/*
 * Im Grunde Zweck hier (ertstmal) nur für Prüfung auf Gleichheit gebraucht
 */
public class DataFilenameAndSizeComparator implements Comparator<Data> {
	
	private static final Logger LOGGER = LogManager.getLogger(DataFilenameAndSizeComparator.class);
	private Comparator<Data> dataFilenameComparator;

    public DataFilenameAndSizeComparator(Comparator<Data> one) {
        this.dataFilenameComparator = one;
    }
	
	@Override
	public int compare(Data d1, Data d2) {
		// make a first comparison using comparator one
        int comparisonByOne = dataFilenameComparator.compare(d1, d2);

        // check if it was 0 (items equal in that attribute)
        if (comparisonByOne == 0) {
            // if yes, return the result of the next comparison
        	if(d1.getSizeInByte() != null && d2.getSizeInByte() != null) {
        		return Long.compare(d1.getSizeInByte(), d2.getSizeInByte());
        	}
        	else {
        		LOGGER.error(d1.getPath() + " konnte nicht mit " + d2.getPath() 
        				+ " bzgl Dateigrösse verglichen werden, da mindestens "
        				+ "1 Datei davon keine gesetzte Grösse hat\"");
        		
        		return -1; // Gleichheit kann also nicht komplett bestimmt werden, daher
        					// willkürliche Bestimmung eines Fluchtwerts
        	}

        } else {
            // otherwise return the result of the first comparison
            return comparisonByOne;
        }
	}
}
