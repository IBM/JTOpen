///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSimpleRow.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.util.Calendar;



/**
<p>The JDSimpleRow class implements a row of data generated
on the client.
**/
class JDSimpleRow
implements JDRow
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private String[]        fieldNames_;
    private int[]           fieldNullables_;
    private SQLData[]       sqlData_;
    private boolean[]       sqlNulls_;



/**
Constructs a JDSimpleRow object.

@param  fieldNames       Field names.
@param  sqlData          Initial contents of SQL data.  This is
                         needed immediately just to describe the
                         format.
@param  fieldNullables   Field nullables (either DatabaseMetaData.
                         columnNoNulls, columnNullable or
                         columnNullableUnknown).
**/
    JDSimpleRow (String[] fieldNames,
                 SQLData[] sqlData,
                 int[] fieldNullables)
    {
        fieldNames_     = fieldNames;
        fieldNullables_ = fieldNullables;
        sqlData_        = sqlData;
        sqlNulls_       = new boolean[sqlData_.length];
    }



/**
Constructs a JDSimpleRow object.  The format is determined
based on another JDRow object.

@param  other           The other row.
@param  clone           true if a full clone should be made, or
                        false if just references.
**/
    JDSimpleRow (JDRow otherRow, boolean clone)
        throws SQLException
    {
        int fieldCount  = otherRow.getFieldCount ();

        fieldNames_     = new String[fieldCount];
        fieldNullables_ = new int[fieldCount];
        sqlData_        = new SQLData[fieldCount];
        sqlNulls_       = new boolean[fieldCount];

        for (int i = 0; i < fieldCount; ++i) {
            fieldNames_[i]      = otherRow.getFieldName (i+1);
            fieldNullables_[i]  = otherRow.isNullable (i+1);

            if (clone) {                                                                // @E1C
                sqlData_[i]     = (SQLData) otherRow.getSQLData (i+1).clone ();
                sqlNulls_[i]    = false;                                                // @E1A
            }                                                                           // @E1A
            else {                                                                      // @E1C
                sqlData_[i]     = otherRow.getSQLData (i+1);
                sqlNulls_[i]    = otherRow.isNull (i+1);                                // @E1C
            }                                                                           // @E1A
        }
    }



/**
Sets the data.

@param  data     The data.
**/
    void setData (Object[] data)
        throws SQLException
    {
        Calendar calendar = Calendar.getInstance ();
        for (int i = 0; i < sqlData_.length; ++i)
            sqlData_[i].set (data[i], calendar, -1);
    }



/**
Sets the nulls.

@param  nulls     The nulls.
**/
    void setNulls (boolean[] nulls)
    {
        sqlNulls_ = nulls;
    }



//-------------------------------------------------------------//
//                                                             //
// INTERFACE IMPLEMENTATIONS                                   //
//                                                             //
//-------------------------------------------------------------//



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



    public int findField (String name)
        throws SQLException
    {
        for (int i = 0; i < fieldNames_.length; ++i)
            if (name.equalsIgnoreCase (fieldNames_[i]))
                return i+1;
        JDError.throwSQLException (JDError.EXC_COLUMN_NOT_FOUND);
        return -1;
    }



    public int getFieldCount ()
    {
        return fieldNames_.length;
    }



    public String getFieldName (int index)
        throws SQLException
    {
        try {
            return fieldNames_[index-1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return null;
        }
    }



    /* @C1D
    public int getFieldPrecision (int index)
        throws SQLException
    {
        try {
            return sqlData_[index-1].getPrecision ();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return -1;
        }
    }



    public int getFieldScale (int index)
        throws SQLException
    {
        try {
            return sqlData_[index-1].getMaximumScale ();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return -1;
        }
    }
    */



    public SQLData getSQLData (int index)
        throws SQLException
    {
        try {
            return sqlData_[index-1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return null;
        }
    }



    public SQLData getSQLType (int index)
        throws SQLException
    {
        try {
            return sqlData_[index-1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return null;
        }
    }



    public boolean isNull (int index)
        throws SQLException
    {
        try {
            return sqlNulls_[index-1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return false;
        }
    }



    public int isNullable (int index)
        throws SQLException
    {
        try {
            return fieldNullables_[index-1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DESCRIPTOR_INDEX_INVALID, e);
            return -1;
        }
    }



}
