///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDServerRowCache.java
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
<p>The JDServerRowCache class implements a set of rows that
are generated on the server.
**/
class JDServerRowCache
implements JDRowCache
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  // Private data.
  private int                     blockingFactor_;
  private AS400JDBCConnection     connection_;
  private boolean                 empty_;
  private boolean                 emptyChecked_;
  private boolean                 firstBlock_;
  private JDServerRow             row_;
  private int                     id_;
  private boolean                 lastBlock_;
  private DBData                  serverData_;



  // Index always points to the row within the cache.
  // It is not the row number within the result set.
  //
  // Cached is the number of rows in the cached block.
  //
  // When the result set is updatable, it should pass
  // in a blocking factor of 1, so that cached_ is
  // always equal to 1.
  //
  private int                 cached_;
  private int                 index_;

  // Keep track of the cursor position of the first 
  // row in the row cache from the front of the 
  // result.  We need to know this to keep the server's
  // cursor in sync with the client's cursor.  Remember we
  // don't know the number of records in the result set so
  // moving the cursor based on the end of the rs makes
  // us put our local variable back to NOT_KNOWN.  We could
  // write some messy code to keep track based on end and beginning 
  // of the RS, but at this time we don't have a requirement
  // to do that.  
  private static int NOT_KNOWN = -9999;                                // @G1a
  private int cursorPositionOfFirstRowInCache_ = NOT_KNOWN;            // @G1a


/**
Constructs a row cache assuming that no data has been
prefetched.

@param  row             The row describing the format.
@param  connection      The connection to the server.
@param  id              The id.
@param  blockingFactor  The blocking factor (in rows).

@exception  SQLException    If an error occurs.
**/
  JDServerRowCache (JDServerRow row,
                    AS400JDBCConnection connection,
                    int id,
                    int blockingFactor)
  throws SQLException
  {
    blockingFactor_ = blockingFactor;
    connection_     = connection;
    emptyChecked_   = false;
    firstBlock_     = true;
    id_             = id;
    lastBlock_      = false;
    row_            = row;
    serverData_     = null;

    cached_         = 0;
    index_          = -1;

    // We are before the first row.  Actually there is no data in the cache
    // when this c'tor is used so it shouldn't make any difference, but
    // we will set it to 0 just to be consistent.  When an RS is opened
    // the cursor is positioned to before the first row, which is what 
    // 0 means.  
    cursorPositionOfFirstRowInCache_ = 0;                            // @G1a

    row_.setRowIndex (index_);
  }



/**
Constructs a row cache including data that has been
prefetched.

@param  row             The row describing the format.
@param  connection      The connection to the server.
@param  id              The id.
@param  blockingFactor  Blocking factor (in rows).
@param  serverData      Prefetched data.
@param  lastBlock       Has the last block been fetched?

@exception  SQLException    If an error occurs.
**/
  JDServerRowCache (JDServerRow row,
                    AS400JDBCConnection connection,
                    int id,
                    int blockingFactor,
                    DBData serverData,
                    boolean lastBlock)
  throws SQLException
  {
    blockingFactor_ = blockingFactor;
    connection_     = connection;
    firstBlock_     = true;
    id_             = id;
    lastBlock_      = lastBlock;
    row_            = row;
    serverData_     = serverData;

    try
    {
      cached_ = serverData.getRowCount ();
    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    }

    emptyChecked_   = true;
    empty_          = (cached_ == 0);

    // If we were able to pre-fetch data then the cache starts with the first
    // row of data.  If we were not able to pre-fetch data then the cache
    // is empty. 
    if (cached_ > 0)                                                 // @G1a
      cursorPositionOfFirstRowInCache_ = 1;                         // @G1a
    else                                                             // @G1a
      cursorPositionOfFirstRowInCache_ = 0;                         // @G1a


    index_          = -1;

    row_.setRowIndex (index_);
    row_.setServerData (serverData_);
  }



/**
Fetches a block of data from the server.

@param  fetchScrollOption   The fetch scroll option.
@return                     true if the first or last block (in
                            the appropriate direction) was fetched,
                            false otherwise.

@exception  SQLException    If an error occurs.
**/
  private boolean fetch (int fetchScrollOption)
  throws SQLException
  {
    return fetch (fetchScrollOption, 0);
  }



/**
Fetches a block of data from the server.

@param  fetchScrollOption   The fetch scroll option.
@param  rows                The number of rows when
                            fetchScrollOption is
                            DBSQLRequestDS.FETCH_RELATIVE.
@return                     true if the first or last block (in
                            the appropriate direction) was fetched,
                            false otherwise.

@exception  SQLException    If an error occurs.
**/
  private boolean fetch (int fetchScrollOption, int rows)
  throws SQLException
  {
    boolean endBlock = false;

    try
    {
      DBSQLRequestDS request = null; //@P0A
      DBReplyRequestedDS reply = null; //@P0A
      try
      {
        request = DBDSPool.getDBSQLRequestDS ( //@P0C
                                               DBSQLRequestDS.FUNCTIONID_FETCH,
                                               id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
                                               + DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

        request.setFetchScrollOption (fetchScrollOption, rows);

        // If fetching next, then fetch a block.  Otherwise,
        // just fetch a single row.  The check was altered under @G1
        // to fetch a block of rows only when we know the cursor
        // location.  If we don't know the cursor location when we 
        // get only one row just in case the next request is to
        // go backward or relative to the current location.  This 
        // will be slower but it is the only way to assure accurate 
        // information is returned to the app. 
        if ((fetchScrollOption == DBSQLRequestDS.FETCH_NEXT) &&       
            (blockingFactor_ > 0)                            &&
            (cursorPositionOfFirstRowInCache_ >= 0))                    // @G1a
        {
          request.setBlockingFactor (blockingFactor_);
        }
        else
        {
          request.setBlockingFactor (1);
        }   

        if (JDTrace.isTraceOn ())
          JDTrace.logInformation (connection_, "Fetching a block of data from the server");

        reply = connection_.sendAndReceive (request, id_); //@P0C

        int errorClass = reply.getErrorClass();
        int returnCode = reply.getReturnCode();

        if (((errorClass == 1) && (returnCode == 100))
            || ((errorClass == 2) && (returnCode == 701)))
          endBlock = true;

        // Other server errors.
        else if (errorClass != 0)
          JDError.throwSQLException (connection_, id_, errorClass, returnCode);

        // Extract data from the row.
        serverData_ = reply.getResultData ();

        if (serverData_ == null)
        {
          endBlock = true;
          cached_ = 0;
        }
        else
        {
          row_.setServerData (serverData_);
          cached_ = serverData_.getRowCount ();
        }

        if (emptyChecked_ == false)
        {
          emptyChecked_ = true;
          empty_        = (cached_ == 0);
        }
      }
      finally
      {
        if (request != null) request.inUse_ = false;
        if (reply != null) reply.inUse_ = false;
      }
    }
    catch (DBDataStreamException e)
    {
      JDError.throwSQLException (JDError.EXC_INTERNAL, e);
    }

    return endBlock;
  }



/**
Sets the fetch size.

@param fetchSize    The fetch size.

@exception          SQLException    If an error occurs.
**/
  public void setFetchSize (int fetchSize)
  throws SQLException
  {
    // The fetch size is only used if the "block size" property
    // is set to "0".
    if (connection_.getProperties ().getInt (JDProperties.BLOCK_SIZE) == 0)
      blockingFactor_ = fetchSize;
  }



//-------------------------------------------------------------//
//                                                             //
// INTERFACE IMPLEMENTATIONS                                   //
//                                                             //
//-------------------------------------------------------------//



  public void open ()
  throws SQLException
  {
    // No-op.  It is assumed here that the cursor is
    // already open.
  }



  public void close ()
  throws SQLException
  {
    // No-op.  It is assumed that the cursor will be closed
    // elsewhere.
  }



  public void flush ()
  throws SQLException
  {
    cached_ = 0;
    emptyChecked_ = false;
  }



  public void refreshRow ()
  throws SQLException
  {
    fetch (DBSQLRequestDS.FETCH_CURRENT);
    index_ = 0;
    row_.setRowIndex (index_);
  }



  public JDRow getRow ()
  {
    return row_;
  }



  public boolean isEmpty ()
  throws SQLException
  {
    // If empty has not been checked yet, then no data has
    // been fetched.  Force the first fetch.    
    // The change for @G1 makes the code block work (I don't
    // think it worked before).  For emptyChecked_ to be false
    // prefetch must be off.  If block size is 0 the beforeFirst()
    // call will return 0 rows.  The empty flag is set based on how
    // many rows are returned so since no rows were returned empty_
    // was set to true.  The change it to call first to get a row,
    // then set the empty_ flag there, if rows are found. 
    if (emptyChecked_ == false)
    {
      firstBlock_ = false;
      first(true);                                                 // @G1a
      beforeFirst ();
    }

    return empty_;
  }



  public boolean isValid ()
  {
    return((serverData_ != null)
           && (index_ >= 0)
           && (index_ < cached_));
  }




  public void absolute (int rowNumber)
  throws SQLException
  {
    // Since the server does not provide a fetch absolute,
    // we have to start at the first or last (depending
    // on the sign of the row number) and go relative
    // from there.
    // 
    // @E1A: I investigated making the call to first()
    // or last() more efficient.  In a nutshell, I changed
    // them to position the cursor on the server, but
    // not to fetch data.  In addition, I tried to chain
    // the requests to the following call to relative().
    // This did not work since, in some cases, the call
    // to first() or last() resulted in an error class:
    // return code of 2:701, signalling the last block.
    // Since this is a non-zero error code, the chained
    // relative() request fails with 7:1000 return code.
    // So, I decided to resign to the fact the absolute
    // will be slow in many cases!
    //
    // The change for @G1 is to force a trip to the server.
    // That syncs the server cursor with the client cursor.
    // Before we forced a trip to the server we would get
    // in the case where the server cursor was one place,
    // we thought we were in a different place, and data
    // for the wrong row was given to the app. 
    if (rowNumber > 0)
    {
      first (true);                                                // @G1c
      relative (rowNumber - 1);
    }
    else
    {
      last (true);                                                 // @G1c
      relative (rowNumber + 1);
    }
  }



  public void afterLast ()
  throws SQLException
  {
    // If the last block is cached, then
    // move the index within the cache.
    if (lastBlock_)
      index_ = cached_;

    // Otherwise, change the cursor on the server.
    else
    {
      fetch (DBSQLRequestDS.FETCH_AFTER_LAST);
      firstBlock_ = false;
      lastBlock_ = true;
      index_ = 0;                                         

      // We just fetched based on the end of the result set so we no longer
      // know the row number of the first row in the cache
      cursorPositionOfFirstRowInCache_ = NOT_KNOWN;                  // @G1a
    }

    row_.setRowIndex (index_);
  }



  public void beforeFirst ()
  throws SQLException
  {
    // If first block is cached, then move the index
    // within the cache.
    if (firstBlock_)
      index_ = -1;

    // Otherwise, change the cursor on the server.
    else
    {
      fetch (DBSQLRequestDS.FETCH_BEFORE_FIRST);
      firstBlock_ = true;
      lastBlock_ = false;
      index_ = -1;

      // BeforeFirst will not return any rows.  Set the 
      // variable to 0 to indicate we are before the first row.
      cursorPositionOfFirstRowInCache_ = 0;                          // @G1a
    }

    row_.setRowIndex (index_);
  }


  // @G1c 
  // The logic of this routine was moved to first(boolean).  Call the new
  // routine saying there is no need to go to the server if the data
  // is in the local cache.
  public void first ()
  throws SQLException
  {
    first(false);
  }


  // @G1 new method
  void first (boolean mustAccessServer)
  throws SQLException
  {
    // If first block is cached, then move the index
    // within the cache (if we are not forced to go to the server).
    // @G1 change -- do this only if we attempted to go to the server
    // at least once.  If we just opened the rs, go to the 
    // server to see if there are rows in the rs.  
    if ((emptyChecked_)       &&                                     // @G1c
        (! mustAccessServer)  && 
        (firstBlock_))
      index_ = 0;

    // Otherwise, change the cursor on the server.
    else
    {
      fetch (DBSQLRequestDS.FETCH_FIRST);
      firstBlock_ = true;
      lastBlock_ = false;
      index_ = 0;

      // We just fetched based on the beginning of the result set so we 
      // know the first row in the rs is row 1.
      cursorPositionOfFirstRowInCache_ = 1;                          // @G1a
    }

    row_.setRowIndex (index_);
  }



  // @G1
  // The logic in this method was moved to last(boolean).  Call that
  // method saying use data in the cache if the cache has the row
  // we need.
  public void last ()
  throws SQLException
  {
    last(false);
  }


  // @G1 new method
  void last (boolean mustGoToServer)
  throws SQLException
  {
    // If the last block is cached and contains data, then
    // move the index within the cache (if we are not forced to
    // go to the server).
    if ((lastBlock_)  && 
        (cached_ > 0) && 
        (!mustGoToServer))
      index_ = cached_ - 1;

    // Otherwise, change the cursor on the server.
    else
    {
      fetch (DBSQLRequestDS.FETCH_LAST);
      firstBlock_ = false;
      lastBlock_ = true;
      index_ = 0;              

      // We just fetched based on the end of the result set so we no longer
      // know the row number of the first row in the cache
      cursorPositionOfFirstRowInCache_ = NOT_KNOWN;                   // @G1a
    }

    row_.setRowIndex (index_);
  }




  public void next ()
  throws SQLException
  {
    // If the next row is cached, then move the index
    // within the cache.
    if (index_ < (cached_ - 1))
      ++index_;

    // If the last block has been fetched,
    // then there are no more.
    else if (lastBlock_)
      index_ = cached_;

    // Otherwise, fetch data from the server.
    else
    {
      int oldCached = cached_;                                     // @G1a
      lastBlock_ = fetch (DBSQLRequestDS.FETCH_NEXT);
      firstBlock_ = false;
      index_ = 0;

      // We get a block of records only when we know the cursor position
      // of the first row in the cache.  When we know the position, update our 
      // variable by the number of rows in the cache.  (This works 
      // even when we are getting one row at a time because oldCached will be 1).
      if (cursorPositionOfFirstRowInCache_ > 0)                    // @G1a
        cursorPositionOfFirstRowInCache_ =                        // @G1a
                                                                  cursorPositionOfFirstRowInCache_ + oldCached;    // @G1a
      else                                                         // @G1a
        cursorPositionOfFirstRowInCache_ = 1;                     // @G1a 
    }

    row_.setRowIndex (index_);
  }



  public void previous ()
  throws SQLException
  {
    // If the previous row is cached, then move the index
    // within the cache.
    if (index_ >= 1)
      --index_;

    // If the first block has not been fetched,
    // then fetch data from the server.
    else if (! firstBlock_)
    {
      // We did not find the record in the cache so we must be 
      // on the first record in the cache trying to back up one.
      // First move the server's cursor so it is in the correct
      // place, then back up one.  Having a block of records in 
      // the cache but not knowing where they came from is a
      // "should not occur" error so throw an internal exception.
      if (cached_ > 1)                                             // @G1a  
      {
        // @G1a  
        if (cursorPositionOfFirstRowInCache_ > 0)                 // @G1a  
          absolute(cursorPositionOfFirstRowInCache_);            // @G1a
        else                                                      // @G1a
          JDError.throwSQLException (JDError.EXC_INTERNAL);      // @G1a
      }                                                            // @G1a

      firstBlock_ = fetch (DBSQLRequestDS.FETCH_PREVIOUS);
      lastBlock_ = false;
      index_ = cached_ - 1;

      if (firstBlock_)                                             // @G1a
        cursorPositionOfFirstRowInCache_ = 1;                     // @G1a
      else                                                         // @G1a
        cursorPositionOfFirstRowInCache_ --;                      // @G1a
    }

    // Else we have moved before the first row.
    else
      index_ = -1;

    row_.setRowIndex (index_);
  }



  public void relative (int rowNumber)
  throws SQLException
  {
    // If row number is 0, then don't change the cursor.
    if (rowNumber != 0)
    {

      // If the row is in the cache, then move the index
      // withing the cache.
      int newIndex = index_ + rowNumber;
      if ((newIndex >= 0) && (newIndex < cached_))
        index_ = newIndex;

      // Otherwise, fetch data from the server.  If the
      // last block is flagged, then this means the
      // request was not valid.
      else
      {
        // Since we could not find the row in the cache we have to find
        // it on the server.  First call the server to resync the 
        // cursor position with what we think it is on the client
        // (the local cursor will move if we have a cache hit), then 
        // the call the server to get data.  Note if we have a block
        // of records in the cache but do not know where they came
        // from, flag an error. That should not happen.  We have to 
        // move the server cursor only if we cached a block of records.
        // If only one record is in the cache then the two cursors
        // are already in sync.
        if (cached_ > 1)                                            // @G1a
        {
          // @G1a
          if (cursorPositionOfFirstRowInCache_ > 0)                // @G1a
            absolute(cursorPositionOfFirstRowInCache_ + index_);  // @G1a 
          else                                                     // @G1a
            JDError.throwSQLException (JDError.EXC_INTERNAL);     // @G1a
        }                                                           // @G1a

        boolean endBlock = fetch (DBSQLRequestDS.FETCH_RELATIVE, rowNumber);
        firstBlock_ = false;                                // @E1A

        if (endBlock == false)
        {
          index_ = 0;
          // @E1D firstBlock_ = false;
          lastBlock_ = endBlock;
        }

        if ((rowNumber > 0) && (cursorPositionOfFirstRowInCache_ > 0)) // @G1a
          cursorPositionOfFirstRowInCache_ =                         // @G1a
                                                                     cursorPositionOfFirstRowInCache_ + rowNumber;           // @G1a
        else                                                           // @G1a
          cursorPositionOfFirstRowInCache_ = NOT_KNOWN;              // @G1a
      }
    }

    row_.setRowIndex (index_);
  }
}
