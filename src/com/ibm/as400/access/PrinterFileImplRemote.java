///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: PrinterFileImplRemote.java
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
 * The PrinterFile class represents an iSeries system printer file.
 * An instance of this class can be used to manipulate an individual
 * iSeries system printer file.
 *
 * See <a href="PrinterFileAttrs.html">Printer File Attributes</a> for
 * valid attributes.
 *
 **/

class PrinterFileImplRemote extends PrintObjectImplRemote
implements PrinterFileImpl
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;


    private synchronized void buildAttrIDsToRtv()
    {
        if (!fAttrIDsToRtvBuilt_)
        {
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ALIGN);       // align page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKMGN_ACR);   // back margin across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKMGN_DWN);   // back margin down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVRLLIB);   // back side overlay library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVRLAY);    // back side overlay name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVL_DWN);   // back overlay offset down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BKOVL_ACR);   // back overlay offset across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHAR_ID);     // set of graphic characters f
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CPI);         // characters per inch
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFNTLIB); // coded font library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE);    // code page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEDFNT);    // coded font
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CONTROLCHAR); // control character
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CONVERT_LINEDATA); // convert line data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_COPIES);      // copies (total)
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CORNER_STAPLE); // corner staple
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DAYS_UNTIL_EXPIRE); // days until file expires
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSDATA);    // contains DBCS character set
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSEXTENSN); // process DBCS extension char
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSROTATE);  // rotate DBCS characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSCPI);     // DBCS CPI
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DBCSSISO);    // DBCS SI/SO positioning
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DESCRIPTION); // text description
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DFR_WRITE);   // defer write
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DUPLEX);      // print on both sides of pape
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_NUMSTAPLES); // edgestich number of staples
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_STPL_OFFSET_INFO);// edgestitch info offset
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_REF);    // edgestitch reference
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EDGESTITCH_REFOFF); // edgestitch reference offset
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ENDPAGE);     // ending page number to print
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_EXPIRATION_DATE); // Spool file expiration date
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FIDELITY);    // the error handling when pri
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FILESEP);     // number of file separators
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FOLDREC);     // wrap text to next line
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONTID);      // Font identifier to use (def
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEFLIB);  // Form definition library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEF);     // Form definition name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMFEED);    // type of paperfeed to be use
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMTYPE);    // name of the form to be used
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTMGN_ACR);   // front margin across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTMGN_DWN);   // front margin down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVRLLIB);   // front side overlay library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVRLAY);    // front side overlay name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVL_ACR);   // front overlay offset across
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FTOVL_DWN);   // front overlay offset down
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_HOLD);        // hold the spool file
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JUSTIFY);     // hardware justification
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_LPI);         // lines per inch
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MAXRCDS);     // *maximum number of records
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MEASMETHOD);  // measurement method (*ROWCOL
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MULTIUP);     // logical pages per physical
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTPTY);      // output priority
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUELIB);   // output queue library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUE);      // output queue
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OVERFLOW);    // overflow line number
         //   attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGE_DEFINITION);  page definition
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGDFNLIB);   // page definition library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGDFN);      // page definition
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGRTT);      // degree of page rotation
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGELEN);     // page length in Units of Mea
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PAGEWIDTH);   // width of page in Units of M
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_POINTSIZE);   // the default font's point si
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRINTER);     // printer device name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTQUALITY);  // print quality
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTTEXT);     // text printed at bottom of e
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTDEVTYPE);  // printer dev type (data stre
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RPLUNPRT);    // replace unprintable charact
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RPLCHAR);     // character to replace unprin
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_NUMSTAPLES); // saddle stitch number of staple
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_REF); // saddle stitch reference
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SADDLESTITCH_STPL_OFFSEINFO);// saddle stitch offset
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SAVE);        // whether to save after print
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SRCDRWR);     // source drawer
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPOOL);       // spool the data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SCHEDULE);    // when available to the write
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_STARTPAGE);   // starting page to print
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_UNITOFMEAS);  // unit of measure
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDATA);    // user data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFDATA);  // User defined data
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOPT);   // User defined options
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJ);   // User defined object
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJLIB);// User defined object library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJTYP);// User defined object type
            fAttrIDsToRtvBuilt_ = true;
        }
    }



    private static NPCPIDPrinterFile buildIDCodePoint(String IFSPrinterFileName)
    {
            QSYSObjectPathName ifsPath = new QSYSObjectPathName(IFSPrinterFileName, "FILE");

            return new NPCPIDPrinterFile(ifsPath.getObjectName(), ifsPath.getLibraryName());
    }



    // This method implements an abstract method of the superclass
    NPCPAttributeIDList getAttrIDsToRetrieve()
    {
        if (!fAttrIDsToRtvBuilt_) {
            buildAttrIDsToRtv();
        }
        return attrsToRetrieve_;
    }

    NPCPAttributeIDList getAttrIDsToRetrieve(int attrToRtv)
    {
      if (!fAttrIDsToRtvBuilt_)
      {
        attrsToRetrieve_.addAttrID(attrToRtv);
      }
      return attrsToRetrieve_;
    }


    /**
     * Sets one or more attributes of the object.  See
     * <a href="PrinterFileAttrs.html">Printer File Attributes</a> for
     * a list of valid attributes that can be changed.
     *
     * @param attributes A print parameter list that contains the
     *  attributes to be changed.
     *
     * @exception AS400Exception If the server returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     **/
     public void setAttributes(PrintParameterList attributes)
      throws AS400Exception,
             AS400SecurityException,
             ErrorCompletingRequestException,
             IOException,
             InterruptedException
    {
            NPDataStream sendDS = new NPDataStream(NPConstants.PRINTER_FILE);       // @B1C
            NPDataStream returnDS = new NPDataStream(NPConstants.PRINTER_FILE);     // @B1C
        NPSystem  npSystem = NPSystem.getSystem(getSystem());

            NPCPAttribute  cpCPFMessage = new NPCPAttribute();
            NPCPAttribute  cpNewAttrs = attributes.getAttrCodePoint();

            sendDS.setAction(NPDataStream.CHANGE_ATTRIBUTES);
        sendDS.addCodePoint(getIDCodePoint());
            sendDS.addCodePoint(cpNewAttrs);

            returnDS.addCodePoint(cpCPFMessage);

            npSystem.makeRequest(sendDS, returnDS);

            // we changed the printer file attributes on the host,
            // merge those changed attributes into our current attributes
            // here.
            if (attrs == null) {
                attrs = new NPCPAttribute();
            }

            attrs.addUpdateAttributes(cpNewAttrs);
    }

}
