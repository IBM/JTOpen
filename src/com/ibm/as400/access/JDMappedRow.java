///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDMappedRow.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.util.Calendar;



/**
<p>The JDMappedRow class implements a row of data that
must be mapped according to maps.
**/
class JDMappedRow
implements JDRow
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private JDFieldMap[]    fieldMaps_;
    private JDRow           fromRow_;
    private JDSimpleRow     toRow_;



/**
Constructs a JDMappedRow object with field names and
types generated on the client, and maps to map other
formats into this one.

@param  formatRow   The row describing the format.
@param  fromRow     The row to map.
**/
    JDMappedRow (JDSimpleRow formatRow,
                 JDFieldMap[] fieldMaps)
    {
        fieldMaps_      = fieldMaps;
        fromRow_        = null;
        toRow_          = formatRow;
    }



/**
Constructs a JDMappedRow object with field names and
types generated on the client, and maps to map other
formats into this one.

@param  fieldNames       Field names.
@param  sqlData          Initial contents of SQL data.  This is
                         needed immediately just to describe the
                         format.
@param  fieldNullables   Field nullables (either DatabaseMetaData.
                         columnNoNulls, columnNullable or
                         columnNullableUnknown).
@param  fromRow          The row to map.
**/
    JDMappedRow (String[] fieldNames,
                 SQLData[] sqlData,
                 int[] fieldNullables,
                 JDFieldMap[] fieldMaps)
    {
        fieldMaps_      = fieldMaps;
        fromRow_        = null;
        toRow_          = new JDSimpleRow (fieldNames, sqlData,
                            fieldNullables);
    }



/**
Sets the row to map from.

@param  fromRow     The row to map from.
**/
    void setRow (JDRow fromRow)
    {
        fromRow_ = fromRow;
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
        return toRow_.findField (name);
    }



    public int getFieldCount ()
    {
        return toRow_.getFieldCount ();
    }



    public String getFieldName (int index)
        throws SQLException
    {
        return toRow_.getFieldName (index);
    }



    /* @C1D
    public int getFieldPrecision (int index)
        throws SQLException
    {
        return toRow_.getFieldPrecision (index);
    }



    public int getFieldScale (int index)
        throws SQLException
    {
        return toRow_.getFieldScale (index);
    }
    */



    public SQLData getSQLData (int index)
        throws SQLException
    {
        SQLData toData = toRow_.getSQLData (index);
        toData.set (fieldMaps_[index-1].getValue (fromRow_),
            Calendar.getInstance (), -1);

        // @A0A
        // Added code to trim the data if it is of SQLChar
        // or SQLVarchar type.
        if (toData instanceof SQLVarchar)                   // @A0A
            ((SQLVarchar) toData).trim();                   // @A0A
        else if (toData instanceof SQLChar)                 // @A0A
            ((SQLChar) toData).trim();                      // @A0A

        return toData;
	}



    public SQLData getSQLType (int index)
        throws SQLException
    {
        return toRow_.getSQLData (index);
	}



    public boolean isNull (int index)
        throws SQLException
    {
        return fieldMaps_[index-1].isNull (fromRow_);
    }



    public int isNullable (int index)
        throws SQLException
    {
        return toRow_.isNullable (index);
    }


}
