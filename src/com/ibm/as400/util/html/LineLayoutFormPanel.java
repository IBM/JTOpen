///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LineLayoutFormPanel.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.util.Vector;

import com.ibm.as400.access.Trace;

/**
*  The LineLayoutFormPanel class represents a line layout of HTML form elements.
*  Form elements in the panel are aligned in a single row.  The trailing slash &quot;/&quot; 
*  on the LineLayoutFormPanel tag allows it to conform to the XHTML specification.
*
*  <P>
*  This example creates a LineLayoutFormPanel object and adds two form elements.
*  <BLOCKQUOTE><PRE>  
*  CheckboxFormInput privacyCheckbox = new CheckboxFormInput("confidential", "yes", "Confidential", true);
*  CheckboxFormInput mailCheckbox = new CheckboxFormInput("mailingList", "yes", "Join our mailing list", false);
*  LineLayoutFormPanel panel = new LineLayoutFormPanel();
*  panel.addElement(privacyCheckbox);
*  panel.addElement(mailCheckbox);
*  String tag = panel.getTag();
*  </PRE></BLOCKQUOTE>
*  <P>
*  The HTML tag that is generated would look like this:<BR>
*  &lt;input type=&quot;checkbox&quot; name=&quot;confidential&quot; value=&quot;yes&quot; checked=&quot;checked&quot; /&gt; Confidential 
*  &lt;input type=&quot;checkbox&quot; name=&quot;mailingList&quot; value=&quot;yes&quot; /&gt; Join our mailing list
*  &lt;br /&gt;
**/
public class LineLayoutFormPanel extends LayoutFormPanel
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    /**
    *  Returns the line layout panel tag.
    *  @return The tag.
    **/
    public String getTag()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.INFORMATION, "Generating LineLayoutFormPanel tag...");

        StringBuffer s = new StringBuffer("");
        for (int i=0; i< getSize(); i++)
        {
            HTMLTagElement e = getElement(i);
            s.append(e.getTag());
            s.append("\n");
        }
        s.append("<br />\n");

        return s.toString();
    }
}
