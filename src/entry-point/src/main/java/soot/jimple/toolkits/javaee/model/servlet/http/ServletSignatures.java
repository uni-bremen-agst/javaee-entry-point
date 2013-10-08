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

/**
 * Interface that just contains some signatures and subsignatures.
 *
 * @author Bernhard Berger
 */
public interface ServletSignatures {
	/**
	 * Class name of HttpServlet.
	 */
	public static final String HTTP_SERVLET_CLASS_NAME = "javax.servlet.http.HttpServlet";
	
	/**
	 * Class name of {@code javax.servlet.http.HttpServletRequest}.
	 */
	public static final String HTTP_SERVLET_REQUEST_CLASS_NAME = "javax.servlet.http.HttpServletRequest";
	
	/**
	 * Class name of {@code javax.servlet.http.HttpServletResponse}.
	 */
	public static final String HTTP_SERVLET_RESPONSE_CLASS_NAME = "javax.servlet.http.HttpServletResponse";
	
	/**
	 * Class name of {@code javax.servlet.GenericServlet}.
	 */
	public static final String GENERIC_SERVLET_CLASS_NAME = "javax.servlet.GenericServlet";
}
