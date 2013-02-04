package soot.jimple.toolkits.javaee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.BooleanType;
import soot.DoubleType;
import soot.FastHierarchy;
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
import soot.jimple.toolkits.javaee.model.servlet.Address;
import soot.jimple.toolkits.javaee.model.servlet.Filter;
import soot.jimple.toolkits.javaee.model.servlet.Listener;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.io.WebXMLReader;
import soot.jimple.toolkits.typing.ClassHierarchy;
import soot.options.Options;
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
	 * Subsignature of the {@code destroy} wrapper method.
	 */
	private static final String DESTROY_METHOD_SUBSIGNATURE = "void destroy()";

	/**
	 * Subsignature of the {@code init} wrapper method.
	 */
	private static final String INIT_METHOD_SUBSIGNATURE = "void init(javax.servlet.ServletConfig)";
    
	/**
	 * Logging facility.
	 */
	private final static SootLogger LOG = new SootLogger();
	
	/**
	 * Subsignature of the {@code service} wrapper method.
	 */
	private static final String SERVICE_METHOD_SUBSIGNATURE = "void service(javax.servlet.ServletRequest,javax.servlet.ServletResponse)";

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
	 * Jimple factory.
	 */
	private final Jimple jimple = Jimple.v();
	
	/**
	 * Generator for the class that contains the main method.
	 */
	private final MainClassGenerator mainGenerator = new MainClassGenerator();

	/**
	 * The package for all generated classes. Can be set with the
	 *   commandline parameter {@code main-package}.
	 */
	private String mainPackage;

	/**
	 * Scene for lookup of classes.
	 */
	private final Scene scene = Scene.v();

	/**
	 * The type we will create as parameter for the {@code init}-method. Can be
	 *   set with the commandline parameter {@code servlet-config-class}.
	 */
	private SootClass servletConfigType = null;
	
	public ServletEntryPointGenerator(final Singletons.Global g) {
		// We need some classes at least at SIGNATURE-level for code generation.
		// Since we cannot be sure that the class is resolved correctly by Soot
		// (maybe the program we analyze does not need the class) we have to
		// resolve it manually. Doing the resolution within the constructor is
		// safe because the class is instantiated after the class path is set up
		// and before the scene is sealed.
		scene.addBasicClass(RANDOM_CLASS_NAME, SootClass.SIGNATURES);
		scene.addBasicClass(HTTP_SERVLET_REQUEST_CLASS_NAME, SootClass.HIERARCHY);
		scene.addBasicClass(HTTP_SERVLET_RESPONSE_CLASS_NAME, SootClass.HIERARCHY);
		
		LOG.setPhase("wjpp.seg");
	}


	private void loadClassesFromModel() {
		for(final Filter filter : this.web.getFilters()) {
			LOG.debug("Loading " + filter.getClazz());
			scene.forceResolve(filter.getClazz(), SootClass.SIGNATURES);
		}

		for(final Listener listener : this.web.getListeners()) {
			LOG.debug("Loading " + listener.getClazz());
			scene.forceResolve(listener.getClazz(), SootClass.SIGNATURES);
		}

		for(final Servlet servlet : this.web.getServlets()) {
			LOG.debug("Loading " + servlet.getClazz());
			scene.forceResolve(servlet.getClazz(), SootClass.SIGNATURES);
		}
	}

	/**
	 * Checks if {@code clazz} inherits from {@code baseClassName}.
	 */
	private boolean classExtends(SootClass clazz, final String baseClassName) {
		while(clazz != null) {
			if(clazz.isPhantom()) {
				LOG.warn("Found phantom class. Maybe it is impossible to detect all servlets.");
			}
			
			if(clazz.getName().equals(baseClassName)) {
				return true;
			}
			
			clazz = clazz.hasSuperclass() ? clazz.getSuperclass() : null;
		}
		
		return false;
	}

	/**
	 * Creates the wrapper for a servlet. The wrapper consists of three static
	 *   methods, each wrapping one of the life cycle states.
	 * 
	 * <pre>
	 * {@code
	 * public class [mainPackage].[clazz] {
	 * 
	 *   private [clazz] instance;
	 *   
	 *   public static void init() {
	 *     ...
	 *   }
	 *   
	 *   public static void service(...) {
	 *     ...
	 *   }
	 *   
	 *   public static void destroy() {
	 *     ...
	 *   }
	 * }
	 * </pre>
	 * 
	 * @param clazz The servlet class.
	 */
	private void createServletWrapper(final SootClass clazz) {
		final JimpleClassGenerator classGenerator = new JimpleClassGenerator(mainPackage + "." + clazz.getName(), OBJECT_CLASS_NAME, true);
		
		final SootField instanceField = classGenerator.field("instance", clazz);
		
		instanceField.setModifiers(instanceField.getModifiers() | Modifier.STATIC | Modifier.PRIVATE);
		
		classGenerator.addToClInit(jimple.newAssignStmt(jimple.newStaticFieldRef(instanceField.makeRef()), NullConstant.v()));
		
		final SootMethod initMethod = generateInit(classGenerator, clazz, instanceField);
		final SootMethod servMethod = generateService(classGenerator, clazz, instanceField);
		final SootMethod destMethod = generateDestroy(classGenerator, clazz, instanceField);
		
		mainGenerator.registerServlet(clazz, initMethod, servMethod, destMethod);
	}

	/**
	 * Finds a method with the given {@code subsignature}. Therefore, the
	 *   method starts at {@code clazz} and visits all super classes until
	 *   a matching method is found.
	 * 
	 * @param clazz Class to start.
	 * @param subsignature Methods signature we are looking for.
	 * 
	 * @return A method or {@code null}.
	 */
	private SootMethod findMethod(SootClass clazz, final String subsignature) {
		while(clazz != null) {
			if(clazz.declaresMethod(subsignature)) {
				return clazz.getMethod(subsignature);
			}
			
			clazz = clazz.hasSuperclass() ? clazz.getSuperclass() : null;
		}

		return null;
	}

	/**
	 * Looks for the class specified by {@code className} in the Soot scene. If
	 *   it cannot be found a class stub will be generated. The class will
	 *   implement {@code interfaceClass} and has got a constructor without
	 *   arguments.
	 *   
	 * @param className Name of the class we are looking for.
	 * @param interfaceClass Name of the interface the class has to extend.
	 * 
	 * @return A valid class, implementing {@code interfaceClass} 
	 */
	private SootClass findOrCreate(final String className, final String interfaceClass) {
		if(scene.containsType(className)) {
			return scene.getSootClass(className);
		} else {
			final JimpleClassGenerator classGenerator = new JimpleClassGenerator(className, OBJECT_CLASS_NAME, false);
			classGenerator.implement(interfaceClass);
			
			final List<Type> parameterTypes = Collections.emptyList();
			classGenerator.method(SootMethod.constructorName, parameterTypes, soot.VoidType.v());
			
			return classGenerator.getClazz();
		}
	}

	/**
	 * Generates the destroy code. Firstly, the servlet's destroy method is
	 *   called. Secondly, the field is set to {@code null}. 
	 *   
	 * <pre>
	 * {@code
	 * public static void destroy() {
	 *   [clazz] local = new [clazz]();
	 *   
	 *   local.destroy();
	 *   
	 *   local = null;
	 *   this.[instanceField] = local;
	 * }
	 * </pre>
	 * 
	 * @param classGenerator The surrounding class of the {@code destroy}-method.
	 * @param clazz The servlet type.
	 * @param instanceField The field that holds the servlet instance.
	 * 
	 * @return The generated method.	 */
	private SootMethod generateDestroy(final JimpleClassGenerator classGenerator, final SootClass clazz, SootField instanceField) {
		final List<Type> parameterTypes = Collections.emptyList();

		final JimpleBodyGenerator destroyMethod = classGenerator.method("destroy", parameterTypes, VoidType.v());
		destroyMethod.setStatic();
		destroyMethod.setPublic();
		
		final Local servletLocal = destroyMethod.local(false, clazz.getType());

		final Chain<Unit> units = destroyMethod.getUnits();
		
	    // assign from field
	    units.add(jimple.newAssignStmt(servletLocal, jimple.newStaticFieldRef(instanceField.makeRef())));
	    
	    // call servlet.destroy
	    SootMethod callee = findMethod(clazz, DESTROY_METHOD_SUBSIGNATURE);
	    units.add(jimple.newInvokeStmt(jimple.newVirtualInvokeExpr(servletLocal, callee.makeRef())));
	    
	    // assign null
	    units.add(jimple.newAssignStmt(servletLocal, NullConstant.v()));
	    
	    // store null value to field
	    units.add(jimple.newAssignStmt(jimple.newStaticFieldRef(instanceField.makeRef()), servletLocal));
		
		return destroyMethod.getMethod();
	}
	

	/**
	 * Generates the initialization code. First, the servlet will be created
	 *   and the {@code init}-method will be  called. The generated code will
	 *   look like the following:
	 *
	 * <pre>
	 * {@code
	 * public static void init() {
	 *   ServletConfig config = new ServletConfig();
	 *   [clazz] local = new [clazz]();
	 *   
	 *   local.init(config);
	 *   
	 *   this.[instanceField] = local;
	 * }
	 * </pre>
	 * 
	 * @param classGenerator The surrounding class of the {@code init}-method.
	 * @param clazz The servlet type.
	 * @param instanceField The field that holds the servlet instance.
	 * 
	 * @return The generated method.
	 */
	private SootMethod generateInit(final JimpleClassGenerator classGenerator, final SootClass clazz, final SootField instanceField) {
		final List<Type> parameterTypes = Collections.emptyList();

		final JimpleBodyGenerator initMethod = classGenerator.method("init", parameterTypes, VoidType.v());
		
		initMethod.setStatic();
		initMethod.setPublic();
		
		final Local servletLocal = initMethod.local(false, clazz.getType());
		final Local paramLocal = initMethod.local(false, servletConfigType.getType());

		final Chain<Unit> units = initMethod.getUnits();
		
		// create parameter
		initMethod.createInstance(paramLocal, servletConfigType);
	    units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(paramLocal, servletConfigType.getMethod(EMPTY_CONSTRUCTOR_SUBSIGNATURE).makeRef())));

		// create servlet instance
	    initMethod.createInstance(servletLocal, clazz);
	    units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(servletLocal, clazz.getMethod(EMPTY_CONSTRUCTOR_SUBSIGNATURE).makeRef())));
	    
	    // call to servlet.init
	    SootMethod callee = findMethod(clazz, INIT_METHOD_SUBSIGNATURE);
	    units.add(jimple.newInvokeStmt(jimple.newVirtualInvokeExpr(servletLocal, callee.makeRef(), paramLocal)));
	    
	    // assign to field
	    units.add(jimple.newAssignStmt(jimple.newStaticFieldRef(instanceField.makeRef()), servletLocal));

	    return initMethod.getMethod();
	}

	/**
	 * Generates the service code. The {@code service}-method of the servlet
	 *   will be called . The generated code will look like the following:
	 *
	 * <pre>
	 * {@code
	 * public static void service(HttpServletRequest req, HttpServletResponse resp) {
	 *   [clazz] local = this.[instanceField];
	 *   
	 *   local.service(req, resp);
	 * }
	 * </pre>
	 * 
	 * @param classGenerator The surrounding class of the {@code init}-method.
	 * @param clazz The servlet type.
	 * @param instanceField The field that holds the servlet instance.
	 * 
	 * @return The generated method.
	 */
	private SootMethod generateService(final JimpleClassGenerator classGenerator, final SootClass clazz, SootField instanceField) {
		final List<Type> parameterTypes = new ArrayList<Type>(2);
		parameterTypes.add(scene.getRefType(HTTP_SERVLET_REQUEST_CLASS_NAME));
		parameterTypes.add(scene.getRefType(HTTP_SERVLET_RESPONSE_CLASS_NAME));
		final JimpleBodyGenerator serviceMethod = classGenerator.method("service", parameterTypes, VoidType.v());

		serviceMethod.setStatic();
		serviceMethod.setPublic();
		
		final Local requestLocal = serviceMethod.local(true, parameterTypes.get(0));
		final Local responseLocal = serviceMethod.local(true, parameterTypes.get(1));
		
		final Local servletLocal = serviceMethod.local(false, clazz.getType());

		final Chain<Unit> units = serviceMethod.getUnits();
		
		// init parameters
		units.add(jimple.newIdentityStmt(requestLocal, jimple.newParameterRef(parameterTypes.get(0), 0)));
		units.add(jimple.newIdentityStmt(responseLocal, jimple.newParameterRef(parameterTypes.get(1), 1)));
		
	    // assign from field
	    units.add(jimple.newAssignStmt(servletLocal, jimple.newStaticFieldRef(instanceField.makeRef())));
	    
	    // call to servlet.service
	    SootMethod callee = findMethod(clazz, SERVICE_METHOD_SUBSIGNATURE);
	    units.add(jimple.newInvokeStmt(jimple.newVirtualInvokeExpr(servletLocal, callee.makeRef(), requestLocal, responseLocal)));
	    
		return serviceMethod.getMethod();
	}

	/**
	 * Does initial setup if not already done. This cannot be done in the
	 *   constructor since we need the command-line options.
	 *   
	 * @param options Command line options.
	 */
	private void initialSetup(@SuppressWarnings("rawtypes") final Map options) {
		final SootClass servletRequestClass = findOrCreate(PhaseOptions.getString(options, "servlet-request-class"), HTTP_SERVLET_REQUEST_CLASS_NAME);
		final SootClass servletResponseClass = findOrCreate(PhaseOptions.getString(options, "servlet-response-class"), HTTP_SERVLET_RESPONSE_CLASS_NAME);
		final String servletConfigTypeName = PhaseOptions.getString(options, "servlet-config-class");
        final String mainClass = PhaseOptions.getString(options, "main-class");

		servletConfigType = findOrCreate(servletConfigTypeName, "javax.servlet.ServletConfig");
		considerAllServlets = PhaseOptions.getBoolean(options, "consider-all-servlets");
		mainPackage = PhaseOptions.getString(options, "root-package");
		
		mainGenerator.start(mainPackage, mainClass, servletRequestClass, servletResponseClass);
		
		loadWebXML();
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
          if ("Ljavax/jws/WebService;".equals(at.getType())&& !clazz.isInterface()){
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
    
    final Local requestLocal = serviceMethod.local(true, parameterTypes.get(0));
    final Local responseLocal = serviceMethod.local(true, parameterTypes.get(1));
    final Chain<Unit> units = serviceMethod.getUnits();
    final Map<SootClass, Local> generatedLocals = new HashMap<SootClass, Local>(wsClasses.size());
    
    
    
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
          units.add(jimple.newAssignStmt(paramLocal, jimple.newNewExpr((RefType)t)));
          units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(paramLocal, ((RefType) t).getSootClass().getMethod("void <init>()").makeRef())));
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
	
	/**
	 * @todo Should we use GenericServlet?
	 * 
	 * @return {@code true} if {@code clazz} is a servlet.
	 */
	private boolean isServlet(final SootClass clazz) {
		return classExtends(clazz, HTTP_SERVLET_CLASS_NAME);
	}

	@Override
	protected void internalTransform(final String phaseName, @SuppressWarnings("rawtypes") final Map options) {
		// configure logging
		LOG.setPhase(phaseName);
		//LOG.setMethod(body.getMethod());
		LOG.setOptions(options);
		
		LOG.info("Running " + phaseName);
		
		initialSetup(options);

		for(final Servlet servlet : web.getServlets()) {
			LOG.info("Processing servlet " + servlet.getName());

			SootClass clazz = scene.getSootClass(servlet.getClazz());

			if(!isServlet(clazz)) {
				LOG.warn("The servlet named " + servlet.getName() + " does not inherit from " + HTTP_SERVLET_CLASS_NAME + " we will skip it.");
				continue;
			}
			createServletWrapper(clazz);
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
		loadClassesFromModel();

		@SuppressWarnings("rawtypes")
		final Map options = PhaseOptions.v().getPhaseOptions("wjpp.seg");
		final String servletRequestClass = PhaseOptions.getString(options, "servlet-request-class");
		final String servletResponseClass = PhaseOptions.getString(options, "servlet-response-class");
		final String servletConfigTypeName = PhaseOptions.getString(options, "servlet-config-class");
 
		scene.forceResolve(servletRequestClass, SootClass.SIGNATURES);
		scene.forceResolve(servletResponseClass, SootClass.SIGNATURES);
		scene.forceResolve(servletConfigTypeName, SootClass.SIGNATURES);
	}
}
