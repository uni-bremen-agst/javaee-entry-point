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
