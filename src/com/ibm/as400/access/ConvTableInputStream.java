///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTableInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;

// ConvTableInputStream is a single purpose InputStream that should be used only be the ConvTable class.
// It is designed to improve the performance of character set conversion.
class ConvTableInputStream extends InputStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // The buffer into which incoming data is placed.
  private byte buffer[] = null;
  private int offset = 0;
  private int end = 0;

  ConvTableInputStream()
  {
  }

  // hold this buffer
  void setContents(byte[] buffer, int offset, int length)
  {
    this.buffer = buffer;
    this.offset = offset;
    this.end = offset+length;
  }

  // read one byte
  public int read()
  {
    if (this.buffer == null)
    {
      return -1;
    }
    int ret = buffer[this.offset++] & 0xFF;
    if (this.offset == this.end)
    {
      // now empty
      this.buffer = null;
    }
    return ret;
  }

  // read array of bytes
  public int read(byte buffer[], int offset, int length)
  {
    if (this.buffer == null)
    {
      return -1;
    }
    int bytesAvail = this.end - this.offset;
    if (length < bytesAvail)
    {
      System.arraycopy(this.buffer, this.offset, buffer, offset, length);
      this.offset += length;
      return length;
    }
    else
    {
      System.arraycopy(this.buffer, this.offset, buffer, offset, bytesAvail);
      this.buffer = null;
      return bytesAvail;
    }
  }

  // number of bytes available
  public int available()
  {
    if (this.buffer == null)
    {
      return 0;
    }
    else
    {
      return this.end - this.offset;
    }
  }
}
