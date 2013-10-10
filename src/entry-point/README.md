Plugin for generating entry points
==================================
Plugin for generating entry points for Java applications that does not have main functions, such as JavaEE applications.

How to build
------------
> $ mvn install


How to use
----------
> $ mvn dependency:copy-dependencies -DincludeScope=runtime

> $ java -cp target/dependency/*:../../../soot/libs/*:../../../soot/lib/sootclasses-trunk.jar:target/soot.plugins.entry-points-0.0.1-SNAPSHOT.jar soot.Main ...

Configuration
-------------
> &lt;phase-plugin phase="wjpp.entry" class="soot.jimple.toolkits.javaee.EntryPointPlugin"/&gt;
