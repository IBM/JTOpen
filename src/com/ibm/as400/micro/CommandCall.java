///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.IOException;

/**
 *  The CommandCall class represents an iSeries command object.  This class allows the user to call a CL command 
 *  from a wireless device.  This class provides a modified subset of the functions available in 
 *  com.ibm.as400.access.CommandCall.
 *
 *  <P>The following example demonstrates the use of CommandCall:
 *  <br>
 *  <pre>
 *   // Work with commands.
 *   AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
 *   try
 *   {
 *       // Run the command "CRTLIB FRED."
 *       String[] messages = CommandCall.run(system, "CRTLIB FRED");
 *       if (messages != null)
 *       {
 *           // Note that there was an error.
 *           System.out.println("Command failed:");
 *           for (int i = 0; i < messages.length; ++i)
 *           {
 *               System.out.println(messages[i]);
 *           }
 *       }
 *       else
 *       {
 *           System.out.println("Command succeeded!");
 *       }
 *   }
 *   catch (Exception e)
 *   {
 *       // Handle the exception
 *   }
 *   // Done with the system object.
 *   system.disconnect();
 *  </pre>
 *
 *  @see com.ibm.as400.access.CommandCall
 **/
public final class CommandCall 
{
    /**
     *  Private CommandCall constructor
     **/
    private CommandCall()
    {  }


    /**
     *  Runs the command on the system.
     *  @param  command  The command to run on the system.  If the command is not library qualified, the library list from the 
     *                          job description specified in the user profile will be used to find the command.
     *  
     *  @return  the message text  returned from running the command.  An empty array will be return if there were no messages
     *               or if the command completed successfully.
     *  
     *  @exception  IOException  If an error occurs while communicating with the server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static String[] run(AS400 system, String command) throws IOException, MEException
    {
        if (system == null)
            throw new NullPointerException("system");
        
        if (command == null)
            throw new NullPointerException("command");

        synchronized(system)
        {
            system.connect(); 
            system.toServer_.writeInt(MEConstants.COMMAND_CALL);
            system.toServer_.writeUTF(command);
            system.toServer_.flush();

            // The returnValue indicates either an exception occurred or
            // the number of AS400Messages that will be returned.  The
            // maximum number of messages that will be returned is 10.
            int returnValue = system.fromServer_.readInt();

            if (returnValue == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg,rc);
            }
            else
            {
                String[] replies = new String[returnValue];
                
                // Retrieve the messages.
                for (int i=0; i<returnValue; ++i)
                {
                    replies[i] = system.fromServer_.readUTF();
                }

                return replies;
            }
        }
    }
}
