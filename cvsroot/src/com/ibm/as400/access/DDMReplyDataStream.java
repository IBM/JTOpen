///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMReplyDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 *DDM reply data stream.
**/
class DDMReplyDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  /**
   *Constructs a DDM reply data stream with default values for the header
.  **/
  DDMReplyDataStream()
  {
    super();
  }

  /**
   *Constructs a DDMObjectDataStream object.
   *@param data the data with which to populate the object.
  **/
  DDMReplyDataStream(byte[] data)
  {
    super(data);
  }

  /**
   *Returns a new instance of a DDMObjectDataStream.
   *used by the DDMDataStream.construct() method.
   *@return a new instance of a DDMObjectDataStream.
  **/
  Object getNewDataStream()
  {
    return new DDMReplyDataStream();
  }

  /**
   *Returns a unique identifier for this type of object.
   *@return a unique identifier for this type of object.
  **/
  public int hashCode()
  {
    return 2;  // Reply
  }
}
