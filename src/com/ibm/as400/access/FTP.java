///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FTP.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.net.*;
import java.util.*;
import java.beans.*;


/**
 * The FTP class represents a generic ftp client.  Methods
 * on the FTP class allow you to connect to an ftp server,
 * send commands to the server, list files on the server,
 * get files from the server, and put files to the server.
 * <P>
 * Most methods that communicate with the server return a boolean
 * to indicate if the request was successful.  The message returned
 * from the server is also available.  getLastMessage() is used
 * to retrieve the message from the previous request.
 * <P>
 * By default, FTP command are sent via server port 21.  The initial
 * data transfer type is ASCII.  Passive mode is used.
 * <P>
 * No encryption is provided by this class.  The user and password
 * flow un-encrypted to the server.  This class is not SSL enabled.
 * <P>
 * The forward slash is the separator character for paths sent
 * to the FTP server.
 * <P>
 * Trace information is available by using the com.ibm.as400.access.Trace
 * class.  When trace is turned on via that class, FTP will
 * produce debug information.
 * <P>
 * The following example copies a set of files from a directory
 * on the server.
 * <a name="example"></a>
 * <ul>
 * <pre>
 * FTP client = new FTP("myServer", "myUID", "myPWD");
 * client.cd("/myDir");
 * client.setDataTransferType(FTP.BINARY);
 * String [] entries = client.ls();
 *
 * for (int i = 0; i < entries.length; i++)
 * {
 *    System.out.println("Copying " + entries[i]);
 *    try
 *    {
 *       client.get(entries[i], "c:\\ftptest\\" + entries[i]);
 *    }
 *    catch (Exception e)
 *    {
 *       System.out.println("  copy failed, likely this is a directory");
 *    }
 * }
 *
 * client.disconnect();
 *
 * </pre>
 * </ul>
**/

public class FTP implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;
    private static final boolean DEBUG = false;


    // **********************************************************
    // *                                                        *
    // * Don't forget to update readObject() if transient       *
    // * variables are added!                                   *
    // *                                                        *
    // **********************************************************

    // **********************************************************
    // *                                                        *
    // * Don't forget to update AS400FTP if a method is added   *
    // *                                                        *
    // **********************************************************

               // connect to this server, with the specified user and password
               // Do not just grab "clearPassword"!!!!  Always use getPassword()
    private String server_;
    private String user_;
    transient private String  clearPassword_ = null;
    transient private byte[]  encryptedPassword_ = null;
    transient private byte[]  mask_  = null;
    transient private byte[]  adder_ = null;
    transient private boolean encrypted_ = false;

    private final static int PARKED = 0;   //waiting to try first connection
    private final static int ACTIVE = 1;   //connection active
    private final static int FAILED = 2;   //connection no longer active,
                                           //  reconnect must be off
    transient private int connectionState_ = PARKED;

                         // flag used to make sure we don't go into an
                         // infininte loop calling connect
    transient private boolean inConnect_ = false;

    transient String  lastMessage_ = "";

               // default port
    private int port_ = 21;

               // socket for sending commands / receiving replies
    transient private Socket controlSocket_;

               // replies arrive via this reader
    transient private BufferedReader reader_;

               // requests are sent via this writer.
    transient private PrintWriter ps_;

    transient private boolean externallyConnected_ = false;    // @D2a
    boolean reuseSocket_ = true;  // default behavior is to reuse socket

               // Lists of listeners
    transient         PropertyChangeSupport changes_   = new PropertyChangeSupport(this);
    transient         VetoableChangeSupport vetos_     = new VetoableChangeSupport(this);
    transient         Vector                listeners_ = new Vector();


               // amount of data to transfer at one time
    private int bufferSize_ = 4096;

    private int mode_ = PASSIVE_MODE;

    /**
     * Transfer files in ASCII mode.
    **/
    public static final int ASCII  = 0;

    /**
     * Transfer files in binary mode.
    **/
    public static final int BINARY = 1;


    /**
     * Use active mode transfers with the server.
    **/
    public static final int ACTIVE_MODE = 10;

    /**
     * Use passive mode transfers with the server.
     * This is the default.
    **/
    public static final int PASSIVE_MODE = 11;


    private transient FTPThread activeModeObject_;
    private transient Thread activeModeThread_;

// -----------------------------------------------------------------------
   /**
    * Constructs an FTP object.
    * The server name, user and password must be set before
    * requests are sent to the server.
    **/

    public FTP()
    {
      checkSocketProperty();  // see if "reuseSocket" property is set
    }





// -----------------------------------------------------------------------
   /**
    * Constructs an FTP object.
    * The user and password must be set before requests are
    * sent to the server.
    *   @param server The system to which to connect.
   **/

    public FTP(String server)
    {
       try
       {
          setServer(server);
          checkSocketProperty();  // see if "reuseSocket" property is set
       }
       catch (PropertyVetoException e) {}
    }



// -----------------------------------------------------------------------
   /**
    * Constructs an FTP object.
    *   @param server   The system to which to connect.
    *   @param user   The userid to use during the login.
    *   @param password The password to use during the login.
   **/

    public FTP(String server, String user, String password)
    {
       try
       {
          setServer(server);
          setUser(user);
          setPassword(password);
          checkSocketProperty();  // see if "reuseSocket" property is set
       }
       catch (PropertyVetoException e) {}
    }




// -----------------------------------------------------------------------
   /**
    * Adds a listener to be notified when the value of any bound property
    * is changed.  It can be removed with removePropertyChangeListener.
    *
    *    @param listener The PropertyChangeListener.
    **/

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
       if (listener == null)
       {
          throw new NullPointerException("listener");
       }
       changes_.addPropertyChangeListener(listener);
    }




// -----------------------------------------------------------------------
   /**
    * Adds a listener to be notified when an FTP event is fired.
    *
    *    @param listener The object listener.
    **/

    public void addFTPListener(FTPListener listener)
    {
       if (listener == null)
       {
          throw new NullPointerException("listener");
       }
       listeners_.addElement(listener);
    }





// ---------------------------------------------------------------------------
// @D5 new method
   /**
    * Starts the process of appending data to a file on the server.  FTP
    * opens the data connection to the server, then opens the file on
    * the server and returns an output stream to the caller.  The caller
    * then writes the file's data to the output stream.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param fileName The file to put.
    *   @return An output stream to the file.  The caller uses the output
    *           stream to write data to the file.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized OutputStream append(String fileName)
                                     throws IOException
    {
          if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"entering append(file)");


          if (fileName == null)
             throw new NullPointerException("file");

          if (fileName.length() == 0)
             throw new IllegalArgumentException("file");

          return doAppendOrPut(fileName, "APPE");
    }





// ---------------------------------------------------------------------------
// @D5 new method
   /**
    * Appends data to a file on the server.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the server.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized boolean append(String sourceFileName, String targetFileName)
                   throws IOException
    {
         if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC,"entering append(String, String)");


         if (sourceFileName == null)
            throw new NullPointerException("source");

         if (sourceFileName.length() == 0)
            throw new IllegalArgumentException("source");

         if (targetFileName == null)
            throw new NullPointerException("target");

         if (targetFileName.length() == 0)
            throw new IllegalArgumentException("target");

         boolean result = append(new java.io.File(sourceFileName), targetFileName);

         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"leaving append(String, String)");

         return result;
    }






// ---------------------------------------------------------------------------
   /**
    * Appends data to a file on the server.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the server.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized boolean append(java.io.File sourceFileName, String targetFileName)
                   throws IOException
    {
         if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC,"entering append(File, String)");


         if (sourceFileName == null)
            throw new NullPointerException("source");

         if (targetFileName == null)
            throw new NullPointerException("target");

         if (targetFileName.length() == 0)
            throw new IllegalArgumentException("target");


         connect();

         byte[] buffer = new byte[bufferSize_];

         FileInputStream f = new FileInputStream(sourceFileName);
         OutputStream out = append(targetFileName);

         int length = f.read(buffer);

         while (length > 0)
         {
             out.write(buffer,0,length);
             length = f.read(buffer);
         }
         out.close();
         f.close();

         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"leaving append(String, String)");

         return true;
    }







// -----------------------------------------------------------------------
   /**
    * Adds a listener to be notified when the value of any
    * constrained property is changed.
    *
    *    @param listener The VetoableChangeListener.
    **/

    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
       if (listener == null)
       {
          throw new NullPointerException("listener");
       }
       vetos_.addVetoableChangeListener(listener);
    }





// ---------------------------------------------------------------------------
   /**
    * Sets the current directory on the server to <i>directory</i>.
    * The method is the same as setCurrentDirectory().
    * The message returned from the server is saved.  Use getLastMessage()
    * to retrieve it.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param directory The current directory to set on the server.
    *   @return true if directory changed; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized boolean cd(String directory)
                   throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering cd()");

        if (directory == null)
           throw new NullPointerException("directory");

        if (directory.length() == 0)
           throw new IllegalArgumentException("directory");


        connect();
        issueCommand("CWD " + directory);


        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving cd()");

        return lastMessage_.startsWith("250");
    }




// ---------------------------------------------------------------------------
   /**
    * Connects to the server.  The connection is via
    * port <i>port</i>.  The user and password must be set
    * before calling this method.
    * Calling connect is optional.  Methods that communicate
    * with the server such as get, put, cd, and ls call connect()
    * if necessary.
    * The message returned from the server is saved.  Use getLastMessage()
    * to retrieve it.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @return true if connection is successful; false otherwise.
    *   @exception UnknownHostException If a path to the server cannot be found.
    *   @exception IOException If an error occurs while connecting to the server.
    *   @exception IllegalStateException If called before user and password are set.
   **/

    public synchronized boolean connect()
         throws UnknownHostException, IOException, IllegalStateException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering connect()");

        inConnect_ = true;

        // possible state 1 -- we were connected but the connection
        // failed or timed out.  If reconnect is off simply return false
        if (connectionState_ == FAILED)
        {

           if (Trace.isTraceOn())
              Trace.log(Trace.DIAGNOSTIC,"leaving  connect(), state=failed");

           inConnect_ = false;
           return false;
        }

        // possible state 2 -- we are connected.
        if (connectionState_ == ACTIVE)
        {
           if (Trace.isTraceOn())
              Trace.log(Trace.DIAGNOSTIC,"leaving  connect(), state=active");

           inConnect_ = false;
           return true;
        }

        //possible state 3 -- this is the first try at connecting
        //or the connection failed and the user wants us to retry.
        //Attempt to connect to the server.
        if ((server_ == null) || (server_.length() == 0))
        {
           inConnect_ = false;
           throw new IllegalStateException("server");
        }

        if ((user_ == null) || (user_.length() == 0))
        {
           inConnect_ = false;
           throw new IllegalStateException("user");
        }

        String password_ = getPassword();
        if ((password_ == null) || (password_.length() == 0))
        {
           inConnect_ = false;
           throw new IllegalStateException("password");
        }


        controlSocket_ = new Socket(server_, port_);

        reader_ = new BufferedReader(new InputStreamReader(controlSocket_.getInputStream()));
        ps_ = new PrintWriter(controlSocket_.getOutputStream(), true);

        String s = readReply();

        login(user_, password_);

        if (lastMessage_.startsWith("230"))
           connectionState_ = ACTIVE;
        else
        {
           ps_.close();
           reader_.close();
           controlSocket_.close();
           connectionState_ = PARKED;
           inConnect_ = false;
           throw new java.lang.SecurityException();
        }

        inConnect_ = false;

        if (connectionState_ == ACTIVE)
           fireEvent(FTPEvent.FTP_CONNECTED);

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving  connect(), state = " + connectionState_ );

        return (connectionState_ == ACTIVE);
    }



// ---------------------------------------------------------------------------
   //
   // Decode the password so because it must be in the clear going
   // to the server.
   //

   private byte[] decode(byte[] data, byte[] mask, byte[] adder)
   {
       return decode(data, 0, data.length, mask, adder);
   }

   private byte[] decode(byte[] data, int offset, int len, byte[] mask, byte[] adder)
   {
       int i;
       byte[] b = new byte[len];

       for (i=0; i<len; i++)
       {
           b[i] = (byte)(mask[i%mask.length] ^ data[offset+i]);
       }

       for (i=0; i<len; i++)
       {
           b[i] = (byte)(b[i] - adder[i%adder.length]);
       }

       return b;
   }






// ---------------------------------------------------------------------------
   /**
    * Lists the contents of the current working directory.  File name and
    * attributes are returned for each entry in the directory.
    * An array of length zero is returned if the directory is empty.
    *   @return The contents of the directory as an array of Strings.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized String[] dir()
                    throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering dir(), (note, no exit dir() entry)");

        return list(true, null);                    // @D4c
    }






// ---------------------------------------------------------------------------
// @D4 new method
   /**
    * Lists the contents of the current directory.  File name and
    * attributes are returned for each entry in the directory.
    * A zero length array is returned if the directory is empty or
    * if no files meet the search criteria.
    *   @return The contents of the directory as an array of Strings.
    *   @param criteria The search criteria.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized String[] dir(String criteria)
                    throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering dir(), (note, no exit dir() entry)");

        return list(true, criteria);                // @D4c
    }





// ---------------------------------------------------------------------------
   /**
    * Closes the connection with the server.  The connection is closed
    * by sending the <i>quit</i> command to the server.
    * The message returned from the server is saved.  Use getLastMessage()
    * to retrieve it.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized void disconnect()
                             throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering disconnect()");

        if (connectionState_ == ACTIVE)
        {

           try
           {
              issueCommand("QUIT");
           }
           catch (Exception e) {}

           try
           {
             if (activeModeThread_ != null)
             {
               activeModeThread_.interrupt();
             }
           }
           catch (Exception e) {}

           ps_.close();
           reader_.close();
           controlSocket_.close();
           connectionState_ = PARKED;
           fireEvent(FTPEvent.FTP_DISCONNECTED);
        }

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving disconnect()");
    }






// ---------------------------------------------------------------------------
// @D5 new method.  This code used to be the put() method that did work. 
   /**
    * Starts the process of putting a file or appending data to a file on 
    * the server.  FTP opens the data connection to the server, then opens 
    * the file on the server and returns an output stream to the caller.  The 
    * caller then writes the file's data to the output stream.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param fileName The file to put.
    *   @param command "APPE" to append data, "STOR" to simply put 
    *   @return An output stream to the file.  The caller uses the output
    *           stream to write data to the file.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    OutputStream doAppendOrPut(String fileName, String command)
                                          throws IOException
    {
      if (Trace.isTraceOn())
        Trace.log(Trace.DIAGNOSTIC,"entering doAppendOrPut(file)");

      connect();

      Socket dataSocket = null;
      if (mode_ == PASSIVE_MODE)
      {
        dataSocket = getDataSocket();
      }
      else
      {
        initiateActiveMode();
      }

      String result = issueCommand(command + " " + fileName);

      if (result.startsWith("4") || result.startsWith("5"))
      {
        if (Trace.isTraceOn())
          Trace.log(Trace.DIAGNOSTIC,"put failed " + result);
        if (dataSocket != null) { // passive_mode
          dataSocket.close();
        }
        throw new IOException(result);
      }

      if (Trace.isTraceOn())
        Trace.log(Trace.DIAGNOSTIC,"leaving put(file)");

      if (dataSocket == null)
      {
        dataSocket = activeModeObject_.getSocket();
      }

      fireEvent(FTPEvent.FTP_PUT);

      return new FTPOutputStream(dataSocket, this);
    }







// ---------------------------------------------------------------------------
   //
   // Encode the password so it is not in the clear sitting in memory
   //

   private byte[] encode(byte[] b, byte[] mask, byte[] adder)
   {
        int i;

        byte[] buf = new byte[b.length];

        for (i=0; i<b.length; i++)
        {
            buf[i] = (byte)(b[i] + adder[i%adder.length]);
        }

        for (i=0; i<buf.length; i++)
        {
            buf[i] = (byte)(buf[i] ^ mask[i%mask.length]);
        }

        return buf;
   }



// ---------------------------------------------------------------------------
// @D2 new method

     void externallyConnected(String system,
                              Socket socket,
                              BufferedReader reader,
                              PrintWriter writer)
     {
        connectionState_     = ACTIVE;
        externallyConnected_ = true;
        server_              = system;
        controlSocket_       = socket;
        reader_              = reader;
        ps_                  = writer;
     }




// ---------------------------------------------------------------------------
     private int extractPortAddress(String s)
         throws IOException
     {
          int returnValue;

          if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"entering extractPortAddress() " + s);

          // @D3 replace implementation.  Spec says reply is
          // "227 Entering Passive Mode (h1,h2,h3,h4,p1,p2)." but VM doesn't
          // return the "()" and omits the "." at times.  Change to be
          // more flexible.

          // int start = s.indexOf('(');
          // int finish = s.indexOf(')');
          // if (finish <= start) throw new IOException("Bad port address, in " + s);
          // String r = s.substring(start + 1, finish);
          //
          // start = r.lastIndexOf(',');
          // String r1 = r.substring(start + 1);
          //
          // String r2 = r.substring(0, start);
          //
          // start = r2.lastIndexOf(',');
          // String r3 = r2.substring(start + 1);
          //
          // int val1 = Integer.parseInt(r1);
          // int val2 = Integer.parseInt(r3);
          // val2 = val2 * 256;
          // returnValue = val1 + val2;

          StringTokenizer tokens = new StringTokenizer(s, ",");

          if (tokens.countTokens() < 6)
             throw new IOException("Unable to extract port address from PASV response: " + s);

          //skip first four tokens that are not required, they are
          // the TCP address (we already know that)
          for (int i = 0; i < 4; i++)
          {
             tokens.nextToken();
          }

          try
          {
            int highOrderPort = Integer.parseInt(tokens.nextToken());
            String lowOrder = tokens.nextToken();

            //the reply for ftp server other than Mainframe server
            //ends with ")". But for mainfrmae server there is no
            //trailing ")". hence check for that
            int lowOrderPort;
            int index = lowOrder.indexOf(")");

            // if the string has the ")"
            if (index != -1)
            {
              lowOrderPort = Integer.parseInt(lowOrder.substring(0, index));
            }
            else
            {
              lowOrderPort = Integer.parseInt(lowOrder);
            }

            returnValue = (highOrderPort * 256) + lowOrderPort;
          }
          catch (NumberFormatException e) {
            throw new IOException("Unable to extract port address from PASV response: " + s);
          }

          if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"leaving extractPortAddress()");

          return returnValue;
     }




// ---------------------------------------------------------------------------
   /**
    * Closes all streams and sockets before this object
    * is garbage collected.
    *   @exception Throwable If an error occurs during cleanup.
   **/

    protected void finalize()
                   throws Throwable
    {
       super.finalize();
       disconnect();
    }




// ---------------------------------------------------------------------------
   //
   // Fires FTP Client events
   //

   void fireEvent(int event)
   {
      if (! listeners_.isEmpty())
      {
         Vector targets = (Vector) listeners_.clone();
         FTPEvent ftpEvent = new FTPEvent(this, event);

         for (int i = 0; i < targets.size(); i++)
         {
            FTPListener listener = (FTPListener) targets.elementAt(i);

            if (event == FTPEvent.FTP_CONNECTED)
            {
               listener.connected(ftpEvent);
            }
            else if (event == FTPEvent.FTP_DISCONNECTED)
            {
               listener.disconnected(ftpEvent);
            }

            else if (event == FTPEvent.FTP_RETRIEVED)
            {
               listener.retrieved(ftpEvent);
            }

            else if (event == FTPEvent.FTP_PUT)
            {
               listener.put(ftpEvent);
            }

            else if (event == FTPEvent.FTP_LISTED)
            {
               listener.listed(ftpEvent);
            }
         }
      }
   }






// ---------------------------------------------------------------------------
   /**
    * Starts the process of getting a file from the server.  FTP
    * opens the data connection to the server, then opens the file on
    * the server and returns an input stream to the caller.  The caller
    * reads the file's data from the input stream.
    * The source file is on the server, accessed via FTP so the path
    * separator character (if any) must be a forward slash.
    * <br>Throws SecurityException if userid or password is invalid.
    *
    *   @param fileName The file to get.
    *   @return An input stream to the file.  The caller uses the input
    *           stream to read the data from the file.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
    *   @exception FileNotFoundException If the name is a directory or the name is not found.
   **/

    public synchronized InputStream get(String fileName)
                       throws IOException, FileNotFoundException
    {
      if (Trace.isTraceOn())
        Trace.log(Trace.DIAGNOSTIC,"entering get(file)");

      if (fileName == null)
        throw new NullPointerException("file");

      if (fileName.length() == 0)
        throw new IllegalArgumentException("file");


      connect();

      Socket dataSocket = null;
      if (mode_ == PASSIVE_MODE)
      {
        dataSocket = getDataSocket();
      }
      else  // active_mode
      {
        initiateActiveMode();
      }

      issueCommand("RETR " + fileName);

      if (Trace.isTraceOn())
        Trace.log(Trace.DIAGNOSTIC,"leaving get(file)");

      // 150 appears to be success
      if (lastMessage_.startsWith("550"))
      {
        if (dataSocket != null) { // passive_mode
          dataSocket.close();
        }
        throw new FileNotFoundException();
      }

      if (dataSocket == null)
      {
        dataSocket = activeModeObject_.getSocket();
      }

      fireEvent(FTPEvent.FTP_RETRIEVED);
      return new FTPInputStream(dataSocket, this);
    }




// ---------------------------------------------------------------------------
   /**
    * Gets a file from the server.
    * The source file is on the server, accessed via FTP so the path
    * separator character (if any) must be a forward slash.
    * The target file is on the client, accessed via java.io
    * so the path separator character (if any) must be client specific.
    *   @param sourceFileName The file to get on the server.
    *   @param targetFileName The file on the target file system.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
    *   @exception FileNotFoundException If the source file or the targe file
    *              cannot be accessed.
   **/

    public synchronized boolean get(String sourceFileName, String targetFileName)
                   throws IOException, FileNotFoundException
    {
         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"entering get(String, String");


         if (sourceFileName == null)
            throw new NullPointerException("source");

         if (sourceFileName.length() == 0)
            throw new IllegalArgumentException("source");

         if (targetFileName == null)
            throw new NullPointerException("target");

         if (targetFileName.length() == 0)
            throw new IllegalArgumentException("target");

         boolean result = get(sourceFileName, new java.io.File(targetFileName));

         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"leaving get(String, String");

         return result;
    }





// ---------------------------------------------------------------------------
   /**
    * Gets a file from the server.
    * The source file is on the server, accessed via FTP so the path
    * separator character (if any) must be a forward slash.
    * The target file is an instance of Java.io.file.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param sourceFileName The file to get on the server.
    *   @param targetFile The file on the target file system.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
    *   @exception FileNotFoundException If the source file or the targe file
    *              cannot be accessed.
   **/

    public synchronized boolean get(String sourceFileName, java.io.File targetFile)
                   throws IOException, FileNotFoundException
    {
         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"entering get(String, file)");



         if (sourceFileName == null)
            throw new NullPointerException("source");

         if (sourceFileName.length() == 0)
            throw new IllegalArgumentException("source");

         if (targetFile == null)
            throw new NullPointerException("target");

         boolean result = true;

         connect();

         byte[] buffer = new byte[bufferSize_];

         InputStream in = get(sourceFileName);

         if (in != null)
         {
            FileOutputStream f = new FileOutputStream(targetFile);

            int length = in.read(buffer);
            while (length > 0)
            {
                f.write(buffer,0,length);
                length = in.read(buffer);
            }
            in.close();
            f.close();
            // readReply();
         }
         else
            result = false;

         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"leaving get(String, file");

         return result;
    }





// ---------------------------------------------------------------------------
   /**
    * Returns the size of the buffer used when transferring files.
    * When this class copies data between the source file and target file,
    * a buffer of this size is used.  The default buffer size is 4096 bytes.
    *   @return The buffer size used when transferring files.
   **/

    public int getBufferSize()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering getBufferSize(), (no leaving entry)");

        return bufferSize_;
    }




// ---------------------------------------------------------------------------




// ---------------------------------------------------------------------------
   /**
    * Returns the current directory on the server.
    *   @return The current directory on the server.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized String getCurrentDirectory()
                  throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering pwd(), (no leaving entry)");

        return pwd();
    }




// ---------------------------------------------------------------------------
   /**
    * Returns the text of the last reply returned from the server.
    * Empty string is returned if no request has been sent.
    *   @return The text of the last reply returned from the server.
   **/

    public synchronized String getLastMessage()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering lastMessage(), (no leaving entry)");

        return lastMessage_;
    }


    /**
     * Returns the current transfer mode.
     * @return The transfer mode. Valid values are ACTIVE_MODE or PASSIVE_MODE.
     * The default is PASSIVE_MODE.
    **/
    public int getMode()
    {
      return mode_;
    }



// ---------------------------------------------------------------------------
   //
   // Returns the password as a string.  This method should *NEVER*
   // be public!  If necessary, this method decrypts the password
   //

    private String getPassword()
    {
       if (encrypted_)
       {
          if (encryptedPassword_ == null)
             return null;
          else
             return new String(decode(encryptedPassword_, mask_, adder_));
       }
       else
          return clearPassword_;
    }




// ---------------------------------------------------------------------------
   /**
    * Returns the port used to connect to the system.
    *   @return The port used to connect to the system.
   **/

    public int getPort()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering getPort(), (no leaving entry)");

        return port_;
    }




// ---------------------------------------------------------------------------
   /**
    * Returns the name of the server.  Null is returned if no system has
    * been set.
    *   @return The name of the server to which this object connects.
   **/

    public String getServer()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering getServer(), (no leaving entry)");

        return server_;
    }



// ---------------------------------------------------------------------------
   /**
    * Returns the user.  Null is returned if no user has
    * been set.
    *   @return The name of the user.
   **/

    public String getUser()
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering getUser(), (no leaving entry)");

        return user_;
    }



// ---------------------------------------------------------------------------
    /**
     * Indicates whether the socket is reused for multiple file transfers, when in active mode.
     * @return true if the socket is reused; false if a new socket is created.
     * @see #setMode
    **/
    public boolean isReuseSocket()
    {
      return reuseSocket_;
    }





// ---------------------------------------------------------------------------
   /**
    * Sends a command to the server, returning the reply from the server.
    * <P>
    * The command is not altered before sending it to the server, so it
    * must be recognized by the server.  Many FTP applications change
    * commands so they are recognized by the server.  For example, the
    * command to get a list of files from the server is NLST, not ls.  Many
    * FTP applications convert ls to NLST before sending the command to
    * the server.  This method will not do the conversion.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param cmd The command to send to the server.
    *   @return The reply to the command.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized String issueCommand(String cmd)
                               throws IOException
    {
        if (Trace.isTraceOn())
        {
           String traceString = cmd;

           if (cmd.startsWith("PASS"))
           {
              traceString = "PASS " + "********************".substring(0, cmd.length() - 5);
           }

           Trace.log(Trace.DIAGNOSTIC,"entering issueCommand(), command is: " + traceString);
        }

        // make sure we are not in connect to prevent an infinite loop
        // (connect calls this method to issue the user and pass commands)
        if (! inConnect_)
          connect();

        ps_.println(cmd);
        readReply();

        //if (echo)
        //   lastMessage_ =  cmd + "\n" + reply;
        //else
        //   lastMessage_ =  reply;

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving  issueCommand(), message is: " + lastMessage_);

        return lastMessage_;
    }



  private void initiateActiveMode() throws IOException
  {
    if (!activeModeThread_.isAlive())
    {
      activeModeObject_.setLocalAddress(controlSocket_.getLocalAddress());
      activeModeThread_.start();
      activeModeObject_.waitUntilStarted();
    }
    activeModeObject_.issuePortCommand();
  }

// ---------------------------------------------------------------------------
   /**
    * Lists the contents of the current working directory.  If <i>details</i>
    * is true, additional information is returned.  If false, only the
    * file name is returned.  An array of length zero is returned if the
    * directory is empty.
    *   @param details True if file details will be returned.
    *   @return The contents of the directory as an array of Strings.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    String[] list(boolean details, String criteria)    // @D4c
        throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering list");

        connect();

      Socket dataSocket = null;
      if (mode_ == PASSIVE_MODE)
      {
        dataSocket = getDataSocket();
      }
      else  // active_mode
      {
        initiateActiveMode();
      }

        String FTPCommand;                                      // @D4a

        if (details)
        {
          FTPCommand = "LIST";                                  // @D4c
        }
        else
        {
          FTPCommand = "NLST";                                  // @D4c
        }

        if (criteria != null)                                   // @D4a
           FTPCommand = FTPCommand + " " + criteria;            // @D4a

        issueCommand(FTPCommand);                               // @D4c

        String[] result = new String[0];

        if (lastMessage_.startsWith("125") || lastMessage_.startsWith("150"))
        {

          if (dataSocket == null)
          {
            dataSocket = activeModeObject_.getSocket();
          }

           InputStream in = dataSocket.getInputStream();
           Vector v = new Vector();
           BufferedReader rdr = new BufferedReader(new InputStreamReader(in));

           String s;
           while ((s = rdr.readLine()) != null)
           {
            v.addElement(s);
           }

           result = new String[v.size()];
           v.copyInto(result);

           rdr.close();
           in.close();
           dataSocket.close();

           readReply();

           fireEvent(FTPEvent.FTP_LISTED);
        }

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving list()");

        return result;
    }



// ---------------------------------------------------------------------------
   /**
    * Logs on to the server
    *   @param user The user.
    *   @param password The password.
    *   @return String containing the reply from the server.
    *   @exception IOException If an error occurs while communicating with the server.
    */

    private String login(String user, String password)
        throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering login");

        // connect();
        // user_   = user;
        // password_ = password;

        String s1 = issueCommand("USER " + user);
        String s2 = issueCommand("PASS " + password);

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving login");

        return s1 + "\n" + s2;
    }




// ---------------------------------------------------------------------------
   /**
    * Lists the contents of the current working directory.  If the directory
    * is empty, an empty list is returned.
    *   @return The contents of the directory as an array of Strings.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized String[] ls()
                    throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering ls(), (no leaving entry)");

        return list(false, null);                                 // @D4c
    }




// ---------------------------------------------------------------------------
// @D4 new method
   /**
    * Lists the contents of the current working directory.  If the directory
    * is empty or no files match the search criteria, an empty list is returned.
    *   @return The contents of the directory as an array of Strings.
    *   @param criteria The search criteria.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized String[] ls(String criteria)
                    throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering ls(), (no leaving entry)");

        return list(false, criteria);
    }




// ---------------------------------------------------------------------------
   /**
    * Sends the NOOP (no operation) command to the server.  This
    * request is most useful to see if the connection to the server
    * is still active.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @return true if the request was successful, false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    // $$$ change this to package scoped after testing
    public synchronized boolean noop()
                   throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering noop");

        if (! inConnect_)
          connect();

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving noop()");


        return issueCommand("NOOP").startsWith("200");
    }




// ---------------------------------------------------------------------------
   /**
    * Starts the process of putting a file to the server.  FTP
    * opens the data connection to the server, then opens the file on
    * the server and returns an output stream to the caller.  The caller
    * then writes the file's data to the output stream.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param fileName The file to put.
    *   @return An output stream to the file.  The caller uses the output
    *           stream to write data to the file.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized OutputStream put(String fileName)
                                     throws IOException
    {
          if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"entering put(file)");


          if (fileName == null)
             throw new NullPointerException("file");

          if (fileName.length() == 0)
             throw new IllegalArgumentException("file");

          return doAppendOrPut(fileName, "STOR");
                                                 
          // @D5d the rest of this method is now in worker method doAppendOrPut()                                       
    }





// ---------------------------------------------------------------------------
   /**
    * Puts a file to the server.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the server.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized boolean put(String sourceFileName, String targetFileName)
                   throws IOException
    {
         if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC,"entering put(String, String)");


         if (sourceFileName == null)
            throw new NullPointerException("source");

         if (sourceFileName.length() == 0)
            throw new IllegalArgumentException("source");

         if (targetFileName == null)
            throw new NullPointerException("target");

         if (targetFileName.length() == 0)
            throw new IllegalArgumentException("target");

         boolean result = put(new java.io.File(sourceFileName), targetFileName);

         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"leaving put(String, String)");

         return result;
    }






// ---------------------------------------------------------------------------
   /**
    * Puts a file to the server.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the server.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized boolean put(java.io.File sourceFileName, String targetFileName)
                   throws IOException
    {
         if (Trace.isTraceOn())
            Trace.log(Trace.DIAGNOSTIC,"entering put(File, String)");


         if (sourceFileName == null)
            throw new NullPointerException("source");

         if (targetFileName == null)
            throw new NullPointerException("target");

         if (targetFileName.length() == 0)
            throw new IllegalArgumentException("target");


         connect();

         byte[] buffer = new byte[bufferSize_];

         FileInputStream f = new FileInputStream(sourceFileName);
         OutputStream out = put(targetFileName);

         int length = f.read(buffer);

         while (length > 0)
         {
             out.write(buffer,0,length);
             length = f.read(buffer);
         }
         out.close();
         f.close();

         if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"leaving put(String, String)");

         return true;
    }




// ---------------------------------------------------------------------------
   /**
    * Returns the current directory on the server.  PWD is the ftp
    * command Print Working Directory.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @return The current directory on the server.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized String pwd()
                               throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering pwd(), (no leaving entry)");

        connect();
        return issueCommand("PWD");
    }





// ---------------------------------------------------------------------------
   /**
    * Removes this listener from being notified when a bound property changes.
    *
    *    @param listener The PropertyChangeListener.
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null)
      {
         throw new NullPointerException("listener");
      }

      changes_.removePropertyChangeListener(listener);
   }



// ---------------------------------------------------------------------------
   /**
    * Removes a listener from the list.
    *
    *    @param listener The FTP listener.
   **/
   public void removeFTPListener(FTPListener listener)
   {
      if (listener == null)
      {
         throw new NullPointerException("listener");
      }

      listeners_.removeElement(listener);
   }



// ---------------------------------------------------------------------------
   /**
    * Removes this listener from being notified when a constrained property changes.
    *
    *    @param listener The VetoableChangeListener.
   **/
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null)
      {
         throw new NullPointerException("listener");
      }

      vetos_.removeVetoableChangeListener(listener);
   }






// ---------------------------------------------------------------------------
   /**
    * During object deserialization, this method is called.  When it is call
    * we need to initialize transient data.
   **/

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        connectionState_     = PARKED;
        inConnect_           = false;
        lastMessage_         = "";
        changes_             = new PropertyChangeSupport(this);
        vetos_               = new VetoableChangeSupport(this);
        listeners_           = new Vector();
        externallyConnected_ = false;                              // @D2a
    }








// ---------------------------------------------------------------------------
   /**
    * Receives a reply from the server.  This method is called <B>only</B>
    * after calling the get() or put() methods where the calling program
    * copies the data.
    *   @return The reply from the server.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    //
    // Protocol is a reply is a three digit number followed by a dash or
    // space, followed by text.  A request can result in multiple replies.
    // The three digit number of the last reply must match number of the
    // first replay -- AND -- the character following the number must
    // be a space.  For example, a request could result in the following:
    // 123-First line
    //     Second line
    // 123-Third line
    // 456-Fourth line
    // 123 Last line
    //

    String readReply()
        throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering readReply()");

        String currentLine = reader_.readLine();

        if ((currentLine == null) || (currentLine.length() == 0))
           throw new IOException();

        String code = currentLine.substring(0, 3);
        StringBuffer buf = new StringBuffer(currentLine);

        boolean Continue = true;

        while (Continue)
        {
          if ((currentLine.length() > 3)                 &&
              (currentLine.substring(0, 3).equals(code)) &&
              (currentLine.charAt(3) == ' '))
          { Continue = false; }
          else
          {
             currentLine = reader_.readLine();
             buf.append("\n" + currentLine);
          }
        }

        lastMessage_ = buf.toString();

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving readReply()");

        return lastMessage_;
    }





// ---------------------------------------------------------------------------
   /**
    * Sets the buffer size used when transferring files.  The default
    * buffer size is 4096 bytes.
    *   @param bufferSize The size of the buffer used when transferring files.
    *   @exception PropertyVetoException If the change is vetoed.
   **/

    public synchronized void setBufferSize(int bufferSize)
                             throws PropertyVetoException
    {
       if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering setBufferSize()");

       if (bufferSize < 1)
          throw new IllegalArgumentException("bufferSize");

       // Remember the old value.
       int oldValue = bufferSize_;

       // Fire a vetoable change event.
       vetos_.fireVetoableChange("bufferSize", new Integer(oldValue), new Integer(bufferSize));

       bufferSize_ = bufferSize;

       // Fire the property change event.
       changes_.firePropertyChange("bufferSize", new Integer(oldValue), new Integer(bufferSize));
    }







// ---------------------------------------------------------------------------
   /**
    * Sets the current directory on the server to <i>directory</i>.
    * The method is the same as cd().
    * The message returned from the server is saved.  Use getLastMessage()
    * to retrieve it.
    *   @param directory The current directory to set on the server.
    *   @return true if directory changed; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized boolean setCurrentDirectory(String directory)
                                throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering setCurrentDirectory(), (no leaving entry)");

        return cd(directory);
    }






// ---------------------------------------------------------------------------
   /**
    * Sets the data transfer type.  Valid values are:
    * <UL>
    * <LI>{@link #ASCII ASCII}
    * <LI>{@link #BINARY BINARY}
    * </UL>
    * <P>
    * If a connection does not
    * already exist, a connection is made to the server.
    * The message returned from the server is saved.  Use getLastMessage()
    * to retrieve it.
    * <br>Throws SecurityException if userid or password is invalid.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public synchronized void setDataTransferType(int transferType)
                throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering setDataTransferType()");

        if ((transferType == ASCII) ||
            (transferType == BINARY))
        {
           connect();
           if (transferType == ASCII)
              issueCommand("TYPE A");
           else
              issueCommand("TYPE I");
        }
        else
           throw new IllegalArgumentException("transferType");


        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving setDataTransferType()");
    }



    /**
     * Sets the transfer mode.
     * @param mode The mode. Valid values are {@link #ACTIVE_MODE ACTIVE_MODE} or {@link #PASSIVE_MODE PASSIVE_MODE}.
    **/
    public void setMode(int mode)
    {
      if (mode != ACTIVE_MODE && mode != PASSIVE_MODE)
      {
        throw new IllegalArgumentException("mode");
      }
      if (mode == ACTIVE_MODE && activeModeThread_ == null)
      {
        activeModeObject_ = new FTPThread(this);
        activeModeThread_ = new Thread(activeModeObject_);
        activeModeThread_.setDaemon(true);
        // Don't start the thread yet, because we need to set our local InetAddress into it first,
        // and we don't know what that is until we have connect()ed.
      }
      mode_ = mode;
    }

// ---------------------------------------------------------------------------
   /**
    * Sets the password.  The password cannot be changed once
    * a connection is made to the server.
    *   @param password The password for the user.
   **/

    public synchronized void setPassword(String password)
    {
       if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering setPassword(), (no leaving entry)");

       if (password == null)
          throw new NullPointerException("password");

       if (password.length() == 0)
          throw new IllegalArgumentException("password");

       try
       {
          Date   d = new Date();
          Random r = new Random(d.getTime());

          mask_ = new byte[7];
          r.nextBytes(mask_);

          adder_ = new byte[9];
          r.nextBytes(adder_);

          encryptedPassword_ = encode(password.getBytes(), mask_, adder_);
          encrypted_ = true;
       }
       catch (Exception e)
       {
          encrypted_ = false;
          clearPassword_ = password;
       }
    }




// ---------------------------------------------------------------------------
   /**
    * Sets the port to use when connecting to the server.
    * The port cannot be changed once
    * a connection is made to the server.
    *   @param port The port to use when connecting to the server.
    *   @exception PropertyVetoException If the change is vetoed.
   **/

    public synchronized void setPort(int port)
                             throws PropertyVetoException
    {
       if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering setPort()");

       if (port < 1)
          throw new IllegalArgumentException("port");

       if (connectionState_ != PARKED)
          throw new IllegalStateException("connected");

       // Remember the old value.
       int oldValue = port_;

       // Fire a vetoable change event.
       vetos_.fireVetoableChange("port", new Integer(oldValue), new Integer(port));

       port_ = port;

       // Fire the property change event.
       changes_.firePropertyChange("port", new Integer(oldValue), new Integer(port));
    }



    /**
     * Indicates whether to reuse a socket for multiple file transfers, when in active mode.
     * By default, the "reuse socket" attribute is set to the value of the <tt>com.ibm.as400.access.FTP.reuseSocket</tt> property.
     * If the property is not set, the default is <tt>true</tt> (sockets are reused).
     * The "reuse socket" attribute (of an FTP object) cannot be reset after that object has connected to the server.
     * @param reuse If true, the socket is reused.  If false, a new socket is created for each subsequent file transfer.
     * @see #setMode
    **/
    public void setReuseSocket(boolean reuse)
    {
      if (connectionState_ != PARKED)
        throw new IllegalStateException("connected");

      reuseSocket_ = reuse;
    }






// ---------------------------------------------------------------------------
   /**
    * Sets the name of the server.  The system name cannot be changed once
    * a connection is made to the server.
    *   @param server The name of the server to which this object connects.
    *   @exception PropertyVetoException If the change is vetoed.
   **/

    public synchronized void setServer(String server)
                             throws PropertyVetoException
    {
       if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering setServer(), (no leaving entry)");

       if (server == null)
          throw new NullPointerException("server");

       if (server.length() == 0)
          throw new IllegalArgumentException("server");

       if (connectionState_ != PARKED)
          throw new IllegalStateException("connected");

       // Remember the old system.
       String oldServer = server_;

       // Fire a vetoable change event for system.
       vetos_.fireVetoableChange("server", oldServer, server);

       server_ = server;

       // Fire the property change event.
       changes_.firePropertyChange("server", oldServer, server);
    }





// ---------------------------------------------------------------------------
   /**
    * Sets the user identifier used when connecting to the server.
    * <B>If the client is connected to the server, this method
    * will disconnect the connection</B>
    *   @param user The user identifier used when connecting to the server.
    *   @exception PropertyVetoException If the change is vetoed.
    *   @exception IllegalStateException If connection already established to the server.
   **/

    public synchronized void setUser(String user)
                             throws PropertyVetoException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering getUser(), (no leaving entry)");

       if (user == null)
          throw new NullPointerException("user");

       if (user.length() == 0)
          throw new IllegalArgumentException("user");

       if (connectionState_ != PARKED)
          throw new IllegalStateException("connected");

       // Remember the old value.
       String oldUser = user_;

       // Fire a vetoable change event.
       vetos_.fireVetoableChange("user", oldUser, user);

       try
       {
          disconnect();
       }
       catch (Exception e) {}

       user_ = user;

       // Fire the property change event.
       changes_.firePropertyChange("user", oldUser, user);

    }

    /**
     * Renames one or more files on the server, according to a specified pattern.
     * <p>
     * For example:
     * <ul>
     * <li>ren("*.txt","*.DONE") renames "file1.txt" to "file1.DONE", and so on
     * <li>ren("*.txt","*_DONE.*) renames "file1.txt" to "file1_DONE.txt", and so on
     * <li>ren("*.txt","*_1055am") renames "file1.txt" to "file1.txt_1055am", and so on
     * </ul>
     *  
     * @param fromName A pattern specifying the file(s) to be renamed.
     * The syntax of the pattern is similar to the syntax for {@link #ls ls()}.
     * @param toName The new file name, or a simple pattern
     * describing how to construct the new name out of fromName.
     * <tt>toName</tt> can contain up to two asterisks, one on each side of the ".".
     * @return The number of files renamed. 
     * @throws IOException If an error occurs while communicating with the server.
     **/

    public synchronized int ren(String fromName, String toName)
      throws IOException
    {
      int renamedCount = 0;

      if (Trace.isTraceOn())
        Trace.log(
                  Trace.DIAGNOSTIC,
                  "entering ren(), from file name is "
                  + fromName
                  + ", to file name is "
                  + toName
                  + ".");

      if (fromName == null)
        throw new NullPointerException("fromName");
      if (toName == null)
        throw new NullPointerException("toName");

      if (fromName.trim().length() == 0)
        throw new IllegalArgumentException("fromName");
      if (toName.trim().length() == 0)
        throw new IllegalArgumentException("toName");

      String[] entries = ls(fromName);

      for (int i=0; i<entries.length; i++) {	

        issueCommand("RNFR " + entries[i]);

        // If server returns status 35x, then it has accepted the file name
        // ok and is waiting for the RNTO subcomand.
        if (lastMessage_.startsWith("35")) {
          issueCommand("RNTO " + generateNewName(entries[i], toName));
          if (lastMessage_.startsWith("25")) {
            renamedCount += 1;
          }
        }

      }

      if (Trace.isTraceOn())
        Trace.log(
                  Trace.DIAGNOSTIC,
                  "leaving ren(). renamedCount = " + renamedCount);

      return renamedCount;
    }

    /**
     * Returns a new file name constructed out of the old
     * file name and a simple expression containing asterisks.
     * <p>
     * For example:
     * <ul>
     * <li>generateNewName("file.txt","*.DONE") returns "file.DONE"
     * <li>generateNewName("file.txt","*_DONE.*) returns "file_DONE.txt"
     * <li>generateNewName("file.txt","*_1055am") returns "file.txt_1055am"
     * </ul>
     *  
     * @param fromName The original file name
     * @param toName The new file name, or a simple pattern
     * describing how to construct the new name out of fromName.
     * <tt>toName</tt> can contain up to two asterisks, one on each side of the ".".
     * @return The new file name.
     * @throws NullPointerException if the fromName or toName
     * parameters are null.
     * @throws IllegalArgumentException if fromName or toName
     * is null, or if the toName contains an invalid expression.
     **/
    public static String generateNewName(String fromName, String toName)
    {
      if (fromName == null)
        throw new NullPointerException("fromName");
      if (toName == null)
        throw new NullPointerException("toName");

      fromName = fromName.trim();
      if (fromName.length() < 1)
        throw new IllegalArgumentException("fromName");
      toName = toName.trim();
      if (toName.length() < 1)
        throw new IllegalArgumentException("toName");

      if (toName.indexOf('*') < 0)
        return toName;

      if (toName.indexOf('.') < 0)
        return newNamePart(fromName, toName);

      String[] fromParts = splitName(fromName, '.');
      String[] toParts = splitName(toName, '.');

      String theFront = newNamePart(fromParts[0], toParts[0]);
      String theBack = newNamePart(fromParts[1], toParts[1]);

      String theNewName = theFront;
      if (theBack.length() > 0)
        theNewName = theNewName + "." + theBack;

      if (DEBUG) System.out.println("generateNewName(): " + fromName +", " + toName + ", -> " + theNewName);
      return theNewName;
    }

    /**
     * Returns a new name constructed out of an old name
     * using a very simple pattern containing at most one
     * asterisk. The asterisk is replaced with the value
     * of the old name.
     * <p>
     * For example:
     * <ul>
     * <li>newNamePart("abc","xyz") returns "xyz"
     * <li>newNamePart("abc","*_DONE") returns "abc_DONE"</li>
     * <li>newNamePart("abc","DONE_*") returns "DONE_abc"</li>
     * <li>newNamePart("abc","DONE_*_DONE") returns "DONE_abc_DONE"
     * </ul>
     * @param fromName the old name.
     * @param toName the new name, containing no more than 1 asterisk.
     * @return the constructed name.
     **/
    static String newNamePart(String fromName, String toName)
    {
      int indexA = toName.indexOf('*');

      // Return immediately if there is no asterisk in the to name.
      if (indexA < 0)
        return toName;

      // Cannot be more than 1 asterisk in the to name.
      if ((indexA < (toName.length() - 1))
          && (toName.indexOf('*', indexA + 1) > 0))
        throw new IllegalArgumentException("toName");

      StringBuffer nameBuff = new StringBuffer();

      // LHS of asterisk
      if (indexA > 0)
        nameBuff.append(toName.substring(0, indexA));

      // Asterisk is replaced with original name
      nameBuff.append(fromName);

      // RHS of asterisk
      if (indexA < (toName.length() - 1))
        nameBuff.append(toName.substring(indexA + 1));

      return nameBuff.toString();
    }

    /**
     * Utility method used to split a string into exactly
     * two parts at the <em>last occurrence</em> of a
     * given character. If the character is not found then
     * the original string is returned as the first part.
     * 
     * @param stringValue the striung value to split.
     * @param c the character at which to split the string.
     * @return String array with exactly 2 elements. 
     */
    static String[] splitName(String stringValue, char c) {

      String[] pieces = new String[2];

      int splitIndex = stringValue.lastIndexOf(c);

      if (splitIndex >= 0) {
        pieces[0] = stringValue.substring(0, splitIndex);
        if (splitIndex < (stringValue.length() - 1)) {
          pieces[1] = stringValue.substring(splitIndex + 1);
        } else {
          pieces[1] = "";
        }
      } else {
        pieces[0] = stringValue;
        pieces[1] = "";
      }

      return pieces;
    }


    /**
     * Utility method used to establish "passive" mode.
     * 
     * @return The socket. 
     */
    final Socket getDataSocket()
      throws IOException
    {
      String response = issueCommand("PASV");
      int p = extractPortAddress(response);
      return new Socket(server_, p);
    }


    // Checks the "FTP.reuseSocket" system property.  If it's set, initializes reuseSocket_ accordingly.
    private void checkSocketProperty()
    {
      String property = SystemProperties.getProperty(SystemProperties.FTP_REUSE_SOCKET);
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "FTP.reuseSocket system property: " +  property);
      if (property != null) {
        reuseSocket_ = (property.equalsIgnoreCase("true") ? true : false);
      }
    }

}
