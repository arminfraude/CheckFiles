package classes;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utils.Utils;

public class Dummy implements Cloneable {
	
	private static final Logger LOGGER = LogManager.getLogger(Dummy.class);	
	private String x = "iniX"; 
	private String y = "iniY";
	
	public Dummy() {
		
	}
	
	public Dummy(String s, String t) {
		x = s;
		y = t;
	}
	
	public String getX() {
		return x;
	}
	
	public void setX(String x) {
		this.x = x;
	}
	
	public String getY() {
		return y;
	}
	
	public void setY(String y) {
		this.y = y;
	}
	
	@Override
	public String toString() {		
		return x + " / " + y;
	}
	
	@Override
    protected Object clone() throws CloneNotSupportedException {
		
		// REICHT SO ALSO FüR DEEP COPY??
		// A: JA -> WOHL WEIL KEINE WEITEREN OBJEKTE ALS INSTANZVARIABLEN ....
		return (Dummy)super.clone();  
    }
	
	@Override
	public int hashCode() {
        int hash = 7;        
        hash = 31 * hash + (x.toString() == null ? 0: x.hashCode());
        hash = 31 * hash + (y.toString() == null ? 0: y.hashCode());
        return hash;
    }
	
	@Override
	public boolean equals(Object o) {
	    // self check
	    if (this == o)
	        return true;
	    // null check
	    if (o == null)
	        return false;
	    // type check and cast
	    if (getClass() != o.getClass())
	        return false;
	    Dummy data = (Dummy) o;
	    // field comparison
	    return Objects.equals(x, data.x)
	            && Objects.equals(y, data.y);
	}
	
}
