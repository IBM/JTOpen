///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDRowCache.java
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
The JDRowCache interface represents a set of rows with the
ability to move among them.
**/
//
// Implementation notes:
//
// There is always exactly 1 JDRow object associated with
// the row cache.  Its contents will change to reflect the
// new cursor position.  This implementation allows us to reduce
// the number of JDRow objects that get created, which will
// ultimately improve performace.
//
// However, this design decision is based on the assumption
// that the user will never access more than one row at a time,
// which is in line with the JDBC model.  However, if JDBC ever
// adds something like ODBC's extended fetch and blocked inserts,
// then this design will need to be reworked slightly.
//
// Having a one-to-one correspondence between JDRowCache objects
// and JDRow objects kind of implies that they could be combined
// into 1 object.  I am choosing at this point to leave them
// separated, however, just to make the code separate, since
// each deals with 2 different issues of result set management.
//
interface JDRowCache
{



//------------------------------------------------------------//
//                                                            //
// OPEN AND CLOSE                                             //
//                                                            //
//------------------------------------------------------------//



/**
Opens the cache.  No rows are available until after the
cache has been opened.

@exception  SQLException    If an error occurs.
**/
    public abstract void open ()
        throws SQLException;



/**
Closes the cache.  No more rows are available after the
cache has been closed.

@exception  SQLException    If an error occurs.
**/
    public abstract void close ()
        throws SQLException;



/**
Flushes the cache.  This forces the next positioning to
fetch.

@exception  SQLException    If an error occurs.
**/
    public abstract void flush ()
        throws SQLException;



/**
Refreshes the row, i.e., re-reads it from the database.

@exception  SQLException    If an error occurs.
**/
    public abstract void refreshRow ()
		throws SQLException;



//------------------------------------------------------------//
//                                                            //
// CURSOR INFORMATION                                         //
//                                                            //
//------------------------------------------------------------//



/**
Returns the row object associated with this cache.

@return The row object.
**/
    public abstract JDRow getRow ();



/**
Indicates if the cache represents an empty result set.

@return true if the cache represents an empty result set,
        false otherwise.

@exception SQLException  If an error occurs.
**/
    public abstract boolean isEmpty ()
        throws SQLException;



/**
Indicates if the cursor is positioned on a valid row.

@return The row object.
**/
    public abstract boolean isValid ();




//------------------------------------------------------------//
//                                                            //
// CURSOR POSITIONING                                         //
//                                                            //
//------------------------------------------------------------//



/**
Positions the cursor to an absolute row number.

@param  rowNumber   The row number.

@exception SQLException  If an error occurs.
*/
    public abstract void absolute (int rowNumber)
        throws SQLException;



/**
Positions the cursor after the last row.

@exception  SQLException    If an error occurs.
**/
    public abstract void afterLast ()
		throws SQLException;



/**
Positions the cursor before the first row.

@exception  SQLException    If an error occurs.
**/
    public abstract void beforeFirst ()
		throws SQLException;



/**
Positions the cursor to the first row.

@exception  SQLException    If an error occurs.
**/
    public abstract void first ()
		throws SQLException;



/**
Positions the cursor to the last row.

@exception  SQLException    If an error occurs.
**/
    public abstract void last ()
		throws SQLException;



/**
Positions the cursor to the next row.

@exception  SQLException    If an error occurs.
**/
    public abstract void next ()
        throws SQLException;



/**
Positions the cursor to the previous row.

@exception  SQLException    If an error occurs.
**/
    public abstract void previous ()
        throws SQLException;



/**
Positions the cursor to a relative row number.
@return             The current row, or null if none.

@exception  SQLException    If an error occurs.
*/
    public abstract void relative (int rowIndex)
		throws SQLException;



}
