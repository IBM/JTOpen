///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RfmlDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.Trace;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.Enumeration;



/**
*/
class RfmlDocument extends PcmlDocument {
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";


    static final long serialVersionUID = 5L;

    // New attributes should be added to the end of this array
    private static final String RFML_ATTRIBUTES[] = {
        "version", "ccsid"
    };

    private static final int DEFAULT_CCSID = 65535;  // value to report if not explicitly set.

    // Note: The following two variables represent the same value in two forms.
    private String m_Ccsid;  // null indicates "uninitialized"
    private int m_CcsidInt;  // zero indicates "uninitialized"

    /**
    */
    RfmlDocument(PcmlAttributeList attrs, String docName)
    {
        super(attrs,docName);
        setNodeType(PcmlNodeType.RFML);
        setCcsid(getAttributeValue("ccsid"));
    }

    /**
     Returns the list of valid attributes for the <rfml> element.
     **/
    String[] getAttributeList()
    {
        return RFML_ATTRIBUTES;
    }


    /**
     Returns the ccsid= String value.  If not set, returns null.
     **/
    public final String getCcsid()
    {
      return m_Ccsid;
    }


    /**
     Returns the ccsid= integer literal value.  If not set, returns 65535.
     **/
    public final int getCcsidInt()
    {
      if (m_CcsidInt == 0) return DEFAULT_CCSID;  // if uninitialized, report default
      else return m_CcsidInt;
    }

    /**
     Overrides the superclass's implementation.
     **/
    protected AS400DataType getConverter(int dataType, int dataLength, int dataPrecision, int ccsid)
      throws PcmlException
    {
      if (dataType == PcmlData.CHAR)
      {
        // If the requested CCSID is not the same as
        // the system's default host CCSID, always create
        // a new converter. I.E. We only cach converters
        // with CCSIDs that match the system object.
        if (ccsid != m_CcsidInt)
        {
          return new AS400Text(dataLength, ccsid);
        }

        switch (dataLength)
        {
          case 1:
            if (m_Text_1 == null)
            {
              m_Text_1 = new AS400Text(dataLength, ccsid);
            }
            return m_Text_1;
          case 10:
            if (m_Text_10 == null)
            {
              m_Text_10 = new AS400Text(dataLength, ccsid);
            }
            return m_Text_10;
          default:
            return new AS400Text(dataLength, ccsid);

        }
      }
      else {
        return super.getConverter(dataType, dataLength, dataPrecision, ccsid);
      }
    }

    /**
     Returns the value of the specified (numeric) field, as a "double".
     **/
    synchronized double getDoubleValue(String name) throws PcmlException 
    {
        return getDoubleValue(name, new PcmlDimensions());
    }

    /**
     Returns the value of the specified (numeric) field, as a "double".
     **/
    synchronized double getDoubleValue(String name, PcmlDimensions indices) throws PcmlException 
    {
        Object value;
    
        value = getValue(name, indices);

        if (value == null)
        {
          throw new PcmlException(DAMRI.INPUT_VALUE_NOT_SET, new Object[] {name} );
        }
        else if (value instanceof String) 
        {
            return Double.parseDouble((String) value); // Note: parseDouble() is new in Java2.
        }
        else if (value instanceof Number) 
        {
            return ((Number) value).doubleValue();
        }  
        else 
        {
            throw new PcmlException(DAMRI.STRING_OR_NUMBER, new Object[] {value.getClass().getName(), name} );
        }
    }


    /**
     Returns a RfmlRecordFormat object representing the specified &lt;recordformat&gt; element.
     **/
    RfmlRecordFormat getRecordFormatNode(String formatName)
      throws XmlException
    {
      PcmlNode node = getElement(formatName);
      if (node instanceof RfmlRecordFormat)
      {
        return (RfmlRecordFormat) node;
      }
      else
      {
        if (node == null)
          throw new XmlException(DAMRI.ELEMENT_NOT_FOUND, new Object[] {formatName, "<recordformat>"} );
        else
          throw new XmlException(DAMRI.WRONG_ELEMENT_TYPE, new Object[] {formatName, "<recordformat>"} );
      }
    }



    /**
     Sets the ccsid= attribute value.
     **/
    protected void setCcsid(String ccsid)
    {
        // Handle null or empty string
        if (ccsid == null || ccsid.equals(""))
        {
            m_Ccsid = null;
            m_CcsidInt = 0;
            return;
        }

        // Try to parse an integer from the attribute value
        try
        {
            m_Ccsid = ccsid;
            m_CcsidInt = Integer.parseInt(ccsid);
        }
        // If value is not an integer, assume that it's an element name.
        // checkAttributes() will be called later to verify the element name.
        catch (NumberFormatException e)
        {
            m_Ccsid = null;
            m_CcsidInt = 0;
        }
    }


    /**
     Generates XML (RFML) representing the data contained in this object.
     Throws a runtime exception if this object contains no data.

     @param outStream The output stream to which to write the text.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    public void toXml(OutputStream outStream)
      throws IOException, XmlException
    {
      PrintWriter xmlFile = new PrintWriter(outStream);
      try
      {
        xmlFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");         // @A1c
        xmlFile.println("<!DOCTYPE rfml SYSTEM \"rfml.dtd\">");

        toXml(this, xmlFile, "");

        if (xmlFile.checkError())  // Note: This flushes the stream.
        {
          Trace.log(Trace.ERROR, "Error when writing RFML to OutputStream.");
          // Possible future enhancement: Throw an exception indicating IO error when writing RFML.
        }
      }
      finally {
        xmlFile.close(); // Note: close() flushes the stream first.
      }
    }


    /**
     Generates XML (RFML) representing the data contained in the specified node.
     Throws a runtime exception if this object contains no data.

     @param node The node to generate RFML for.
     @param writer The writer to which to write the text.
     @param indent The indentation with which to prepend the generated XML.
     @exception IOException  If an error occurs while writing the data.
     @exception XmlException  If an error occurs while processing RFML.
     **/
    private static void toXml(PcmlDocNode node, PrintWriter writer, String indent)
      throws IOException, XmlException
    {
      // Start the start tag.
      writer.print(indent + "<" + node.getTagName());

      // Print out any attributes that have non-null values.
      String[] attrs = node.getAttributeList();
      for (int i=0; i<attrs.length; i++) {
        if (node.getAttributeValue(attrs[i]) != null) {
          writer.print(" " + attrs[i] + "=\"" + node.getAttributeValue(attrs[i]) + "\"");
        }
      }

      if (!(node.getNodeType() == PcmlNodeType.DATA &&
            node.getAttributeValue("type").equals("struct"))
          && node.hasChildren())
      {
        // Finish the start tag.
        writer.println(">");

        // Step through each element, starting at the document root, and generate RFML for each element.
        Enumeration children = node.getChildren();
        while (children.hasMoreElements())
        {
          PcmlDocNode child = (PcmlDocNode) children.nextElement();
          toXml(child, writer, indent+"  ");
        }

        // Write the end tag.
        writer.println(indent + "</" + node.getTagName() + ">");
      }
      else {
        // Finish the start tag.
        writer.println("/>");
      }
      writer.flush();
    }

}
