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
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.LocalOrWildcard;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.NonExpr;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.QualifiedName;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.SootMethodRef;
import soot.jimple.toolkits.transformation.utils.TypeHelper;

/**
 * Visitor for invoke expressions. The result (see {@link #getResult()}) will be set to {@code Boolean.TRUE} if
 *   the pattern matches. Otherwise it will return {@code false}.
 * 
 * @author Bernhard Berger
 */
public class ExprMatchVisitor extends AbstractJimpleValueSwitch {
	
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(ExprMatchVisitor.class);

	/**
	 * Symbol table for binding wildcards.
	 */
	private SymbolTable symbolTable;
	
	/**
	 * The pattern we are looking for
	 */
	private soot.jimple.toolkits.transformation.dsl.transformationLanguage.Value patternExpr;
	
	/**
	 * Visitor for comparing locals.
	 */
	private LocalMatchVisitor localVisitor = new LocalMatchVisitor();

	public void setSymbolTable(final SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
		localVisitor.setSymbolTable(symbolTable);
	}

	public void setPatternExpr(final soot.jimple.toolkits.transformation.dsl.transformationLanguage.Value patternExpr) {
		this.patternExpr = patternExpr;
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr expr) {
		if(!(patternExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.InterfaceInvokeExpr)) {
			setResult(false);
			return;
		}

		caseInvokeExpr(expr);
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr expr) {
		if(!(patternExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.SpecialInvokeExpr)) {
			setResult(false);
			return;
		}
		
		caseInvokeExpr(expr);
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr expr) {
		if(!(patternExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.StaticInvokeExpr)) {
			setResult(false);
			return;
		}
		
		caseInvokeExpr(expr);	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr expr) {
		if(!(patternExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.VirtualInvokeExpr)) {
			setResult(false);
			return;
		}
		
		caseInvokeExpr(expr);
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr expr) {
		LOG.warn("Dynamic invoke expressions are not yet implemented");
		setResult(false);
	}
	
	/**
	 * Generic method to compare invoke expressions. The invocation type has already been checked on
	 *   the caller site.
	 * 
	 * @param expr The invoke statement to check.
	 */
	public void caseInvokeExpr(final InvokeExpr expr) {
		if(!(patternExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeExpr)) {
			setResult(false);
			return;
		}

		final soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeExpr patternInvokeExpr = (soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeExpr) patternExpr;
		
		// check base for methods invoked for an instance
		if(expr instanceof InstanceInvokeExpr) {
			final soot.jimple.toolkits.transformation.dsl.transformationLanguage.InstanceInvokeExpr patternInstanceInvokeExpr = (soot.jimple.toolkits.transformation.dsl.transformationLanguage.InstanceInvokeExpr) patternExpr;
			final InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr)expr;

			localVisitor.setPatternLocal(patternInstanceInvokeExpr.getBase());			
			instanceInvokeExpr.getBase().apply(localVisitor);
			
			if(!(Boolean)localVisitor.getResult()) {
				setResult(false);
				return;
			}
		}
		
		// check method signature
		SootMethodRef callee = patternInvokeExpr.getMethod();
		if(!expr.getType().equals(TypeHelper.resolveType(callee.getType()))) {
			setResult(false);
			return;
		}

		// TODO What about wildcards ...
		String methodSignature = "<" + callee.getClass_().getName() + ": " + TypeHelper.resolveType(callee.getType()) + " " + callee.getName() + "(";
		boolean notFirst = false;
		
		for(final QualifiedName parameter : callee.getParameters()) {
			if(notFirst) {
				methodSignature += ",";
			} else {
				notFirst = true;
			}
			methodSignature += parameter.getName();
		}
		methodSignature += ")>";

		if(!expr.getMethod().getSignature().equals(methodSignature)) {
			setResult(false);
			return;
		}
		
		for(int index = 0; index < expr.getArgCount(); ++index) {
			final NonExpr nonExpr = patternInvokeExpr.getParameters().get(index);
			
			if(nonExpr instanceof LocalOrWildcard) {
				localVisitor.setPatternLocal((LocalOrWildcard)nonExpr);			
				expr.getArg(index).apply(localVisitor);
				
				if(!(Boolean)localVisitor.getResult()) {
					setResult(false);
					return;
				}
			} else {
				LOG.warn("Unhandled parameter type {}.", nonExpr.getClass().getName());
				setResult(false);
				return;
			}
		}
		
		setResult(true);
	}
	
	@Override
	public void caseLocal(final Local local) {
		if(!(patternExpr instanceof LocalOrWildcard)) {
			setResult(false);
			return;
		}
		
		localVisitor.setPatternLocal((LocalOrWildcard)patternExpr);
		local.apply(localVisitor);
		setResult(localVisitor.getResult());
	}

	@Override
	public void defaultCase(Object v) {
		//LOG.error("Unhandled jimple '{}'.", v);
		setResult(false);
	}
}
