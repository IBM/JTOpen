///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCXAConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;



/**
The AS400JDBCXACConnection class represents a pooled connection
object that provides hooks for connection pool management and
XA resource management.

<p>Because transaction boundaries are controlled by a 
transaction manager, the caller cannot explicitly commit 
or rollback on connections pooled by this object.  In addition, 
auto commit is initialized to false and cannot be set to true.

<p>This support is only available on AS/400s running the release after v4r5, or later.

<p>The following example creates an AS400JDBCXAConnection object 
that can be used to manage XA transactions.

<pre><blockquote>
// Create an XA data source for making the XA connection.
AS400JDBCXADataSource xaDataSource = new AS400JDBCXADataSource("myAS400");
xaDataSource.setUser("muUser");
xaDataSource.setPassword("myPasswd");

// Get an XAConnection and get the associated XAResource.
// This provides access to the resource manager.
XAConnection xaConnection = xaDataSource.getXAConnection();
XAResource xaResource = xaConnection.getXAResource();

// ... work with the XA resource.

// Close the XA connection when done.  This implicitly
// closes the XA resource.
xaConnection.close();
</blockquote></pre>

@see AS400JDBCXADataSource
@see AS400JDBCXAResource
**/
public class AS400JDBCXAConnection 
extends AS400JDBCPooledConnection
implements XAConnection
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private AS400JDBCConnection connection_             = null;
    private AS400JDBCXAResource xaResource_             = null;



/**
Constructs an AS400JDBCXAConnection object.

@param connection The connection to use for XA flows.
**/
    AS400JDBCXAConnection(Connection connection)
    throws SQLException
    {
        super(connection);
        connection_ = (AS400JDBCConnection)connection;
    }



/** 
Closes the physical connection.  This closes the associated
XA resource, if any.

@exception SQLException If an error occurs.
**/
/* @A1D
    public void close() 
    throws SQLException
    {
        super.close();

        if (xaResource_ != null) {
            try {
                xaResource_.close();
            }
            catch(XAException e) {
                JDError.throwSQLException(JDError.EXC_INTERNAL, e);
            }
            xaResource_ = null;
        }
    }
    */

                           

/**
Returns the XA resource associated with this connection.
This implicitly opens the resource the first time it is
called.

@return The XA resource.

@exception SQLException If an error occurs.
**/
    public XAResource getXAResource()
    throws SQLException
    {
        if (connection_ == null)
            JDError.throwSQLException (JDError.EXC_CONNECTION_NONE);
        if (connection_.isClosed())
            JDError.throwSQLException (JDError.EXC_CONNECTION_NONE);

        // The spec says we should create at most one XA resource for
        // each connection, and return the same one on subsequent.
        if (xaResource_ == null) {
            try {
                xaResource_ = new AS400JDBCXAResource(connection_);                   
            }
            catch(XAException e) {
                JDError.throwSQLException(JDError.EXC_CONNECTION_UNABLE, e);
            }
        }

        return xaResource_;
    }


}
