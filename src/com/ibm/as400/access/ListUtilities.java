///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ListUtilities.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The ListUtilities class provides utilities for use in manipulating lists of objects.
**/
class ListUtilities
{
  private static final String copyright = "Copyright (C) 2004-2004 International Business Machines Corporation and others.";


  // Checks the value of the "list status indicator" field returned by QGY* API's.
  static void checkListStatus(byte listStatusIndicator) throws ErrorCompletingRequestException
  {
    if (listStatusIndicator != (byte)0xF2) // '2' means the list has been completely built
    {
      if (Trace.isTraceOn()) Trace.log(Trace.ERROR, "Unable to build object list on server ("+listStatusIndicator+")");
      throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
    }
  }


}
