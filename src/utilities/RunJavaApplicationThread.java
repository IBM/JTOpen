///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RunJavaApplicationThread.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package utilities;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.JavaApplicationCall;
import com.ibm.as400.access.Trace;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ResourceBundle;

/**
    This class is only used by RunJavaApplication class.
**/
class RunJavaApplicationThread extends Thread
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Indicates which thread should be started.
    final static int RUN_APPLICATION = 1;
    final static int OUTPUT = 2;
    final static int INPUT = 3;
    final static int ERROR = 4;
    // Where MRI comes from.
    private static ResourceBundle resources_ = ResourceBundle.getBundle("utilities.UTMRI");

    private int threadID_ = 0;
    private JavaApplicationCall runMain_ = null;

    /**
       Constructor.
    **/
    RunJavaApplicationThread(JavaApplicationCall jac, int whichThread)
    {
        runMain_ = jac;
        threadID_ = whichThread;
    }

    /**
       Start the threads.
    **/
    public void run()
    {
        if (threadID_ == RUN_APPLICATION)
        {
            System.out.println(resources_.getString("REMOTE_START_PROGRAM")
                               +" "+runMain_.getJavaApplication()+"\n");

            boolean success = false;

            try
            {
               success = runMain_.run();
            }
            catch (Exception e)
            {
               System.out.println(resources_.getString("REMOTE_CALL_JAVA_ERROR"));
               System.out.println(e.toString());
            }

            com.ibm.as400.access.AS400Message[] messageList = runMain_.getMessageList();

            if ((messageList != null) && (messageList.length > 0))
            {
                for(int i=0; i<messageList.length;i++)
                {
                     System.out.print  ( messageList[i].getID() );
                     System.out.print  (resources_.getString("REMOTE_MESSAGE_FROM_COMMAND_SEP"));
                     System.out.println( messageList[i].getText() );
                }
            }
            System.out.println();
            System.out.print(resources_.getString("REMOTE_PROMPT"));
        }


        else if (threadID_ == OUTPUT)
        {
            // receives output string from server, prints it by System.out
            while(true)
            {
                String so = null;
                while (true)
                {
                    so = runMain_.getStandardOutString();
                    if (so != null)
                        System.out.println(so);
                    else
                        break;
                }
                delay();
            }
        }

        else if (threadID_ == ERROR)
        {
            // receives error string from server, prints it by System.out
            while(true)
            {
                String se = null;
                while (true)
                {
                    se = runMain_.getStandardErrorString();
                    if (se != null)
                        System.out.println(se);
                    else
                        break;
                }
                delay();
            }
        }

        else //threadID_ == INPUT
        {
            InputStreamReader reader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(reader);

            while(true)
            {
                try
                {
                    String data = bufferedReader.readLine();

                    if (data != null)
                        RunJavaApplication.getInputBuffer().addElement(data);
                }
                catch (Exception e)
                {
                    Trace.log(Trace.ERROR, e.toString(), e);
                }
                delay();
            }
        }
    }

    private void delay()
    {
       try { sleep(100); }
       catch (Exception e) {}
    }
}
