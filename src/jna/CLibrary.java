/* https://github.com/java-native-access/jna/blob/master/www/GettingStarted.md */
package jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/** Simple example of JNA interface mapping and usage. 
 * The following example maps the printf function from the standard C library and calls it.
 */
public interface CLibrary extends Library {

	// This is the standard, stable way of mapping, which supports extensive
    // customization and mapping of Java to native types.
	
	
	/*Now, dynamic library names are usually system-dependent, 
	and C standard library is no exception: libc.so in most Linux-based systems, 
	but msvcrt.dll in Windows. 
	
	This is why we've used the Platform helper class, included in JNA, to check which platform 
	we're running in and select the proper library name.

	Notice that we don't have to add the .so or .dll extension, 
	as they're implied. Also, for Linux-based systems,
	we don't need to specify the “lib” prefix that is standard for shared libraries.
	*/
	
	
	CLibrary INSTANCE = (CLibrary)
            Native.load((Platform.isWindows() ? "msvcrt" : "c"),
                                CLibrary.class);

	void printf(String format, Object... args);
	double cosh(double value);	
	int atol(String s);
	int _getwch();
}
