///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ValueMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;



/**
The ValueMap interface represents a mapping between physical and
logical values.  While this could be used in many contexts, the
most common is to consider the physical value as stored on or
communicated to an AS/400, and the logical value to be manipulated
or externalized in Java.

<p>This class is intended as a helper class for implementing subclasses
of <a href="Resource.html">Resource</a>.

@see AbstractValueMap
**/
public interface ValueMap
{



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@param system           The system.
@return                 The physical value.
**/
    public abstract Object ltop(Object logicalValue, AS400 system);



/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@param system           The system.
@return                 The logical value.
**/
    public abstract Object ptol(Object physicalValue, AS400 system);


}
