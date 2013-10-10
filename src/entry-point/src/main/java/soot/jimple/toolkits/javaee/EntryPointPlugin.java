package soot.jimple.toolkits.javaee;

import soot.Transformer;
import soot.plugins.SootPhasePlugin;
import soot.plugins.model.PhasePluginDescription;

public class EntryPointPlugin implements SootPhasePlugin {

	@Override
	public String[] getDeclaredOptions() {
		return new String [] {"root-package",
				              "main-class",
				              "consider-all-servlets",
				              "filter-config-class",
				              "servlet-config-class",
				              "servlet-request-class",
				              "servlet-response-class",
				              "dump-model",
				              "output-dir" };
	}

	@Override
	public String[] getDefaultOptions() {
		return new String [] {"root-package:com.example",
	              "main-class:ServletMain",
	              "consider-all-servlets:false",
	              "filter-config-class:soot.javaee.stubs.servlet.FilterConfigImpl",
	              "servlet-config-class:soot.javaee.stubs.servlet.ServletConfigImpl",
	              "servlet-request-class:soot.javaee.stubs.servlet.ServletRequestImpl",
	              "servlet-response-class:soot.javaee.stubs.servlet.ServletResponseImpl",
	              "output-dir:." };

	}

	@Override
	public Transformer getTransformer() {
		return new ServletEntryPointGenerator();
	}

	@Override
	public void setDescription(PhasePluginDescription arg0) {
	}
}
