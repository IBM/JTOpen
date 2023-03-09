///////////////////////////////////////////////////////////////////////////////
//
//JTOpen (IBM Toolbox for Java - OSS version)                                 
//
//Filename: JDSQLXMLProxy.java
//
//The source code contained herein is licensed under the IBM Public License   
//Version 1.0, which has been approved by the Open Source Initiative.         
//Copyright (C) 2006-2006 International Business Machines Corporation and     
//others. All rights reserved.                                                
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
/* endif */ 
import java.sql.SQLException;




//@PDA jdbc40 new class

/**
 The JDSQLXMLProxy class provides access to character XML
 objects.  
 **/
class JDSQLXMLProxy
extends AbstractProxyImpl
/* ifdef JDBC40 */
implements SQLXML
/* endif */ 
{
    
    
    // Copied from JDError:
    static final String EXC_FUNCTION_NOT_SUPPORTED       = "IM001";
    
    public Reader getCharacterStream ()
    throws SQLException
    {
        try {
            JDReaderProxy newReader = new JDReaderProxy ();
            return (JDReaderProxy) connection_.callFactoryMethod (
                    pxId_, "getCharacterStream",
                    newReader);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    public Writer setCharacterStream ()
    throws SQLException
    {
        try {
            JDReaderProxy newReader = new JDReaderProxy ();
            return (JDWriterProxy) connection_.callFactoryMethod (
                    pxId_, "setCharacterStream",
                    newReader);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
    public void setString (String str)
    throws SQLException
    {
        try {
            connection_.callMethod (pxId_, "setString",
                    new Class[] { String.class},
                    new Object[] { str});
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
    public void free() throws SQLException
    {
        try {
            connection_.callMethod (pxId_, "free");
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
    
    
    public InputStream getBinaryStream() throws SQLException
    {
        try {
            JDInputStreamProxy newStream = new JDInputStreamProxy ();
            return (JDInputStreamProxy) connection_.callFactoryMethod (
                    pxId_, "getBinaryStream",
                    newStream);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
/* ifdef JDBC40 */
    public <T extends Source> T getSource(Class<T> sourceClass) throws SQLException
    {
        try
        {
            //not currently supported.  get exception from proxy server
            JDWriterProxy newWriter = new JDWriterProxy ();
            return (T) connection_.callFactoryMethod (pxId_, "getSource", 
                                                                        new Class[] { Class.class},
                                                                        new Object[] { sourceClass },
                                                                        newWriter);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
/* endif */ 
    
    public String getString() throws SQLException
    {
        try {
            return (String) connection_.callMethod (pxId_, "getString").getReturnValue();
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
    public OutputStream setBinaryStream() throws SQLException
    {
        try {
            JDOutputStreamProxy newStream = new JDOutputStreamProxy ();
            return (JDOutputStreamProxy) connection_.callFactoryMethod (
                    pxId_, "setBinaryStream",
                    newStream);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
    
/* ifdef JDBC40 */
    public <T extends Result> T setResult(Class<T> resultClass) throws SQLException
    {
        try
        {
            //not currently supported.  get exception from proxy server
            JDWriterProxy newWriter = new JDWriterProxy ();
            return (T) connection_.callFactoryMethod (pxId_, "setResult", 
                                                                        new Class[] { Class.class},
                                                                        new Object[] { resultClass },
                                                                        newWriter);
        }
        catch (InvocationTargetException e) {
            throw JDConnectionProxy.rethrow1 (e);
        }
    }
    
/* endif */ 
    
}
