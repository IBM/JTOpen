///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FTP.java
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
 * The AS400FTP class represents a client for the AS/400 FTP server.
 * It is written to take advantage of AS/400 server features.
 * Use the parent class, FTP, if you need a generic
 * client.
 *
 * <P>AS400FTP does the following extra processing:
 * <OL>
 * <LI>Using the AS400 object to determine system name,
 * userid and password so these properties can be shared with other
 * Toolbox classes.  If you use the FTP class, your application must set
 * the system name, userid, and password in both the FTP class and other
 * Toolbox classes.
 * <P>
 * One restriction when the client is an AS/400 --
 * the userid and password must be
 * set on the AS400 object in this environment.  That is, *CURRENT
 * will not work when connecting from one AS/400 to another AS/400.
 * <P><LI>
 * Automatically setting the current directory to the
 * root of the AS/400 when a connection is made. If the
 * FTP class is used, the root may be QSYS.LIB when
 * a connection is made.  The integrated file system name
 * of objects in libraries must be used to access them.
 * For example, /QSYS.LIB/MYLIB.LIB/MYFILE.FILE.  See
 * <A HREF="QSYSObjectPathName.html">
 * QSYSObjectPathName</A> documentation for more information.
 * <P><LI>
 * Handling extra work necessary to put a save file
 * to the AS/400.  If you use the FTP class, you have to
 * do the extra steps.  The extra processing is done only
 * if the extension of the file is .savf and the file will be
 * put into an AS/400 library.
 * <P>
 * The extra processing includes internally using Toolbox CommandCall
 * to create the save file on the AS/400.  The default *Public authority
 * is *EXCLUDE.  Use
 * the setSaveFilePublicAuthority() method to change the *Public
 * authority value specified on the create save file command.
 * </OL>
 * <P>
 * No encryption is provided by this class.  The userid and password
 * flow un-encrypted to the server.  This class is not SSL enabled.
 * <P>
 * The forward slash is the separator character for paths sent
 * to the FTP server.
 * <P>
 * The following example puts a save file to the AS/400.
 * Note the application does not set data transfer type to
 * binary or use Toolbox CommandCall to create the save file.
 * Since the extension is .savf,
 * AS400FTP class detects the file to put is a save file
 * so it does these step automatically.
 * <ul>
 * <pre>
 * AS400 system = new AS400();
 * AS400FTP   ftp    = new AS400FTP(system);
 * ftp.put("myData.savf", "/QSYS.LIB/MYLIB.LIB/MYDATA.SAVF");
 * </pre>
 * </ul>
**/

public class AS400FTP
             extends FTP
             implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;


    // **********************************************************
    // *                                                        *
    // * This class has two main tasks:                         *
    // *   1) Use the Toolbox AS400 object to identify          *
    // *      the server, userid and password, the use the      *
    // *      AS400 object to connect to the FTP server.        *
    // *   2) Special save file process above.  Detour the      *
    // *      put routine to see if we first need to create the *
    // *      save file.                                        *
    // *                                                        *
    // * Other than these two tasks it just punts to the super  *
    // * class to do all the work.                              *
    // *                                                        *
    // **********************************************************






    // **********************************************************
    // *                                                        *
    // * Don't forget to update readObject() if transient       *
    // * variables are added!                                   *
    // *                                                        *
    // **********************************************************

              private AS400 system_;
    transient private boolean connected_ = false;
    transient private boolean inConnect_ = false;
    transient private boolean inSaveFileProcessing_ = false;

    private String saveFilePublicAuthority_ = "*EXCLUDE";
    private String CDToRoot = "/";

    private static ResourceBundleLoader loader_;

// -----------------------------------------------------------------------
   /**
    * Constructs an AS400FTP object.
    * The AS400 object must be set before
    * requests are sent to the server.
    * @see #setSystem
    **/

    public AS400FTP()
    {
    }





// -----------------------------------------------------------------------
   /**
    * Constructs an AS400FTP object.
    *   @param server The AS/400 to which to connect.
   **/

    public AS400FTP(AS400 system)
    {
       if (system == null)
          throw new NullPointerException("system");

       system_ = system;
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
       if (connect())
          return super.cd(directory);
       else
          return false;
    }




// ---------------------------------------------------------------------------
   /**
    * Connects to the server.
    * The AS400 object must be set
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
    *   @see #setSystem
   **/

    public synchronized boolean connect()
         throws UnknownHostException,
                IOException,
                IllegalStateException
    {
       if (! connected_)
       {

          if (system_ == null)
          {
             throw new ExtendedIllegalStateException("system",
                       ExtendedIllegalStateException.PROPERTY_NOT_SET);
          }

          // Force a sign-on.  Since the super class does not throw
          // AS400 exceptions I have to catch it and throw an
          // IOException.
          try
          {
             system_.getVRM();
          }
          catch (AS400SecurityException e)
          {
             if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC,"Security exception in getVRM()", e);

             throw new IOException(e.getMessage());
          }

          AS400ImplRemote systemImpl = (AS400ImplRemote)system_.getImpl();                               // @D2a
          Socket socket = systemImpl.getConnection(super.getPort());                                     // @D2a
          BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));    // @D2a
          PrintWriter  writer = new PrintWriter(socket.getOutputStream(), true);                         // @D2a
                                                                                                         // @D2a
          super.externallyConnected(system_.getSystemName(),                                             // @D2a
                                    socket,                                                              // @D2a
                                    reader,                                                              // @D2a
                                    writer);                                                             // @D2a
          connected_ = true;                                                                             // @D2a

          inConnect_ = true;

          try
          {
             if (! cd(CDToRoot))
             {
                if (Trace.isTraceOn())
                    Trace.log(Trace.DIAGNOSTIC,"CD to root failed " + getLastMessage());
             }
          }
          finally { inConnect_ = false; }

          if (connected_)                                               // @D2a
             super.fireEvent(FTPEvent.FTP_CONNECTED);                   // @D2a
       }

       return connected_;
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

        if (connect())
           return super.dir();
        else
           return new String[0];
    }











// ---------------------------------------------------------------------------
   /**
    * Starts the process of getting a file from the server.  AS400FTP
    * opens the data connection to the server, then opens the file on
    * the server and returns an input stream to the caller.  The caller
    * reads the file's data from the input stream.
    * The source file is on the server, accessed via FTP so the path
    * separator character (if any) must be a forward slash.
    *
    *   @param fileName The file to get.
    *   @return An input stream to the file.  The caller uses the input
    *           stream to read the data from the file.  Null is returned
    *           if the connection to the server fails.
    *   @exception IOException If an error occurs while communicating with the server.
    *   @exception FileNotFoundException If the name is a directory or the name is not found.
   **/

    public InputStream get(String fileName)
                       throws IOException, FileNotFoundException
    {
       if (connect())
          return super.get(fileName);
       else
          return null;
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
       if (connect())
          return super.get(sourceFileName, targetFileName);
       else
          return false;
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
       if (connect())
          return super.get(sourceFileName, targetFile);
       else
          return false;
    }







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
       if (connect())
          return super.getCurrentDirectory();
       else
          return null;
    }





// ---------------------------------------------------------------------------
   /**
    * Returns the public authority of save files created by
    * this object.  If this object detects putting a save file
    * to the library file system of the AS/400, it will first
    * create the save file by sending a CRTSAVF command to
    * the AS/400.  The method lets you set the *PUBLIC authority
    * value on the CRTSAVF command.  The default is *EXCLUDE.
    *   @return The authority granted to *PUBLIC.
   **/

    public String getSaveFilePublicAuthority()
    {
       return saveFilePublicAuthority_;
    }






// ---------------------------------------------------------------------------
   /**
    * Returns the name of the server.  Null is returned if no system has
    * been set.
    *   @return The name of the server to which this object connects.
   **/

    public AS400 getSystem()
    {
       return system_;
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
       boolean result = true;

       if (! inConnect_)
          result = connect();

       if (result)
          return super.issueCommand(cmd);
       else
          return null;
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
       if (connect())
          return super.ls();
       else
          return new String[0];
    }




// ---------------------------------------------------------------------------
   /**
    * Sends the NOOP (no operation) command to the server.  This
    * request is most useful to see if the connection to the server
    * is still active
    *   @return true if the request was successful, false otherwise.
    *   @exception IOException If an error occurs while communicating with the server.
   **/

    public boolean noop()
                   throws IOException
    {
       boolean result = true;

       if (! inConnect_)
          result = connect();

       if (result)
          return super.noop();
       else
          return false;
    }





// ---------------------------------------------------------------------------
   /**
    * Starts the process of putting a file to the server.  AS400FTP
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
       OutputStream result = null;

       if (connect())
       {
          saveFileProcessing(fileName);
          try     { result = super.put(fileName);  }
          finally { inSaveFileProcessing_ = false; }
       }

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

    public synchronized boolean put(String sourceFileName, String targetFileName)
                   throws IOException
    {
       boolean result = false;

       if (connect())
       {
          saveFileProcessing(targetFileName);
          try     { result = super.put(sourceFileName, targetFileName); }
          finally { inSaveFileProcessing_ = false; }
       }

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
       boolean result = false;

       if (connect())
       {
          saveFileProcessing(targetFileName);
          try     { result = super.put(sourceFileName, targetFileName); }
          finally { inSaveFileProcessing_ = false; }
       }

       return result;
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

       if (connect())
          return super.pwd();
       else
          return null;
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

        connected_            = false;
        inConnect_            = false;
        inSaveFileProcessing_ = false;
    }







// ---------------------------------------------------------------------------
   // does processing before putting a save file

   private void saveFileProcessing(String target)
                                   throws IOException
   {
      // My wonderful design causes this method to be called
      // multiple times.  put(xx,xx) calls put(yy,yy) which
      // calls put(zz).  Each of the put routines calls this
      // method so, if the first put method is called this method
      // will be called three times.  To prevent doing this function
      // three times I will set a flag here and clear it when I
      // exit the put routine.
      if (! inSaveFileProcessing_)
      {
         inSaveFileProcessing_ = true;

         target = target.toUpperCase();
         target = target.trim();

         // First look for the .savf extension.  If the extension
         // is not .savf then skip all this special processing
         // and do a normal put.
         if (target.endsWith(".SAVF"))
         {
            // The extension is .savf so we may need to do
            // special processing.  Next determine if the name
            // is fully qualified or relative to the current
            // directory.  If relative then do a pwd() to get
            // the current directory.
            if (! target.startsWith("/"))
            {
               String currentPath = pwd();

               if (Trace.isTraceOn())
                  Trace.log(Trace.DIAGNOSTIC,"pwd: " + currentPath);

               // If I am not able to get the current path
               // then just return and attempt to do a normal
               // put.
               if (currentPath == null)
                  return;

               // We received something from the AS/400.  On
               // the AS/400, pwd() returns
               //   257  "/a/b/c" is current directory
               // so I need to pull the real directory out
               // of the message.  If the message does not
               // start with 257 then I received some other
               // unexpected message so I will return and
               // do normal processing.
               if (currentPath.startsWith("257"))
               {
                  int start = currentPath.indexOf("\"");

                  if (start < 0)
                     return;
                  else
                     start++;

                  int end   = currentPath.indexOf("\"", start);

                  if (end < start)
                     return;

                  currentPath = currentPath.substring(start,end);

                  if (Trace.isTraceOn())
                     Trace.log(Trace.DIAGNOSTIC, start + " " + end + " " + currentPath);
               }
               else
                  return;

               // Now finish building the fully qualified path + name
               if (currentPath.endsWith("/"))
                  target = currentPath + target;
               else
                  target = currentPath + "/" + target;

            }

            if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC,"target: " + target);

            try
            {
               // "target" if the fully qualified name.  Now see if the
               // name is in the library file system.  Load target
               // into QSYSObjectPathName.  If illegalPathName exception
               // is thrown then we are in ifs not the library file
               // system so don't do special processing.
               QSYSObjectPathName target2 = new QSYSObjectPathName(target);

               // We didn't get an exception so we are in the library file
               // system.  The next step is to create the save file.
               // Create a command call string to do a create save file.
               CommandCall c = new CommandCall(system_);

               String command = "CRTSAVF " +
                                " FILE(" + target2.getLibraryName() + "/" + target2.getObjectName() + ")" +
                                " AUT(" + saveFilePublicAuthority_ + ")";

               if (Trace.isTraceOn())
                  Trace.log(Trace.DIAGNOSTIC,"command string " + command);

               try { c.setCommand(command); } catch (PropertyVetoException pve) {}
               c.setThreadSafe(false);  // CRTSAVF is not threadsafe.  @A1A

               try
               {
                  boolean result = c.run();

                  AS400Message[] messageList = c.getMessageList();
                  String crtSaveFileMessage = messageList[0].getText();

                  if (Trace.isTraceOn())
                      Trace.log(Trace.DIAGNOSTIC,"message[0]: " + crtSaveFileMessage);

                  // ignore 'save file already exists' message
                  if (! result)
                  {
                     if (messageList[0].getID().startsWith("CPF5813"))
                        result = true;
                  }

                  if (! result)
                  {
                     String MRIMessage = " ";
                     MRIMessage = loader_.getText("CREATE_SAVE_FILE_FAILED");
                     throw new IOException(MRIMessage + crtSaveFileMessage);
                  }
               }
               catch (Exception e)
               {
                  if (Trace.isTraceOn())
                     Trace.log(Trace.DIAGNOSTIC,"IO Exception running command call ", e);

                  throw new IOException(e.getMessage());
               }

               // The last step is to set transfer mode to binary.  After
               // setting the mode to binary we are ready to put
               // the data.
               setDataTransferType(FTP.BINARY);

            }
            catch (IllegalPathNameException e)
            {
               if (Trace.isTraceOn())
                  Trace.log(Trace.DIAGNOSTIC,"the put ends up in ifs");
            }
         }
         else
         {
            if (Trace.isTraceOn())
              Trace.log(Trace.DIAGNOSTIC,"the put is not for a save file ");
         }
      }
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
       if (connect())
          return super.setCurrentDirectory(directory);
       else
          return false;
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
       if (connect())
          super.setDataTransferType(transferType);
    }



// ---------------------------------------------------------------------------
   /**
    * Calling setPassword() is valid only for FTP objects.  An
    * IllegalStateException is thrown if setPassword() is called
    * on an AS400FTP object.  SetPassword() is not needed
    * because AS400FTP gets the
    * password from the AS400 object.
   **/

    public void setPassword(String password)
    {
       if (inConnect_)
          super.setPassword(password);
       else
          throw new IllegalStateException("password");
    }







// ---------------------------------------------------------------------------
   /**
    * Sets the public authority of save files created by
    * this object.  If this object detects putting a save file
    * to the library file system of the AS/400, it will first
    * create the save file by sending a CRTSAVF command to
    * the AS/400.  Through this method you can set the *PUBLIC
    * authority used when sending the CRTSAVF command.  The
    * value is set only if the save file is created.  The public
    * authority of an existing save file is not changed.
    * <P>Valid values are:
    * <ul>
    * <li>*ALL
    * <li>*CHANGE
    * <li>*EXCLUDE
    * <li>*LIBCRTAUT
    * <li>*USE
    * <li>authorization-list name.
    * </ul>
    * <P> The default value is *EXCLUDE
    * @param publicAuthoirty *PUBLIC authority.
    * @exception PropertyVetoException If the change is vetoed.
   **/

    public void setSaveFilePublicAuthority(String publicAuthority)
                                           throws PropertyVetoException
    {
       if (publicAuthority == null)
          throw new NullPointerException("saveFilePublicAuthority");

       if (publicAuthority.length() == 0)
          throw new IllegalArgumentException("saveFilePublicAuthority");

       // Remember the old system.
       String oldValue = saveFilePublicAuthority_;

       // Fire a vetoable change event for system.
       vetos_.fireVetoableChange("saveFilePublicAuthority", oldValue, publicAuthority);

       saveFilePublicAuthority_ = publicAuthority;

       // Fire the property change event.
       changes_.firePropertyChange("saveFilePublicAuthority", oldValue, publicAuthority);
    }




// ---------------------------------------------------------------------------
   /**
    * Calling setServer() is valid only for FTP objects.  An
    * IllegalStateException is thrown if setServer() is called
    * on an AS400FTP object.  SetServer() is not needed
    * because AS400FTP gets the
    * system name from the AS400 object.
    *   @exception PropertyVetoException If the change is vetoed.
   **/

    public void setServer(String server)
                          throws PropertyVetoException
    {
       if (inConnect_)
          super.setServer(server);
       else
          throw new IllegalStateException("server");
    }








// ---------------------------------------------------------------------------
   /**
    * Sets the name of the server.  The system name cannot be changed once
    * a connection is made to the server.
    *   @param system The name of the server to which this object connects.
    *   @exception PropertyVetoException If the change is vetoed.
   **/

    public synchronized void setSystem(AS400 system)
                             throws PropertyVetoException
    {
       if (system == null)
          throw new NullPointerException("system");

       if (connected_)
          throw new ExtendedIllegalStateException("system",
                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

       // Remember the old system.
       AS400 oldSystem = system_;

       // Fire a vetoable change event for system.
       vetos_.fireVetoableChange("system", oldSystem, system);

       system_ = system;

       // Fire the property change event.
       changes_.firePropertyChange("system", oldSystem, system);
    }




// ---------------------------------------------------------------------------
   /**
    * Calling setUser() is valid only for FTP objects.  An
    * IllegalStateException is thrown if setUser() is called
    * on an AS400FTP object.  SetUser() is not needed
    * because AS400FTP gets the
    * userid from the AS400 object.
    *   @exception PropertyVetoException If the change is vetoed.
   **/

    public void setUser(String user)
                        throws PropertyVetoException
    {
       if (inConnect_)
          super.setUser(user);
       else
          throw new IllegalStateException("user");
    }

}



