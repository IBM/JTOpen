///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ByteArrayKey.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

/**
 * Utility class for mapping byte array portions.
**/
public final class ByteArrayKey
{
  private byte[] key_;
  private int offset_;
  private int length_;
  private int hash_;

  public ByteArrayKey()
  {
  }

  public ByteArrayKey(final byte[] key)
  {
    key_ = key;
    offset_ = 0;
    length_ = key.length;
    hash_ = computeHash(key, 0, key.length);
  }

  private static final int computeHash(final byte[] key, final int offset, final int length)
  {
/*    int hash = 0;
    for (int i=0; i<length_ && i<4; ++i)
    {
      hash = hash | key_[offset_+i];
    }
    if (length_ > 4)
    {
      for (int i=length_-4; i<length_; ++i)
      {
        hash = hash | key_[offset_+i];
      }
    }
    hash_ = hash;
*/
    return key[offset] + key[offset+length-1] + key[offset+(length >> 1)];
  }

  public void setHashData(final byte[] data, final int offset, final int length)
  {
    key_ = data;
    offset_ = offset;
    length_ = length;
    hash_ = computeHash(data, offset, length);
  }

  public int hashCode()
  {
    return hash_;
  }

  public boolean equals(Object obj)
  {
    if (obj != null && obj instanceof ByteArrayKey)
    {
      ByteArrayKey e = (ByteArrayKey)obj;
      return this.matches(e.key_, e.offset_, e.length_);
    }
    return false;
  }

  public byte[] getKey()
  {
    return key_;
  }

  public int getOffset()
  {
    return offset_;
  }

  public int getLength()
  {
    return length_;
  }

  public boolean matches(final byte[] data, final int offset, final int length)
  {
    if (length_ != length) return false;
    for (int i=0; i<length_; ++i)
    {
      if (key_[offset_+i] != data[offset+i]) return false;
    }
    return true;
  }
}

