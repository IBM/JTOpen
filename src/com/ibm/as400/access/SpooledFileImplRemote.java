///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SpooledFileImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 * The SpooledFile class represents a server spooled file.
 * You can use an instance of this class to manipulate an individual
 * server spooled file (hold, release, delete, send, read, and so on).
 * To create new spooled files on the server, use the
 * SpooledFileOutputStream class.
 *
 * See <a href="SpooledFileAttrs.html">Spooled File Attributes</a> for
 * valid attributes.
 *
 * @see PrintObjectInputStream
 * @see PrintObjectPageInputStream
 * @see PrintObjectTransformedInputStream
 **/

class SpooledFileImplRemote extends PrintObjectImplRemote
implements SpooledFileImpl
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;
    private static final int CMD_SEND_NET = 1;
    private static final int CMD_SEND_TCP = 2;

    transient NPCPMsgHandle cpMsgHandle_ = null;
    transient boolean       fMsgRetrieved_ = false;

    // We have decide that spooled files are too transient to
    // be JavaBeans

    /** Replies to the message that caused the spooled file to wait.
     *
     * @param reply The string that contains the reply for the message.
     *              The default reply can be obtained by calling
     *              the getMessage() method, and then calling the
     *              getDefaultReply() method on the message object that is returned.
     *              Other possible replies are given in the message help,
     *              which can also be retrieved from the message object returned
     *              on the getMessage() method.
     *
     * @exception AS400Exception If the system  returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is
     *                                            completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          system is not at the correct level.
     **/
    public void answerMessage(String reply)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        synchronized( this )
        {
            if (reply == null)
            {
                Trace.log(Trace.ERROR, "Parameter 'reply' is null.");
                throw new NullPointerException("reply");
            }

            if (!fMsgRetrieved_)
            {
                // retrieve just the message handle
                retrieveMessage(null, null);
            }

            NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
            NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
            NPSystem  npSystem = NPSystem.getSystem(getSystem());
            NPCPAttribute  cpCPFMessage = new NPCPAttribute();
            NPCPAttribute  cpMsgReply = new NPCPAttribute();

            // set up the send data stream - we set the action to
            // retrieve message and we set the code points of
            // the message handle along with what attribute code point for
            // the answer

            sendDS.setAction(NPDataStream.ANSWER_MESSAGE);
            sendDS.addCodePoint(cpMsgHandle_);

            cpMsgReply.setAttrValue(PrintObject.ATTR_MSGREPLY, reply);
            sendDS.addCodePoint(cpMsgReply);

            // setup the return datastream - we get back a cpf message maybe
            returnDS.addCodePoint(cpCPFMessage);

            int rc = npSystem.makeRequest(sendDS, returnDS);
            switch(rc)
            {
                case 0:
                   fMsgRetrieved_ = false;   // we just answered the message
                   // update the spooled file attributes
                   updateAttrs(getAttrIDsToRetrieve());
                   break;

                case NPDataStream.RET_SPLF_NO_MESSAGE:
                   // this should not be a runtime exception
                   Trace.log(Trace.ERROR, "No spooled file message waiting.");
                   throw new ErrorCompletingRequestException(
                      ErrorCompletingRequestException.SPOOLED_FILE_NO_MESSAGE_WAITING);

                default:
                   // Throw some other internal/unexpected error or something here
                   // create new RC for this
                   //throw new InternalErrorException(InternalErrorException.UNKNOWN,
                   //                                  "NPServer Error RC = " + rc);
                   Trace.log(Trace.ERROR, "Unexpected Error.");
                   throw new InternalErrorException(InternalErrorException.UNKNOWN);
            }
        }

    }   // answerMessage()



    private synchronized void buildAttrIDsToRtv()
    {
        if (!fAttrIDsToRtvBuilt_)
        {
            fAttrIDsToRtvBuilt_ = true;
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_3812SCS);       // 3812 SCS (fonts)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ACCOUNT_CODE);  // Accounting code
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AFP);           // AFP resources used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AFPRESOURCE);   // AFP Resource
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ALIGN);         // Align page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ASCIITRANS);    // ASCII transparency
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ASPDEVICE);     // Auxiliary storage pool device name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AUX_POOL);      // Auxiliary storage pool
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BARCODE);       // Barcode
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKMGN_ACR);     // Back margin across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKMGN_DWN);     // Back margin down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVL_ACR);     // Back overlay offset across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVL_DWN);     // Back overlay offset down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVRLAY);      // Back side overlay name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVRLLIB);     // Back side overlay library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHAR_ID);       // Graphic character set
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHARID);        // Character ID
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHR_RTT_CMDS);  // DBCS character rotation commands
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHRSET);        // Character set name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHRSET_LIB);    // Character set library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHRSET_SIZE);   // Character set point size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFNT);      // *Coded font name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFNTLIB);   // *Coded font library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFONT_SIZE); // Coded font point size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE);      // Code page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE_NAME); // Code page name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE_NAME_LIB); // Code page library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODFNT_ARRAY);  // Coded font array
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_COLOR);         // Color
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CONSTBCK_OVL);  // Constant back overlay
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CONTROLCHAR);   // Control character
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CONVERT_LINEDATA); // Convert line data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_COPIES);        // Copies (total)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_COPIESLEFT);    // Copies left to produce
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CORNER_STAPLE); // Corner staple
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CPI);           // Characters per inch
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CPI_CHANGES);   // Characters per inch changes
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CURPAGE);       // Current page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE);          // Date file opened (created)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE_USED);     // Date file last used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE_WTR_BEGAN_FILE); // Date writer began processing file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE_WTR_CMPL_FILE);  // Date writer finished processing file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCS_FNT);      // DBCS coded font name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCS_FNT_LIB);  // DBCS coded font library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCS_FNT_SIZE); // DBCS coded font point size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSCPI);       // DBCS characters per inch
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSDATA);      // Contains DBCS character set data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSEXTENSN);   // Process DBCS extension characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSROTATE);    // Rotate DBCS characters before printing
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSSISO);      // DBCS SI/SO space requirements
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DDS);           // DDS
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DECIMAL_FMT);   // Decimal format used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DOUBLEWIDE);    // Double wide characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DRAWERCHANGE);  // Drawer change
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DUPLEX);        // Print on both sides (duplex)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_NUMSTAPLES); // Edge stitch number of staples
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_REF); // Edge stitch reference edge
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_REFOFF); // Offset from edge stitch reference edge
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ENDPAGE);       // Ending page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EXPIRATION_DATE); // Spool file expiration date
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FIDELITY);      // Print fidelity
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FIELD_OUTLIN);  // Field outlining
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FILESEP);       // Number of file separators
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FOLDREC);       // Fold records
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONT_CHANGES);  // Font changes
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONTID);        // Font identifier
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONTRESFMT);    // Font resolution for formatting
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEF);       // Form definition name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEFLIB);    // Form definition library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMFEED);      // Form feed
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMTYPE);      // Form type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTMGN_ACR);     // Front margin across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTMGN_DWN);     // Front margin down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVL_ACR);     // Front overlay offset across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVL_DWN);     // Front overlay offset down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVRLAY);      // Front overlay name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVRLLIB);     // Front overlay library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_GRAPHICS);      // Graphics in spooled file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_GRAPHICS_TOK);  // Graphics token
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_GRPLVL_IDXTAG); // Group level index tags
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_HIGHLIGHT);     // Highlight
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_HOLD);          // Hold the spool file before written
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPDSPASSTHRU);  // IPDS pass-through
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_ATTR_CCSID); // IPP attributes-charset
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_ATTR_NL);   // IPP natural language
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_ID);    // IPP job ID
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_NAME);  // IPP job name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_NAME_NL);  // IPP job name NL
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_ORIGUSER); // IPP job original user
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_ORIGUSER_NL); // IPP job original user NL
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_PRINTER_NAME);    // IPP printer name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBCCSID);       // ccsid of the job that created file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBNAME);       // name of the job that created file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBNUMBER);     // number of the job that created file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBSYSTEM);     // System which job that created splf ran
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBUSER);       // name of the user that created file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JUSTIFY);       // Justification
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LASTPAGE);      // Last page that printed
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LINESPACING);   // Line spacing
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LPI);           // Lines per inch
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LPI_CHANGES);   // Lines per inch changes
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MAXRCDS);       // Maximum records
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MEASMETHOD);    // Measurement method (*ROWCOL or *UOM)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MULTIUP);       // logical pages per physical side
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NETWORK);       // network were output was created
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMBYTES_SPLF); // number of bytes contained w/in splf
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMRSC_LIB_ENT);// Number of user resource library list entries
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OFFICEVISION);  // OfficeVision
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OS4_CRT_AFP);   // i5/OS created AFPDS
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTPTY);        // Output priority
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTPUTBIN);     // Output bin
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUE);        // Output queue
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUELIB);     // Output queue library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OVERFLOW);      // Overflow line number
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGDFN);        // Page definition library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGDFNLIB);     // Page definition library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGE_GROUPS);   // Spooled File contains Page groups
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGE_ROTATE);   // Page rotation used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGELEN);       // page length in measurement method
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGELVLIDXTAG); // Page level index tags
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGES);         // Number of pages in spool file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGES_EST);     // Number of pages is estimated 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGEWIDTH);     // Page width in measurement method
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGRTT);        // Degree of page rotation
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PGM_OPN_FILE);  // Program that opened file name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PGM_OPN_LIB);   // Program that opened the file library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_POINTSIZE);     // the default font's point size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRINTER);       // Printer (device name)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTASSIGNED);   // Printer assigned
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTDEVTYPE);    // Printer dev type (data stream type (*SCS, *AFPDS, etc))
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTFILE);       // Printer file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTFLIB);       // Printer file library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTQUALITY);    // Print quality
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTTEXT);       // Text printed at bottom of each page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RCDFMT_DATA);   // Record format name present in data stream
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RECLENGTH);     // Record length
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_REDUCE);        // Reduce output
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RESTART);       // where to restart printing at
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RPLCHAR);       // character to replace unprintables with
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RPLUNPRT);      // replace unprintable characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSC_LIB_LIST);  // User resource library list
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSCLIB);        // Resource library 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSCNAME);       // Resource name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSCTYPE);       // Resource object type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_NUMSTAPLES);  // Saddle stitch number of staples
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_REF); // Saddle stitch reference edge
            //attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_STPL_OFFSEINFO); // saddle stitch offset distance
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE);          // whether to save after printing or not
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE_COMMAND);  // Save command
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE_DEVICE);   // Save device
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVEFILE);      // Save file name 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVEFILELIB);   // Save file library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE_LABEL);    // Save label 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE_SEQUENCE_NUMBER);  // Save sequence number
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE_VOLUME_FORMAT);    // Save volume format
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE_VOLUME_ID);// Save volume ID 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SCHEDULE);      // File available (schedule)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_AUTH_METHOD);     // Spooled file authentication method
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_CREATOR);         // Spooled file creator
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_RESTORED_DATE);   // Spooled file restored date 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_RESTORED_TIME);   // Spooled file restored time
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SAVED_DATE);      // Spooled file saved date
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SAVED_TIME);      // Spooled file saved time
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SECURITY_METHOD); // Spooled file security method
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SIZE);     // Spooled file size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SIZE_MULT);// Spooled file size multiplier
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLFNUM);       // Spooled file number
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLFSTATUS);    // Spooled file status
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPOOLFILE);     // Spooled file name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SRCDRWR);       // Source drawer
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_STARTPAGE);     // Starting page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SYSTEM);        // System where output was created
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME);          // Time spooled file was opened at
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME_WTR_BEGAN_FILE); // Time writer began processing file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME_WTR_CMPL_FILE);  // Time writer finished processing file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TRC1403);       // Trc for 1403
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_UNITOFMEAS);    // Unit of measure
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USER_DFN_TXT);  // User defined text
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERCMT);       // User comment
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDATA);      // User data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERGEN_DATA);  // System Validated data stream
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFDATA);    // User define data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFFILE);    // User define file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJ);     // *User defined object
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJLIB);  // *User defined object library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJTYP);  // *User defined object type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOPT);     // User defined options
        }
    }


    /**
     * Creates a copy of the spooled file this object represents.  The
     * new spooled file is created on the specified output queue.
     * A reference to the new spooled file is returned.
     *
     * @param outputQueue The output queue location to create the new version of the
     *       original spooled file.  The spooled file will be created to the first
     *       position on this output queue.  The output queue and this spooled
     *       file must reside on the same system.
     *
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          system is not at the correct level.
     **/
    public NPCPIDSplF copy(OutputQueueImpl outputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {

        NPDataStream sendDS   = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem     = NPSystem.getSystem(getSystem());

        NPCPAttribute cpCPFMessage = new NPCPAttribute();
        NPCPIDOutQ tgtOutQID = (NPCPIDOutQ)((OutputQueueImplRemote)outputQueue).getIDCodePoint();

        sendDS.setAction(NPDataStream.COPY);
        sendDS.addCodePoint(getIDCodePoint());
        sendDS.addCodePoint(tgtOutQID);

        NPCPIDSplF splfID = new NPCPIDSplF();
        returnDS.addCodePoint(splfID);
        returnDS.addCodePoint(cpCPFMessage);

        int rc = npSystem.makeRequest(sendDS, returnDS);
        if (rc == 0) {
            return splfID;
        }
        else {
            NPConversation conversation = npSystem.getConversation();
            String curLevel = conversation.getAttribute(PrintObject.ATTR_NPSLEVEL);
            npSystem.returnConversation(conversation);

            switch(rc) {
                    // we get back RET_INV_REQ_ACT on pre-V5R3 systems if we try
                    // to copy a spooled file, so throw a requestNotSupportedException
                    // here.
                case NPDataStream.RET_INV_REQ_ACT:
                    throw new RequestNotSupportedException(curLevel,
                                                           RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);

                    // any other error is either an unexpected error or an error
                    // completing request
                default:
                    Trace.log(Trace.ERROR, "SpooledFileImplRemote::copy - An exception was thrown attempting to " +
                                   "copy the spooled file. RC = " + rc);

                    break;
            }
            return null;
        }
    }



    /**
      * Deletes the spooled file on the server.
      *
      * @exception AS400Exception If the system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void delete()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem  npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();

        sendDS.setAction(NPDataStream.DELETE);
        sendDS.addCodePoint(getIDCodePoint());

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);
    }



    /**
      * Creates the message handle codepoint. It is
      * a synchonized method so that we don't get 2 threads creating
      * the code point at once.
      **/
    synchronized void generateMsgHandle()
    {
        if (cpMsgHandle_ == null)
        {
           cpMsgHandle_ = new NPCPMsgHandle();
        }
    }
    
   // retrieve only one attribute
   NPCPAttributeIDList getAttrIDsToRetrieve(int attrToRtv)
    {
        if (!fAttrIDsToRtvBuilt_) {
            attrsToRetrieve_.addAttrID(attrToRtv);
        }
        return attrsToRetrieve_;
    }


    NPCPAttributeIDList getAttrIDsToRetrieve()
    {
        if (!fAttrIDsToRtvBuilt_) {
            buildAttrIDsToRtv();
        }
        return attrsToRetrieve_;
    }



    // method for synchronization with base class.
    public boolean getFMsgRetrieved()
    {
        return fMsgRetrieved_;
    }



    /**
      * Returns the message that is associated with this spooled file.
      * A spooled file has a message associated with it if its
      * ATTR_SPLFSTATUS attribute returns *MESSAGE.
      *
      * @return The AS400Message object that contains the message text,
      *   type, severity, id, date, time, and default reply.
      * @exception AS400Exception If the system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public AS400Message getMessage()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        AS400Message msg = null;

        NPCPAttributeIDList cpAttrsToRetrieve = new NPCPAttributeIDList();
        NPCPAttribute  cpMessage = new NPCPAttribute();

        // set which attributes we want to retrieve
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTEXT);
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTYPE);
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGHELP);
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGREPLY);
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGID);
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGSEV);
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_DATE);
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_TIME);

        // call to synchoronized method to make the request.  This
        // serves 2 purposes:
        // 1) synchonizes access to instance variable cpMsgHandle
        // 2) synchronizes accsss to instance variable fMsgRetrieved
        retrieveMessage(cpAttrsToRetrieve, cpMessage);

        // create an AS400Message and set appropriate values
        msg = new AS400Message(cpMessage.getStringValue(PrintObject.ATTR_MSGID),
                               cpMessage.getStringValue(PrintObject.ATTR_MSGTEXT));
        msg.setDate(cpMessage.getStringValue(PrintObject.ATTR_DATE),cpMessage.getStringValue(PrintObject.ATTR_TIME));
        msg.setDefaultReply(cpMessage.getStringValue(PrintObject.ATTR_MSGREPLY));
        msg.setHelp(cpMessage.getStringValue(PrintObject.ATTR_MSGHELP));
        msg.setSeverity((cpMessage.getIntValue(PrintObject.ATTR_MSGSEV)).intValue());
        msg.setType(Integer.parseInt(cpMessage.getStringValue(PrintObject.ATTR_MSGTYPE)));

        return msg;
    } // getMessage



    /**
     * Holds the spooled file.
     * @param holdType When to hold the spooled file.
     *  May be any of the following values:
     * <UL>
     *   <LI> *IMMED - The spooled file is held immediately.
     *   <LI> *PAGEEND - The spooled file is held at the end of the current page.
     * </UL>
     *  <i>holdType</i> may be null.  If <i>holdType</i> is not specified, the default is
     * *IMMED.
     *
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         system is not at the correct level.
     **/
    public void hold(String holdType)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();

        sendDS.setAction(NPDataStream.HOLD);
        sendDS.addCodePoint(getIDCodePoint());

        if (holdType != null)
        {
            NPCPAttribute cpAttr = new NPCPAttribute();
            cpAttr.setAttrValue(PrintObject.ATTR_HOLDTYPE, holdType);
            sendDS.addCodePoint(cpAttr);
        }

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

        // update the spooled file attributes
        updateAttrs(getAttrIDsToRetrieve());
     }



    /**
     * Moves the spooled file to another output queue or to another
     * position on the same output queue.
     *
     * @param targetSpooledFile The spooled file to move this
     *       spooled file after.  The targetSpooledFile and this spooled file
     *       must reside on the same system.
     *
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         system is not at the correct level.
     **/
    public void move(SpooledFileImpl targetSpooledFile)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
            NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        // make a copy of the target spooled file ID codepoint so we can
        // change its ID
        NPCPIDSplF tgtSplfID = new NPCPIDSplF((NPCPIDSplF)((SpooledFileImplRemote)targetSpooledFile).getIDCodePoint());

        // must change the ID of the target splf ID from a SPOOLED_FILE_ID codepoint
        // to a TARGET_SPOOLED_FILE_ID codpoint
        tgtSplfID.setID(NPCodePoint.TARGET_SPOOLED_FILE_ID);
        sendDS.setAction(NPDataStream.MOVE);
        sendDS.addCodePoint(getIDCodePoint());
        sendDS.addCodePoint(tgtSplfID);

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

        // update the spooled file attributes
        updateAttrs(getAttrIDsToRetrieve());
    }



    /**
     * Moves the spooled file to another output queue.
     *
     * @param targetOutputQueue The output queue to move the
     *       spooled file to.  The spooled file will be moved to the first
     *       position on this output queue.  The output queue and this spooled
     *       file must reside on the same system.
     *
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          system is not at the correct level.
     **/
    public void move(OutputQueueImpl targetOutputQueue)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        NPCPIDOutQ tgtOutQID = (NPCPIDOutQ)((OutputQueueImplRemote)targetOutputQueue).getIDCodePoint();

        sendDS.setAction(NPDataStream.MOVE);
        sendDS.addCodePoint(getIDCodePoint());
        sendDS.addCodePoint(tgtOutQID);

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

        // update the spooled file attributes
        updateAttrs(getAttrIDsToRetrieve());
    }



    /**
     * Moves the spooled file to the first position on the output queue.
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          system is not at the correct level.
     **/
    public void moveToTop()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();

        sendDS.setAction(NPDataStream.MOVE);
        sendDS.addCodePoint(getIDCodePoint());

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

        // update the spooled file attributes
        updateAttrs(getAttrIDsToRetrieve());
    }



    /**
     * Releases a held spooled file on the server.
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          system is not at the correct level.
     **/
    public void release()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem  npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();

        sendDS.setAction(NPDataStream.RELEASE);
        sendDS.addCodePoint(getIDCodePoint());

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

        // update the spooled file attributes
        updateAttrs(getAttrIDsToRetrieve());
    }



    // synchronized method to retrieve the message for this spooled file -
    // we'll keep the message handle for answering it later
    synchronized void retrieveMessage(NPCPAttributeIDList cpAttrsToRetrieve,
                                      NPCPAttribute       cpMessage)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        if (cpMessage == null)
        {
           cpMessage = new NPCPAttribute();
        }

        generateMsgHandle();   // create message handle code point if
                               // not already created

        // set up the send data stream - we set the action to
        // retrieve message and we set the code points of
        // this spooled files ID along with what attributes we want for
        // the message
        sendDS.setAction(NPDataStream.RETRIEVE_MESSAGE);
        sendDS.addCodePoint(getIDCodePoint());

        // if we did not get some attributes then
        // generate them.
        if (cpAttrsToRetrieve == null)
            {
            NPCPAttributeIDList cpGenAttrsToRetrieve = new NPCPAttributeIDList();

            // set which attributes we want to retrieve
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTEXT);
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTYPE);
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGHELP);
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGREPLY);
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGID);
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGSEV);
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_DATE);
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_TIME);
            sendDS.addCodePoint(cpGenAttrsToRetrieve);
            }
        else
            sendDS.addCodePoint(cpAttrsToRetrieve);


        // setup the return datastream - we get back a message handle
        // code point and an attribues/attributes value code point.
        // We will get back either a CPF message or the spooled file message
        // in the cpMessage code point
        returnDS.addCodePoint(cpMessage);
        returnDS.addCodePoint(cpMsgHandle_);

        int rc = npSystem.makeRequest(sendDS, returnDS);
        switch(rc)
        {
            case 0:
                fMsgRetrieved_ = true;
                break;
            case NPDataStream.RET_SPLF_NO_MESSAGE:
                // This should throw a non-runtime exception so the caller may
                // catch it.
                Trace.log(Trace.ERROR, "No spooled file message waiting.");
                throw new ErrorCompletingRequestException(
                            ErrorCompletingRequestException.SPOOLED_FILE_NO_MESSAGE_WAITING);
            default:
                // Throw some other internal/unexpected error or something here
                // create new RC for this
                //throw new InternalErrorException(InternalErrorException.UNKNOWN,
                //                                  "NPServer Error RC = " + rc);
                Trace.log(Trace.ERROR, "Unexpected Error.");
                throw new InternalErrorException(InternalErrorException.UNKNOWN);
        }

    }  // retrieveMessage()



    /**
      * Sends the spooled file to another user on the same system or to
      * a remote system on the network.  The equivalent of the server
      * Send Network Spooled File
      * (SNDNETSPLF) command will be issued against the spooled file.
      *
      * @param sendOptions A print parameter list that contains the
      *  parameters for the send.  The following attributes MUST be set:
      * <UL>
      *   <LI> ATTR_TOUSERID  - Specifies the user ID to send the spooled file to.
      *   <LI> ATTR_TOADDRESS - Specifies the remote system to send the spooled file to.
      * </UL>
      * The following attributes are optional:
      * <UL>
      *   <LI> ATTR_DATAFORMAT - Specifies the data format in which to transmit the
      *                           spooled file.  May be either of *RCDDATA or
      *                           *ALLDATA.  *RCDDATA is the default.
      *   <LI> ATTR_VMMVSCLASS - Specifies the VM/MVS SYSOUT class for distributions
      *                          sent to a VM host system or to an MVS host system.
      *                          May be A to Z or 0 to 9.  A is the default.
      *   <LI> ATTR_SENDPTY - Specifies the queueing priority used for this spooled file
      *                        when it is being routed through a SNADS network.  May be
      *                        *NORMAL or *HIGH.  *NORMAL is the default.
      * </UL>
      *
      * @exception AS400Exception If the system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void sendNet(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        NPCPAttribute  cpSendOptions;

        // if the user has given us any options,
        //  send them
        // else
        //  create our own attribute/value codepoint to put in the type of
        //   send to do.
        // endif
        if (sendOptions != null)
        {
            cpSendOptions = sendOptions.getAttrCodePoint();
        } else {
            cpSendOptions = new NPCPAttribute();
        }
        // set the type of send we want to do here (1=SNDNETSPLF)
        cpSendOptions.setAttrValue(PrintObject.ATTR_SPLFSENDCMD, CMD_SEND_NET);

        sendDS.setAction(NPDataStream.SEND);
        sendDS.addCodePoint(getIDCodePoint());
        sendDS.addCodePoint(cpSendOptions);

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);
    }



    /**
      * Sends a spooled file to be printed on a remote system.
      * The equivalent of the server Send TCP/IP Spooled File
      * (SNDTCPSPLF) command will be issued against the spooled file.
      * This is the server version of the TCP/IP LPR command.
      *
      * @param sendOptions A print parameter list that contains the
      *  parameters for the send.  The following attributes MUST be set:
      * <UL>
      *   <LI> ATTR_RMTSYSTEM - Specifies the remote system to which the print
      *                          request will be sent.  May be a remote system
      *                          name or the special value *INTNETADR.
      *   <LI> ATTR_RMTPRTQ - Specifies the name of the destination print queue.
      * </UL>
      * The following attributes are optional:
      * <UL>
      *   <LI> ATTR_DELETESPLF - Specifies whether or not to delete the spooled file
      *                           after it has been successfully sent.  May be *NO
      *                           or *YES.   *NO is the default.
      *   <LI> ATTR_DESTOPTION - Specifies a destination-dependant option.  These options will
      *                          be sent to the remote system with the spooled file.
      *   <LI> ATTR_DESTINATION - Specifies the type of system to which the spooled file is
      *                           being sent.  When sending to other systems, this value
      *                           should be *AS/400.  May also be *OTHER or *PSF/2.
      *                           *OTHER is the default.
      *   <LI> ATTR_INTERNETADDR - Specifies the Internet address of the receiving system.
      *   <LI> ATTR_MFGTYPE  - Specifies the manufacturer, type, and model when transforming print
      *                        data from SCS or AFP to ASCII.
      *   <LI> ATTR_SCS2ASCII - Specifies whether the print data is to be transformed to
      *                         ASCII.  May be *NO or *YES.  *NO is the default.
      *   <LI> ATTR_WSCUSTMOBJ - Specifies the name of the workstation customization object.
      *   <LI> ATTR_WSCUSTMOBJL - Specifies the name of the workstation customization object library.
      *   <LI> ATTR_SEPPAGE - Specifies whether to print the separator page.  May be
      *                        *NO or *YES.  *YES is the default.
      *   <LI> ATTR_USRDTATFMLIB - Specifies the name of the user data transform library.
      *   <LI> ATTR_USRDTATFM - Specifies the name of the user data transform.
      * </UL>
      *
      * @exception AS400Exception If the system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void sendTCP(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        NPCPAttribute  cpSendOptions;

        // if the user has given us any options,
        //     send them
        // else
        //     create our own attribute/value codepoint to put in
        //     the type of send to do.
        // endif
        if (sendOptions != null)
        {
            cpSendOptions = sendOptions.getAttrCodePoint();
        } else {
            cpSendOptions = new NPCPAttribute();
        }
        // set the type of send we want to do here (2=SNDTCPSPLF)
        cpSendOptions.setAttrValue(PrintObject.ATTR_SPLFSENDCMD, CMD_SEND_TCP);

        sendDS.setAction(NPDataStream.SEND);
        sendDS.addCodePoint(getIDCodePoint());
        sendDS.addCodePoint(cpSendOptions);

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);
    }



    /**
     * Sets one or more attributes of the object.  See
     * <a href="SpooledFileAttrs.html">Spooled File Attributes</a> for
     * a list of valid attributes that can be changed.
     *
     * @param attributes A print parameter list that contains the
     *  attributes to be changed.
     *
     * @exception AS400Exception If the system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          system is not at the correct level.
     **/
    public void setAttributes(PrintParameterList attributes)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        if (attributes == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'attributes' is null.");
            throw new NullPointerException("attributes");
        }

        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        NPCPAttribute  cpNewAttrs = attributes.getAttrCodePoint();

        sendDS.setAction(NPDataStream.CHANGE_ATTRIBUTES);
        sendDS.addCodePoint(getIDCodePoint());
        sendDS.addCodePoint(cpNewAttrs);

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

        // we changed the spooled file attributes on the server,
        // merge those changed attributes into our current attributes
        // here.
        if (attrs == null)
        {
            attrs = new NPCPAttribute();
        }
        attrs.addUpdateAttributes(cpNewAttrs);
    }

} // SpooledFile class


