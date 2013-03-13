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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.IntType;
import soot.Scene;
import soot.SootClass;
import soot.Type;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeStmt;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.NonExpr;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.VirtualInvokeExpr;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.Wildcard;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.util.TransformationLanguageSwitch;
import soot.jimple.toolkits.transformation.pattern.SymbolTable;

/**
 * Creates a Jimple statement according to a statement in the transformation language.
 * 
 * @author Bernhard Berger
 */
public class JimpleStmtFactory extends TransformationLanguageSwitch<Object> {
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(JimpleStmtFactory.class);
	
	/**
	 * Body of surrounding method.
	 */
	private Body body;
	
	/**
	 * Symbol table for wildcard binding.
	 */
	private SymbolTable symbolTable;
	
	/**
	 * Jimple factory.
	 */
	private Jimple jimple = Jimple.v();

	public void setBody(final Body body) {
		this.body = body;
	}
	
	public void setSymbolTable(final SymbolTable symbolTable) {
		this.symbolTable = symbolTable;		
	}

	@Override
	public Object caseInvokeStmt(final InvokeStmt object) {
		soot.jimple.InvokeExpr expr = (soot.jimple.InvokeExpr)doSwitch(object.getInvokeExpr());
		soot.jimple.InvokeStmt stmt = jimple.newInvokeStmt(expr);
		
		return stmt;
	}

	@Override
	public Object caseVirtualInvokeExpr(final VirtualInvokeExpr expr) {
		soot.Local base = (soot.Local)doSwitch(expr.getBase());
		
		SootClass declaringClass = Scene.v().getSootClass(expr.getMethod().getClass_());
		List<soot.Type> parameterTypes = new ArrayList<soot.Type>();
		for(final String parameter : expr.getMethod().getParameters()) {
			parameterTypes.add(resolveType(parameter));
		}
		soot.Type returnType = resolveType(expr.getMethod().getType());

		// TODO Remove false 
		soot.SootMethodRef methodRef = Scene.v().makeMethodRef(declaringClass, expr.getMethod().getName(), parameterTypes, returnType, false);
		
		List<soot.Value> args = new ArrayList<soot.Value>(expr.getParameters().size());
		for(final NonExpr nonExpr : expr.getParameters()) {
			args.add((soot.Value)doSwitch(nonExpr));
		}
		
		return jimple.newVirtualInvokeExpr(base, methodRef, args);
	}

	/**
	 * Creates a {@link soot.Type} for a string.
	 * 
	 * @todo Complete types.
	 * 
	 * @param type Name of the type.
	 * @return A valid type.
	 */
	private Type resolveType(final String type) {
		if(type.equals("void")) {
			return VoidType.v();
		} else if(type.equals("int")) {
			return IntType.v();
		} else {
			return Scene.v().getSootClass(type).getType();
		}
	}

	@Override
	public Object caseWildcard(final Wildcard object) {
		final soot.Value local = symbolTable.get(object.getName());
		
		if(local == null) {
			LOG.error("Cannot find local for {}.", object);
			return null;
		}
		
		return local;
	}

	@Override
	public Object defaultCase(final EObject object) {
		LOG.error("Unhandled model instance {}.", object);
		return null;
	}

}
