///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSLookupRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;



/**
Create Working Directory Handle reply.
**/
class IFSLookupRep extends IFSDataStream
{
  private static final String copyright = "Copyright (C) 2004-2004 International Business Machines Corporation and others.";

  private static final int LLCP_LENGTH = 6;
  
  private static final int TEMPLATE_LENGTH_OFFSET = 16;
  private static final int OBJECT_HANDLE_OFFSET = 22;
  
  static final int OA_NONE = IFSLookupReq.OA_NONE;
  static final int OA1 = IFSLookupReq.OA1;
  static final int OA2 = IFSLookupReq.OA2;

/**
Get object handle.
@return the object handle.
**/
  int getHandle()
  {
    return get32bit(OBJECT_HANDLE_OFFSET);
  }
  
  /**
   Get ASP number
   @return the ASP number
   * @throws UnsupportedEncodingException 
   */
  int getASP() throws UnsupportedEncodingException {
    return new IFSObjAttrs1(getObjAttrBytes(OA1)).getASP();
  }
  
  int getCCSID(int datastreamLevel) {
    return new IFSObjAttrs2(getObjAttrBytes(OA2)).getCCSID(datastreamLevel);
  }
  
  String getOwnerName(int systemCcsid) throws UnsupportedEncodingException {
    return new IFSObjAttrs1(getObjAttrBytes(OA1)).getOwnerName(systemCcsid);
  }
  

/**
Generate a new instance of this type.
@return a reference to the new instance
**/
  public Object getNewDataStream()
  {
    return new IFSLookupRep();
  }

/**
Generates a hash code for this data stream.
@return the hash code
**/
  public int hashCode()
  {
    return 0x800C;
  }
  
  
  private final byte[] getObjAttrBytes(int type)
  {
    if (type == OA_NONE) return null;

    // Find the first OA structure in the reply's "Optional/Variable Section".
    int offset = HEADER_LENGTH + get16bit(TEMPLATE_LENGTH_OFFSET);
    int oaCodePoint = (type == OA1 ? 0x0010 : 0x000F); // OA1: CP == 0x0010; OA2: CP == 0x000F
    byte[] buf = null;
    while (buf == null && offset < data_.length)
    {
      // Look for an LLCP with a CP value that specifies an OA* structure.
      int length = get32bit(offset);           // Get the LL value.
      short codePoint = (short)get16bit(offset + 4);  // Get the CP value.
      if (codePoint == oaCodePoint) {  // We found an OA* of the desired type.
        int OAlength = length - LLCP_LENGTH;  // Exclude the LLCP.
        buf = new byte[OAlength];
        System.arraycopy(data_, offset + LLCP_LENGTH, buf, 0, OAlength);
      }
      else {  // not what we're looking for, so keep looking
        offset += length;  // skip to next LLCP
      }
    }

    if (buf == null) {
      Trace.log(Trace.ERROR, "The reply does not contain an OA"+type+ " structure.");
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    return buf;
  }
}




