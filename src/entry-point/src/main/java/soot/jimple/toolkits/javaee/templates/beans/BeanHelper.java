/*
   This file is part of Soot entry point creator.

   Soot entry point creator is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Soot entry point creator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Soot entry point creator.  If not, see
<http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
   Copyright 2015 Ecole Polytechnique de Montreal & Tata Consultancy Services
*/
package soot.jimple.toolkits.javaee.templates.beans;

import soot.*;

import java.util.*;

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
		Collection<SootClass> subClasses;
		
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
