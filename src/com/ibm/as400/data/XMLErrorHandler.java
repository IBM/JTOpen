///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: XMLErrorHandler.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.data;

import org.xml.sax.ErrorHandler;                                    // @C1A
import org.xml.sax.SAXParseException;                               // @C1A

class XMLErrorHandler extends Object implements ErrorHandler        // @C1C
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String			m_filename;
    private int				m_headerLineCount;
    private boolean			m_ignorePCML = false;

    private ParseException	m_exc;

 	public XMLErrorHandler(String sxml, int headerLineCount)
 	{
 		m_filename = sxml;
 		m_headerLineCount = headerLineCount;
 	}

    public void error(SAXParseException spe)                        // @C1C
    {			  
        int lineNumber = spe.getLineNumber();                       // @C1A
        int columnNumber = spe.getColumnNumber();                   // @C1A
        
    	// Filename is either the source file or the DTD file 
    	// based on the line number because the DTD was prepended to the
    	// source as an inline DTD.
    	String fname;

    	// Adjust the line number to account for the PCML header
    	if (!(lineNumber - m_headerLineCount > 0))
    	{
    		// We have an error in the DTD.
    		// Change the filename and ignore any subsequent PCML errors.
    		fname = "pcml.dtd";
    		m_ignorePCML = true;
    	}
    	else
    	{
    		// Error is in the PCML.  Suppress if errors occurred in the DTD.
    		if (m_ignorePCML)                                       // @C1C
    		    return;                                             // @C1A
    		    
        	fname = m_filename;
    	    lineNumber -= m_headerLineCount;
    	}

    	// If exception object doesn't exist, create it
    	if (m_exc == null)
    	{
    		m_exc = new ParseException(SystemResourceFinder.format(DAMRI.FAILED_TO_PARSE, new Object[] {fname} ) );
    	}

		// Add the formatted message
   		m_exc.addMessage("[" + lineNumber + "," + (columnNumber - 1) + "]: " + spe.getMessage() );
    }
    
    public void fatalError(SAXParseException spe)                   // @C1A
    {                                                               // @C1A
        error(spe);                                                 // @C1A
    }
    
    public void warning(SAXParseException spe)                      // @C1A
    {                                                               // @C1A
        int lineNumber = spe.getLineNumber();                       // @C1A
        int columnNumber = spe.getColumnNumber();                   // @C1A
        
    	// Filename is either the source file or the DTD file 
    	// based on the line number because the DTD was prepended to the
    	// source as an inline DTD.
    	String fname;                                               // @C1A

    	// Adjust the line number to account for the PCML header
    	if (!(lineNumber - m_headerLineCount > 0))                  // @C1A
    	{                                                           // @C1A
    		// We have an error in the DTD.
    		// Change the filename.
    		fname = "pcml.dtd";                                     // @C1A
    	}                                                           // @C1A
    	else                                                        // @C1A
    	{                                                           // @C1A
        	fname = m_filename;                                     // @C1A
    	    lineNumber -= m_headerLineCount;                        // @C1A
    	}                                                           // @C1A

   		System.err.println(fname + "[" + lineNumber + "," + (columnNumber - 1) + "]: " + spe.getMessage() ); // @C1A
    }                                                               // @C1A

    public ParseException getException()
    {
    	return m_exc;
    }
}
