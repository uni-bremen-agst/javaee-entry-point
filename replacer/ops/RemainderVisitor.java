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

public class RemainderVisitor extends BinOpNumberVisitor<Number> {
	
	private static RemainderVisitor instance = new RemainderVisitor();
	
	public static RemainderVisitor getInstance() {
		return instance;
	}

	@Override
	protected Double calc(Double leftOp, Double rightOp) {
		return leftOp % rightOp;
	}

	@Override
	protected Float calc(Float leftOp, Float rightOp) {
		return leftOp % rightOp;
	}

	@Override
	protected Long calc(Long leftOp, Long rightOp) {
		return leftOp % rightOp;
	}

	@Override
	protected Integer calc(Integer leftOp, Integer rightOp) {
		return leftOp % rightOp;
	}

	@Override
	protected Short calc(Short leftOp, Short rightOp) {
		return (short)(leftOp % rightOp);
	}

	@Override
	protected Byte calc(Byte leftOp, Byte rightOp) {
		return (byte)(leftOp % rightOp);
	}
}
