///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDRow.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;



/**
<p>The JDRow interface represents a row of data with access to
all fields.  It is intended that the row will be reused for
entire result set, prepared statement, etc., so as to minimize
object creation.
**/
interface JDRow
{



/**
Return the index for the specified field name.


@param      name            The field name.
@return                     The field index (1-based).

@exception  SQLException    If the name is not
                            found or an error occurs.
**/
    public abstract int findField (String name)
        throws SQLException;



/**
Return the number of fields.

@return     The number of fields.

@exception  SQLException    If an error occurs.
**/
    public abstract int getFieldCount ();



/**
Return the name of a field.

@param  index   The field index (1-based).
@return         The field name.

@exception  SQLException    If the index is invalid
                            or an error occurs.
**/
    public abstract String getFieldName (int index)
        throws SQLException;



/**
Return the precision for a field.  This is the number
of decimal digits the field may hold.

@param  index   The field index (1-based).
@return         The precision.

@exception  SQLException    If the index is invalid
                            or an error occurs.
**/
// @C1D    public abstract int getFieldPrecision (int index)
// @C1D        throws SQLException;



/**
Return the scale for a field.  This is number of digits
to the right of the decimal point.

@param  index   The field index (1-based).
@return         The scale.

@exception  SQLException    If the index is invalid
                            or an error occurs.
**/
// @C1D    public abstract int getFieldScale (int index)
// @C1D        throws SQLException;



/**
Return the SQL data object a field.

@param  index   The field index (1-based).
@return         The SQL data object.

@exception  SQLException    If the index is invalid
                            or an error occurs.
**/
    public abstract SQLData getSQLData (int index)
        throws SQLException;



/**
Return the SQL data object a field intended for type
information only.  This method does not perform any
data copying or converion, it just returns a representative
useful only for gathering type information.

@param  index   The field index (1-based).
@return         The SQL data object.

@exception  SQLException    If the index is invalid
                            or an error occurs.
**/
    public abstract SQLData getSQLType (int index)
        throws SQLException;



/**
Is the field value SQL NULL?

@param      index   The field index (1-based).
@return             true or false

@exception  SQLException    If the index is invalid or an
                            error occurs.
**/
    public abstract boolean isNull (int index)
        throws SQLException;



/**
Can the field contain a SQL NULL value?

@param  index   The field index (1-based).
@return         true if nullable.

@exception  SQLException    If the index is invalid
                            or an error occurs.
**/
    public abstract int isNullable (int index)
        throws SQLException;



}

