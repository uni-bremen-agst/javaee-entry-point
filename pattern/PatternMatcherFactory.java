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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.toolkits.transformation.dsl.transformationLanguage.Pattern;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.SequencePattern;

/**
 * Factory for pattern matcher. The pattern matcher is created according to the specified
 *   pattern type.
 * 
 * @author Bernhard Berger
 */
public final class PatternMatcherFactory {
	/**
	 * Logging facility.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(PatternMatcherFactory.class);
	
	public static PatternMatcher create(final Pattern pattern) {
		if(pattern instanceof SequencePattern) {
			LOG.info("Creating sequence pattern-matcher.");
			return new SequencePatternMatcher((SequencePattern)pattern);
		} else {
			LOG.error("Unsupported pattern type");
			return new PatternMatcher() {
				
				@Override
				public Iterator<Match> iterator() {
					return new Iterator<Match>() {

						@Override
						public boolean hasNext() {
							return false;
						}

						@Override
						public Match next() {
							return null;
						}

						@Override
						public void remove() {
						}
					};
				}
			};
		}
	}
}
