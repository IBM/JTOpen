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
are generated on the system.
**/
class JDServerRowCache
implements JDRowCache
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




  // Private data.
  private int                     blockingFactor_;
  private AS400JDBCResultSet      resultSet_;  // @D9A Provide warnings to this result set
  private AS400JDBCConnection     connection_;
  private boolean                 empty_; //empty_ is not cache empty but resultset returning 0 rows "empty"; thats why it is only set once
  private boolean                 emptyChecked_;
  private boolean                 firstBlock_;
  private JDServerRow             row_;
  private int                     id_;
  private boolean                 lastBlock_;
  private DBData                  serverData_;
  private boolean                 variableFieldCompressionSupported_ = false;   //@K54
  private int                     bufferSize_;                                  //@K54  
  private JDCursor                cursor_ = null; //@pda perf2 - fetch/close
  private DBReplyRequestedDS fetchReply = null; //@P0A
  

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
  private int                 index_; //(array index), not row number

  // Keep track of the cursor position of the first 
  // row in the row cache from the front of the 
  // result.  We need to know this to keep the system's
  // cursor in sync with the client's cursor.  Remember we
  // don't know the number of records in the result set so
  // moving the cursor based on the end of the rs makes
  // us put our local variable back to NOT_KNOWN.  We could
  // write some messy code to keep track based on end and beginning 
  // of the RS, but at this time we don't have a requirement
  // to do that.  
  private static int NOT_KNOWN = -9999;                                // @G1a
  private int cursorPositionOfFirstRowInCache_ = NOT_KNOWN;            // @G1a //0 means empty cache, 1 means cache is at the beginning (cache 1st row matches host 1st row)


/**
Constructs a row cache assuming that no data has been
prefetched.

@param  row             The row describing the format.
@param  connection      The connection to the system.
@param  id              The id.
@param  blockingFactor  The blocking factor (in rows).
@param  lastBlock       Has the last block been fetched?
@param  resultSetType   The type of result set.

@exception  SQLException    If an error occurs.
**/
  JDServerRowCache (JDServerRow row,
                    AS400JDBCConnection connection,
                    int id,
                    int blockingFactor, 
                    boolean lastBlock,  //@PDA perf
                    int resultSetType)
  throws SQLException
  {
    blockingFactor_ = blockingFactor;
    connection_     = connection;
    lastBlock_      = lastBlock;  //@PDC perf 
    emptyChecked_   = lastBlock_;  //@PDC perf - false unless we are at lastBlock, then true
    empty_          = lastBlock_;  //@PDA perf
    firstBlock_     = true;
    id_             = id;
    row_            = row;
    serverData_     = null;

    cached_         = 0;
    index_          = -1;

    if(connection_.getServerFunctionalLevel() >=14 && 
       connection_.getProperties().getBoolean(JDProperties.VARIABLE_FIELD_COMPRESSION) &&
       //blockingFactor_ != 1)               //@K54    If blocking factor is one, than the result set is scrollable.
       resultSetType == java.sql.ResultSet.TYPE_FORWARD_ONLY)
        variableFieldCompressionSupported_ = true;                //@K54

    bufferSize_ = connection_.getProperties().getInt(JDProperties.BLOCK_SIZE);  //@K54

    // We are before the first row.  Actually there is no data in the cache
    // when this c'tor is used so it shouldn't make any difference, but
    // we will set it to 0 just to be consistent.  When an RS is opened
    // the cursor is positioned to before the first row, which is what 
    // 0 means.  
    cursorPositionOfFirstRowInCache_ = 0;                            // @G1a

    row_.setRowIndex (index_);
  }
  
  
  
    //@pda perf2
    /**
    Constructs a row cache assuming that no data has been
    prefetched.
   
    @param  row             The row describing the format.
    @param  connection      The connection to the system.
    @param  id              The id.
    @param  blockingFactor  The blocking factor (in rows).
    @param  lastBlock       Has the last block been fetched?
    @param  resultSetType   The type of result set.
    @param  JDCursor        Cursor associated with rows.
   
    @exception  SQLException    If an error occurs.
    **/
    JDServerRowCache (JDServerRow row,
                      AS400JDBCConnection connection,
                      int id,
                      int blockingFactor, 
                      boolean lastBlock,  
                      int resultSetType,
                      JDCursor cursor) 
    throws SQLException
    {
        this(row, connection, id, blockingFactor, lastBlock, resultSetType);
        cursor_ = cursor;  
    }
    


/**
Constructs a row cache including data that has been
prefetched.

@param  row             The row describing the format.
@param  connection      The connection to the system.
@param  id              The id.
@param  blockingFactor  Blocking factor (in rows).
@param  serverData      Prefetched data.
@param  lastBlock       Has the last block been fetched?
@param  resultSetType   The type of result set.

@exception  SQLException    If an error occurs.
**/
  JDServerRowCache (JDServerRow row,
                    AS400JDBCConnection connection,
                    int id,
                    int blockingFactor,
                    DBData serverData,
                    boolean lastBlock,
                    int resultSetType)
  throws SQLException
  {
    blockingFactor_ = blockingFactor;
    connection_     = connection;
    firstBlock_     = true;
    id_             = id;
    lastBlock_      = lastBlock;
    row_            = row;
    serverData_     = serverData;

    if(connection_.getServerFunctionalLevel() >=14 &&
       connection_.getProperties().getBoolean(JDProperties.VARIABLE_FIELD_COMPRESSION) &&
       //blockingFactor_ != 1)               //@K54  If blocking factor is one, than the result set is scrollable
       resultSetType == java.sql.ResultSet.TYPE_FORWARD_ONLY)
        variableFieldCompressionSupported_ = true;          //@K54

    bufferSize_ = connection_.getProperties().getInt(JDProperties.BLOCK_SIZE);  //@K54

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
  
  
    //@pda perf2
    /**
    Constructs a row cache including data that has been
    prefetched.

    @param  row             The row describing the format.
    @param  connection      The connection to the system.
    @param  id              The id.
    @param  blockingFactor  Blocking factor (in rows).
    @param  serverData      Prefetched data.
    @param  lastBlock       Has the last block been fetched?
    @param  resultSetType   The type of result set.
    @param  JDCursor        Cursor associated with rows.

    @exception  SQLException    If an error occurs.
    **/
    JDServerRowCache (JDServerRow row,
                      AS400JDBCConnection connection,
                      int id,
                      int blockingFactor,
                      DBData serverData,
                      boolean lastBlock,
                      int resultSetType,
                      JDCursor cursor)
    throws SQLException
    {
        this(row, connection, id, blockingFactor, serverData, lastBlock, resultSetType);
        cursor_ = cursor;  
    }


/**
Fetches a block of data from the system.

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
Fetches a block of data from the system.

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
            if(variableFieldCompressionSupported_)   //@K54
            {                   
                //Do not need to set the blocking factor if using variable-length field compression
                //If both the buffer size and blocking factor were set, the buffer size will override
                //the blocking factor and the number of rows that will fit in the buffer size will be returned
                //regardless of the blocking factor value                                                                                                        //@K54
                request.setVariableFieldCompression(true);                                                                              //@K54
                request.setBufferSize(bufferSize_ * 1024);                                                                                     //@K54
            }                                                                                                                           //@K54
            else                                                                                                                        //@K54
                request.setBlockingFactor (blockingFactor_);
        }
        else
        {
          request.setBlockingFactor (1);
        }   

        if (JDTrace.isTraceOn ())
          JDTrace.logInformation (connection_, "Fetching a block of data from the system");

        if (fetchReply != null) { fetchReply.returnToPool(); fetchReply = null; } 
        fetchReply = connection_.sendAndReceive (request, id_); //@P0C

        int errorClass = fetchReply.getErrorClass();
        int returnCode = fetchReply.getReturnCode();

        if (((errorClass == 1) && (returnCode == 100))
            || ((errorClass == 2) && (returnCode == 701)))
          endBlock = true;
        else if((errorClass == 2) && (returnCode == 700)) //@pda perf2 - fetch/close
        {
            endBlock = true;
            if(cursor_ != null)
                cursor_.setState(true); //closed cursor already on system
            
        }
        // As in AS400JDBCStatement, post a warning if the system gives us a warning,
        // otherwise throw an exception
        else if (errorClass != 0)
        {                                                                                // @D1a
           // JDError.throwSQLException (connection_, id_, errorClass, returnCode);      // @D1d
           if (returnCode < 0)    {                                                      // @D1a
              JDError.throwSQLException (connection_, id_, errorClass, returnCode);      // @D1a
           } else  {                                                                     // @D1a
              // Post the warning to the resultSet, not the connection @D9A
             if (resultSet_ != null) { 
               resultSet_.postWarning (JDError.getSQLWarning (connection_, id_, errorClass, returnCode)); // @D1a
             } else {
               if (JDTrace.isTraceOn ())           {
                 JDTrace.logInformation(connection_, "posting warning to connection");                
               }
               connection_.postWarning (JDError.getSQLWarning (connection_, id_, errorClass, returnCode)); // @D1a
             }
             
           }
           
        }                                                                                // @D1a

        // Extract data from the row.
        serverData_ = fetchReply.getResultData ();

        if (serverData_ == null)
        {
          endBlock = true; //@rel4 last block was not returned if null????  //@rel9backtotrue and deal with it after called
          cached_ = 0;
        }
        else
        {
          row_.setServerData (serverData_);
          cached_ = serverData_.getRowCount ();
        }

        if (emptyChecked_ == false) //empty_ is refering to empty resultset, not empty cache_
        {
          emptyChecked_ = true;
          empty_        = (cached_ == 0);
        }
      }
      finally
      {
        if (request != null) { request.returnToPool(); request =null; } 
        // if (fetchReply != null) { fetchReply.returnToPool(); fetchReply = null; } 
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
    
      // Make sure reply is returned to pool 
      if (fetchReply != null) {
        fetchReply.returnToPool();  fetchReply = null; 
      }
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


  /**
  isEmpty is not cache empty but resultset returning 0 rows "empty"; thats why it is only set once
  */
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




  //@abs2 re-design method
  /* absolute may or maynot go to server */
  public void absolute (int rowNumber)
  throws SQLException
  {	  
      // If the row is in the cache, then move the index
      // withing the cache.

      // int newIndex = index_;
      
      //int serverRowIndexFromBeg = cursorPositionOfFirstRowInCache_ + index_; //1 is on first row
      //int numberOfServerRows = cursorPositionOfFirstRowInCache_ + cached_ - 1;//!!when lastBlock_==true

      if ( (rowNumber >= cursorPositionOfFirstRowInCache_) && (rowNumber < cursorPositionOfFirstRowInCache_+ cached_ )&& (rowNumber >= 0) && (cursorPositionOfFirstRowInCache_ != NOT_KNOWN))
      {
          //rowNumber is positive and row is in cache_
          index_ = rowNumber - cursorPositionOfFirstRowInCache_;
      }
      else if((rowNumber<0) && lastBlock_ && (cached_ >= -rowNumber) && (cursorPositionOfFirstRowInCache_ != NOT_KNOWN))
      {
          //rowNumber is negative and row is in cache_
          //if lastBlock_ then we know we can just count backwards to the offset in cache_
          //(ie if rowNumber is in cache and last row in cache_ is last row on server)
          index_ = cached_ + rowNumber;
      }
      // Otherwise, fetch data from the system.  If the
      // last block is flagged, then this means the
      // request was not valid.
      else
      {
          // Since we could not find the row in the cache we have to find
          // it on the system. 

          absolute(rowNumber, true);
          return; 
      }

      row_.setRowIndex (index_);
  }


  //@abs2
  void absolute (int rowNumber, boolean mustAccessServer)
  throws SQLException
  {
  
      if(mustAccessServer == false)
      {
          absolute(rowNumber);
          return;
      }

      //int newIndex = rowNumber; 
      boolean endBlock = false;
      if(rowNumber > 0)
      {
          if(cursor_.isClosed() )//&& !firstBlock_)  
          {                    //@max1
              return;          //@max1 //can't go and refetch since cursor is closed.
          }                    //@max1
          else
          {
              if(cursor_.getCursorAttributeSensitive() == 0 && cursor_.getCursorAttributeUpdatable() == 0) //@abs3 fix: only use fetch-direct with insensitive read-only cursors
              {
                  endBlock = fetch (DBSQLRequestDS.FETCH_DIRECT, rowNumber);
              }
              else                          //@abs3
              {                             //@abs3
                  first (true);             //@abs3
                  relative (rowNumber - 1); //@abs3
              }                             //@abs3
          }
      }
      else if(rowNumber == 0)
      {
          //fetch(direct,0) followed by fetch(relative,-x) does not work
          beforeFirst(true);
      }
      else
      {
          //hostserver fetch(direct) does not support negative directions
          //have to do last() and then reletive(-x)
          last (true);
          relative (rowNumber + 1);
      }
      firstBlock_ = false;                               

      if (endBlock == false)
      {
          index_ = 0;
          // @E1D firstBlock_ = false;
          lastBlock_ = endBlock;
      }

      cursorPositionOfFirstRowInCache_ = rowNumber;

      row_.setRowIndex (index_);
  }

 


  public void afterLast ()
  throws SQLException
  {
    // If the last block is cached, then
    // move the index within the cache.
    if (lastBlock_)
      index_ = cached_;

    // Otherwise, change the cursor on the system.
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

  public void beforeFirst (boolean mustAccessServer)
  throws SQLException
  {
      if(mustAccessServer == false)
      {
          beforeFirst();
          return;
      }
      else
      {
          fetch (DBSQLRequestDS.FETCH_BEFORE_FIRST);
          firstBlock_ = true;
          lastBlock_ = false;
          index_ = -1;

          // BeforeFirst will not return any rows.  Set the 
          // variable to 0 to indicate we are before the first row.
          cursorPositionOfFirstRowInCache_ = 0;

          row_.setRowIndex (index_);
      }
  }

  public void beforeFirst ()
  throws SQLException
  {
    // If first block is cached, then move the index
    // within the cache.
    if (firstBlock_)
      index_ = -1;

    // Otherwise, change the cursor on the system.
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
  // routine saying there is no need to go to the system if the data
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
    // within the cache (if we are not forced to go to the system).
    // @G1 change -- do this only if we attempted to go to the system
    // at least once.  If we just opened the rs, go to the 
    // system to see if there are rows in the rs.  
    boolean done = false; //@rel4
    if ((emptyChecked_)       &&                                     // @G1c
        (! mustAccessServer)  && 
        (firstBlock_))
    {                     //@rel4
      index_ = 0;
      done = true;        //@rel4
    }                     //@rel4
    
    // Otherwise, change the cursor on the system.
    //else  //@rel4
    if(!done || !isValid())  //@rel4 go to system also if not valid
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
    // go to the system).
    if ((lastBlock_)  && 
        (cached_ > 0) && 
        (!mustGoToServer))
      index_ = cached_ - 1;

    // Otherwise, change the cursor on the system.
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

    // Otherwise, fetch data from the system.
    else
    {
      boolean wasBeforeFirst = (index_== -1) ? true : false;   //@rel4
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
      else if(wasBeforeFirst)                                     // @G1a //@rel4 (only set to 1 if wasBeforeFirst)
        cursorPositionOfFirstRowInCache_ = 1;                     // @G1a 
      else                                             //@rel4
          cursorPositionOfFirstRowInCache_ = NOT_KNOWN;//@rel4
    }

    row_.setRowIndex (index_);
  }



  public void previous ()
  throws SQLException
  {
    // If the previous row is cached, then move the index
    // within the cache.
    if (index_ >= 1 && cached_ > 0) //@rel4
      --index_;

    // If the first block has not been fetched,
    // then fetch data from the system.
    else if (! firstBlock_)
    {
      // We did not find the record in the cache so we must be 
      // on the first record in the cache trying to back up one.
      // First move the system's cursor so it is in the correct
      // place, then back up one.  Having a block of records in 
      // the cache but not knowing where they came from is a
      // "should not occur" error so throw an internal exception.
      if (cached_ > 1)                                             // @G1a  
      {
        // @G1a  
        if (cursorPositionOfFirstRowInCache_ > 0)                 // @G1a  
          absolute(cursorPositionOfFirstRowInCache_, true);            // @G1a //@abs2
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

    if(index_ == -1 && cached_ == 0)     //@rel4  cache is empty
        cursorPositionOfFirstRowInCache_ = 0;  //@rel4
        
    row_.setRowIndex (index_);
  }



  public void relative (int rowNumber)
  throws SQLException
  {
    //boolean wasBeforeFirst = (index_==-1) ? true : false;//@rel4
    //boolean wasAfterLast = (wasBeforeFirst == false && lastBlock_ == true && index_ == cached_) ? true : false;//@rel4
    // If row number is 0, then don't change the cursor.
    if (rowNumber != 0)
    {

      // If the row is in the cache, then move the index
      // withing the cache.
      int newIndex = index_ + rowNumber;
      if ((newIndex >= 0) && (newIndex < cached_)) //@rel2 allow for before first row //@rel4 -1->0
          index_ = newIndex;
      else if ((newIndex >= 0) && (newIndex == cached_) && (cursor_.isClosed())) //@max1
          index_ = newIndex; //@max1

      // Otherwise, fetch data from the system.  If the
      // last block is flagged, then this means the
      // request was not valid.
      else
      {
        // Since we could not find the row in the cache we have to find
        // it on the system.  First call the system to resync the 
        // cursor position with what we think it is on the client
        // (the local cursor will move if we have a cache hit), then 
        // the call the system to get data.  Note if we have a block
        // of records in the cache but do not know where they came
        // from, flag an error. That should not happen.  We have to 
        // move the system cursor only if we cached a block of records.
        // If only one record is in the cache then the two cursors
        // are already in sync.
        //Note:  relative and absolute call each other, but will not endless-loop since first() always fetches only one row and so cached_=1!
        if (cached_ > 1)                                            // @G1a 
        {
          // @G1a
          if (cursorPositionOfFirstRowInCache_ > 0)                // @G1a
            absolute(cursorPositionOfFirstRowInCache_ + index_, true);  // @G1a //@abs2
          else                                                     // @G1a
            JDError.throwSQLException (JDError.EXC_INTERNAL);     // @G1a
        }                                                           // @G1a

        if(index_ == -1 && cached_ > 0) //@rel2 (if index_ is positioned before any rows, subtract 1 from rowNumber to account that index_ is not on a current row) //@rel4 already set to -1 and added
            rowNumber--; //@rel2
        else if(index_ == cached_) //@rel3 (if index_ is past the cache_ content, add 1 to rowNumber to account that index_ is not on a current row) //@rel4
            rowNumber += index_;  //@rel3 //@rel4 even if after last, cursor on host may not know it and may be off by count in cache_
        boolean endBlock = fetch (DBSQLRequestDS.FETCH_RELATIVE, rowNumber); //@abs2 comment: why not combine absolutesynchwithserver and fetch using something like fetch(-cache_ + index_ + rowNumber)
        firstBlock_ = false;                                // @E1A

        if (endBlock == false)
        {
          index_ = 0;
          // @E1D firstBlock_ = false;
          lastBlock_ = endBlock;
        }else                //@rel4
        {                    //@rel4
            if(rowNumber < 0)//@rel4
            {                //@rel4
                index_ = -1; //empty //@rel4
                cursorPositionOfFirstRowInCache_ = 0; //not aligned
            }                //@rel4
            else             //@rel4
                cursorPositionOfFirstRowInCache_ = NOT_KNOWN;  //@rel4
        }

        //if ((rowNumber > 0) && (cursorPositionOfFirstRowInCache_ > 0)) // @G1a
        if(cursorPositionOfFirstRowInCache_ >=0)   //@rel4 if beforefirst also
        
          cursorPositionOfFirstRowInCache_ =                         // @G1a
                                                                     cursorPositionOfFirstRowInCache_ + rowNumber;           // @G1a
        else                                                           // @G1a
          cursorPositionOfFirstRowInCache_ = NOT_KNOWN;              // @G1a
      }
    }

    row_.setRowIndex (index_);
  }
  
  protected void finalize() throws Throwable {
		super.finalize();
        if (fetchReply != null) { fetchReply.returnToPool(); fetchReply=null; } 
  }

  /*
   * Set the result set to be used for reporting warnings.  @D9A
   * This is typically called in the constructor of the result set. 
   */
  public void setResultSet(AS400JDBCResultSet resultSet) {
    resultSet_      = resultSet; 
  }
}
