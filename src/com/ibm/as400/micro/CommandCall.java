///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: CommandCall.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.io.*;
import com.ibm.as400.access.MEConstants;

/**
 The CommandCall class represents an iSeries command object.  This class allows the user to call a CL command.
 <P>The following example demonstrates the use of CommandCall:
 <br>
 <pre>
    // Work with commands.
    AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
    try
    {
        // Run the command "CRTLIB FRED."
        String messages = CommandCall.run(system, "CRTLIB FRED");
        if (messages != null)
        {
            // Note that there was an error.
            System.out.println("Command failed:");
            for (int i = 0; i < messages.length; ++i)
            {
                System.out.println(messages[i]);
            }
        }
        else
        {
            System.out.println("Command succeeded!");
        }
    }
    catch (Exception e)
    {
        System.out.println("Command issued an exception!");
        e.printStackTrace();
    }
    // Done with the system.
    system.disconnect();
 </pre>
 **/
public final class CommandCall 
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

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
     *  @return  the message text  returned from running the command.
     *  
     *  @exception  IOException  If an error occurs while communicating with the system.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public static String[] run(AS400 system, String command) throws IOException, MEException
    {
        synchronized(system)
        {
            system.signon(); 
            system.toServer_.writeInt(MEConstants.COMMAND_CALL);
            system.toServer_.writeUTF(command);
            system.toServer_.flush();

            int numReplies = system.fromServer_.readInt();

            if (numReplies == MEConstants.EXCEPTION_OCCURRED)
            {
                int rc = system.fromServer_.readInt();
                String msg = system.fromServer_.readUTF();
                throw new MEException(msg,rc);
            }
            else
            {
                String[] replies = new String[numReplies];
                
                // Retrieve the messages.
                for (int i=0; i<numReplies; ++i)
                {
                    replies[i] = system.fromServer_.readUTF();
                }

                return replies;
            }
        }
    }
}
