///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBOverlay.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBOverlay interface describes a structure that lays on top of
an existing byte array.  An implementation will set the bytes according
to some structure.

<p>The main point to this interface is to reduce the number of
intermediate byte array creations and copies by allowing structures
to set a given byte array directly.
**/
interface DBOverlay
{



/**
Positions the overlay structure.

@param  rawBytes        The byte array to overlay.
@param  offset          The starting offset.
**/
    public abstract void overlay (byte[] rawBytes, int offset);



/**
Returns the length of the overlay structure.

@return The length of the overlay structure.
**/
    public abstract int getLength ();



}
