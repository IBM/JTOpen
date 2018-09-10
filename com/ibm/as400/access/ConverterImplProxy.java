///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConverterImplProxy.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;
import java.io.UnsupportedEncodingException;

class ConverterImplProxy extends AbstractProxyImpl implements ConverterImpl
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    ConverterImplProxy()
    {
        super("Converter");
    }

    public void setEncoding(String encoding) throws UnsupportedEncodingException
    {
        try
        {
            connection_.callMethod(pxId_, "setEncoding", new Class[] { String.class}, new Object[] { encoding });
        }
        catch (InvocationTargetException e)
        {
            Throwable target = e.getTargetException();
            if (target instanceof UnsupportedEncodingException) throw (UnsupportedEncodingException) target;
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public void setCcsid(int ccsid, AS400Impl systemImpl) throws UnsupportedEncodingException
    {
        try
        {
            connection_.callMethod(pxId_, "setCcsid", new Class[] { Integer.TYPE, AS400Impl.class }, new Object[] { new Integer(ccsid), systemImpl });
        }
        catch (InvocationTargetException e)
        {
            Throwable target = e.getTargetException();
            if (target instanceof UnsupportedEncodingException) throw (UnsupportedEncodingException) target;
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public String getEncoding()
    {
        try
        {
            return(String)connection_.callMethod(pxId_, "getEncoding").getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public int getCcsid()
    {
        try
        {
            return connection_.callMethod(pxId_, "getCcsid").getReturnValueInt();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public String byteArrayToString(byte[] source, int offset, int length)
    {
        try
        {
            return(String)connection_.callMethod(pxId_, "byteArrayToString", new Class[] { byte[].class, Integer.TYPE, Integer.TYPE}, new Object[] { source, new Integer(offset), new Integer(length)}).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public String byteArrayToString(byte[] source, int offset, int length, BidiConversionProperties properties)
    {
        try
        {
            return(String)connection_.callMethod(pxId_, "byteArrayToString", new Class[] { byte[].class, Integer.TYPE, Integer.TYPE, BidiConversionProperties.class }, new Object[] { source, new Integer(offset), new Integer(length), properties }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public byte[] stringToByteArray(String source)
    {
        try
        {
            return(byte[])connection_.callMethod(pxId_, "stringToByteArray", new Class[] { String.class}, new Object[] { source }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    public byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        try
        {
            return(byte[])connection_.callMethod(pxId_, "stringToByteArray", new Class[] { String.class, BidiConversionProperties.class}, new Object[] { source, properties }).getReturnValue();
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }
}
