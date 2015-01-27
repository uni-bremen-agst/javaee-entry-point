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

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.http.FileLoader;

public class ReaderTest {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, Exception {
		final FileLoader  loader = new FileLoader(args[0]);
		final Web web = new Web();
		
		new WebXMLReader ().readWebXML(loader, web);

		final JAXBContext context = JAXBContext.newInstance( Web.class ); 
		final Marshaller m = context.createMarshaller();
		Writer w = null; 
		
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		
		try { 
		  w = new FileWriter( "servlet-model.xml" ); 
		  m.marshal( web, w ); 
		} 
		finally { 
		  try {
			  w.close();
		  } catch ( Exception e ) {
			  
		  } 
		}
	}

}
