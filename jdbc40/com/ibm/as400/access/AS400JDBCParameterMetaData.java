///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCParameterMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.ParameterMetaData;
import java.sql.SQLException;


/**
The AS400JDBCParameterMetaData class can be used to retrieve information about the parameters 
of a PreparedStatement.  

Use PreparedStatement.getParameterMetaData() to create new ParameterMetaData objects.
**/
public class AS400JDBCParameterMetaData
/* ifdef JDBC40 */
extends ToolboxWrapper
/* endif */ 
           implements ParameterMetaData                         
{
  final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";


    // Private data.
    private AS400JDBCPreparedStatement prepStmt_;



    /**
    Constructs an AS400JDBCParameterMetaData object.
    **/
    AS400JDBCParameterMetaData (AS400JDBCPreparedStatement prepStmt)
    { 
        prepStmt_ = prepStmt;
    }



    /**
    Returns the fully-qualified name of the Java class of the specified parameter. 
    
    @param  parameterIndex    The parameter index (1-based).
    @return                   The fully-qualified name of the Java class.
    
    @exception  SQLException  If the prepared statement is not open.
    **/
    public String getParameterClassName (int parameterIndex)
    throws SQLException
    {   
        return prepStmt_.getParameterClassName(parameterIndex);
    }



    /**
    Returns the number of parameters in this ParameterMetaData object. 

    @return                   The number of parameters in the ParameterMetaData object.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getParameterCount ()
    throws SQLException
    {
        return prepStmt_.getParameterCount();
    }



    /**
    Returns the mode of the specified parameter or if that information is unknown. 

    @param  parameterIndex    The parameter index (1-based).
    @return                   The mode of the parameter.  Valid values are 
                              ParameterMetaData.parameterModeIn, 
                              ParameterMetaData.parameterModeOut, 
                              ParameterMetaData.parameterModeInOut, and 
                              ParameterMetaData.parameterModeUnknown.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getParameterMode (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getParameterMode(parameterIndex);
    }



    /**
    Returns the SQL type of the specified parameter. 

    @param  parameterIndex    The parameter index (1-based).
    @return                   The SQL type of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getParameterType (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getParameterType(parameterIndex);
    }



    /**
    Returns the database-specific type name of the specified parameter. 

    @param  parameterIndex    The parameter index (1-based).
    @return                   The type name of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public String getParameterTypeName (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getParameterTypeName(parameterIndex);
    }



    /**
    Returns the number of decimal digits of the specified parameter. 

    @param  parameterIndex    The parameter index (1-based).
    @return                   The precision of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getPrecision (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getPrecision(parameterIndex);
    }



    /**
    Returns the number of digits to the right of the decimal point 
    of the specified parameter. 

    @param  parameterIndex    The parameter index (1-based).
    @return                   The scale of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getScale (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getScale(parameterIndex);
    }



    /**
    Returns if the specified parameter can be null or that information is unknown.   

    @param  parameterIndex    The parameter index (1-based).
    @return                   Returns if the parameter can be null.  The valid values are 
                              ParameterMetaData.parameterNoNulls, 
                              ParameterMetaData.parameterNullable, and
                              ParameterMetaData.parameterNullableUnknown.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int isNullable (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.isNullable(parameterIndex);
    }



    /**
    Returns if values can be signed numbers for the specified parameter. 

    @param  parameterIndex    The parameter index (1-based).
    @return                   Returns true if values for the specified parameter
                              can be signed numbers, false otherwise.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public boolean isSigned (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.isSigned(parameterIndex);
    }

    //@pda jdbc40
    protected String[] getValidWrappedList()
    {
        return new String[] {  "com.ibm.as400.access.AS400JDBCParameterMetaData", "java.sql.ParameterMetaData" };
    } 
 
}

