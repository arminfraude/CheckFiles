package comparators;

import java.util.Comparator;

import classes.Data;

public class DataModifyDateComparator implements Comparator<Data> {

	public int compare(Data d1, Data d2) {
        return d1.getLastModifiedTime().compareTo(d2.getLastModifiedTime());
    }
	
}
