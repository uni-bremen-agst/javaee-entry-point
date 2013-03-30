package soot.jimple.toolkits.javaee;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.WorkflowContextDefaultImpl;
import org.eclipse.emf.mwe.core.WorkflowInterruptedException;
import org.eclipse.emf.mwe.core.issues.IssuesImpl;
import org.eclipse.emf.mwe.core.issues.MWEDiagnostic;
import org.eclipse.emf.mwe.core.monitor.NullProgressMonitor;
import org.eclipse.emf.mwe.internal.core.Workflow;
import org.eclipse.emf.mwe.utils.StandaloneSetup;
import org.eclipse.xpand2.Generator;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xtend.check.CheckComponent;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.eclipse.xtend.type.impl.java.beans.JavaBeansStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.Singletons;
import soot.SootClass;
import soot.jimple.toolkits.javaee.detectors.HttpServletDetector;
import soot.jimple.toolkits.javaee.detectors.JaxWsServiceDetector;
import soot.jimple.toolkits.javaee.detectors.ServletDetector;
import soot.jimple.toolkits.javaee.detectors.StrutsServletDetector;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.http.ServletSignatures;

/**
 * This class drives the generation of a main method that creates and calls
 *   all configured servlets. This is necessary to calculate a correct call
 *   graph for web projects.
 *
 * @author Bernhard Berger
 */
public class ServletEntryPointGenerator extends SceneTransformer implements ServletSignatures {
 
	/**
	 * Logging facility.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(ServletEntryPointGenerator.class);
	
	/**
	 * List of all available servlet detectors.
	 */
	private final List<ServletDetector> servletDetectors = new ArrayList<ServletDetector>();
	
	/**
	 * @return The singleton instance.
	 */
	public static ServletEntryPointGenerator v() {
    	return G.v().soot_jimple_toolkits_javaee_ServletEntryPointGenerator();
    }

	/**
	 * Shall all servlets within the application be handled or just the ones
	 *   that are configured within a {@code web.xml}. Can be set with the
	 *   commandline parameter {@code consider-all-servlets}.
	 */
	private boolean considerAllServlets = false;

	/**
	 * Scene for lookup of classes.
	 */
	private final Scene scene = Scene.v();
	
	public ServletEntryPointGenerator(final Singletons.Global g) {
		servletDetectors.add(new HttpServletDetector());
		servletDetectors.add(new JaxWsServiceDetector());
		servletDetectors.add(new StrutsServletDetector());
	}
	
	/**
	 * The model.
	 */
	private Web web = new Web();

	/**
	 * Loads the {@code web.xml} or fakes it if the corresponding command line
	 *   parameter was given.
	 *
	 * TODO Implement proper war support for locator.
	 */
	private void loadWebXML(@SuppressWarnings("rawtypes") final Map options) {
		web.getGeneratorInfos().initializeFromOptions(options);
		
		if(considerAllServlets) {
			for(final ServletDetector dector : servletDetectors) {
				dector.setOptions(options);
				dector.detectFromSource(web);
			}
		} else {
			for(final ServletDetector dector : servletDetectors) {
				dector.setOptions(options);
				dector.detectFromConfig(web);
			}
		}
	}

	@Override
	protected void internalTransform(final String phaseName, @SuppressWarnings("rawtypes") final Map options) {
		LOG.info("Running {}", phaseName);

		final boolean wsOnly = PhaseOptions.getBoolean(options, "wsonly");
		considerAllServlets = PhaseOptions.getBoolean(options, "consider-all-servlets");

        loadWebXML(options);

        final String modelDestination = PhaseOptions.getString(options,
                "dump-model");
        if (!modelDestination.isEmpty()) {
            storeModel(modelDestination);
        }

        if (web.getServlets().isEmpty()){
            LOG.error("No servlets/WS detected.");
        } else {
            LOG.info("Processing templates");
            processTemplate(options);
            
            LOG.info("Loading main class.");
            final SootClass sootClass = scene.forceResolve(
                    PhaseOptions.getString(options, "root-package")
                            + "."
                            + PhaseOptions.getString(options,"main-class"), SootClass.BODIES);
            scene.setMainClass(sootClass);
            sootClass.setApplicationClass();
        }
	}

    /**
	 * Processes all templates.
	 */
	private void processTemplate(@SuppressWarnings("rawtypes") final Map options) {
		for(final ServletDetector detector : servletDetectors) {
				final JavaBeansMetaModel metaModel = new JavaBeansMetaModel();
				metaModel.setTypeStrategy(new JavaBeansStrategy());
				
				final WorkflowContext context = new WorkflowContextDefaultImpl();
				context.set("root", web);
				
				final CheckComponent check = new CheckComponent();
				check.setExpression("root");
				check.addMetaModel(metaModel);
				for(final String checkFile : detector.getCheckFiles()) {
					check.addCheckFile(checkFile);
				}

				final Outlet outlet = new Outlet();
				outlet.setPath(PhaseOptions.getString(options, "output-dir"));
				
				final Workflow workflow = new Workflow();
				workflow.addBean(new StandaloneSetup());
				workflow.addBean(metaModel);
				workflow.addComponent(check);

				for(final String templateFile : detector.getTemplateFiles()) {
					final Generator generator = new Generator();
					generator.addMetaModel(metaModel);
					generator.setExpand(templateFile + " FOR root");
					generator.addOutlet(outlet);
					workflow.addComponent(generator);
				}
				
				final IssuesImpl issues = new IssuesImpl();
				NullProgressMonitor monitor = new NullProgressMonitor();
				
				try {
					workflow.invoke(context, monitor, issues);
				} catch(final WorkflowInterruptedException e) {
					System.err.println("Workflow interrupted: " + e.getMessage());
				}
				
				if(issues.hasErrors()) {
					for(final MWEDiagnostic diag : issues.getErrors()) {
						LOG.error("{}", diag);
					}
				}
				
				if(issues.hasWarnings()) {
					for(final MWEDiagnostic diag : issues.getWarnings()) {
						LOG.warn("{}", diag);
					}
				}
				
				if(issues.hasInfos()) {
					for(final MWEDiagnostic diag : issues.getInfos()) {
						LOG.info("{}", diag);
					}
				}
		} //end for
	}

	/**
	 * Stores the model into a file name {@code modelName}.
	 * 
	 * @param modelName Name of the resulting file.
	 */
	private void storeModel(final String modelName) {
		LOG.info("Storing model to {}.", modelName);
	
		try {
			final HashSet<Class<?>> classes = new HashSet<Class<?>>();
			for(final ServletDetector detector : servletDetectors) {
				classes.addAll(detector.getModelExtensions());
			}
			classes.add(Web.class);
			
			final JAXBContext context = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));

			final Marshaller marshaller = context.createMarshaller();
			
			Writer writer = null; 
			
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			
			try { 
			  writer = new FileWriter(modelName); 
			  marshaller.marshal( web, writer ); 
			} catch(final IOException e) {
				
			}
			finally { 
			  try {
				  writer.close();
			  } catch ( Exception e ) {
				  LOG.error("Unable to dump model to {}", modelName, e);
			  } 
			}
		} catch(JAXBException e) {
			LOG.error("Unable to dump model to {}", modelName, e);
		}
	}
	
	public void addServletDetector(final ServletDetector detector) {
		servletDetectors.add(detector);
	}
}
