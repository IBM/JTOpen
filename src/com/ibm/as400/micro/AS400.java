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

import java.io.*;
import java.util.Random;
import javax.microedition.io.*;

/**
 *  The AS400 class represents an iSeries system sign-on from a wireless device.
 *  This class provides a modified subset of the functions available in 
 *  com.ibm.as400.access.AS400.
 *
 *  <P>The following example demonstrates the use of AS400:
 *  <br>
 *  <pre>
 *  AS400 system = new AS400("mySystem", "myUserid", "myPwd", "myMEServer");
 *  try
 *  {
 *      system.connect();
 *  }   
 *  catch (Exception e)
 *  {
 *      // Handle the exception
 *  }
 *  // Done with the system object.
 *  system.disconnect();
 *  </pre>
 *
 *  @see com.ibm.as400.access.AS400
 **/
public final class AS400 
{
    private String systemName_;             // System name.
    private String userId_;                     // User ID.
    
    // Password bytes twiddled.
    private char[] bytes_ = null;
    
    private String MEServer_;                 // ME server system name.

    private boolean signedOn_ = false;           // Sign-on status.


    // Client side ME connection information.
    DataInputStream fromServer_ = null;
    DataOutputStream toServer_ = null;
    private Connection    client_ = null;


    /**
     *  Constructs an AS400 object.
     *  @param  systemName  The name of the system.
     *  @param  userId  The user ID to use to connect to the system.
     *  @param  password  The password to use to connect to the system.
     *  @param  MEServer  The system name and port of the ME server in the format <code>serverName[:port]</code>.  
     *                            If no port is specified, the default will be used.
     **/
    public AS400(String systemName, String userId, String password, String MEServer)
    {
        if (systemName == null)
            throw new NullPointerException("systemName");

        if (userId == null)
            throw new NullPointerException("userId");

        if (password == null)
            throw new NullPointerException("password");

        if (MEServer == null)
            throw new NullPointerException("MEServer");

        systemName_ = systemName;
        userId_ = userId;
        bytes_ = store(password);

        // determine if a port was specified.
        int colon = MEServer.indexOf(':');
        if (colon >= 0)
        {
            // if a colon and a port were correctly specified, otherwise get the MEServer name and use the default port.
            if (colon < MEServer.length() - 1)
                MEServer_ = MEServer;
            else
                MEServer_ = MEServer.substring(0, colon) + MEConstants.ME_SERVER_PORT;
        }
        else
            MEServer_ = MEServer + ":" + MEConstants.ME_SERVER_PORT;
    }


    /**
     *  Disconnects the client from the MEServer.  All socket connections associated with this object will be closed.
     **/
    public void disconnect()
    {
        synchronized(this)
        {
            if (toServer_ == null)
                return;

            try
            {
                toServer_.writeInt(MEConstants.DISCONNECT);
                toServer_.flush();
            }
            catch (Exception ioe)
            { /* Ignore */
            }

            try
            {
                toServer_.close();
            }
            catch (Exception ioe)
            { /* Ignore */
            }

            try
            {
                fromServer_.close();
            }
            catch (Exception ioe)
            { /* Ignore */
            }

            try
            {
                client_.close();
            }
            catch (Exception ioe)
            { /* Ignore */
            }

            signedOn_ = false;
        }
    }


    // Scramble some bytes.
    private static char[] encode(char[] adder, char[] mask, char[] bytes)
    {
        if (bytes == null)
            return null;

        int length = bytes.length;
        char[] buf = new char[length];

        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(bytes[i] + adder[i % 9]);
        }

        for (int i = 0; i < length; ++i)
        {
            buf[i] = (char)(buf[i] ^ mask[i % 7]);
        }

        return buf;
    }



    /**
     *  When the garbage collector calls finalize(), this
     *  method will call disconnect() on the AS400 object.
     **/
    protected void finalize()
    {
        disconnect();
    }


    /**
     *  Connect to an iSeries server.<p>
     *
     *  A connection is typically made implicitly; therefore, this method does not have to be 
     *  called to connect to the iSeries. This method can be used to control when the connection 
     *  is established.
     *
     *  @exception  IOException  If an error occurs while communicating with the iSeries server.
     *  @exception  MEException  If an error occurs while processing the ToolboxME request.
     **/
    public boolean connect() throws IOException, MEException
    {
        synchronized(this)
        {
            // If we haven't already signed on.
            if (!signedOn_)
            {
                client_ = Connector.open("socket://" + MEServer_ , Connector.READ_WRITE);

                toServer_ = new DataOutputStream(((OutputConnection)client_).openOutputStream());
                fromServer_ = new DataInputStream(((InputConnection)client_).openInputStream());

                toServer_.writeInt(MEConstants.SIGNON);
                toServer_.writeUTF(systemName_);
                toServer_.writeUTF(userId_);
                toServer_.writeUTF( new String(bytes_) );  //password
                toServer_.flush();
                

                int retVal = fromServer_.readInt();

                if (retVal == MEConstants.SIGNON_FAILED || retVal == MEConstants.EXCEPTION_OCCURRED)
                {
                    signedOn_ = false;
                    int rc = fromServer_.readInt();
                    String msg = fromServer_.readUTF();

                    disconnect();

                    throw new MEException(msg,rc);
                }
                
                signedOn_ = true;
            }

            return signedOn_;
        }
    }


    // Twiddle password bytes.
    private static  char[] store(String info)
    {
        Random rng = new Random();
        
        char[] adder = nextBytes(rng, 18);
        char[] mask = nextBytes(rng, 14);

        char[] infoBytes = encode(adder, mask, info.toCharArray());
        char[] returnBytes = new char[info.length() + 16];

        System.arraycopy(adder, 0, returnBytes, 0, 9);
        System.arraycopy(mask, 0, returnBytes, 9, 7);
        System.arraycopy(infoBytes, 0, returnBytes, 16, info.length());

        return returnBytes;
    }


    // Randomly generate bytes and put them into a new char array
    private static  char[] nextBytes(Random r, int numBytes)
    {
        char[] buf = new char[numBytes/2]; // 2 bytes for each char
        int length = buf.length / 4;             // 4 chars for each long
        
        for (int i=0; i<length; ++i)
        {
            long l =  r.nextLong();
            int offset = i*4;
            
            for (int j=0; j<4 && (j+offset) < buf.length; ++j, ++offset)
            {
                buf[j+offset] = (char)(((byte)(0xFF & l) << 8) + ((byte)(0xFF & l >> 8))); 
                l = l >> 16;
            }
        }
        
        return buf;
    }
}
