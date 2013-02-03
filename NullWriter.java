package soot.jimple.toolkits.javaee;

import java.io.IOException;
import java.io.Writer;

/**
 * Simple writer that discards all output.
 * 
 * @author Bernhard Berger
 */
public class NullWriter extends Writer {

	@Override
	public void close() throws IOException {
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(char[] arg0, int arg1, int arg2) throws IOException {
	}
}
