///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VJobLogMessage.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RQueuedMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;
import javax.swing.Icon;



/**
The VJobLogMessage class defines the representation of
a job log message on a server for use in various models
and panes in this package.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VJobLogMessage objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>
**/
public class VJobLogMessage
implements VObject, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String description_    = ResourceLoader.getText ("JOB_LOG_MESSAGE_DESCRIPTION");
    private static final Icon   icon16_         = ResourceLoader.getIcon ("VMessage16.gif", description_);
    private static final Icon   icon32_         = ResourceLoader.getIcon ("VMessage32.gif", description_);



    // Properties.
    private RQueuedMessage       message_        = null;



    // Private data.
    transient private VPropertiesPane     propertiesPane_;



    // Event support.
    transient private ErrorEventSupport           errorEventSupport_;
    transient private VObjectEventSupport         objectEventSupport_;
    transient private WorkingEventSupport         workingEventSupport_;



/**
Property identifier for the date and time that the message was sent.
**/
    public static final Object      DATE_PROPERTY                 = "Date";

/**
Property identifier for the from program.
**/
    public static final Object      FROM_PROGRAM_PROPERTY         = "From program";

/**
Property identifier for the ID.
**/
    public static final Object      ID_PROPERTY                   = "ID";

/**
Property identifier for the severity.
**/
    public static final Object      SEVERITY_PROPERTY             = "Severity";

/**
Property identifier for the text.
**/
    public static final Object      TEXT_PROPERTY                 = "Text";

/**
Property identifier for the type.
**/
    public static final Object      TYPE_PROPERTY                 = "Type";



// Map vaccess property identifiers to resource attribute IDs:
    private static final Hashtable map_ = new Hashtable();
    static {
        map_.put(DATE_PROPERTY,             RQueuedMessage.DATE_SENT);
        map_.put(FROM_PROGRAM_PROPERTY,     RQueuedMessage.SENDING_PROGRAM_NAME);
        map_.put(ID_PROPERTY,               RQueuedMessage.MESSAGE_ID);
        map_.put(SEVERITY_PROPERTY,         RQueuedMessage.MESSAGE_SEVERITY);
        map_.put(TEXT_PROPERTY,             RQueuedMessage.MESSAGE_TEXT);
        map_.put(TYPE_PROPERTY,             RQueuedMessage.MESSAGE_TYPE);
    }



    // Properties pane layout.
    private static ResourceProperties   properties_         = null;
    static {
        // Resource properties pane layout.
        properties_ = new ResourceProperties();
        properties_.addProperties(new Object[] { RQueuedMessage.MESSAGE_TEXT, RQueuedMessage.MESSAGE_HELP, 
                                      RQueuedMessage.MESSAGE_SEVERITY, RQueuedMessage.MESSAGE_TYPE, 
                                      RQueuedMessage.DATE_SENT, RQueuedMessage.SENDING_PROGRAM_NAME });

    }



/**
Constructs a VJobLogMessage object.

@param message  The job log message.
**/
    VJobLogMessage (RQueuedMessage message)
    {
        if (message == null)
            throw new NullPointerException ("message");

        message_    = message;

        initializeTransient ();
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

@return Always null.  There are no actions.
**/
    public VAction[] getActions ()
    {
        return null;
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
                value is given, then return a default.
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
                                  <li>ID_PROPERTY
                                  <li>TEXT_PROPERTY
                                  <li>SEVERITY_PROPERTY
                                  <li>TYPE_PROPERTY
                                  <li>DATE_PROPERTY
                                  <li>FROM_PROGRAM_PROPERTY
                                </ul>
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the name.
        if (propertyIdentifier == NAME_PROPERTY)
            return this;

        // Get the description.
        else if (propertyIdentifier == DESCRIPTION_PROPERTY)
            return description_;

        else if (propertyIdentifier == null)
            return null;

        else {
            Object attributeID = map_.get(propertyIdentifier);
            if (attributeID == null)
                return null;
            Object propertyValue;
            try {
                propertyValue = message_.getAttributeValue(attributeID);
            }
            catch(ResourceException e) {
                errorEventSupport_.fireError(e);
                return null;
            }
            if (propertyIdentifier == TYPE_PROPERTY)
                return MessageUtilities.getTypeText(((Integer)propertyValue).intValue());
            else
                return propertyValue;
        }
    }



/**
Returns the text.  This is the message ID.

@return The message ID text.
**/
    public String getText ()
    {
        String id;
        try {
            return (String)message_.getAttributeValue(RQueuedMessage.MESSAGE_ID);
        }
        catch(ResourceException e) {
            return "";
        }
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        // Initialize the event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        // Initialize the properties pane.
        propertiesPane_ = new VResourcePropertiesPane(this, message_, properties_);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);
    }



/**
Loads information about the object from the server.
**/
    public void load ()
    {
        // This does nothing.
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
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
Returns the string representation.  This is the message ID.

@return The string representation of the message ID.
**/
    public String toString ()
    {
        return getText();
    }



}
