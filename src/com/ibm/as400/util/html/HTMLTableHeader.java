///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTableHeader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.Trace;
import java.beans.PropertyVetoException;

/**
*  The HTMLTableHeader represents an HTML table header tag.
*
*  <P>This example creates an HTMLTableHeader and displays the tag output.
*  <P><BLOCKQUOTE><PRE>
*  HTMLTableHeader header = new HTMLTableHeader();
*  header.setHorizontalAlignment(HTMLTableHeader.CENTER);
*  HTMLText headerText = new HTMLText("Customer Name");
*  header.setElement(headerText);
*  System.out.println(header.getTag());
*  </PRE></BLOCKQUOTE>
*  Here is the output of the tag:
*  <BLOCKQUOTE><PRE>
*  &lt;th align="center"&gt;Customer Name&lt;/th&gt;
*  </PRE></BLOCKQUOTE>
*
**/
public class HTMLTableHeader extends HTMLTableCell
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   /**
   *  Constructs a default HTMLTableHeader object.
   **/
   public HTMLTableHeader()
   {
      super();
   }
   
   /**
   *  Constructs an HTMLTableHeader object with the specified data <i>element</i>.
   *  @param element An HTMLTagElement object containing the data.
   **/
   public HTMLTableHeader(HTMLTagElement element)
   {
      super(element);
   }

   /**
   *  Returns the table header tag.
   *  @return The HTML tag.
   **/
   public String getTag()
   {
      return getTag(getElement());
   }

   /**
   *  Returns the table header tag with the specified data <i>element</i>.
   *  @return The HTML tag.
   **/
   public String getTag(HTMLTagElement element)
   {
      if (Trace.isTraceOn())
         Trace.log(Trace.INFORMATION, "Generating HTMLTableHeader tag...");

      if (element == null)
         throw new NullPointerException("element");

      StringBuffer tag = new StringBuffer();
      tag.append("<th");
      tag.append(getAttributeTag());
      tag.append(element.getTag());
      tag.append("</th>\n");
      
      return new String(tag);
   }

   /**
   *  Returns the HTML table header tag.
   *  @return The header tag.
   **/
   public String toString()
   {
      return getTag();
   }
}
