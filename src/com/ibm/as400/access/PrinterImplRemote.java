///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PrinterImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * The  Printer class represents an AS/400 printer.
 * An instance of this class can be used to manipulate an individual
 * AS/400 printer.
 *
 * See <a href="PrinterAttrs.html">Printer Attributes</a> for
 * valid attributes.
 *
 **/

class PrinterImplRemote extends PrintObjectImplRemote
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String x = Copyright.copyright;     // @A4C - Copyright change
    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;
    private static final String NAME = "name";


    private synchronized void buildAttrIDsToRtv()
    {
        if (!fAttrIDsToRtvBuilt_)
        {
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AFP);         // advanced function printing
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ALIGNFORMS);  // align forms @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ALWDRTPRT);   // allow direct printing @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BTWNCPYSTS);  // between copies status @A2A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BTWNFILESTS); // between files status @A2A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHAR_ID);     // set of graphic characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHANGES);     // changes take effect @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE);    // code page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DESCRIPTION); // text description
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVCLASS);    // device class
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVMODEL);    // device model
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVSTATUS);   // device status @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVTYPE);     // device type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DRWRSEP);     // drawer for separators
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ENDPNDSTS);   // end pending status @A2A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FILESEP);     // number of file separators @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONTID);      // Font identifier to use
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEFLIB);  // Form definition library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEF);     // Form definition name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMFEED);    // type of paperfeed to be use
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMTYPE);    // name of the form to be used @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMTYPEMSG); // form type message option @A1A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_HELDSTS);     // held status @A2A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_HOLDPNDSTS);  // hold pending status @A2A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBUSER);     // name of the user that created file @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MFGTYPE);     // manufacturer's type & model
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MSGQUELIB);   // message queue library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MSGQUE);      // message queue name
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_ONJOBQSTS);   // on job queue status @A2A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUELIB);   // output queue library @A1A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUE);      // output queue @A1A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQSTS);     // output queue status @A3A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OVERALLSTS);  // printer overall status @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_POINTSIZE);   // the default font's point size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRINTER);     // printer
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTDEVTYPE);  // printer dev type @A1A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPOOLFILE);   // spool file name @A1A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLFNUM);     // spool file number @A1A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_STARTEDBY);   // started by @A1A
    	    attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDATA);    // user data @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDRV);     // User driver program name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJ);   // User defined object
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJLIB);// User defined object library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJTYP);// User defined object type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOPT);   // user defined options
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDRVLIB);   // User driver program library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRTFM);      // User transform program name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRTFMLIB);   // User transform program library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SCS2ASCII);   // transform SCS to ASCII
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WRTNGSTS);    // writing status @A2A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTNGDATASTS); // waiting for data status @A2A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTNGDEVSTS);  // waiting for device status @A2A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTNGMSGSTS);  // waiting for message status @A2A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNAME);  // writer job name @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNUM);   // writer job number @A3A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBSTS);   // writer job status @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBUSER);  // writer job user name @A3A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRAUTOEND);  // when to automatically end writer @A1A
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRSTRTD);    // writer started @A2A
            fAttrIDsToRtvBuilt_ = true;
        }
    }



    // Check the run time state
    void checkRunTimeState()
    {
        // check whatever the base class needs to check
        super.checkRunTimeState();

        // Printer's need to additionally check the name.
        if( getIDCodePoint() == null )
        {
            Trace.log(Trace.ERROR, "Parameter 'name' has not been set.");
            throw new ExtendedIllegalStateException(
              NAME, ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }
    }



    // This method implements an abstract method of the superclass
    NPCPAttributeIDList getAttrIDsToRetrieve()
    {
        if (!fAttrIDsToRtvBuilt_) {
            buildAttrIDsToRtv();
        }
        return attrsToRetrieve_;
    }



    /**
     * Copyright.
     **/
    static private String getCopyright()
    {
	    return Copyright.copyright;
    }

} // end Printer class
