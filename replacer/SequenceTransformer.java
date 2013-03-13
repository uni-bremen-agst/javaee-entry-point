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
package soot.jimple.toolkits.transformation.replacer;

import java.util.Map;

import soot.PatchingChain;
import soot.Unit;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.LabelOrStatement;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.StatementSequence;
import soot.jimple.toolkits.transformation.pattern.SequenceMatch;
import soot.util.Chain;
import soot.util.HashChain;

/**
 * Transforms a sequence of statements.
 * 
 * @author Bernhard Berger
 */
public class SequenceTransformer extends Transformer {

	@Override
	public void transform() {
		final PatchingChain<Unit> units = getMatch().getBody().getUnits();
		
		// create new statements
		final JimpleStmtFactory factory = new JimpleStmtFactory();
		factory.setSymbolTable(getMatch().getSymbolTable());
		factory.setBody(getMatch().getBody());
		
		final Chain<Unit> newStatmements = new HashChain<Unit>();
		for(final LabelOrStatement los : getSequence().getStatements()) {
			newStatmements.add((Unit)factory.doSwitch(los));
		}

		// insert new statements
		final Map<LabelOrStatement, Unit> bindings = getMatch().getBindings();
		units.insertBefore(newStatmements, bindings.get(getMatch().getPattern().getSequence().getStatements().get(0)));
		
		// delete old statements
		for(final Map.Entry<LabelOrStatement, Unit> entry : bindings.entrySet()) {
			units.remove(entry.getValue());
		}
		
	}
	
	/**
	 * @return The sequence of statements in the replacement.
	 */
	private StatementSequence getSequence() {
		return replacement.getSequence();
	}

	private SequenceMatch getMatch() {
		return (SequenceMatch)match;
	}
}
