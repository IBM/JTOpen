///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCParameterMetaData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.ParameterMetaData;
import java.sql.SQLException;


/**
The AS400JDBCParameterMetaData class can be used to get information about the types 
and properties of the parameters in a PreparedStatement object.  

Use PreparedStatement.getParameterMetaData() to create new ParameterMetaData objects.
**/
public class AS400JDBCParameterMetaData
           implements ParameterMetaData                         
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


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
    Returns the fully-qualified name of the Java class whose 
    instances should be passed to the method PreparedStatement.setObject. 
    
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
    Returns the number of parameters in the PreparedStatement about
    which this ParameterMetaData object contains information. 

    @return                   The number of parameters in the ParameterMetaData object.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getParameterCount ()
    throws SQLException
    {
        return prepStmt_.getParameterCount();
    }



    /**
    Returns the designated parameter's mode. 

    @param  parameterIndex    The parameter index (1-based)
    @return                   The mode of the parameter; one of ParameterMetaData.parameterModeIn, 
                              ParameterMetaData.parameterModeOut, 
                              ParameterMetaData.parameterModeInOut, or 
                              ParameterMetaData.parameterModeUnknown.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getParameterMode (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getParameterMode(parameterIndex);
    }



    /**
    Returns the designated parameter's SQL type. 

    @param  parameterIndex    The parameter index (1-based)
    @return                   The SQL type of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getParameterType (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getParameterType(parameterIndex);
    }



    /**
    Returns the designated parameter's database-specific type name. 

    @param  parameterIndex    The parameter index (1-based)
    @return                   The type name of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public String getParameterTypeName (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getParameterTypeName(parameterIndex);
    }



    /**
    Returns the designated parameter's number of decimal digits. 

    @param  parameterIndex    The parameter index (1-based)
    @return                   The precision of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getPrecision (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getPrecision(parameterIndex);
    }



    /**
    Returns the designated parameter's number of digits
    to the right of the decimal point. 

    @param  parameterIndex    The parameter index (1-based)
    @return                   The scale of the parameter.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int getScale (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.getScale(parameterIndex);
    }



    /**
    Returns if the designated parameter can be null.   

    @param  parameterIndex    The parameter index (1-based)
    @return                   If the parameter can be null; one of 
                              ParameterMetaData.parameterNoNulls, 
                              ParameterMetaData.parameterNullable, or
                              ParameterMetaData.parameterNullableUnknown.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public int isNullable (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.isNullable(parameterIndex);
    }



    /**
    Returns if values for the designated parameter can be signed numbers. 

    @param  parameterIndex    The parameter index (1-based)
    @return                   Returns true if values can be signed number, false otherwise.

    @exception  SQLException  If the prepared statement is not open.
    **/
    public boolean isSigned (int parameterIndex)
    throws SQLException
    {
        return prepStmt_.isSigned(parameterIndex);
    }

}

