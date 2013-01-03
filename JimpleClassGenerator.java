package soot.jimple.toolkits.javaee;

import java.util.Collections;
import java.util.List;

import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;

/**
 * A generator that generates a class with jimple based methods.
 * 
 * @author Bernhard Berger
 */
public class JimpleClassGenerator {
	/**
	 * The class that corresponds to this generator.
	 */
	private final SootClass clazz;

	private JimpleBodyGenerator clInitGenerator = null;
	
	/**
	 * The soot scene.
	 */
	private final Scene scene = Scene.v();
	
	/**
	 * Creates a new class with name {@code className} that inherits from {@code superClassName}.
	 * 
	 * @param className Name of class.
	 * @param superClassName Name of superclass. At least {@code java.lang.Object}.
	 * @param isApplicationClass Determines if the class is an application class.
	 */
	public JimpleClassGenerator(final String className, final String superClassName, final boolean isApplicationClass) {
		clazz = new SootClass(className, Modifier.PUBLIC);
		clazz.setSuperclass(scene.getSootClass(superClassName));

		scene.addClass(clazz);

		if(isApplicationClass) {
			clazz.setApplicationClass();
		}
	}
	
	/**
	 * Adds a statement to the class initialization method.
	 * 
	 * @param stmt The statement to add.
	 */
	public void addToClInit(final Unit stmt) {
		if(clInitGenerator == null) {
			final List<Type> parameterTypes = Collections.emptyList();
			clInitGenerator = method(SootMethod.staticInitializerName, parameterTypes, VoidType.v());
			clInitGenerator.setStatic();
		}
		
		clInitGenerator.getUnits().add(stmt);
	}

	/**
	 * Creates a new field in the class.
	 * 
	 * @param name Name of the field.
	 * @param type Type of the field.
	 * 
	 * @return The created field.
	 */
	public SootField field(final String name, final SootClass type) {
		final SootField field = new SootField(name, type.getType());

		clazz.addField(field);
		
		return field;
	}

	/**
	 * @return The soot class object.
	 */
	public SootClass getClazz() {
		return clazz;
	}

	/**
	 * The class implements the interface {@code clazzName}.
	 * 
	 * @param clazzName The interface to implement.
	 */
	public void implement(final String clazzName) {
		clazz.implementsInterface(clazzName);
	}

	/**
	 * Creates a new method with the given signature.
	 * 
	 * @param name Name of the method.
	 * @param parameterTypes Types of the parameters.
	 * @param returnType Return type of the method.
	 * 
	 * @return A body generator for the generated method.
	 */
	public JimpleBodyGenerator method(final String name, final List<Type> parameterTypes, final Type returnType) {
		final SootMethod method = new SootMethod(name, parameterTypes, returnType);
		
		clazz.addMethod(method);

		return new JimpleBodyGenerator(method);
	}
}
