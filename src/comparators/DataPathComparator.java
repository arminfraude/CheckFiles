package comparators;

import java.util.Comparator;

import classes.Data;

public class DataPathComparator implements Comparator<Data> {

	public int compare(Data d1, Data d2) {
		// WIE NUN MIT FILENAME VERGLEICH WENN EIN DIR ???
		// DANN IST JA switch case K�SE ??
		// A: getName() LIEFERT NUR LETZTEN TEIL, DEN MAN THEORETISCH VERGLEICHEN K�NNTE
		// GEHT NATIV �BERHAUPT MIT STRING???
		// A: JA: lexicographic ordering ...
		
		// FRAGE ABER NUN WIE HANDELN WENN dir <> file ....
		// A: SOLLTE KEIN PROBLEM BEI SORTIERUNG SEIN, WOHL AUCH UNN�IG NUR DEN name SONDERN SOGAR WICHTIG DEN GANZEN PATH ZU VERGLEICHEN !
        return d1.getPath().compareTo(d2.getPath());
    }
}
