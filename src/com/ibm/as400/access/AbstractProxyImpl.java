///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AbstractProxyImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.lang.reflect.InvocationTargetException;

// The AbstractProxyImpl class provides a default implementation for the ProxyImpl and ProxyFactoryImpl interfaces.
abstract class AbstractProxyImpl
/*ifdef JDBC40
extends ToolboxWrapper 
endif */ 
implements ProxyImpl, ProxyFactoryImpl
{
    static final String copyright = "Copyright (C) 1997-2010 International Business Machines Corporation and others.";

    // Private data.
    private String className_;
    protected ProxyClientConnection connection_;
    protected long pxId_;

    // Called for ProxyFactoryImpl objects:
    protected AbstractProxyImpl()
    {
    }

    // Called for ProxyImpl objects:
    protected AbstractProxyImpl(String className)
    {
        className_ = className;
    }

    // From the ProxyImpl interface:
    public void construct(ProxyClientConnection connection)
    {
        connection_ = connection;
        try
        {
            pxId_ = connection_.callConstructor(className_);
        }
        catch (InvocationTargetException e)
        {
            throw ProxyClientConnection.rethrow(e);
        }
    }

    // Cleans up the object.
    protected void finalize() throws Throwable
    {
        connection_.callFinalize(pxId_);
        super.finalize();
    }

    // From the ProxyImpl interface:
    public long getPxId()
    {
        return pxId_;
    }

    // From the AbstractProxyImpl interface:
    public void initialize(long pxId, ProxyClientConnection connection)
    {
        pxId_ = pxId;
        connection_ = connection;
    }
}
