//////////////////////////////////////////////////////////////////////
//
// IBM Confidential
//
// OCO Source Materials
//
// The Source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office
//
// 5722-JC1
// (C) Copyright IBM Corp. 2002
//
////////////////////////////////////////////////////////////////////////
//
// File Name:    OutputParameterData.java
//
// Description:  See comments below
//
// Classes:      OutputParameterData
//
////////////////////////////////////////////////////////////////////////
//
// CHANGE ACTIVITY:
//
//  Flg=PTR/DCR   Release       Date        Userid     Comments
//        D98585    v5r2m0.jacl  08/21/01    wiedrich   Created.
//
// END CHANGE ACTIVITY
//
////////////////////////////////////////////////////////////////////////
package com.ibm.as400.micro;


/**
 *  The OutputParameterData class is used to store the parameterIndex and
 *  parameterType of an SQLCallableStatement output parameter.
 *
 **/
public class OutputParameterData
{
    int index_;
    int type_;

    /**
     *  Package scope constructor used to hold information about SQLCallableStatement
     *  output parameters.
     *
     *  @param parameterIndex  The parameter index (1- based).
     *  @param parameterType   The SQL stored procedure call output parameter type.  The valid types are defined in the SQLCallableStatement 
     *                                       class.  The only output parameters that are NOT supported are:  ARRAY, DISTINCT, JAVA_OBJECT, REF, 
     *                                       STRUCT, and TINYINT.  
     **/
    OutputParameterData(int parameterIndex, int parameterType)
    {
        index_ = parameterIndex;
        type_ = parameterType;
    }
}
