///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: XmlException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import java.awt.Component;

/**
 * Thrown when an error is encountered processing XML.
 */
public class XmlException extends Exception
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

	private String         m_localizedMessage;
	private Exception      m_exception;

    /**
     * Constructs a <code>XmlException</code> without a user message.
     */
    XmlException()
	{
		super();
    }

    /**
     * Constructs a <code>XmlException</code> with a user message.
     *
     * @param   key   the resource key for the message string
     */
    XmlException(String key)
    {
        super();
        m_localizedMessage = SystemResourceFinder.format(key);
    }

    /**
     * Constructs a <code>XmlException</code> with a user message and substitution values.
     *
     * @param   key   The resource key for the message string
     * @param   args  Array of substitution values
     */
    XmlException(String key, Object[] args)
    {
        super();
        m_localizedMessage = SystemResourceFinder.format(key, args);
    }

    /**
     * Constructs a <code>XmlException</code> with another exception.
	 *
     * @param   e   the exception wrapped by the XmlException
     */
    XmlException(Exception e)
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
     * Returns a localized description of this <code>XmlException</code>.
     */
	public String getLocalizedMessage()
	{
		if (m_localizedMessage != null)
			return m_localizedMessage;

		return super.getLocalizedMessage();
	}

    /**
     * Returns a description of this <code>XmlException</code>.
     */
	public String getMessage()
	{
          return getLocalizedMessage();
	}

    /**
     * Returns the original exception that caused this <code>XmlException</code>.
     *
     * @return The exception that causes this instance of <code>XmlException</code>.
     * If this exception was not caused by another exception, null is returned.
     */
	public Exception getException()
	{
	    return m_exception;
	}
}
