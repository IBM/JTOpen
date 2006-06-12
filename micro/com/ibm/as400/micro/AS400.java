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
 *  The AS400 class represents a sign-on to the i5/OS host servers from a wireless device.
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
    private byte[] bytes_;

    private String MEServer_;                 // ME server system name.

    private boolean signedOn_ = false;           // Sign-on status.

    // increment datastream level whenever a client/server datastream change is made.
    private static final int DATASTREAM_LEVEL = 0;  

    private static final Random rng_ = new Random();

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


    // Copied from com.ibm.as400.access.BinaryConverter
    static byte[] charArrayToByteArray(char[] charValue)
    {
        if (charValue == null)
            return null;

        byte[] byteValue = new byte[charValue.length * 2];
        int inPos = 0;
        int outPos = 0;

        while (inPos < charValue.length)
        {
            byteValue[outPos++] = (byte)(charValue[inPos] >> 8);
            byteValue[outPos++] = (byte)charValue[inPos++];
        }

        return byteValue;
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
    private static byte[] encode(byte[] adder, byte[] mask, byte[] bytes)
    {
        int length = bytes.length;
        byte[] buf = new byte[length];

        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(bytes[i] + adder[i % adder.length]);
        }

        for (int i = 0; i < length; ++i)
        {
            buf[i] = (byte)(buf[i] ^ mask[i % mask.length]);
        }

        return buf;
    }


    // This method is removed because the J2MEWTK by default when it preverifies
    // the classes uses the -nofinalize option.  So it is now up to the
    // application to properly disconnect.
    //
    ///**
    // *  When the garbage collector calls finalize(), this
    // *  method will call disconnect() on the AS400 object.
    // **/
    //protected void finalize()
    //{
    //    disconnect();
    //}


    /**
     *  Connect to an i5/OS system.<p>
     *
     *  A connection is typically made implicitly; therefore, this method does not have to be 
     *  called to connect to the system. This method can be used to control when the connection 
     *  is established.
     *
     *  @exception  IOException  If an error occurs while communicating with the system.
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

                // Tell the server what our datastream level is.
                toServer_.writeInt(DATASTREAM_LEVEL);          

                // Exchange random seeds.
                byte[] proxySeed = nextBytes(rng_, MEConstants.ADDER_LENGTH/*PROXY_SEED_LENGTH_*/);
                toServer_.write(proxySeed);
                toServer_.flush();

                int serverDatastreamLevel = fromServer_.readInt();
                byte[] remoteSeed = new byte[MEConstants.MASK_LENGTH/*REMOTE_SEED_LENGTH_*/];
                fromServer_.readFully(remoteSeed);

                // Design note: On Palm devices, the only encoding that is guaranteed to be supported is ISO8859_1.
                // On MIDP, the default encoding is in the system property "microedition.encoding".

                toServer_.writeUTF(systemName_);
                toServer_.writeUTF(userId_);

                byte[] encodedBytes = encode(proxySeed, remoteSeed, bytes_);
                toServer_.writeInt(encodedBytes.length);
                toServer_.write(encodedBytes);  //twiddled and encoded password
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
    private static byte[] store(String info)
    {
        byte[] adder = nextBytes(rng_, MEConstants.ADDER_LENGTH);
        byte[] mask = nextBytes(rng_, MEConstants.MASK_LENGTH);

        byte[] infoBytes = encode(adder, mask, charArrayToByteArray(info.toCharArray()));
        byte[] returnBytes = new byte[MEConstants.ADDER_PLUS_MASK_LENGTH + infoBytes.length];

        System.arraycopy(adder, 0, returnBytes, 0, MEConstants.ADDER_LENGTH);
        System.arraycopy(mask, 0, returnBytes, MEConstants.ADDER_LENGTH, MEConstants.MASK_LENGTH);
        System.arraycopy(infoBytes, 0, returnBytes, MEConstants.ADDER_PLUS_MASK_LENGTH, infoBytes.length);

        return returnBytes;
    }


    // Randomly generate bytes and put them into a new byte array.
    static byte[] nextBytes(Random r, int numBytes)
    {
        byte[] buf = new byte[numBytes];

        long feeder = r.nextLong();

        int feederIndex = 0;  // We will step through the bytes of the long, 1 byte at a time.

        for (int i=0; i<buf.length; i++)
        {
            if (feederIndex > 7)  // time to grab another long
            {
                feeder = r.nextLong();
                feederIndex = 0;
            }

            buf[i] = (byte)(0xFF & (feeder >> 8*feederIndex++));
        }

        return buf;
    }
}
