package comparators;

import java.util.Comparator;

import classes.Data;

public class DataPathReverseComparator implements Comparator<Data> {

	public int compare(Data d1, Data d2) {
		return d2.getPath().compareTo(d1.getPath());
    }
}
