package soot.jimple.toolkits.javaee.model.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A simple fake class loader.
 * 
 * @author Bernhard Berger
 */
public class FileLoader {
	@XmlAttribute(name="path", required=true)
	private String path;

	public FileLoader(final String path) {
		this.path = path;
	}

	public InputStream getInputStream(final String path)
			throws FileNotFoundException {
		return new FileInputStream(this.path + File.separator + path);
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(final String path) {
		this.path = path;
	}
}
