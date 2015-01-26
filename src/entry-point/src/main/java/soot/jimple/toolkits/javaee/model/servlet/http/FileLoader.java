/*
   This file is part of Soot entry point creator.

   Soot entry point creator is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Soot entry point creator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Soot entry point creator.  If not, see
<http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
   Copyright 2015 Ecole Polytechnique de Montreal & Tata Consultancy Services
*/
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
 * @author Marc-André Laverdière-Papineau
 */
public class FileLoader {
	/**
	 * Logger.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(FileLoader.class);

    private File basePath;

	public FileLoader() {
	}
	
	public FileLoader(final String path) {
        this.basePath = new File(path);
	}

    public FileLoader(final File path) {
        this.basePath = path;
    }

	public InputStream getInputStream(final String path)
			throws FileNotFoundException {
		final File file = new File(this.basePath,path);
		
		LOG.warn("Searching for file {}. Exists? {} ", file, file.exists());
		
		return new FileInputStream(file);
	}
	
	@XmlAttribute(name="basepath", required=true)
	public String getBasepath() {
		return basePath.getAbsolutePath();
	}
	
	public void setBasepath(final String path) {
		this.basePath = new File(path);
	}
	
	@Override
	public String toString() {
		return "[FileLoader " + basePath.getAbsolutePath() + "]";
	}
}
