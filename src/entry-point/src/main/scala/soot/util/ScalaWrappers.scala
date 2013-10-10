/**
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.util

import soot.{Unit=>SootUnit, _}
import scala.collection.JavaConverters._
import soot.jimple.{FieldRef, ArrayRef, InvokeExpr, Stmt}
import soot.options.Options
import soot.jimple.toolkits.callgraph.{ReachableMethods, ContextSensitiveCallGraph, CallGraph}
import scala.Some
import soot.tagkit.{Host, AnnotationElem, AnnotationTag, VisibilityAnnotationTag, Tag}
import soot.toolkits.exceptions.ThrowAnalysis
import soot.jimple.toolkits.pointer.SideEffectAnalysis

/**
 * Convenience classes for using Soot with a scala-like API
 */
object ScalaWrappers {

  implicit class RichNumerable(val n: Numberable) extends AnyVal {
    @inline def number : Int = n.getNumber
    @inline def number_= (newNumber : Int) = n.setNumber(newNumber)
  }

  implicit class RichSootClass(val v : SootClass) extends AnyVal  {

    @inline def name : String = v.getName
    @inline def name_=(newName : String) = v.setName(newName)
    @inline def packageName : String = v.getPackageName
    @inline def shortName : String = v.getShortName

    @inline def modifiers : Int = v.getModifiers
    @inline def modifiers_= (mods : Int) = v.setModifiers(mods)

    @inline def fields : RichChain[SootField] = v.getFields
    //@inline def fields_+= (newField : SootField) = v.addField(newField)
    @inline def interfaces: RichChain[SootClass] = v.getInterfaces
    //@inline def interfaces_+= (newInterface : SootClass) = v.addInterface(newInterface)
    @inline def superclass: SootClass = v.getSuperclass
    @inline def superclass_= (sc : SootClass) = v.setSuperclass(sc)

    @inline def methods: Traversable[SootMethod] = v.getMethods.asScala
    //@inline def methods_+= (newMethod : SootMethod) = v.addMethod(newMethod)
    @inline def outerClass : SootClass = v.getOuterClass
    @inline def outerClass_= (sc : SootClass) = v.setOuterClass(sc)
    @inline def `type` : RefType = v.getType
    @inline def type_= (typ : RefType) = v.setRefType(typ)

    @inline def field(subsignature: String) : Option[SootField] = if (v.declaresField(subsignature)) Some(v.getField(subsignature)) else None
    @inline def field(name: String, typ : Type) : Option[SootField] = if (v.declaresField(name,typ)) Some (v.getField(name,typ)) else None
    @inline def fieldByName(name:String) : Option[SootField] = if (v.declaresFieldByName(name)) Some(v.getFieldByName(name)) else None
    @inline def fieldsByName(name : String) : Traversable[SootField] = fields.filter(_.name == name)

    @inline def methodsByName(name : String): Traversable[SootMethod] = methods.filter(_.name==name)
    @inline def methodByName(name : String): Option[SootMethod] = if (v.declaresMethodByName(name)) Some(v.getMethodByName(name)) else None
    @inline def method(subsignature : String) : Option[SootMethod] = if (v.declaresMethod(subsignature)) Some(v.getMethod(subsignature)) else None
    @inline def method(subsignature : NumberedString) : Option[SootMethod] = if (v.declaresMethod(subsignature)) Some(v.getMethod(subsignature)) else None
    @inline def method(name : String,paramTypes : List[Type]) : Option[SootMethod] = {
      val paramJava = paramTypes.asJava
      if (v.declaresMethod(name,paramJava))
        Some(v.getMethod(name,paramJava))
      else
        None
    }
    @inline def method(name : String,paramTypes : List[Type], retType : Type) : Option[SootMethod] = {
      val paramJava = paramTypes.asJava
      if (v.declaresMethod(name,paramJava,retType))
        Some(v.getMethod(name,paramJava, retType))
      else
        None
    }

    //Those have good getter APIs, but annoying setters
    @inline def isInScene_=(flag: Boolean) = v.setInScene(flag)
    @inline def isPhantom_(flag: Boolean) = v.setPhantom(flag)
    @inline def resolvingLevel_=(lvl : Int) = v.setResolvingLevel(lvl)
  }

  implicit class RichClassMember(val c: ClassMember) extends AnyVal {
    @inline def declaringClass : SootClass = c.getDeclaringClass
    @inline def modifiers : Int = c.getModifiers
    @inline def modifiers_= (mods : Int) = c.setModifiers(mods)
    @inline def isPhantom_=(flag : Boolean) = c.setPhantom(flag)
  }

  implicit class RichSootField (val v: SootField) extends AnyVal {
    @inline def name : String = v.getName
    @inline def name_=(newName : String) = v.setName(newName)

    @inline def signature : String = v.getSignature
    @inline def subSignature : String = v.getSubSignature
    @inline def declaration : String = v.getDeclaration

    @inline def `type` : Type = v.getType
  }

  implicit class RichSootMethod(val v : SootMethod) extends AnyVal {
    @inline def isClinit = v.getName == "<clinit>"
    @inline def name : String = v.getName
    @inline def name_=(newName : String) = v.setName(newName)

    @inline def isDeclared_=(flag : Boolean) = v.setDeclared(true)

    @inline def signature : String = v.getSignature
    @inline def subSignature : String = v.getSubSignature

    @inline def body: Option[Body] = if (v.hasActiveBody) Some(v.getActiveBody) else None
    @inline def body_= (body : Body) = v.setActiveBody(body)

    @inline def source : MethodSource = v.getSource
    @inline def source_= (ms : MethodSource) = v.setSource(ms)

    @inline def parameterTypes : Seq[Type] = v.getParameterTypes.asScala
    @inline def parameterTypes_= (newPt : Seq[Type]) =  v.setParameterTypes(newPt.asJava)

    @inline def exceptions : Seq[SootClass] = v.getExceptions.asScala
    @inline def exceptions_= (newEx : Seq[SootClass]) = v.setExceptions(newEx.asJava)

    @inline def returnType : Type = v.getReturnType
    @inline def returnType_= (typ : Type) = v.setReturnType(typ)

    @inline def declaringClass : SootClass = v.getDeclaringClass
    @inline def declaringClass_= (sc : SootClass) = v.setDeclaringClass(sc)

    @inline def units: PatchingChain[SootUnit] = if (v.hasActiveBody) v.getActiveBody.getUnits else new PatchingChain[SootUnit](new HashChain[SootUnit]())
    @inline def statements : PatchingChain[Stmt] =
      if (v.hasActiveBody) v.getActiveBody.getUnits.asInstanceOf[PatchingChain[Stmt]] else new PatchingChain[Stmt](new HashChain[Stmt]())

    @inline def numberedSignature : NumberedString = v.getNumberedSubSignature
  }

  implicit class RichBody(val v : Body) extends AnyVal {
    @inline def units : PatchingChain[Stmt] = v.getUnits.asInstanceOf[PatchingChain[Stmt]]
    @inline def locals : Chain[Local] = v.getLocals
    @inline def method : SootMethod = v.getMethod
    @inline def thisLocal : Local = v.getThisLocal
    @inline def parameterLocal(i: Int) = v.getParameterLocal(i)
    @inline def traps : Chain[Trap] = v.getTraps
  }

  implicit class RichStmt(val v : Stmt) extends AnyVal {
    @inline def invokeExpr : Option[InvokeExpr] = if (v.containsInvokeExpr()) Some(v.getInvokeExpr) else None
    @inline def arrayRef : Option[ArrayRef] = if (v.containsArrayRef()) Some(v.getArrayRef) else None
    @inline def fieldRef : Option[FieldRef] = if (v.containsArrayRef()) Some(v.getFieldRef) else None
  }

  implicit class RichFastHierarchy(val v: FastHierarchy) extends AnyVal {
    @inline def abstractDispatch(sm : SootMethod) : Set[SootMethod] = v.resolveAbstractDispatch(sm.getDeclaringClass, sm).asScala.toSet
    @inline def interfaceImplementers(sc : SootClass) : Set[SootClass] = if (sc.isInterface) v.getAllImplementersOfInterface(sc).asScala.toSet else Set[SootClass]()
    @inline def subClassesOf(sc: SootClass) : Set[SootClass] = v.getSubclassesOf(sc).asScala.toSet
    @inline def allSubinterfaces(sc: SootClass) : Set[SootClass] = v.getAllSubinterfaces(sc).asScala.toSet
  }

  implicit class RichScene(val v : Scene) extends AnyVal {
    @inline def availableClasses : Traversable[SootClass] ={
      if (v.getApplicationClasses.isEmpty && Options.v.process_dir().isEmpty)
        v.getClasses.asScala
      else
        v.getApplicationClasses.asScala
    }

    @inline def applicationClasses : Traversable[SootClass] = v.getApplicationClasses.asScala
    @inline def classes : Traversable[SootClass] = v.getClasses.asScala
    @inline def libraryClasses : Traversable[SootClass] = v.getLibraryClasses.asScala
    @inline def phantomClasses : Traversable[SootClass] = v.getPhantomClasses.asScala

    @inline def field(fieldSpec : String) : SootField =  v.getField(fieldSpec)
    @inline def refType(className : String) : RefType = v.getRefType(className)
    @inline def sootClass(className : String) : SootClass = v.getSootClass(className)
    @inline def method(sig: String) : Option[SootMethod] = {
      try{
        Option(v.getMethod(sig))
      } catch {
        case e : RuntimeException => None
      }
    }

    @inline def hierarchy : Hierarchy = v.getActiveHierarchy
    @inline def hierarchy_= (h: Hierarchy) = v.setActiveHierarchy(h)

    @inline def fastHierarchy : FastHierarchy = v.getOrMakeFastHierarchy
    @inline def fastHierarchy_= (fh : FastHierarchy) = v.setFastHierarchy(fh)

    @inline def callGraph : CallGraph = v.getCallGraph
    @inline def callGraph_= (cg : CallGraph) = v.setCallGraph(cg)

    @inline def contextNumberer : Numberer[_] = v.getContextNumberer
    @inline def contextNumberer_=[E] (cn : Numberer[E]) = v.setContextNumberer(cn)

    @inline def contextSensitiveCallGraph : ContextSensitiveCallGraph = v.getContextSensitiveCallGraph
    @inline def contextSensitiveCallGraph_=(cscg : ContextSensitiveCallGraph) = v.setContextSensitiveCallGraph(cscg)

    @inline def defaultThrowAnalysis : ThrowAnalysis = v.getDefaultThrowAnalysis
    @inline def defaultThrowAnalysis_= (ta : ThrowAnalysis) = v.setDefaultThrowAnalysis(ta)

    @inline def entryPoints : Seq[SootMethod] = v.getEntryPoints.asScala
    @inline def entryPoints_= (ep : Seq[SootMethod]) = v.setEntryPoints(ep.asJava)

    @inline def mainClass : SootClass = v.getMainClass
    @inline def mainClass_= (sc : SootClass) = v.setMainClass(sc)

    @inline def mainMethod : SootMethod = v.getMainMethod

    @inline def phantomRefs : Boolean = v.getPhantomRefs
    @inline def phantomRefs_= (flag : Boolean) = v.setPhantomRefs(flag)

    @inline def pkgList :Seq[String] = v.getPkgList.asScala
    @inline def pkgList_= (pl : Seq[String]) = v.setPkgList(pl.asJava)

    @inline def pta : PointsToAnalysis = v.getPointsToAnalysis
    @inline def pointsToAnalysis : PointsToAnalysis = v.getPointsToAnalysis
    @inline def pointsToAnalysis_= (pta : PointsToAnalysis) = v.setPointsToAnalysis(pta)

    @inline def sootClassPath : String = v.getSootClassPath
    @inline def sootClassPath_= (scp : String) = v.setSootClassPath(scp)

    @inline def reachableMethods : ReachableMethods = v.getReachableMethods()
    @inline def reachableMethods_= (rm : ReachableMethods) = v.setReachableMethods(rm)

    @inline def sideEffectAnalysis : SideEffectAnalysis = v.getSideEffectAnalysis
    @inline def sideEffectAnalysis_= (sea : SideEffectAnalysis) = v.setSideEffectAnalysis(sea)

  }

  implicit class RichChain[E](val v: Chain[E]) extends Traversable[E]{
    def foreach[U](f: (E) => U) {v.iterator().asScala.foreach(f)}
  }

  implicit class RichHost (val v: Host) extends AnyVal {
    @inline def tags = v.getTags.asScala
    @inline def tag(aName : String) : Option[Tag] = Option(v.getTag(aName))
    @inline def tag[T<:Tag](typ : Class[T]) : Option[T] = v.tags.find(t => t.getClass eq typ).map(_.asInstanceOf[T])
  }

  implicit class RichVisibilityAnnotationTag(val v: VisibilityAnnotationTag) extends AnyVal{
    @inline def annotations : Traversable[AnnotationTag] = v.getAnnotations.asScala
  }

  implicit class RichAnnotationTag(val v: AnnotationTag) extends AnyVal {
    @inline def elements : Traversable[AnnotationElem] = v.getElems.asScala
    @inline def info : String = v.getInfo
    @inline def name : String = v.getName
  }

  implicit class RichAnnotationElement(val v: AnnotationElem) extends AnyVal{
    @inline def kind : Char = v.getKind
    @inline def name : String = v.getName
  }

}
