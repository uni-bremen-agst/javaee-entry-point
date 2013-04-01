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
package soot.jimple.toolkits.javaee.model.servlet.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import soot.jimple.toolkits.javaee.model.servlet.Parameter;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;

public abstract class AbstractServlet extends Servlet {

	private FileLoader loader;
	private List<Parameter> parameters = new ArrayList<Parameter>();

	public AbstractServlet() {
		super();
	}

	public AbstractServlet(String clazz, String name) {
		super(clazz, name);
	}

	@XmlElement
	public FileLoader getLoader() {
		return loader;
	}

	@XmlElement(name = "parameter")
	@XmlElementWrapper(name = "init-parameters")
	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setLoader(final FileLoader loader) {
		this.loader = loader;
	}
}