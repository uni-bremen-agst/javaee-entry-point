Repository
==========

This maven repository contains all eclipse-specific depencencies necessary to build and run the plugins.
The bundles were extracted from the Kepler eclipse release using:

> mvn eclipse:to-maven -DdeployTo=local::default::{directory} -DeclipseDir={directory} -DstripQualifier=true
