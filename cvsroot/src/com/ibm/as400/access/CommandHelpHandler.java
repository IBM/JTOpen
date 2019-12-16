///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandHelpHandler.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

// This class is used by Command to parse the XML and get the help identifier keywords out of it.
class CommandHelpHandler extends DefaultHandler
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  private final Vector keywords_ = new Vector();
  private String panelGroup_;
  private String helpID_;
  private String productLibrary_;

  String getHelpID()
  {
    return helpID_;
  }

  Vector getKeywords()
  {
    return keywords_;
  }

  String getPanelGroup()
  {
    return panelGroup_;
  }

  String getProductLibrary()
  {
    return productLibrary_;
  }

  public void startElement(String namespaceURI, String localName, String name, Attributes attributes) throws SAXException
  {
    if (name.equals("Parm"))
    {
      String kwd = attributes.getValue("Kwd");
      if (kwd != null)
      {
        keywords_.addElement(kwd);
      }
    }
    else if (name.equals("Cmd")) // Assume there is only one Cmd element in the XML.
    {
      String helpName = attributes.getValue("HlpPnlGrp");
      String helpLib = attributes.getValue("HlpPnlGrpLib");

      if (helpLib != null && helpLib.equals("__LIBL"))
      {
        helpLib = "*LIBL";
      }

      if (helpLib != null && helpName != null)
      {
        panelGroup_ = QSYSObjectPathName.toPath(helpLib, helpName, "PNLGRP");
      }

      helpID_ = attributes.getValue("HlpID");
      productLibrary_ = attributes.getValue("PrdLib");
    }
  }
}

