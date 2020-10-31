///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MicroDataInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import com.ibm.as400.access.Trace;

/**
This class allows the DataInputStream used for client/server
communication to be optionally traced.
**/
class MicroDataInputStream 
{
    DataInputStream   in_;

    /**
    Constructor.
     * @param in 
     * @throws IOException 
    **/
    public MicroDataInputStream(InputStream in) throws IOException 
    {
        in_ = new DataInputStream(in);
    }


    /**
    Read a boolean from the underlying DataInputStream.  Optionally trace
    the value.
     * @return boolean 
     * @throws IOException 
    **/
    public boolean readBoolean() throws IOException 
    {
        boolean b = in_.readBoolean();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + b);

        return b;
    }


    /**   
    Read a byte from the underlying DataInputStream.  Optionally trace
    the value.
     * @return byte from the data stream
     * @throws IOException 
    **/
    public byte readByte() throws IOException 
    {
        byte b = in_.readByte();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + b);

        return b;
    }


    /**   
    Read a byte array from the underlying DataInputStream, storing it into the
    object that is passed in.  Optionally trace the value.
     * @param b 
     * @throws IOException 
    **/
    public void readBytes(byte[] b) throws IOException 
    {
        in_.readFully(b);

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "  " + Thread.currentThread().getName() + " in > ", b);
    }


    /**
    Read a double from the underlying DataInputStream.  Optionally trace
    the value.
     * @return double
     * @throws IOException 
    **/
    public double readDouble() throws IOException 
    {
        double d = in_.readDouble();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + d);

        return d;
    }


    /**
    Read a float from the underlying DataInputStream.  Optionally trace
    the value.
     * @return float
     * @throws IOException 
    **/
    public float readFloat() throws IOException 
    {
        float f = in_.readFloat();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + f);

        return f;
    }


    /**   
    Read an int from the underlying DataInputStream.  Optionally trace
    the value.
     * @return integer
     * @throws IOException 
    **/
    public int readInt() throws IOException 
    {
        int i = in_.readInt();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + Integer.toHexString(i));

        return i;
    }


    /**
    Read a long from the underlying DataInputStream.  Optionally trace
    the value.
     * @return long
     * @throws IOException 
    **/
    public long readLong() throws IOException 
    {
        long l = in_.readLong();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + l);

        return l;
    }


    /**
    Read a short from the underlying DataInputStream.  Optionally trace
    the value.
     * @return short
     * @throws IOException 
    **/
    public short readShort() throws IOException 
    {
        short s = in_.readShort();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + s);

        return s;
    }


    /**
    Convenience method... the fact that a string gets put into UTF format is 
    just an implementation detail.
     * @return String
     * @throws IOException 
    **/
    public String readString() throws IOException 
    {
        return readUTF();
    }


    /**
    Read a String from the underlying DataInputStream.  Optionally trace
    the value.
     * @return String from DataInputStream
     * @throws IOException 
    **/
    public String readUTF() throws IOException 
    {
        String s = in_.readUTF();

        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " in  > " + s);

        return s;
    }


    /**
    May be needed someday to allow us to skip over optional information 
    returned in the stream.
     * @param count bytes to skip
    @return The actual number of bytes skipped.
     * @throws IOException 
    **/
    public int skipBytes(int count) throws IOException
    {
        return in_.skipBytes(count);
    }
}
