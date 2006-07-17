///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: XPCMLHelper.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import com.ibm.as400.access.Trace;

// This class contains the transform methods originally added to ProgramCallDocument.
// They are here so that ProgramCallDocument does not have a direct dependency on the XSL/XML
// classes at runtime, in case a user wants to use a ProgramCallDocument to just de-serialize
// their PCML under pre-JDK 1.4 (and they don't have the XML parser or XSL processor in their
// CLASSPATH).
class XPCMLHelper
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    /**
      doTransform -- Transforms one XML stream to another.  Inputs are transform file
                     (.xsl file), XML input stream, and XML output stream containing
                     transformed XML.
     **/

    //@E2C -- Change protected scope to package scope
    static void doTransform(String transformFile, InputStream streamSource, OutputStream streamResult)
           throws TransformerException, TransformerConfigurationException,
           SAXException, IOException, PcmlException	
    	{
            StreamSource in = new StreamSource(SystemResourceFinder.getXPCMLTransformFile(transformFile));  // Note: Class 'StreamSource' has no close() method.
            TransformerFactory tFactory = TransformerFactory.newInstance(); //@CRS
            Transformer transformer = tFactory.newTransformer(in);
            transformer.transform(new StreamSource(streamSource), new StreamResult(streamResult));
      }


    /**
      doCondenseTransform -- Transforms one XML stream to another.  Inputs are transform file
                     (.xsl file), full XPCML input stream, XSD stream and XML output
                     stream containing transformed XPCML.
     **/

    //@E2C -- Change protected scope to package scope
    static void doCondenseTransform(String transformFile, InputStream streamSource, OutputStream streamResult, String xsdStreamName)
           throws TransformerException, TransformerConfigurationException,
           SAXException, IOException, PcmlException	
    	{
            StreamSource in = new StreamSource(SystemResourceFinder.getXPCMLTransformFile(transformFile));
            TransformerFactory tFactory = TransformerFactory.newInstance(); //@CRS
            Transformer transformer = tFactory.newTransformer(in);
            transformer.setParameter("xsdFileName", xsdStreamName);

            StreamSource streamIn = new StreamSource(streamSource);
            transformer.transform(streamIn, new StreamResult(streamResult));
     }


    /**
      doSimplifyXSDTransform -- Transform that takes XSD file and transforms it to a more readable form for identifying types and their attributes.
     **/
    static void doSimplifyXSDTransform(InputStream streamSource, OutputStream streamResult)
      throws SAXException, IOException 
    {
      try
      {
        StreamSource in = new StreamSource(SystemResourceFinder.getXPCMLTransformFile("xpcml_xpcml.xsl"));
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(in);
        transformer.transform(new StreamSource(streamSource), new StreamResult(streamResult));
      }
      catch (TransformerException e){
        Trace.log(Trace.ERROR, e);
        throw new SAXException(e);
      }
    }


}
