///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileOutputStreamImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.OutputStream;
import java.io.IOException;

/**
  * The SpooledFileOutputStream class is used to write data into a server spooled file.
  **/
class SpooledFileOutputStreamImplRemote
implements SpooledFileOutputStreamImpl
{   
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final String DT_AUTO = "*AUTO";
    static final String DT_PRTF = "*PRTF";

    private byte[] buffer_ = new byte[4096];  // we'll buffer up to 4K before sending
    private byte[] buffer1Byte_ = new byte[1];  // for the write(byte) method

    private NPConversation conversation_;

    private NPCPAttribute  cpAttr_;             // attributes to create spooled file with
    private NPCPAttribute  cpCPFMsg_;           // any error messages come back here
    private NPCPIDSplF     cpIDSplF_;           // ID codepoint of spooled file created
    private NPCPIDOutQ     cpIDOutQ_;           // output queue ID (may be null)
    private NPCPIDPrinterFile  cpIDPrtrFile_;   // printer file ID (may be null)
    private NPCPSplFHandle cpSplFHndl_;         // handle used for writes and close request

    private boolean        fCreatePending_;
    private NPSystem       npSystem_;
    private int            offset_ = 0;                  // current offset into buffer
    private AS400ImplRemote sys_;

    /**
      * Constructs a SpooledFileOutputStream object.
      * Use this object to create a new spooled file on the given system
      * with the specified parameters.
      * @param system The system on which to create the spooled file.
      * @param options       Optional.  A print parameter list that contains
      *                          a list of attributes with which to create the spooled file.
      *                          The attributes set in <I>options</I> will
      *                          override those attributes in the printer file that is used.
      *                          The printer file used will be the one specified with the
      *                          <I>printerFile</I> parameter, or if that parameter is null,
      *                          it will be the default network print server printer file (QPNPSPRTF).
      *                          If the output queue is specified in <I>options</I>, it
      *                          will override any output queue passed in the <I>outputQueue</I>
      *                          parameter.
      *                          The following parameters may be set:
      * <ul>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY2">
      *          ATTR_ALIGN - Align page
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEYIFS_1">
      *          ATTR_BACK_OVERLAY - Back overlay integrated file system Name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY12">
      *          ATTR_BKOVL_DWN - Back overlay offset down
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY11">
      *          ATTR_BKOVL_ACR - Back overlay offset across
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY13">
      *          ATTR_CPI - Characters per inch
      *       </A>
      *  <li> (1) <A HREF="PrintAttributes.html#HDRKEY14">
      *          ATTR_CODEPAGE - Code page
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY17.5">
      *          ATTR_CONTROLCHAR - Control character
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY177">
      *          ATTR_CONVERT_LINEDATA - Convert line data
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY191">
      *          ATTR_CORNER_STAPLE - Corner staple		
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY17">
      *          ATTR_COPIES - Copies
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY0x0140">  
      *          ATTR_DAYS_UNTIL_EXPIRE - Days Until File Expires
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY24">
      *          ATTR_DBCSDATA - User-specified DBCS data
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY25">
      *          ATTR_DBCSEXTENSN - DBCS extension characters
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY26">
      *          ATTR_DBCSROTATE - DBCS character rotation
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY27">
      *          ATTR_DBCSCPI - DBCS characters per inch
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY28">
      *          ATTR_DBCSSISO - DBCS SO/SI spacing
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY29">
      *          ATTR_DFR_WRITE - Defer write
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY31">
      *          ATTR_PAGRTT - Degree of page rotation
      *       </A>
      *   <li> <A HREF="PrintAttributes.html#HDRKEY194">
      *       ATTR_EDGESTITCH_NUMSTAPLES - Edge Stitch Number of Staples		
      *      </A>
      *   <li> <A HREF="PrintAttributes.html#HDRKEY192">
      *         ATTR_EDGESTITCH_REF - Edge Stitch Reference	
      *       </A>
      *   <li> <A HREF="PrintAttributes.html#HDRKEY193">
      *        ATTR_EDGESTITCH_REFOFF - Edge Stitch Reference Offset
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY40">
      *          ATTR_ENDPAGE - Ending page
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY0x0141">  
      *          ATTR_EXPIRATION_DATE - Expiration Date
      *       </A>
      *  <li> (2) <A HREF="PrintAttributes.html#HDRKEY41">
      *          ATTR_FILESEP - File separators
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY42">
      *         ATTR_FOLDREC - Fold records
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY43">
      *         ATTR_FONTID - Font identifier
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEYIFS_3">
      *         ATTR_FORM_DEFINITION - Form definition integrated file system name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY44">
      *         ATTR_FORMFEED - Form feed
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY45">
      *         ATTR_FORMTYPE - Form type
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEYIFS_4">
      *         ATTR_FRONT_OVERLAY - Front overlay integrated file system same
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY51">
      *         ATTR_FTOVL_ACR - Front overlay offset across
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY52">
      *         ATTR_FTOVL_DWN - Front overlay offset down
      *       </A>
      *  <li> (1) <A HREF="PrintAttributes.html#HDRKEY53">
      *         ATTR_CHAR_ID - Graphic character set
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY54">
      *         ATTR_JUSTIFY - Hardware justification
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY55">
      *         ATTR_HOLD - Hold spool file
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY55.5">
      *         ATTR_HOLDPNDSTS - Hold Pending Status
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY180">
      *         ATTR_IPP_ATTR_CCSID - IPP Attributes-ccsid
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY181">
      *         ATTR_IPP_JOB_ID - IPP Job ID
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY182">
      *         ATTR_IPP_JOB_NAME - IPP Job Name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY183">
      *         ATTR_IPP_JOB_NAME_NL - IPP Job Name NL
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY184">
      *         ATTR_IPP_JOB_ORIGUSER - IPP Job Originating User Name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY185">
      *         ATTR_IPP_JOB_ORIGUSER_NL - IPP Job Originating User Name NL
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY186">
      *         ATTR_IPP PRINTER_NAME - IPP Printer  Name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY180.5">
      *         ATTR_IPP_ATTR_NL - IPP Natural Language
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY203">
      *         ATTR_JOBSYSTEM - Job System Name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY64">
      *         ATTR_LPI - Lines per inch
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY66">
      *         ATTR_MAXRCDS - Maximum spooled output records
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY80">
      *        ATTR_OUTPTY - Output priority
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEYIFS_6">
      *         ATTR_OUTPUT_QUEUE - Output queue integrated file system name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY84">
      *         ATTR_OVERFLOW - Overflow line number
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEYIFS_D">
      *         ATTR_PAGE_DEFINITION - Page definition integrated file system name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY62">
      *         ATTR_PAGELEN - Length of page
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY67">
      *         ATTR_MEASMETHOD - Measurement method
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY126">
      *         ATTR_PAGEWIDTH - Width of page
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY85">
      *         ATTR_MULTIUP - Pages per side
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY86">
      *         ATTR_POINTSIZE - Point size
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY87">
      *         ATTR_FIDELITY - Print fidelity
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY88">
      *         ATTR_DUPLEX - Print on both sides
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY89">
      *         ATTR_PRTQUALITY - Print quality
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY91">
      *         ATTR_PRTTEXT - Print text
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY92">
      *         ATTR_PRINTER - Printer
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY93">
      *         ATTR_PRTDEVTYPE - Printer device type
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY99">
      *         ATTR_RPLUNPRT - Replace unprintable characters
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY100">
      *         ATTR_RPLCHAR - Replacement character
      *       </A>
      *   <li> <A HREF="PrintAttributes.html#HDRKEY197">
      *        ATTR_SADDLESTITCH_NUMSTAPLES - Saddle Stitch Number of Staples		
      *       </A>
      *   <li> <A HREF="PrintAttributes.html#HDRKEY196">
      *         ATTR_SADDLESTITCH_REF - Saddle Stitch Reference		
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY102">
      *         ATTR_SAVE - Save spooled file
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY106">
      *         ATTR_SRCDRWR - Source drawer
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY107">
      *         ATTR_SPOOL - Spool the data
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY108">
      *         ATTR_SPOOLFILE - Spooled file name
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY111">
      *         ATTR_SCHEDULE - Spooled output schedule
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY112">
      *         ATTR_STARTPAGE - Starting page
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY117">
      *         ATTR_UNITOFMEAS - Unit of measure
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY118">
      *         ATTR_USERCMT - User comment
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY119">
      *         ATTR_USERDATA - User data
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY1061">
      *         ATTR_SPLSCS - Spool SCS
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEY1191">
      *         ATTR_USRDEFDATA - User defined data
      *       </A>
      *  <li> (3) <A HREF="PrintAttributes.html#HDRKEY1192">
      *         ATTR_USRDEFOPT - User defined options
      *       </A>
      *  <li> <A HREF="PrintAttributes.html#HDRKEYIFS_9">
      *         ATTR_USER_DEFINED_OBJECT - User defined object integrated file system name
      *       </A>
      *
      * </ul>
      * Note 1: Code page and graphical character set are dependent upon each
      *  other.  If you set one you must set the other.
      * <br>
      * Note 2: The special value of *FILE is not allowed when creating a new
      *  spooled file.
      * <br>
      * Note 3: Up to 4 user-defined options may be specified.
      *<p>
      * @param printerFile   Optional.  The printer file that should be used
      *                          to create the spooled file.  This printer file
      *                          must reside on the same server system that the
      *                          spooled file is being created on.
      * @param outputQueue   Optional.  The output queue on which to create the
      *                          spooled file.  The output queue must reside on
      *                          the same server system that the spooled file
      *                          is being created on.
      * @return An output stream that can be used to write data into the spooled
      *         file and to close the spooled file.
      * @exception AS400Exception If the server returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      **/

    public synchronized void createSpooledFileOutputStream(AS400Impl system,
                                         PrintParameterList options,
                                         PrinterFileImpl printerFile,
                                         OutputQueueImpl outputQueue)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               InterruptedException,
               IOException
    {
        // Force the system param to connect if it isn't already
        // so that we can check its system name against the
        // system outputQueue and printerFile are on

        // NPSystem npSystem = NPSystem.getSystem(system);
        // NPConversation conv = npSystem.getConversation();
        // npSystem.returnConversation(conv);  // we're done with it

        fCreatePending_ = true;   // we haven't issued the create yet
        sys_            = (AS400ImplRemote) system;
        npSystem_       = NPSystem.getSystem(sys_);
        conversation_   = npSystem_.getConversation();
        cpCPFMsg_       = new NPCPAttribute();
        cpIDSplF_       = new NPCPIDSplF();
        cpSplFHndl_     = new NPCPSplFHandle();


        // if the user passed in a printer file, get its ID
        if (printerFile != null)
        {
            cpIDPrtrFile_ = (NPCPIDPrinterFile)((PrinterFileImplRemote) printerFile).getIDCodePoint();
        }

        // if the user passed in an output queue, get its ID
        if (outputQueue != null)
        {
            cpIDOutQ_ = (NPCPIDOutQ)((OutputQueueImplRemote) outputQueue).getIDCodePoint();
        }

        /////////////////////////////////////////////////////////////////////////
        // figure out what data type we're using.
        // If the user has specified nothing or *AUTO
        //   delay the open until we get some data to analyze.
        // If the user has specified *PRTF, change it to be nothing and the server
        //   will use what is in the printer file.
        /////////////////////////////////////////////////////////////////////////
        String strDataType = null;

        cpAttr_ = new NPCPAttribute();       // we need our own copy because we may change things

        if (options != null)
        {
            cpAttr_.addUpdateAttributes(options.getAttrCodePoint());
        }

        strDataType = cpAttr_.getStringValue(PrintObject.ATTR_PRTDEVTYPE);
        if (strDataType == null)
        {
             // datastream type not specified, so use *AUTO
            strDataType = DT_AUTO;
        } else {
            ///////////////////////////////////////////////////////////////
            // strip trailing nulls & uppercase user specified value.
            ///////////////////////////////////////////////////////////////
            strDataType = strDataType.trim();
            strDataType.toUpperCase();
        }

        //////////////////////////////////////////////////////////////////
        // strDataType now contains the data type to use, whether the user
        // explicitly set it or we defaulted it.
        // Need to see if we should delay the create here
        // IF not
        //    Need to change *PRTF to ""
        //    IF pAttrs specified, change it to "".
        // ELSE
        //   set create pending flag on
        // ENDIF
        //////////////////////////////////////////////////////////////////
        if (strDataType.equals(DT_AUTO))
        {
           fCreatePending_ = true;  // data type is automatic so wait for create
        } else {
           if (strDataType.equals(DT_PRTF))
           {
              strDataType = "";
           }
           makeCreateRequest(strDataType);
        }

    }


    /**
      * Closes the stream.
      * It must be called to release any resources associated with the stream.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void close()
       throws IOException
    {
        if (conversation_ == null)
        {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        // if there is any data pending. write it out
        if (offset_ != 0)
        {
            makeWriteRequest(buffer_, 0, offset_);
            offset_ = 0;
        }
        // if create is still pending here, then we have an empty spooled
        // file, send it up anyway...
        if (fCreatePending_)
        {
            makeCreateRequest(null);
        }


        NPDataStream closeReq = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream closeRep = new NPDataStream(NPConstants.SPOOLED_FILE);

        // setup the close request data stream
        closeReq.setAction(NPDataStream.CLOSE);
        closeReq.addCodePoint(cpSplFHndl_);

        // setup the close reply to catch the return codepoints
        closeRep.addCodePoint(cpCPFMsg_);

        try
        {
           // make the request
           conversation_.makeRequest(closeReq, closeRep);
        }

        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
            throw new IOException(e.toString());
        }
        finally
        {
           // if we still have a conversation, return it
           if (conversation_ != null)
           {
               npSystem_.returnConversation(conversation_);
               conversation_ = null;
           }
        }
    } // close()



    /**
      * Closes the stream when garbage is collected.
      * @exception Throwable If an error occurs.
      **/
    protected void finalize()
       throws Throwable
    {
        // We must be very careful here to not try to receive a
        // reply from the server - we are being called by the
        // garbage collector which could be running on the
        // background thread of the AS400Server object in our
        // conversation.  If we were to call back to the AS400Server
        // to wait for a reply we could end up in a deadlock situation
        // Any requests we make we'll discard the reply on

        // if we still have a conversation
        // they must not have sent the close yet

       if (conversation_ != null)
       {
           // if we have sent the create request
           if (!fCreatePending_)
           {
               // send up a close request and ignore the reply
               NPDataStream closeReq = new NPDataStream(NPConstants.SPOOLED_FILE);


               // setup the close request data stream
               closeReq.setAction(NPDataStream.CLOSE);
               closeReq.addCodePoint(cpSplFHndl_);

               AS400Server server= conversation_.getServer();
               if (server != null)
               {
                   // @D closeReq.setHostCCSID(conversation_.getHostCCSID());
                   closeReq.setConverter(conversation_.getConverter());
                   server.sendAndDiscardReply(closeReq);
               }
           }
           npSystem_.returnConversation(conversation_);
           conversation_ = null;
           npSystem_ = null;
       }
       super.finalize();   // always call super.finalize()!
    }



    /** Flushes the stream.  This will write any buffered output bytes.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void flush()
        throws IOException
    {
        // send what we have, if any
        if (offset_ != 0)
        {
            makeWriteRequest(buffer_, 0, offset_);
            offset_ = 0;
        }
    }



    /** Returns the spooled file that was created (or is being created) with
      * this output stream.
      * @return A reference to the spooled file object.
      **/
    public synchronized NPCPIDSplF getSpooledFile()
       throws IOException
    {
        // @D SpooledFile sf = null;
        NPCPIDSplF sfID = null;

        // flush any data we have first
        // if the file hasn't been closed already
        if (conversation_ != null)
        {
            flush();
        }

        // if we've issued the create already
        if (!fCreatePending_)
        {
            // return the spooled file that we created
            
            // sf = new SpooledFile(sys_, cpIDSplF_, null);   // @D (see below)
            // The call to create the SpooledFile will be made on the proxy side
            sfID = cpIDSplF_;
        } else {
            Trace.log(Trace.ERROR, "Spooled File has not been created.");
            throw new
                ExtendedIllegalStateException(ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
        }

        // return sf;    // @D
        return sfID;
    }



    /**
      * Generates the create request.
      * @param strDataType  The PRTDEVTYPE to use (*SCS, *AFPDS, *USERASCII)...
      * @exception IOException If an error occurs while communicating with the server.
      **/
    private synchronized void makeCreateRequest(String strDataType)
       throws IOException
    {
        if (conversation_ == null)
        {
        Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        /////////////////////////////////////////////////////////////////
        // if datatype is something (override the printer file
        //  set the datatype in the attributes
        /////////////////////////////////////////////////////////////////
        if ((strDataType != null) &&
            (!strDataType.equals("")))     // if strDataType is something
        {
           if (cpAttr_ == null)
            {
                cpAttr_ = new NPCPAttribute();
            }
            cpAttr_.setAttrValue(PrintObject.ATTR_PRTDEVTYPE, strDataType);
        }
        NPDataStream createReq = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream createRep = new NPDataStream(NPConstants.SPOOLED_FILE);

        // setup the create request data stream
        createReq.setAction(NPDataStream.CREATE);
        if (cpAttr_ != null)
        {
            createReq.addCodePoint(cpAttr_);
        }
        if (cpIDOutQ_ != null)
        {
           createReq.addCodePoint(cpIDOutQ_);
        }
        if (cpIDPrtrFile_ != null)
        {
           createReq.addCodePoint(cpIDPrtrFile_);
        }

        // setup the create reply to catch the return codepoints
        createRep.addCodePoint(cpSplFHndl_);
        createRep.addCodePoint(cpCPFMsg_);
        createRep.addCodePoint(cpIDSplF_);

        try
        {
           // make the request
           conversation_.makeRequest(createReq, createRep);
        }

        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
            throw new IOException(e.toString());
        }
        fCreatePending_ = false;   // reset the create flag

    } // makeCreateRequest()



    /**
      * Generates the write request.
      * @param buf  The array of bytes to write.
      * @param length  The length of data to write.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    private synchronized void makeWriteRequest(byte[] buf,
                                               int    offset,
                                               int    len)
         throws IOException
    {
        if (conversation_ == null)
        {
        Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        // if the create is still pending here then
        // we should sniff the data and then issue the create
        // request with the correct data type
        if (fCreatePending_)
        {
            String strDataType = NPDataAnalyzer.sniff(buf, offset, len);
            makeCreateRequest(strDataType);
        }

        //
        // now make the write request
        //
        NPDataStream writeReq = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream writeRep = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPCPData     cpData   = new NPCPData();
        cpData.setDataBuffer(buf, len, offset);

        // setup the write request data stream
        writeReq.setAction(NPDataStream.WRITE);

        writeReq.addCodePoint(cpSplFHndl_);
        writeReq.addCodePoint(cpData);


        // setup the create reply to catch the return codepoints
        writeRep.addCodePoint(cpCPFMsg_);

        try
        {
           // make the request
           conversation_.makeRequest(writeReq, writeRep);
        }

        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
            throw new IOException(e.toString());
        }

    }



    /**
      * Writes up to <i>length</i> bytes of data from the byte array <i>data</i>,
      * starting at <i>offset</i>, to this spooled file.
      *
      * @param data   The data to be written.
      * @param offset The start offset in the data.
      * @param length The number of bytes that are written.
      *
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public synchronized void write(byte data[], int offset, int length)
        throws IOException
    {
        if (conversation_ == null)
        {
            Trace.log(Trace.ERROR, "SpooledFileOutputStream already closed.");
            throw new IOException();
        }

        int currentSourceOffset = offset;
        int dataLeftToSend = length;

        while(dataLeftToSend > 0)
        {


            ////////////////////////////////////////////////////////////
            // calculate the available buffer space left in the current
            // buffer
            ////////////////////////////////////////////////////////////
            int availLen = buffer_.length - offset_;

            if (availLen >= dataLeftToSend)
            {
                //////////////////////////////////////////////////////////
                // If we have enough to hold it all
                //   Move it all to the current buffer
                //////////////////////////////////////////////////////////
                System.arraycopy(data, currentSourceOffset,
                                 buffer_, offset_,
                                 dataLeftToSend);
                currentSourceOffset += dataLeftToSend;
                offset_ += dataLeftToSend;
                dataLeftToSend -= dataLeftToSend;
            } else {
                if (availLen != 0)
                {
                    ///////////////////////////////////////////////////////
                    // If we have ANY room at all
                    //   Move what we can from the callers buffer to
                    //     the current write bitstream
                    ///////////////////////////////////////////////////////
                    System.arraycopy(data, currentSourceOffset,
                                     buffer_, offset_,
                                     availLen);

                    currentSourceOffset += availLen;
                    offset_ += availLen;
                    dataLeftToSend -= availLen;
                 }

                 ///////////////////////////////////////////////////////
                 // if there is any data in the bytestream, send it out
                 ///////////////////////////////////////////////////////
                 if (offset_ != 0)
                 {
                     makeWriteRequest(buffer_, 0, offset_);
                     offset_ = 0;
                 }
            }
        }  // end while
    } // write(byte[], int, int)

}
