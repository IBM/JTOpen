///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VetoableChangeSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;



/**
The PropertyChangeSupport class represents a list of
VetoableChangeListeners.  This is also a VetoableChangeListener
and will dispatch all vetoable change events that it receives.
**/
class VetoableChangeSupport
extends java.beans.VetoableChangeSupport
implements VetoableChangeListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




/**
Constructs an VetoableChangeSupport object.

@param  source  The source for the events.
**/
    public VetoableChangeSupport (Object source)
    {
        super (source);
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        super.addVetoableChangeListener (listener);
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeVetoableChangeListener (VetoableChangeListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        super.removeVetoableChangeListener (listener);
    }



/**
Processes a vetoable change event.

@param  event   The event.

@exception PropertyVetoException The property change has been rejected.
**/
    public void vetoableChange (PropertyChangeEvent event)
        throws PropertyVetoException
    {
        fireVetoableChange (event.getPropertyName (), event.getOldValue (), event.getNewValue ());
    }



}


