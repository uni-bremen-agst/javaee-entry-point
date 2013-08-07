package soot.jimple.toolkits.javaee.templates.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.ArrayType;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

public class BeanHelper {
	/**
	 * This method collects for a list of bean classes all necessary types
	 * to create instances of these bean classes. This includes calling
	 * all setters.
	 * 
	 * @param beanTypes List of bean classes.
	 * 
	 * @return List of all necessary types including the bean types itself.
	 */
	public static List<Type> collectTypesDeep(final List<Type> beanTypes) {
		final Set<Type> workingList = new HashSet<Type>(beanTypes);
		final Set<Type> result = new HashSet<Type>();
		
		while(!workingList.isEmpty()) {
			final Type type = workingList.iterator().next();
			workingList.remove(type);
			
			if(result.contains(type) || skipType(type)) {
				continue;
			}
			
			result.add(type);
			
			if(type instanceof RefType) {
				final RefType refType = (RefType)type;
				workingList.addAll(collectSubstitutionTypes(refType));

				for(final SootMethod method : collectSetters(refType.getSootClass())) {
					workingList.add(method.getParameterTypes().get(0));
				}
			} else if(type instanceof ArrayType) {
				final ArrayType arrayType = (ArrayType)type;
				workingList.add(arrayType.baseType);
			}
		}
		
		return new ArrayList<Type>(result);
	}

	private static Collection<? extends Type> collectSubstitutionTypes(final RefType refType) {
		List<SootClass> subClasses;
		
		if(refType.getSootClass().isInterface()) {
			subClasses = Scene.v().getActiveHierarchy().getImplementersOf(refType.getSootClass());
		} else {
			subClasses = Scene.v().getActiveHierarchy().getSubclassesOf(refType.getSootClass());
		}
		final List<Type> subTypes = new ArrayList<Type>(subClasses.size());
		
		for(SootClass child : subClasses) {
			if(child.isConcrete()) {
				subTypes.add(child.getType());
			}
		}
	
		return subTypes;
	}

	private static boolean skipType(final Type type) {
		return type instanceof VoidType;
	}

	private static Collection<SootMethod> collectSetters(final SootClass sootClass) {
		final Set<SootMethod> result = new HashSet<SootMethod>();

		SootClass current = sootClass;
		while(true) {
			for(final SootMethod method : sootClass.getMethods()) {
				if(isSetter(method) && method.isConcrete()) {
					result.add(method);
				}
			}
			
			if(!current.hasSuperclass()) {
				return result;
			}
			
			current = current.getSuperclass();
		} 
	}

	public static Collection<SootMethod> collectSetters(final RefType type) {
		return collectSetters(type.getSootClass());
	}
	
	/**
	 * Checks if {@code method} is a setter method. Setter methods are {@code public},
	 *   its name starts with  {@code set} and has a single parameter.
	 * 
	 * @param method to check.
	 * @return {@code true} if {@code method} is a setter.
	 */
	private static boolean isSetter(final SootMethod method) {
		if(!method.getName().startsWith("set")) {
			return false;
		}

		if(method.getName().length() < 4) {
			return false;
		}
		
		if(!Character.isUpperCase(method.getName().charAt(3))) {
			return false;
		}
		
		if(!method.isPublic()) {
			return false;
		}
		
		if(method.isStatic()) {
			return false;
		}

		if(method.getParameterCount() != 1) {
			return false;
		}
		
		String name = method.getName().substring(3).toLowerCase();
		for(final SootField field : method.getDeclaringClass().getFields()) {
			if(field.getName().toLowerCase().equals(name)) {
				return true;
			}
		}

		return false;
	}
	
	public static Collection<? extends Type> childTypes(final RefType type) {
		return collectSubstitutionTypes(type);
	}
	
	public static boolean isEnum(final SootClass clazz) {
		return Modifier.isEnum(clazz.getModifiers());
	}
}
