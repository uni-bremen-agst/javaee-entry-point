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