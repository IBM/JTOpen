///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ParseException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.Trace;     // @A1A
import java.util.Vector;
import java.io.StringWriter;           // @A1A
import java.io.PrintWriter;            // @A1A

class ParseException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	Vector	m_messages = new Vector();

    ParseException()
	{
		super();
    }

    ParseException(String s)
	{
		super(s);
    }

    void addMessage(String msg)
    {
    	m_messages.addElement(msg);
    }

    public void reportErrors()
    {
      ///System.err.println(toString());                         // @A1D
      reportErrors(new PrintWriter(System.err, true));           // @A1C
    }

    // @A1A
    public String getLocalizedMessage()
    {
      return getMessage();
    }

    // @A1A - Moved logic to here from original reportErrors() method.
    private void reportErrors(PrintWriter writer)
    {
      int count = m_messages.size();
      for (int i = 0; i < count; i++)
      {
        // Check if first message indicates DTD not found and
        // suppress all the parser errors if true
        if (i == 0)
        {
          String msg = (String)m_messages.elementAt(i);
          if (msg != null &&                                         // @A1A
              msg.indexOf("java.io.FileNotFoundException") != -1 && msg.indexOf(".dtd") != -1)
            count = 1;
        }

        writer.println((String)m_messages.elementAt(i));
      }

      if (count == 1)
        writer.println( SystemResourceFinder.format( DAMRI.ONE_PARSE_ERROR ) );
      else
        writer.println( SystemResourceFinder.format( DAMRI.MANY_PARSE_ERRORS, new Object[] {new Integer(count)} ) );
      // Note: The previous 2 messages refer to "PCML".                     @A1A
      if (writer.checkError())   // See if writer had any trouble.          @A1A
      {
        Trace.log(Trace.ERROR, "Error when reporting errors to PrintWriter."); // @A1A
        System.err.println(toString());                                  // @A1A
      }
      // Note: Don't close the writer here, as it might be System.out or System.err.

    }

    public String getMessage()
    {
      StringWriter writer = new StringWriter();
      reportErrors(new PrintWriter(writer));
      String msg = writer.toString();
      try { writer.close(); } catch (java.io.IOException e) {}
      return msg;
    }

}
