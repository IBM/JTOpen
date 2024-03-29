///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400XChgRandSeedDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

// A class representing an "exchange random seed" request data stream.
class AS400XChgRandSeedDS extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
  private byte[] seed;
  
    AS400XChgRandSeedDS(int serverId)
    {
        super(new byte[28]);
        setLength(28);
        // Header ID replaced with Attributes.
        //data_[4] = 0x01;  // Client Attributes, 1 means can use SHA-1. @AF2D 
        data_[4] = 0x02;    // Client Attributes, 1 means can use SHA-2 512, password level 4. @AF2A
        // data_[5] = 0x00;  // Server Attributes.
        setServerID(serverId);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(8);
        setReqRepID(0x7001);

        // We generate a "random" seed using the current time in milliseconds.
        // This seed will be used to encrypt the password.
        long t = System.currentTimeMillis();

        // Performance: break into 2 ints first and avoid long temporaries.
        int high = (int)(t >>> 32);
        int low = (int)t;

        data_[20] = (byte)(high >>> 24);
        data_[21] = (byte)(high >>> 16);
        data_[22] = (byte)(high >>> 8);
        data_[23] = (byte)high;

        data_[24] = (byte)(low >>> 24);
        data_[25] = (byte)(low >>> 16);
        data_[26] = (byte)(low >>> 8);
        data_[27] = (byte)low;
    }
    
    AS400XChgRandSeedDS(int serverId, boolean isHCS)
    {
      super(new byte[34]);
      setLength(34);
      setHeaderID(0x00000000);
      setServerID(serverId);
      setTemplateLen(0);
      setReqRepID(0x7103);
      
      // Optional Parameters
      set32bit(14, 20);
      set16bit(0x1103, 24);

      // We generate a "random" seed using the current time in milliseconds.
      // This seed will be used to encrypt the password.
      long t = System.currentTimeMillis();

      // Performance: break into 2 ints first and avoid long temporaries.
      int high = (int)(t >>> 32);
      int low = (int)t;

      data_[26] = (byte)(high >>> 24);
      data_[27] = (byte)(high >>> 16);
      data_[28] = (byte)(high >>> 8);
      data_[29] = (byte)high;

      data_[30] = (byte)(low >>> 24);
      data_[31] = (byte)(low >>> 16);
      data_[32] = (byte)(low >>> 8);
      data_[33] = (byte)low;
      
      seed = new byte[8];
      System.arraycopy(data_, 26, seed, 0, 8);
    }

    byte[] getClientSeed()
    {
        byte[] seed = new byte[8];
        System.arraycopy(data_, 20, seed, 0, 8);
        return seed;
    }
    
    byte[] getHCSClientSeed()
    {
      return seed;
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending exchange random seeds request..."); //@P0C
        super.write(out);
    }
}
