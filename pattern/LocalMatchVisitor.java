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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.LocalOrWildcard;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardName;

/**
 * Visitor to compare locals.
 * 
 * @author Bernhard Berger
 */
public class LocalMatchVisitor extends AbstractJimpleValueSwitch {
	/**
	 * Logger instance
	 */
	private final static Logger LOG = LoggerFactory.getLogger(LocalMatchVisitor.class);

	/**
	 * Symbol table for binding lookup.
	 */
	private SymbolTable symbolTable;
	
	/**
	 * Local or wildcard on the pattern side.
	 */
	private LocalOrWildcard patternLocal;

	public void setSymbolTable(final SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	/**
	 * Compares a pattern local to a jimple local. If {@code patternLocal} is a wildcard the method
	 *   will check if it is already bound in the symbol table. In this case, it will check if it is
	 *   bound to the same local then the last occurence. If the wildcard is not yet bound the
	 *   wildcard name will be bound to the value found in the jimple local. If {@code patternLocal} is
	 *   a local itself the method will check whether {@code jimpleLocal} has the same name.
	 */
	@Override
	public void caseLocal(final Local local) {
		if(patternLocal instanceof WildcardName) {
			final String name = ((WildcardName)patternLocal).getWName();
			
			if(symbolTable.contains(name)) {
				setResult(symbolTable.get(name).equals(local.getName()));
			} else {
				symbolTable.bind(name, local);
				setResult(true);
			}
		} else {
			setResult(((soot.jimple.toolkits.transformation.dsl.transformationLanguage.Local)patternLocal).getName().equals(local.getName()));
		}
	}

	@Override
	public void caseStringConstant(final StringConstant string) {
		if(patternLocal instanceof WildcardName) {
			final String name = ((WildcardName)patternLocal).getWName();
			
			if(symbolTable.contains(name)) {
				setResult(symbolTable.get(name).equals(string.value));
			} else {
				symbolTable.bind(name, string);
				setResult(true);
			}

		} else {
			setResult(((soot.jimple.toolkits.transformation.dsl.transformationLanguage.StringConstant)patternLocal).getValue().equals(string.value));
		}
	}

	public void setPatternLocal(final LocalOrWildcard patternLocal) {
		this.patternLocal = patternLocal;	
	}
}
