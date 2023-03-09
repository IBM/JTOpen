///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IntegerHashtable.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * This class represents a hashtable that uses primitive ints for both keys and values.
 * It is much smaller and faster than using a java.util.Hashtable since you do not
 * have to wrap the primitive type in an Integer object. The current hash is hardcoded as 4
 * (to keep the memory usage low) but could conceivably be adjusted in a constructor if needed.
 * A valid key is any non-zero integer that can be used as an array index in Java.
 * A valid value is any integer.
 * This class is appropriately synchronized and can be considered threadsafe.
**/
final class IntegerHashtable implements java.io.Serializable
{
  static final long serialVersionUID = 5L;
  private static final int HASH = 4;
  final int[][] values_ = new int[HASH][];
  final int[][] keys_ = new int[HASH][];

  final int get(int key)
  {
    if (key == 0) return -1;
    int hash = key % HASH;
    synchronized(keys_)
    {
      int[] keyChain = keys_[hash];
      if (keyChain == null) return -1;
      for (int i=0; i<keyChain.length; ++i)
      {
        if (keyChain[i] == key)
        {
          return values_[hash][i];
        }
      }
    }
    return -1;
  }

  // Keys == 0 not allowed
  final void put(int key, int value)
  {
    if (key == 0) return;
    int hash = key % HASH;
    synchronized(keys_)
    {
      int[] valueChain = values_[hash];
      int[] keyChain = keys_[hash];
      if (keyChain == null)
      {
        keyChain = new int[] { key };
        valueChain = new int[] { value };
        keys_[hash] = keyChain;
        values_[hash] = valueChain;
        return;
      }
      else
      {
        int len = keyChain.length;
        for (int i=0; i<len; ++i)
        {
          if (keyChain[i] == key)
          {
            valueChain[i] = value;
            return;
          }
          if (keyChain[i] == 0)
          {
            keyChain[i] = key;
            valueChain[i] = value;
            return;
          }
        }
        int[] newKeyChain = new int[len*2];
        System.arraycopy(keyChain, 0, newKeyChain, 0, len);
        int[] newValueChain = new int[len*2];
        System.arraycopy(valueChain, 0, newValueChain, 0, len);
        newKeyChain[len] = key;
        newValueChain[len] = value;
        keys_[hash] = newKeyChain;
        values_[hash] = newValueChain;
      }
    }
  }
}
  
