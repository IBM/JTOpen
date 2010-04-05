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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
/* endif */ 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/* ifdef JDBC40 */
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
/* endif */ 
import javax.xml.transform.Result;
/* ifdef JDBC40 */
import javax.xml.transform.Source;
/* endif */ 
import javax.xml.transform.dom.DOMResult;
/* ifdef JDBC40 */
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import org.xml.sax.InputSource;
/* endif */ 

import javax.xml.parsers.ParserConfigurationException;
/* ifdef JDBC40 */
import org.xml.sax.SAXException;
/* endif */ 
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

//@PDA jdbc40 new class
//@xml2 whole class is redesigned after that of The Native Driver
/**
This class provides the object interface for using XML data through JDBC.
The mapping in the Java programming language for the SQL XML type. 
XML is a built-in type that stores an XML value as a column value in a row of a database table. 
The SQLXML interface provides methods for accessing the XML value as a String, a Reader or Writer, or as a Stream. 
This class returns the data as an XML type.  The actual type on the host may vary.
Instances of this class are created by AS400JDBCConnection.
This class should not be used if JDK 1.6 is not in use. 
**/
public class AS400JDBCSQLXML
/* ifdef JDBC40 */
implements SQLXML  
/* endif */ 

{ 
   
    static final int MAX_XML_SIZE = AS400JDBCDatabaseMetaData.MAX_LOB_LENGTH; //@xml3
    // We may internally store the SQLXML object as a DOM document
    // if getResult was used to create the object
    static final int DOM_DOCUMENT = 22345;
    static final int LOB_FREED    = 0; 
    int lobType = LOB_FREED;   //corresponding to SQLData numbers
    
    AS400JDBCClob   clobValue_ = null;                
    AS400JDBCBlob   blobValue_ = null;                
    AS400JDBCClobLocator  clobLocatorValue_ = null;   
    AS400JDBCBlobLocator  blobLocatorValue_ = null;   
    
    /* Document available in JDK 1.4, so this cannot be moved back to  */
    /* V5R4 which still supports JDK 1.3.  In V5R4, this will need to  */
    /* be JDBC 4.0 only */ 
    org.w3c.dom.Document  domDocument_ = null;
    
    //Today, the only case where isXML_ can be true is if this is a AS400JDBCSQLXMLLocator 
    //(since xml columns are always returned as locators from hostserver)
    protected boolean isXML_ = false;      //@xml4 true if this data originated from a native XML column type
    
    /**
     * Constructs an AS400JDBCSQLXML object.  This is only a dummy constructor used by subclasses.
     */
    protected AS400JDBCSQLXML()
    {
    }
    
    /**
     * Constructs an AS400JDBCSQLXML object.  The data is contained
     * in the String.  No further communication with the IBM i system is necessary.
     *
     * @param  data     The SQLXML data.
     * @param maxLength Max length.
     */
    AS400JDBCSQLXML(String data, int maxLength)
    {
        isXML_ = true;//@xmltrim (match native jdbc for trimming xml decl if using sqlxml)
        
        lobType   = SQLData.CLOB;                         
        clobValue_ = new AS400JDBCClob(data, maxLength, isXML_); //@xmltrim   
        
    }

    /**
     * Constructs an AS400JDBCSQLXML object.  The data is contained
     * in the String.  No further communication with the IBM i system is necessary.
     *
     * @param  data     The SQLXML data.
     */
    AS400JDBCSQLXML(char[] data)
    {
        isXML_ = true;//@xmltrim (match native jdbc for trimming xml decl if using sqlxml)
        
        lobType   = SQLData.CLOB;                         
        clobValue_ = new AS400JDBCClob(data, isXML_); //@xmltrim              
    }


    /**
     * Constructs an AS400JDBCSQLXML object.  The data is contained
     * in the array.  No further communication with the IBM i system is necessary.
     * This constructor is used for returning blob data in an XML object.
     *
     * @param  data     The SQLXML data.
     * @param maxLength Max length.
     */
    AS400JDBCSQLXML (byte [] data, long maxLength)         
    {
        isXML_ = true;//@xmltrim (match native jdbc for trimming xml decl if using sqlxml)
        
        lobType   = SQLData.BLOB;                              
        blobValue_ = new AS400JDBCBlob(data, (int)maxLength);   
    } 

    
    /**
     * Retrieves the XML value designated by this SQLXML instance as a java.io.Reader object.
     * The format of this stream is defined by org.xml.sax.InputSource,
     * where the characters in the stream represent the Unicode code points for  
     * XML according to section 2 and appendix B of the XML 1.0 specification.
     * Although an encoding declaration other than Unicode may be present, 
     * the encoding of the stream is Unicode.
     * The behavior of this method is the same as ResultSet.getCharacterStream()
     * when the designated column of the ResultSet has a type java.sql.Types of SQLXML.
     * <p>
     * The SQL XML object becomes not readable when this method is called and
     * may also become not writable depending on implementation.
     * 
     * @return a stream containing the XML data.
     * @exception SQLException if there is an error processing the XML value.
     *   The getCause() method of the exception may provide a more detailed exception, for example,
     *   if the stream does not contain valid characters. 
     *   An exception is thrown if the state is not readable.
     */
    public synchronized Reader getCharacterStream() throws SQLException
    {
        Reader r = null;
        if (lobType == LOB_FREED) {
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
            return null;
        }
        
        switch (lobType)
        {

            case SQLData.CLOB:
            case SQLData.DBCLOB:
                r = clobValue_.getCharacterStream();
                break;
            case SQLData.CLOB_LOCATOR:
            case SQLData.DBCLOB_LOCATOR:
                r = clobLocatorValue_.getCharacterStream();
                break;
            case SQLData.BLOB:
            case SQLData.BLOB_LOCATOR:
            case DOM_DOCUMENT:
                // Calling getString will do the appropriate conversion
                String stringValue = this.getString();
                r = new StringReader(stringValue); 
                break;
            default:
            {
                JDError.throwSQLException(this, JDError.EXC_INTERNAL, "Invalid value: "+ lobType);
                return null;
            }
        }

        return r;
    }


    /**
     * Retrieves a string representation of the XML value designated by this
     * <code>SQLXML</code> object.
     * @return a String that is the string representation of the XML value
     * @throws SQLException if there is an error accessing the XML value
     */
    public synchronized String getString() throws SQLException
    {
        String s = null;

        switch (lobType)
        {
            case SQLData.DBCLOB:
            case SQLData.CLOB:  
                s = clobValue_.getSubString((long) 1, (int) clobValue_.length());
                break;
            case SQLData.DBCLOB_LOCATOR: 
            case SQLData.CLOB_LOCATOR:  
                s = clobLocatorValue_.getSubString((long) 1, (int) clobLocatorValue_.length());
                break;
            case SQLData.BLOB: 
            case SQLData.BLOB_LOCATOR:

                // Check for a byte order mark
                String encoding;
                int byteOrderMarkSize = 0;
                byte[] bytes = null;
                if (lobType == SQLData.BLOB)
                {
                    if(blobValue_.length() > 4)//@xmlzero
                        bytes = blobValue_.getBytes(1, 4);
                    else
                        bytes = new byte[0]; //@xmlzero
                        
                } else
                {
                    InputStream is = blobLocatorValue_.getBinaryStream();
                    bytes = new byte[4];
                    try
                    {
                        int bytesRead = is.read(bytes);
                        if (bytesRead == 0)
                            bytes = new byte[0];
                    } catch (Exception e)
                    {
                        JDError.throwSQLException(this, JDError.EXC_INTERNAL);
                        return null;
                    }finally
                    {
                        try{
                            is.close();
                        }catch(Exception e){ }
                    }
                }
                if (bytes.length < 4)
                {
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    return null;
                }

                if ((bytes[0] == (byte) 0xEF) && (bytes[1] == (byte) 0xBB)
                        && (bytes[2] == (byte) 0xBF))
                {
                    encoding = "UTF-8";
                    byteOrderMarkSize = 3;
                } else if ((bytes[0] == (byte) 0xFE)
                        && (bytes[1] == (byte) 0xFF))
                {
                    encoding = "UTF-16BE";
                    byteOrderMarkSize = 2;
                } else if ((bytes[0] == (byte) 0xFF)
                        && (bytes[1] == (byte) 0xFE))
                {
                    encoding = "UTF-16LE";
                    byteOrderMarkSize = 2;
                } else
                {
                    // We don't support the UCS-4 variants. In the future, we
                    // may need to.

                    // In this case we need to peek at the stream to determine
                    // which format it is in.
                    // At a minimum, we expect it to begin with '<' (after
                    // ignoring possible white space)
                    //
                    int position = 1;
                    byte nonWhiteSpaceByte = 0x20;
                    int i = 0;
                    while (nonWhiteSpaceByte == 0x20)
                    {
                        // Get the first few bytes of the blob in order to
                        // determine the encoding to use.
                        long len = -1;
                        if (lobType == SQLData.BLOB)
                        {
                            len = blobValue_.length();
                            if(len > 80)
                                len = 80;
                            bytes = blobValue_.getBytes(position, (int)len);
                        } else
                        {
                            InputStream is = blobLocatorValue_.getBinaryStream();
                            bytes = new byte[80];
                            try
                            {
                                int bytesRead = is.read(bytes);
                                if (bytesRead == 0)
                                    bytes = new byte[0];
                            } catch (Exception e)
                            {
                                JDError.throwSQLException(this, JDError.EXC_INTERNAL);
                                return null;
                            }finally
                            {
                                try{
                                    is.close();
                                }catch(Exception e){ }
                            }
                        }
                        position += len-1;
                        if (bytes.length == 0)
                        {
                            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                            return null;
                        }
                        while (i < bytes.length - 1
                                && ((bytes[i] == 0x20) || (bytes[i] == 0x40) || /* space */
                                (bytes[i] == 0x09) || (bytes[i] == 0x05) || /* tab */
                                (bytes[i] == 0x0d) || /* CR */
                                (bytes[i] == 0x0a) || bytes[i] == 0x15 /* LF */
                                || bytes[i] == 0))
                        {
                            if (bytes[i] == 0)
                            {
                                i++;
                                if ((bytes[i] == 0x20) || /* space */
                                (bytes[i] == 0x09) || /* table */
                                (bytes[i] == 0x0d) || /* CR */
                                (bytes[i] == 0x0a)) /* LF */
                                {
                                    i++;
                                } else
                                {
                                    i--;
                                    nonWhiteSpaceByte = 0;
                                    break;
                                }
                            } else
                            {
                                // If LE encoding..
                                if (bytes[i + 1] == 0)
                                {
                                    i++;
                                }
                                i++;
                            }
                        }
                        if (i < (bytes.length - 1))
                            nonWhiteSpaceByte = bytes[i];
                    } /* while looking for nonspace byte */
                    /* We need to look at the next byte */
                    byte nextByte;
                    i++;
                    nextByte = bytes[i];

                    // Determine what the encoding is
                    if (nonWhiteSpaceByte == 0x3c)
                    {
                        if (nextByte == 0x00)
                        {
                            encoding = "UTF-16LE";
                        } else
                        {
                            encoding = "UTF-8";
                        }
                    } else if (nonWhiteSpaceByte == 0x00)
                    {
                        if (nextByte == 0x3c)
                        {
                            encoding = "UTF-16BE";
                        } else
                        {   
                            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                            return null;
                        }
                    } else if (nonWhiteSpaceByte == 0x4C /* < */)
                    {
                        //
                        // Determine the internal encoding. If it cannot be
                        // determined then use CCSID 37

                        encoding = getInternalEncodingFromEbcdic(bytes);
                        if (encoding == null)
                        {
                            encoding = "IBM-37";
                        }

                    } else
                    {
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                        return null;
                    } // checking first byte
                } // determine encoding
                // At this point the encoding is set and we can obtain the
                // string
                InputStream stream = null;
                BufferedReader reader = null;
                StringBuffer sb = new StringBuffer();
                try
                {
                    if (lobType == SQLData.BLOB)
                    {
                        stream = blobValue_.getBinaryStream();
                    } else
                    {
                        stream = blobLocatorValue_.getBinaryStream();
                    }
                    while (byteOrderMarkSize > 0)
                    {
                        try
                        {
                            stream.read();
                        } catch ( IOException ioex)
                        {
                            // shouldn't ever happen since we've already looked at
                            // this.
                            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                            return null;
                        }
                        byteOrderMarkSize--;
                    }

                    try
                    { 
                        reader = new BufferedReader(new InputStreamReader(stream,
                                encoding));
                    } catch (UnsupportedEncodingException e)
                    {
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                        return null;
                    }


                    String line;
                    try
                    {
                        line = reader.readLine();
                        if (line != null)
                        {
                            sb.append(line);
                        }
                        line = reader.readLine();
                        while (line != null)
                        {
                            sb.append("\n");
                            sb.append(line);
                            line = reader.readLine();
                        }
                    } catch (IOException e)
                    {
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                        return null;
                    }
                } catch (Exception e)
                {
                    JDError.throwSQLException(this, JDError.EXC_INTERNAL);
                    return null;
                }finally
                {
                    try{                        
                        stream.close();
                    }catch(Exception e){ }
                    try{                        
                        reader.close();
                    }catch(Exception e){ }
                }
                s = sb.toString();

                break; // end of BLOB case
            case DOM_DOCUMENT:

                DOMImplementation implementation = domDocument_.getImplementation();

                DOMImplementationLS domImplementationLS = (DOMImplementationLS) implementation
                    .getFeature("LS", "3.0");

                LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
                s  = lsSerializer.writeToString(domDocument_);

                break;
            case 0:
                //freed already
                JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
                return null;
            default:
            {
                JDError.throwSQLException(this, JDError.EXC_INTERNAL, "Invalid value: "+ lobType);
                return null;
            }
        }
        if(isXML_)//@xmltrim
            return JDUtilities.stripXMLDeclaration(s); //@xmltrim
        else
            return s;
    }
    

    /**
    Retrieves the internal encoding specified in the bytes that represent the beginning of an 
    XML document.  If no internal encoding is present, then null is returned.
    **/
    private String getInternalEncodingFromEbcdic(byte[] bytes)
    {
        /* EBCDIC encoding for CCSID 37 and CCSID 290 for "encoding=" */
        final byte[][] encodingPattern =
        {
                { (byte) 0x85, (byte) 0x95, (byte) 0x83, (byte) 0x96,
                        (byte) 0x84, (byte) 0x89, (byte) 0x95, (byte) 0x87 },
                { (byte) 0x66, (byte) 0x76, (byte) 0x64, (byte) 0x77,
                        (byte) 0x65, (byte) 0x71, (byte) 0x76, (byte) 0x68 } };
        String[] encodingName = { "IBM-37", "IBM-930" }; //@ibm-290 not supported

        String encoding = null;
        int i = 0;
        int length = bytes.length;
        while (bytes[i] != 0x4c && i < length)
        {
            i++;
        }
        i++; //one more
        int currentPattern = -1;
        int patternPosition = 0;
        while (encoding == null && i < length && bytes[i] != 0x6e )
        {
            if (currentPattern < 0)
            {
                for (int j = 0; j < encodingPattern.length
                        && currentPattern < 0; j++)
                {
                    if (bytes[i] == encodingPattern[j][0])
                    {
                        currentPattern = j;
                        patternPosition = 1;
                    }
                }
            } else
            {
                if (patternPosition == encodingPattern[currentPattern].length)
                {
                    // Pick the encoding for the string to obtain the pattern
                    // from withing the string
                    encoding = encodingName[currentPattern];
                    // skip space
                     
                    while (bytes[i] == 0x40 && i < length)
                        i++;
                    // make sure = is found. = is invariant at 0x7e
                    if (bytes[i] == 0x7e && i < length)
                    {
                        i++;
                        // Skip space
                        while (bytes[i] == 0x40 && i < length)
                        {
                            i++;
                        }
                        // Make sure " is found. " is invariant at 0x7f except
                        // for
                        // code pages 905 and 1026 (turkey) where it is found at
                        // 0xfc
                        if ((bytes[i] == 0x7f || bytes[i] == 0xfc)
                                && (i < length))
                        {
                            i++;
                            // Find the ending quote
                            int endingQuote = i;
                            while (bytes[endingQuote] != 0x7f
                                    && bytes[endingQuote] != 0xfc
                                    && endingQuote < length)
                            {
                                endingQuote++;
                            }
                            if (endingQuote < length)
                            {
                                // Create a string from the bytes and return the
                                // encoding
                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                                        bytes, i, endingQuote - i);

                                BufferedReader reader = null;
                                try
                                {
                                    reader = new BufferedReader(
                                            new InputStreamReader(
                                                    byteArrayInputStream,
                                                    encodingName[currentPattern]));
                                    encoding = reader.readLine();
                                } catch (UnsupportedEncodingException e)
                                {
                                    // Should never get here since the encoding should be valid
                                    currentPattern = -1;
                                } catch (IOException e)
                                {
                                    // Should never get here since we should get io exception on a byteArrayInputStream
                                    currentPattern = -1;
                                }finally
                                {
                                    try{
                                        byteArrayInputStream.close();
                                    }catch(Exception e){ }
                                    try{
                                        reader.close();
                                    }catch(Exception e){ }
                                }

                            } else
                            {
                                currentPattern = -1;
                            }
                        } else
                        {
                            currentPattern = -1;
                        }
                    } else
                    {
                        currentPattern = -1;
                    }

                } else
                {
                    if (bytes[i] == encodingPattern[currentPattern][patternPosition])
                    {
                        patternPosition++;
                    } else
                    {
                        currentPattern = -1;
                    }
                }
            }
            i++;
        }

        return encoding;
    } 

    /**
     * Writes the given Java String to the XML value that this <code>SQLXML</code> object designates.
     * @param str the string to be written to the XML value that this <code>SQLXML</code> designates
     * @throws SQLException if there is an error accessing the XML value
     */
    public synchronized void setString(String str) throws SQLException
    {
        freeInternals(); 
        if(lobType == LOB_FREED)
        {
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
        }
        
        clobValue_ = new AS400JDBCClob(str, str.length()); 

        lobType = SQLData.CLOB; 
    }


    /**
    Package level helper method.  Only valid on Clob-like data.
    Writes a String to this XML, starting at position <i>position</i> in the XML.  
    The XML will be truncated after the last character written.  The <i>lengthOfWrite</i>
    characters written will start from <i>offset</i> in the string that was provided by the
    application.

    @param position The position (1-based) in the XML where writes should start.
    @param string The string that will be written to the XML.
    @param offset The offset into string to start reading characters (0-based).
    @param lengthOfWrite The number of characters to write.
    @return The number of characters written.

    @exception SQLException If there is an error
    **/
    synchronized int setString(long position, String string, int offset, int lengthOfWrite) throws SQLException
    {
        switch (lobType) 
        {
            case SQLData.DBCLOB:
            case SQLData.CLOB: 
                return clobValue_.setString(position, string, offset, lengthOfWrite);
            case SQLData.DBCLOB_LOCATOR:
            case SQLData.CLOB_LOCATOR: 
                return clobLocatorValue_.setString(position, string, offset, lengthOfWrite); 
            default:
                JDError.throwSQLException(this, JDError.EXC_INTERNAL, "Invalid value: "+ lobType);
            return 0;
        }
        
    
    }
    
    
    /**
     * Retrieves the XML value designated by this SQLXML instance as a stream. 
     * @return a stream containing the XML data.
     * @throws SQLException if there is an error processing the XML value.
     *   An exception is thrown if the state is not readable.
     */
    public InputStream getBinaryStream() throws SQLException
    {
        InputStream is = null;
        switch (lobType) 
        {
            case SQLData.DBCLOB:
            case SQLData.CLOB: 
                try
                {
                    // Check for an internal encoding in the string. If there is
                    // one, we must use it
                    String clobString = new String(clobValue_.data_);
                    String internalEncoding = getInternalEncoding(clobString);
                    if (internalEncoding != null)
                    {
                        is = new ByteArrayInputStream(clobString.getBytes(internalEncoding));
                    } else
                    {
                        is = new ByteArrayInputStream(clobString.getBytes("UTF-8"));
                    }
                } catch (UnsupportedEncodingException e)
                {
                    JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                    return null;
                }
                break;
            case SQLData.DBCLOB_LOCATOR:
            case SQLData.CLOB_LOCATOR:   
                //This will also be the case for XML column data
                if(isXML_) //@xml6 if xml column and thus also a locator, then get bytes from bloblocator code
                    is = blobLocatorValue_.getBinaryStream();   //@xml6 (no trim of XML declaration because it is binary)
                else
                {
                    
                    try
                    {
                        // Check for an internal encoding in the string. If there is
                        // one, we must use it
                        String clobString = clobLocatorValue_.getSubString((long)1, (int)clobLocatorValue_.length());
                        String internalEncoding = getInternalEncoding(clobString);
                        if (internalEncoding != null)
                        {
                            is = new ByteArrayInputStream(clobString.getBytes(internalEncoding));
                        } else
                        {
                            is = new ByteArrayInputStream(clobString.getBytes("UTF-8"));
                        }
                    } catch (UnsupportedEncodingException e)
                    {
                        JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                        return null;
                    }
                }
                break;
            case SQLData.BLOB:  
                //
                // In this case, we rely on the binary stream to identify its own encoding
                // It appears the that binary encoding will be used the most.. However,
                // this is not useful from a DB perspective since it cannot be searched.
                // 
                is = blobValue_.getBinaryStream();

                break;
            case SQLData.BLOB_LOCATOR: 
                //
                // In this case, we rely on the binary stream to identify its own encoding
                // It appears the that binary encoding will be used the most.. However,
                // this is not useful from a DB perspective since it cannot be searched.
                // 
                is = blobLocatorValue_.getBinaryStream();

                break;
             // Obtain a stream from a dom document by first converting it to
                // a String and then getting the UTF-8 representation
                case DOM_DOCUMENT:
                    try
                    {
                        String string = this.getString();
                        is = new ByteArrayInputStream(string.getBytes("UTF-8"));

                    } catch (UnsupportedEncodingException e)
                    {
                        JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                        return null;
                    }

                    break;
            default:  
            {
                JDError.throwSQLException(this, JDError.EXC_INTERNAL, "Invalid value: "+ lobType);
                return null;
            }
        }
        return is;
    }



    /**
     * Returns a Source for reading the XML value designated by this SQLXML instance.
     * Sources are used as inputs to XML parsers and XSLT transformers.
     * @param sourceClass The class of the source, or null.  
     * If the class is null, a vendor specific Source implementation will be returned.
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
     */
/* ifdef JDBC40 */
    public synchronized <T extends Source> T getSource(Class<T> sourceClass) throws SQLException
    {
        String classname;
        if (sourceClass == null)
        {
            classname = "javax.xml.transform.stream.StreamSource";
        } else
        {
            classname = sourceClass.getName();
        }
  
        if (lobType == LOB_FREED)
        {
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
            return null;
        }

        if (classname.equals("javax.xml.transform.dom.DOMSource"))
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder parser = factory.newDocumentBuilder();
                //parser.setErrorHandler(errorHandler);
                org.w3c.dom.Document doc;
                if (lobType == SQLData.CLOB           //@clob
                        || lobType == SQLData.DBCLOB  //@clob
                        || lobType == SQLData.CLOB_LOCATOR
                        || lobType == SQLData.DBCLOB_LOCATOR)
                {
                    // Do something different for CLOB locators because
                    // getBinaryStream results in getAsciiStream which
                    // cannot be parsed if an internal encoding is present
                    doc = parser.parse(new InputSource(getCharacterStream()));
                } else if (lobType == DOM_DOCUMENT)
                {
                    doc = domDocument_;
                } else
                {
                    doc = parser.parse(getBinaryStream());
                }
                return (T) new DOMSource(doc);
            } catch (ParserConfigurationException e)
            {
                // This exception will probably not occur
                JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                return null;
            } catch (SAXException e)
            {
                JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                return null;
            } catch (IOException e)
            {
                JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                return null;
            }
        } else if (classname.equals("javax.xml.transform.sax.SAXSource"))
        {
            InputSource inputSource = new InputSource(getCharacterStream());
            return (T) new javax.xml.transform.sax.SAXSource(inputSource);
        } else if (classname.equals("javax.xml.transform.stax.StAXSource"))
        {
            try
            {
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                XMLStreamReader xmlStreamReader;
                xmlStreamReader = inputFactory.createXMLStreamReader(getCharacterStream());
                return (T) new StAXSource(xmlStreamReader);
            } catch (XMLStreamException e)
            {
                JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                return null;
            }

        } else if (classname.equals("javax.xml.transform.stream.StreamSource"))
        {
            return (T) new javax.xml.transform.stream.StreamSource( getCharacterStream());
        } else
        {
            JDError.throwSQLException (this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            return null;
        }
         
    }
/* endif */ 

    /**
     * Retrieves a stream that can be used to write the XML value that this SQLXML instance represents.
     * @return a stream to which data can be written.
     * @throws SQLException   if there is an error processing the XML value. An exception
     *             is thrown if the state is not writable.
     * @exception If there is an error
     */
    public synchronized OutputStream setBinaryStream() throws SQLException
    {

        if (lobType == LOB_FREED)
        {
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
            return null;
        }

        freeInternals();

        if (blobValue_ == null)
        {
            byte[] dummy = new byte[0];
            blobValue_ = new AS400JDBCBlob(dummy, AS400JDBCBlob.MAX_LOB_SIZE);
        }
        lobType = SQLData.BLOB;
        return blobValue_.setBinaryStream(1);
    }

    /**
     * Retrieves a Writer to be used to write the XML value that this SQLXML instance represents. 
     * @return a stream to which data can be written.
     * @throws SQLException if there is an error processing the XML value.
     *   The getCause() method of the exception may provide a more detailed exception, for example,
     *   if the stream does not contain valid characters. 
     *   An exception is thrown if the state is not writable.
     * @exception If there is an error
     */
    public synchronized Writer setCharacterStream() throws SQLException
    {
        if (lobType == LOB_FREED) {
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
            return null;
        }

        freeInternals();

        clobValue_ = new AS400JDBCClob("", AS400JDBCClob.MAX_LOB_SIZE); 
        lobType = SQLData.CLOB; 

        return clobValue_.setCharacterStream(1L); 
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
     * @exception If there is an error
     */
    public synchronized <T extends Result> T setResult(Class<T> resultClass) throws SQLException
    {
        String classname;
        if (resultClass == null)
        {
            classname = "javax.xml.transform.stream.StreamSource";
        } else
        {
            classname = resultClass.getName();
        }
        
        if (lobType == LOB_FREED)
        {
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE);
            return null;
        }

        if (classname.equals("javax.xml.transform.dom.DOMResult"))
        {
            try
            {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                freeInternals();
                lobType = DOM_DOCUMENT;
                domDocument_ = documentBuilder.newDocument();

                return (T) new DOMResult(domDocument_);

            } catch (ParserConfigurationException e)
            {
                JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                return null;
            }
        } else if (classname.equals("javax.xml.transform.sax.SAXResult"))
        {

            // 
            // TODO
            //
            // To get this to work, you would need to implement a SAX
            // content handler to dump the handled information to the
            // an output character stream
            // 
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            return null;

        } else if (classname.equals("javax.xml.transform.stax.StAXResult"))
        {
            try
            {
/* ifdef JDBC40 */
                XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
                XMLStreamWriter xmlStreamWriter;
                xmlStreamWriter = outputFactory.createXMLStreamWriter(setCharacterStream());
                return (T) new StAXResult(xmlStreamWriter);
/* endif */ 
/* ifndef JDBC40 
            	throw new SQLException("NOT SUPPORTED"); 
 endif */ 
            } catch (Exception e)
            {
                JDError.throwSQLException(this, JDError.EXC_XML_PARSING_ERROR, e);
                return null;
            }
        } else if (classname.equals("javax.xml.transform.stream.StreamResult"))
        {
            return (T) new javax.xml.transform.stream.StreamResult(setCharacterStream());
        } else
        {
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_NOT_SUPPORTED);
            return null;
        }
    }
    

    
    /**
     * This method frees the object and releases the
     * the resources that it holds. The object is invalid once the
     * <code>free</code> method is called. If <code>free</code> is called
     * multiple times, the subsequent calls to <code>free</code> are treated
     * as a no-op.
     * 
     * @throws SQLException
     *             if an error occurs releasing the Clob's resources
     */
    public synchronized void free() throws SQLException
    {
        freeInternals();
        lobType = LOB_FREED; 
    }
    

    /**
     * Free the internal representation used by the SQLXML.
     * Call before setting a new type of internal representation
     */
    private void freeInternals() throws SQLException
    {
        if (blobValue_ != null)
        {
            blobValue_.free();
            blobValue_ = null;
        }
        if (blobLocatorValue_ != null)
        {
            //blobLocatorValue_.free(); //@olddesc
            blobLocatorValue_ = null;
        }

        if (clobValue_ != null)
        {
/* ifdef JDBC40 */
            clobValue_.free();
/* endif */ 
            clobValue_ = null;
        }

        if (clobLocatorValue_ != null)
        {
            //clobLocatorValue_.free();//@olddesc
            clobLocatorValue_ = null;
        }

        if (domDocument_ != null)
        {
            domDocument_ = null;
        }
    } 
    /**
     * Retrieves the internal encoding specified in the string that represents
     * an XML document. If no internal encoding is present, then null is
     * returned.
     */
    private String getInternalEncoding(String xml)
    {
        String encoding = null;
        // find the first <
        int start = xml.indexOf("<");
        if (start >= 0)
        {
            // find the ending >
            int end = xml.indexOf(">", start);
            if (end >= 0)
            {
                String piece = xml.substring(start, end + 1);
                int encodingIndex = piece.indexOf("encoding=");
                if (encodingIndex > 0)
                {
                    int firstQuote = piece.indexOf("\"", encodingIndex);
                    if (firstQuote > 0)
                    {
                        int lastQuote = piece.indexOf("\"", firstQuote + 1);
                        if (lastQuote > 0)
                        {
                            return piece.substring(firstQuote + 1, lastQuote);
                        }
                    }
                }
            }
        }
        return encoding;
    } 
}
