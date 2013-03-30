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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.toolkits.transformation.dsl.transformationLanguage.Wildcard;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardAdditiveExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardBoolean;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardByte;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardCall;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardChar;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardClassLiteral;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardConditionalAndExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardConditionalOrExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardDereference;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardDouble;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardEqualityExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardField;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardFloat;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardInstanceOfExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardInt;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardLong;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardMultiplicativeExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardName;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardNew;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardNull;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardParExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardQName;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardRelationalExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardShort;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardString;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardType;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardUnaryExpression;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardUnaryExpressionNotPlusMinus;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardVoid;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.util.TransformationLanguageSwitch;
import soot.jimple.toolkits.transformation.pattern.SymbolTable;
import soot.jimple.toolkits.transformation.replacer.ops.AddVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.DivideVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.GreaterVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.LessEqualVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.LessVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.MinusVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.MultiplyVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.PlusVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.RemainderVisitor;
import soot.jimple.toolkits.transformation.replacer.ops.SubtractVisitor;

/**
 * Interpreter for wildcard expressions.
 * 
 * @author Bernhard Berger
 */
public class WildcardVisitor extends TransformationLanguageSwitch<Object> {
	/**
	 * Logger isntance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(WildcardVisitor.class);

	/**
	 * Current value.
	 */
	private Object value;
	
	/**
	 * Symbol table for name bindings.
	 */
	private SymbolTable symbolTable;
	
	public void setSymbolTable(final SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	@Override
	public Object doSwitch(EObject eObject) {
		return super.doSwitch(eObject);
	}

	@Override
	public Object caseWildcard(final Wildcard wildcard) {
		return doSwitch(wildcard.getExpression());
	}

	@Override
	public Object caseWildcardExpression(final WildcardExpression expression) {
		return doSwitch(expression.getExpression());
	}

	@Override
	public Object caseWildcardBoolean(final WildcardBoolean wBoolean) {
		return wBoolean.getValue().equals("true");
	}

	@Override
	public Object caseWildcardInt(final WildcardInt wInt) {
		return wInt.getValue();
	}

	@Override
	public Object caseWildcardString(final WildcardString wString) {
		return wString.getValue();
	}

	@Override
	public Object caseWildcardNew(final WildcardNew wNew) {
		// TODO Auto-generated method stub
		return super.caseWildcardNew(wNew);
	}

	@Override
	public Object caseWildcardQName(final WildcardQName qName) {
		String name = qName.getName();
		
		if(!name.contains("::") && symbolTable.contains(name)) {
			return symbolTable.get(name);
		} else {
			name = name.replace("::", ".");
			try {
				return Class.forName(name);
			} catch (ClassNotFoundException e) {
				LOG.error("Cannot find class '{}'.", name);
				return null;
			}
		}
	}

	@Override
	public Object caseWildcardCall(final WildcardCall wCall) {
		final boolean staticMethod = value instanceof Class;
		final Class<?> clazz = staticMethod ? (Class<?>)value : value.getClass();
		final Object instance = staticMethod ? null : value;
		
		final List<Method> matchingMethods = new ArrayList<Method>(clazz.getMethods().length);
		
		for(final Method method : clazz.getMethods()) {
			if(Modifier.isStatic(method.getModifiers()) != staticMethod) {
				continue;
			}
			
			if(!method.getName().equals(wCall.getName())) {
				continue;
			}
			
			if(method.isVarArgs()) {
				matchingMethods.add(method);
			} else {
				matchingMethods.add(method);
			}
		}
		
		if(matchingMethods.size() == 0) {
			LOG.error("No matching method '{}' in '{}' found.", wCall.getName(), instance);
			return null;
		}
		
		for(final Method method : matchingMethods) {
			if(matchingMethods.size() > 1) {
				LOG.warn("Multiple matching methods with name '{}' in '{}' found. Trying {}.", wCall.getName(), instance, method);
			}
			
			final Object [] parameters = new Object [wCall.getParameters().size()];
			
			for(int index = 0; index < parameters.length; ++index) {
				parameters[index] = doSwitch(wCall.getParameters().get(index));
				
				LOG.info("Parameter {} is {} of type {}..", index, parameters[index], parameters[index].getClass());
			}
			
			try {
				return method.invoke(instance, parameters);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return null;
	}

	@Override
	public Object caseWildcardField(final WildcardField wField) {
		final boolean staticField = value instanceof Class;
		final Class<?> clazz = staticField ? (Class<?>)value : value.getClass();
		final Object instance = staticField ? null : value;
		
		try {
			final Field field = clazz.getField(wField.getName());
			return field.get(instance);
		} catch(final NoSuchFieldException e) {
			LOG.error("Object '{}' has no field with name '{}'.", instance, wField.getName());
			return null;
		} catch (final IllegalArgumentException e) {
			LOG.error("Illegal argument for field {} in object '{}'.", wField.getName(), instance);
			return null;
		} catch (final IllegalAccessException e) {
			LOG.error("Cannot access field {} in object '{}'.", wField.getName(), instance);
			return null;
		}

	}

	@Override
	public Object caseWildcardConditionalOrExpression(final WildcardConditionalOrExpression or) {
		Object result = doSwitch(or.getOperands().get(0));
		
		if(or.getOperands().size() == 1) {
			return result;
		}
		
		for(final WildcardConditionalAndExpression and : or.getOperands()) {
			if(and == or.getOperands().get(0)) {
				// first already handled
				if((Boolean)result) {
					// if first already evaluated to true we can stop
					return true;
				}
				continue;
			}
			
			result = (Boolean)result | (Boolean)doSwitch(and);
			
			if((Boolean)result) {
				return result; // early exit;
			}
		}

		return result;
	}

	@Override
	public Object caseWildcardName(final WildcardName name) {
		return symbolTable.get(name.getWName());
	}

	@Override
	public Object caseWildcardConditionalAndExpression(final WildcardConditionalAndExpression and) {
		Object result = doSwitch(and.getOperands().get(0));
		
		if(and.getOperands().size() == 1) {
			return result;
		}
		
		for(final WildcardEqualityExpression equal : and.getOperands()) {
			if(equal == and.getOperands().get(0)) {
				// first already handled
				if(!(Boolean)result) {
					// if first already evaluated to false we can stop
					return false;
				}
				continue;
			}
			
			result = (Boolean)result & (Boolean)doSwitch(equal);
			
			if(!(Boolean)result) {
				return result; // early exit;
			}
		}

		return result;
	}

	@Override
	public Object caseWildcardEqualityExpression(final WildcardEqualityExpression equal) {
		assert equal.getOperands().size() == equal.getOperator().size() + 1;
		
		Object result = doSwitch(equal.getOperands().get(0));
		
		for(int index = 0; index < equal.getOperator().size(); ++index) {
			String operator = equal.getOperator().get(index);
			
			if(operator.equals("==")) {
				result = result == doSwitch(equal.getOperands().get(index + 1));
			} else {
				assert operator.equals("!=");
				result = result != doSwitch(equal.getOperands().get(index + 1));
			}
		}
		
		return result;
	}

	@Override
	public Object caseWildcardInstanceOfExpression(final WildcardInstanceOfExpression instof) {
		LOG.info("InstanceOf {} and type is {}", instof, instof.getType());
		final Object instance = doSwitch(instof.getOperand());
		
		if(instof.getType() == null) {
			return instance;
		} else {
			final Class<?> clazz = (Class<?>)doSwitch(instof.getType());
			LOG.info("   -> class {}", clazz);
			LOG.info("   -> instance {}", instance.getClass());
			
			boolean result = clazz.isInstance(instance);
			LOG.info("     -> {}", result);
			return result;
		}
	}

	@Override
	public Object caseWildcardRelationalExpression(final WildcardRelationalExpression relOp) {
		assert relOp.getOperands().size() == relOp.getOperators().size() + 1;
		
		Object result = doSwitch(relOp.getOperands().get(0));
		
		for(int index = 0; index < relOp.getOperators().size(); ++index) {
			final String operator = relOp.getOperators().get(index);

			if(operator.equals("<=")) {
				result = LessEqualVisitor.getInstance().calc((Number) result, (Number) doSwitch(relOp.getOperands().get(index + 1)));
			} else if(operator.equals(">=")) {
				result = GreaterVisitor.getInstance().calc((Number) result, (Number) doSwitch(relOp.getOperands().get(index + 1)));
			} else if(operator.equals("<")) {
				result = LessVisitor.getInstance().calc((Number) result, (Number) doSwitch(relOp.getOperands().get(index + 1)));
			} else if(operator.equals(">")) {
				result = GreaterVisitor.getInstance().calc((Number) result, (Number) doSwitch(relOp.getOperands().get(index + 1)));
			} else {
				LOG.error("Unknown operator '{}' in relational expression", operator);
			}
		}
		
		return result;
	}

	@Override
	public Object caseWildcardAdditiveExpression(final WildcardAdditiveExpression addOp) {
		assert addOp.getOperands().size() == addOp.getOperators().size() + 1;
		
		Object result = doSwitch(addOp.getOperands().get(0));
		
		for(int index = 0; index < addOp.getOperators().size(); ++index) {
			final String operator = addOp.getOperators().get(index);

			if(operator.equals("+")) {
				result = AddVisitor.getInstance().calc((Number) result, (Number) doSwitch(addOp.getOperands().get(index + 1)));
			} else if(operator.equals(".")) {
				result = SubtractVisitor.getInstance().calc((Number) result, (Number) doSwitch(addOp.getOperands().get(index + 1)));
			} else {
				LOG.error("Unknown operator '{}' in additive expression", operator);
			}
		}
		
		return result;
	}

	@Override
	public Object caseWildcardMultiplicativeExpression(WildcardMultiplicativeExpression mulOp) {
		assert mulOp.getOperands().size() == mulOp.getOperators().size() + 1;
		
		Object result = doSwitch(mulOp.getOperands().get(0));
		
		for(int index = 0; index < mulOp.getOperators().size(); ++index) {
			final String operator = mulOp.getOperators().get(index);

			if(operator.equals("*")) {
				result = MultiplyVisitor.getInstance().calc((Number) result, (Number) doSwitch(mulOp.getOperands().get(index + 1)));
			} else if(operator.equals("/")) {
				result = DivideVisitor.getInstance().calc((Number) result, (Number) doSwitch(mulOp.getOperands().get(index + 1)));
			} else if(operator.equals("%")) {
				result = RemainderVisitor.getInstance().calc((Number) result, (Number) doSwitch(mulOp.getOperands().get(index + 1)));
			} else {
				LOG.error("Unknown operator '{}' in multiplicative expression", operator);
			}
		}
		
		return result;
	}

	@Override
	public Object caseWildcardUnaryExpression(final WildcardUnaryExpression unOp) {
		final String operator = unOp.getOperator();

		if(operator != null && operator.equals("+")) {
			return PlusVisitor.getInstance().calc((Number) doSwitch(unOp.getOperand()));
		} else if(operator != null && operator.equals("-")) {
			return MinusVisitor.getInstance().calc((Number) doSwitch(unOp.getOperand()));
		} else {
			return doSwitch(unOp.getOperand());
		}
	}
	
	@Override
	public Object caseWildcardUnaryExpressionNotPlusMinus(final WildcardUnaryExpressionNotPlusMinus unOp) {
		Object operand = doSwitch(unOp.getOperand());
		
		for(final WildcardDereference deref : unOp.getDereferences()) {
			this.value = operand;
			
			operand = doSwitch(deref);
		}
		
		if(unOp.isNot()) {
			return !(Boolean)operand;
		}

		return operand;
	}

	@Override
	public Object caseWildcardParExpression(final WildcardParExpression parExpr) {
		return doSwitch(parExpr);
	}

	@Override
	public Object caseWildcardClassLiteral(final WildcardClassLiteral classLiteral) {
		String typeSignature = "";
		
		for(int index = 0; index < classLiteral.getDimension().size() / 2; ++index) {
			typeSignature = "[" + typeSignature;
		}
		
	    final WildcardType baseType = classLiteral.getBaseType();

	    if(baseType instanceof WildcardQName) {
	    	typeSignature = typeSignature + "L" + ((WildcardQName)baseType).getName().replace("::", ".") + ";";
	    } else if(baseType instanceof WildcardBoolean) {
	    	typeSignature = typeSignature + "Z";
	    } else if(baseType instanceof WildcardChar) {
	    	typeSignature = typeSignature + "C";
		} else if(baseType instanceof WildcardByte) {
			typeSignature = typeSignature + "Z";
		} else if(baseType instanceof WildcardShort) {
			typeSignature = typeSignature + "S";
		} else if(baseType instanceof WildcardInt) {
			typeSignature = typeSignature + "I";
		} else if(baseType instanceof WildcardLong) {
			typeSignature = typeSignature + "J";
		} else if(baseType instanceof WildcardFloat) {
			typeSignature = typeSignature + "F";
		} else if(baseType instanceof WildcardDouble) {
			typeSignature = typeSignature + "D";
		} else if(baseType instanceof WildcardVoid) {
			typeSignature = typeSignature + "V";
		}
	    
		try {
			return Class.forName(typeSignature);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public Object caseWildcardNull(final WildcardNull nullObj) {
		return null;
	}
}
