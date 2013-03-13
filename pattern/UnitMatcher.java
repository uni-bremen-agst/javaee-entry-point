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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.Unit;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.LabelOrStatement;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.SequencePattern;
import soot.util.Chain;

/**
 * Tries to match a pattern in a body. Runtime complexity is {@code O(nm)} where {@code n} is the number of jimple
 *   statements in the method and {@code m} is the number of statements in the pattern.
 * 
 * @author Bernhard Berger
 */
public class UnitMatcher {
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(UnitMatcher.class);

	/**
	 * The sequence pattern we are looking for.
	 */
	private SequencePattern pattern;
	
	/**
	 * Units of the method
	 */
	private Chain<Unit> units;
	
	/**
	 * List of matches found in the method.
	 */
	private LinkedList<Match> matches = new LinkedList<Match>();

	/**
	 * Method body.
	 */
	private Body body;
	
	public UnitMatcher(final SequencePattern pattern, final Body body) {
		this.pattern = pattern;
		this.body = body;
		this.units = body.getUnits();
	}

	/**
	 * Tries to match the pattern for each jimple statement.
	 */
	public void match() {
		if(pattern.getSequence().getStatements().isEmpty()) {
			return;
		}
		
		final ArrayList<Unit> statementList = new ArrayList<Unit>(units);
		final StmtMatchVisitor visitor = new StmtMatchVisitor();
		
		StmtLoop:
		for(int i = 0; i < statementList.size(); i++) {
			final SequenceMatch match = new SequenceMatch(pattern);
			match.setBody(body);
			
			for(int j = 0; j < pattern.getSequence().getStatements().size(); ++j) {
				final Unit stmt = statementList.get(i + j);
				final LabelOrStatement patternStmt = pattern.getSequence().getStatements().get(j);
			
				visitor.setPatternUnit(patternStmt);
				visitor.setSymbolTable(match.getSymbolTable());
				stmt.apply(visitor);

				if((Boolean)visitor.getResult()) {
					match.bind(patternStmt, stmt);
				} else {
					// statement did not match continue with next jimple statement.
					continue StmtLoop;
				}
			}

			LOG.info("Found match {}", match);
			matches.add(match);
		}
	}

	public Collection<? extends Match> getMatches() {
		return matches;
	}
}
