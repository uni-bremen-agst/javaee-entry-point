package soot.jimple.toolkits.javaee;

import java.io.PrintStream;
import java.util.Map;

import soot.G;
import soot.SootMethod;

/**
 * Logging abstraction for soot.
 * 
 * @author Bernhard Berger
 */
public class SootLogger {
	/**
	 * Name of current phase.
	 */
	private String phase = "";
	
	/**
	 * Set the name of the current phase.
	 * 
	 * @param phase Name of phase.
	 */
	public void setPhase(final String phase) {
		this.phase = phase;
		
		setPrefix();
	}
	
	public void setOptions(final @SuppressWarnings("rawtypes") Map options) {
		
	}
	
	/**
	 * Current method name.
	 */
	private String method = "";
	
	/**
	 * Set the current method name.
	 * 
	 * @param method Current method.
	 */
	public void setMethod(final SootMethod method) {
		this.method = method.getSubSignature();
		
		setPrefix();
	}
	
	/**
	 * Current prefix
	 */
	private String prefix = "";

	/**
	 * Derives the current prefix from {@link phase} and {@link method}.
	 */
	private void setPrefix() {
		prefix = "[" + method + " - " + phase + "] ";
	}
	
	/**
	 * The output stream.
	 */
	private final static PrintStream out = G.v().out;
	
	/**
	 * Logs an info message.
	 * 
	 * @param message The message to log.
	 */
	public void info(final String message) {
		out.print(prefix);
		out.print("INFO  - ");
		out.println(message);
	}

	/**
	 * Logs an debug message.
	 * 
	 * @param message The message to log.
	 */
	public void debug(final String message) {
		out.print(prefix);
		out.print("DEBUG - ");
		out.println(message);
	}

	/**
	 * Logs an warning.
	 * 
	 * @param message The message to log.
	 */
	public void warn(final String message) {
		out.print(prefix);
		out.print("WARN  - ");
		out.println(message);	}

}
