package soot.jimple.toolkits.transformation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.G;
import soot.SceneTransformer;
import soot.Singletons;

/**
 * Transformation support for in-memory Jimple using a DSL.
 * 
 * @author Bernhard Berger
 */
public class JimpleTransformer extends SceneTransformer {
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(JimpleTransformer.class);
	
	/**
	 * @return The singleton instance.
	 */
	public static JimpleTransformer v() {
    	return G.v().soot_jimple_toolkits_transformation_JimpleTransformer();
    }
	
	public JimpleTransformer(final Singletons.Global g) {	
	}

	@Override
	protected void internalTransform(final String phaseName, @SuppressWarnings("rawtypes") final Map options) {
		LOG.info("Transforming scene.");

	}
}
