///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PxParm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The PxParm interface represents a parameter in a proxy datastream.
This DOES NOT refer to a parameter in a method call, but instead a
parameter that is part of some request or reply.
**/
interface PxParm
extends PxDSRV, PxDSWV
{



    public abstract Object getObjectValue ();



}
