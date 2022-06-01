/* https://github.com/java-native-access/jna/blob/master/www/GettingStarted.md */
package jna;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.win32.StdCallLibrary;

//kernel32.dll uses the __stdcall calling convention (check the function
//declaration for "WINAPI" or "PASCAL"), so extend StdCallLibrary
//Most C libraries will just extend com.sun.jna.Library,
public interface Kernel32 extends StdCallLibrary {

	Kernel32 INSTANCE = (Kernel32)
		    Native.load("kernel32", Kernel32.class);
	
		// Optional: wraps every call to the native library in a
		// synchronized block, limiting native calls to one at a time
	Kernel32 SYNC_INSTANCE = (Kernel32)
		    Native.synchronizedLibrary(INSTANCE);
	
	
	/*
	 * Declare methods that mirror the functions in the target library by defining Java methods 
	 * with the same name and argument types as the native function 
	 * (refer to the basic mappings below or the detailed table of type mappings). 
	 * 
	 * You may also need to declare native structures to pass to your native functions. 
	 * To do this, create a class within the interface definition that extends Structure 
	 * and add public fields (which may include arrays or nested structures).
	 */	
	@FieldOrder({ "wYear", "wMonth", "wDayOfWeek", "wDay", "wHour", "wMinute", "wSecond", "wMilliseconds" })
	public static class SYSTEMTIME extends Structure {
	    public short wYear;
	    public short wMonth;
	    public short wDayOfWeek;
	    public short wDay;
	    public short wHour;
	    public short wMinute;
	    public short wSecond;
	    public short wMilliseconds;
	}

	void GetSystemTime(SYSTEMTIME result);
		
}
