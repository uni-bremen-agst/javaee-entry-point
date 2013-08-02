package soot.jimple.toolkits.javaee.templates.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

public class BeanHelper {
	public static List<Type> collectTypesDeep(final List<Type> types) {
		final Set<Type> workingList = new HashSet<Type>(types);
		final Set<Type> result = new HashSet<Type>();
		
		while(!workingList.isEmpty()) {
			final Type type = workingList.iterator().next();
			workingList.remove(type);
			
			if(result.contains(type) || type instanceof VoidType) {
				continue;
			}
			
			result.add(type);
			
			if(type instanceof RefType) {
				final RefType refType = (RefType)type;
				for(final SootMethod method : refType.getSootClass().getMethods()) {
					if(!method.getName().startsWith("set")) {
						continue;
					}
					
					if(method.getParameterCount() != 1) {
						continue;
					}
					
					workingList.add(method.getParameterTypes().get(0));
				}
			} else if(type instanceof ArrayType) {
				final ArrayType arrayType = (ArrayType)type;
				workingList.add(arrayType.baseType);
			}
		}
		
		return new ArrayList<Type>(result);
	}
}
