///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

import java.util.Vector;

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
    	System.err.println(toString());

    	int count = m_messages.size();
    	for (int i = 0; i < count; i++)
		{
			// Check if first message indicates DTD not found and
			// suppress all the parser errors if true
			if (i == 0)
			{
				String msg = (String)m_messages.elementAt(i);
				if (msg.indexOf("java.io.FileNotFoundException") != -1 && msg.indexOf(".dtd") != -1)
					count = 1;
			}

   			System.err.println((String)m_messages.elementAt(i));
		}

    	if (count == 1)
            System.err.println( SystemResourceFinder.format( DAMRI.ONE_PARSE_ERROR ) );
    	else
            System.err.println( SystemResourceFinder.format( DAMRI.MANY_PARSE_ERRORS, new Object[] {new Integer(count)} ) );
    }
}
