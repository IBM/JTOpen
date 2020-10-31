///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400FTP.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.net.*;
import java.util.*;
import java.beans.*;



/**
 * Represents a client for the IBM i FTP server.
 * It is written to take advantage of IBM i features.
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
 * One restriction when the client is an IBM i system --
 * the userid and password must be
 * set on the AS400 object in this environment.  That is, *CURRENT
 * will not work when connecting from one IBM i system to another IBM i system.
 * <LI>
 * Automatically setting the current directory to the
 * root directory on the system when a connection is made. If the
 * FTP class is used, the root may be QSYS.LIB when
 * a connection is made.  The integrated file system name
 * of objects in libraries must be used to access them.
 * For example, /QSYS.LIB/MYLIB.LIB/MYFILE.FILE.  See
 * <A HREF="QSYSObjectPathName.html">
 * QSYSObjectPathName</A> documentation for more information.
 * <LI>
 * Handling extra work necessary to put a save file
 * to the system.  If you use the FTP class, you have to
 * do the extra steps.  The extra processing is done only
 * if the extension of the file is .savf and the file will be
 * put into an IBM i library.
 * <P>
 * The extra processing includes internally using Toolbox CommandCall
 * to create the save file on the system.  The default *Public authority
 * is *EXCLUDE.  Use
 * the setSaveFilePublicAuthority() method to change the *Public
 * authority value specified on the create save file command.
 * </OL>
 * <P>
 * No encryption is provided by this class.  The userid and password
 * flow un-encrypted to the system.  This class is not SSL enabled.
 * <P>
 * The forward slash is the separator character for paths sent
 * to the FTP server.
 * <P>
 * The following example puts a save file to the system.
 * Note the application does not set data transfer type to
 * binary or use Toolbox CommandCall to create the save file.
 * Since the extension is .savf,
 * AS400FTP class detects the file to put is a save file
 * so it does these step automatically.
 *
 * <pre>
 * AS400 system = new AS400();
 * AS400FTP   ftp    = new AS400FTP(system);
 * ftp.put("myData.savf", "/QSYS.LIB/MYLIB.LIB/MYDATA.SAVF");
 * </pre>
 *
**/

public class AS400FTP
             extends FTP
             implements java.io.Serializable
{
    static final long serialVersionUID = 4L;


    // **********************************************************
    // *                                                        *
    // * This class has two main tasks:                         *
    // *   1) Use the Toolbox AS400 object to identify          *
    // *      the system, userid and password, the use the      *
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
    * requests are sent to the system.
    * @see #setSystem
    **/

    public AS400FTP()
    {
    }





// -----------------------------------------------------------------------
   /**
    * Constructs an AS400FTP object.
    *   @param system The system to which to connect.
   **/

    public AS400FTP(AS400 system)
    {
       if (system == null)
          throw new NullPointerException("system");

       system_ = system;
    }



// @D5 new method
// ---------------------------------------------------------------------------
   /**
    * Starts the process of appending to a file on the system.  AS400FTP
    * opens the data connection to the system, then opens the file on
    * the system and returns an output stream to the caller.  The caller
    * then writes the file's data to the output stream.
    *   @param fileName The file to put.
    *   @return An output stream to the file.  The caller uses the output
    *           stream to write data to the file.
    *           Null is returned if the connection to the system fails.
    *   @exception IOException If an error occurs while communicating with the system.
   **/

    public synchronized OutputStream append(String fileName)
                                     throws IOException
    {
       OutputStream result = null;

       if (connect())
       {
          saveFileProcessing(fileName);
          try     { result = super.append(fileName);  }
          finally { inSaveFileProcessing_ = false; }
       }

       return result;
    }




// @D5 new method
// ---------------------------------------------------------------------------
   /**
    * Appends to a file on the system.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the system.
    *   @return true if the append was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
   **/

    public synchronized boolean append(String sourceFileName, String targetFileName)
                   throws IOException
    {
       boolean result = false;

       if (connect())
       {
          saveFileProcessing(targetFileName);
          try     { result = super.append(sourceFileName, targetFileName); }
          finally { inSaveFileProcessing_ = false; }
       }

       return result;
    }





// @D5 new method
// ---------------------------------------------------------------------------
   /**
    * Appends to a file on the system.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the system.
    *   @return true if the append was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
   **/

    public synchronized boolean append(java.io.File sourceFileName, String targetFileName)
                   throws IOException
    {
       boolean result = false;

       if (connect())
       {
          saveFileProcessing(targetFileName);
          try     { result = super.append(sourceFileName, targetFileName); }
          finally { inSaveFileProcessing_ = false; }
       }

       return result;
    }



// ---------------------------------------------------------------------------
   /**
    * Sets the current directory on the system to <i>directory</i>.
    * The method is the same as setCurrentDirectory().
    * The message returned from the system is saved.  Use getLastMessage()
    * to retrieve it.
    *   @param directory The current directory to set on the system.
    *   @return true if directory changed; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Connects to the system.
    * The AS400 object must be set
    * before calling this method.
    * Calling connect is optional.  Methods that communicate
    * with the system such as get, put, cd, and ls call connect()
    * if necessary.
    * The message returned from the system is saved.  Use getLastMessage()
    * to retrieve it.
    *   @return true if connection is successful; false otherwise.
    *   @exception UnknownHostException If a path to the system cannot be found.
    *   @exception IOException If an error occurs while connecting to the system.
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
          // IOException.  The @D5 change is to call signon(false)
          // instead of getVRM();
          try
          {
             system_.signon(false);                                           // @D5a
          }
          catch (AS400SecurityException e)
          {
             if (Trace.isTraceOn())
                Trace.log(Trace.DIAGNOSTIC,"Security exception in getVRM()", e);

             IOException throwException = new IOException(e.getMessage());
             try { 
               throwException.initCause(e); 
             } catch (Throwable t) {}
             throw throwException;
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
    *   @exception IOException If an error occurs while communicating with the system.
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
// @d4 new method
   /**
    * Lists the contents of the current directory.  File name and
    * attributes are returned for each entry in the directory.
    * A zero length array is returned if the directory is empty
    * or if no files match the search criteria.
    *   @return The contents of the directory as an array of Strings.
    *   @param criteria The search criteria.
    *   @exception IOException If an error occurs while communicating with the system.
   **/

    public String[] dir(String criteria)
                    throws IOException
    {

        if (connect())
           return super.dir(criteria);
        else
           return new String[0];
    }











// ---------------------------------------------------------------------------
   /**
    * Starts the process of getting a file from the system.  AS400FTP
    * opens the data connection to the system, then opens the file on
    * the system and returns an input stream to the caller.  The caller
    * reads the file's data from the input stream.
    * The source file is on the system, accessed via FTP so the path
    * separator character (if any) must be a forward slash.
    *
    *   @param fileName The file to get.
    *   @return An input stream to the file.  The caller uses the input
    *           stream to read the data from the file.  Null is returned
    *           if the connection to the system fails.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Gets a file from the system.
    * The source file is on the system, accessed via FTP so the path
    * separator character (if any) must be a forward slash.
    * The target file is on the client, accessed via java.io
    * so the path separator character (if any) must be client specific.
    *   @param sourceFileName The file to get on the system.
    *   @param targetFileName The file on the target file system.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Gets a file from the system.
    * The source file is on the system, accessed via FTP so the path
    * separator character (if any) must be a forward slash.
    * The target file is an instance of Java.io.file.
    *   @param sourceFileName The file to get on the system.
    *   @param targetFile The file on the target file system.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Returns the current directory on the system.
    *   @return The current directory on the system.
    *           Null is returned if the connection to the system fails.
    *   @exception IOException If an error occurs while communicating with the system.
   **/

    synchronized public String getCurrentDirectory()
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
    * to the library file system of the system, it will first
    * create the save file by sending a CRTSAVF command to
    * the system.  The method lets you set the *PUBLIC authority
    * value on the CRTSAVF command.  The default is *EXCLUDE.
    *   @return The authority granted to *PUBLIC.
   **/

    public String getSaveFilePublicAuthority()
    {
       return saveFilePublicAuthority_;
    }






// ---------------------------------------------------------------------------
   /**
    * Returns the name of the system.  Null is returned if no system has
    * been set.
    *   @return The name of the system to which this object connects.
   **/

    public AS400 getSystem()
    {
       return system_;
    }








// ---------------------------------------------------------------------------
   /**
    * Sends a command to the system, returning the reply from the system.
    * <P>
    * The command is not altered before sending it to the system, so it
    * must be recognized by the system.  Many FTP applications change
    * commands so they are recognized by the system.  For example, the
    * command to get a list of files from the system is NLST, not ls.  Many
    * FTP applications convert ls to NLST before sending the command to
    * the system.  This method will not do the conversion.
    *   @param cmd The command to send to the system.
    *   @return The reply to the command.
    *           Null is returned if the connection to the system fails.
    *   @exception IOException If an error occurs while communicating with the system.
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
    *   @exception IOException If an error occurs while communicating with the system.
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
// @d4 new method
   /**
    * Lists the contents of the current directory.  If the directory
    * is empty or no files match the search criteria, an empty list is returned.
    *   @return The contents of the directory as an array of Strings.
    *   @param criteria The search criteria.
    *   @exception IOException If an error occurs while communicating with the system.
   **/

    public String[] ls(String criteria)
                    throws IOException
    {
       if (connect())
          return super.ls(criteria);
       else
          return new String[0];
    }






// ---------------------------------------------------------------------------
   /**
    * Sends the NOOP (no operation) command to the system.  This
    * request is most useful to see if the connection to the system
    * is still active
    *   @return true if the request was successful, false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Starts the process of putting a file to the system.  AS400FTP
    * opens the data connection to the system, then opens the file on
    * the system and returns an output stream to the caller.  The caller
    * then writes the file's data to the output stream.
    *   @param fileName The file to put.
    *   @return An output stream to the file.  The caller uses the output
    *           stream to write data to the file.
    *           Null is returned if the connection to the system fails.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Puts a file to the system.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the system.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Puts a file to the system.
    *   @param sourceFileName The file to put.
    *   @param targetFileName The file on the system.
    *   @return true if the copy was successful; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * Returns the current directory on the system.  PWD is the ftp
    * command Print Working Directory.
    *   @return The current directory on the system.
    *           Null is returned if the connection to the system fails.
    *   @exception IOException If an error occurs while communicating with the system.
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

               // We received something from the server.  On
               // the server, pwd() returns
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
               c.suggestThreadsafe(false);  // CRTSAVF is not threadsafe.  @A1A

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
                  IOException throwException = new IOException(e.getMessage());
                  try { 
                    throwException.initCause(e); 
                  } catch (Throwable t) {}
                  throw throwException;

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
    * Sets the current directory on the system to <i>directory</i>.
    * The method is the same as cd().
    * The message returned from the system is saved.  Use getLastMessage()
    * to retrieve it.
    *   @param directory The current directory to set on the system.
    *   @return true if directory changed; false otherwise.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * <LI>{@link FTP#ASCII ASCII}
    * <LI>{@link FTP#BINARY BINARY}
    * </UL>
    * <P>
    * If a connection does not
    * already exist, a connection is made to the system.
    * The message returned from the system is saved.  Use getLastMessage()
    * to retrieve it.
    *   @exception IOException If an error occurs while communicating with the system.
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
    * to the library file system of the system, it will first
    * create the save file by sending a CRTSAVF command to
    * the system.  Through this method you can set the *PUBLIC
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
    * @param publicAuthority *PUBLIC authority.
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
       if (vetos_ != null) {
         vetos_.fireVetoableChange("saveFilePublicAuthority", oldValue, publicAuthority);
       }

       saveFilePublicAuthority_ = publicAuthority;

       // Fire the property change event.
       if (changes_ != null) {
         changes_.firePropertyChange("saveFilePublicAuthority", oldValue, publicAuthority);
       }
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
    * Sets the name of the system.  The system name cannot be changed once
    * a connection is made to the system.
    *   @param system The name of the system to which this object connects.
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
       if (vetos_ != null) {
         vetos_.fireVetoableChange("system", oldSystem, system);
       }

       system_ = system;

       // Fire the property change event.
       if (changes_ != null) {
         changes_.firePropertyChange("system", oldSystem, system);
       }
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



