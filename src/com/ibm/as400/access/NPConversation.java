///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPConversation.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.util.*;

/**
  * NPConversation class - this internal class is used to represent an network print
  * conversation to a particular system.  You can get one of these things from
  * a NPServer object.
  * It is a wrapper for the AS400Server object that takes care of initializing the
  * conversation with the host (exchanging CCSIDs and Language IDs) and holding
  * the attributes of the server job.
  **/
class NPConversation extends Object
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";



    // private members
    // @B1D private int          hostCCSID_ = 37;
    private ConverterImplRemote converter_        = null;             // @B1A
    private AS400ImplRemote     system_           = null;             // @B1A 
    private AS400Server server_;
    private NPCPAttribute serverAttributes_;




   /**
    *  constructor -
    **/
    NPConversation(AS400ImplRemote aSystem, AS400Server aServer)        // @B1A
       throws AS400Exception,
              ErrorCompletingRequestException,
              IOException, InterruptedException
    {
        serverAttributes_ = new NPCPAttribute();
        system_ = aSystem;                              // @B1A
        server_ = aServer;
        try {                                           // @A2A
            retrieveServerAttributes();    //xchange our attributes
        }                                               // @A2A
        catch (RequestNotSupportedException e) {        // @A2A
             throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);  // @A2A
        }                                               // @A2A

    }

    /**
     * get reference to the AS400Server object for doing your own thing
     **/
    AS400Server getServer()
    {
        return server_;
    }

    /**
     * get a server attribute
     *@parameter attributeID which attribute to retrieve.  Maybe
     *                       and of ATTR_NPSLEVEL,
     *                       ATTR_JOBUSER, ATTR_JOBNUMBER or
     *                       ATTR_JOBNAME
     *@see NPObject for the attribute ID defines
     **/
    String getAttribute(int attributeID)
    {
        return serverAttributes_.getStringValue(attributeID);
    }

    // @B1D int getHostCCSID()
    // @B1D {
    // @B1D    return hostCCSID_;
    // @B1D }



    ConverterImpl getConverter()        // @B1A
    {                                   // @B1A
        return converter_;              // @B1A
    }                                   // @B1A



    /**
     * make a request of this host server
     * @return the NPDataStream return code
     **/
    int makeRequest(NPDataStream request, NPDataStream reply)
      throws AS400Exception,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException   // @A2A
    {
    	int rc = 0;
    	int correlation = server_.newCorrelationId();
    	// @B1D request.setHostCCSID(hostCCSID_);
    	// @B1D reply.setHostCCSID(hostCCSID_);
        request.setConverter(converter_);               // @B1A
        reply.setConverter(converter_);                 // @B1A
    	request.setCorrelation(correlation);
    	server_.clearInstanceReplyStreams();
    	server_.addInstanceReplyStream(reply);
    	server_.send(request, correlation);

    	reply = (NPDataStream)server_.receive(correlation);
    	if (reply == null)
    	{
    	    Trace.log(Trace.ERROR, "Didn't get me datastream back!");
    	    throw new NullPointerException();
    	} else {
    	    rc = reply.getReturnCode();
    	    if (rc != 0)
    	    {
    	        // Do not log an ERROR here - some RCs are not hard ERRORs
    	        // and the RC was already logged as INFORMATION in NPDataStream.
        		// Trace.log(Trace.ERROR, "DataStream RC = " + rc);
        		switch (rc)
        		{
        		    // For CPF_MESSAGEs we throw an AS400Exeption error
        		    case NPDataStream.RET_CPF_MESSAGE:


                       NPCPAttribute cpCPFMessage = (NPCPAttribute)reply.getCodePoint(NPCodePoint.ATTRIBUTE_VALUE);
                       if (cpCPFMessage != null)
                       {
                          String msgFileName = new String();                                      //&C1
                          String msgLibraryName = new String();                                   //&C1
                          String msgDate = cpCPFMessage.getStringValue(PrintObject.ATTR_DATE);    //&C1
                          String msgTime = cpCPFMessage.getStringValue(PrintObject.ATTR_TIME);    //&C1
                          String strCPFMessageID = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGID);
                          String strCPFMessageText = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGTEXT);
                          String strCPFMessageHelp = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGHELP);
                          String strCPFMessageDefaultReply = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGREPLY); //&C1
                          Integer intCPFMessageSeverity = cpCPFMessage.getIntValue(PrintObject.ATTR_MSGSEV); //&C1
                          String strCPFMessageType = cpCPFMessage.getStringValue(PrintObject.ATTR_MSGTYPE); //&C1
                          byte[] substitutionData = new byte[cpCPFMessage.getLength()];                  //&C1
                          Integer intCPFMessageType = new Integer(strCPFMessageType);

                          Trace.log(Trace.ERROR, "CPF Message("+strCPFMessageID+") = "
                               + strCPFMessageText + ", HelpText= " +strCPFMessageHelp);

                         // Create an AS400Message object
                         AS400Message msg = new AS400Message(strCPFMessageID, strCPFMessageText, msgFileName , msgLibraryName, intCPFMessageSeverity.intValue(), intCPFMessageType.intValue(),  substitutionData , strCPFMessageHelp, msgDate, msgTime, strCPFMessageDefaultReply);

                        // AS400Message msg = new AS400Message(strCPFMessageID, strCPFMessageText);
                         msg.setHelp(strCPFMessageHelp);

                         // throw an exception containing our CPF message
                         throw new AS400Exception(msg);

                       }

                       //
                       // For READ_EOF we let the caller handle that one
                       // For empty list we just return the RC and that is OK.
                       // For return code 4 and 21 (INVALID request action combination
                       // and function not supported yet) we just return the RC to
                       // the caller and let them decide if they want to throw
                       // an exception or just consume it.  Exchanging NLVs to older
                       // systems gives us RC of 21 and we just consume it.
                       // Opening AFP resources on pre-v3r7 systems gives us
                       // invalid request-action rc and we end up throwing a
                       // RequestNotSupportedException elsewhere.  We do not throw
                       // RequestNotSupportedException here because we do not want
                       // to have it on every request
                       // For SPLF_NO_MESSAGE we let the caller handle that one
                       //
                       case NPDataStream.RET_READ_EOF:
                       case NPDataStream.RET_EMPTY_LIST:
                       case NPDataStream.RET_INV_REQ_ACT:
                       case NPDataStream.RET_FUNCTION_NOT_SUP:
                       case NPDataStream.RET_SPLF_NO_MESSAGE:
                       case NPDataStream.RET_READ_OUT_OF_RANGE:   // @A1A
                       case NPDataStream.RET_PAGE_OUT_OF_RANGE:   // @A1A
                          break;
                       // NLV_NOT_AVAILABLE  means that we requested the server change
                       // its attributes to use our NLV and it doesn't have our NLV
                       // installed.  We'll just consume this RC and continue with the default
                       // host NLV
                       case NPDataStream.RET_NLV_NOT_AVAILABLE:
                          Trace.log(Trace.WARNING, "NetPrint DataStream host cannot change to our NLV.  RC = " + rc);
                          break;
                       //
                       // fill in the rest of these RCs with appropriate
                       // exceptions - Can be ErrorCompletingRequestExceptions
                       // or internal errors/datastream errors.
                       case NPDataStream.RET_INV_ACT_ID:                                            // @A2A
                          Trace.log(Trace.ERROR, "NetPrint DataStream RC = " + rc);                 // @A2A
                          throw new RequestNotSupportedException(
                                            this.getAttribute(PrintObject.ATTR_NPSLEVEL),
                                            RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT); // @A2A
                       default:
                          Trace.log(Trace.ERROR, "NetPrint DataStream RC = " + rc);
                          throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);



                }

    		}


    	}
    	return rc;

    }

        private void retrieveServerAttributes()
      throws AS400Exception,
             ErrorCompletingRequestException,
             IOException, InterruptedException,
             RequestNotSupportedException  // @A2A
    {
        // first thing to do is to get the host CCSID
        int rc;
        NPDataStream req = new NPDataStream(NPConstants.NP_SERVER);  // @B1C
        NPDataStream reply = new NPDataStream(NPConstants.NP_SERVER); // @B1C
        NPCPAttributeIDList attrsToRetrieve = new NPCPAttributeIDList();
        NPCPAttribute sendAttrs = new NPCPAttribute();
        NPCPAttribute recvAttrs = new NPCPAttribute();      // catches returned attributes or CPF message
        //-----------------------------------------------------------------------
        // 1. Exchange CCSIDs
        //-----------------------------------------------------------------------
        // Our ccsid is always new unicode (0x34b0.

//        sendAttrs.setAttrValue(PrintObject.ATTR_NPSCCSID, 0x34b0);  //  HARDCODE to client CCSID 0x34B0 - new unicode
        sendAttrs.setAttrValue(PrintObject.ATTR_NPSCCSID,
                               ExecutionEnvironment.getCcsid()); //
        attrsToRetrieve.addAttrID(PrintObject.ATTR_NPSCCSID);    // this is what we want back


        req.setAction(NPDataStream.RETRIEVE_ATTRIBUTES);
        req.addCodePoint(sendAttrs);
        req.addCodePoint(attrsToRetrieve);

        reply.addCodePoint(recvAttrs);

        rc = makeRequest(req, reply);

        if (rc == NPDataStream.RET_OK)
        {
            Integer serverCCSID = recvAttrs.getIntValue(PrintObject.ATTR_NPSCCSID);
            if (serverCCSID != null)
            {
                int hostCCSID = serverCCSID.intValue();                                     // @B1C
                serverAttributes_.setAttrValue(PrintObject.ATTR_NPSCCSID, hostCCSID);       // @B1C
                converter_ = ConverterImplRemote.getConverter(hostCCSID, system_);          // @B1C
            }

            //-----------------------------------------------------------------------
            // 2. Exchange NLV ID (ie: "2924")
            //-----------------------------------------------------------------------
            // Call to change the server language attribute
            // we send up a string (in the server's CCSID which is why we first
            // had to get the server CCSID value) indicating our language (ie "2924").
            // The server will attempt to use that language for CFP messages that it
            // sends back (changes its library list order) but it might not be able to
            // if that language isn't on the 400 for the server's product.  The server
            // will send back the langauge that it is using on the reply.

            sendAttrs.reset();
            attrsToRetrieve.reset();


            //
            // we send up our NLV_ID (ie: "2924") and we ask the server
            // to send back its NLV_ID
            //
            sendAttrs.setAttrValue(PrintObject.ATTR_NLV_ID,
                               system_.getNLV());  // @F1C
            attrsToRetrieve.addAttrID(PrintObject.ATTR_NLV_ID);

            req.setAction(NPDataStream.CHANGE_ATTRIBUTES);
            req.resetCodePoints();
            req.addCodePoint(sendAttrs);
            req.addCodePoint(attrsToRetrieve);

            recvAttrs.reset();
            reply.resetCodePoints();
            reply.addCodePoint(recvAttrs);

            // make the change server request
            //  makeRequest() will throw an exception if it fails
            makeRequest(req, reply);

            // put the attributes that we received back (should just be the
            // the server's NLV_ID) into our serverAttributes_ codepoint
            serverAttributes_.addUpdateAttributes(recvAttrs);

            //-----------------------------------------------------------------------
            // 3. Get the other server attributes (job info and VRM)
            //-----------------------------------------------------------------------

            // set which attribute we are interested in now from the server
            //  Job name, number, user, version
            attrsToRetrieve.reset();
            attrsToRetrieve.addAttrID(PrintObject.ATTR_NPSLEVEL);
            attrsToRetrieve.addAttrID(PrintObject.ATTR_JOBNAME);
            attrsToRetrieve.addAttrID(PrintObject.ATTR_JOBUSER);
            attrsToRetrieve.addAttrID(PrintObject.ATTR_JOBNUMBER);

            req.setAction(NPDataStream.RETRIEVE_ATTRIBUTES);
            req.resetCodePoints();
            req.addCodePoint(attrsToRetrieve);

            recvAttrs.reset();
            reply.resetCodePoints();
            reply.addCodePoint(recvAttrs);

            rc = makeRequest(req, reply);
            if (rc == NPDataStream.RET_OK)
            {
                serverAttributes_.addUpdateAttributes(recvAttrs);
                // if information tracing is on,
                //    log the VRM of the system we are talking to
                if (Trace.isTraceOn() &&
                    Trace.isTraceInformationOn())
                {
                    Trace.log(Trace.INFORMATION,
                              " Network Print Server started ");
                    Trace.log(Trace.INFORMATION,
                               "   NetPrint Server info (System/Job#/JobUser/JobName): " +
                   //            server_.getSystem().getSystemName() + "/" +
                               serverAttributes_.getStringValue(PrintObject.ATTR_JOBNUMBER) + "/" +
                               serverAttributes_.getStringValue(PrintObject.ATTR_JOBUSER) + "/" +
                               serverAttributes_.getStringValue(PrintObject.ATTR_JOBNAME)
                              );

                    Trace.log(Trace.INFORMATION,
                              "   NetPrint Server VRM = " +
                                 serverAttributes_.getStringValue(PrintObject.ATTR_NPSLEVEL)
                             );
                }

            }

        }

    } // retrieveServerAttributes
} // NPServer class
