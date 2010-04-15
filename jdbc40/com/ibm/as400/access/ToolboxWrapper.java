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
/* ifdef JDBC40 */
import java.sql.Wrapper;
/* endif */ 


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
/* ifdef JDBC40 */
implements Wrapper 
/* endif */ 
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

     /**
      * Returns true if this either implements the interface argument or is
      * directly or indirectly a wrapper for an object that does. Returns false
      * otherwise. If this implements the interface then return true, else if
      * this is a wrapper then return the result of recursively calling
      * <code>isWrapperFor</code> on the wrapped object. If this does not
      * implement the interface and is not a wrapper, return false. This method
      * should be implemented as a low-cost operation compared to
      * <code>unwrap</code> so that callers can use this method to avoid
      * expensive <code>unwrap</code> calls that may fail. If this method
      * returns true then calling <code>unwrap</code> with the same argument
      * should succeed.
      * 
      * @param iface
      *            a Class defining an interface.
      * @return true if this implements the interface or directly or indirectly
      *         wraps an object that does.
      * @throws java.sql.SQLException
      *             if an error occurs while determining whether this is a
      *             wrapper for an object with the given interface.
      */
/* ifdef JDBC40 */
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
/* endif */ 
     /**
      * Returns an object that implements the given interface to allow access to
      * non-standard methods, or standard methods not exposed by the proxy.
      * 
      * If the receiver implements the interface then the result is the receiver
      * or a proxy for the receiver. If the receiver is a wrapper and the wrapped
      * object implements the interface then the result is the wrapped object or
      * a proxy for the wrapped object. Otherwise return the the result of
      * calling <code>unwrap</code> recursively on the wrapped object or a
      * proxy for that result. If the receiver is not a wrapper and does not
      * implement the interface, then an <code>SQLException</code> is thrown.
      * 
      * @param iface
      *            A Class defining an interface that the result must implement.
      * @return an object that implements the interface. May be a proxy for the
      *         actual implementing object.
      * @throws java.sql.SQLException
      *             If no object found that implements the interface
      */
/* ifdef JDBC40 */
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
/* endif */ 
}
