///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCSQLXMLLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2009 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/* ifdef JDBC40 
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 
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
endif */ 

//import com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser;


//@PDA jdbc40 new class
//@xml2 whole class is redesigned after that of The Native Driver
/**
<p>The AS400JDBCSQLXMLLocator class provides the object interface for using XML data through JDBC.
The mapping in the Java programming language for the SQL XML type. 
XML is a built-in type that stores an XML value as a column value in a row of a database table. 
The SQLXML interface provides methods for accessing the XML value as a String, a Reader or Writer, or as a Stream. 
**/
public class AS400JDBCSQLXMLLocator extends AS400JDBCSQLXML 
{
   

     
    /**
    Constructs an AS400JDBCSQLXMLLocator object of Clob/DBClob locator data.  (This constructor signature is only 
    available on ClobLocator.)
    This could be created as a result of an actual XML column or some other data type column and calling resultSet.getSQLXML()
    The data for the
    SQLXML will be retrieved as requested, directly from the
    IBM i system, using the locator handle.
    
    @param  locator             The locator.
    @param  converter           The text converter.
    @param  savedObject         Saved Object.
    @param  savedScale          Saved scale.
    @param  isXML               Flag that stream is from an XML column type (needed to strip xml declaration)
    **/
    AS400JDBCSQLXMLLocator(JDLobLocator locator, ConvTable converter, Object savedObject, int savedScale, boolean isXml)
    {
        super();
        //Since SQLXML has both text and binary getter methods, we need to preserve converter, but need to be able to get 
        //to the bits as binary without doing any conversion or trimming of XML declaration
        
        //Native JDBC changed to always trim off xml header if accessing data through SQLXML object even it column is not XML...(TB also will now do this)
        isXml = true;//@xmltrim (match native jdbc for trimming xml decl if using sqlxml)
        
        clobLocatorValue_ = new AS400JDBCClobLocator( locator, converter, savedObject, savedScale, isXml); //@xml4 allow AS400JDBCClobLocator to trim off xml header if needed 
        if(isXml)
            blobLocatorValue_ = new AS400JDBCBlobLocator( locator, savedObject, savedScale); //@xml6 also need ref to bloblocator in case SQLXML.getBinaryStream is called 
        
        lobType = SQLData.CLOB_LOCATOR;
        isXML_ = isXml;      //@xml4 
    }

     
    /**
    Constructs an AS400JDBCSQLXMLLocator object of BlobLocator data.  (This constructor signature is only 
    available on BlobLocator.)
    The data for the
    BLOB will be retrieved as requested, directly from the
    IBM i system, using the locator handle.
    @param  locator             The locator.
    @param  savedObject         Saved Object.
    @param  savedScale          Saved scale.
    **/
    AS400JDBCSQLXMLLocator(JDLobLocator locator, Object savedObject, int savedScale)
    {
        super();
        blobLocatorValue_ = new AS400JDBCBlobLocator( locator, savedObject, savedScale); 
        lobType = SQLData.BLOB_LOCATOR;
        isXML_ = true;//@xmltrim (match native jdbc for trimming xml decl if using sqlxml)
    }

    //@olddesc
    /**
    Returns the handle to this locator in the database.

    @return             The handle to this locator in the databaes.
    **/
    synchronized int getHandle()throws SQLException  
    {
         
        if(clobLocatorValue_ != null)
            return clobLocatorValue_.getHandle();
        else if(blobLocatorValue_ != null)
            return blobLocatorValue_.getHandle();
        else
            return -1; //not set or updated by non-locator value and not needed

    }
       
}
