///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

import java.util.Vector;

/**
 * Thrown when an errors are detected in the PCML source file.
 *
 * @see ProgramCallDocument
 */
class PcmlSpecificationException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
     * Logs the specfication errors to error logging stream
	 * determined by <code>PcmlMessageLog</code>.
     *
     * @see PcmlMessageLog
     */
    public void reportErrors()
    {
		PcmlMessageLog.logError(this);

    	int count = m_messages.size();
    	for (int i = 0; i < count; i++)
   			PcmlMessageLog.logError(m_messages.elementAt(i));
    		
    	if (count == 1)
    		PcmlMessageLog.logError(SystemResourceFinder.format(DAMRI.ONE_PARSE_ERROR));
    	else
		{
			Object[] args = { new Integer(count) };
    		PcmlMessageLog.logError(SystemResourceFinder.format(DAMRI.MANY_PARSE_ERRORS, args));
		}
    }
}
