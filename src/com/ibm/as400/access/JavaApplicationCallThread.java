///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JavaApplicationCallThread.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;

/**
    This class is only used by class JavaApplicationCall.
**/
class JavaApplicationCallThread extends Thread
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Socket errorSocket_ = null;
    Socket readSocket_  = null;
    Socket writeSocket_ = null;

    ServerSocket error_ = null;
    ServerSocket read_  = null;
    ServerSocket write_ = null;

    BufferedReader err_ = null;
    BufferedReader in_  = null;
    PrintWriter out_    = null;

    // This counter is used to make sure we don't close the sockets
    // before all output is out of the pipe.  The main thread (the
    // one just returned from the the program call) will wait to
    // close sockets until all data is out of the sockets.
    long readCounter_ = 0;

    /**
        Constructor.
    **/
    JavaApplicationCallThread(ServerSocket write, ServerSocket read, ServerSocket error)
    {
        read_ = read;
        write_ = write;
        error_ = error;
    }


    /**
        Closes sockets communicating with AS/400.
    **/
    void closeSockets()
    {
        // The following will wait until all data is out of the
        // standard error and standard out pipe before closing
        // them.  The standard error/out code will increment the
        // counter every time they get data from the socket.
        // This code will keeping looping until all data
        // is out of the sockets.  This code will not wait
        // forever.  This thread is the one that called
        // the Java API.  Since it has control again, the
        // Java program on the AS/400 is done.  We just have
        // to get all the data out of the sockets.
        long localCounter = readCounter_;

        try { Thread.sleep(500); } catch (Exception e) {}

        while (localCounter < readCounter_)
        {
           localCounter = readCounter_;
           try { Thread.sleep(500); } catch (Exception e) {}
        }

        try
        {
            if (in_ != null)
               in_.close();

            if (read_ != null)
               read_.close();

            if (readSocket_ != null)
               readSocket_.close();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, e.toString(), e);
        }
        finally
        {
            in_         = null;
            read_       = null;
            readSocket_ = null;
        }

        try
        {
            if (out_ != null)
               out_.close();

            if (write_ != null)
               write_.close();

            if (writeSocket_ != null)
               writeSocket_.close();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, e.toString(), e);
        }
        finally
        {
            out_         = null;
            write_       = null;
            writeSocket_ = null;
        }

        try
        {
            if (err_ != null)
               err_.close();

            if (error_ != null)
               error_.close();

            if (errorSocket_ != null)
               errorSocket_.close();

        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, e.toString(), e);
        }
        finally
        {
            err_         = null;
            error_       = null;
            errorSocket_ = null;
        }
    }

    /**
       Copyright.
    **/
    private static String getCopyright ()
    {
        return Copyright.copyright;
     }

    /**
        Returns the standard error come from the application running on AS/400.
    **/
    String getStandardErrorString()
    {
        String ret = null;

        if (err_ != null)
        {
           try
           {
               ret = err_.readLine();

               if (ret != null)
                  readCounter_++;
           }
           catch(Exception e)
           {
               Trace.log(Trace.ERROR, e.toString(), e);
           }
        }

        return ret;
    }

    /**
        Returns the standard output come from the application running on AS/400.
    **/
    String getStandardOutString()
    {
        String ret = null;

        if (in_ != null)
        {
           try
           {
               ret = in_.readLine();
               if (ret != null)
                  readCounter_++;
           }
           catch(Exception e)
           {
               Trace.log(Trace.ERROR, e.toString(), e);
           }
        }

        return ret;
    }

    /**
       Starts a thread to get the sockets before the call to Java application is executed.
    **/
    public void run()
    {
           try
           {
               readSocket_ = read_.accept();
               in_ = new BufferedReader(new InputStreamReader(readSocket_.getInputStream()));
           }
           catch (Exception e)
           {
               Trace.log(Trace.ERROR, e.toString(), e);
           }
           try
           {
               writeSocket_ = write_.accept();
               out_  = new PrintWriter(writeSocket_.getOutputStream(), true);
           }
           catch (Exception e)
           {
               Trace.log(Trace.ERROR, e.toString(), e);
           }
           try
           {
               errorSocket_ = error_.accept();
               err_ = new BufferedReader(new InputStreamReader(errorSocket_.getInputStream()));
           }
           catch (Exception e)
           {
            Trace.log(Trace.ERROR, e.toString(), e);
           }
    }


    /**
        Sends the standard input to the application running on AS/400.
    **/
    void sendStandardInString(String s)
    {
        if ((s!=null) && (out_ !=null))
        {
            out_.println(s);
        }
    }
}

