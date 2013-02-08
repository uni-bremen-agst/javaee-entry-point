package soot.jimple.toolkits.javaee;

import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;

/**
 * Generator for jimple-based method bodies.
 *  
 * @author Bernhard Berger
 */
@Deprecated
public class JimpleBodyGenerator {
	/**
	 * The body we are going to populate.
	 */
	private final JimpleBody body;

	/**
	 * Jimple instance used for creating new statements.
	 */
	private final Jimple jimple = Jimple.v(); 
	
	/**
	 * Number of locals of type {@code int}.
	 */
	private int localIntegerCounter = 0;

	/**
	 * Number of locals of type {@code long}.
	 */
	private int localLongCounter = 0;
	
	/**
	 * Number of reference locals. 
	 */
	private int localReferenceCounter = 0;
	
	/**
	 * Creates a new jimple body for {@code method}.
	 * 
	 * @param method The method you want to generate a body for.
	 */
	public JimpleBodyGenerator(final SootMethod method) {
		body = jimple.newBody(method);
		method.setActiveBody(body);
	}
	
	/**
	 * Creates an instance.
     *
	 * <pre>
	 * {@code
	 *   [var] = new [clazz];
	 * }</pre>
	 * 
	 * @param var Destination local.
	 * @param clazz Type to create.
	 */
	public void createInstance(final Local var, final SootClass clazz) {
		body.getUnits().add(jimple.newAssignStmt(var, jimple.newNewExpr(clazz.getType())));
	}

	/**
	 * @return The generated body.
	 */
	public Body getBody() {
		return body;
	}

	/**
	 * @return The associated method.
	 */
	public SootMethod getMethod() {
		return body.getMethod();
	}

	/**
	 * @return Chain of all traps.
	 */
	public Chain<Trap> getTraps() {
		return body.getTraps();
	}

	/**
	 * @return Chain of all units.
	 */
	public Chain<Unit> getUnits() {
		return body.getUnits();
	}

	/**
	 * Creates a new local.
	 * 
	 * @param isParameter Whether the local is a parameter local.
	 * @param type The type of the local.
	 * @return The freshly created local.
	 */
	public Local local(boolean isParameter, final Type type) {
		String name = isParameter ? "" : "$";
		
		if(type instanceof RefType || type instanceof ArrayType) {
			name += "r";
			name += localReferenceCounter++;
		} else if(type instanceof LongType) {
			name += "l";
			name += localLongCounter++;
		} else {
			name += "i";
			name += localIntegerCounter++;
		}

		final Local local = jimple.newLocal(name, type);

		body.getLocals().add(local);
		
		return local;
	}

	/**
	 * Marks the method {@code public}.
	 */
	public void setPublic() {
		body.getMethod().setModifiers(body.getMethod().getModifiers() | Modifier.PUBLIC);
	}
	
	/**
	 * Marks the method {@code static}.
	 */
	public void setStatic() {
		body.getMethod().setModifiers(body.getMethod().getModifiers() | Modifier.STATIC);
	}

	/**
	 * Creates a call to the static method {@code method} without parameters.
	 * 
	 * @param method The method to call.
	 */
	public void staticCall(final SootMethod method) {
		assert method.isStatic() && method.getParameterCount() == 0 : "Preconditions do not hold.";
		
		body.getUnits().add(jimple.newInvokeStmt(jimple.newStaticInvokeExpr(method.makeRef())));
	}
}
