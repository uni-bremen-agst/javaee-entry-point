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

import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.LabelOrStatement;

/**
 * Statement vistor that checks whether the visited statement equals {@code stmtPattern}.
 * 
 * The result of the visitor (returned by {@code getResult} is of type {@code Boolean}.
 * 
 * @author Bernhard Berger
 */
public class StmtMatchVisitor extends AbstractStmtSwitch {
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(StmtMatchVisitor.class);
	
	/**
	 * The pattern we are looking for.
	 */
	private LabelOrStatement stmtPattern;
	
	/**
	 * Bindings for wildcards.
	 */
	private SymbolTable symbolTable;

	public void setPatternUnit(final LabelOrStatement current) {
		this.stmtPattern = current;
	}

	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	@Override
	public void caseBreakpointStmt(BreakpointStmt stmt) {
		// TODO Auto-generated method stub
		super.caseBreakpointStmt(stmt);
	}

	@Override
	public void caseInvokeStmt(final InvokeStmt stmt) {
		if(!(stmtPattern instanceof soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeStmt)) {
			setResult(Boolean.FALSE);
			return;
		} else {
			InvokeExprVisitor visitor = new InvokeExprVisitor();
			visitor.setSymbolTable(symbolTable);
			visitor.setPatternInvokeExpr(((soot.jimple.toolkits.transformation.dsl.transformationLanguage.InvokeStmt)stmtPattern).getInvokeExpr());
			
			stmt.getInvokeExpr().apply(visitor);
			setResult(visitor.getResult());
		}
	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		// TODO Auto-generated method stub
		super.caseAssignStmt(stmt);
	}

	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		// TODO Auto-generated method stub
		super.caseIdentityStmt(stmt);
	}

	@Override
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		// TODO Auto-generated method stub
		super.caseEnterMonitorStmt(stmt);
	}

	@Override
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		// TODO Auto-generated method stub
		super.caseExitMonitorStmt(stmt);
	}

	@Override
	public void caseGotoStmt(GotoStmt stmt) {
		// TODO Auto-generated method stub
		super.caseGotoStmt(stmt);
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		// TODO Auto-generated method stub
		super.caseIfStmt(stmt);
	}

	@Override
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		// TODO Auto-generated method stub
		super.caseLookupSwitchStmt(stmt);
	}

	@Override
	public void caseNopStmt(NopStmt stmt) {
		// TODO Auto-generated method stub
		super.caseNopStmt(stmt);
	}

	@Override
	public void caseRetStmt(RetStmt stmt) {
		// TODO Auto-generated method stub
		super.caseRetStmt(stmt);
	}

	@Override
	public void caseReturnStmt(ReturnStmt stmt) {
		// TODO Auto-generated method stub
		super.caseReturnStmt(stmt);
	}

	@Override
	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		// TODO Auto-generated method stub
		super.caseReturnVoidStmt(stmt);
	}

	@Override
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		// TODO Auto-generated method stub
		super.caseTableSwitchStmt(stmt);
	}

	@Override
	public void caseThrowStmt(ThrowStmt stmt) {
		// TODO Auto-generated method stub
		super.caseThrowStmt(stmt);
	}

	@Override
	public void defaultCase(Object obj) {
		LOG.info("Unhandled stmt type {}", obj.getClass());
		setResult(Boolean.FALSE);
	}

	
}
