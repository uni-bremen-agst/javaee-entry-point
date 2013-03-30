package soot.jimple.toolkits.transformation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.Scene;
import soot.ShortType;
import soot.Type;
import soot.VoidType;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.QualifiedName;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.SootType;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardBoolean;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardByte;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardChar;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardDouble;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardFloat;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardInt;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardLong;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardPrimitiveType;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardShort;
import soot.jimple.toolkits.transformation.dsl.transformationLanguage.WildcardVoid;

/**
 * Helper methods for type handling.
 * 
 * @author Bernhard Berger
 */
public class TypeHelper {
	
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(TypeHelper.class);
	
	/**
	 * Creates a {@link soot.Type} for a string.
	 * 
	 * @param type Name of the type.
	 * @return A valid type.
	 */
	public static Type resolveType(final SootType type) {
		if(type instanceof WildcardPrimitiveType) {
			if(type instanceof WildcardBoolean) {
				return BooleanType.v();
			} else if(type instanceof WildcardByte) {
				return ByteType.v();
			} else if(type instanceof WildcardChar) {
				return CharType.v();
			} else if(type instanceof WildcardDouble) {
				return DoubleType.v();
			} else if(type instanceof WildcardFloat) {
				return FloatType.v();
			} else if(type instanceof WildcardInt) {
				return IntType.v();
			} else if(type instanceof WildcardLong) {
				return LongType.v();
			} else if(type instanceof WildcardShort) {
				return ShortType.v();
			} else if(type instanceof WildcardVoid) {
				return VoidType.v();
			} else {
				LOG.error("Unknown primitive type {}.", type.getClass());
				return null;
			}
		} else {
			return Scene.v().getSootClass(((QualifiedName)type).getName()).getType();
		}
	}
}
