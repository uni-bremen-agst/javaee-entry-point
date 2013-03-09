package soot.jimple.toolkits.javaee.detectors;

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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.BooleanType;
import soot.DoubleType;
import soot.FloatType;
import soot.G;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PhaseOptions;
import soot.PrimType;
import soot.Printer;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.javaee.JimpleBodyGenerator;
import soot.jimple.toolkits.javaee.JimpleClassGenerator;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.http.ServletSignatures;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.util.Chain;
import soot.util.EscapedWriter;

public class WebServiceDetector extends AbstractServletDetector implements
		ServletSignatures {

	public static final String GENERATED_CLASS_NAME = "WSCaller";

    /**
	 * For creation of jimple nodes.
	 */
	private final Jimple jimple = Jimple.v();

	private final Scene scene = Scene.v();

	private final Collection<SootClass> wsClasses = new ArrayList<SootClass>();
	private final Collection<SootMethod> wsMethods = new ArrayList<SootMethod>();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void detectFromSource(Web web) {
	    findWSInApplication();

	    if (!wsClasses.isEmpty()) {
	        SootClass theClass = synthetizeWSServlet();
	        HttpServletDetector.registerGenericServlet(web, theClass);
	    }
	}


	public SootClass detectFromSource() {
	    findWSInApplication();

	    if (!wsClasses.isEmpty()) {
	        return synthetizeWSServletMain();
	        // main
	    }
	    return null;
	}


    private void findWSInApplication() {
        for (final SootClass clazz : Scene.v().getApplicationClasses()) {
            if (!clazz.isConcrete()) // ignore interfaces and abstract classes
                continue;

            if (clazz.hasTag("VisibilityAnnotationTag")) { // for web services
                VisibilityAnnotationTag vat = (VisibilityAnnotationTag) clazz
                        .getTag("VisibilityAnnotationTag");
                for (AnnotationTag at : vat.getAnnotations()) {

                    // Handling @WebService and @WebMethod
                    if ("Ljavax/jws/WebService;".equals(at.getType())) {
                        wsClasses.add(clazz);
                        Collection<SootClass> interfaces = getClassesAnnotatedWithWebService(getInterfacesTransitively(clazz));
                        for (SootMethod sm : clazz.getMethods()) {

                            // Case #1: The annotation is on the class itself
                            if (getWebMethodAnnotation(sm) != null) {
                                wsMethods.add(sm);
                                G.v().out.println("Found web method: " + sm);
                                continue;
                            }

                            // Case #2: The annotation is on the interface's
                            // method
                            for (SootClass sc : interfaces) {
                                if (sc.declaresMethod(sm.getSubSignature())
                                        && getWebMethodAnnotation(sc
                                                .getMethod(sm.getSubSignature())) != null) {
                                    wsMethods.add(sm);
                                    G.v().out
                                            .println("Found web method: " + sm);
                                    break; // no need to check the other
                                            // interfaces
                                }
                            }
                        }

                        // Handling @WebServiceProvider
                    } else if ("Ljavax/jws/WebServiceProvider;".equals(at
                            .getType())) {
                        SootClass providerClass = Scene.v().getSootClass(
                                "javax.xml.ws.Provider");
                        if (Scene
                                .v()
                                .getFastHierarchy()
                                .canStoreType(clazz.getType(),
                                        providerClass.getType())
                                && clazz.declaresMethodByName("invoke")) {
                            wsMethods.add(clazz.getMethodByName("invoke"));
                            wsClasses.add(clazz);
                        }
                    }
                }

            }
        }
    }
	
	/**
	 * Filters a collection of classes to keep only those annotated with @WebService
	 * 
	 * @param classes
	 *            the classes to filter
	 * @return a non-null collection of classes meeting that criteria, possibly
	 *         empty
	 */
	private Collection<SootClass> getClassesAnnotatedWithWebService(
			Collection<SootClass> classes) {
		Collection<SootClass> wsInterfaces = new ArrayList<SootClass>();
		for (SootClass sc : classes) {
			if (isClassAnnotatedWithWebService(sc))
				wsInterfaces.add(sc);

		}
		return wsInterfaces;
	}

	/**
	 * Checks if a class is annotated with @WebService
	 * 
	 * @param sc
	 *            the class to examine
	 * @return <code>true</code> if the annotation is present,
	 *         <code>false</code> otherwise.
	 */
	private boolean isClassAnnotatedWithWebService(SootClass sc) {
		if (sc.hasTag("VisibilityAnnotationTag")) {
			VisibilityAnnotationTag vatInterface = (VisibilityAnnotationTag) sc
					.getTag("VisibilityAnnotationTag");
			for (AnnotationTag atInterface : vatInterface.getAnnotations())
				if ("Ljavax/jws/WebService;".equals(atInterface.getType()))
					return true;
		}
		return false;
	}

	/**
	 * Gets the @WebMethod annotation, if any
	 * 
	 * @param sm
	 *            the method to check
	 * @return <code>null</code> if the annotation is not found, the annotation
	 *         otherwise.
	 */
	private AnnotationTag getWebMethodAnnotation(SootMethod sm) {
		if (sm.hasTag("VisibilityAnnotationTag")) {
			VisibilityAnnotationTag vat = (VisibilityAnnotationTag) sm
					.getTag("VisibilityAnnotationTag");
			for (AnnotationTag at : vat.getAnnotations())
				if ("Ljavax/jws/WebMethod;".equals(at.getType()))
					return at;
		}
		return null;
	}

	/**
	 * Gets all the interfaces and superinterfaces of a class
	 * 
	 * @param sc
	 *            the class to examine
	 * @return a non-null collection of interfaces
	 */
	private Collection<SootClass> getInterfacesTransitively(SootClass sc) {
		final Collection<SootClass> retVal = new ArrayList<SootClass>();
		for (SootClass interf : sc.getInterfaces()) {
			retVal.add(interf);
			retVal.addAll(getInterfacesTransitively(interf));
		}
		return retVal;
	}

    /**
     * Creates a Servlet that only calls the web services
     * 
     * @return a class
     */
    @SuppressWarnings("deprecation")
    private SootClass synthetizeWSServlet() {
        JimpleClassGenerator classGen = new JimpleClassGenerator(PhaseOptions.getString(options, "root-package") + "."
                + GENERATED_CLASS_NAME, HTTP_SERVLET_CLASS_NAME, true);
        final SootClass clazz = classGen.getClazz();

        final JimpleBodyGenerator staticInit = classGen.method("<clinit>", Collections.<Type> emptyList(), VoidType.v());

        Map<SootClass, SootField> generatedFields = new HashMap<SootClass, SootField>();
        for (SootClass sc : wsClasses) {
            SootField sf = classGen.field("ws" + sc.getShortName(), sc);
            generatedFields.put(sc, sf);
            sf.setModifiers(Modifier.STATIC | Modifier.FINAL | Modifier.PUBLIC);
            Local sfLocal = staticInit.local(false, sc.getType());
            staticInit.createInstance(sfLocal, sc);
            staticInit.getUnits().addLast(
                    jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(sfLocal, sc.getMethod("void <init>()").makeRef())));
            staticInit.getUnits().addLast(jimple.newAssignStmt(jimple.newStaticFieldRef(sf.makeRef()), sfLocal));
        }

        // create default constructor
        final JimpleBodyGenerator constructor = classGen.method("<init>", Collections.<Type> emptyList(), VoidType.v());
        final Local thisLocal = constructor.local(false, clazz.getType());
        constructor.getUnits().add(jimple.newIdentityStmt(thisLocal, jimple.newThisRef(clazz.getType())));

        // Save to the same dir as the templates
        try {
            File dir = new File((String) options.get("output-dir"));
            if (!dir.exists())
                dir.mkdir();
            PrintWriter pw = new PrintWriter(new File(dir, clazz.getName() + ".jimple"));
            Printer.v().printTo(clazz, pw);
            pw.close();
        } catch (IOException e) {
            logger.error("Unable to export WSCaller", e);
        }

        return clazz;
    }

    /**
     * Creates a Servlet that only calls the web services and offers a main method
     * 
     * @return a class
     */
    @SuppressWarnings("deprecation")
    private SootClass synthetizeWSServletMain() {
        JimpleClassGenerator classGen = new JimpleClassGenerator(PhaseOptions.getString(options, "root-package") + "."
                + GENERATED_CLASS_NAME, HTTP_SERVLET_CLASS_NAME, true);
        final SootClass clazz = classGen.getClazz();

        final JimpleBodyGenerator staticInit = classGen.method("<clinit>", Collections.<Type> emptyList(), VoidType.v());

        Map<SootClass, SootField> generatedFields = new HashMap<SootClass, SootField>();
        for (SootClass sc : wsClasses) {
            SootField sf = classGen.field("ws" + sc.getShortName(), sc);
            generatedFields.put(sc, sf);
            sf.setModifiers(Modifier.STATIC | Modifier.FINAL | Modifier.PUBLIC);
            Local sfLocal = staticInit.local(false, sc.getType());
            staticInit.createInstance(sfLocal, sc);
            staticInit.getUnits().addLast(
                    jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(sfLocal, sc.getMethod("void <init>()").makeRef())));
            staticInit.getUnits().addLast(jimple.newAssignStmt(jimple.newStaticFieldRef(sf.makeRef()), sfLocal));
        }

        // create default constructor
        final JimpleBodyGenerator constructor = classGen.method("<init>", Collections.<Type> emptyList(), VoidType.v());
        final Local thisLocal = constructor.local(false, clazz.getType());
        constructor.getUnits().add(jimple.newIdentityStmt(thisLocal, jimple.newThisRef(clazz.getType())));

        List<Type> parameterTypes = Arrays.asList((Type) scene.getRefType("java.lang.String").getArrayType());

        final JimpleBodyGenerator serviceMethod = classGen.method("main", parameterTypes, VoidType.v());
        serviceMethod.setPublic();
        serviceMethod.setStatic();

        final Local serviceThisLocal = serviceMethod.local(true, clazz.getType());
        final Local requestLocal = serviceMethod.local(true, parameterTypes.get(0));
        final Local responseLocal = serviceMethod.local(true, parameterTypes.get(1));
        final Chain<Unit> units = serviceMethod.getUnits();
        final Map<SootClass, Local> generatedLocals = new HashMap<SootClass, Local>(wsClasses.size());

        // Add this object ref
        units.add(jimple.newIdentityStmt(serviceThisLocal, jimple.newThisRef(clazz.getType())));

        // init parameters
        units.add(jimple.newIdentityStmt(requestLocal, jimple.newParameterRef(parameterTypes.get(0), 0)));
        units.add(jimple.newIdentityStmt(responseLocal, jimple.newParameterRef(parameterTypes.get(1), 1)));

        for (SootClass sc : wsClasses) {
            final Local newLocal = serviceMethod.local(false, sc.getType());
            generatedLocals.put(sc, newLocal);
            units.add(jimple.newAssignStmt(newLocal, jimple.newStaticFieldRef(generatedFields.get(sc).makeRef())));
        }

        final RefType stringType = Scene.v().getSootClass("java.lang.String").getType();

        // Add the calls to the WS methods
        // TODO we need to figure out the parameters and get them from the
        // request

        for (SootMethod sm : wsMethods) {

            List<Type> argTypes = sm.getParameterTypes();
            List<Value> arguments = new ArrayList<Value>(argTypes.size());
            for (Type t : argTypes) {

                Local paramLocal = serviceMethod.local(false, t);
                if (t instanceof PrimType) {
                    if (t instanceof IntType) {
                        units.add(jimple.newAssignStmt(paramLocal, IntConstant.v(0)));
                    } else if (t instanceof LongType) {
                        units.add(jimple.newAssignStmt(paramLocal, LongConstant.v(0)));
                    } else if (t instanceof FloatType) {
                        units.add(jimple.newAssignStmt(paramLocal, FloatConstant.v(0.0f)));
                    } else if (t instanceof DoubleType) {
                        units.add(jimple.newAssignStmt(paramLocal, DoubleConstant.v(0.0)));
                    } else if (t instanceof BooleanType) {
                        units.add(jimple.newAssignStmt(paramLocal, IntConstant.v(1))); // 1
                                                                                       // is
                                                                                       // true
                    }

                } else if (t instanceof RefType) {
                    if (t.equals(stringType)) {
                        units.add(jimple.newAssignStmt(paramLocal, StringConstant.v("")));
                    } else {
                        units.add(jimple.newAssignStmt(paramLocal, jimple.newNewExpr((RefType) t)));
                        units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(paramLocal,
                                ((RefType) t).getSootClass().getMethod("void <init>()").makeRef())));
                    }
                }
                arguments.add(paramLocal);// TODO
            }
            units.add(jimple.newInvokeStmt(jimple.newVirtualInvokeExpr(generatedLocals.get(sm.getDeclaringClass()), sm.makeRef(), arguments)));
        }
        // Save to the same dir as the templates
        try {
            File dir = new File((String) options.get("output-dir"));
            if (!dir.exists())
                dir.mkdir();
            PrintWriter pw = new PrintWriter(new File(dir, clazz.getName() + ".jimple"));
            Printer.v().printTo(clazz, pw);
            pw.close();
        } catch (IOException e) {
            logger.error("Unable to export WSCaller", e);
        }

        logger.info("Generated class calling the web services: {}", clazz.getJavaStyleName());
        return clazz;
    }

	@Override
	public void detectFromConfig(Web web) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Class<?>> getModelExtensions() {
		return Collections.emptyList();
	}

	@Override
	public String getTemplateFile() {
		return null;
	}

	@Override
	public boolean isXpandTemplate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getCheckFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTemplateFiles() {
		// TODO Auto-generated method stub
		return null;
	}

}
