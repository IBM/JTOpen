///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrinterUnavailableAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Printer;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.Trace;

/**
The PrinterUnavailableAction class represents the releasing of a printer.
The actual affect is to release the writer that is associated with the
printer**/

class PrinterUnavailableAction
extends AbstractVAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_UNAVAILABLE");
    private Printer printer_                            = null; // the printer

/**
Constructs a PrinterUnavailableAction object.

@param  object      The object.
@param  printer     The printer.
**/
    public PrinterUnavailableAction (VObject object, Printer printer)
    {
        super (object);
        printer_ = printer;
    }


    // Returns the copyright.
    private static String getCopyright()
    {
        return Copyright_v.copyright;
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

            // issue the call
            CommandCall cmd = new CommandCall( printer_.getSystem());
            String cmdString = new String("VRYCFG CFGOBJ("+ printer_.getName() + ") ");
            cmdString += "CFGTYPE(*DEV) STATUS(*OFF) RANGE(*OBJ)";
            try
            {
                if (cmd.run(cmdString)!=true)
                {
                    // Note that there was an error
                    if (Trace.isTraceOn())
                        Trace.log (Trace.ERROR, "ERROR VRYCFG cmd for [" + printer_.getName () + "].");

                    // fire an error event
                    Exception e = new Exception(ResourceLoader.getText("EXC_AS400_ERROR"));
                    fireError(e);
                }
                else                                                //@A1A
                {
                    //Everything is rosy
                    // trace the release
                    if (Trace.isTraceOn())                          //@A1M
                        Trace.log (Trace.INFORMATION, "Varied Off printer [" + printer_.getName () + "].");//@A1M

                    fireObjectChanged ();                           //@A1M
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

                fireError(e);                               //@A1A
            }

            // fire stopped working event
            fireStopWorking();
        } // end try block
        catch (Exception e)
        {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR Varied Off printer [" + printer_.getName () + "].");

            fireError (e);
        }
    }

} // end PrinterUnavailableAction class

