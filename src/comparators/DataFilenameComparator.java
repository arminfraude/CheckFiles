package comparators;

import java.io.File;
import java.util.Comparator;

import classes.Data;

public class DataFilenameComparator implements Comparator<Data> {
	
	@Override
	public int compare(Data d1, Data d2) {
		File f1 = new File(d1.getPath());
		File f2 = new File(d2.getPath());		
		return (f1.getName()).compareTo(f2.getName());
	}

}
