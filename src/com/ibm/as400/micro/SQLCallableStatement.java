//////////////////////////////////////////////////////////////////////
//
// IBM Confidential
//
// OCO Source Materials
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// 5722-JC1
// (C) Copyright IBM Corp. 2001
//
////////////////////////////////////////////////////////////////////////
//
// File Name:    SQLCallableStatement.java
//
// Description:  See comments below
//
// Classes:       SQLCallableStatement
//
////////////////////////////////////////////////////////////////////////
//
// CHANGE ACTIVITY:
//
//  Flg=PTR/DCR   Release       Date       Userid    Comments
//        D98585    v5r2m0.jacl  08/01/01   wiedrich Created
//
// END CHANGE ACTIVITY
//
////////////////////////////////////////////////////////////////////////
package com.ibm.as400.micro;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  The SQLCallableStatement class runs a stored procedure.  This class allows the user to call  any stored procedure
 *  from a wireless device.  This class provides a modified subset of the JDBC functions available in 
 *  the com.ibm.as400.access package.
 *
 *  Parameters are indexed sequentially, by number, starting at 1.  Input and Ouput parameters are supported, but
 *  InOut parameters are not supported.  The only output parameters that are NOT supported are:  ARRAY, DISTINCT,
 *  JAVA_OBJECT, REF, STRUCT, and TINYINT.  
 *
 *  The IBM Toolbox for Java JDBC driver can use different JDBC driver implementations based on the environment. 
 *  If the environment is an iSeries Java Virtual Machine on the same iSeries as the database to which the program
 *  is connecting, the native iSeries Developer Kit for Java JDBC driver can be used. In any other environment, the 
 *  IBM Toolbox for Java JDBC driver is used.
 *
 *  <P>The following example demonstrates the use of SQLCallableStatement:
 *  <br>
 *  <pre>
 *   AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
 *   try
 *   {
 *       // Create a hashtable containing any SQL connection properties.
 *       Hashtable properties = new Hashtable();
 *       properties.put("package cache", "true");
 *       properties.put("trace", "true");
 
 *       // Execute an SQL callable statement.
 *       SQLCallableStatement cs = new SQLCallableStatement(system, "call mycollection.myprocedure (?,?)");
 *       
 *       // Set the connection properties.
 *       cs.setInfo(prop);
 *
 *       // Set the input parameter.
 *       cs.setInput(1, "332");
 *       
 *       // Set the output parameter.
 *       cs.setOutput(2, SQLCallableStatement.INTEGER);
 *
 *       // Get the data from the output parameter.
 *       String[] column = cs.getOutputData();
 *       
 *       // Display the output data.
 *       for (int i=0; i<column.length; ++i)
 *       {
 *           // Display the output data here.
 *       }
 *   }
 *   catch (Exception e)
 *   {
 *       // Handle the exception
 *   }
 *   // Done with the system object.
 *   system.disconnect();
 *  </pre>
 **/
public final class SQLCallableStatement extends SQLStatement
{
    // These constants match the int values in the java.sql.Types class.
    // The java.sqlTypes that are not supported include:  ARRAY,
    // DISTINCT, JAVA_OBJECT, REF, STRUCT, and TINYINT


    /**
     *  The constant that identifies the SQL parameter as input.
     **/
    public static final int TYPE_INPUT = MEConstants.SQL_TYPE_INPUT;

    /**
     *  The constant that identifies the generic SQL parameter as output.
     **/
    public static final int TYPE_OUTPUT = MEConstants.SQL_TYPE_OUTPUT;

    /**
     *  The constant that identifies the generic SQL output  type BIGINT.
     **/
    public static final int BIGINT = -5;

    /**
     *  The constant that identifies the generic SQL output  type BINARY.
     **/
    public static final int BINARY = -2;

    /**
     *  The constant that identifies the generic SQL output  type BIT.
     **/
    public static final int BIT = -7;

    /**
     *  The constant that identifies the generic SQL output  type CHAR.
     **/
    public static final int CHAR = 1;

    /**
     *  The constant that identifies the generic SQL output  type DATE.
     **/
    public static final int DATE = 91;

    /**
     *  The constant that identifies the generic SQL output  type DECIMAL.
     **/
    public static final int DECIMAL = 3;

    /**
     *  The constant that identifies the generic SQL output  type DOUBLE.
     **/
    public static final int DOUBLE = 8;

    /**
     *  The constant that identifies the generic SQL output  type FLOAT.
     **/
    public static final int FLOAT = 6;

    /**
     *  The constant that identifies the generic SQL output  type INTEGER.
     **/
    public static final int INTEGER = 4;

    /**
     *  The constant that identifies the generic SQL output  type LONGVARBINARY.
     **/
    public static final int LONGVARBINARY = -4;

    /**
     *  The constant that identifies the generic SQL output  type LONGVARCHAR.
     **/
    public static final int LONGVARCHAR = -1;

    /**
     *  The constant that identifies the generic SQL output  type NULL.
     **/
    public static final int NULL = 0;

    /**
     *  The constant that identifies the generic SQL output  type NUMERIC.
     **/
    public static final int NUMERIC = 2;

    /**
     *  The constant that identifies the generic SQL output  type REAL.
     **/
    public static final int REAL = 7;

    /**
     *  The constant that identifies the generic SQL output  type SMALLINT.
     **/
    public static final int SMALLINT = 5;

    /**
     *  The constant that identifies the generic SQL output  type TIME.
     **/
    public static final int TIME = 92;

    /**
     *  The constant that identifies the generic SQL output  type TIMESTAMP.
     **/
    public static final int TIMESTAMP = 93;

    /**
     *  The constant that identifies the generic SQL output  type VARBINARY.
     **/
    public static final int VARBINARY = -3;

    /**
     *  The constant that identifies the generic SQL output  type VARCHAR.
     **/
    public static final int VARCHAR = 12;

    // Private data.
    private AS400 system_ = null;
    private String sql_ = null;                    // The sql statement.
    private Hashtable info_ = null;             // The connection properties.
    private Hashtable input_ = null;           // The input parameters.
    private Hashtable output_ = null;         // The output parameters.



    /**
     *  The SQLCallableStatement class runs a stored procedure. 
     *
     *  Parameters are indexed sequentially, by number, starting at 1. The caller must set 
     *  output parameters before executing the stored procedure. 
     *
     *  @param system The iSeries server.
     *  @param sql       The SQL stored procedure call.
     **/
    public SQLCallableStatement(AS400 system, String sql)
    {
        if (system == null)
            throw new NullPointerException("system");

        if (sql == null)
            throw new NullPointerException("sql");

        system_ = system;
        sql_ = sql;
    }


    /**
     *   Set the connection properties.
     *
     *  @param info The connection properties.
     **/
    public void setInfo(Hashtable info)
    {
        if (info == null)
            throw new NullPointerException("info");

        info_ = info;
    }


    /**
     *  Sets an input parameter.
     *
     *  @param parameterIndex The parameter index (1-based).
     *  @param parameterValue The parameter value.
     **/
    public void setInput(int parameterIndex, String parameterValue)
    {
        if (input_ == null)
            input_ = new Hashtable();

        input_.put(new Integer(parameterIndex), parameterValue == null ? "null" : parameterValue);
    }


    /**
     *  Sets an output parameter.
     *
     *  @param parameterIndex  The parameter index (1- based).
     *  @param parameterType   The SQL stored procedure call output parameter type.  The valid types are defined in the SQLCallableStatement 
     *                                       class.  The only output parameters that are NOT supported are:  ARRAY, DISTINCT, JAVA_OBJECT, REF, 
     *                                       STRUCT, and TINYINT.  
     **/
    public void setOutput(int parameterIndex, int parameterType)
    {
        if (output_ == null)
            output_ = new Hashtable();

        output_.put(new Integer(parameterIndex), new Integer(parameterType));
    }


    /**
     *  Set the SQL statement.
     *
     *  @param sql The SQL stored procedure call.
     **/
    public void setSQL(String sql)
    {
        if (sql == null)
            throw new NullPointerException("sql");

        sql_ = sql;
    }


    /**
     *  Runs an SQL callable statement that returns a single result set. 
     *
     *  @return The result set that contains the data produced by the query.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public SQLResultSet executeQuery() throws IOException, MEException
    {
        synchronized(system_)
        {
            system_.connect();
            system_.toServer_.writeInt(MEConstants.SQL_CALLABLE_QUERY);

            sendToServer();

            int retVal = system_.fromServer_.readInt();

            if (retVal == MEConstants.SQL_STATEMENT_SUCCEEDED)
            {
                int transactionID = system_.fromServer_.readInt();

                return new SQLResultSet(system_, transactionID);
            }
            else
                throw new MEException(system_.fromServer_.readUTF(), retVal);
        }
    }


    /**
     *  Runs an SQL stored procedure that returns output data.
     *
     *  @return A String array containing the values of the output parameter.
     *
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public String[] getOutputData() throws IOException, MEException
    {
        synchronized(system_)
        {
            system_.connect();
            system_.toServer_.writeInt(MEConstants.SQL_CALLABLE_GET_OUTPUT);

            sendToServer();

            int retVal = system_.fromServer_.readInt();

            if (retVal == MEConstants.SQL_STATEMENT_SUCCEEDED)
            {
                int numOutputData = system_.fromServer_.readInt();

                String[] data = new String[numOutputData];

                for (int i=0; i<numOutputData; ++i)
                {
                    data[i] = system_.fromServer_.readUTF();
                }

                return data;
            }
            else
                throw new MEException(system_.fromServer_.readUTF(), retVal);
        }
    }


    /**
     *  Helper method used to send the properties, sql query, and parameters to the MEServer.
     **/
    private void sendToServer() throws IOException
    {
        // The size of the hashtables.
        int size;

        if (info_ == null)
        {
            system_.toServer_.writeInt(MEConstants.NO_CONNECTION_PROPERTIES);
            system_.toServer_.flush();
        }
        else
        {
            // Write the number of sql url properties specified in the hashtable.
            size = info_.size();
            system_.toServer_.writeInt(size);
            system_.toServer_.flush();

            // Loop through the hashtable and write out the key (sql property name)
            // and the key value (sql property value)
            for (Enumeration e = info_.keys() ; e.hasMoreElements() ;)
            {
                String key = (String)e.nextElement();
                system_.toServer_.writeUTF(key);
                system_.toServer_.writeUTF((String) info_.get(key));
            }
            system_.toServer_.flush();
        }

        system_.toServer_.writeUTF(sql_);
        system_.toServer_.flush();


        if (input_ != null)
        {
            // Write the number of callable statement input parameters.
            size = input_.size();
            system_.toServer_.writeInt(size);
            system_.toServer_.flush();

            // Loop through the hashtable and write out the key (sql parameter type)
            // and the key value (sql parameter or output type)
            for (Enumeration e = input_.keys() ; e.hasMoreElements() ;)
            {
                Integer index = (Integer)e.nextElement();
                system_.toServer_.writeInt(index.intValue());
                system_.toServer_.writeUTF((String) input_.get(index));
            }
            system_.toServer_.flush();
        }
        else
        {
            system_.toServer_.writeInt(MEConstants.NO_CALLABLE_PARAMETERS);
            system_.toServer_.flush();
        }

        if (output_ != null)
        {
            // Write the number of callable statement input parameters.
            size = output_.size();
            system_.toServer_.writeInt(size);
            system_.toServer_.flush();

            // Loop through the hashtable and write out the key (sql parameter type)
            // and the key value (sql parameter or output type)
            for (Enumeration e = output_.keys() ; e.hasMoreElements() ;)
            {
                Integer index = (Integer)e.nextElement();
                system_.toServer_.writeInt(index.intValue());
                system_.toServer_.writeInt( ((Integer) output_.get(index)).intValue() );
            }
            system_.toServer_.flush();
        }
        else
        {
            system_.toServer_.writeInt(MEConstants.NO_CALLABLE_PARAMETERS);
            system_.toServer_.flush();
        }
    }
}