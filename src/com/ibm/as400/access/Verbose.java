///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: Verbose.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.PrintStream;



/**
The Verbose class prints verbose output depending
on the state of a flag.
**/
class Verbose
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    private String x = Copyright.copyright;


    // Private data.
    private static PrintStream output_         = System.out;
    private static boolean state_              = false;



/**
Prints text.

@param text The text.
**/
    public static void forcePrintln (String text)
    {
        output_.println (text);
        if (Trace.isTraceInformationOn ())
            Trace.log (Trace.INFORMATION, text);
    }



/**
Indicates if verbose output is printed.

@return true if verbose output is printed; false otherwise.
**/
    public static boolean isVerbose ()
    {
        return state_;
    }



    public static void println (Exception e)
    {
        if (state_) {
            String text = e.getMessage();
            if (text == null)
                text = e.getClass().getName();
            forcePrintln(text);
        }
    }



/**
Prints text, if appropriate.

@param text The text.
**/
    public static void println (String text)
    {
        if (state_)
            forcePrintln (text);
    }



/**
Sets whether verbose output is printed.

@param verbose true if verbose output is printed; false otherwise.
**/
    public static void setVerbose (boolean verbose)
    {
        state_ = verbose;
    }




}
