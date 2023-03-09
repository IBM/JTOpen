///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputReleaseAction.java
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
The OutputReleaseAction class represents the releasing of a spooled file.
**/
class OutputReleaseAction
extends AbstractVAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // Private data.
    private static final String displayName_            = ResourceLoader.getText("ACTION_RELEASE");
    private SpooledFile splF_                           = null; // the spooled file

/**
Constructs an OutputReleaseAction object.

@param  object      The object.
@param  splF        The spooled file.
**/
    public OutputReleaseAction (VObject object, SpooledFile splF)
    {
        super (object);
        splF_ = splF;
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

            // release the spooled file
            splF_.release();

            // fire stopped working event
            fireStopWorking();

            // trace the release
            if (Trace.isTraceOn())
                Trace.log (Trace.INFORMATION, "Released file ["
                           + splF_.getName () + "].");

            fireObjectChanged ();
            } // end try block
        catch (Exception e)
            {
            // trace the error
            if (Trace.isTraceOn())
                Trace.log (Trace.ERROR, "ERROR releasing file [" + splF_.getName () + "].");

            fireError (e);
            }
    }

} // end OutputReleaseAction class

