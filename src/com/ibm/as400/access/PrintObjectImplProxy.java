///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectImplProxy.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * The PrintObjectImplProxy class implements proxy versions of
 * the public methods defined in the PrintObjectImpl class.
 * Unless commented otherwise, the implementations of the methods below
 * are merely proxy calls to the corresponding method in the remote
 * implementation class (PrintObjectImplRemote).
 **/

abstract class PrintObjectImplProxy extends AbstractProxyImpl
implements PrintObjectImpl, ProxyImpl
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    PrintObjectImplProxy(String className)
    {
        super(className);
    }



    /**
     * Invokes getAttrValue() on the server to retrieve the PrintObject
     * attributes.
     **/
    public NPCPAttribute getAttrValue()
    {
        try {
            return (NPCPAttribute) connection_.callMethod(pxId_,
                                                          "getAttrValue").getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }



    public Integer getIntegerAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException
    {
        try {
            return (Integer) connection_.callMethod(pxId_, "getIntegerAttribute",
                                                    new Class[] { Integer.TYPE },
                                                    new Object[] { new Integer (attributeID)}).getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public Float getFloatAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException
    {
        try {
            return (Float) connection_.callMethod(pxId_, "getFloatAttribute",
                                                  new Class[] { Integer.TYPE },
                                                  new Object[] { new Integer (attributeID)}).getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public String getStringAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException
        {
        try {
            return (String) connection_.callMethod(pxId_, "getStringAttribute",
                                                   new Class[] { Integer.TYPE },
                                                   new Object[] { new Integer (attributeID)}).getReturnValue();
        }
        catch (InvocationTargetException e) {
                throw ProxyClientConnection.rethrow6a(e);
        }
    }


    public Integer getSingleIntegerAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException
    {
        try {
            return (Integer) connection_.callMethod(pxId_, "getIntegerAttribute",
                                                    new Class[] { Integer.TYPE },
                                                    new Object[] { new Integer (attributeID)}).getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public Float getSingleFloatAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException
    {
        try {
            return (Float) connection_.callMethod(pxId_, "getFloatAttribute",
                                                  new Class[] { Integer.TYPE },
                                                  new Object[] { new Integer (attributeID)}).getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }



    public String getSingleStringAttribute(int attributeID)
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException
        {
        try {
            return (String) connection_.callMethod(pxId_, "getStringAttribute",
                                                   new Class[] { Integer.TYPE },
                                                   new Object[] { new Integer (attributeID)}).getReturnValue();
        }
        catch (InvocationTargetException e) {
                throw ProxyClientConnection.rethrow6a(e);
        }
    }


    public void setPrintObjectAttrs(NPCPID idCodePoint,
                                    NPCPAttribute cpAttrs,
                                    int type)
    {
        try {
            connection_.callMethod (pxId_, "setPrintObjectAttrs",
                            new Class[] { NPCPID.class, NPCPAttribute.class, Integer.TYPE },
                            new Object[] { idCodePoint, cpAttrs, new Integer (type)});
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }



    final public void setSystem(AS400Impl system)
    {
        try {
            connection_.callMethod (pxId_, "setSystem",
                                    new Class[] { AS400Impl.class },
                                    new Object[] { system });
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow(e);
        }
    }



    public void update()
        throws AS400Exception,
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException
    {
        try {
            connection_.callMethod(pxId_, "update");
        }
        catch (InvocationTargetException e) {
            throw ProxyClientConnection.rethrow6a(e);
        }
    }

}
