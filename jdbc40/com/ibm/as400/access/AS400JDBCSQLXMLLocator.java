///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCSQLXMLLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

//import com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser;


//@PDA jdbc40 new class
/**
<p>The AS400JDBCSQLXMLLocator class provides the object interface for usin XML data through JDBC.
The mapping in the JavaTM programming language for the SQL XML type. 
XML is a built-in type that stores an XML value as a column value in a row of a database table. 
The SQLXML interface provides methods for accessing the XML value as a String, a Reader or Writer, or as a Stream. 
**/

public class AS400JDBCSQLXMLLocator extends AS400JDBCClobLocator implements SQLXML
{
   

    /**
    Constructs an AS400JDBCSQLXMLLocator object.  The data for the
    SQLXML will be retrieved as requested, directly from the
    IBM i system, using the locator handle.
    
    @param  locator             The locator.
    @param  converter           The text converter.
    @param  savedObject         Saved Object.
    @param  savedScale          Saved scale.
    **/
    AS400JDBCSQLXMLLocator(JDLobLocator locator, ConvTable converter, Object savedObject, int savedScale)
    {
        super(locator, converter, savedObject, savedScale);
    }

    /**
     * Retrieves the XML value designated by this SQLXML instance as a stream. 
     * @return a stream containing the XML data.
     * @throws SQLException if there is an error processing the XML value.
     *   An exception is thrown if the state is not readable.
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public InputStream getBinaryStream() throws SQLException
    {        
        synchronized(locator_)
        {
          try
          {
            return new ReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(locator_), converter_.getCcsid(), converter_.bidiStringType_), 13488); 
          }
          catch (UnsupportedEncodingException e)
          {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
          }
        }
    }
   

    /**
     * Returns a Source for reading the XML value designated by this SQLXML instance.
     * Sources are used as inputs to XML parsers and XSLT transformers.
     * @param sourceClass The class of the source, or null.  
     * If the class is null, a vendor specifc Source implementation will be returned.
     * The following classes are supported at a minimum:
     * <pre>
     *   javax.xml.transform.dom.DOMSource - returns a DOMSource
     *   javax.xml.transform.sax.SAXSource - returns a SAXSource
     *   javax.xml.transform.stax.StAXSource - returns a StAXSource
     *   javax.xml.transform.stream.StreamSource - returns a StreamSource
     * </pre>
     * @return a Source for reading the XML value.
     * @throws SQLException if there is an error processing the XML value
     *   or if this feature is not supported.
     *   The getCause() method of the exception may provide a more detailed exception, for example,
     *   if an XML parser exception occurs. 
     *   An exception is thrown if the state is not readable.
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public synchronized <T extends Source> T getSource(Class<T> sourceClass) throws SQLException
    {
        
        try
        {
            if (sourceClass == javax.xml.transform.dom.DOMSource.class)
            {
                DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = parser.parse(this.getBinaryStream());
                return (T) new DOMSource(doc);
            } else if (sourceClass == javax.xml.transform.sax.SAXSource.class)
            {      
                return (T) new SAXSource( new InputSource(this.getBinaryStream()));

            } else if (sourceClass == javax.xml.transform.stax.StAXSource.class)
            {
                JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            	//return (T) new StAXSource( new StAXDocumentParser(this.getBinaryStream())); //this does not compile on sun 1.6 unless rt.jar is in classpath...strange
            } else if (sourceClass == javax.xml.transform.stream.StreamSource.class)
            {
                return (T) new StreamSource(this.getBinaryStream());
            }
        } catch (Exception e)
        {
            JDError.throwSQLException(JDError.EXC_INTERNAL, e);
           
        }
        return null;
    }


    /**
     * Retrieves a string representation of the XML value designated by this
     * <code>SQLXML</code> object.
     * @return a String that is the string representation of the XML value
     * @throws SQLException if there is an error accessing the XML value
     */
    public synchronized String getString() throws SQLException
    {
        synchronized(locator_)
        {
            int offset = 0;
            int lengthToUse = (int)length();
          
            DBLobData data = locator_.retrieveData(offset, lengthToUse);
            int actualLength = data.getLength();
            return converter_.byteArrayToString(data.getRawBytes(), data.getOffset(), actualLength);
        }
    }

    /**
     * Writes the given Java String to the XML value that this <code>SQLXML</code> object designates.
     * @param str the string to be written to the XML value that this <code>SQLXML</code> designates
     * @throws SQLException if there is an error accessing the XML value
     */
    public synchronized void setString(String str) throws SQLException
    {
        setString(1, str, 0, str.length());
    }

    public OutputStream setBinaryStream() throws SQLException
    {
        try
        {
          return new AS400JDBCClobLocatorOutputStream(this, 1, ConvTable.getTable(819, null));
        }
        catch (UnsupportedEncodingException e)
        {
          // Should never happen.
          JDError.throwSQLException(JDError.EXC_INTERNAL, e);
          return null;
        }
    }

    /**
     * Retrieves a Writer to be used to write the XML value that this SQLXML instance represents. 
     * @return a stream to which data can be written.
     * @throws SQLException if there is an error processing the XML value.
     *   The getCause() method of the exception may provide a more detailed exception, for example,
     *   if the stream does not contain valid characters. 
     *   An exception is thrown if the state is not writable.
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public Writer setCharacterStream() throws SQLException
    {
        return new AS400JDBCWriter(this, 1);  
    }

    /**
     * Returns a Result for setting the XML value designated by this SQLXML instance.
     * @param resultClass The class of the result, or null.  
     * If resultClass is null, a vendor specific Result implementation will be returned.
     * The following classes are supported at a minimum:
     * <pre>
     *   javax.xml.transform.dom.DOMResult - returns a DOMResult
     *   javax.xml.transform.sax.SAXResult - returns a SAXResult
     *   javax.xml.transform.stax.StAXResult - returns a StAXResult
     *   javax.xml.transform.stream.StreamResult - returns a StreamResult
     * </pre>
     * @return Returns a Result for setting the XML value.
     * @throws SQLException if there is an error processing the XML value
     *   or if this feature is not supported.
     *   The getCause() method of the exception may provide a more detailed exception, for example,
     *   if an XML parser exception occurs. 
     *   An exception is thrown if the state is not writable.
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public synchronized <T extends Result> T setResult(Class<T> resultClass) throws SQLException
    {
        //todo how do we implement this method?
        //how to have a Result that hooks into a clob for writes?
        //have to extend DOMResult etc???
        //throw exception for now
        JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
        return null;
    }

}
