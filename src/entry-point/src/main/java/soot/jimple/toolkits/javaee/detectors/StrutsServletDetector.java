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
package soot.jimple.toolkits.javaee.detectors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.toolkits.javaee.model.servlet.Parameter;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.http.FileLoader;
import soot.jimple.toolkits.javaee.model.servlet.http.HttpServlet;
import soot.jimple.toolkits.javaee.model.servlet.struts1.ActionServlet;
import soot.jimple.toolkits.javaee.model.servlet.struts1.io.StrutsReader;

/**
 * Detects Struts 1 servlets and creates servlets for actions.
 * 
 * @author Bernhard Berger
 */
public class StrutsServletDetector extends AbstractServletDetector {
	/**
	 * Logger
	 */
	private final static Logger LOG = LoggerFactory.getLogger(StrutsServletDetector.class);

	@Override
	public void detectFromSource(Web web) {
		LOG.warn("Detecting struts actions from source is not yet implemented.");
	}

	@Override
	public void detectFromConfig(final Web web) {
		LOG.info("Detecting struts actions from configuration file");
		for(final Servlet servlet : new ArrayList<Servlet>(web.getServlets())) {
			if(!servlet.getClazz().equals("org.apache.struts.action.ActionServlet")) {
				continue;
			}
			
			LOG.info("Found action servlet {}", servlet);
			
			HttpServlet httpServlet = (HttpServlet)servlet;
			
			final FileLoader fileLoader = httpServlet.getLoader();

			for(final Parameter parameter : httpServlet.getParameters()) {
				if(parameter.getName().equals("config")) {
					for(final String xmlFile : parameter.getValue().split(",")) {
						LOG.info("Found configuration file {}.", xmlFile);
						try {
							final InputStream inputStream = fileLoader.getInputStream(xmlFile);
							new StrutsReader(options, web, inputStream).readStrutsConfig();
						} catch(final IOException e) {
							LOG.error("Unable to load configuration file.", e);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Class<?>> getModelExtensions() {
		return (List<Class<?>>)(List<?>)Collections.singletonList(ActionServlet.class);
	}

	@Override
	public List<String> getCheckFiles() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getTemplateFiles() {
		return Collections.singletonList("soot::jimple::toolkits::javaee::templates::struts::ActionServlet::main");
	}
}
