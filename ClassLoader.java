package soot.jimple.toolkits.javaee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A simple fake class loader.
 * 
 * @author Bernhard Berger
 */
public class ClassLoader {

	private String path;

	public ClassLoader(final String path) {
		this.path = path;
	}

	public InputStream getInputStream(final String path) throws FileNotFoundException {
		return new FileInputStream(this.path + File.separator + path);
	}
}
