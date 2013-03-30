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
import soot.Scene;
import soot.SootClass;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.AssignStmt;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.ClassConstant;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeStmt;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.NonExpr;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.QualifiedName;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.VirtualInvokeExpr;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.Wildcard;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardName;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.util.TransformationLanguageSwitch;
import soot.jimple.toolkits.transformation.pattern.SymbolTable;
import soot.jimple.toolkits.transformation.utils.TypeHelper;

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
	
	/**
	 * Visitor for wildcards.
	 */
	private WildcardVisitor visitor = new WildcardVisitor();

	public void setBody(final Body body) {
		this.body = body;
	}
	
	public void setSymbolTable(final SymbolTable symbolTable) {
		this.symbolTable = symbolTable;	
		visitor.setSymbolTable(symbolTable);
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
		
		// TODO Handle wildcards
		SootClass declaringClass = Scene.v().getSootClass(expr.getMethod().getClass_().getName());
		List<soot.Type> parameterTypes = new ArrayList<soot.Type>();
		for(final QualifiedName parameter : expr.getMethod().getParameters()) {
			parameterTypes.add(TypeHelper.resolveType(parameter));
		}
		soot.Type returnType = TypeHelper.resolveType(expr.getMethod().getType());

		// TODO Remove false 
		soot.SootMethodRef methodRef = Scene.v().makeMethodRef(declaringClass, expr.getMethod().getName(), parameterTypes, returnType, false);
		
		List<soot.Value> args = new ArrayList<soot.Value>(expr.getParameters().size());
		for(final NonExpr nonExpr : expr.getParameters()) {
			args.add((soot.Value)doSwitch(nonExpr));
		}
		
		return jimple.newVirtualInvokeExpr(base, methodRef, args);
	}

	@Override
	public Object caseWildcard(final Wildcard object) {
		return visitor.doSwitch(object);
	}
	
	@Override
	public Object caseWildcardName(final WildcardName object) {
		return visitor.doSwitch(object);
	}
	
	@Override
	public Object caseAssignStmt(final AssignStmt object) {
		final Value lValue = (Value)this.doSwitch(object.getLhs());
		final Value rValue = (Value)this.doSwitch(object.getRhs());
		
		LOG.info("Creating assignment to {} from {}.", lValue, rValue);

		return jimple.newAssignStmt(lValue, rValue);
	}

	@Override
	public Object caseClassConstant(final ClassConstant object) {
		System.err.println("Class constant is " + object + " " + object.getName());
		return soot.jimple.ClassConstant.v((String)doSwitch(object.getName()));
	}

	@Override
	public Object defaultCase(final EObject object) {
		LOG.error("Unhandled model instance {}.", object);
		return null;
	}
}
