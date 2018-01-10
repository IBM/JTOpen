///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JobHashtable.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

// This class represents a hashtable that uses primitive ints for both keys and regular Objects for values.  It is used primarily in the Job and JobList classes, but is general-purpose and can be used elsewhere if desired.  It is much smaller and faster than using a java.util.Hashtable since you do not have to wrap the primitive type in an Integer object.  The current hash is hardcoded as 4 (to keep the memory usage low) but could conceivably be adjusted in a constructor if needed.  A valid key is any non-zero integer that can be used as an array index in Java.  A valid value is any Object, including null.  This class is appropriately synchronized and can be considered threadsafe.
final class JobHashtable implements Serializable
{
    static final long serialVersionUID = 5L;
    private static final int HASH = 4;
    final Object[][] values_ = new Object[HASH][];
    final int[][] keys_ = new int[HASH][];
    int size_ = 0;

    final void clear()
    {
        for (int i = 0; i < HASH; ++i)
        {
            values_[i] = null;
            keys_[i] = null;
        }
        size_ = 0;
    }

    final boolean contains(int key)
    {
        if (key == 0) return false;
        int hash = key % HASH;
        int[] keyChain = keys_[hash];
        if (keyChain == null) return false;
        for (int i = 0; i < keyChain.length; ++i)
        {
            if (keyChain[i] == key)
            {
                return true;
            }
        }
        return false;
    }

    final Object get(int key)
    {
        if (key == 0) return null;
        int hash = key % HASH;
        synchronized (keys_)
        {
            int[] keyChain = keys_[hash];
            if (keyChain == null) return null;
            for (int i = 0; i < keyChain.length; ++i)
            {
                if (keyChain[i] == key)
                {
                    return values_[hash][i];
                }
            }
        }
        return null;
    }

    // Keys == 0 not allowed.
    final void put(int key, Object value)
    {
        if (key == 0) return;
        int hash = key % HASH;
        synchronized (keys_)
        {
            Object[] valueChain = values_[hash];
            int[] keyChain = keys_[hash];
            if (keyChain == null)
            {
                keyChain = new int[] { key };
                valueChain = new Object[] { value };
                keys_[hash] = keyChain;
                values_[hash] = valueChain;
                ++size_;
                return;
            }
            else
            {
                int len = keyChain.length;
                for (int i = 0; i < len; ++i)
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
                        ++size_;
                        return;
                    }
                }
                int[] newKeyChain = new int[len * 2];
                System.arraycopy(keyChain, 0, newKeyChain, 0, len);
                Object[] newValueChain = new Object[len * 2];
                System.arraycopy(valueChain, 0, newValueChain, 0, len);
                newKeyChain[len] = key;
                newValueChain[len] = value;
                keys_[hash] = newKeyChain;
                values_[hash] = newValueChain;
                ++size_;
            }
        }
    }

    final Object remove(int key)
    {
        if (key == 0) return null;
        int hash = key % HASH;
        synchronized (keys_)
        {
            int[] keyChain = keys_[hash];
            if (keyChain == null) return null;
            for (int i = 0; i < keyChain.length; ++i)
            {
                if (keyChain[i] == key)
                {
                    // Remove the key and value, and collapse the chains.
                    Object value = values_[hash][i];
                    int j;
                    for (j = i + 1; j < keyChain.length; ++j)
                    {
                        // Collapse chains.
                        keyChain[j - 1] = keyChain[j];
                        values_[hash][j - 1] = values_[hash][j];
                        if (keyChain[j] == 0) break;  // Don't bother collapsing zeros.
                    }
                    // If key was in final or next-to-final slot, clean up final slot.
                    if (j == keyChain.length)
                    {
                        keyChain[j - 1] = 0;
                        values_[hash][j - 1] = null;
                    }

                    --size_;
                    return value;
                }
            }
        }
        return null;
    }
}
