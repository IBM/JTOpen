///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ClassDecoupler.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.util.*;

/**
 * This class exists to remove the dependencies AS400ImplRemote had on some of
 * the DDM and DB datastream classes.  In this way, a JarMaker-ed jt400.jar file
 * can effectively operate its AS400 object without needing the DDM or JDBC classes.
**/
class ClassDecoupler
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  static void freeDBReplyStream(DataStream ds)
  {
    if (ds instanceof DBReplyRequestedDS)
    {
      ((DBReplyRequestedDS)ds).inUse_ = false;
    }
  }

  static Object[] connectDDMPhase1(OutputStream outStream, InputStream inStream, boolean passwordType_, int byteType_) throws ServerStartupException, IOException
  {
    // Exchange server start up/security information with DDM server.
    // Exchange attributes.
    DDMEXCSATRequestDataStream EXCSATRequest = new DDMEXCSATRequestDataStream();
    EXCSATRequest.write(outStream);

    DDMEXCSATReplyDataStream EXCSATReply = new DDMEXCSATReplyDataStream();
    EXCSATReply.read(inStream);

    if (!EXCSATReply.checkReply())
    {
        throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
    }
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM EXCSAT successful.");

    DDMACCSECRequestDataStream ACCSECReq = new DDMACCSECRequestDataStream(passwordType_, byteType_, null); // We currently don't need to pass the IASP to the ACCSEC, but may in the future.
    ACCSECReq.write(outStream);

    DDMACCSECReplyDataStream ACCSECRep = new DDMACCSECReplyDataStream();
    ACCSECRep.read(inStream);

    if (!ACCSECRep.checkReply(byteType_))
    {
        throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
    }
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM ACCSEC successful.");

    // Seeds for substitute password generation.
    byte[] clientSeed = null;
    byte[] serverSeed = null;
    if (byteType_ == AS400.AUTHENTICATION_SCHEME_PASSWORD)
    {
        clientSeed = ACCSECReq.getClientSeed();
        serverSeed = ACCSECRep.getServerSeed();
    }
    return new Object[] { clientSeed, serverSeed };
  }

  static void connectDDMPhase2(OutputStream outStream, InputStream inStream, byte[] userIDbytes, byte[] ddmSubstitutePassword, byte[] iaspBytes, int byteType_, String ddmRDB_, String systemName_) throws ServerStartupException, IOException
  {
    // If the ddmSubstitutePassword length is 8, then we are using DES encryption.  If its length is 20, then we are using SHA encryption.
    // Build the SECCHK request; we build the request here so that we are not passing the password around anymore than we have to.
    DDMSECCHKRequestDataStream SECCHKReq = new DDMSECCHKRequestDataStream(userIDbytes, ddmSubstitutePassword, iaspBytes, byteType_);

    // Send the SECCHK request.
    SECCHKReq.write(outStream);

    DDMSECCHKReplyDataStream SECCHKRep = new DDMSECCHKReplyDataStream();
    SECCHKRep.read(inStream);

    // Validate the reply.
    if (!SECCHKRep.checkReply())
    {
        throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
    }
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM SECCHK successful.");
    if (iaspBytes != null)
    {
      // We need to send an RDB datastream to make sure the RDB name we sent on the SECCHK is a valid RDB.
      DDMASPRequestDataStream aspReq = new DDMASPRequestDataStream(iaspBytes);
      aspReq.write(outStream);
      DDMASPReplyDataStream aspRep = new DDMASPReplyDataStream();
      aspRep.read(inStream);
      if (!aspRep.checkReply())
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "RDB name '"+ddmRDB_+"' is not a valid IASP name on system '"+systemName_+"'.");
        throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
      }
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM RDB name '"+ddmRDB_+"' verified.");
    }
  }

  static DataStream constructDDMDataStream(InputStream inStream, Hashtable replyStreams, AS400ImplRemote system) throws IOException
  {
    return DDMDataStream.construct(inStream, replyStreams, system);
  }
}

