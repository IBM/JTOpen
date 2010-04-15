///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ToolboxWrapper.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
/* ifdef JDBC40 
import java.sql.Wrapper;
endif */ 


/* 
 * This class provides a single point where we implement java.sql.Wrapper.
 * Any of the other classes (ie Connection, Statement, ResultSet, etc) that
 * implement java.sql.Wrapper are done so by extending this class. 
 * The original purpose of this class was so that we could have a single code base
 * for both pre jdk 1.6 and post jdk 1.6.  Then this class (containing Generics), could
 * be in a jdk 1.6 only release, and a stub could be in the pre jdk 1.6 release.  But 
 * because of so many issues with security and class loading, we did not pursue the
 * "stub" infrastructure.  But since this class design was a nice way to keep the
 * wrapper code in one class, we decided to keep it. 
 */
class ToolboxWrapper 
/* ifdef JDBC40 
implements Wrapper 
endif */ 
{

  
    //@PDA jdbc40
    //Copied from JDError.  Needed for proxy classes that extend this class.
    private static final String EXC_PARAMETER_TYPE_INVALID = "HY105";
    
    
    //this method needs to be overridden by classes that extend ToolboxWrapper to return a list of classes
    //that the class may be a wrapper of.
    protected String[] getValidWrappedList()
    {
        return new String[] { }; //return empty array
    }

  //JDBC40DOC     /**
  //JDBC40DOC      * Returns true if this either implements the interface argument or is
  //JDBC40DOC      * directly or indirectly a wrapper for an object that does. Returns false
  //JDBC40DOC      * otherwise. If this implements the interface then return true, else if
  //JDBC40DOC      * this is a wrapper then return the result of recursively calling
  //JDBC40DOC      * <code>isWrapperFor</code> on the wrapped object. If this does not
  //JDBC40DOC      * implement the interface and is not a wrapper, return false. This method
  //JDBC40DOC      * should be implemented as a low-cost operation compared to
  //JDBC40DOC      * <code>unwrap</code> so that callers can use this method to avoid
  //JDBC40DOC      * expensive <code>unwrap</code> calls that may fail. If this method
  //JDBC40DOC      * returns true then calling <code>unwrap</code> with the same argument
  //JDBC40DOC      * should succeed.
  //JDBC40DOC      * 
  //JDBC40DOC      * @param iface
  //JDBC40DOC      *            a Class defining an interface.
  //JDBC40DOC      * @return true if this implements the interface or directly or indirectly
  //JDBC40DOC      *         wraps an object that does.
  //JDBC40DOC      * @throws java.sql.SQLException
  //JDBC40DOC      *             if an error occurs while determining whether this is a
  //JDBC40DOC      *             wrapper for an object with the given interface.
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public boolean isWrapperFor(Class<?> iface) throws SQLException 
    {
        if (iface == null)
            return false;

        String[] validWrappedList = getValidWrappedList();
        for (int i = 0; i < validWrappedList.length; i++) 
        {
            if (iface.getName().equals(validWrappedList[i]))
                return true;
        }

        return false;
    }
    endif */ 
  //JDBC40DOC     /**
  //JDBC40DOC      * Returns an object that implements the given interface to allow access to
  //JDBC40DOC      * non-standard methods, or standard methods not exposed by the proxy.
  //JDBC40DOC      * 
  //JDBC40DOC      * If the receiver implements the interface then the result is the receiver
  //JDBC40DOC      * or a proxy for the receiver. If the receiver is a wrapper and the wrapped
  //JDBC40DOC      * object implements the interface then the result is the wrapped object or
  //JDBC40DOC      * a proxy for the wrapped object. Otherwise return the the result of
  //JDBC40DOC      * calling <code>unwrap</code> recursively on the wrapped object or a
  //JDBC40DOC      * proxy for that result. If the receiver is not a wrapper and does not
  //JDBC40DOC      * implement the interface, then an <code>SQLException</code> is thrown.
  //JDBC40DOC      * 
  //JDBC40DOC      * @param iface
  //JDBC40DOC      *            A Class defining an interface that the result must implement.
  //JDBC40DOC      * @return an object that implements the interface. May be a proxy for the
  //JDBC40DOC      *         actual implementing object.
  //JDBC40DOC      * @throws java.sql.SQLException
  //JDBC40DOC      *             If no object found that implements the interface
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface))
            return (T)this;
        else
        {
            throw new SQLException (
                    AS400JDBCDriver.getResource("JD" + EXC_PARAMETER_TYPE_INVALID),
                    EXC_PARAMETER_TYPE_INVALID, -99999);
            //JDError.throwSQLException(this, JDError.EXC_PARAMETER_TYPE_INVALID);
        }
        
    }
    endif */
}
