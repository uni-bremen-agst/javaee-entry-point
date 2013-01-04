package soot.jimple.toolkits.javaee;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.ArrayType;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.util.Chain;

/**
 * This class generates the class containing the {@code main}-method that
 *   calls all existing servlets.
 *   
 * @author Bernhard Berger
 */
public final class MainClassGenerator implements Signatures {
	private static final Scene scene = Scene.v();

	/**
	 * Class generator for the main class.
	 */
	private JimpleClassGenerator classGenerator = null;
	
	/**
	 * Destroy method body generator.
	 */
	private JimpleBodyGenerator destroyMethodGen;

	/**
	 * The exit unit terminates the service-loop. 
	 */
	private Unit exitUnit;

	/**
	 * Init method body generator.
	 */
	private JimpleBodyGenerator initGenerator;

	/**
	 * Jimple instance for creating new jimple statements.
	 */
	private final Jimple jimple = Jimple.v();

	/**
	 * Start of loop.
	 */
	private Unit loopStart;

	/**
	 * Local containing the random value
	 */
	private Local randValueLocal;

	/**
	 * ServletRequest local.
	 */
	private Local requestLocal;
	
	/**
	 * ServletResponse local.
	 */
	private Local responseLocal;
	
	/**
	 * Generator of service method.
	 */
	private JimpleBodyGenerator serviceGenerator;

	/**
	 * Maps call of service wrapper method to unit where call starts.
	 */
	private Map<SootMethod, Unit> serviceMethods = new HashMap<SootMethod, Unit>();
	
	/**
	 * Current switch statement.
	 */
	private Unit switchUnit = jimple.newNopStmt();
	
	/**
	 * @return SootClass of {@code java.util.Random}.
	 */
	private SootClass getRandomClass() {
		return scene.getSootClass(RANDOM_CLASS_NAME);
	}

	public void registerServlet(final SootClass clazz, final SootMethod initMethod, final SootMethod serviceMethod, final SootMethod destroyMethod) {
		// append call to init method
		initGenerator.getUnits().add(jimple.newInvokeStmt(jimple.newStaticInvokeExpr(initMethod.makeRef())));
		
		// append call to destroy method
		destroyMethodGen.getUnits().add(jimple.newInvokeStmt(jimple.newStaticInvokeExpr(destroyMethod.makeRef())));
		
		// append call to service method
		final Chain<Unit> units = serviceGenerator.getUnits();
		units.add(jimple.newInvokeStmt(jimple.newStaticInvokeExpr(serviceMethod.makeRef(), requestLocal, responseLocal)));
		serviceMethods.put(serviceMethod, units.getLast());
		units.add(jimple.newGotoStmt(loopStart));
		
		Unit newSwitchStmt = jimple.newTableSwitchStmt(randValueLocal, 0, serviceMethods.size() - 1, new LinkedList<Unit>(serviceMethods.values()), exitUnit);
		
		units.swapWith(switchUnit, newSwitchStmt);
		
		switchUnit = newSwitchStmt;
	}
	
	/**
	 * The start method sets up the main class generator and generates the
	 *   skeleton of the class. The resulting class will look like the
	 *   following template:
	 *   
	 * <pre>
	 * {@code
	 * public class [packageName].[className] {
	 *   
	 *   public static void init() {
	 *     &lt;for servlet in registeredServlets&gt;
	 *     servlet.init();
	 *     &lt;/for&gt;
	 *   }
	 *   
	 *   public static void service() {
	 *     Random random = new Random();
	 *     
	 *     while(true) {
	 *       int randomValue = random.nextInt();
	 *       HttpServletRequest req = new [requestClass]();
	 *       HttpServletResponse resp = new [responseClass]();
	 *       
	 *       switch(randomValue) {
	 *         &lt;for index, servlet in registeredServlets&gt;
	 *         case index:
	 *           servlet.service(req, resp);
	 *           break;
	 *         &lt;/for&gt;
	 *         
	 *         default:
	 *           return;
	 *       }
	 *     }
	 *   }
	 *   
	 *   public static void destroy() {
	 *     &lt;for servlet in registeredServlets&gt;
	 *     servlet.destroy();
	 *     &lt;/for&gt;
	 *   }
	 *   
	 *   public static void main(final String []args) {
	 *     init();
	 *     service();
	 *     destroy();
	 *   }
	 * }
	 *}</pre>
	 * 
	 * @param packageName Package for class generation.
	 * @param className Name of generated class.
	 * @param requestClass Type of servlet requests.
	 * @param responseClass Type of servlet responses.
	 */
	public void start(final String packageName, final String className, final SootClass requestClass, final SootClass responseClass) {
		final List<Type> noParameters = Collections.emptyList();

		classGenerator = new JimpleClassGenerator(packageName + "." + className, OBJECT_CLASS_NAME, true);
		initGenerator = classGenerator.method("init", noParameters, VoidType.v());
		serviceGenerator = classGenerator.method("service", noParameters, VoidType.v());
		destroyMethodGen = classGenerator.method("destroy", noParameters, VoidType.v());
		
		initGenerator.setStatic();
		serviceGenerator.setStatic();
		destroyMethodGen.setStatic();
		
		generateMain();
		
		generateServiceSkeleton(requestClass, responseClass);
	}

	/**
	 * Generates the skeleton of the service method.
	 * 
	 * @param requetClass Type of servlet requests.
	 * @param responseClass Type of servlet responses.
	 */
	private void generateServiceSkeleton(final SootClass requestClass, final SootClass responseClass) {
		final SootClass randomClass = getRandomClass();
		
		final Chain<Unit> units = serviceGenerator.getUnits();
		requestLocal = serviceGenerator.local(false, scene.getRefType(HTTP_SERVLET_REQUEST_CLASS_NAME));
		responseLocal = serviceGenerator.local(false, scene.getRefType(HTTP_SERVLET_RESPONSE_CLASS_NAME));
		randValueLocal = serviceGenerator.local(false, IntType.v());
		final Local randLocal = serviceGenerator.local(false, randomClass.getType());
		
		serviceGenerator.createInstance(randLocal, randomClass);
	    units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(randLocal, randomClass.getMethod("void <init>()").makeRef())));

	    units.add(jimple.newAssignStmt(randValueLocal, jimple.newVirtualInvokeExpr(randLocal, randomClass.getMethod("int nextInt()").makeRef())));		
	    loopStart = units.getLast();

	    serviceGenerator.createInstance(requestLocal, requestClass);
	    units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(requestLocal, requestClass.getMethod(EMPTY_CONSTRUCTOR_SUBSIGNATURE).makeRef())));
	    
	    serviceGenerator.createInstance(responseLocal, responseClass);
	    units.add(jimple.newInvokeStmt(jimple.newSpecialInvokeExpr(responseLocal, responseClass.getMethod(EMPTY_CONSTRUCTOR_SUBSIGNATURE).makeRef())));

	    units.add(switchUnit);
	    
	    units.add(jimple.newReturnVoidStmt());
	    exitUnit = units.getLast();
	}

	/**
	 * Generates the main method.
	 */
	private void generateMain() {
		final ArrayType stringArrayType = ArrayType.v(RefType.v(STRING_CLASS_NAME), 1);
		JimpleBodyGenerator mainGenerator = classGenerator.method("main", Arrays.asList((Type)stringArrayType), soot.VoidType.v());

		final Local argsLocal = mainGenerator.local(true, stringArrayType);
		
		Chain<Unit> units = mainGenerator.getUnits();
		units.add(jimple.newIdentityStmt(argsLocal, jimple.newParameterRef(stringArrayType, 0)));
		mainGenerator.staticCall(initGenerator.getMethod());
		mainGenerator.staticCall(serviceGenerator.getMethod());
		mainGenerator.staticCall(destroyMethodGen.getMethod());
	}
}
