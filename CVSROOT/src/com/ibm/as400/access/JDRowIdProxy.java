///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDRowIdProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
/* ifdef JDBC40 
import java.sql.RowId;
endif */ 

//@PDA jdbc40 new class

/**
The JDRowIdProxy class provides access to binary large
objects.  The data is valid only within the current
transaction.
**/
class JDRowIdProxy
extends AbstractProxyImpl
/* ifdef JDBC40 
implements RowId
endif */ 

{
 
  // Copied from JDError:
  private static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";


  public byte[] getBytes ( )
  {
    try {
      return (byte[]) connection_.callMethod (pxId_, "getBytes").getReturnValue();
    }
    catch (InvocationTargetException e) {
    
        return null;  //interface does not throw SQLException as of current version
    }
  }

    
    public String toString()
    {
        try {
            return (String) connection_.callMethod (pxId_, "toString").getReturnValue();
          }
          catch (InvocationTargetException e) {
             
              return null;  //interface does not throw SQLException as of current version
          }
    }

    public boolean equals(Object obj)
    {
        try {
            return connection_.callMethod(pxId_, "equals",     
                    new Class[] { Object.class },
                    new Object[] { obj }, false).getReturnValueBoolean();
          }
          catch (InvocationTargetException e) {
             
              return false;  //interface does not throw SQLException as of current version
          }
    }
    
    public int hashCode()
    {
        try {
            return connection_.callMethod(pxId_, "hashCode").getReturnValueInt();
          }
          catch (InvocationTargetException e) {
               
              return 0;  //interface does not throw SQLException as of current version
          }
    }
}
