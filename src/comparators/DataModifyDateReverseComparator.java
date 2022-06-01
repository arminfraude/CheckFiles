package comparators;

import java.util.Comparator;

import classes.Data;

public class DataModifyDateReverseComparator implements Comparator<Data> {
	
	public int compare(Data d1, Data d2) {
        return d2.getLastModifiedTime().compareTo(d1.getLastModifiedTime());
    }
}
