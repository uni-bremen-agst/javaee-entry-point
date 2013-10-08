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
package soot.jimple.toolkits.javaee.model.servlet;

import java.text.Collator;
import java.util.Comparator;

public class NamedElementComparator<T extends NamedElement> implements Comparator<T> {
	private final Collator collator = Collator.getInstance();
	
	@Override
	public int compare(final T left, final T right) {
		return collator.compare(left.getName(), right.getName());
	}
}
