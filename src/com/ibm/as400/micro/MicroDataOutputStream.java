//////////////////////////////////////////////////////////////////////
//
// IBM Confidential
//
// OCO Source Materials
//
// The Source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office
//
// 5722-JC1
// (C) Copyright IBM Corp. 2002
//
////////////////////////////////////////////////////////////////////////
//
// File Name:    MicroDataOutputStream.java
//
// Description:  See comments below
//
// Classes:      MicroDataOutputStream
//
////////////////////////////////////////////////////////////////////////
//
// CHANGE ACTIVITY:
//
//  Flg=PTR/DCR   Release       Date        Userid     Comments
//        D98585.1  v5r2m0.jacl  09/11/01  wiedrich    Created.
//
// END CHANGE ACTIVITY
//
////////////////////////////////////////////////////////////////////////
package com.ibm.as400.micro;

import java.io.*;
import com.ibm.as400.access.Trace;


/**
This class allows the DataOutputStream used for client/server
communication to be optionally traced.
**/
class MicroDataOutputStream 
{
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
