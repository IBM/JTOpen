///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
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
 * The Printer class represents a server printer.
 * An instance of this class can be used to manipulate an individual
 * printer.
 *
 * See <a href="PrinterAttrs.html">Printer Attributes</a> for
 * valid attributes.
 *
 **/

class PrinterImplRemote extends PrintObjectImplRemote
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static final NPCPAttributeIDList attrsToRetrieve_  = new NPCPAttributeIDList();
    private static boolean fAttrIDsToRtvBuilt_ = false;
    private static final String NAME = "name";


    private synchronized void buildAttrIDsToRtv()
    {
        if (!fAttrIDsToRtvBuilt_)
        {
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AFP);         // advanced function printing
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ALIGNFORMS);  // align forms 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ALWDRTPRT);   // allow direct printing 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BTWNCPYSTS);  // between copies status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_BTWNFILESTS); // between files status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHAR_ID);     // set of graphic characters
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CHANGES);     // changes take effect 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_CODEPAGE);    // code page
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DESCRIPTION); // text description
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVCLASS);    // device class
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVMODEL);    // device model
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVSTATUS);   // device status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DEVTYPE);     // device type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_DRWRSEP);     // drawer for separators
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ENDPNDSTS);   // end pending status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FILESEP);     // number of file separators 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FONTID);      // Font identifier to use
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEFLIB);  // Form definition library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMDEF);     // Form definition name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMFEED);    // type of paperfeed to be use
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMTYPE);    // name of the form to be used 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_FORMTYPEMSG); // form type message option 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_HELDSTS);     // held status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_HOLDPNDSTS);  // hold pending status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_IMGCFG);      // Image configuration 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_JOBUSER);     // name of the user that created file 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MFGTYPE);     // manufacturer's type & model
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MSGQUELIB);   // message queue library name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_MSGQUE);      // message queue name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_ONJOBQSTS);   // on job queue status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUELIB);   // output queue library 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQUE);      // output queue 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OUTQSTS);     // output queue status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_OVERALLSTS);  // printer overall status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_POINTSIZE);   // the default font's point size
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRINTER);     // printer
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PRTDEVTYPE);  // printer dev type 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PUBINF_COLOR_SUP);// Color supported indicator 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PUBINF_DS); // Data Stream supported    
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PUBINF_PPM_COLOR);// Pages per minute (color printing) 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PUBINF_PPM); // Papers per minute (monochrome printing) 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PUBINF_DUPLEX_SUP);// Duplex supported indicator 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PUBINF_LOCATION);// Published location description 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_RMTLOCNAME); // remote loc of the printer device 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPOOLFILE);   // spool file name 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SPLFNUM);     // spool file number 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_STARTEDBY);   // started by 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SYS_DRV_PGM); // System driver program 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDATA);    // user data 
            //attrsToRetrieve_.addAttrID(PrintObject.ATTR_USERDRV);     // User driver program name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USER_DRIVER_PROG); // User driver program name 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJ);   // User defined object
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJLIB);// User defined object library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOBJTYP);// User defined object type
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDEFOPT);   // user defined options
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRDRVLIB);   // User driver program library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRTFM);      // User transform program name
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_USRTFMLIB);   // User transform program library
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_SCS2ASCII);   // transform SCS to ASCII
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WRTNGSTS);    // writing status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTNGDATASTS); // waiting for data status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTNGDEVSTS);  // waiting for device status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTNGMSGSTS);  // waiting for message status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNAME);  // writer job name 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBNUM);   // writer job number 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBSTS);   // writer job status 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRJOBUSER);  // writer job user name 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRAUTOEND);  // when to automatically end writer 
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_WTRSTRTD);    // writer started 
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


    NPCPAttributeIDList getAttrIDsToRetrieve(int attrToRtv)
    {
      if (!fAttrIDsToRtvBuilt_)
      {
        attrsToRetrieve_.addAttrID(attrToRtv);
      }
      return attrsToRetrieve_;
    }
}
