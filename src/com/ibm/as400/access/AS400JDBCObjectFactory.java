///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCObjectFactory.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
*  The following classes implement the javax.naming.Referenceable interface.
*  @see com.ibm.as400.access.AS400JDBCDataSource
*  @see com.ibm.as400.access.AS400JDBCConnectionPoolDataSource
**/
public class AS400JDBCObjectFactory implements ObjectFactory
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
         AS400JDBCDataSource dataSource = new AS400JDBCDataSource();      
         dataSource.setProperties(reference);
         return dataSource;
      }
      else if (reference.getClassName().equals("com.ibm.as400.access.AS400JDBCConnectionPoolDataSource"))
      {
         AS400JDBCConnectionPoolDataSource dataSource = new AS400JDBCConnectionPoolDataSource();      
         dataSource.setProperties(reference);
         return dataSource;
      }
      else
      {
         if (Trace.isTraceOn()) 
            Trace.log(Trace.ERROR, "Lookup error.  Class not found.");
         return null;
      }
   }
}
