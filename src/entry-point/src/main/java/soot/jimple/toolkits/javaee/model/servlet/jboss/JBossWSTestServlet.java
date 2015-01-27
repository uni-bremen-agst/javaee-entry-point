/*
    This file is part of Soot entry point creator.

    Soot entry point creator is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Soot entry point creator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Soot entry point creator.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2013 Ecole Polytechnique de Montreal & Tata Consultancy Services
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
