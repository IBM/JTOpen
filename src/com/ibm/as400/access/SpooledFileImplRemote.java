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
 * The SpooledFile class represents an AS/400 spooled file.
 *  You can use an instance of this class to manipulate an individual
 *  AS/400 spooled file (hold, release, delete, send, read, and so on).
 * To create new spooled files on the AS/400, use the
 * SpooledFileOutputStream class.
 *
 * See <a href="SpooledFileAttrs.html">Spooled File Attributes</a> for
 * valid attributes.
 *
 * @see PrintObjectInputStream
 * @see PrintObjectPageInputStream
 * @see PrintObjectTransformedInputStream
 **/

/* @A2C - Changed to implement java.io.Serializable */
class SpooledFileImplRemote extends PrintObjectImplRemote
implements SpooledFileImpl
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;
    private static final int CMD_SEND_NET = 1;
    private static final int CMD_SEND_TCP = 2;

    transient NPCPMsgHandle cpMsgHandle_ = null;    /* @A2C - Changed to transient */
    transient boolean       fMsgRetrieved_ = false; /* @A2C - Changed to transient */

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
     * @exception AS400Exception If the AS/400 system  returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is
     *                                            completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
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

            NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);       // @B1C
            NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);     // @B1C
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
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_3812SCS);       // 3812 SCS @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ACCOUNT_CODE);  // accounting code @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AFP);           // AFP resources used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AFPRESOURCE);   // AFP Resource     @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ALIGN);         // align forms before printing
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ASCIITRANS);    // ASCII transparency  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ASPDEVICE);     // ASP Device   @D1A R530 attribute
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AUX_POOL);      // Auxiliary storage pool @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BARCODE);       // Barcode  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKMGN_ACR);     // back margin across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKMGN_DWN);     // back margin down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVL_ACR);     // back overlay offset across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVL_DWN);     // back overlay offset down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVRLAY);      // *back side overlay name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVRLLIB);     // *back side overlay library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHAR_ID);       // set of graphic characters for this file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHRSET_SIZE);   // character set point size  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE);      // code page @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFNT);      // coded font
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFNTLIB);   // coded font library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE_NAME_LIB);  // Code page library name  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE_NAME); // Code page name @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFONT_SIZE); // Coded font point size @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CONSTBCK_OVL);  // Constant back overlay  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHARID);        // Character ID  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHRSET_LIB);   // character set library name @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHR_RTT_CMDS); // DBCS character rotation commands @D3A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHRSET);       // character set name @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_COLOR);         // Color   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODFNT_ARRAY);  // coded font array @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CONTROLCHAR);   // control char @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_COPIES);        // copies (total)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_COPIESLEFT);    // copies left to produce
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CORNER_STAPLE); // corner staple @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CPI);           // characters per inch
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CURPAGE);       // current page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CPI_CHANGES);   // Characters per inch changes  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE);          // date file was opened
 //           attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE_END);    date spooled file creation ended @C5A@D3D
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE_WTR_BEGAN_FILE); // date writer began processing file
                                                                        //               @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE_WTR_CMPL_FILE); // date writer finished processing file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DATE_USED);    // Date file last used  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSCPI);       // DBCS CPI
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSDATA);      // contains DBCS character set data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSEXTENSN);   // process DBCS extension characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCS_FNT_LIB);  // DBCS-coded font library name  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCS_FNT);      // DBCS-coded font name      @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCS_FNT_SIZE); // DBCS-coded font point size @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSROTATE);    // rotate DBCS characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSSISO);      // DBCS SI/SO positioning
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DECIMAL_FMT);   // Decimal format  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DDS);           // DDS  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DOUBLEWIDE);    // Double-wide characters  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DRAWERCHANGE);  // Drawer change   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DUPLEX);        // print on both sides of paper
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ENDPAGE);       // ending page number to print
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_NUMSTAPLES); // edge stitch number of staples @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_REF); // edge stitch reference  @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_REFOFF); // edge stitch reference offset @C1A
       //     attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_STPL_OFFSET_INFO); edgestitch info offset @A6D
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FIDELITY);      // the error handling when printing
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FIELD_OUTLIN);  // Field outlining  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FILESEP);       // number of file separators
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FOLDREC);       // wrap text to next line
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONT_CHANGES);  // Font changes   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONTID);        // Font identifier to use (default)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONTRESFMT);    // Font resolution for formatting  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEF);       // *Form definition name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEFLIB);    // *Form definition library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMFEED);      // type of paperfeed to be used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMTYPE);      // name of the form to be used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTMGN_ACR);     // front margin across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTMGN_DWN);     // front margin down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVL_ACR);     // front overlay offset across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVL_DWN);     // front overlay offset down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVRLAY);      // *front side overlay name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVRLLIB);     // *front side overlay library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_GRAPHICS);        // Graphic   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_GRAPHICS_TOK);    // Grapics token  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_GRPLVL_IDXTAG);// Group level index tags   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_HIGHLIGHT);       // Highlight   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_HOLD);          // hold the spool file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPDSPASSTHRU);  // IPDS pass-through  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_ATTR_CCSID); // IPP attributes-charset  @C1AC4C
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_ID);       // IPP job ID              @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_NAME);     // IPP job name            @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_NAME_NL);  // IPP job name NL         @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_ORIGUSER); // IPP job original user   @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_JOB_ORIGUSER_NL); // IPP job original user NL  @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_PRINTER_NAME);   // IPP printer name        @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IPP_ATTR_NL);   // IPP natural language         @C4A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBNAME);       // name of the job that created file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBNUMBER);     // number of the job that created file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBSYSTEM);     // System which job that created splf ran @C5A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBUSER);       // name of the user that created file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JUSTIFY);       // hardware justification
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LASTPAGE);      // last page that printed
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LINESPACING);   // line spacing @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LPI);           // lines per inch
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LPI_CHANGES);   // LPI changes  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MAXRCDS);       // *maximum number of records allowed
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MEASMETHOD);    // measurement method (*ROWCOL or *UOM)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MFGTYPE);       // manufacturer type and model  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MULTIUP);       // logical pages per physical side
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NETWORK);       // network were output was created @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMBYTES);      // number of bytes to read/write
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMBYTES_SPLF); // number of bytes contained w/in splf @C5A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_NUMRSC_LIB_ENT);//Number of user resource library list entries @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OFFICEVISION);  // OfficeVision  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OS4_CRT_AFP);   // OS/400-created AFPDS @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTPTY);        // output priority
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTPUTBIN);     // output bin @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUE);        // *output queue
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUELIB);     // *output queue library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OVERFLOW);      // overflow line number
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGDFN);        // *Page definition library @C5A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGDFNLIB);     // *Page definition library name @C5A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGE_GROUPS);   // Page groups  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGELEN);       // page length in measurement method
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGELVLIDXTAG); // Page level index tags   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGE_ROTATE);   // Page rotate  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGES);         // number of pages in spool file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGEWIDTH);     // page width in measurement method
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGRTT);        // degree of page rotation
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PGM_OPN_LIB);   // Program that opened the file library name @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PGM_OPN_FILE);  // Program that opened file name  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_POINTSIZE);     // the default font's point size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTASSIGNED);   // printer assigned @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTDEVTYPE);    // printer dev type (data stream type (*SCS, *AFPDS, etc))
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTFILE);       // *printer file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTFLIB);       // *printer file library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRINTER);       // printer @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTQUALITY);    // print quality
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTTEXT);       // text printed at bottom of each page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RCDFMT_DATA);   // Record format   @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RECLENGTH);     // record length
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_REDUCE);        // reduce output @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RESTART);       // where to restart printing at
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RPLCHAR);       // character to replace unprintables with
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RPLUNPRT);      // replace unprintable characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_NUMSTAPLES);  // saddle stitch number of staples  @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_REF); // saddle stitch reference @C1A
                //      attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_STPL_OFFSEINFO);  sad stich info offset @A6D
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE);          // whether to save after printing or not
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SCHEDULE);      // when available to the writer
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SEPPAGE);       // allow separator page to be printed @C3A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_AUTH_METHOD); // spool file authentication method @C3A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SECURITY_METHOD); // spool file security method   @C3A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLFNUM);       // spool file number
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLFSTATUS);    // spool file status
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPOOLFILE);     // spool file name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_CREATOR);  // spool file creator @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SIZE);     // spool file size  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLF_SIZE_MULT);// Spooled file size multiplier
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SRCDRWR);       // source drawer
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_STARTPAGE);     // starting page to print
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SYSTEM);        // system where output was created @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SYS_DRV_PGM);   // system driver program  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME);          // time spooled file was opened at
    //        attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME_END);     ending time which spooled file was created @C5A@D3D
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME_WTR_BEGAN_FILE); // time writer began processing file
                                                                        //               @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TIME_WTR_CMPL_FILE); // time writer finished processing file
                                                            //               @C1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_TRC1403);       // Trc for 1403  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_UNITOFMEAS);    // unit of measure
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERCMT);       // user comment
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDATA);      // user data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFDATA);    // user define data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFFILE);    // user define file @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJ);     // *User defined object
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJLIB);  // *User defined object library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJTYP);  // *User defined object type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOPT);     // user defined options
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERGEN_DATA);  // User who created file  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USER_DFN_TXT);  // User-defined text  @D1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RSC_LIB_LIST);  // User resource library list @D1A
        }
    }



    // @C6A - (added method)
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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
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
      * Deletes the spooled file on the AS/400.
      *
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void delete()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);   // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE); // @B1C
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
// @D2A  retrieve only one attribute
   NPCPAttributeIDList getAttrIDsToRetrieve(int attrToRtv)
    {
        String x = Copyright.copyright;
        if (!fAttrIDsToRtvBuilt_) {
            attrsToRetrieve_.addAttrID(attrToRtv);
        }
        return attrsToRetrieve_;
    }

//@D2A


    NPCPAttributeIDList getAttrIDsToRetrieve()
    {
        if (!fAttrIDsToRtvBuilt_) {
            buildAttrIDsToRtv();
        }
        return attrsToRetrieve_;
    }



    // @A1A - Added method for synchronization with base class.
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
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
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
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTEXT);  // @A3A -added PrintObject.
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTYPE);  // @A3A -added PrintObject.
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGHELP);  // @A3A -added PrintObject.
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGREPLY); // @A3A -added PrintObject.
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGID);    // @A3A -added PrintObject.
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGSEV);   // @A3A -added PrintObject.
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_DATE);     // @A3A -added PrintObject.
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_TIME);     // @A3A -added PrintObject.

        // call to synchoronized method to make the request.  This
        // serves 2 purposes:
        // 1) synchonizes access to instance variable cpMsgHandle
        // 2) synchronizes accsss to instance variable fMsgRetrieved
        retrieveMessage(cpAttrsToRetrieve, cpMessage);

        // create an AS400 Message and set appropriate values
        msg = new AS400Message(cpMessage.getStringValue(PrintObject.ATTR_MSGID),    // @A3A -added PrintObject.
                               cpMessage.getStringValue(PrintObject.ATTR_MSGTEXT)); // @A3A -added PrintObject.
        msg.setDate(cpMessage.getStringValue(PrintObject.ATTR_DATE),cpMessage.getStringValue(PrintObject.ATTR_TIME)); // @A3A -added PrintObject.
        msg.setDefaultReply(cpMessage.getStringValue(PrintObject.ATTR_MSGREPLY));   // @A3A -added PrintObject.
        msg.setHelp(cpMessage.getStringValue(PrintObject.ATTR_MSGHELP));            // @A3A -added PrintObject.
        msg.setSeverity((cpMessage.getIntValue(PrintObject.ATTR_MSGSEV)).intValue());   // @A3A -added PrintObject.
        msg.setType(Integer.parseInt(cpMessage.getStringValue(PrintObject.ATTR_MSGTYPE))); // @C2A -added

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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public void hold(String holdType)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);       // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);     // @B1C
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();

        sendDS.setAction(NPDataStream.HOLD);
        sendDS.addCodePoint(getIDCodePoint());

        if (holdType != null)
        {
            NPCPAttribute cpAttr = new NPCPAttribute();
            cpAttr.setAttrValue(PrintObject.ATTR_HOLDTYPE, holdType);   // @A3A -added PrintObject.
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
     *       must reside on the same AS/400.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                         AS/400 system is not at the correct level.
     **/
    public void move(SpooledFileImpl targetSpooledFile) // @A4C
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);       // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);     // @B1C
            NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        // make a copy of the target spooled file ID codepoint so we can
        // change its ID
        NPCPIDSplF tgtSplfID = new NPCPIDSplF((NPCPIDSplF)((SpooledFileImplRemote)targetSpooledFile).getIDCodePoint()); // @A4C

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
     *       file must reside on the same AS/400.
     *
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void move(OutputQueueImpl targetOutputQueue)  // @A4C
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);       // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);     // @B1C
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        NPCPIDOutQ tgtOutQID = (NPCPIDOutQ)((OutputQueueImplRemote)targetOutputQueue).getIDCodePoint();  // @A4C

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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void moveToTop()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);       // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);     // @B1C
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
     * Releases a held spooled file on the AS/400.
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
     **/
    public void release()
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException,
             RequestNotSupportedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);       // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);     // @B1C
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
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);   // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE); // @B1C
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
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTEXT);   // @A3A -added PrintObject.
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGTYPE);   // @A3A -added PrintObject.
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGHELP);   // @A3A -added PrintObject.
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGREPLY);  // @A3A -added PrintObject.
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGID);     // @A3A -added PrintObject.
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_MSGSEV);    // @A3A -added PrintObject.
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_DATE);      // @A3A -added PrintObject.
            cpGenAttrsToRetrieve.addAttrID(PrintObject.ATTR_TIME);      // @A3A -added PrintObject.
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
      * a remote system on the network.  The equivalent of the AS/400
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
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void sendNet(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);       // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE);     // @B1C
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
      * The equivalent of the AS/400 Send TCP/IP Spooled File
      * (SNDTCPSPLF) command will be issued against the spooled file.
      * This is the AS/400 version of the TCP/IP LPR command.
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
      *                           being sent.  When sending to other AS/400 systems, this value
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
      * @exception AS400Exception If the AS/400 system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the AS/400.
      * @exception InterruptedException If this thread is interrupted.
      **/
    public void sendTCP(PrintParameterList sendOptions)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);   // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE); // @B1C
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
        cpSendOptions.setAttrValue(PrintObject.ATTR_SPLFSENDCMD, CMD_SEND_TCP);  // @A3A -added PrintObject.

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
     * @exception AS400Exception If the AS/400 system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the AS/400
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the
     *                                          AS/400 system is not at the correct level.
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

        NPDataStream sendDS = new NPDataStream(NPConstants.SPOOLED_FILE);   // @B1C
        NPDataStream returnDS = new NPDataStream(NPConstants.SPOOLED_FILE); // @B1C
        NPSystem npSystem = NPSystem.getSystem(getSystem());

        NPCPAttribute  cpCPFMessage = new NPCPAttribute();
        NPCPAttribute  cpNewAttrs = attributes.getAttrCodePoint();

        sendDS.setAction(NPDataStream.CHANGE_ATTRIBUTES);
        sendDS.addCodePoint(getIDCodePoint());
        sendDS.addCodePoint(cpNewAttrs);

        returnDS.addCodePoint(cpCPFMessage);

        npSystem.makeRequest(sendDS, returnDS);

        // we changed the spooled file attributes on the host,
        // merge those changed attributes into our current attributes
        // here.
        if (attrs == null)
        {
            attrs = new NPCPAttribute();
        }
        attrs.addUpdateAttributes(cpNewAttrs);
    }

} // SpooledFile class


