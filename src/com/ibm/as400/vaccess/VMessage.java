///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VMessage.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import javax.swing.Icon;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;



/**
The VMessage class defines the representation of a message
returned from an AS/400 for use in various models and panes
in this package.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VMessage objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see com.ibm.as400.access.AS400Message
**/
public class VMessage
implements VObject, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String description_                        = ResourceLoader.getText ("MESSAGE_DESCRIPTION");
    private static final Icon   icon16_                             = ResourceLoader.getIcon ("VMessage16.gif", description_);
    private static final Icon   icon32_                             = ResourceLoader.getIcon ("VMessage32.gif", description_);



    // Properties.
    private AS400Message                        message_               = null;



    // Private data.
    transient private VPropertiesPane           propertiesPane_;



    // Event support.
    transient private ErrorEventSupport         errorEventSupport_;
    transient private VObjectEventSupport       objectEventSupport_;
    transient private WorkingEventSupport       workingEventSupport_;



/**
Property identifier for the message ID.
**/
    public static final Object      ID_PROPERTY                   = "ID";

/**
Property identifier for the message severity.
**/
    public static final Object      SEVERITY_PROPERTY             = "Severity";

/**
Property identifier for the message text.
**/
    public static final Object      TEXT_PROPERTY                 = "Text";

/**
Property identifier for the message type.
**/
    public static final Object      TYPE_PROPERTY                 = "Type";



/**
Constructs a VMessage object.

@param  message     The message.
**/
    VMessage (AS400Message message)
    {
        if (message == null)
            throw new NullPointerException ("message");

        message_ = message;

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
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
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
                                  <li>TYPE_PROPERTY
                                  <li>SEVERITY_PROPERTY
                                </ul>
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public synchronized Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the name.
        if (propertyIdentifier == NAME_PROPERTY)
            return this;

        // Get the description.
        else if (propertyIdentifier == DESCRIPTION_PROPERTY)
            return description_;

        // Get the ID.
        if (propertyIdentifier == ID_PROPERTY)
            return message_.getID ();

        // Get the text.
        else if (propertyIdentifier == TEXT_PROPERTY)
            return message_.getText ();

        // Get the severity.
        else if (propertyIdentifier == SEVERITY_PROPERTY)
            return new Integer (message_.getSeverity ());

        // Get the type.
        else if (propertyIdentifier == TYPE_PROPERTY)
            return MessageUtilities.getTypeText (message_.getType ());

        // By default, return null.
        return null;
    }



/**
Returns the text.  This is the message ID.

@return The message ID text.
**/
    public String getText ()
    {
        String id = message_.getID ();
        if (id == null)
            return "";
        else
            return id;
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
        propertiesPane_ = new MessagePropertiesPane (this, message_);

        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);
    }



/**
Loads information about the object from the AS/400.
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
        String id = message_.getID ();
        if (id == null)
            return "";
        else
            return id;
    }



}
