///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2016 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.*; 
import java.net.UnknownHostException;

class NLSTableDownload extends Object
{
  private static final String copyright = "Copyright (C) 1997-2016 International Business Machines Corporation and others.";

    private AS400Server server_;
    private AS400ImplRemote       sys_;

    static
    {
        AS400Server.addReplyStream(new NLSExchangeAttrReply(), "as-central");
        AS400Server.addReplyStream(new NLSGetTableReply(), "as-central");
    }

    boolean checkRetry = false; 
    /**
     * @param  system  The server to execute the program.
     **/
    NLSTableDownload(AS400ImplRemote system)
    {
        setSystem(system);

	String retryProperty = System.getProperty("retry");
	if (retryProperty != null) {
	    checkRetry = true; 
	} 
	
    }

    /**
     * connect to the previously set server.
     **/
    void connect() throws ServerStartupException, UnknownHostException, AS400SecurityException, ConnectionDroppedException, InterruptedException, IOException
    {
        // Connect to server
        if (server_ == null)
        {
            server_ = sys_.getConnection(AS400.CENTRAL, false, false);

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
                        NLSExchangeAttrRequest exchangeAttrRequest;
			try { 
			   exchangeAttrRequest = new NLSExchangeAttrRequest(true);
			} catch (java.lang.NoSuchMethodError e) {
			    exchangeAttrRequest = new NLSExchangeAttrRequest();
			} 
                        baseReply = server_.sendExchangeAttrRequest(exchangeAttrRequest);
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
                            Trace.log(Trace.ERROR, "Exchange attribute failed, secondary return code =", NLSReply.getSecondaryRC_() );
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
    char[] download( int fromCCSID, int toCCSID, boolean doublebyte ) throws ConnectionDroppedException, IOException, InterruptedException
    {
	NLSGetTableReply NLSReply = null; 
        NLSGetTableRequest reqDs = null;
	BufferedReader reader = null; 
	boolean retry = true;
	while (retry) {
	    retry = false; 
	    if (doublebyte) { 
		reqDs = new NLSGetDoubleByteTableRequest(fromCCSID);
	    } else { 
		reqDs = new NLSGetTableRequest();
	    }
	    reqDs.setCCSIDs( fromCCSID, toCCSID );
	    DataStream repDs = server_.sendAndReceive(reqDs);
	    if (repDs instanceof NLSGetTableReply)
	    {
		NLSReply = (NLSGetTableReply)repDs;
		if (NLSReply.primaryRC_ != 0)
		{
		    System.out.println("fromCCSID="+fromCCSID+" toCCSID="+toCCSID+" doublebyte="+doublebyte); 
		    System.out.println("ERROR:  Exchange attribute failed, primary return code ="+ NLSReply.primaryRC_);
		    System.out.println("ERROR:    Exchange attribute failed, secondary return code ="+ NLSReply.secondaryRC_ );
		    Trace.log(Trace.WARNING, "Exchange attribute failed, primary return code =", NLSReply.primaryRC_);
		    Trace.log(Trace.ERROR, "Exchange attribute failed, secondary return code =", NLSReply.secondaryRC_ );

		    if (checkRetry) {
			System.out.println("Enter Y to retry");
			if (reader == null) { 
			reader = new BufferedReader(new InputStreamReader(System.in));
			}
			String line = reader.readLine();
			if (line != null && line.length() > 0 && line.charAt(0) == 'Y') {
			    retry = true;
			    System.out.println("Retrying"); 
			}
		    } 
		    if (!retry) { 
			throw new IOException();
		    }
		}
	    }
	    else // unknown data stream
	    {
		disconnect();       //@A1A
		Trace.log(Trace.ERROR, "Unknown instance returned from Exchange Attribute Reply");
		throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
	    }
	}
	disconnect();       //@A1A
	if (NLSReply != null) { 
	    return NLSReply.table_;
	} else {
	    return null; 
	} 

    }

    /**
     * Retrieve the system to execute the program.
     * @return iSeries system where the program will execute.
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
