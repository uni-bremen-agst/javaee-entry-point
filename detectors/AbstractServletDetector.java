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

import java.util.Map;

/**
 * An abstract servlet detector with some additional functions.
 * 
 * @author Bernhard Berger
 */
public abstract class AbstractServletDetector implements ServletDetector {
	/**
	 * Phase options for servlet detection.
	 */
	@SuppressWarnings("rawtypes")
	protected Map options;
	
	@Override
	public void setOptions(@SuppressWarnings("rawtypes") final Map options) {
		this.options = options;
	}
}
