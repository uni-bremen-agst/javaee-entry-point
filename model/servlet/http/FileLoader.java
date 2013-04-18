/**
 * Copyright 2013 Bernhard Berger - Universität Bremen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		
		LOG.info("Searching for file " + file + " exists " + file.exists());
		
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
