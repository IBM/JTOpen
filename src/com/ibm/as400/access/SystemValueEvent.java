///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SystemValueEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.EventObject;

/**
 * The SystemValueEvent class represents a system value event. 
 * @see SystemValueListener
 *
**/
public class SystemValueEvent extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data
    Object newValue;
    Object oldValue;
    Object source;

    /**
     * Constructs a new SystemValueEvent object.
     * @param source The source of the event.
     * @param newValue The new value.
     * @param oldValue The old value.
     *
    **/
    public SystemValueEvent(Object source, Object newValue, Object oldValue)
    {
        super(source);
        this.source = source;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    /* Copyright.
    */
    private static String getCopyright()
    {
        return Copyright.copyright;
    }

    /**
     * Returns the new value of the system value.
     * @return The new value of the system value.
     *
    **/
    public Object getNewValue()
    {
        return newValue;
    }

    /**
     * Returns the old value of the system value.
     * @return The old value of the system value.
     *
    **/
    public Object getOldValue()
    {
        return oldValue;
    }

}
    
