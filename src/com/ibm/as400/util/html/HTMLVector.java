///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: HTMLTree.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.Vector;

/**
 * This Vector is used internally by the HTML classes for performance reasons.
**/
final class HTMLVector extends Vector
{
  /**
   * This returns the superclass's element data array, which may be longer
   * than the actual number of elements. Therefore, the objects at the end
   * of the array could be null. Care should be taken to avoid these when
   * looping, by using the elementCount returned by getCount().
  **/
  public Object[] getData()
  {
    return elementData;
  }
  
  /**
   * This returns the superclass's element count, which is the actual number
   * of elements that are populated in the array returned by getData().
   * Use this number for the maximum element count when looping through the
   * element data, to avoid grabbing the null elements at the end of the
   * element data array.
  **/
  public int getCount()
  {
    return elementCount;
  }
}
