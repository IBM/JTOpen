///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: NLSImplRemote.java
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

// Remote implementation of central server function
class NLSImplRemote extends NLSImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static String getCopyright()
    {
	return Copyright.copyright;
    }

    static
    {
	AS400Server.addReplyStream(new NLSExchangeAttrReply(), AS400.CENTRAL);
	AS400Server.addReplyStream(new NLSGetTableReply(), AS400.CENTRAL);
    }

    private AS400Server server;
    private int ccsid;

    // connect to the central server of the AS/400.
    void connect() throws ServerStartupException, UnknownHostException, AS400SecurityException, ConnectionDroppedException, InterruptedException, IOException
    {
	// Connect to server
	if (this.server == null)
	{
	    this.server = system.getConnection(AS400.CENTRAL, false);

	    // Exchange attributes with server job.  (This must be first
	    // exchange with server job to complete initialization.)
	    // First check to see if server has already been initialized
	    // by another user.
	    synchronized (server)
	    {
		DataStream baseReply = this.server.getExchangeAttrReply();
		if (baseReply == null)
		{
		    try
		    {
			baseReply = this.server.sendExchangeAttrRequest(new NLSExchangeAttrRequest());
		    }
		    catch(IOException e)
		    {
			Trace.log(Trace.ERROR, "IOException After Exchange Attribute Request");
			disconnect();
			throw (IOException)e.fillInStackTrace();
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
			else
			{
			    this.ccsid = NLSReply.getCcsid();
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

    // Disconnect from the central server.
    void disconnect()
    {
	if (this.server != null)
	{
	    try
	    {
		this.system.disconnectServer(this.server);
		this.server = null;
	    }
	    catch (Exception e)
	    {
	    }
	}
    }

    int getCcsid() throws IOException
    {
	return this.ccsid;
    }

    // Download table
    char[] getTable(int fromCCSID, int toCCSID) throws ConnectionDroppedException, IOException, InterruptedException
    {
	NLSGetTableRequest reqDs = new NLSGetTableRequest();
	reqDs.setCCSIDs(fromCCSID, toCCSID);
	DataStream repDs = this.server.sendAndReceive(reqDs);
	if (repDs instanceof NLSGetTableReply)
	{
	    NLSGetTableReply NLSReply = (NLSGetTableReply)repDs;
	    if (NLSReply.primaryRC_ != 0)
	    {
		Trace.log(Trace.WARNING, "Exchange attribute failed, primary return code =", NLSReply.primaryRC_);
		Trace.log(Trace.ERROR, "Exchange attribute failed, secondary return code =", NLSReply.secondaryRC_ );
		throw new IOException();
	    }
	    return NLSReply.table_;
	}
	else // unknown data stream
	{
	    disconnect();
	    Trace.log(Trace.ERROR, "Unknown instance returned from Exchange Attribute Reply");
	    throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
	}
    }
}
