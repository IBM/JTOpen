///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AbstractValueMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import java.io.Serializable;



/**
The AbstractValueMap class is a default implementation for a ValueMap.
This is useful if an implementation only needs to provide a single
method.

<p>This class is intended as a helper class for implementing subclasses
of {@link com.ibm.as400.resource.Resource Resource}.
**/
public abstract class AbstractValueMap
implements ValueMap, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@param system           The system.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue, AS400 system)
    {
        return ltop(logicalValue);
    }



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue)
    {
        return logicalValue;
    }



/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@param system           The system.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue, AS400 system)
    {
        return ptol(physicalValue);
    }


/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue)
    {
        return physicalValue;
    }


}
