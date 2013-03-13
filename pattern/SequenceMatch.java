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

import soot.Body;
import soot.Unit;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.LabelOrStatement;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.SequencePattern;

/**
 * A match for a {@link SequencePattern}.
 * 
 * @author Bernhard Berger
 */
public class SequenceMatch extends Match {
	/**
	 * Log instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(SequenceMatch.class);
	
	/**
	 * Binds each pattern statement to the corresponding jimple statement.
	 */
	private final Map<LabelOrStatement, Unit> bindings = new HashMap<LabelOrStatement, Unit>();
	
	/**
	 * The pattern used to create the match.
	 */
	private final SequencePattern pattern;

	/**
	 * The surrounding jimple body.
	 */
	private Body body;
	
	public SequenceMatch(final SequencePattern pattern) {
		this.pattern = pattern;
	}

	/**
	 * Binds a statement ({@code patternStmt}) of the pattern to a Jimple statement ({@code stmt}).
	 */
	public void bind(final LabelOrStatement patternStmt, final Unit stmt) {
		LOG.info("Binding {} -> {}.", patternStmt, stmt);
		
		bindings.put(patternStmt, stmt);
	}
	
	public Map<LabelOrStatement, Unit> getBindings() {
		return bindings;
	}

	public Body getBody() {
		return body;
	}
	
	@Override
	public SequencePattern getPattern() {
		return pattern;
	}
	
	public void setBody(final Body body) {
		this.body = body;
	}
}
