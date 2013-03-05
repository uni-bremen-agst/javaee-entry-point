package soot.jimple.toolkits.javaee.model.servlet.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.annotation.XmlAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple fake class loader.
 * 
 * TODO Add support for real projects.
 * 
 * @author Bernhard Berger
 */
public class FileLoader {
	/**
	 * Logger.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(FileLoader.class);
	
	private String basepath;

	public FileLoader() {
	}
	
	public FileLoader(final String path) {
		this.basepath = path;
	}

	public InputStream getInputStream(final String path)
			throws FileNotFoundException {
		final File file = new File(this.basepath + File.separator + path);
		
		LOG.info("Searching for file " + file + " exists " + file.exists());
		
		return new FileInputStream(file);
	}
	
	@XmlAttribute(name="basepath", required=true)
	public String getBasepath() {
		return basepath;
	}
	
	public void setBasepath(final String path) {
		this.basepath = path;
	}
	
	@Override
	public String toString() {
		return "[FileLoader " + basepath + "]";
	}
}
