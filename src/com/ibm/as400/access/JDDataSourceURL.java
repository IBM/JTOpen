///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDDataSourceURL.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;


/**
 * <p>
 * A class representing a URL specifying an IBM i system data source. This is
 * needed to connect before accessing data. The supported syntaxes for the DB2
 * for IBM i database URL are documented in the comments for AS400JDBCDriver.
 **/
public class JDDataSourceURL implements Serializable {
  static final long serialVersionUID = 4L;

  private static final String PROTOCOL_ = "jdbc";

  // This subprotocol was registered with JavaSoft on 03/10/97.
  private static final String SUB_PROTOCOL_ = "as400";

  private static final String NATIVE_SUB_PROTOCOL_ = "db2"; // @B1A

  private boolean extraPathSpecified_;
  private boolean portSpecified_;
  private Properties properties_;
  private String schema_;
  private String serverName_;
  private String url_;
  private boolean valid_;
  private String secondaryUrl_;
  transient private int    portNumber_ = 0; /*@V1A*/

  /**
   * Constructor.
   * 
   * @param url
   *          The URL to parse.
   **/
  //
  // @A1C
  //
  public JDDataSourceURL(String url) {
    // Initialize.
    extraPathSpecified_ = false;
    portSpecified_ = false;
    properties_ = new Properties();
    schema_ = "";
    secondaryUrl_ = "";
    serverName_ = "";
    url_ = url;
    valid_ = false;

    // Check for null.
    if (url != null) {

      // Parse the URL. @A2C
      parseURL(url);

      // If valid, then if a secondary URL was specified, clean it up. @A2A
      if (valid_) {
        if (properties_.containsKey(JDProperties.SECONDARY_URL_))
          secondaryUrl_ = (String) properties_.get(JDProperties.SECONDARY_URL_);
        // Collapse any "\;" into ";", by backing through the URL.
        if (secondaryUrl_.indexOf("\\;") != -1) {
          StringBuffer buf = new StringBuffer(secondaryUrl_.length());
          char subsequentChar = ' ';
          for (int i = secondaryUrl_.length() - 1; i > -1; i--) {
            char thisChar = secondaryUrl_.charAt(i);
            if (thisChar == '\\' && subsequentChar == ';') {
            } // Do not copy the backslash if it precedes a semicolon.
            else
              buf.insert(0, thisChar);
            subsequentChar = thisChar;
          }
          secondaryUrl_ = buf.toString();
          properties_.put(JDProperties.SECONDARY_URL_, secondaryUrl_);
        }
      }
    }
  }

  public JDDataSourceURL(JDDataSourceURL originalDataSourceUrl_, String server,
      String port) {
    extraPathSpecified_ = originalDataSourceUrl_.extraPathSpecified_; 
    properties_ = (Properties) originalDataSourceUrl_.properties_.clone();
    schema_  = originalDataSourceUrl_.schema_;

    valid_ = originalDataSourceUrl_.valid_;
    secondaryUrl_ = originalDataSourceUrl_.secondaryUrl_;
    
    serverName_  = server;
    int portNumber = 0; 
    if (port != null ) { 
      try { 
        portNumber = Integer.parseInt(port);
      } catch (NumberFormatException nfe) { 
        portNumber = 0; 
      }
    }
    if (portNumber == 0) { 
      portSpecified_ = false;
      portNumber_ = 0;
    } else {
      portSpecified_ = true; 
      portNumber_ = portNumber; 
    }
    
    regenerateUrl(); 
  }

  /** 
   * regenerate the URL field base on the current properties. 
   */
  void regenerateUrl() {
    StringBuffer sb = new StringBuffer(); 
    sb.append("jdbc:as400:");
    sb.append(serverName_);
    if (portSpecified_) {
      sb.append(":");
      sb.append(portNumber_); 
    }
    if (schema_ != null && schema_.length() > 0 ) {
      sb.append("/");
      sb.append(schema_);
    }
    Enumeration keyEnum = properties_.keys(); 
    while (keyEnum.hasMoreElements()) {
      String key = (String) keyEnum.nextElement(); 
      String value=  properties_.getProperty(key);
      sb.append(";");
      sb.append(key);
      sb.append("="); 
      sb.append(value); 
    }
    
    url_ = sb.toString(); 
    
  }

  // @B1A
  String getNativeURL() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(PROTOCOL_);
    buffer.append(':');
    buffer.append(NATIVE_SUB_PROTOCOL_);
    buffer.append(':');
    // @B3D if (url_.length() >= 11) // @B2A
    // @B3D buffer.append(url_.substring(11));
    buffer.append("LOCALHOST"); // @B3A
    String user = (String) properties_.getProperty(JDProperties.USER_); // @native
    String pass = (String) properties_.getProperty(JDProperties.PASSWORD_); // @native
    if (user != null) // @native
      buffer.append(";user=" + user); // @native
    if (pass != null) // @native
      buffer.append(";password=" + pass); // @native

    Enumeration en = properties_.keys(); // @natprops
    while (en.hasMoreElements()) // @natprops
    { // @natprops
      String key = (String) en.nextElement(); // @natprops
      buffer.append(";" + key + "=" + (String) properties_.getProperty(key)); // @natprops
    } // @natprops

    return buffer.toString();
  }

  /**
   * Get the properties that were specified as part of the URL.
   **/
  Properties getProperties() {
    return properties_;
  }

  /**
   * Validate and return the schema as it was parsed from the URL. We do
   * validation here since the system does not do any - which results in errors
   * happening later that are difficult to debug.
   * 
   * @return URL schema, or null if it is invalid.
   * @throws SQLException  If a database error occurs.
   **/
  public String getSchema() throws SQLException {
    int length = schema_.length();

    // Check the length.
    if (length > 128) // @128sch
      return null;

    // @C1D Check that every character is alphanumeric.
    // @C1D for (int i = 0; i < length; ++i)
    // @C1D if ((! Character.isDigit (schema_.charAt (i)))
    // @C1D && (! Character.isUpperCase (schema_.charAt (i)))
    // @C1D && (! Character.isLowerCase (schema_.charAt (i))))
    // @C1D return null;

    return schema_;
  }

  // @A2A
  /**
   * Get the secondary URL, as it was parsed from the URL.
   * 
   * @return Secondary URL. "" if none was specified.
   **/
  String getSecondaryURL() {
    return secondaryUrl_;
  }

  /**
   * Get the system name as it was parsed from the URL.
   * 
   * @return System name.
   **/
  public String getServerName() {
    return serverName_;
  }

  /**
   * Returns the index of the first occurrence of either string.
   * 
   * @param s
   *          The string in which to search.
   * @param a
   *          The first string to search for.
   * @param b
   *          The second string to search for.
   * @param fromIndex
   *          Starting at index.
   * @return The index, or -1 if neither string is found.
   **/
  //
  // @A1A
  //
  static private int indexOfEither(String s, String a, String b, int fromIndex) {
    int ia = s.indexOf(a, fromIndex);
    int ib = s.indexOf(b, fromIndex);
    if (ia == -1)
      return ib;
    else if (ib == -1)
      return ia;
    else
      return Math.min(ia, ib);
  }

  /**
   * Was any extra path specified in the URL (i.e., anything that we will
   * ignore)?
   * 
   * @return true if extra path was specified.
   **/
  boolean isExtraPathSpecified() {
    return extraPathSpecified_;
  }

  /**
   * Was a port specified in the URL?
   * 
   * @return true if a port was specified.
   **/
  public boolean isPortSpecified() {
    return portSpecified_;
  }

  /**
   * Is the URL valid for access by this JDBC driver?
   * 
   * @return true if valid.
   **/
  public boolean isValid() {
    return valid_;
  }

  /**
   * Parse a URL.
   **/
  private void parseURL(String url) {
    int urlLength = url.length();

    // Parse and verify the protocol.
    int mark1 = url.indexOf(":");
    if (mark1 == -1)
      return;
    if (!url.substring(0, mark1).equalsIgnoreCase(PROTOCOL_))
      return;

    // Parse and verify the subprotocol.
    int mark2 = indexOfEither(url, ":", ";", mark1 + 1);
    if (mark2 == -1)
      mark2 = urlLength;
    if (!url.substring(mark1 + 1, mark2).equalsIgnoreCase(SUB_PROTOCOL_))
      return;
    valid_ = true;

    // Parse the subname.
    String subname;
    if (mark2 == urlLength)
      subname = "";
    else if (url.charAt(mark2) == ':')
      subname = url.substring(mark2 + 1);
    else
      subname = url.substring(mark2);

    String token;
    int tokenCount = 0;
    int nextSemicolonPos = 0;
    int priorSemicolonPos = -1;
    int searchPos = 0; // @A2A
    while (nextSemicolonPos != -1) {
      nextSemicolonPos = subname.indexOf(';', searchPos); // @A2C
      if (nextSemicolonPos == -1)
        token = subname.substring(priorSemicolonPos + 1);
      else if (nextSemicolonPos > 0 && // @A2A
          subname.charAt(nextSemicolonPos - 1) == '\\') // @A2A
      { // Ignore the semicolon if preceded by backslash. //@A2A
        searchPos = nextSemicolonPos + 1; // @A2A
        continue; // @A2A
      } else
        token = subname.substring(priorSemicolonPos + 1, nextSemicolonPos);
      // @delim3 allow quoted default SQL schema in url
      int nextQuote = -1; // @delim3
      // Check if schema is quoted. Parsing could have split schema if it
      // contains ';'. //@delim3
      if (token.indexOf('"') != -1) // @delim3
      { // @delim3
        if (token.endsWith("\"")) // @delim3
        { // @delim3
          nextQuote = nextSemicolonPos - 1; // @delim3
        } else // @delim3
        { // @delim3
          nextQuote = subname.indexOf('"', nextSemicolonPos); // @delim3
          token += subname.substring(nextSemicolonPos, nextQuote + 1); // @delim3
          nextSemicolonPos = subname.indexOf(';', nextQuote);// @delim4
          // nextSemicolonPos = nextQuote + 1; //@delim3
        } // @delim3
      } // @delim3

      ++tokenCount;

      // Parse the first token. This is the system name and
      // default SQL schema.
      if (tokenCount == 1) {

        // Strip off // if it is there.
        // boolean doubleSlash = false;
        if (token.length() >= 2)
          if (token.substring(0, 2).equals("//"))
            token = token.substring(2);

        // Split it into system name and schema.
        int slash = token.indexOf('/');
        if (slash == -1)
          serverName_ = token;
        else {
          serverName_ = token.substring(0, slash);
          schema_ = token.substring(slash + 1);
        }

        //
        // Check for IPV6 name @J1A
        //

        boolean ipV6Name = false;
        if ((serverName_.length() > 0) && (serverName_.charAt(0) == '[')) {
          int braceIndex = serverName_.indexOf(']');
          if (braceIndex > 0) {
            int colonIndex = serverName_.indexOf(':', braceIndex);
            if (colonIndex != -1) {
              portSpecified_ = true;
              setPortNumber(colonIndex); /*@V1A*/
            }
            serverName_ = serverName_.substring(1, braceIndex);
            ipV6Name = true;
          }
        }
        if (!ipV6Name) {
          // Validate the system name.
          int colonIndex = serverName_.indexOf(':');
          if (colonIndex != -1) {
            portSpecified_ = true;
            setPortNumber(colonIndex);  /*@V1A*/
            serverName_ = serverName_.substring(0, colonIndex);

          }
        }
        // Validate the schema.
        int slash2 = schema_.indexOf('/');
        if (slash2 != -1 && nextQuote == -1) { // @delim3
          schema_ = schema_.substring(0, slash2);
          extraPathSpecified_ = true;
        }
      }

      // Parse other tokens. These specify properties.
      else {
        int equalSignPos = token.indexOf('=');
        String key;
        String value;
        if (equalSignPos == -1) {
          key = token.trim();
          value = "";
        } else {
          key = token.substring(0, equalSignPos).trim();
          value = token.substring(equalSignPos + 1).trim();
        }
        properties_.put(key, value);
      }

      // End of while loop.
      priorSemicolonPos = nextSemicolonPos;
      searchPos = priorSemicolonPos + 1; // @A2A
    }
  }

  /*@V1A*/
  private void setPortNumber(int colonIndex) {
    try {
      portNumber_ = Integer.parseInt(serverName_.substring(colonIndex+1)); 
    } catch (Exception e) { 
      // TODO:  Add tracing here
    }
    
  }
  /*@V1A*/
  public int getPortNumber() { 
     return portNumber_; 
  }
  /**
   * Return the URL as a String.
   * 
   * @return The URL as a String.
   **/
  public String toString() {
    return url_;
  }

}
