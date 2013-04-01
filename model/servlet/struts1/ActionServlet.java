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
package soot.jimple.toolkits.javaee.model.servlet.struts1;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


import soot.jimple.toolkits.javaee.model.servlet.Servlet;

@XmlRootElement(name="ActionServlet")
public class ActionServlet extends Servlet {
	private String parameter;
	
	@XmlAttribute
	public String getParameter() {
		return parameter;
	}
	
	public void setParameter(final String parameter) {
		this.parameter = parameter;
	}

	private String scope;
	
	@XmlAttribute
	public String getScope() {
		return scope;
	}
	
	public void setScope(final String scope) {
		this.scope = scope;
	}

	private boolean validate;
	
	@XmlAttribute
	public boolean getValidate() {
		return validate;
	}
	
	public void setValidate(final boolean validate) {
		this.validate = validate;
	}
	
	private List<ActionForward> forwards = new LinkedList<ActionForward>();

	@XmlElement(name="forward")
	@XmlElementWrapper(name="forwards")
	public List<ActionForward> getForwards() {
		return forwards;
	}
	
	private FormBean formBean;
	
	@XmlElement
	public FormBean getFormBean() {
		return formBean;
	}
	
	public void setFormBean(final FormBean formBean) {
		this.formBean = formBean;
	}

	private String actionClass;

	@XmlAttribute
	public String getActionClass() {
		return actionClass;
	}

	public void setActionClass(final String actionClass) {
		this.actionClass = actionClass;
	}

	private List<String> methods = new LinkedList<String>();

	@XmlElement(name="method")
	@XmlElementWrapper(name="methods")
	public List<String> getMethods() {
		return methods;
	}
}
