/**
 * Provides classes that assist in writing servlets that manipulate IBM i data.
 *
 * <P>
 * The servlet classes that are provided with the IBM Toolbox for Java work with
 * the classes in the com.ibm.as400.access package to give you access to
 * information located on the system. You decide how to use the servlet classes
 * to assist you with your own servlet projects.
 * </P>
 *
 * <P>
 * A typical scenario is this: A web browser connects to the web server that is
 * running the servlet. The jt400Servlet.jar and jt400Access.jar files reside on
 * the web server because the servlet classes use some of the access classes to
 * retrieve the data, and some of the HTML classes to present the data. The web
 * server is connected to the IBM i system where the data is stored.
 * </P>
 *
 * <P>
 * <B>Note:</B> The jt400Servlet.jar file includes both the HTML and Servlet
 * classes. You must update your CLASSPATH to point to the jt400Servlet.jar file
 * if you want to use the classes in the com.ibm.as400.util.servlet package.
 * </P>
 */
package com.ibm.as400.util.servlet;
