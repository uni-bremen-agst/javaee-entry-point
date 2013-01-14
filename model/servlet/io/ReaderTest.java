package soot.jimple.toolkits.javaee.model.servlet.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import soot.jimple.toolkits.javaee.model.servlet.Web;

public class ReaderTest {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, Exception {
		Web web = WebXMLReader.readWebXML(new FileInputStream(args[0]));

		System.out.println(web);
	}

}
