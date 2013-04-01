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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

public class FilterMapping {
	private Filter filter;
	
	@XmlIDREF
	@XmlAttribute
	public Filter getFilter() {
		return filter;
	}
	
	public void setFilter(final Filter filter) {
		this.filter = filter;
	}
	
	private String urlPattern;
	
	@XmlAttribute
	public String getURLPattern() {
		return urlPattern;
	}
	
	public void setURLPattern(final String urlPattern) {
		this.urlPattern = urlPattern;
	}
	
	@Override
	public String toString() {
		return "FilterMapping [" + urlPattern + " to " + filter + "]";
	}
}
