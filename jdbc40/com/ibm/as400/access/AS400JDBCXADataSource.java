///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCXADataSource.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.sql.XAConnection;
import javax.sql.XADataSource;


/**
The AS400JDBCXADataSource class represents a factory for 
AS400JDBCXAConnection objects.

<p>
This support is only available when connecting to systems running OS/400 V5R1 or later, or IBM i.

<p>The following example creates an AS400JDBCXADataSource 
object and creates a connection to the database.

<pre><blockquote>
// Create an XA data source for making the connection.
AS400JDBCXADataSource xaDataSource = new AS400JDBCXADataSource("myAS400");
xaDataSource.setUser("myUser");
xaDataSource.setPassword("myPasswd");

// Get the XAConnection.
XAConnection xaConnection = xaDataSource.getXAConnection();
</blockquote></pre>

@see AS400JDBCXAConnection
@see AS400JDBCXAResource
**/
public class AS400JDBCXADataSource
extends AS400JDBCDataSource  
implements XADataSource
{
  private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

/**
Constructs a default AS400JDBCXADataSource object.
**/
   public AS400JDBCXADataSource()
   {
      super();
   }



/**
Constructs an AS400JDBCXADataSource with the specified <i>serverName</i>.

@param serverName The name of the IBM i system.
**/
   public AS400JDBCXADataSource(String serverName)
   {
      super(serverName);
   }



/**
Constructs an AS400JDBCXADataSource with the specified signon information.
   
@param serverName The name of the IBM i system.
@param user The user id.
@param password The password.
**/
   public AS400JDBCXADataSource(String serverName, String user, String password)
   {
      super(serverName, user, password);
   }


//@A1A
/**
Constructs an AS400JDBCXADataSource with the specified signon information 
to use for SSL communications with the IBM i system.
   
@param serverName The name of the IBM i system.
@param user The user id.
@param password The password.
@param keyRingName The key ring class name to be used for SSL communications with the system.
@param keyRingPassword The password for the key ring class to be used for SSL communications with the system.	
**/
   public AS400JDBCXADataSource(String serverName, String user, String password, 
				String keyRingName, String keyRingPassword)
   {
      super(serverName, user, password, keyRingName, keyRingPassword);
   }


// @F0A - added the following constructor to avoid some object construction
/**
*  Constructs an AS400JDBCXADataSource from the specified Reference
*  @param reference to retrieve DataSource properties from
**/
    AS400JDBCXADataSource(Reference reference) {
        super(reference);
    }


/**
Returns an XA connection to IBM i.
   
@return An XA connection.
@exception SQLException If a database error occurs.
**/
    public XAConnection getXAConnection() 
    throws SQLException
    {
        AS400JDBCConnection connection = (AS400JDBCConnection)getConnection();
        return new AS400JDBCXAConnection(connection);
    }



/**
Returns an XA connection to IBM i.

@param user The userid for the connection.
@param password The password for the connection.
@return An XA connection.
@exception SQLException If a database error occurs.
**/
   public XAConnection getXAConnection(String user, String password) 
   throws SQLException
   {
       AS400JDBCConnection connection = (AS400JDBCConnection)getConnection(user, password);
       return new AS400JDBCXAConnection(connection);
   }
   


/**
Returns the Reference object for the data source object.
This is used by JNDI when bound in a JNDI naming service.
Contains the information necessary to reconstruct the data source 
object when it is later retrieved from JNDI via an object factory.

@return A Reference object for the data source object.
@exception NamingException If a naming error occurs resolving the object.
**/
   public Reference getReference() 
   throws NamingException
   {
      Reference ref = new Reference(this.getClass().getName(),
                                    "com.ibm.as400.access.AS400JDBCObjectFactory", 
                                    null);

      Reference dsRef = super.getReference();
      for (int i=0; i< dsRef.size(); i++) 
         ref.add( dsRef.get(i) );
      
      return ref;
   }
   
   //@pda jdbc40
   protected String[] getValidWrappedList()
   {
       return new String[] {  "com.ibm.as400.access.AS400JDBCXADataSource", "javax.sql.XADataSource"  };
   } 

}
