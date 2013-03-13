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

import java.util.Iterator;

/**
 * Iterator extension to support retrieval of current object.
 *
 * @author Bernhard Berger
 *
 * @param <T> Type of iterated elements.
 */
public class CurrentIterator<T> implements Iterator<T>{
	
	/**
	 * Current element.
	 */
	private T current;

	/**
	 * The iterator we want to traverse.
	 */
	private final Iterator<T> delegate;
	
	public CurrentIterator(final Iterator<T> iterator) {
		delegate = iterator;
	}
	
	public T current() {
		return current;
	}

	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	@Override
	public T next() {
		current = delegate.next();
		return current;
	}

	@Override
	public void remove() {
		delegate.remove();
	}
}
