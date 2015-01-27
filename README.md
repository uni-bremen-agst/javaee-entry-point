Soot plugin for generating JavaEE entry points
==============================================
This repository contains a Soot plugin for generating entry points for JavaEE applications.

How to build (hardcore)
-----------------------
> $ mvn install

How to build with Eclipse
-------------------------
Install the eclipse m2e feature and the plugin for scala from http://alchim31.free.fr/m2e-scala/update-site/ . Afterwards,
just import the plugin and everything is fine.

How to use
----------
> $ mvn dependency:copy-dependencies -DincludeScope=runtime

> $ java -cp target/dependency/*:../../../soot/libs/*:../../../soot/lib/sootclasses-trunk.jar:target/soot.plugins.entry-points-0.0.1-SNAPSHOT.jar soot.Main -plugin &lt;plugin.xml&gt; ...

Configuration
-------------
> &lt;soot-plugins&gt;
> &lt;phase-plugin phase="wjpp.entry" class="soot.jimple.toolkits.javaee.EntryPointPlugin"/&gt;
> &lt;/soot-plugins&gt;
