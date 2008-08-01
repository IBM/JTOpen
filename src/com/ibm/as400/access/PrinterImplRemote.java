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

import java.io.IOException;

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
implements PrinterImpl
{

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
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_AUTOEND);     // automatically end writer?
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
            attrsToRetrieve_.addAttrID(PrintObject.ATTR_PUBINF);      // Published Printer? 
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


    // Determines if the CommandCall.threadSafe property has been set.
    // The CommandCall "threadSafe" property provides a way for the application to force the Toolbox to call commands either on-thread or off-thread, regardless of the documented thread-safety of the command.
    // If property is set, this method returns the value of the property.
    // If property is not set, this method returns defaultVal.
    private static boolean checkThreadSafetyProperty(boolean defaultVal)
    {
      boolean result;
      String property = CommandCall.getThreadSafetyProperty();
      if (property == null) result = defaultVal;
      else                  result = property.equalsIgnoreCase("true");
      return result;
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
     * <a href="PrinterAttrs.html">Printer Attributes</a> for
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
       checkRunTimeState();

       // See if any changeable attributes were specified.
       String changes    = attributes.getStringParameter(PrintObject.ATTR_CHANGES);
       String formType   = attributes.getStringParameter(PrintObject.ATTR_FORMTYPE);
       Integer fileSep   = attributes.getIntegerParameter(PrintObject.ATTR_FILESEP);
       String outQ       = attributes.getStringParameter(PrintObject.ATTR_OUTPUT_QUEUE);
       Integer drawerSep = attributes.getIntegerParameter(PrintObject.ATTR_DRWRSEP);
       String desc       = attributes.getStringParameter(PrintObject.ATTR_DESCRIPTION);

       String printerName = getIDCodePoint().getStringValue(PrintObject.ATTR_PRINTER);

       if (desc != null)
       {
         // Call CHGDEVPRT to change the printer's Text Description attribute.  Note that this command is not designated to be threadsafe.

         desc = prepareForSingleQuotes(desc); // double any embedded single-quotes
         String cmdText = "CHGDEVPRT DEVD(" + printerName + ") TEXT('" + desc + "')";
         RemoteCommandImplRemote cmd = new RemoteCommandImplRemote();
         cmd.setSystem(getSystem());
         boolean threadSafety = checkThreadSafetyProperty(false);
         boolean result = cmd.runCommand(cmdText, threadSafety, AS400Message.MESSAGE_OPTION_UP_TO_10);
         if (!result) {
           Trace.log(Trace.ERROR, "Error when changing printer attributes.");
           throw new AS400Exception(cmd.getMessageList());
         }
       }

       if (changes   != null ||
           formType  != null ||
           fileSep   != null ||
           outQ      != null ||
           drawerSep != null)
       {
         // Call CHGWTR change the specified printer attributes.  Note that this command is not designated to be threadsafe.

         StringBuffer cmdBuf = new StringBuffer("CHGWTR WTR(" + printerName + ")");

         if (changes != null) {  // When to change writer.
           cmdBuf.append(" OPTION(" + changes + ")");
         }

         if (formType != null) {  // Form type options.
           cmdBuf.append(" FORMTYPE(" + formType + ")");
         }

         if (fileSep != null) {  // File separators.
           int intVal = fileSep.intValue();
           String stringVal = null;
           switch (intVal)
           {
             case -1: stringVal = "*FILE"; break;
             default: stringVal = fileSep.toString();
           }
           cmdBuf.append(" FILESEP(" + stringVal + ")");
         }

         if (outQ != null) {  // Output queue.
           // Note that this parameter comes to us as a fully qualified IFS pathname.
           QSYSObjectPathName path = new QSYSObjectPathName(outQ);
           cmdBuf.append(" OUTQ(" + path.getLibraryName() + "/" + path.getObjectName() + ")");
         }

         if (drawerSep != null) {  // Drawer for separators.
           int intVal = drawerSep.intValue();
           String stringVal = null;
           switch (intVal)
           {
             case -1: stringVal = "*FILE"; break;
             case -2: stringVal = "*DEVD"; break;
             default: stringVal = drawerSep.toString();
           }
           cmdBuf.append(" SEPDRAWER(" + stringVal + ")");
         }

         RemoteCommandImplRemote cmd = new RemoteCommandImplRemote();
         cmd.setSystem(getSystem());
         boolean threadSafety = checkThreadSafetyProperty(false);
         boolean result = cmd.runCommand(cmdBuf.toString(), threadSafety, AS400Message.MESSAGE_OPTION_UP_TO_10);
         if (!result) {
           Trace.log(Trace.ERROR, "Error when changing printer attributes.");
           throw new AS400Exception(cmd.getMessageList());
         }

       }

       NPCPAttribute  cpNewAttrs = attributes.getAttrCodePoint();

       // we changed the printer file attributes on the server,
       // merge those changed attributes into our current attributes
       // here.
       if (attrs == null) {
         attrs = new NPCPAttribute();
       }

       attrs.addUpdateAttributes(cpNewAttrs);
     }


     /**
      Prepares the text to be enclosed in single-quotes; for example, for use in the TEXT parameter of CHGDEVPRT.
      1. Collapse all sequences of single-quotes to a single single-quote.
      2. Strip outer single-quotes (if present).
      3. Double-up any embedded single-quotes (that aren't already doubled).
      **/
     private static final String prepareForSingleQuotes(String text)
     {
       if (text.indexOf('\'') == -1) return text;  // text contains no single-quotes

       // 1. Collapse each sequence of multiple single-quotes to a single single-quote.
       StringBuffer buf = new StringBuffer(text.trim());
       if (buf.indexOf("''") != -1)
       {
         boolean followedByQuote = false;
         for (int i=buf.length()-1; i >= 0; i--)  // examine char-by-char, from end
         {
           char thisChar = buf.charAt(i);
           if (thisChar == '\'')
           {
             if (followedByQuote)
             {
               buf.deleteCharAt(i);  // collapse sequence of quotes
               continue;
             }
             else followedByQuote = true;
           }
           else followedByQuote = false;
         }
       }

       // 2. Strip outer single-quotes (if present).
       if (buf.charAt(0) == '\'' &&
           buf.charAt(buf.length()-1) == '\'')
       {
         buf.deleteCharAt(buf.length()-1);
         buf.deleteCharAt(0);
       }

       // 3. Double-up all embedded single-quotes.
       for (int i=buf.length()-1; i >= 0; i--)  // examine char-by-char
       {
         char thisChar = buf.charAt(i);
         if (thisChar == '\'')
         {
           buf.insert(i, '\'');  // double the single-quote
         }
       }
       return buf.toString();
     }

}
