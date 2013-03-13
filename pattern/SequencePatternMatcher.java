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

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.SequencePattern;

/**
 * The sequence pattern matcher searches for a sequence of statements within a body.
 * 
 * @author Bernhard Berger
 */
public class SequencePatternMatcher implements PatternMatcher {
	
	/**
	 * Logger instance
	 */
	private final static Logger LOG = LoggerFactory.getLogger(SequencePatternMatcher.class);

	/**
	 * The pattern we are looking for.
	 */
	private SequencePattern pattern;

	public SequencePatternMatcher(final SequencePattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public Iterator<Match> iterator() {
		return new MatchIterator();
	}

	/**
	 * This inner class does the real detection stuff.
	 * 
	 * @author Bernhard Berger
	 */
	private class MatchIterator implements Iterator<Match> {
		
		private Iterator<SootClass> classes;
		private Iterator<SootMethod> methods;
		private Iterator<? extends Match> matches;


		public MatchIterator() {
			this.classes = Scene.v().getClasses().iterator();
			this.methods = new EmptyIterator<SootMethod>();
			this.matches = new EmptyIterator<Match>();
		}

		@Override
		public boolean hasNext() {
			if(matches.hasNext()) {
				return true;
			}
			
			do {
				SootMethod nextMethod = nextMethod();
			
				if(nextMethod == null) {
					matches = new EmptyIterator<Match>();
					return false;
				}
				
				collectMatches(nextMethod);
			} while(!matches.hasNext());
			
			return true;
		}

		private void collectMatches(final SootMethod method) {
			LOG.debug("Collecting matches for {}.", method.getSignature());
			
			if(!method.isConcrete()) {
				matches = new EmptyIterator<Match>();
				return;
			}
			
			final UnitMatcher matcher = new UnitMatcher(pattern, method.retrieveActiveBody());

			matcher.match();
			
			matches = matcher.getMatches().iterator();
		}

		private SootMethod nextMethod() {
			if(methods.hasNext()) {
				return methods.next();
			} else {
				SootClass nextClass = nextClass();

				if(nextClass == null) {
					return null;
				} else {
					methods = nextClass.getMethods().iterator();
				}
				
				return nextMethod();
			}
		}
		
		/**
		 * @return Next class or {@code null}.
		 */
		private SootClass nextClass() {
			if(classes.hasNext()) {
				return classes.next();
			} else {
				return null;
			}
		}

		@Override
		public Match next() {
			return matches.next();
		}

		@Override
		public void remove() {
			throw new IllegalStateException("Cannot remove match.");
		}
	}
}
