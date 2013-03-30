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

public class MinusVisitor extends UnOpNumberVisitor {

	private static MinusVisitor instance = new MinusVisitor();
	
	public static MinusVisitor getInstance() {
		return instance;
	}

	@Override
	protected Number calc(Double op) {
		return -op;
	}

	@Override
	protected Number calc(Float op) {
		return -op;
	}

	@Override
	protected Number calc(Long op) {
		return -op;
	}

	@Override
	protected Number calc(Integer op) {
		return -op;
	}

	@Override
	protected Number calc(Short op) {
		return -op;
	}

	@Override
	protected Number calc(Byte op) {
		return -op;
	}

}
