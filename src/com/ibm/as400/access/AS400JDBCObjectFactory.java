///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCObjectFactory.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Hashtable;
import javax.naming.Context;           // JNDI
import javax.naming.Name;              // JNDI
import javax.naming.Reference;         // JNDI
import javax.naming.spi.ObjectFactory; // JNDI

/**
*  The AS400JDBCObjectFactory is used by a JNDI service provider to reconstruct an object
*  when it is retrieved from JNDI.
*
*  <p>When constructing your own Reference object, at a minimum, you should set the serverName,
*  userName, pwd, and secure properties.  
*  
*  <p>For Example:        
*  <pre><blockquote>
*  XADataSource xads = null; 
*  String objFactoryName = "com.ibm.as400.access.AS400JDBCObjectFactory";
*  String xadsName = "com.ibm.as400.access.AS400JDBCXADataSource";
*  Reference ref = new Reference(xadsName, objFactoryName, "");
*  ref.add(new StringRefAddr("serverName", "someserver"));
*  ref.add(new StringRefAddr("userName", "someuser"));
*  ref.add(new StringRefAddr("pwd", "somepassword"));
*  ref.add(new StringRefAddr("secure", "false"));
*  ref.add(new StringRefAddr("trace", "true"));
*  try {
*      ObjectFactory objectFactory = (ObjectFactory)Class.forName(objFactoryName).newInstance();
*      xads = (XADataSource)objectFactory.getObjectInstance(ref, null, null, null);
*      XAConnection xacon = xads.getXAConnection();
*      Connection con = xacon.getConnection();
*  } catch (Exception ex) {
*      ex.printStackTrace();
*      System.err.println("Exception caught: " + ex);
*  }
*  </blockquote></pre>
*  The following classes implement the javax.naming.Referenceable interface.
*  @see com.ibm.as400.access.AS400JDBCDataSource
*  @see com.ibm.as400.access.AS400JDBCConnectionPoolDataSource
**/
public class AS400JDBCObjectFactory implements ObjectFactory
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

   /**
   *  Returns the object requested.
   *  @param referenceObject The object reference.
   *  @param name The object name.
   *  @param nameContext The context of the name.
   *  @param environment The environment.
   *  @return The object requested.
   *  @exception Exception If an error occurs during object creation.
   **/
   public Object getObjectInstance(Object referenceObject,
					Name name,
					Context nameContext,
					Hashtable environment) throws Exception
   {
      Reference reference = (Reference)referenceObject;

      if (reference.getClassName().equals("com.ibm.as400.access.AS400JDBCDataSource"))
      {
         AS400JDBCDataSource dataSource = new AS400JDBCDataSource(reference);  // @F0M
         // @F0D dataSource.setProperties(reference);
         return dataSource;
      }
      else if (reference.getClassName().equals("com.ibm.as400.access.AS400JDBCConnectionPoolDataSource"))
      {
         AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource(reference);  // @F0M
         // @F0D dataSource.setProperties(reference);
         return dataSource;
      }
      else if (reference.getClassName().equals("com.ibm.as400.access.AS400JDBCXADataSource"))       // @F0A
      {                                                                                             // @F0A
         AS400JDBCXADataSource dataSource = new AS400JDBCXADataSource(reference);                   // @F0A
         return dataSource;                                                                         // @F0A
      }                                                                                             // @F0A
      else if (reference.getClassName().equals("com.ibm.as400.access.AS400JDBCManagedConnectionPoolDataSource"))  // @CPMa
      {
          AS400JDBCManagedConnectionPoolDataSource dataSource = new AS400JDBCManagedConnectionPoolDataSource(reference);
          return dataSource;
      }
      else if (reference.getClassName().equals("com.ibm.as400.access.AS400JDBCManagedDataSource"))  // @CPMa
      {
          AS400JDBCManagedDataSource dataSource = new AS400JDBCManagedDataSource(reference);
          return dataSource;
      }
      else
      {
         if (JDTrace.isTraceOn())                                              // @B1C
            JDTrace.logInformation (this, "Lookup error.  Class not found.");  // @B1C
         return null;
      }
   }
}
