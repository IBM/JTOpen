///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDMappedRowCache.java
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
The JDMappedRowCache class implements a set of rows that are
mapped from one row cache to a new format.  This comes in handy
for DatabaseMetaData catalog methods, where the format that comes
back from the system is different from the format that the JDBC
specification says we must return.
**/
class JDMappedRowCache
implements JDRowCache
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private JDMappedRow         row_;
    private JDRowCache          fromCache_;




/**
Constructs a JDMappedRowCache object.

@param  formatRow   The row describing the format.
@param  fromCache   The row cache from which to map.
**/
    JDMappedRowCache (JDMappedRow formatRow,
                      JDRowCache fromCache)
    {
        row_        = formatRow;
        fromCache_  = fromCache;

        row_.setRow (fromCache_.getRow ());
    }



//------------------------------------------------------------//
//                                                            //
// OPEN AND CLOSE                                             //
//                                                            //
//------------------------------------------------------------//



    public void open ()
        throws SQLException
    {
        fromCache_.open ();
    }



    public void close ()
        throws SQLException
    {
        fromCache_.close ();
    }



    public void flush ()
        throws SQLException
    {
        fromCache_.flush ();
    }



    public void refreshRow ()
        throws SQLException
    {
        fromCache_.refreshRow ();
    }



//------------------------------------------------------------//
//                                                            //
// CURSOR INFORMATION                                         //
//                                                            //
//------------------------------------------------------------//



    public JDRow getRow ()
    {
        return row_;
    }



    public boolean isEmpty ()
        throws SQLException
    {
        return fromCache_.isEmpty ();
    }



    public boolean isValid ()
    {
        return fromCache_.isValid ();
    }



//------------------------------------------------------------//
//                                                            //
// CURSOR POSITIONING                                         //
//                                                            //
//------------------------------------------------------------//



    public void absolute (int rowNumber)
        throws SQLException
    {
	    fromCache_.absolute (rowNumber);
    }



    public void afterLast ()
		throws SQLException
    {
        fromCache_.afterLast ();
    }



    public void beforeFirst ()
		throws SQLException
    {
        fromCache_.beforeFirst ();
    }



    public void first ()
		throws SQLException
	{
	    fromCache_.first ();
    }



    public void last ()
		throws SQLException
	{
	    fromCache_.last ();
    }



    public void next ()
        throws SQLException
    {
        fromCache_.next ();
    }



    public void previous ()
        throws SQLException
    {
        fromCache_.previous ();
    }



    public void relative (int rowIndex)
		throws SQLException
    {
        fromCache_.relative (rowIndex);
    }



}

