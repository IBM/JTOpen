///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PropertyChangeSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;



/**
The PropertyChangeSupport class represents a list of
PropertyChangeListeners.  This is also a PropertyChangeListener
and will dispatch all property change events that it receives.
**/
class PropertyChangeSupport
extends java.beans.PropertyChangeSupport
implements PropertyChangeListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs an PropertyChangeSupport object.

@param  source  The source for the events.
**/
    public PropertyChangeSupport (Object source)
    {
        super (source);
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        super.addPropertyChangeListener (listener);
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Processes a property change event.

@param  event   The event.
**/
    public void propertyChange (PropertyChangeEvent event)
    {
        firePropertyChange (event.getPropertyName (), event.getOldValue (), event.getNewValue ());
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        super.removePropertyChangeListener (listener);
    }



}



