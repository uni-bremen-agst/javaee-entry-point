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
 * Base class for evaluating binary operators on numbers, such as add and multiply.
 *  
 * @author Bernhard Berger
 */
public abstract class BinOpNumberVisitor<T> {
	public T calc(final Number leftOp, final Number rightOp) {
		if(leftOp instanceof Double) {
			return calc(leftOp.doubleValue(), rightOp.doubleValue());
		} else if(leftOp instanceof Float) {
			return calc(leftOp.floatValue(), rightOp.floatValue());
		} else if(leftOp instanceof Long || rightOp instanceof Long) {
			return calc(leftOp.longValue(), rightOp.longValue());
		} else if(leftOp instanceof Integer || rightOp instanceof Integer) {
			return calc(leftOp.intValue(), rightOp.intValue());
		} else if(leftOp instanceof Short || rightOp instanceof Short) {
			return calc(leftOp.shortValue(), rightOp.shortValue());
		} else if(leftOp instanceof Byte || rightOp instanceof Byte) {
			return calc(leftOp.byteValue(), rightOp.byteValue());
		} else {
			return null;
		}
	}
	
	protected abstract T calc(final Double leftOp, final Double rightOp);
	
	protected abstract T calc(final Float leftOp, final Float rightOp);
	
	protected abstract T calc(final Long leftOp, final Long rightOp);
	
	protected abstract T calc(final Integer leftOp, final Integer rightOp);
	
	protected abstract T calc(final Short leftOp, final Short rightOp);
	
	protected abstract T calc(final Byte leftOp, final Byte rightOp);
}
