///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCSQLXML.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
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
<p>The AS400JDBCSQLXML class provides the object interface for using XML data through JDBC.
The mapping in the JavaTM programming language for the SQL XML type. 
XML is a built-in type that stores an XML value as a column value in a row of a database table. 
The SQLXML interface provides methods for accessing the XML value as a String, a Reader or Writer, or as a Stream. 
**/
public class AS400JDBCSQLXML extends AS400JDBCClob implements SQLXML
{ 
   
    static final int MAX_XML_SIZE = 2147483647;

    /**
     Constructs an AS400JDBCSQLXML object.  The data is contained
     in the String.  No further communication with the IBM i system is necessary.

     @param  data     The SQLXML data.
     @param maxLength Max length.
     **/
    AS400JDBCSQLXML(String data, int maxLength)
    {
        super(data, maxLength);
    }

    /**
     Constructs an AS400JDBCSQLXML object.  The data is contained
     in the String.  No further communication with the IBM i system is necessary.

     @param  data     The SQLXML data.
     **/
    AS400JDBCSQLXML(char[] data)
    {
        super(data);
    }

    
    /**
     * Retrieves the XML value designated by this SQLXML instance as a java.io.Reader object.
     * The format of this stream is defined by org.xml.sax.InputSource,
     * where the characters in the stream represent the unicode code points for  
     * XML according to section 2 and appendix B of the XML 1.0 specification.
     * Although an encoding declaration other than unicode may be present, 
     * the encoding of the stream is unicode.
     * The behavior of this method is the same as ResultSet.getCharacterStream()
     * when the designated column of the ResultSet has a type java.sql.Types of SQLXML.
     * <p>
     * The SQL XML object becomes not readable when this method is called and
     * may also become not writable depending on implementation.
     * 
     * @return a stream containing the XML data.
     * @throws SQLException if there is an error processing the XML value.
     *   The getCause() method of the exception may provide a more detailed exception, for example,
     *   if the stream does not contain valid characters. 
     *   An exception is thrown if the state is not readable.
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     */
    public synchronized Reader getCharacterStream() throws SQLException
    {
        return new CharArrayReader(data_, 0, data_.length);
    }

    /**
     * Returns a writer that will populate the stream with XML events. This is empty until the
     * returned writer's close method is called.
     * @return a <code>javax.xml.stream.XMLStreamWriter</code> object
     * @throws SQLException if the receiver is not empty
     */
    /* removed from jdbc40 spec
    public XMLStreamWriter createXMLStreamWriter() throws SQLException
    {
        return new XMLStreamWriterImpl(new AS400JDBCWriter(this, 1), new PropertyManager(PropertyManager.CONTEXT_WRITER));
    } */

    /**
     * Retrieves a string representation of the XML value designated by this
     * <code>SQLXML</code> object.
     * @return a String that is the string representation of the XML value
     * @throws SQLException if there is an error accessing the XML value
     */
    public synchronized String getString() throws SQLException
    {
        return new String(data_);
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

    
    /**
     * Denotes whether the <code>SQLXML</code> object is empty.
     * @return true if the object is empty; false otherwise
     * @throws SQLException if there is an error accessing the XML value
     */
    /* removed from jdbc40 spec 
     
     public synchronized boolean isEmpty() throws SQLException
    {
        if (data_ == null || data_.length == 0)
            return true;
        else
            return false;
    }*/
 
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
        try
        {
          return new ByteArrayInputStream((new String(data_)).getBytes("UTF-16"));
        }
        catch (UnsupportedEncodingException e)
        {
          JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
          return null;
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
     * Retrieves a stream that can be used to write the XML value that this SQLXML instance represents.
     * @return a stream to which data can be written.
     * @throws SQLException
     *             if there is an error processing the XML value. An exception
     *             is thrown if the state is not writable.
     * @exception SQLFeatureNotSupportedException
     *                if the JDBC driver does not support this method
     */
    public synchronized OutputStream setBinaryStream() throws SQLException
    {
        try
        {
          return new AS400JDBCClobOutputStream(this, 1, ConvTable.getTable(13488, null)); 
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
    public synchronized Writer setCharacterStream() throws SQLException
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
