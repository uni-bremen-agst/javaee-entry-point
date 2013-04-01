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
