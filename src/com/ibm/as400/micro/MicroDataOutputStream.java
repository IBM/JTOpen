///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MicroDataOutputStream.java
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
This class allows the DataOutputStream used for client/server
communication to be optionally traced.
**/
class MicroDataOutputStream 
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    DataOutputStream  out_;

    /**
    Constructor.
    **/
    public MicroDataOutputStream(OutputStream out) throws IOException 
    {
        out_ = new DataOutputStream(out);
    }


    /**
    Flush the underlying DataOutputStream.
    **/
    public void flush() throws IOException 
    {
        out_.flush();
    }


    /**
    Write a boolean to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeBoolean(boolean b) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + b);

        out_.writeBoolean(b);
    }


    /**   
    Write a byte to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeByte(byte b) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + b);

        out_.writeByte(b);
    }


    /**   
    Write an array of bytes to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeBytes(byte[] b) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, "  " + Thread.currentThread().getName() + " out < ", b);

        out_.write(b, 0, b.length);
    }

    
    /**
    Write a double to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeDouble(double d) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + d);

        out_.writeDouble(d);
    }


    /**
    Write a float to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeFloat(float f) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + f);

        out_.writeFloat(f);
    }      


    /**   
    Write an int to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeInt(int i) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + Integer.toHexString(i));

        out_.writeInt(i);
    }


    /**
    Write a long to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeLong(long l) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + l);

        out_.writeLong(l);
    }      


    /**
    Write a short to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeShort(short s) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + s);

        out_.writeShort(s);
    }


    /**
    Convenience method... the fact that a string gets put into UTF format is 
    just an implementation detail.
    **/
    public void writeString(String s) throws IOException 
    {
        writeUTF(s);
    }


    /**
    Write a String to the underlying DataOutputStream.  Optionally trace
    the value.
    **/
    public void writeUTF(String s) throws IOException 
    {
        if (Trace.isTraceOn())
            Trace.log(Trace.PROXY, Thread.currentThread().getName() + " out < " + s);

        out_.writeUTF(s);
    }
}
