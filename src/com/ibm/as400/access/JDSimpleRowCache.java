///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDSimpleRowCache.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;
import java.util.Vector;



/**
The JDSimpleRowCache class implements a set of rows that
is contained completely on the client, without regard to
where they were generated.
**/
class JDSimpleRowCache
implements JDRowCache
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private int         index_;
    private JDSimpleRow row_;
    private Object[][]  data_;
    private boolean[][] nulls_;
    private boolean     valid_;



/**
Constructs a JDSimpleRowCache object.  This is intended for
empty results, i.e., a cache with no data.

@param  formatRow   The row describing the format.
**/
    JDSimpleRowCache (JDSimpleRow formatRow)
    {
        index_      = -1;
        valid_      = false;
        row_        = formatRow;

        int fieldCount = row_.getFieldCount ();

        data_       = new Object[0][fieldCount];
        nulls_      = new boolean[0][fieldCount];
    }



/**
Constructs a JDSimpleRowCache object.  This is intended for
SQL data generated entirely on the client.

@param  formatRow   The row describing the format.
@param  data        The data generated on the client.
@param  nulls       The nulls corresponding to the data.
**/
    JDSimpleRowCache (JDSimpleRow formatRow,
                      Object[][] data,
                      boolean[][] nulls)
    {
        index_      = -1;
        valid_      = false;
        row_        = formatRow;

        data_       = data;
        nulls_      = nulls;
    }



/**
Constructs a JDSimpleRowCache object.  This is intended for
SQL data generated entirely on the client.

@param  formatRow   The row describing the format.
@param  data        The data generated on the client.
**/
    JDSimpleRowCache (JDSimpleRow formatRow,
                      Object[][] data)
    {
        index_      = -1;
        valid_      = false;
        row_        = formatRow;

        data_       = data;
        nulls_      = new boolean[data.length][data[0].length];
    }



/**
Constructs a JDSimpleRowCache object.  This is intended for
data to be fully cached based on another row cache (most likely
one from the server).

@param  otherRowCache   The other row cache.

@exception  SQLException    If an error occurs.
**/
    JDSimpleRowCache (JDRowCache otherRowCache)
        throws SQLException
    {
        Vector tempData = new Vector ();
        Vector tempNulls = new Vector ();
        JDRow otherRow = otherRowCache.getRow ();
        int fieldCount = otherRow.getFieldCount ();
        int rowCount = 0;

        row_ = new JDSimpleRow (otherRow, false);

        otherRowCache.open ();
        otherRowCache.next ();
        while (otherRowCache.isValid ()) {
            Object[] rowOfData = new Object[fieldCount];
            for (int i = 1; i <= fieldCount; ++i)
                rowOfData[i-1] = otherRow.getSQLData (i).toObject ();
            tempData.addElement (rowOfData);

            boolean[] rowOfNulls = new boolean[fieldCount];
            for (int i = 1; i <= fieldCount; ++i)
                rowOfNulls[i-1] = otherRow.isNull (i);
            tempNulls.addElement (rowOfNulls);

            ++rowCount;
            otherRowCache.next ();
        }

        index_      = -1;
        data_       = new Object[rowCount][fieldCount];
        nulls_      = new boolean[rowCount][fieldCount];
        valid_      = false;

        for (int i = 0; i < rowCount; ++i) {
            data_[i] = (Object[]) tempData.elementAt (i);
            nulls_[i] = (boolean[]) tempNulls.elementAt (i);
        }
    }


/**
Repositions the cursor so that the row reflects the appropriate
data.

@param  valid   Indicates if the current position is valid.
**/
    private void reposition (boolean valid)
        throws SQLException
    {
        valid_ = valid;
        if (valid_) {
            row_.setData (data_[index_]);
            row_.setNulls (nulls_[index_]);
        }
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



    public void open ()
        throws SQLException
    {
        // No-op.
    }



    public void close ()
        throws SQLException
    {
        // No-op.
    }


    public void flush ()
        throws SQLException
    {
        // No-op.
    }



    public JDRow getRow ()
    {
        return row_;
    }



    public boolean isEmpty ()
        throws SQLException
    {
        return data_.length == 0;
    }



    public boolean isValid ()
    {
        return valid_;
    }



    public void absolute (int rowNumber)
        throws SQLException
    {
        if (rowNumber > 0) {
            if (rowNumber <= data_.length) {
                index_ = rowNumber - 1;
                reposition (true);
            }
            else {
                index_ = data_.length;
                reposition (false);
            }
        }
        else if (rowNumber < 0) {
            if (-rowNumber <= data_.length) {
                index_ = data_.length + rowNumber;
                reposition (true);
            }
            else {
                index_ = -1;
                reposition (false);
            }
        }
        /* @C1D - AS400JDBCResultSet will check for this 
                  case (rowNumber == 0), and throw an exception.
                  Therefore, this code never gets called.
        else {
            index_ = 0;
            reposition (false);
        }
        */
    }



    public void afterLast ()
		throws SQLException
    {
        index_ = data_.length;
        reposition (false);
    }



    public void beforeFirst ()
		throws SQLException
    {
        index_ = -1;
        reposition (false);
    }



    public void first ()
		throws SQLException
	{
	    index_ = 0;
        reposition (data_.length > 0);
    }



    public void last ()
		throws SQLException
	{
	    index_ = data_.length - 1;
        reposition (data_.length > 0);
    }



    public void next ()
        throws SQLException
    {
        if (index_ < data_.length - 1) {
            ++index_;
            reposition (true);
        }
        else {
            index_ = data_.length;
            reposition (false);
        }
    }



    public void previous ()
        throws SQLException
    {
        if (index_ > 0) {
            --index_;
            reposition (true);
        }
        else {
            index_ = -1;
            reposition (false);
        }
    }



    public void refreshRow ()
        throws SQLException
    {
        reposition (valid_);
    }



    public void relative (int rowIndex)
		throws SQLException
    {
        int newIndex = index_ + rowIndex;
        if ((newIndex >= 0) && (newIndex < data_.length)) {
            index_ = newIndex;
            reposition (true);
        }
        else
            reposition (false);
    }



}
