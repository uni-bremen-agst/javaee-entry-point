package soot.jimple.toolkits.javaee.model.servlet.http;

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
	private String basepath;

	public FileLoader() {
	}
	
	public FileLoader(final String path) {
		this.basepath = path;
	}

	public InputStream getInputStream(final String path)
			throws FileNotFoundException {
		return new FileInputStream(this.basepath + File.separator + path);
	}
	
	@XmlAttribute(name="basepath", required=true)
	public String getBasepath() {
		return basepath;
	}
	
	public void setBasepath(final String path) {
		this.basepath = path;
	}
}
