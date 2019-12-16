///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlSpecificationException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import com.ibm.as400.access.Trace;                  // @A1A
import java.util.Vector;

/**
 * Thrown when syntax errors are detected in a PCML source file.
 *
 * @see ProgramCallDocument
 * @see XmlException
 * @see PcmlException
 */
public class PcmlSpecificationException extends Exception
{
	Vector	m_messages = new Vector();
	
    PcmlSpecificationException()
	{
		super();
    }

    PcmlSpecificationException(String s)
	{
		super(s);
    }
    
    void addMessage(String msg)
    {
    	m_messages.addElement(msg);
    }
    
    /**
     * Returns the error messages that were logged to this object.
     * @return A list of error messages associated with this exception.
     * If no error messages, then an empty list is returned.
     */
    public String[] getMessages()
    {
        String[] msgs = (String[])m_messages.toArray(new String[m_messages.size()]);
        return msgs;
    }

    /**
     * Logs the specfication errors to error logging stream
	 * determined by <code>com.ibm.as400.access.Trace</code>.
     *
     * @see com.ibm.as400.access.Trace
     */
    void reportErrors()
    {
		Trace.log(Trace.PCML, this);

    	int count = m_messages.size();
    	for (int i = 0; i < count; i++)
            Trace.log(Trace.PCML, (String)(m_messages.elementAt(i)));
    		
    	if (count == 1)
            Trace.log(Trace.PCML, SystemResourceFinder.format(DAMRI.ONE_PARSE_ERROR));
    	else
		{
			Object[] args = { new Integer(count) };
    		Trace.log(Trace.PCML, SystemResourceFinder.format(DAMRI.MANY_PARSE_ERRORS, args));
		}
    }
}
