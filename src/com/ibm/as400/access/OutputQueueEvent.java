///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: OutputQueueEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
 * The OutputQueueEvent class represents an OutputQueue event.
 *
 * @see OutputQueueListener
 *
 **/
public class OutputQueueEvent extends java.util.EventObject
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;


    /**
     * The output queue cleared event ID.
     **/
    public static final int CLEARED = 1;

    /**
     * The output queue held event ID.
     **/
    public static final int HELD = 2;

    /**
     * The output queue released event ID.
     **/
    public static final int RELEASED = 3;

    private int id_;

    /**
     * Constructs an OutputQueueEvent object. It uses the specified
     * source and ID.
     *
     * @param source The object sourcing the event.
     * @param id The event identifier.
     **/
    public OutputQueueEvent( Object source, int id )
    {
        super( source );

        if( (id < CLEARED) || (id > RELEASED) )
        {
            throw new ExtendedIllegalArgumentException("id",
              ExtendedIllegalArgumentException.RANGE_NOT_VALID );
        }

        id_ = id;
    }


    /**
     * Returns the output queue event identifier.
     * @return The event identifier.
     *
     **/
    public int getID()
    {
        return id_;
    }
}
