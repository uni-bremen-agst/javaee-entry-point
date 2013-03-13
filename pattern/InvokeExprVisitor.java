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
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.SootMethodRef;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.Wildcard;

/**
 * Visitor for invoke expressions. The result (see {@link #getResult()}) will be set to {@code Boolean.TRUE} if
 *   the pattern matches. Otherwise it will return {@code Boolean.FALSE}.
 * 
 * @author Bernhard Berger
 */
public class InvokeExprVisitor extends AbstractJimpleValueSwitch {
	
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(InvokeExprVisitor.class);

	/**
	 * Symbol table for binding wildcards.
	 */
	private SymbolTable symbolTable;
	
	/**
	 * The pattern we are looking for
	 */
	private soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeExpr patternInvokeExpr;

	public void setSymbolTable(final SymbolTable symbolTable) {
		this.symbolTable = symbolTable;

	}

	public void setPatternInvokeExpr(final soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeExpr invokeExpr) {
		this.patternInvokeExpr = invokeExpr;
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr expr) {
		if(!(patternInvokeExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.InterfaceInvokeExpr)) {
			setResult(Boolean.FALSE);
			return;
		}

		caseInvokeExpr(expr);
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr expr) {
		if(!(patternInvokeExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.SpecialInvokeExpr)) {
			setResult(Boolean.FALSE);
			return;
		}
		
		caseInvokeExpr(expr);
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr expr) {
		if(!(patternInvokeExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.StaticInvokeExpr)) {
			setResult(Boolean.FALSE);
			return;
		}
		
		setResult(Boolean.FALSE);
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr expr) {
		if(!(patternInvokeExpr instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.VirtualInvokeExpr)) {
			setResult(Boolean.FALSE);
			return;
		}
		
		caseInvokeExpr(expr);
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr expr) {
		LOG.warn("Dynamic invoke expressions are not yet implemented");
		setResult(Boolean.FALSE);
	}
	
	public void caseInvokeExpr(final InvokeExpr expr) {
		// check base for methods invoked for an instance
		if(expr instanceof InstanceInvokeExpr) {
			soot.jimple.toolkits.transformation.dsl.transformationLanguage.InstanceInvokeExpr patternInstanceInvokeExpr = (soot.jimple.toolkits.transformation.dsl.transformationLanguage.InstanceInvokeExpr) patternInvokeExpr;
			InstanceInvokeExpr instanceInvokeExpr = (InstanceInvokeExpr)expr;

			LocalOrWildcard low = patternInstanceInvokeExpr.getBase();
			Local base = (Local)instanceInvokeExpr.getBase();
			
			if(!compareLocals(low, base)) {
				return;
			}
		}
		
		// check method signature
		SootMethodRef callee = patternInvokeExpr.getMethod();
		if(!expr.getType().toString().equals(callee.getType())) {
			setResult(Boolean.FALSE);
			return;
		}

		String methodSignature = "<" + callee.getClass_() + ": " + callee.getType() + " " + callee.getName() + "(";
		boolean notFirst = false;
		for(final String parameter : callee.getParameters()) {
			if(notFirst) {
				methodSignature += ",";
			} else {
				notFirst = true;
			}
			methodSignature += parameter;
		}
		methodSignature += ")>";

		if(!expr.getMethod().getSignature().equals(methodSignature)) {
			setResult(Boolean.FALSE);
			return;
		}
		
		LOG.info("Found matching method {} - {}.", expr.getMethod().getSignature(), methodSignature);
		
		for(int index = 0; index < expr.getArgCount(); ++index) {
			
			if(!compareLocals((LocalOrWildcard)patternInvokeExpr.getParameters().get(index), (Local)expr.getArg(index))) {
				return;
			}
		}
		
		setResult(Boolean.TRUE);
	}

	/**
	 * Compares a pattern local to a jimple local. If {@code patternLocal} is a wildcard the method
	 *   will check if it is already bound in the symbol table. In this case, it will check if it is
	 *   bound to the same local then the last occurence. If the wildcard is not yet bound the
	 *   wildcard name will be bound to the value found in the jimple local. If {@code patternLocal} is
	 *   a local itself the method will check whether {@code jimpleLocal} has the same name.<br/>
	 * 
	 * If the algorithm detects that the locals do not match it calls {@code setResult(Boolean.FALSE)}.<br/>
	 * 
	 * Pseudo code:
	 * <pre>{@code
	 *   if(patternLocal instanceof Wildcard) {
	 *     if(symbolTable.contains(patternLocal.getName()) {
	 *       return symbolTable.get(patternLocal.getName()) == jimpleLocal.getName();
	 *     } else {
	 *       symbolTable.bind(patternLocal.getName(), jimpleLocal.getName());
	 *       return true;
	 *     }
	 *   } else {
	 *     return patternLocal.getName() == jimpleLocal.getName();
	 *   }
	 * 
	 *   return true;
	 * }</pre>
	 * 
	 * @param patternLocal
	 * @param jimpleLocal
	 */
	private boolean compareLocals(final LocalOrWildcard patternLocal, final Local jimpleLocal) {
		if(patternLocal instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.Local) {
			if(!((soot.jimple.toolkits.transformation.dsl.transformationLanguage.Local)patternLocal).getName().equals(jimpleLocal.getName())) {
				setResult(Boolean.FALSE);
				return false;
			}
		} else {
			Wildcard wc = (Wildcard)patternLocal;
			if(symbolTable.contains(wc.getName())) {
				if(!symbolTable.get(wc.getName()).equals(jimpleLocal.getName())) {
					setResult(Boolean.FALSE);
					return false;
				}
			} else {
				symbolTable.bind(wc.getName(), jimpleLocal);
			}
		}
		
		return true;
	}

	@Override
	public void defaultCase(Object v) {
		LOG.error("Unhandled jimple '{}'.", v);
	}
}
