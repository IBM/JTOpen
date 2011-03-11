///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: WriterJobImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 * The WriterJob class represents a server writer job.
 * An instance of this class can be used to manipulate an individual
 * writer.  Use the start method to obtain a instance of this class.
 *
 * See <a href="WriterJobAttrs.html">Writer Job Attributes</a> for
 * valid attributes.
 *
 **/

class WriterJobImplRemote extends PrintObjectImplRemote
implements WriterJobImpl
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;


    private synchronized void buildAttrIDsToRtv()
    {
        if (!fAttrIDsToRtvBuilt_)
        {
            fAttrIDsToRtvBuilt_ = true;
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNAME);       // writer job name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNUM);        // writer job number
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBSTS);        // writer job status
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBUSER);       // writer job user name
        }
    }



    /**
     * Ends a writer on the server.
     *
     * @param endType When to end the writer.
     *  May be any of the following values:
     * <UL>
     *   <LI> *CNTRLD - The writer is ended at the end of the current spooled file.
     *   <LI> *IMMED - The writer is ended immediately.
     *   <LI> *PAGEEND - The writer is ended at the end of the current page.
     * </UL>
     *  <i>endType</i> may be null.  If <i>endType</i> is not specified, the default is
     * *IMMED.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         server operating system is not at the correct level.
     **/
    public void end(String endType)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.WRITER_JOB); 
        NPDataStream returnDS = new NPDataStream(NPConstants.WRITER_JOB);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute cpCPFMessage = new NPCPAttribute();

        sendDS.setAction(NPDataStream.END);
        sendDS.addCodePoint(getIDCodePoint());

        if (endType != null)
        {
            NPCPAttribute cpAttr = new NPCPAttribute();
            cpAttr.setAttrValue(PrintObject.ATTR_WTREND, endType);
            sendDS.addCodePoint(cpAttr);
        }

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

    } // end end



    NPCPAttributeIDList getAttrIDsToRetrieve()
    {
        if (!fAttrIDsToRtvBuilt_) {
            buildAttrIDsToRtv();
        }
        return attrsToRetrieve_;
    }

   // retrieve only one attribute 
   NPCPAttributeIDList getAttrIDsToRetrieve(int attrToRtv)
    {
        if (!fAttrIDsToRtvBuilt_) {
            attrsToRetrieve_.addAttrID(attrToRtv);
        }
        return attrsToRetrieve_;
    }


    /**
     * Returns the name of the writer.
     *
     * @return The name of the writer.
     **/
    public String getName()
    {
        NPCPID IDCodePoint = getIDCodePoint();

        if( IDCodePoint == null ) {
            return PrintObject.EMPTY_STRING; // ""
        } else {
            return IDCodePoint.getStringValue(PrintObject.ATTR_WTRJOBNAME);
        }
    }



    /**
     * Starts a writer on the server.
     * Use this method to start a new writer job on the given server
     * with the specified parameters.
     * @param system The system on which to start the writer job.
     * @param printer The printer that should be used
     *                to start the writer job.  This printer
     *                must reside on the same server that the
     *                writer job is being started on.
     * @param options Optional.  A print parameter list that contains
     *                          a list of attributes to start the writer job.
     *                          The output queue parameters set in this list override the
     *                          output queue parameter.
     *                          The following parameters may be set:
     * <ul>
     * <li> <A HREF="PrintAttributes.html#HDRKEY2">
     *         ATTR_ALIGN - Align page
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY3">
     *         ATTR_ALWDRTPRT - Allow direct print
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY6">
     *         ATTR_AUTOEND - Automatically end writer
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY39">
     *         ATTR_DRWRSEP - Drawer for separators
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY41">
     *         ATTR_FILESEP - File separators
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY45">
     *         ATTR_FORMTYPE - Form type
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY551">
     *         ATTR_WTRINIT - Initialize the writer
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY57">
     *         ATTR_JOBNAME - Job name
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY58">
     *         ATTR_JOBNUMBER - Job number
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY60">
     *         ATTR_JOBUSER - Job user
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY46">
     *         ATTR_FORMTYPEMSG - Form type message option
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEYIFS_5">
     *         ATTR_MESSAGE_QUEUE - Message queue integrated file system  name
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEYIFS_6">
     *         ATTR_OUTPUT_QUEUE - Output queue integrated file system  name
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY108">
     *         ATTR_SPOOLFILE - Spooled file name
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY109">
     *         ATTR_SPLFNUM - Spooled file number
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY123">
     *         ATTR_WTRAUTOEND - When to automatically end writer
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY133">
     *         ATTR_WTRSTRPAGE - Writer starting page
     *      </A>
     * <li> <A HREF="PrintAttributes.html#HDRKEY129">
     *         ATTR_WTRJOBNAME - Writer job name
     *      </A>
     *
     * </ul>
     * <br>
     *
     * @param outputQueue Optional.  The output queue to start the
     *                               writer job.  The output queue must reside on
     *                               the same server that the writer job
     *                               is being created on.
     *
     * @return A writer job object that was created.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     **/
    public /* static @A1D */ NPCPIDWriter start(AS400Impl system,
                  PrintObjectImpl printer,
                  PrintParameterList options, 
                  OutputQueueImpl outputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException

    {
        // note: This is a static method
        //
        // build the send and reply datastreams
        // We send up the printer ID codepoint and optionally an output queue ID codedpoint
        //  and an Attribute/Attribute Value code point.
        // We receive a writer job ID code point or maybe a CPF error message code point
        //

        NPDataStream sendDS = new NPDataStream(NPConstants.WRITER_JOB);
        NPDataStream returnDS = new NPDataStream(NPConstants.WRITER_JOB);
        NPCPAttribute cpCPFMessage = new NPCPAttribute();
        NPCPIDWriter cpWriterID = new NPCPIDWriter();

        NPSystem npSystem = NPSystem.getSystem((AS400ImplRemote) system);
        sendDS.setAction(NPDataStream.START);

        sendDS.addCodePoint(((PrinterImplRemote) printer).getIDCodePoint());
        if (outputQueue != null)
        {
            sendDS.addCodePoint(((OutputQueueImplRemote) outputQueue).getIDCodePoint());
        }
        if (options != null)
        {
            sendDS.addCodePoint(options.getAttrCodePoint());
        }
        returnDS.addCodePoint(cpCPFMessage);
        returnDS.addCodePoint(cpWriterID);
        int rc = npSystem.makeRequest(sendDS, returnDS);
        //
        // if there is some error that occured that makerequest didn't throw an
        // exception for, throw a generic AS400_ERROR here with the RC in the text
        //
        if (rc != NPDataStream.RET_OK)
        {
             Trace.log(Trace.ERROR, "Bad RC starting writer from server.  RC = " + rc );
             throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR,
                                                       " Network Print Server RC = " + rc);
        }

        // everything went OK, create and return the WriterJob object
        // Changed method to return cpWriterID instead of WriterJob
        // Create the WriterJob on the proxy(client) side instead.
        // return new WriterJob(system, cpWriterID, null);
        return cpWriterID;

    } // end start

    // go to the server and get the lastest attributes for this object
    // Override this from the PrintObject class because the network
    // print server doesn't allow retrieve attributes on a writer
    // We will implement it by going to the to list 1 writer (this one)
    // and if the writer is there we will get its new attributes.  If
    // the writer is not there, we will get back an empty list an we
    // will return an ErrorCompletingRequestException with a RC of
    // WRITER_JOB_ENDED
    //
    void updateAttrs(NPCPAttributeIDList attrIDs)
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException
    {
        NPDataStream req   = new NPDataStream(NPConstants.WRITER_JOB);
        NPDataStream reply = new NPDataStream(NPConstants.WRITER_JOB);
        NPSystem npSystem  = NPSystem.getSystem(getSystem());

        // code point we will send up to select just one writer job
        NPCPSelWrtJ writerSelection = new NPCPSelWrtJ();
        String strWriter = getName();
        writerSelection.setWriter(strWriter);

        // This code point will hold the returned attribute OR the
        // CPF message if we get one of those instead
        NPCPAttribute cpAttrs = new NPCPAttribute();

        // This code point will hold the writer job ID that comes back
        // on the list request - we will just throw it away
        NPCPIDWriter cpWriterID = new NPCPIDWriter();

        req.setAction(NPDataStream.LIST);
        req.addCodePoint(writerSelection);
        req.addCodePoint(attrIDs);
        reply.addCodePoint(cpAttrs);
        reply.addCodePoint(cpWriterID);

       int rc = npSystem.makeRequest(req, reply);
        switch (rc)
        {
            case 0:
               if (attrs != null)
               {
                    attrs.addUpdateAttributes(cpAttrs);
               } else {
                     attrs = cpAttrs;
               }
               break;
            case NPDataStream.RET_EMPTY_LIST:
               Trace.log(Trace.ERROR, "Writer Job " + strWriter + " not active");
               throw new ErrorCompletingRequestException(
                           ErrorCompletingRequestException.WRITER_JOB_ENDED,
                           strWriter);
            default:
               Trace.log(Trace.ERROR, "NetPrint DataStream RC = " + rc);
               throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
        }
    }  // updateAttrs()

}
