///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

import netscape.security.*;                                          // @D1a
import com.ms.security.*;                                            // @D1a

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

public class FTP
             implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


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
               // Do not just grab "cleanPassword"!!!!  Always use getPassword()
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

    transient private String  lastMessage_ = "";

               // default port
    private int port_ = 21;

               // socket for sending commands / receiving replies
    transient private Socket controlSocket_;

               // replies arrive via this reader
    transient private BufferedReader reader_;

               // requests are sent via this writer.
    transient private PrintWriter ps_;

    transient private boolean externallyConnected_ = false;    // @D2a

               // Lists of listeners
    transient         PropertyChangeSupport changes_   = new PropertyChangeSupport(this);
    transient         VetoableChangeSupport vetos_     = new VetoableChangeSupport(this);
    transient         Vector                listeners_ = new Vector();


               // amount of data to transfer at one time
    private int bufferSize_ = 4096;


    /**
     * Transfer files in ASCII mode.
    **/
    public static final int ASCII  = 0;

    /**
     * Transfer files in binary mode.
    **/
    public static final int BINARY = 1;


// -----------------------------------------------------------------------
   /**
    * Constructs an FTP object.
    * The server name, user and password must be set before
    * requests are sent to the server.
    **/

    public FTP()
    {
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
    *   @param directory The current directory to set on the server.
    *   @return true if directory changed; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public boolean cd(String directory)
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



        // If running inside a browser we must enable the proper
        // connect privileges so that signed applets using our classes can
        // connect to servers other than the web server that delivered
        // the applet.
        //
        // You probably noticed this code is in four places.  The privilege
        // is lost when the code that requests it 'pops off the stack'
        // so we cannot put common code in a subroutine.
        //
        // Start of change for @D1

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading browser security classes");

        Class privilegeManagerClass = null;
        Class permissionIDClass     = null;
        Class policyEngineClass     = null;

        try
        {
           privilegeManagerClass = Class.forName("netscape.security.PrivilegeManager");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes not available");
        }

        try
        {
           permissionIDClass = Class.forName("com.ms.security.PermissionID");
           policyEngineClass = Class.forName("com.ms.security.PolicyEngine");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes not available");
        }

        // If available, invoke the Navigator enablePrivilege method.
        if (privilegeManagerClass != null)
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling Netscape privilige");
              PrivilegeManager.enablePrivilege("UniversalConnect");
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling Netscape privilige");
           }
           catch (Throwable e)
           {
             if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling Netscape privilege", e);
           }
        }

        // If available, invoke the IE assertPermission method.
        if ((permissionIDClass != null) && (policyEngineClass != null))
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling IE privilige");
              PolicyEngine.assertPermission(PermissionID.NETIO);
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling IE privilige");
           }
           catch (Throwable e)
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling IE privilege", e);
           }
        }

        // End of change for @D1


        controlSocket_ = new Socket(server_, port_);
//      controlSocket_.setTcpNoDelay(true);
//      controlSocket_.setSoLinger(true, 60);

        reader_ = new BufferedReader(new InputStreamReader(controlSocket_.getInputStream()));
        ps_ = new PrintWriter(controlSocket_.getOutputStream(), true);

        String s = readReply();

        login(user_, password_);
        //String s1 = issueCommand("USER " + user_);
        //String s2 = issueCommand("PASS " + password_);

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

    public String[] dir()
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

    public String[] dir(String criteria)
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
              lastMessage_ = issueCommand("QUIT");
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
             throw new IOException("Bad port address, in " + s);

          //skip first four tokens that are not required, they are
          // the TCP address (we already know that)
          for (int i = 0; i < 4; i++)
          {
             tokens.nextToken();
          }

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
    *
    *   @param fileName The file to get.
    *   @return An input stream to the file.  The caller uses the input
    *           stream to read the data from the file.
    *           Null is returned if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
    *   @exception FileNotFoundException If the name is a directory or the name is not found.
   **/

    public InputStream get(String fileName)
                       throws IOException, FileNotFoundException
    {
          if (Trace.isTraceOn())
             Trace.log(Trace.DIAGNOSTIC,"entering get(file)");

          if (fileName == null)
             throw new NullPointerException("file");

          if (fileName.length() == 0)
             throw new IllegalArgumentException("file");


          connect();

          String response = issueCommand("PASV");
          int p = extractPortAddress(response);





        // If running inside a browser we must enable the proper
        // connect privileges so that signed applets using our classes can
        // connect to servers other than the web server that delivered
        // the applet.
        //
        // You probably noticed this code is in four places.  The privilege
        // is lost when the code that requests it 'pops off the stack'
        // so we cannot put common code in a subroutine.
        //
        // Start of change for @D1

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading browser security classes");

        Class privilegeManagerClass = null;
        Class permissionIDClass     = null;
        Class policyEngineClass     = null;

        try
        {
           privilegeManagerClass = Class.forName("netscape.security.PrivilegeManager");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes not available");
        }

        try
        {
           permissionIDClass = Class.forName("com.ms.security.PermissionID");
           policyEngineClass = Class.forName("com.ms.security.PolicyEngine");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes not available");
        }

        // If available, invoke the Navigator enablePrivilege method.
        if (privilegeManagerClass != null)
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling Netscape privilige");
              PrivilegeManager.enablePrivilege("UniversalConnect");
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling Netscape privilige");
           }
           catch (Throwable e)
           {
             if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling Netscape privilege", e);
           }
        }

        // If available, invoke the IE assertPermission method.
        if ((permissionIDClass != null) && (policyEngineClass != null))
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling IE privilige");
              PolicyEngine.assertPermission(PermissionID.NETIO);
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling IE privilige");
           }
           catch (Throwable e)
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling IE privilege", e);
           }
        }

        // End of change for @D1









          Socket dataSocket = new Socket(server_, p);
 //       dataSocket.setTcpNoDelay(true);
 //       dataSocket.setSoLinger(true, 60);

          issueCommand("RETR " + fileName);

          if (Trace.isTraceOn())
              Trace.log(Trace.DIAGNOSTIC,"leaving get(file)");

          // 150 appears to be success
          if (lastMessage_.startsWith("550"))
          {
             dataSocket.close();
             throw new FileNotFoundException();
          }
          else
          {
             fireEvent(FTPEvent.FTP_RETRIEVED);
             return new FTPInputStream(dataSocket, this);
          }

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

    public boolean get(String sourceFileName, String targetFileName)
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
    *   @param sourceFileName The file to get on the server.
    *   @param targetFileName The file on the target file system.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
    *   @exception FileNotFoundException If the source file or the targe file
    *              cannot be accessed.
   **/

    public boolean get(String sourceFileName, java.io.File targetFile)
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

    public String getCurrentDirectory()
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
    * Sends a command to the server, returning the reply from the server.
    * <P>
    * The command is not altered before sending it to the server so it
    * much be recognized by the server.  Many FTP applications change
    * commands so they are recognized by the server.  For example, the
    * command to get a list of files from the server is NLST, not ls.  Many
    * FTP applications convert ls to NLST before sending the command to
    * the server.  This method will not do the conversion.
    *   @param command The command to send to the server.
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
        String reply = readReply();

        //if (echo)
        //   lastMessage_ =  cmd + "\n" + reply;
        //else
           lastMessage_ =  reply;

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving  issueCommand(), message is: " + lastMessage_);

        return lastMessage_;
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

    synchronized String[] list(boolean details, String criteria)    // @D4c
        throws IOException
    {
        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"entering list");

        connect();

        String response = issueCommand("PASV");
        int p = extractPortAddress(response);




        // If running inside a browser we must enable the proper
        // connect privileges so that signed applets using our classes can
        // connect to servers other than the web server that delivered
        // the applet.
        //
        // You probably noticed this code is in four places.  The privilege
        // is lost when the code that requests it 'pops off the stack'
        // so we cannot put common code in a subroutine.
        //
        // Start of change for @D1

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading browser security classes");

        Class privilegeManagerClass = null;
        Class permissionIDClass     = null;
        Class policyEngineClass     = null;

        try
        {
           privilegeManagerClass = Class.forName("netscape.security.PrivilegeManager");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes not available");
        }

        try
        {
           permissionIDClass = Class.forName("com.ms.security.PermissionID");
           policyEngineClass = Class.forName("com.ms.security.PolicyEngine");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes not available");
        }

        // If available, invoke the Navigator enablePrivilege method.
        if (privilegeManagerClass != null)
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling Netscape privilige");
              PrivilegeManager.enablePrivilege("UniversalConnect");
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling Netscape privilige");
           }
           catch (Throwable e)
           {
             if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling Netscape privilege", e);
           }
        }

        // If available, invoke the IE assertPermission method.
        if ((permissionIDClass != null) && (policyEngineClass != null))
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling IE privilige");
              PolicyEngine.assertPermission(PermissionID.NETIO);
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling IE privilige");
           }
           catch (Throwable e)
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling IE privilege", e);
           }
        }

        // End of change for @D1






        Socket dataSocket = new Socket(server_, p);
     // dataSocket.setTcpNoDelay(true);
     // dataSocket.setSoLinger(true, 60);

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

           lastMessage_ = readReply();

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

    public String[] ls()
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

    public String[] ls(String criteria)
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
    * is still active
    *   @return true if the request was successful, false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    // $$$ change this to package scoped after testing
    public boolean noop()
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

          connect();

          String response = issueCommand("PASV");
          int p = extractPortAddress(response);




        // If running inside a browser we must enable the proper
        // connect privileges so that signed applets using our classes can
        // connect to servers other than the web server that delivered
        // the applet.
        //
        // You probably noticed this code is in four places.  The privilege
        // is lost when the code that requests it 'pops off the stack'
        // so we cannot put common code in a subroutine.
        //
        // Start of change for @D1

        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Loading browser security classes");

        Class privilegeManagerClass = null;
        Class permissionIDClass     = null;
        Class policyEngineClass     = null;

        try
        {
           privilegeManagerClass = Class.forName("netscape.security.PrivilegeManager");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Netscape classes not available");
        }

        try
        {
           permissionIDClass = Class.forName("com.ms.security.PermissionID");
           policyEngineClass = Class.forName("com.ms.security.PolicyEngine");
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes available");
        }
        catch (Throwable e)
        {
           if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "IE classes not available");
        }

        // If available, invoke the Navigator enablePrivilege method.
        if (privilegeManagerClass != null)
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling Netscape privilige");
              PrivilegeManager.enablePrivilege("UniversalConnect");
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling Netscape privilige");
           }
           catch (Throwable e)
           {
             if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling Netscape privilege", e);
           }
        }

        // If available, invoke the IE assertPermission method.
        if ((permissionIDClass != null) && (policyEngineClass != null))
        {
           try
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Enabling IE privilige");
              PolicyEngine.assertPermission(PermissionID.NETIO);
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Done enabling IE privilige");
           }
           catch (Throwable e)
           {
              if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Error enabling IE privilege", e);
           }
        }

        // End of change for @D1



          Socket dataSocket = new Socket(server_, p);

       // dataSocket.setTcpNoDelay(false);
       // dataSocket.setSoLinger(true, 60);

          String result = issueCommand("STOR " + fileName);

          if (result.startsWith("4") || result.startsWith("5"))
          {
             if (Trace.isTraceOn())
                 Trace.log(Trace.DIAGNOSTIC,"put failed " + result);

             throw new IOException(result);
          }

          if (Trace.isTraceOn())
              Trace.log(Trace.DIAGNOSTIC,"leaving put(file)");

          fireEvent(FTPEvent.FTP_PUT);

          return new FTPOutputStream(dataSocket, this);
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
      String x = Copyright.copyright;

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

        String r = reader_.readLine();

        if ((r == null) || (r.length() == 0))
           throw new IOException();

        String code = r.substring(0, 3);
        String currentLine = r;

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
             r += "\n" + currentLine;
          }
        }

        if (Trace.isTraceOn())
           Trace.log(Trace.DIAGNOSTIC,"leaving readReply()");

        return r;
    }





// ---------------------------------------------------------------------------
   /**
    * Sets the buffer size used when transferring files.  The default
    * buffer size is 4096 bytes.
    *   @param size The size of the buffer used when transferring files.
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
    * <LI>ASCII
    * <LI>BINARY
    * </UL>
    * <P>
    * If a connection does not
    * already exist, a connection is made to the server.
    * The message returned from the server is saved.  Use getLastMessage()
    * to retrieve it.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public void setDataTransferType(int transferType)
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







// ---------------------------------------------------------------------------
   /**
    * Sets the password.  The password cannot be changed once
    * a connection is made to the server.
    *   @param The password for the user.
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
    *   @param Port The port to use when connecting to the server.
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






// ---------------------------------------------------------------------------
   /**
    * Sets the name of the server.  The system name cannot be changed once
    * a connection is made to the server.
    *   @param The name of the server to which this object connects.
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

}
