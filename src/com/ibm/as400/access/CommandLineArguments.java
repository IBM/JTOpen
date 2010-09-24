///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: CommandLineArguments.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;



/**
 *  A utility that parses command line arguments into
 *  options specified in the format "-optionName optionValue".
 *  <p>
 *  Here is an example of calling a program from the command line with arguments:
 *  <br>
 *  <BLOCKQUOTE><PRE>
 *  A sample program:  java myProgram systemName -userid myID -password myPWD
 *  <p>
 *  The Java code to parse the command:
 *  <br>
 *  // Create a vector to hold all the defined/expected command line arguments.
 *  Vector options = new Vector();
 *  options.addElement("-userID");
 *  options.addElement("-password");
 *  <p>
 *  // Create a Hashtable to map shortcuts to the command line arguments. 
 *  Hashtable shortcuts = new Hashtable();
 *  shortcuts.put("-u", "-userID");
 *  shortcuts.put("-p", "-password");
 *  <p>
 *  // Create a CommandLineArguments object with the args array passed into main(String args[])
 *  // along with the vector and hashtable just created.
 *  CommandLineArguments arguments = new CommandLineArguments(args, options, shortcuts);
 *  <p>
 *  // Get the name of the IBM i system that the user wants to run to.
 *  String system = arguments.getOptionValue("");
 *  <p>
 *  // Get the user ID that the user wants to log in with.
 *  String uid = arguments.getOptionValue("-userID");
 *  <p>
 *  // Get the password that the user wants to log in with.
 *  String pwd = arguments.getOptionValue("-password");
 *  
 *  </PRE></BLOCKQUOTE>
 *
**/
public class CommandLineArguments                               //$B1C
{
    // Private data.    
    private Vector      extraOptions_   = new Vector ();
    private Hashtable   map_            = new Hashtable ();


/**
Creates a CommandLineArguments object.

@param  args    The command line arguments.
**/
    public CommandLineArguments (String[] args)
    {
        this (args, null, new Hashtable ());
    }



/**
Creates a CommandLineArguments object.

@param args                 The command line arguments.
@param expectedOptions      The expected options.  This is a Vector of
                            Strings, each starting with a hyphen ("-").
                            These are not case sensitive.  Specify null
                            if any options are expected.
@param shortcuts            The shortcuts.  This is a Hashtable where
                            the keys are Strings for the shortcuts (e.g. "-?") and
                            the elements are Strings for the option (e.g. "-help").
                            All strings start with a hyphen ("-").  Specify
                            null if no shortcuts are used.
**/
    public CommandLineArguments (String[] args, Vector expectedOptions, Hashtable shortcuts)
    {
        // Lowercase all of the expected option names.
        if (expectedOptions != null) {
            Vector normalizedExpectedOptions = new Vector ();
            Enumeration list = expectedOptions.elements ();
            while (list.hasMoreElements ()) 
                normalizedExpectedOptions.addElement (list.nextElement ().toString ().toLowerCase ());        
            expectedOptions = normalizedExpectedOptions;
        }

        // Parse through the args.

        // Note: Any leading arguments that aren't preceded by an option name,
        // are associated as values to the special "unnamed" option, "-".
        String currentOptionName = "-";
        StringBuffer currentOptionValue = new StringBuffer ();
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (isOptionName(arg)) {                                            // @A2C
                map_.put (currentOptionName, currentOptionValue);
                currentOptionName = arg.toLowerCase ();

                if (shortcuts != null)
                    if (shortcuts.containsKey (currentOptionName))
                        currentOptionName = ((String) shortcuts.get (currentOptionName)).toLowerCase ();

                if (expectedOptions != null) 
                    if (! expectedOptions.contains (currentOptionName))
                        extraOptions_.addElement (currentOptionName);

                if (map_.containsKey (currentOptionName))
                    currentOptionValue = (StringBuffer) map_.get (currentOptionName);
                else
                    currentOptionValue = new StringBuffer ();
            }
            else {
                currentOptionValue.append (" ");
                currentOptionValue.append (arg);
            }
        }
        map_.put (currentOptionName, currentOptionValue);
    }



/**
Returns the list of any extra options that were specified.
These are options that the application was not expecting.

@return The list of extra options.  This is an Enumeration
        which contains a String for each extra option.
        If there were no extra options, an empty Enumeration is returned.
**/
    public Enumeration getExtraOptions ()
    {
        return extraOptions_.elements ();
    }



/**
Returns the list of option names that were specified.

@return The list of option names.  This is an Enumeration
        which contains a String for each option name.
        Note: The list may include the special "unnamed" option, "-",
        which is aliased as "".
**/
    public Enumeration getOptionNames ()
    {
        return map_.keys ();
    }



/**
Returns the value of an option.

@param optionName   The option name.
@return             The option value, or null if the 
                    option was not specified.
**/
    public String getOptionValue (String optionName)
    {
        // Note: We map "" to the special (unnamed) option "-".
        String key;
        if (! optionName.startsWith ("-"))
            key = "-" + optionName.toLowerCase ();
        else
            key = optionName.toLowerCase ();

        if (map_.containsKey (key))
            return map_.get (key).toString ().trim ();
        else
            return null;
    }



/**
Indicates whether the option was specified.

@param optionName   The option name.
@return             <tt>true</tt> if the option was specified; <tt>false</tt> otherwise.
**/
    public boolean isOptionSpecified (String optionName)
    {
        return ( getOptionValue(optionName) != null );
    }



    private boolean isOptionName(String arg)                                        // @A2A
    {                                                                               // @A2A
        if (! arg.startsWith("-"))                                                  // @A2A
            return false;                                                           // @A2A
                                                                                    // @A2A
        // Allow numeric option values, but disallow numeric option names.          // @A2A
        // This is to avoid mistaking a negative number for an option indicator.
        try {                                                                       // @A2A
            Double.valueOf(arg.substring(1));                                       // @A2A
            return false;                                                           // @A2A
        }                                                                           // @A2A
        catch(NumberFormatException e) {                                            // @A2A
            return true;                                                            // @A2A
        }                                                                           // @A2A
    }                                                                               // @A2A



/**
The main() method for test this class.  This should
be commented out for production code.
**/
    /*
    public static void main (String[] args)
    {
        Hashtable shortcuts = new Hashtable ();
        shortcuts.put ("-sc", "-shortcut");

        CommandLineArguments cla = new CommandLineArguments (args, null, shortcuts);
        Enumeration list = cla.getOptionNames ();
        while (list.hasMoreElements ()) {
            String optionName = (String) list.nextElement ();
            String optionValue = cla.getOptionValue (optionName);
            System.out.println ("Option:");
            System.out.println ("  name:  [" + optionName + "]");
            System.out.println ("  value: [" + optionValue + "]");
        }
    }
    */


}

