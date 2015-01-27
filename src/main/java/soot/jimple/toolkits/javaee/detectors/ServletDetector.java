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
   along with Soot entry point creator.  If not, see <http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
*/
package soot.jimple.toolkits.javaee.detectors;

import java.util.List;
import java.util.Map;

import soot.jimple.toolkits.javaee.model.servlet.Web;

/**
 * A servlet detector detects and processes framework specific servlets.
 * 
 * @author Bernhard Berger
 */
public interface ServletDetector {
	/**
	 * If you subclass a class belonging to the internal model we have to
	 *   register the classes for serialization. This method is called by the
	 *   framework to do the necessary work.
	 * 
	 * @return A list of classes that extend the internal data model.
	 */
	public List<Class<?>> getModelExtensions();
	
	/**
	 * Sets the phase options.
	 * 
	 * @param options Actual phase options.
	 */
	public void setOptions(@SuppressWarnings("rawtypes") final Map options);
	
	/**
	 * Detects the servlets from source.
	 * 
	 * @param web The web instance where the servlets have to be registered. 
	 */
	public void detectFromSource(final Web web);

	/**
	 * Detects the servlets from config files.
	 * 
	 * @param web The web instance where the servlets have to be registered. 
	 */
	public void detectFromConfig(final Web web);
	
	/**
	 * @return A list of supported check files.
	 */
	public List<String> getCheckFiles();

	/**
	 * @return A list of template files.
	 */
	public List<String> getTemplateFiles();
}
