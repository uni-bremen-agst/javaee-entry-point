package soot.jimple.toolkits.javaee.model.servlet.http;


/**
 * Interface that just contains some signatures and subsignatures.
 *
 * @author Bernhard Berger
 */
public interface ServletSignatures {
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
	 * Class name of {@code javax.servlet.GenericServlet}.
	 */
	public static final String GENERIC_SERVLET_CLASS_NAME = "javax.servlet.GenericServlet";
}
