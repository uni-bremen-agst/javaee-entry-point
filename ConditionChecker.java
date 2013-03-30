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
package soot.jimple.toolkits.transformation;

import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardExpression;
import soot.jimple.toolkits.transformation.pattern.SymbolTable;
import soot.jimple.toolkits.transformation.replacer.WildcardVisitor;

/**
 * Checks whether the condition of a transformation rule holds.
 *
 * @author Bernhard Berger
 */
public class ConditionChecker {

	/**
	 * Expression to check.
	 */
	private final WildcardExpression condition;
	
	/**
	 * Symbol table containing binding used in the condition.
	 */
	private final SymbolTable symbolTable;

	public ConditionChecker(final WildcardExpression condition, final SymbolTable symbolTable) {
		this.condition = condition;
		this.symbolTable = symbolTable;
	}

	/**
	 * Checks if the condition evaluates to {@code true}.
	 * @return {@code true} if the condition holds.
	 */
	public boolean holds() {
		final WildcardVisitor visitor = new WildcardVisitor();
		visitor.setSymbolTable(symbolTable);

		Object result = visitor.doSwitch(condition);
		
		if(result instanceof Boolean) {
			return (Boolean) result;
		} else {
			return result != null;
		}
	}

}
