///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBConcatenatedRequestDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;



/**
The DBConcatenatedRequestDS class represents a datstream
whose contents are the concatenated contents of multiple
DBBaseRequestDS datastreams.
**/
class DBConcatenatedRequestDS 
extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private int                 count_;
    private int                 countMinusOne_;
    private DBBaseRequestDS[]   requests_;



/**
Constructs a DBConcatenatedRequestDS object.

@param heldRequests     A Vector of DBBaseRequestDS objects which have been held.
                        It is assumed that only the last request will have/need
                        a corresponding reply.
@param lastRequest      The most recent request.
**/
    public DBConcatenatedRequestDS(Vector heldRequests, DBBaseRequestDS lastRequest)
    {
        count_ = heldRequests.size() + 1;
        countMinusOne_ = count_ - 1;

        requests_ = new DBBaseRequestDS[count_];
        heldRequests.copyInto(requests_);
        requests_[countMinusOne_] = lastRequest;
    }




/**
Returns the correlation ID.

@return The correlation ID.
**/
    int getCorrelation()
    {
        // Since we only need a reply from the last request, then
        // we will use its correlation ID.
	    return requests_[countMinusOne_].getCorrelation();
    }



/**
Sets the correlation ID.

@param id The correlation ID.
**/
    void setCorrelation(int id)
    {
        // Since we only need a reply from the last request, then
        // we will set its correlation ID.  The rest will be 0.
        for(int i = 0; i < countMinusOne_; ++i)
            requests_[i].setCorrelation(0);
        requests_[countMinusOne_].setCorrelation(id);
    }



/**
Write the datastream.

@param out  The output stream.

@exception IOException If an I/O error occurs.
**/
    void write(OutputStream out) throws IOException
    {
        // I tried just writing the requests in succession to "out"
        // but that resulted in separate requests (which is exactly
        // what we are trying to avoid here).  
        //
        // This code writes each request to a temporary byte array
        // and then sends that byte array all at once.  This results
        // in a superfluous byte copy which would be nice to avoid.
        // In most cases, the size of this byte array will be small,
        // since its used mostly for close requests, so it is still
        // beneficial.
        //
        ByteArrayOutputStream concatented = new ByteArrayOutputStream();
        for(int i = 0; i < count_; ++i) {
            requests_[i].write(concatented);
            requests_[i].inUse_ = false; //@P1A
        }
                                                
        synchronized(out)                                     // @W1a
        {                                                     // @W1a
           out.write(concatented.toByteArray());
           out.flush();       
        }                                                     // @W1a
        if (Trace.traceOn_) Trace.log(Trace.DATASTREAM, "Data stream sent...", concatented.toByteArray()); //@A1A @P0C
	}


}
