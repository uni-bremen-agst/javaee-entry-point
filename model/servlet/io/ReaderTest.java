package soot.jimple.toolkits.javaee.model.servlet.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import soot.jimple.toolkits.javaee.model.servlet.Web;

public class ReaderTest {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, Exception {
		Web web = WebXMLReader.readWebXML(new FileInputStream(args[0]));

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
