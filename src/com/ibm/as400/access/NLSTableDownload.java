///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NLSTableDownload.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.net.UnknownHostException;


class NLSTableDownload extends Object
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private AS400Server server_;
    private AS400ImplRemote       sys_;

    static
    {
	AS400Server.addReplyStream(new NLSExchangeAttrReply(), "as-central");
	AS400Server.addReplyStream(new NLSGetTableReply(), "as-central");
    }

    /**
     * @param  system  The AS/400 to execute the program.
     **/
    NLSTableDownload(AS400ImplRemote system)
    {
	setSystem(system);
    }

    /**
     * connect to the previously set AS/400.
     **/
    void connect() throws ServerStartupException, UnknownHostException, AS400SecurityException, ConnectionDroppedException, InterruptedException, IOException
    {
	// Connect to server
	if (server_ == null)
	{
	    server_ = sys_.getConnection(AS400.CENTRAL, false);

	    // Exchange attributes with server job.  (This must be first
	    // exchange with server job to complete initialization.)
	    // First check to see if server has already been initialized
	    // by another user.
	    synchronized (server_)
	    {
		DataStream baseReply = server_.getExchangeAttrReply();
		if (baseReply == null)
		{
		    try
		    {
			baseReply = server_.sendExchangeAttrRequest(new NLSExchangeAttrRequest());
		    }
		    catch(IOException e)
		    {
			Trace.log(Trace.ERROR, "IOException After Exchange Attribute Request");
			disconnect();
			throw e;
		    }
		    if (baseReply instanceof NLSExchangeAttrReply)
		    {
		        // means request completed OK
			NLSExchangeAttrReply NLSReply = (NLSExchangeAttrReply)baseReply;
			if (NLSReply.primaryRC_ != 0)
			{
			    Trace.log(Trace.WARNING, "Exchange attribute failed, primary return code =", NLSReply.primaryRC_);
			    Trace.log(Trace.ERROR, "Exchange attribute failed, secondary return code =", NLSReply.secondaryRC_ );
			    disconnect();
			    throw new IOException();
			}
		    }
		    else // unknown data stream
		    {
			Trace.log(Trace.ERROR, "Unknown instance returned from Exchange Attribute Reply");
			throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
		    }
		}
	    }
	}
    }

    /**
     * Disconnect from the host server.
     **/
    void disconnect()
    {
	if (server_ != null)
	    try {
	      getSystem().disconnectServer(server_);
	      server_ = null;
	  }
	catch (Exception e)
	{}
    }

    /**
     * Download table
     **/
    char[] download( int fromCCSID, int toCCSID ) throws ConnectionDroppedException, IOException, InterruptedException
    {
	NLSGetTableRequest reqDs = new NLSGetTableRequest();
	reqDs.setCCSIDs( fromCCSID, toCCSID );
	DataStream repDs = server_.sendAndReceive(reqDs);
	if (repDs instanceof NLSGetTableReply)
	{
	    NLSGetTableReply NLSReply = (NLSGetTableReply)repDs;
	    if (NLSReply.primaryRC_ != 0)
	    {
		Trace.log(Trace.WARNING, "Exchange attribute failed, primary return code =", NLSReply.primaryRC_);
		Trace.log(Trace.ERROR, "Exchange attribute failed, secondary return code =", NLSReply.secondaryRC_ );
		throw new IOException();
	    }
	    disconnect();       //@A1A
	    return NLSReply.table_;
	}
	else // unknown data stream
	{
	    disconnect();       //@A1A
	    Trace.log(Trace.ERROR, "Unknown instance returned from Exchange Attribute Reply");
	    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
	}
    }

    /**
     * Retrieve the system to execute the program.
     * @return AS/400 where the program will execute.
     **/
    AS400ImplRemote getSystem()
    {
	return(sys_);
    }

    void setSystem(AS400ImplRemote system)
    {
	if (system == null)
	{
	    throw new NullPointerException();
	}

	sys_ = system;
    }

}
