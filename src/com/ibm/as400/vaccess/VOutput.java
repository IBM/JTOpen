///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VOutput.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.QSYSObjectPathName;
import javax.swing.Icon;
import java.util.Calendar;

/**
The VOutput class defines the representation of a
spooled file output on a server for use in various
models and panes in this package.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VOutput objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.SpooledFile
**/
public class VOutput
implements VObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

/**
Property identifier for the output name.
**/
    public static final Object  OUTPUTNAME_PROPERTY       = "Output name";

/**
Property identifier for the user specified data.
**/
    public static final Object  USERSPECDATA_PROPERTY     = "User-specified data";

/**
Property identifier for the name of the user of the job which produced the output.
**/
    public static final Object  USER_PROPERTY             = "User";

/**
Property identifier for the status of the output.
**/
    public static final Object  STATUS_PROPERTY           = "Status";

/**
Property identifier for the printer name.
**/
    public static final Object  PRINTER_PROPERTY          = "Printer";

/**
Property identifier for the number of pages per copy.
**/
    public static final Object  PAGESPERCOPY_PROPERTY     = "Pages per copy";

/**
Property identifier for the number of copies left.
**/
    public static final Object  COPIESLEFT_PROPERTY       = "Copies left";

/**
Property identifier for the date the output was created.
**/
    public static final Object  DATE_PROPERTY             = "Date created";

/**
Property identifier for the form type.
**/
    public static final Object  FORMTYPE_PROPERTY         = "Form type";

/**
Property identifier for the name of the job which produced the output.
**/
    public static final Object  JOB_PROPERTY              = "Job";

/**
Property identifier for the number of the job which produced the output.
**/
    public static final Object  JOBNUMBER_PROPERTY        = "Job number";

/**
Property identifier for the number of the output.
**/
    public static final Object  NUMBER_PROPERTY           = "Number";

/**
Property identifier for the output queue.
**/
    public static final Object  OUTPUTQUEUE_PROPERTY      = "Output queue";

/**
Property identifier for the output queue library.
**/
    public static final Object  OUTPUTQUEUELIB_PROPERTY   = "Output queue library";

/**
Property identifier for the priority of the output on the output queue.
**/
    public static final Object  PRIORITY_PROPERTY         = "Priority on output queue";

/**
Property identifier for the user comment.
**/
    public static final Object  USERCOMMENT_PROPERTY      = "User comment";


    // Static data.
    private static final String description_      = ResourceLoader.getPrintText ("OUTPUT_DESCRIPTION");
    private static final Icon   icon16_           = ResourceLoader.getIcon ("VOutput16.gif", description_);
    private static final Icon   icon32_           = ResourceLoader.getIcon ("VOutput32.gif", description_);
    private static final String closedText_       = ResourceLoader.getPrintText ("CLOSED");
    private static final String heldText_         = ResourceLoader.getPrintText ("HELD");
    private static final String msgWaitingText_   = ResourceLoader.getPrintText ("MESSAGE_WAITING");
    private static final String openText_         = ResourceLoader.getPrintText ("OPEN");
    private static final String pendingText_      = ResourceLoader.getPrintText ("PENDING");
    private static final String printerText_      = ResourceLoader.getPrintText ("PRINTER");
    private static final String readyText_        = ResourceLoader.getPrintText ("READY");
    private static final String savedText_        = ResourceLoader.getPrintText ("SAVED");
    private static final String writingText_      = ResourceLoader.getPrintText ("WRITING");
    private static final String standardText_     = ResourceLoader.getPrintText ("STANDARD");
    private static final String jobValueText_     = ResourceLoader.getPrintText ("JOB_VALUE");
    private static final String notAssignedText_  = ResourceLoader.getPrintText ("NOT_ASSIGNED");
    private static final String groupText_        = ResourceLoader.getPrintText ("GROUP");
    private static Calendar     calendar_         = Calendar.getInstance ();

    // private data.
    private VAction[]           actions_            = null;
    private Integer             actionsLock_        = new Integer (0);
    private SpooledFile         splF_               = null;
    private VPrinterOutput      parent_             = null;
    private VPropertiesPane     propertiesPane_     = null;
    private Integer             propertiesPaneLock_ = new Integer (0);
    private Integer             reloadLock_         = new Integer (0);

    // event support
    private ErrorEventSupport        errorEventSupport_     = new ErrorEventSupport (this);
    private VObjectEventSupport      objectEventSupport_    = new VObjectEventSupport (this);
    private WorkingEventSupport      workingEventSupport_   = new WorkingEventSupport (this);


/**
Constructs a VOutput object.

@param  parent      The parent.
@param  spooledFile        The spooled file.
**/
    public VOutput (VPrinterOutput parent, SpooledFile spooledFile)
    {
        if (spooledFile == null)
            throw new NullPointerException ("spooledFile");

        if (parent == null)
            throw new NullPointerException ("parent");

        parent_ = parent;
        splF_ = spooledFile;

        // create the actions
        actions_    = new VAction[8];    /* @A1C - changed from 7 to 8 */

        actions_[0] = new OutputReplyAction (this, spooledFile);      // reply
        actions_[1] = new OutputHoldAction (this, spooledFile);       // hold
        actions_[2] = new OutputReleaseAction (this, spooledFile);    // release
        actions_[3] = new OutputPrintNextAction (this, spooledFile, parent);  // print next
        actions_[4] = new OutputSendAction (this, spooledFile);       // send
        actions_[5] = new OutputMoveAction (this, spooledFile, parent);// move
        actions_[6] = new OutputDeleteAction (this, spooledFile, parent);// delete
        actions_[7] = new OutputViewAction (this, spooledFile);    // view  @A1A

        // Listen to the actions
        for (int i = 0; i< actions_.length; ++i)
            {
            actions_[i].addErrorListener (errorEventSupport_);
            actions_[i].addVObjectListener (objectEventSupport_);
            actions_[i].addWorkingListener (workingEventSupport_);
            } // end for

        // create the properties pane and listen for the events
        propertiesPane_ = new OutputPropertiesPane (this, spooledFile);
        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);
    }


/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }


/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }


/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }


/**
Returns the list of actions that can be performed.
<ul>
  <li>reply
  <li>hold
  <li>release
  <li>print next
  <li>send
  <li>move
  <li>delete
  <li>view
</ul>

@return The actions.
**/
    public synchronized VAction[] getActions ()
    {
        // need to enable/disable the correct ones
        try {
            // retrieve the spooled file status
            String status = splF_.getStringAttribute(PrintObject.ATTR_SPLFSTATUS).trim();

            // reply action valid when status is message waiting
            if (status.equals("*MESSAGE")) actions_[0].setEnabled(true);
            else actions_[0].setEnabled(false);

            // hold action valid only when status is released
            if (status.equals("*READY")) actions_[1].setEnabled(true);
            else actions_[1].setEnabled(false);

            // release action valid only when status is held
            if (status.equals("*HELD")) actions_[2].setEnabled(true);
            else actions_[2].setEnabled(false);

            } // end try block
        catch (Exception e)
            {
            errorEventSupport_.fireError (e);
            } // end catch block

        return actions_;
    }


/**
Returns the default action.

@return Always null.  There is no default action.
**/
    public VAction getDefaultAction ()
    {
        return null;
    }


/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return the default of 16.
@param  open    This parameter has no effect.
@return         The icon.
**/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }


/**
Returns the properties pane.

@return The properties pane.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }


/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                <ul>
                                  <li>NAME_PROPERTY
                                  <li>DESCRIPTION_PROPERTY
                                  <li>OUTPUTNAME_PROPERTY
                                  <li>USERSPECDATA_PROPERTY
                                  <li>USER_PROPERTY
                                  <li>STATUS_PROPERTY
                                  <li>PRINTER_PROPERTY
                                  <li>PAGESPERCOPY_PROPERTY
                                  <li>COPIESLEFT_PROPERTY
                                  <li>DATE_PROPERTY
                                  <li>FORMTYPE_PROPERTY
                                  <li>JOB_PROPERTY
                                  <li>JOBNUMBER_PROPERTY
                                  <li>NUMBER_PROPERTY
                                  <li>OUTPUTQUEUE_PROPERTY
                                  <li>OUTPUTQUEUELIB_PROPERTY
                                  <li>PRIORITY_PROPERTY
                                  <li>USERCOMMENT_PROPERTY
                                </ul>
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public synchronized Object getPropertyValue (Object propertyIdentifier)
    {
        try {
            // Get the file name.
            if (propertyIdentifier == NAME_PROPERTY)
                return this;

            // Get the description.
            else if (propertyIdentifier == DESCRIPTION_PROPERTY)
                return description_;

            // Output name
            if (propertyIdentifier == OUTPUTNAME_PROPERTY)
                return this;

            // user-specified data
            else if (propertyIdentifier == USERSPECDATA_PROPERTY)
                return splF_.getStringAttribute(PrintObject.ATTR_USERDATA);

            // user
            else if (propertyIdentifier == USER_PROPERTY)
                return splF_.getJobUser();

            // status
            else if (propertyIdentifier == STATUS_PROPERTY)
                {
                String status = splF_.getStringAttribute(PrintObject.ATTR_SPLFSTATUS);
                if (status.trim().equals("*CLOSED")) return closedText_;
                if (status.trim().equals("*HELD")) return heldText_;
                if (status.trim().equals("*MESSAGE")) return msgWaitingText_;
                if (status.trim().equals("*OPEN")) return openText_;
                if (status.trim().equals("*PENDING")) return pendingText_;
                if (status.trim().equals("*PRINTER")) return printerText_;
                if (status.trim().equals("*READY")) return readyText_;
                if (status.trim().equals("*SAVED")) return savedText_;
                if (status.trim().equals("*WRITING")) return writingText_;
                return null;
                }

            // printer
            else if (propertyIdentifier == PRINTER_PROPERTY)
                {
                String ptrAssigned = splF_.getStringAttribute(PrintObject.ATTR_PRTASSIGNED).trim();
                if (ptrAssigned.equals("1")) // splf is assigned to a printer
                    return splF_.getStringAttribute(PrintObject.ATTR_PRINTER);
                else if (ptrAssigned.equals("2")) // splf is assigned to multiple printers
                    return groupText_;
                else // splf is not assigned
                    return notAssignedText_;
                }

            // pages per copy
            else if (propertyIdentifier == PAGESPERCOPY_PROPERTY)
                return splF_.getIntegerAttribute(PrintObject.ATTR_PAGES);

            // copies left
            else if (propertyIdentifier == COPIESLEFT_PROPERTY)
                return splF_.getIntegerAttribute(PrintObject.ATTR_COPIESLEFT);

            // date
            else if (propertyIdentifier == DATE_PROPERTY)
                {
                // return the string that represents the date
                String date = splF_.getStringAttribute(PrintObject.ATTR_DATE);
                String time = splF_.getStringAttribute(PrintObject.ATTR_TIME);
                // @A2A - The code below used to substring (1,3) for the year, omitting
                //        the century field, and did not add 1900 to the result (base time).
                //        The code now substrings (0,3) and adds in 1900, thus
                //        producing the correct date.
                calendar_.set((Integer.parseInt(date.substring(0,3)) + 1900),// year @A2C
                               Integer.parseInt(date.substring(3,5))-1,// month is zero based
                               Integer.parseInt(date.substring(5,7)),  // day
                               Integer.parseInt(time.substring(0,2)),  // hour
                               Integer.parseInt(time.substring(2,4)),  // minute
                               Integer.parseInt(time.substring(4,6))); // second

                return calendar_.getTime();
                }

            // formtype
            else if (propertyIdentifier == FORMTYPE_PROPERTY)
                {
                // we need to check for *STD
                String formType =  splF_.getStringAttribute(PrintObject.ATTR_FORMTYPE).trim();
                if (formType.equals("*STD")) return standardText_;
                else return formType;
                }

            // job
            else if (propertyIdentifier == JOB_PROPERTY)
                return splF_.getJobName();

            // job number
            else if (propertyIdentifier == JOBNUMBER_PROPERTY)
                return splF_.getJobNumber();

            // spooled file number
            else if (propertyIdentifier == NUMBER_PROPERTY)
                return new Integer(splF_.getNumber());

            // output queue
            else if (propertyIdentifier == OUTPUTQUEUE_PROPERTY)
                {
                // extract the IFS path name of the output queue
                QSYSObjectPathName outQPath = new QSYSObjectPathName(splF_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE));
                // send back just the output queue name
                return outQPath.getObjectName();
                }

            // output queue library
            else if (propertyIdentifier == OUTPUTQUEUELIB_PROPERTY)
                {
                // extract the IFS path name of the output queue
                QSYSObjectPathName outQPath = new QSYSObjectPathName(splF_.getStringAttribute(PrintObject.ATTR_OUTPUT_QUEUE));
                // send back just the output queue library
                return outQPath.getLibraryName();
                }

            // priority
            else if (propertyIdentifier == PRIORITY_PROPERTY)
                {
                // we need to check for *JOB
                String outQPty = splF_.getStringAttribute(PrintObject.ATTR_OUTPTY).trim();
                if (outQPty.equals("*JOB")) return jobValueText_;
                else return outQPty;
                }

            // user comment
            else if (propertyIdentifier == USERCOMMENT_PROPERTY)
                return splF_.getStringAttribute(PrintObject.ATTR_USERCMT);

            // By default, return null.
            return null;
         } // end try block

        catch (Exception e)
            {
            errorEventSupport_.fireError (e);
            } // end catch block

        // By default, return null.
        return null;
    }


// @A3A
/**
Returns the spooled file.

@return The spooled file.
**/
    public SpooledFile getSpooledFile()
    {
        return splF_;
    }



/**
Loads information about the object from the server.
**/
    public synchronized void load ()
    {
        workingEventSupport_.fireStartWorking();

        try {
            // just update the spooled file attributes
            splF_.update();
            } // end try block

        catch (Exception e)
            {
            errorEventSupport_.fireError (e);
            } // end catch block

        workingEventSupport_.fireStopWorking ();
    }


/**
Returns the text.  This is the name of the spooled file (output).

@return The text, which is the name of the spooled file.
**/
    public String getText ()
    {
        // return the name of the spooled file
        return splF_.getName();
    }


/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }


/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }


/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }


/**
Returns the string representation.
This is the spooled file (output) name, number, job user,
job number and job name, all separated with blanks.

@return The string representation.
**/
    public String toString ()
    {
        return splF_.getName().trim() + "  " +
               Integer.toString(splF_.getNumber()) + "  " +
               splF_.getJobUser().trim() + "  " +
               splF_.getJobNumber().trim() + "  " +
               splF_.getJobName().trim();
    }



} // end VOutput class


