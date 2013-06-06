/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee.model.servlet.jboss;


import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.javaee.model.servlet.http.GenericServlet;

import java.util.ArrayList;
import java.util.List;

public class JBossWSTestServlet extends GenericServlet {

    private final List<SootClass> jBossWsClients;
    private final List<SootMethod> testMethods;

    //Required by XPand
    public JBossWSTestServlet(){
        this(new ArrayList<SootClass>(0), new ArrayList<SootMethod>(0));
    }


    public JBossWSTestServlet(List<SootClass> clients, List<SootMethod> methods){
        jBossWsClients = clients;
        testMethods = methods;
    }

    public List<SootClass> getjBossWsClients() {
        return jBossWsClients;
    }

    public List<SootMethod> getTestMethods() {
        return testMethods;
    }

}
