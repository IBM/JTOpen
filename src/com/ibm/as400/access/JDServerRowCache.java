///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDServerRowCache.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




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

        try {            
            cached_ = serverData.getRowCount ();
        }
        catch (DBDataStreamException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);
        }       

        emptyChecked_   = true;
        empty_          = (cached_ == 0);

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

  		try {
    		DBSQLRequestDS request = new DBSQLRequestDS (
    		    DBSQLRequestDS.FUNCTIONID_FETCH,
	    	    id_, DBBaseRequestDS.ORS_BITMAP_RETURN_DATA
		        + DBBaseRequestDS.ORS_BITMAP_RESULT_DATA, 0);

            request.setFetchScrollOption (fetchScrollOption, rows);

            // If fetching next, then fetch a block.  Otherwise,
            // just fetch a single row.
            if (fetchScrollOption == DBSQLRequestDS.FETCH_NEXT) {
                if (blockingFactor_ > 0)
                    request.setBlockingFactor (blockingFactor_);
            }
            else
                request.setBlockingFactor (1);

            if (JDTrace.isTraceOn ())
                JDTrace.logInformation (connection_, "Fetching a block of data from the server");

            DBReplyRequestedDS reply = connection_.sendAndReceive (request, id_);

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
    		if (serverData_ == null) {
    		    endBlock = true;
    		    cached_ = 0;
    		}
    		else {
                row_.setServerData (serverData_);
        		cached_ = serverData_.getRowCount ();
        	}

        	if (emptyChecked_ == false) {
        	    emptyChecked_ = true;
        	    empty_        = (cached_ == 0);
        	}
   		}
    	catch (DBDataStreamException e) {
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
        if (emptyChecked_ == false) {
            firstBlock_ = false;
            beforeFirst ();
        }

        return empty_;
    }



    public boolean isValid ()
    {
        return ((serverData_ != null)
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
        if (rowNumber > 0) {
            first ();
            relative (rowNumber - 1);
        }
        else {
            last ();
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
		else {
		    fetch (DBSQLRequestDS.FETCH_AFTER_LAST);
		    firstBlock_ = false;
		    lastBlock_ = true;
		    index_ = 0;
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
        else {
		    fetch (DBSQLRequestDS.FETCH_BEFORE_FIRST);
		    firstBlock_ = true;
		    lastBlock_ = false;
		    index_ = -1;
    	}

    	row_.setRowIndex (index_);
    }



    public void first ()
		throws SQLException
	{
        // If first block is cached, then move the index
        // within the cache.
        if (firstBlock_)
            index_ = 0;

        // Otherwise, change the cursor on the server.
        else {
		    fetch (DBSQLRequestDS.FETCH_FIRST);
		    firstBlock_ = true;
		    lastBlock_ = false;
		    index_ = 0;
    	}

        row_.setRowIndex (index_);
    }



    public void last ()
		throws SQLException
	{
		// If the last block is cached and contains data, then
		// move the index within the cache.
		if ((lastBlock_) && (cached_ > 0))
		    index_ = cached_ - 1;

		// Otherwise, change the cursor on the server.
		else {
		    fetch (DBSQLRequestDS.FETCH_LAST);
		    firstBlock_ = false;
		    lastBlock_ = true;
		    index_ = 0;
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
        else  {
            lastBlock_ = fetch (DBSQLRequestDS.FETCH_NEXT);
            firstBlock_ = false;
            index_ = 0;
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
        else if (! firstBlock_) {
            firstBlock_ = fetch (DBSQLRequestDS.FETCH_PREVIOUS);
            lastBlock_ = false;
            index_ = cached_ - 1;
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
        if (rowNumber != 0) {

            // If the row is in the cache, then move the index
            // withing the cache.
            int newIndex = index_ + rowNumber;
            if ((newIndex >= 0) && (newIndex < cached_))
                index_ = newIndex;

            // Otherwise, fetch data from the server.  If the
            // last block is flagged, then this means the
            // request was not valid.
            else {
                boolean endBlock = fetch (DBSQLRequestDS.FETCH_RELATIVE,
                    rowNumber);
                firstBlock_ = false;                                // @E1A
                if (endBlock == false) {
                    index_ = 0;
                    // @E1D firstBlock_ = false;
                    lastBlock_ = endBlock;
                }
            }
        }

        row_.setRowIndex (index_);
    }



}
