///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterReleaseAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Printer;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.PrintObject;

/**
The PrinterReleaseAction class represents the releasing of a printer.
The actual affect is to release the writer that is associated with the
printer**/

class PrinterReleaseAction
extends AbstractVAction
{
  static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_RELEASE");
    private Printer printer_                            = null; // the printer

/**
Constructs a PrinterReleaseAction object.

@param  object      The object.
@param  printer     The printer.
**/
    public PrinterReleaseAction (VObject object, Printer printer)
    {
        super (object);
        printer_ = printer;
    }


/**
Returns the display name for the action.

@return The display name.
**/
    public String getText ()
    {
        return displayName_;
    }

/**
Performs the action.

@param  context The action context.
**/
    public void perform (VActionContext context)
    {
        try
        {
            // fire started working event
            fireStartWorking();

            // We need to get the status of the writer associated with this printer.
            String status_ = printer_.getStringAttribute(PrintObject.ATTR_WTRJOBNAME).trim();

            // If the writer name is null then there is no writer and we
            // shouldn't even be here.
            if((status_ == null) || (status_.equals("")))
            {
                // Trace the error
                if (Trace.isTraceOn())
                    Trace.log (Trace.ERROR, "ERROR No writer for [" + printer_.getName () + "].");

                Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));    //@A1A
                fireError(e);               //@A1A
            }
            else
            {
                // The writer exists so issue the call
                CommandCall cmd = new CommandCall( printer_.getSystem());
                String cmdString = "RLSWTR WTR("+ printer_.getName() + ")";
                try
                {
                    if (cmd.run(cmdString)!=true)
                    {
                        // Note that there was an error
                        if (Trace.isTraceOn())
                            Trace.log (Trace.ERROR, "ERROR RLSWTR cmd for [" + printer_.getName () + "].");

                        // fire an error event
                        Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));
                        fireError(e);
                    }
                    else                                                    //@A1A
                    {
                        // Everything worked great
                        // trace the release
                        if (Trace.isTraceOn())                              //@A1M
                            Trace.log (Trace.INFORMATION, "Released printer [" + printer_.getName () + "].");//@A1M

                        fireObjectChanged ();                        //@A1M
                    }

                    // Trace the messages (returned whether or not there was an error)
                    if (Trace.isTraceOn())
                    {
                        AS400Message[] messagelist = cmd.getMessageList();
                        for (int i=0; i < messagelist.length; i++)
                        {
                            // show each message
                            Trace.log (Trace.INFORMATION, messagelist[i].getText());
                        }
                    }
                }
                catch (Exception e)
                {
                    if (Trace.isTraceOn())
                        Trace.log (Trace.ERROR, "ERROR CommandCall exception for [" + printer_.getName () + "].");

                    fireError(e);                   //@A1A
                }
            }
            // fire stopped working event
            fireStopWorking();

        } // end try block
        catch (Exception e)
        {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR releasing printer [" + printer_.getName () + "].");

            fireError (e);
        }
    }

} // end PrinterReleaseAction class

