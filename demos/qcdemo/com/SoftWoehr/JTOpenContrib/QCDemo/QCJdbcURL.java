/*
 * QCJdbcURL.java
 *
 * Created on July 19, 2000, 9:44 PM
 *
 * This is free open source software distributed under the IBM Public License found
 * on the World Wide Web at http://oss.software.ibm.com/developerworks/opensource/license10.html
 * Copyright *C* 2000, Jack J. Woehr, PO Box 51, Golden, CO 80402-0051 USA jax@well.com
 * Copyright *C* 2000, International Business Machines Corporation and others. All Rights Reserved.
 */

package com.SoftWoehr.JTOpenContrib.QCDemo;

/** A class to canonicalize JDBC URL's.
 * @author jax
 * @version 1.0
 */
public class QCJdbcURL extends Object {

  /** Protocol portion of JDBC URL.
   */
  public String protocol;
  /** Driver portion of JDBC URL.
   */
  public String driver;
  /** Server portion of JDBC URL.
   */
  public String server;
  /** Optional arguments portion of JDBC URL, e.g., "?user=fred&amp;password=blue"
   */
  public java.util.Vector arguments;

  /** Creates new QCJdbcURL */
  public QCJdbcURL() {
  }

  /** Create a URL representation from a URL string.
   * @param url The URL string of the form jdbc:driver:server&lt;options&gt;
   */
  public QCJdbcURL(java.lang.String url) {
    parseURL(url);
  }

  /** Parse a string URL into the fields of this object.
   * @param url The URL string.
   */
  public void parseURL(java.lang.String url) {
    java.util.StringTokenizer st;
    StringBuffer urlInProcess = new StringBuffer(url);
    protocol = null; driver = null; server = null; arguments = null;
    st = new java.util.StringTokenizer(urlInProcess.toString(), ":", false);

    // Get the protocol if we have any string.
    if (st.hasMoreTokens()) {
      protocol = st.nextToken();

      // Lose the head of the url as passed in.
      try {
        // Delete the protocol header.
        urlInProcess.delete(0, protocol.length());
      }
      catch (StringIndexOutOfBoundsException e) {
        e.printStackTrace();
      }
    }

    // Get the driver if we have anything left.
    if (st.hasMoreElements()) {
      driver = st.nextToken();

      // Lose that bit of the url as passed in.
      try {
        // Delete the colon that must have been there along with the driver name.
        urlInProcess.delete(0, 1 + driver.length());
      }
      catch (StringIndexOutOfBoundsException e) {
        e.printStackTrace();
      }
    }
    // Now delete the trailing colon and any server-leading "//".
    while (urlInProcess.length() >0) {
      if  ((urlInProcess.charAt(0) == ':') | (urlInProcess.charAt(0) == '/')) {
        try {
          urlInProcess.deleteCharAt(0);
        }
        catch (StringIndexOutOfBoundsException e) {
          e.printStackTrace();
        }
      }
      else { // Else we are done with this step.
        break;
      }
    }

    // Create a new tokenizer to process server and remaining arguments.
    st = new java.util.StringTokenizer(urlInProcess.toString(), "&?;", true);

    // Get server name
    if (st.hasMoreTokens()) {
      server = st.nextToken();

      try {
        urlInProcess.delete(0, server.length());
      }
      catch (StringIndexOutOfBoundsException e) {
        e.printStackTrace();
      }
    }

    // See if we need a Vector to hold arguments.
    if (st.hasMoreTokens()) {
      arguments = new java.util.Vector(10,10);
    }
    while (st.hasMoreTokens()) {
      StringBuffer s = new StringBuffer(st.nextToken());
      if (((s.charAt(0) == '&')
      | (s.charAt(0) == '?')
      | (s.charAt(0) == ';'))
      & st.hasMoreTokens())
      {
        s.append(st.nextToken());
      }
      arguments.add(s.toString());
    }
  }

  /** Returns a string representation of the URL.
   * @return The string for the URL.
   */
  public String getURL() {
    StringBuffer result = new StringBuffer();
    if (null != protocol) {
      result.append(protocol + ":");
      if (null != driver) {
        result.append(driver + ":");
        if (driver.equals("as400")
        | driver.equals("postgresql"))
        {
          result.append("//");
        }
        if (null != server) {
          result.append(server);
          if (null != arguments) {
            for (java.util.Enumeration e = arguments.elements() ; e.hasMoreElements() ;) {
              result.append(e.nextElement().toString());
            }
          }
        }
      }
    }
    return result.toString();
  }

  /** Just testing. Pass this main() a string URL to parse and it will
   * parse it and display the parts and its string reconstruction of same.
   * @param argv argv[0] == A URL to parse.
   */
  public static void main(String[] argv) {
    QCJdbcURL url = new QCJdbcURL(argv[0]);

    if (url.protocol != null) {
      System.out.println("Protocol:  " + url.protocol);
    }

    if (url.driver != null) {
      System.out.println("Driver:    " + url.driver);
    }

    if (url.server != null) {
      System.out.println("Server:    " + url.server);
    }

    if (url.arguments != null) {
      for (java.util.Enumeration e = url.arguments.elements() ; e.hasMoreElements() ;) {
        System.out.println("Argument:    " + e.nextElement());
      }
    }
    System.out.println(url.getURL());
  }

}
