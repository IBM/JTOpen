///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataQueueEvent.java
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
 The DataQueueEvent class represents a DataQueue event.
 **/
public class DataQueueEvent extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


    /**
     The DataQueue clear event ID.  This event is delivered when a clear has been performed.
     **/
    public static final int DQ_CLEARED = 0;

    /**
     The DataQueue peek event ID.  This event is delivered when a peek has been performed.
     **/
    public static final int DQ_PEEKED = 1;

    /**
     The DataQueue read event ID.  This event is delivered when a read has been performed.
     **/
    public static final int DQ_READ = 2;

    /**
     The DataQueue write event ID.  This event is delivered when a write has been performed.
     **/
    public static final int DQ_WRITTEN = 3;

    private int id_; // event identifier

    /**
     Constructs a DataQueueEvent object. It uses the specified source and ID.
     @param  source  The object where the event originated.
     @param  id  The event identifier.
     **/
    public DataQueueEvent(Object source, int id)
    {
        super(source);

        if (id < DQ_CLEARED || id > DQ_WRITTEN)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'id' is not valid:", id);
            throw new ExtendedIllegalArgumentException("id (" + id + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        id_ = id;
    }

    /**
     Returns the identifier for this event.
     @return  The identifier for this event.
     **/
    public int getID()
    {
        return id_;
    }
}
