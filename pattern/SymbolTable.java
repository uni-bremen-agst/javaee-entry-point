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
package soot.jimple.toolkits.transformation.pattern;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Value;

/**
 * The symbol table binds wildcards of a pattern to the values found in a matching Jimple. This is necessary
 *   to check if the same wildcard name is bound to the same value in Jimple.
 * 
 * @author Bernhard Berger
 */
public class SymbolTable {
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(SymbolTable.class);
	
	/**
	 * Stores all bindings.
	 */
	private Map<String, Value> bindings = new HashMap<String, Value>();

	/**
	 * @return Iff there is a binding for {@code wildcardName}.
	 */
	public boolean contains(final String wildcardName) {
		return bindings.containsKey(wildcardName);
	}

	/**
	 * @return The binding for {@code wildcardName} or {@code null}.
	 */
	public Value get(final String wildcardName) {
		return bindings.get(wildcardName);
	}

	/**
	 * Binds {@code wildcardName} to {@code jimpleValue}.
	 */
	public void bind(final String wildcardName, final Value jimpleValue) {
		LOG.debug("Binding {} to {}.", wildcardName, jimpleValue);
		bindings.put(wildcardName, jimpleValue);
	}
}
