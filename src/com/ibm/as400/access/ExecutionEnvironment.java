///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ExecutionEnvironment.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Locale;

class ExecutionEnvironment
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";
    
    // No need for instances of this class.
    private ExecutionEnvironment()
    {
    }

    // Get the "best guess" CCSID for the server based on the default locale.
    // @return  The CCSID.
    static int getBestGuessAS400Ccsid()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting best guess CCSID.");
        try
        {
            String localeString = Locale.getDefault().toString();
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Default Locale: " + localeString);
            // Search from most specific to most general.
            while (true)
            {
                String ccsidString = (String)ConversionMaps.localeCcsidMap_.get(localeString);
                if (ccsidString != null)
                {
                    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Best guess for Locale: " + localeString + ", CCSID: " + ccsidString);
                    return Integer.parseInt(ccsidString);
                }
                localeString = localeString.substring(0, localeString.lastIndexOf('_'));
            }
        }
        catch (Exception e)
        {
            // If all else fails return 37.
            Trace.log(Trace.DIAGNOSTIC, "Exception taking best guess CCSID, default to 37:", e);
            return 37;
        }
    }

    // Get the CCSID for this execution environment.
    // @return  The CCSID.
    static int getCcsid()
    {
        return 13488;  // Unicode.
    }

    // Get the corresponding CCSID number as a String.
    static int getCcsid(String encoding)
    {
        return Integer.parseInt((String)ConversionMaps.encodingCcsid_.get(encoding));
    }

    // Get the corresponding encoding from a CCSID.
    static String getEncoding(int ccsid)
    {
        return (String)ConversionMaps.ccsidEncoding_.get(String.valueOf(ccsid));
    }

    // Get the NLV for the given Locale.
    // @return  String that represents the national language version.
    static String getNlv(Locale locale)
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Getting NLV.");
        try
        {
            String localeString = locale.toString();
            if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "NLV Locale: " + localeString);
            // Search from most specific to most general.
            while(true)
            {
                String nlvString = (String)ConversionMaps.localeNlvMap_.get(localeString);
                if (nlvString != null)
                {
                    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "NLV for Locale: " + localeString + ", NLV: " + nlvString);
                    return nlvString;
                }
                localeString = localeString.substring(0, localeString.lastIndexOf('_'));
            }
        }
        catch (Exception e)
        {
            // If all else fails return 2924.
            Trace.log(Trace.DIAGNOSTIC, "Exception getting NLV, default to 2924:", e);
            return "2924";
        }
    }
}
