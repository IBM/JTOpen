///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PcmlException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.awt.Component;

/**
 * Thrown when an error is encountered using a ProgramCallDocument.
 *
 * @see ProgramCallDocument
 */
public class PcmlException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	private String					m_localizedMessage;
	private Exception 				m_exception;

    /**
     * Constructs a <code>PcmlException</code> without a
     * user message.
     *
     */
    PcmlException()
	{
		super();
    }

    /**
     * Constructs a <code>PcmlException</code> with a user message.
     *
     * @param   key   the resource key for the message string
     */
    PcmlException(String key)
    {
        super();
        m_localizedMessage = SystemResourceFinder.format(key);
    }

    /**
     * Constructs a <code>PcmlException</code> with a user message and substitution values.
     *
     * @param   key   The resource key for the message string
     * @param   args  Array of substitution values
     */
    PcmlException(String key, Object[] args)
    {
        super();
        m_localizedMessage = SystemResourceFinder.format(key, args);
    }

    /**
     * Constructs a <code>PcmlException</code> with another exception.
	 *
     * @param   e   the exception wrapped by the PcmlException
     */
    PcmlException(Exception e)
	{
	    super();

        // Try to load a string using the exception's Class name as the key
        m_localizedMessage = SystemResourceFinder.getString(e.getClass().getName());
        
        // If there is no string for this exception, use a generic "Exception received" message.
        if (m_localizedMessage == null) 
        {
            m_localizedMessage = SystemResourceFinder.format(DAMRI.EXCEPTION_RECEIVED, new Object[] {e.getClass().getName()});
        }
	    
		m_exception = e;
    }

    /**
     * Returns a localized description of this <code>PcmlException</code>.
     *
     */
	public String getLocalizedMessage()
	{
		if (m_localizedMessage != null)
			return m_localizedMessage;

		return super.getLocalizedMessage();
	}

    /**
     * Returns the original exception that caused this <code>PcmlException</code>.
     *
     *
     * @return The exception that causes this instance of <code>PcmlException</code>.
     * If this exception was not caused by another exception, null is returned.
     *
     */
	public Exception getException()
	{
	    return m_exception;
	}
}
