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
package soot.jimple.toolkits.javaee.model.servlet.http.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Factory for creating a {@see XPathExpressionProvider} instance according to
 *   the {@code web.xml} file.
 * 
 * @author Bernhard Berger
 */
public final class ProviderFactory {
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(ProviderFactory.class);
	
	/**
	 * Creates a new {@code XPathExpressionProvider} instance.
	 * 
	 * @param doc The document we have to parse.
	 * 
	 * @return A new instance.
	 */
	public final static XPathExpressionProvider create(final Document doc) {
		final NodeList elements = doc.getChildNodes();

		if(elements.getLength() == 0) {
			LOG.error("Empty document.");
			return new DTDProvider();
		}
		
		if(elements.getLength() != 1) {
			LOG.warn("More than one root node on element.");
		}
		
		if(elements.item(0).getNamespaceURI() == null) {
			return new DTDProvider();
		} else {
			return new SchemaProvider();
		}
	}
}
