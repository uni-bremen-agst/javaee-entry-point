package soot.jimple.toolkits.javaee;

import soot.SootMethod;

/**
 * Interface that just contains some signatures and subsignatures.
 *
 * @author Bernhard Berger
 */
public interface Signatures {

	/**
	 * Subsignature of an empty constructor.
	 */
	public static final String EMPTY_CONSTRUCTOR_SUBSIGNATURE = "void " + SootMethod.constructorName + "()";
	
	/**
	 * Class name of HttpServlet.
	 */
	public static final String HTTP_SERVLET_CLASS_NAME = "javax.servlet.http.HttpServlet";
	
	/**
	 * Class name of {@code javax.servlet.http.HttpServletRequest}.
	 */
	public static final String HTTP_SERVLET_REQUEST_CLASS_NAME = "javax.servlet.http.HttpServletRequest";
	
	/**
	 * Class name of {@code javax.servlet.http.HttpServletResponse}.
	 */
	public static final String HTTP_SERVLET_RESPONSE_CLASS_NAME = "javax.servlet.http.HttpServletResponse";
	
	/**
	 * Class name of {@code java.lang.Object}.
	 */
	public static final String OBJECT_CLASS_NAME = "java.lang.Object";
	
	/**
	 * Class name of {@code java.util.Random}.
	 */
	public static final String RANDOM_CLASS_NAME = "java.util.Random";
	
	/**
	 * Class name of {@code java.lang.String}.
	 */
	public static final String STRING_CLASS_NAME = "java.lang.String";

}
