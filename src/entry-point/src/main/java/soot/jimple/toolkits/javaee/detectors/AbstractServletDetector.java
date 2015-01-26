/*
   This file is part of Soot entry point creator.

   Soot entry point creator is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Soot entry point creator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Soot entry point creator.  If not, see
<http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
*/
package soot.jimple.toolkits.javaee.detectors;

import java.util.Map;

/**
 * An abstract servlet detector with some additional functions.
 * 
 * @author Bernhard Berger
 */
public abstract class AbstractServletDetector implements ServletDetector {
	/**
	 * Phase options for servlet detection.
	 */
	@SuppressWarnings("rawtypes")
	protected Map options;
	
	@Override
	public void setOptions(@SuppressWarnings("rawtypes") final Map options) {
		this.options = options;
	}
}
