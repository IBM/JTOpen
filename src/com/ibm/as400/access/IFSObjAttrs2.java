///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSObjAttrs2.java
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
 Encapsulates a File Server "Object Attributes 2" structure (OA2, OA2s, OA2b, or OA2c).
 OA2* structures are used in several File Server requests and replies.
 **/
final class IFSObjAttrs2 implements Serializable
{
  // Design note: The File Server team has an include file named "vattr.h", which contains the full details for the OA2 structure.
  static final long serialVersionUID = 4L;
  private byte[] data_;


  IFSObjAttrs2(byte[] data)
  {
    data_ = data;
  }

  // Returns the value of the "CCSID of the object" field.
  final int getCCSID(int datastreamLevel)
  {
    // Determine the offset into the OA* structure of the CCSID (or codepage) field.
    int offsetToField = determineCCSIDOffset(datastreamLevel);
    return (int)BinaryConverter.byteArrayToShort(data_, offsetToField) & 0x0000ffff;
  }

  // Sets the value of the "CCSID of the object" field.
  final void setCCSID(int ccsid, int datastreamLevel)
  {
    int offsetToField = determineCCSIDOffset(datastreamLevel);
    byte[] ccsidBytes = BinaryConverter.shortToByteArray((short)ccsid);
    System.arraycopy(ccsidBytes, 0, data_, offsetToField, 2);
  }

  final byte[] getData()
  {
    return data_;
  }

  // Returns the value of the "Owner user ID" field.  (Offset 64)
  final long getOwnerUID()
  {
    return (long)BinaryConverter.byteArrayToInt(data_, 64) & 0x0FFFFFFFFL;
  }


  /**
   Get offset of the the 2-byte CCSID (or codepage) field in the OA2x structure.
   @return the CCSID offset
   **/
  static final int determineCCSIDOffset(int datastreamLevel)
  {
    // Note: Only if the server is reporting Datastream Level 2 (or later) will the reply have a CCSID field.
    // If prior to Level 2, we must make do with the codepage value.

    /*

     Note: To figure out the format of the returned information, we need to
     consider both the requested and reported Datastream Levels (DSLs):

     DSL requested     DSL reported    OA format sent
     by client         by server       by server
     _____________     ____________    _______________

     0                 any             OA2

     2                 0               OA2

     2                 F4F4            OA2a

     2                 2               OA2b

     2                 3               OA2b

     8                 0               OA2

     8                 F4F4            OA2a

     8                 2               OA2b

     8                 3               OA2b

     8                 8               OA2c

     16                0               OA2

     16                F4F4            OA2a

     16                2               OA2b

     16                3               OA2b

     16                8               OA2c

     16                12              OA2c

     16                16              OA2c

     Note: Since the Toolbox will only request levels 0, 2, 8, or 16,
     the server will never report level 1.
     */
    int offset_into_OA;  // offset into OA* structure for CCSID or codepage field
    switch (datastreamLevel)
    {
      case 0:       // OA2
        offset_into_OA = 126;  // offset of the 'codepage' field in the OA2 structure
        break;
      case 0xF4F4:  // OA2a
        offset_into_OA = 142;  // offset of the 'codepage' field in the OA2a structure
        break;
      default:      // OA2b or OA2c
        offset_into_OA = 134;  // offset of the 'CCSID of object' field in the OA2b/OA2c structure
        break;
    }
    return offset_into_OA;
  }


  final int length()
  {
    return data_.length;
  }

}
