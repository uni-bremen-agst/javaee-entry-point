/**
 * Copyright 2013 Bernhard Berger - Universität Bremen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package soot.jimple.toolkits.transformation;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import soot.G;
import soot.PhaseOptions;
import soot.SceneTransformer;
import soot.Singletons;
import soot.jimple.toolkits.transformation.dsl.TransformationLanguageStandaloneSetup;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.Transformation;
import soot.jimple.toolkits.transformation.pattern.Match;
import soot.jimple.toolkits.transformation.pattern.PatternMatcher;
import soot.jimple.toolkits.transformation.pattern.PatternMatcherFactory;
import soot.jimple.toolkits.transformation.replacer.TransformerFactory;
import soot.jimple.toolkits.transformation.replacer.Transformer;

/**
 * Transformation support for in-memory Jimple using a DSL.
 * 
 * @todo Add an API to execute transformation from analysis.
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
		
		processTemplatesInPaths(options);
	}

	/**
	 * Processes all templates in a the specified paths.
	 * 
	 * @param options The options for this phase.
	 */
	private void processTemplatesInPaths(@SuppressWarnings("rawtypes") final Map options) {
		final String paths = PhaseOptions.getString(options, "process-paths");
		
		if(paths == null || paths.isEmpty()) {
			return;
		}
		
		final String [] splittedPaths = paths.split(File.pathSeparator);
		
		for(final String path : splittedPaths) {
			final File folder = new File(path);
			
			LOG.info("Processing template directory {}.", folder);
			
			if(!folder.exists()) {
				LOG.warn("Specified template directory '{}' does not exist.", folder);
				continue;
			}
			
			if(!folder.isDirectory()) {
				LOG.warn("Specified template directory '{}' is not a directory.", folder);
				continue;
			}

			final File [] templateFiles = folder.listFiles( new FileFilter() {
	           public boolean accept( File file ) {
	               return file.getName().toLowerCase().endsWith(".tlf");
	           }
			});
			
			for(final File templateFile : templateFiles) {
				LOG.info("Found template {}.", templateFile);
				try {
					final InputStream ruleStream = new FileInputStream(templateFile);
					applyTransformation(ruleStream, templateFile.getName());
				} catch(final FileNotFoundException e) {
					LOG.error("Cannot find file '{}'.", templateFile);
				}
			}
		}
	}

	/**
	 * Applies a transformation rule to the scene.
	 * 
	 * @param ruleStream An input stream of a rule.
	 * @param name The name of the transformation rule.
	 */
	public void applyTransformation(final InputStream ruleStream, final String name) {
		LOG.info("Applying transformation '{}'.", name);

		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri(".");

		final Injector injector = new TransformationLanguageStandaloneSetup().createInjectorAndDoEMFRegistration();
		final XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		resourceSet.addLoadOption(XtextResource.OPTION_ENCODING, "UTF-8");

		final Resource resource = resourceSet.createResource(URI.createURI("dummy:/example.tlf"));
		try {
			resource.load(ruleStream, resourceSet.getLoadOptions());
			if(!resource.getErrors().isEmpty()) {
				for(Resource.Diagnostic diagnostic : resource.getErrors()) {
					LOG.error("Error while processing rule '': {}", name, diagnostic);
				}

				return;
			}
		
			final Transformation model = (Transformation) resource.getContents().get(0);
			final PatternMatcher matcher = PatternMatcherFactory.create(model.getPattern());
			int numberOfReplacemens = 0;
			
			for(final Match match : matcher) {
				LOG.info("Found match {}.", match);
				Transformer transformator = TransformerFactory.create(model.getReplacement());
				transformator.setMatch(match);
				transformator.transform();
				numberOfReplacemens += 1;
			}
			
			LOG.info("{} successfull replacements for rule '{}'.", numberOfReplacemens, name);
		} catch (IOException e) {
			LOG.error("Unable to load rule {}.", name);
		}
	}
}
