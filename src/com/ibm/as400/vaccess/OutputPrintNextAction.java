///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputPrintNextAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.Trace;

/**
The OutputPrintNextAction class represents the action of moving a spooled
file to the top so it can be printed next.
**/
class OutputPrintNextAction
extends AbstractVAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_PRINTNEXT");
    private SpooledFile splF_                           = null; // the spooled file
    private VPrinterOutput parent_                      = null; // parent (the spooled list)

/**
Constructs an OutputPrintNextAction object.

@param  object      The object.
@param  splF        The spooled file.
**/
    public OutputPrintNextAction (VObject object, SpooledFile splF, VPrinterOutput parent)
    {
        super (object);
        splF_ = splF;
        parent_ = parent;
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
        try {

            // fire started working event
            fireStartWorking();

            // move the spooled file
            splF_.moveToTop();

            // fire stopped working event
            fireStopWorking();

            // trace the move
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Moved file ["
                           + splF_.getName () + "].");

            // fire the object changed passing in the object's parent so the list is
            // rearranged
            fireObjectChanged (parent_);
            } // end try block
        catch (Exception e)
            {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR moving file [" + splF_.getName () + "].");

            fireError (e);
            }
    }

} // end OutputPrintNextAction class

