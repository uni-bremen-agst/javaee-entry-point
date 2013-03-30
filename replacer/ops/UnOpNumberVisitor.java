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
package soot.jimple.toolkits.transformation.replacer.ops;

/**
 * Base class for evaluating unary operators on numbers, such as + and -.
 *  
 * @author Bernhard Berger
 */
public abstract class UnOpNumberVisitor {
	public Number calc(final Number op) {
		if(op instanceof Double) {
			return calc(op.doubleValue());
		} else if(op instanceof Float) {
			return calc(op.floatValue());
		} else if(op instanceof Long) {
			return calc(op.longValue());
		} else if(op instanceof Integer) {
			return calc(op.intValue());
		} else if(op instanceof Short) {
			return calc(op.shortValue());
		} else if(op instanceof Byte) {
			return calc(op.byteValue());
		} else {
			return null;
		}
	}
	
	protected abstract Number calc(final Double op);
	
	protected abstract Number calc(final Float op);
	
	protected abstract Number calc(final Long op);
	
	protected abstract Number calc(final Integer op);
	
	protected abstract Number calc(final Short op);
	
	protected abstract Number calc(final Byte op);
}
