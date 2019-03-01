///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSObjAttrs1.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;


/**
 Encapsulates a File Server "Object Attributes 1" structure (OA1, OA1a, or OA1b).
 OA1* structures are used in several File Server requests and replies.
 **/
final class IFSObjAttrs1 implements Serializable
{
  // Design note: The File Server team has an include file named "sysattr.h", which contains the full details for the OA1 structure, including the details of the "Flags(1)" and "Flags(2)" fields.

  static final long serialVersionUID = 4L;

  // Location of the "Owner name flag" in the "Flags(1)" field of the OA1* structure.
  // To retrieve the Owner name, this flag must be set in the 'list attributes' request.
  static final int OWNER_NAME_FLAG = 0x00000800;



  private byte[] data_;


  IFSObjAttrs1(byte[] data)
  {
    data_ = data;
  }

  // Returns the value of the "Name of the owner of the object" field (at offset 224).
  final String getOwnerName(int systemCcsid) throws UnsupportedEncodingException
  {
    ConvTable conv = ConvTable.getTable(systemCcsid, null);
    String owner = conv.byteArrayToString(data_, 224, 10).trim();
    return owner;
  }


  final int length()
  {
    return data_.length;
  }

}
