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
