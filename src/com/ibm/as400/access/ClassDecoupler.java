///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ClassDecoupler.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2017 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * This class exists to remove the dependencies AS400ImplRemote had on some of
 * the DDM and DB datastream classes.  In this way, a JarMaker-ed jt400.jar file
 * can effectively operate its AS400 object without needing the DDM or JDBC classes.
**/
public class ClassDecoupler
{
// For future use.
//    static
//    {
//        // Identify all DDM server reply data streams.
//        AS400Server.addReplyStream(new DDMEXCSATReplyDataStream(), AS400.RECORDACCESS);
//        AS400Server.addReplyStream(new DDMACCSECReplyDataStream(), AS400.RECORDACCESS);
//        AS400Server.addReplyStream(new DDMSECCHKReplyDataStream(), AS400.RECORDACCESS);
//        AS400Server.addReplyStream(new DDMASPReplyDataStream(), AS400.RECORDACCESS);
//    }

   /*@U4A  force the use of ENCUSRPWD or AES using JVM properties */ 
  public static boolean forceENCUSRPWD  = false;
  public static boolean forceAES = false; 
  static {
    String property = System.getProperty("com.ibm.as400.access.DDMPWDRQD");
    if (property != null) { 
      property = property.toUpperCase(); 
      if (property.equals("ENCUSRPWD")) {
        forceENCUSRPWD  = true; 
      }
    }
    property = System.getProperty("com.ibm.as400.access.DDMENCALC"); 
    if (property != null) { 
      property = property.toUpperCase(); 
      if (property.equals("AES")) {
        forceAES = true; 
      }
    }
  }
  
  static void freeDBReplyStream(DataStream ds)
  {
    if (ds instanceof DBReplyRequestedDS)
    {
      ((DBReplyRequestedDS)ds).returnToPool();
    }
  }

  static Object[] connectDDMPhase1(OutputStream outStream, InputStream inStream, boolean passwordType_, int byteType_, int connectionID) throws ServerStartupException, IOException
  {
    KeyPair keyPair = null; 
    String encryptUserId = null; 
    // Exchange server start up/security information with DDM server.
    // Exchange attributes.
    DDMEXCSATRequestDataStream EXCSATRequest = new DDMEXCSATRequestDataStream();
    if (Trace.traceOn_) EXCSATRequest.setConnectionID(connectionID);
    EXCSATRequest.write(outStream);

    DDMEXCSATReplyDataStream EXCSATReply = new DDMEXCSATReplyDataStream();
    if (Trace.traceOn_) EXCSATReply.setConnectionID(connectionID);
    EXCSATReply.read(inStream);

    if (!EXCSATReply.checkReply())
    {
        throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
    }
    byte[] jobString = EXCSATReply.getEXTNAM();
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM EXCSAT successful.");
    int requestByteType; 
    if (forceENCUSRPWD) {  /*@U4A*/ 
      requestByteType = AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD;
      encryptUserId =  "TRUE"; 
      try {
        if (!forceAES) {
          try {
             keyPair = DDMTerm.getDESKeyPair();
          } catch (InvalidAlgorithmParameterException iape) {
            // JDK 1.8 does not support 256 bit keys 
            // Upgrade to AES
            if (Trace.traceOn_) {
              Trace.log(Trace.DIAGNOSTIC, "ClassDecoupler: Upgrading to AES due to InvalidAlgorithmParameterException ", iape);
            }
            forceAES=true; 
            keyPair = DDMTerm.getAESKeyPair();
          }
        } else {
          keyPair = DDMTerm.getAESKeyPair();
        }
      } catch (GeneralSecurityException e) {
        ServerStartupException serverStartupException = new ServerStartupException(
            ServerStartupException.CONNECTION_NOT_ESTABLISHED);
        serverStartupException.initCause(e);
        throw serverStartupException; 
      }
    } else {
      requestByteType = byteType_; 
    }
    DDMACCSECRequestDataStream ACCSECReq = new DDMACCSECRequestDataStream(passwordType_, requestByteType, null, keyPair, forceAES); // We currently don't need to pass the IASP to the ACCSEC, but may in the future.
    if (Trace.traceOn_) ACCSECReq.setConnectionID(connectionID);
    ACCSECReq.write(outStream);

    DDMACCSECReplyDataStream ACCSECRep = new DDMACCSECReplyDataStream();
    if (Trace.traceOn_) ACCSECRep.setConnectionID(connectionID);
    ACCSECRep.read(inStream);

    if  ( !ACCSECRep.checkReply(requestByteType) ) 
    {
      // Check to see if *ENCUSRPWD supported, if so then renegotiate the setting @U4A 
      requestByteType = AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD; 
      if (  ACCSECRep.checkReplyForEUSRIDPWD(byteType_)) { 
        try { 
            if (!forceAES) { 
              try { 
                 keyPair = DDMTerm.getDESKeyPair(); 
              } catch (InvalidAlgorithmParameterException iape) {
                 // JDK 1.8 does not support 256 bit keys 
                 // Upgrade to AES
                if (Trace.traceOn_) {
                  Trace.log(Trace.DIAGNOSTIC, "ClassDecoupler: Upgrading to AES due to InvalidAlgorithmParameterException ", iape);
                }
                 forceAES=true; 
                 keyPair = DDMTerm.getAESKeyPair();
              }

            } else {
              keyPair = DDMTerm.getAESKeyPair(); 
            }
        } catch (GeneralSecurityException e) {
          ServerStartupException serverStartupException = new ServerStartupException(
              ServerStartupException.CONNECTION_NOT_ESTABLISHED);
          serverStartupException.initCause(e);
          throw serverStartupException; 
        }

        ACCSECReq = new DDMACCSECRequestDataStream(passwordType_, requestByteType, null, keyPair, forceAES); // We currently don't need to pass the IASP to the ACCSEC, but may in the future.
        if (Trace.traceOn_) ACCSECReq.setConnectionID(connectionID);
        ACCSECReq.write(outStream);

        ACCSECRep = new DDMACCSECReplyDataStream();
        if (Trace.traceOn_) ACCSECRep.setConnectionID(connectionID);
        ACCSECRep.read(inStream);
        // Check to see if we need to upgrade to AES
        if (ACCSECRep.aesUpgrade()) {
          try { 
            if (Trace.traceOn_) {
              Trace.log(Trace.DIAGNOSTIC, "ClassDecoupler: Upgrading to AES due to server negotiation");
            }
              keyPair = DDMTerm.getAESKeyPair();
              forceAES = true; 
          } catch (GeneralSecurityException e) {
            ServerStartupException serverStartupException = new ServerStartupException(
                ServerStartupException.CONNECTION_NOT_ESTABLISHED);
            serverStartupException.initCause(e);
            throw serverStartupException; 
          }
          
          ACCSECReq = new DDMACCSECRequestDataStream(passwordType_, requestByteType, null, keyPair, forceAES); // We currently don't need to pass the IASP to the ACCSEC, but may in the future.
          if (Trace.traceOn_) ACCSECReq.setConnectionID(connectionID);
          ACCSECReq.write(outStream);

          ACCSECRep = new DDMACCSECReplyDataStream();
          if (Trace.traceOn_) ACCSECRep.setConnectionID(connectionID);
          ACCSECRep.read(inStream);
          
          
        }
        
        if (!ACCSECRep.checkReplyForEUSRIDPWD(byteType_)) {
          throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
        }
        encryptUserId =  "TRUE"; 
      } else {      
        throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
      }
        
        
    }
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM ACCSEC successful.");

    // Seeds for substitute password generation.
    byte[] clientSeed = null;
    byte[] serverSeed = null;
    if (encryptUserId != null) { 
      serverSeed = ACCSECRep.getServerSeed();
    } else if ((byteType_ == AS400.AUTHENTICATION_SCHEME_PASSWORD) ||
               (requestByteType == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD))
    {
        clientSeed = ACCSECReq.getClientSeed();
        serverSeed = ACCSECRep.getServerSeed();
    }
    return new Object[] { clientSeed, serverSeed, jobString, encryptUserId, keyPair };
  }

  /*@U4C*/ 
  static void connectDDMPhase2(OutputStream outStream, InputStream inStream, byte[] userIDbytes, byte[] ddmSubstitutePassword, byte[] iaspBytes, int byteType_, String ddmRDB_, String systemName_, int connectionID) throws ServerStartupException, IOException, AS400SecurityException
  {
    // If the ddmSubstitutePassword length is 8, then we are using DES encryption.  If its length is 20, then we are using SHA encryption.
    // Build the SECCHK request; we build the request here so that we are not passing the password around anymore than we have to.
    DDMSECCHKRequestDataStream SECCHKReq = new DDMSECCHKRequestDataStream(userIDbytes, ddmSubstitutePassword, iaspBytes, byteType_);
    if (Trace.traceOn_) SECCHKReq.setConnectionID(connectionID);

    // Send the SECCHK request.
    SECCHKReq.write(outStream);

    DDMSECCHKReplyDataStream SECCHKRep = new DDMSECCHKReplyDataStream();
    if (Trace.traceOn_) SECCHKRep.setConnectionID(connectionID);
    SECCHKRep.read(inStream);

    // Validate the reply.
    if (!SECCHKRep.checkReply()) {
      int rc = SECCHKRep.getErrorCode();
      /* return the error found @U4A*/ 
      switch (rc) {
      case DDMTerm.PASSWORD_EXPIRED:
        throw new AS400SecurityException(
            AS400SecurityException.PASSWORD_EXPIRED);
      case DDMTerm.PASSWORD_INVALID:
        throw new AS400SecurityException(
            AS400SecurityException.PASSWORD_INCORRECT);
      case DDMTerm.PASSWORD_MISSING:
        throw new AS400SecurityException(
            AS400SecurityException.PASSWORD_NOT_SET);
      case DDMTerm.USERID_INVALID:
        throw new AS400SecurityException(
            AS400SecurityException.USERID_UNKNOWN);
      case DDMTerm.USERID_MISSING:
        throw new AS400SecurityException(
            AS400SecurityException.USERID_NOT_SET);
      case DDMTerm.USERID_REVOKED:
        throw new AS400SecurityException(
            AS400SecurityException.USERID_DISABLE);
      case DDMTerm.NEWPASSWORD_INVALID:
        throw new AS400SecurityException(
            AS400SecurityException.PASSWORD_NEW_DISALLOWED);

      default:
        throw new ServerStartupException(
            ServerStartupException.CONNECTION_NOT_ESTABLISHED);
      }
    }
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM SECCHK successful.");
    if (iaspBytes != null)
    {
      // We need to send an RDB datastream to make sure the RDB name we sent on the SECCHK is a valid RDB.
      DDMASPRequestDataStream aspReq = new DDMASPRequestDataStream(iaspBytes);
      if (Trace.traceOn_) aspReq.setConnectionID(connectionID);
      aspReq.write(outStream);
      DDMASPReplyDataStream aspRep = new DDMASPReplyDataStream();
      if (Trace.traceOn_) aspRep.setConnectionID(connectionID);
      aspRep.read(inStream);
      if (!aspRep.checkReply())
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "RDB name '"+ddmRDB_+"' is not a valid IASP name on system '"+systemName_+"'.");
        throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
      }
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "DDM RDB name '"+ddmRDB_+"' verified.");
    }
  }

  static DataStream constructDDMDataStream(InputStream inStream, Hashtable replyStreams, AS400ImplRemote system, int connectionID) throws IOException
  {
    return DDMDataStream.construct(inStream, replyStreams, system, connectionID);
  }
}

