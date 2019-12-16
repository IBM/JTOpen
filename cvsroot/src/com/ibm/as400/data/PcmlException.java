///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
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
 * @see PcmlSpecificationException
 */
public class PcmlException extends XmlException            // @C3C
{
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
        super(key);            // @C3C
    }

    /**
     * Constructs a <code>PcmlException</code> with a user message and substitution values.
     *
     * @param   key   The resource key for the message string
     * @param   args  Array of substitution values
     */
    PcmlException(String key, Object[] args)
    {
        super(key, args);            // @C3C
    }

    /**
     * Constructs a <code>PcmlException</code> with another exception.
	 *
     * @param   e   the exception wrapped by the PcmlException
     */
    PcmlException(Exception e)
	{
	    super(e);          // @C3C
    }


// @C3D - Moved to XmlException: getLocalizedMessage(), getException().

}
