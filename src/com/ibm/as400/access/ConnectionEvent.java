///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConnectionEvent.java
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
 The ConnectionEvent class represents a Connection event.
 **/
public class ConnectionEvent extends EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;

    int service_;

    /**
     Constructs a ConnectionEvent object.  It uses the specified source and service.
     @param  source  The object where the event originated.
     @param  service  The service ID.
     **/
    public ConnectionEvent(Object source, int service)
    {
        super(source);

        service_ = service;
    }

    /**
     Returns the service ID.  This identifies the service that was connected or disconnected.  The service ID's are defined in the AS400 class.
     @return  The service ID.
     **/
    public int getService()
    {
        return service_;
    }

    void setSource(Object s)
    {
        source = s;
    }
}
