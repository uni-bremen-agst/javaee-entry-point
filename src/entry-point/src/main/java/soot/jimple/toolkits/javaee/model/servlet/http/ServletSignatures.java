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
   along with Soot entry point creator.  If not, see <http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
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
