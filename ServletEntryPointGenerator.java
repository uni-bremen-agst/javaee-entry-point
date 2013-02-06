package soot.jimple.toolkits.javaee;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.common.tools.FileTool;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import soot.BooleanType;
import soot.DoubleType;
import soot.FloatType;
import soot.G;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PhaseOptions;
import soot.PrimType;
import soot.Printer;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.Singletons;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.javaee.model.servlet.Address;
import soot.jimple.toolkits.javaee.model.servlet.Filter;
import soot.jimple.toolkits.javaee.model.servlet.Listener;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.io.WebXMLReader;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.util.Chain;
import soot.util.EscapedWriter;

/**
 * This class drives the generation of a main method that creates and calls
 *   all configured servlets. This is necessary to calculate a correct call
 *   graph with soot.
 *
 * @author Bernhard Berger
 */
public class ServletEntryPointGenerator extends SceneTransformer implements Signatures {
 
	/**
	 * Logging facility.
	 */
	private final static SootLogger LOG = new SootLogger();
	
	/**
	 * For creation of jimple nodes.
	 */
	private final Jimple jimple = Jimple.v();

	/**
	 * @return Whether {@code clazz} is an application class.
	 */
	private static boolean isApplicationClass(final SootClass clazz) {
		return clazz.isApplicationClass();
	}

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
	
	/**
	 * Name of main package.
	 */
	private String mainPackage;

	/**
	 * The type we will create as parameter for the {@code init}-method. Can be
	 *   set with the commandline parameter {@code servlet-config-class}.
	 */
	private SootClass servletConfigType = null;
	
	public ServletEntryPointGenerator(final Singletons.Global g) {
		LOG.setPhase("wjpp.seg");
	}




	private Web web = new Web();

	/**
	 * Loads the {@code web.xml} or fakes it if the corresponding command line
	 *   parameter was given.
	 *   
	 * @todo Implement proper war support for locator.
	 */
	private void loadWebXML() {
		if(considerAllServlets) {
			configureAllServlets();
		} else {
			SourceLocator locator = SourceLocator.v();
			
			for(String part : locator.classPath()) {
				if(!part.endsWith("WEB-INF/classes")) {
					continue;
				}
				
				part = part.substring(0, part.length() - 7) + "web.xml";
				try {
					web = WebXMLReader.readWebXML(new FileInputStream(part));
				} catch (Exception e) {
					LOG.error("Cannot read web.xml.");
				}
			}
		}
	}

	/**
	 * Assumes that all servlets in the application scope are configured and
	 *   creates a fake configuration.
	 */
	private void configureAllServlets() {

	    Collection<SootClass> wsClasses = new ArrayList<SootClass>();
	    Collection<SootMethod> wsMethods = new ArrayList<SootMethod>();

	    Hierarchy cha = Scene.v().getActiveHierarchy();
	    SootClass servletClass = Scene.v().getSootClass(HTTP_SERVLET_CLASS_NAME);

	    for(final SootClass clazz : Scene.v().getApplicationClasses()) {
	        if (!clazz.isConcrete()) //ignore interfaces and abstract classes
	            continue;
	        if (cha.isClassSubclassOf(clazz, servletClass)){//if(isServlet(clazz)) { //for servlets
	            registerServlet(clazz);
	        } else if (clazz.hasTag("VisibilityAnnotationTag")) { //for web services
	            VisibilityAnnotationTag vat = (VisibilityAnnotationTag) clazz.getTag("VisibilityAnnotationTag");
	            for (AnnotationTag at : vat.getAnnotations()) {

	                //Handling @WebService and @WebMethod
	                if ("Ljavax/jws/WebService;".equals(at.getType())){
	                    wsClasses.add(clazz);
	                    Collection<SootClass> interfaces = getClassesAnnotatedWithWebService(
	                            getInterfacesTransitively(clazz));
	                    for (SootMethod sm : clazz.getMethods()){

	                        //Case #1: The annotation is on the class itself
	                        if (getWebMethodAnnotation(sm) != null){
	                            wsMethods.add(sm);
	                            G.v().out.println("Found web method: " + sm);
	                            continue;
	                        }

	                        //Case #2: The annotation is on the interface's method
	                        for (SootClass sc : interfaces){
	                            if (sc.declaresMethod(sm.getSubSignature()) 
	                                    && getWebMethodAnnotation(sc.getMethod(sm.getSubSignature())) != null){
	                                wsMethods.add(sm);
	                                G.v().out.println("Found web method: " + sm);
	                                break; //no need to check the other interfaces
	                            }
	                        }
	                    }

	                // Handling @WebServiceProvider
	                } else if ("Ljavax/jws/WebServiceProvider;".equals(at.getType())){
	                    SootClass providerClass = Scene.v().getSootClass("javax.xml.ws.Provider");
	                    if (Scene.v().getFastHierarchy().canStoreType(clazz.getType(), providerClass.getType())
	                            && clazz.declaresMethodByName("invoke")){
	                        wsMethods.add(clazz.getMethodByName("invoke"));
	                        wsClasses.add(clazz);
	                    }            
	                }
	            }

	        }
	    }

	    if (!wsClasses.isEmpty()){
	        SootClass theClass = synthetizeWSServlet(wsClasses, wsMethods);
	        registerServlet(theClass);
	    }

	}

	
	/**
	 * Filters a collection of classes to keep only those annotated with @WebService
	 * @param classes the classes to filter
	 * @return a non-null collection of classes meeting that criteria, possibly empty
	 */
	private Collection<SootClass> getClassesAnnotatedWithWebService(Collection<SootClass> classes){
	    Collection<SootClass> wsInterfaces = new ArrayList<SootClass>();
	    for (SootClass sc : classes){
	        if (isClassAnnotatedWithWebService(sc))
	            wsInterfaces.add(sc);

	    }
	    return wsInterfaces;
	}

	/**
	 * Checks if a class is annotated with @WebService
	 * @param sc the class to examine
	 * @return <code>true</code> if the annotation is present, <code>false</code> otherwise.
	 */
	private boolean isClassAnnotatedWithWebService(SootClass sc){
	    if (sc.hasTag("VisibilityAnnotationTag")){
	        VisibilityAnnotationTag vatInterface = (VisibilityAnnotationTag) sc.getTag("VisibilityAnnotationTag");
	        for (AnnotationTag atInterface : vatInterface.getAnnotations())
	            if ("Ljavax/jws/WebService;".equals(atInterface.getType()))
	                return true;
	    }
	    return false;
	}

	/**
	 * Gets the @WebMethod annotation, if any
	 * @param sm the method to check
	 * @return <code>null</code> if the annotation is not found, the annotation otherwise.
	 */
	private AnnotationTag getWebMethodAnnotation(SootMethod sm){
	    if (sm.hasTag("VisibilityAnnotationTag")){
	        VisibilityAnnotationTag vat = (VisibilityAnnotationTag) sm.getTag("VisibilityAnnotationTag");
	        for (AnnotationTag at : vat.getAnnotations())
	            if ("Ljavax/jws/WebMethod;".equals(at.getType()))
	                return at;
	    }
	    return null;
	}

	/**
	 * Gets all the interfaces and superinterfaces of a class
	 * @param sc the class to examine
	 * @return a non-null collection of interfaces
	 */
	private Collection<SootClass> getInterfacesTransitively(SootClass sc){
	    final Collection<SootClass> retVal = new ArrayList<SootClass>();
	    for (SootClass interf : sc.getInterfaces()){
	        retVal.add(interf);
	        retVal.addAll(getInterfacesTransitively(interf));
	    }
	    return retVal;
	}
	
	/**
	 * Registers a servlet as if it was declared in web.xml
	 * @param clazz the class
	 */
  private void registerServlet(final SootClass clazz) {
    final Servlet servlet = new Servlet(clazz.getName(), clazz.getName());
    web.getServlets().add(servlet);
    
    final Address address = new Address();
    address.setName(clazz.getName());
    address.setServlet(servlet);
    web.getRoot().getChildren().add(address);
  }
	
  /**
   * Creates a Servlet that only calls the web services
   * @param wsClasses the Web Service classes found
   * @param wsMethods the Web service methods found
   * @return a class
   */
  private SootClass synthetizeWSServlet(Collection<SootClass> wsClasses, Collection<SootMethod> wsMethods){
      JimpleClassGenerator classGen = new JimpleClassGenerator(mainPackage+".WSCaller", HTTP_SERVLET_CLASS_NAME, true);
      final SootClass clazz = classGen.getClazz();

      //create default constructor
      final JimpleBodyGenerator constructor = classGen.method("<init>", Collections.EMPTY_LIST, VoidType.v());
      final Local thisLocal = constructor.local(false, clazz.getType());
      constructor.getUnits().add(jimple.newIdentityStmt(thisLocal, jimple.newThisRef(clazz.getType())));

      final List<Type> parameterTypes = Arrays.asList(
              (Type) scene.getRefType(HTTP_SERVLET_REQUEST_CLASS_NAME),
              (Type) scene.getRefType(HTTP_SERVLET_RESPONSE_CLASS_NAME)
              );
      final JimpleBodyGenerator serviceMethod = classGen.method("doGet", parameterTypes, VoidType.v());
      serviceMethod.setPublic();

      final Local serviceThisLocal = serviceMethod.local(true, clazz.getType());
      final Local requestLocal = serviceMethod.local(true, parameterTypes.get(0));
      final Local responseLocal = serviceMethod.local(true, parameterTypes.get(1));
      final Chain<Unit> units = serviceMethod.getUnits();
      final Map<SootClass, Local> generatedLocals = new HashMap<SootClass, Local>(wsClasses.size());

      //Add this object ref
      units.add(jimple.newIdentityStmt(serviceThisLocal, jimple.newThisRef(clazz.getType())));

      // init parameters
      units.add(jimple.newIdentityStmt(requestLocal, jimple.newParameterRef(parameterTypes.get(0), 0)));
      units.add(jimple.newIdentityStmt(responseLocal, jimple.newParameterRef(parameterTypes.get(1), 1)));


      //Construct the instances
      for (SootClass sc : wsClasses){
          final Local newLocal = serviceMethod.local(false, sc.getType());
          generatedLocals.put(sc, newLocal);
          serviceMethod.createInstance(newLocal, sc);
          units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(newLocal, sc.getMethod("void <init>()").makeRef())));
      }

      final RefType stringType = Scene.v().getSootClass("java.lang.String").getType();

      //Add the calls to the WS methods
      //TODO we need to figure out the parameters and get them from the request
      for (SootMethod sm : wsMethods){

          List<Type> argTypes = sm.getParameterTypes();
          List<Value> arguments = new ArrayList<Value>(argTypes.size());
          for(Type t: argTypes){

              Local paramLocal = serviceMethod.local(false, t);
              if (t instanceof PrimType){
                  if (t instanceof IntType){
                      units.add(jimple.newAssignStmt(paramLocal, IntConstant.v(0)));
                  } else if (t instanceof LongType){
                      units.add(jimple.newAssignStmt(paramLocal, LongConstant.v(0)));
                  } else if (t instanceof FloatType){
                      units.add(jimple.newAssignStmt(paramLocal, FloatConstant.v(0.0f)));
                  } else if (t instanceof DoubleType){
                      units.add(jimple.newAssignStmt(paramLocal, DoubleConstant.v(0.0)));
                  } else if (t instanceof BooleanType){
                      units.add(jimple.newAssignStmt(paramLocal, IntConstant.v(1))); //1 is true
                  }

              } else if (t instanceof RefType){
                  if (t.equals(stringType)){
                      units.add(jimple.newAssignStmt(paramLocal, StringConstant.v("")));
                  } else{
                      units.add(jimple.newAssignStmt(paramLocal, jimple.newNewExpr((RefType)t)));
                      units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(paramLocal, ((RefType) t).getSootClass().getMethod("void <init>()").makeRef())));                
                  }
              }
              arguments.add(paramLocal);//TODO
          }
          units.add(jimple.newInvokeStmt(jimple.newVirtualInvokeExpr(
                  generatedLocals.get(sm.getDeclaringClass()), sm.makeRef(), arguments)));      
      }
      PrintWriter pw = new PrintWriter(new EscapedWriter(new OutputStreamWriter(G.v().out)));
      Printer.v().printTo(clazz, pw);
      pw.close();

      return clazz;
  }
	
	@Override
	protected void internalTransform(final String phaseName, @SuppressWarnings("rawtypes") final Map options) {
		// configure logging
		LOG.setPhase(phaseName);
		//LOG.setMethod(body.getMethod());
		LOG.setOptions(options);
		
		LOG.info("Running " + phaseName + " with options " + options);
		
		mainPackage = PhaseOptions.getString(options, "root-package");
		considerAllServlets = PhaseOptions.getBoolean(options, "consider-all-servlets");
		
		loadWebXML();
		
		final String modelDestination = PhaseOptions.getString(options, "dump-model");
		if(!modelDestination.isEmpty()) {
			storeModel(modelDestination);
		}

		try {
			LOG.info("Processing templates");
			processTemplate(options);
		} catch(final ResourceNotFoundException e) {
			LOG.error("Could not find template file.");
		} catch(final ParseErrorException e) {
			LOG.error("Failed to parse the template.");
		} catch(final MethodInvocationException e) {
			LOG.error("Error while calling Java code from template.");
			e.printStackTrace();
		}
		
		final SootClass sootClass = scene.forceResolve(PhaseOptions.getString(options, "root-package") + "." + PhaseOptions.getString(options, "main-class"), SootClass.BODIES);
		scene.setMainClass(sootClass);
	}

	/**
	 * Sets up and configures the template engine.
	 * 
	 * @return Velocity context.
	 */
	private void processTemplate(@SuppressWarnings("rawtypes") final Map options) {
		final VelocityEngine engine = new VelocityEngine();
		engine.setProperty("resource.loader", "class");
		//engine.setProperty("file.resource.loader.path", "/");
		engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		
		engine.init();
				
		final VelocityContext context = new VelocityContext();

		context.put("root", web);
		context.put("root-package", PhaseOptions.getString(options, "root-package"));
		context.put("main-class", PhaseOptions.getString(options, "main-class"));
		context.put("output-dir", PhaseOptions.getString(options, "output-dir"));	
		context.put("FileTool", FileTool.class);
		context.put("filter-config-impl", PhaseOptions.getString(options, "filter-config-class"));
		context.put("servlet-config-impl", PhaseOptions.getString(options, "servlet-config-class"));
		context.put("servlet-request-impl", PhaseOptions.getString(options, "servlet-request-class"));
		context.put("servlet-response-impl", PhaseOptions.getString(options, "servlet-response-class"));
		
	    final InputStream input = getClass().getClassLoader().getResourceAsStream("soot/jimple/toolkits/javaee/templates/root.vm");
	    if (input == null) {
	        throw new RuntimeException("Template file doesn't exist");           
	    }

	    final InputStreamReader reader = new InputStreamReader(input); 

        if (!engine.evaluate(context, new NullWriter(), "root", reader)) {
            throw new RuntimeException("Failed to convert the template into html.");
        }
	}

	private void storeModel(final String modelName) {
		try {
			final JAXBContext context = JAXBContext.newInstance( Web.class ); 
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
				  LOG.error("Unable to dump model to " + modelName);
			  } 
			}
		} catch(JAXBException e) {
			LOG.error("Unable to dump model to " + modelName);
			LOG.error(e.toString());
		}
	}

	public void loadEntryPoints() {
		// the following code allows us to run in normal mode (consider-all-servlets = false)
		// without using the -process-xxx option. We will parse the web.xml in this early
		// step to load all servlet, filter, listener and so forth classes before the scene
		// is sealed. If consider-all-servlets is specified we will discard the container
		// model during the setup and since the -process-xxx option is necessary in this
		// case these classes will be loaded nevertheless.
		loadWebXML();
		//loadClassesFromModel();
	}
}
