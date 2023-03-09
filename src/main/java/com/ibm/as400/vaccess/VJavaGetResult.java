///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VJavaGetResult.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JTextArea;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.JavaApplicationCall;
import com.ibm.as400.access.Trace;

/**
 * The VJavaGetResult class runs the java application
 * and returns its results to the VJavaApplicationCall.
 */
class VJavaGetResult implements Runnable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private variable representing the object of VJavaApplicationCall.
    private VJavaApplicationCall vJavaAppCall_;

    // Private variable representing the object of JavaApplicationCall.
    private JavaApplicationCall javaAppCall_;

    // Private variable indicating when the java application is
    // complete.
    private boolean javaAppRunOver_ = false;

    // Private variable representing the thread that runs the method call
    // of JavaApplicationCall.
    private Thread runAppThread_;

    // Private variable representing the thread that gets the results
    // from the java application.
    private Thread outputThread_;

    // Private variable representing the thread that gets the error information.
    private Thread errorThread_;

    // This counter is used to make sure we don't close the sockets
    // before all output is out of the pipe.  The main thread (the
    // one just returned from the the program call) will wait to
    // close sockets until all data is out of the sockets.
    long readCounter_ = 0;                            // @D1A

    /**
     * Constructs a VJavaGetResult object.
     *
     * @param vJavaAppCall The VJavaApplicationCall object.
    **/
    public VJavaGetResult(VJavaApplicationCall vJavaAppCall)
    {
        vJavaAppCall_ = vJavaAppCall;
        javaAppCall_  = vJavaAppCall_.getJavaApplicationCall();
    }


    private void delay()                                     //@D1a
    {                                                        //@D1a
       try { Thread.sleep(100); }                            //@D1a
       catch (Exception e) {}                                //@D1a
    }                                                        //@D1a


    /**
     * Stops the threads.
    **/
    protected void finalize() throws Throwable
    {
        runAppThread_ = null;
        errorThread_  = null;
        outputThread_ = null;
        super.finalize();
    }

    /**
     * Starts the threads for running java application and gets the results.
    **/
    public void play()
    {
        javaAppRunOver_ = false;
        outputThread_ = new Thread(this);
        errorThread_  = new Thread(this);
        outputThread_.start();
        errorThread_.start();

        runAppThread_ = new Thread(this);
        runAppThread_.start();
    }

    /**
     * Runs the java application and gets the results.
     **/
    public void run()
    {

        if(Thread.currentThread() == runAppThread_)
        {
            vJavaAppCall_.setJavaAppRunOver(false);
            try
            {
                javaAppCall_.run();
            }
            catch(Exception e)
            {
                Trace.log(Trace.ERROR,e.toString());
                vJavaAppCall_.appendOutput(e.toString());
            }

            com.ibm.as400.access.AS400Message[] messageList = javaAppCall_.getMessageList();
            if ((messageList != null) && (messageList.length > 0 ))
            {
                for(int i=0; i<messageList.length;i++)
                {
                    vJavaAppCall_.appendOutput( messageList[i].getID());
                    vJavaAppCall_.appendOutput(ResourceLoader.getText("REMOTE_COMMAND_MESSAGE_SEP"));
                    vJavaAppCall_.appendOutput( messageList[i].getText()+"\n");
                }
            }


            // The following will wait until all data is out of the
            // standard error and standard out pipe before closing
            // them.  The standard error/out code will increment the
            // counter every time they get data from the socket.
            // This code will keeping looping until all data
            // is out of the sockets.  This code will not wait
            // forever.  This thread is the one that called
            // the Java API.  Since it has control again, the
            // Java program on the server is done.  We just have
            // to get all the data out of the sockets.
            long localCounter = readCounter_;                       // @D1A
                                                                    // @D1A
            try { Thread.sleep(100); } catch (Exception e) {}       // @D1A
                                                                    // @D1A
            while (localCounter < readCounter_)                     // @D1A
            {                                                       // @D1A
               localCounter = readCounter_;                         // @D1A
               try { Thread.sleep(100); } catch (Exception e) {}    // @D1A
            }                                                       // @D1A

            vJavaAppCall_.setJavaAppRunOver(true);
            javaAppRunOver_ = true;
            vJavaAppCall_.appendOutput("\n"+ResourceLoader.getText("REMOTE_PROMPT"));
        }
        else if(Thread.currentThread() == outputThread_)
        {
            // stream used to receive output from server
            String receiveStr;
            while(!javaAppRunOver_)
            {
                while(true)
                {
                    receiveStr = javaAppCall_.getStandardOutString();
                    if (receiveStr != null)
                    {
                        readCounter_++;             // @D1A
                        vJavaAppCall_.appendOutput(receiveStr+"\n");
                    }
                    else
                        break;
                }
                delay();                            // @D1A
            }
        }
        else
        {
           // stream used to receive error message from server
            String errorStr = null;
            while(!javaAppRunOver_)
            {

                while(true)
                {
                    errorStr = javaAppCall_.getStandardErrorString();
                    if (errorStr != null)
                    {
                        readCounter_++;             // @D1A
                        vJavaAppCall_.appendOutput(errorStr+"\n");
                    }
                    else
                        break;
                }
                delay();                            // @D1A
            }
        }
    }
}

