///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFileImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InterruptedIOException;
import java.io.IOException;
import java.util.Vector;

/**
 Provides a full remote implementation for the IFSFile class.
 **/
class IFSFileImplRemote
implements IFSFileImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  /**
   The integrated file system directory separator string used to separate directory/file components in a path.
   **/
  final static String separator = "/";
  /**
   The integrated file system directory separator character used to separate directory/file components in a path.
   **/
  final static char separatorChar = '/';

  transient private IFSListAttrsRep attributes_; // cached attributes
  transient private ConverterImplRemote converter_; // character converter for path names
  private String path_ = "";
  transient private int preferredServerCCSID_;
  transient private AS400Server server_;
  private AS400ImplRemote system_;

  // Static initialization code.
  static
  {
    // Add all byte stream reply data streams of interest to the
    // AS400 server's reply data stream hash table.
    AS400Server.addReplyStream(new IFSCloseRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSExchangeAttrRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSListAttrsRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSOpenRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSQuerySpaceRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
  }

  /**
   Determines if the applet or application can read from the integrated file system object represented by this object.
   **/
  public int canRead0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    return (checkAccess(IFSOpenReq.READ_ACCESS));
  }


  /**
   Determines if the applet or application can write to the integrated file system object represented by this object.
   **/
  public int canWrite0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    return (checkAccess(IFSOpenReq.WRITE_ACCESS));
  }

  // Determine if the directory entry can be accessed in the specified
  // manner.
  private int checkAccess(int access)
    throws IOException
  {
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    // Try to open the file for the specified type of access.
    try
    {
      // Convert the path name to the AS/400 CCSID.
      byte[] pathname = converter_.stringToByteArray(path_);

      // Process an open file request.
      IFSOpenReq req = new IFSOpenReq(pathname, preferredServerCCSID_,
                                      0, access, IFSOpenReq.DENY_NONE,
                                      IFSOpenReq.NO_CONVERSION, 8);
      ClientAccessDataStream ds = (ClientAccessDataStream) server_.sendAndReceive(req);
      if (ds instanceof IFSOpenRep)
      {
        // The open was successful.  Close the file.  We don't
        // check the close reply because a failure at this point
        // isn't of interest.  
        returnCode = IFSReturnCodeRep.SUCCESS;
        IFSCloseReq closeReq = new
          IFSCloseReq(((IFSOpenRep) ds).getFileHandle());
        ds = (ClientAccessDataStream) server_.sendAndReceive(closeReq);
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        returnCode = ((IFSReturnCodeRep) ds).getReturnCode();
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
        {
          Trace.log(Trace.ERROR, "IFSReturnCodeRep return code ",
                    returnCode);
        }
      }
      else
      {
        // Unknown data stream.
        Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
        throw new
          InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                                 InternalErrorException.DATA_STREAM_UNKNOWN);
      }
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost", e);
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted", e);
      throw new InterruptedIOException(e.getMessage());
    }

    return returnCode;
  }

  // Establish communications with the AS400.
  private void connect()
    throws IOException, AS400SecurityException
  {
    // Connect to the AS400 byte stream server.
    try
    {
      server_ = system_.getConnection(AS400.FILE, false);
    }
    catch(AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, "Access to byte stream server on '" +
                system_.getSystemName() + "' denied.");
      throw (AS400SecurityException)e.fillInStackTrace();
    }

    // Exchange attributes with the server.
    synchronized (server_)
    {
	IFSExchangeAttrRep rep =
	  (IFSExchangeAttrRep)server_.getExchangeAttrReply();
	if (rep == null)
	{
	    try
	    {
	        // Use GMT date/time, don't use posix style return codes,
	        // use PC pattern matching semantics, maximum data transfer size
	        // of 0xffffffff, preferred CCSID of 0xf200.
		rep = (IFSExchangeAttrRep)server_.sendExchangeAttrRequest(new IFSExchangeAttrReq(true, false,
					 IFSExchangeAttrReq.PC_PATTERN_MATCH,
					 0xffffffff, 0xf200));
	    }
	    catch(ConnectionDroppedException e)
	    {
		Trace.log(Trace.ERROR, "Byte stream server connection lost");
		connectionDropped(e);
	    }
	    catch(InterruptedException e)
	    {
		system_.disconnectServer(server_);
		server_ = null;
		Trace.log(Trace.ERROR, "Connection attempt interrupted.");
		throw new InterruptedIOException(e.getMessage());
	    }
	    catch(IOException e)
	    {
		Trace.log(Trace.ERROR, "I/O error during attribute exchange.");
		system_.disconnectServer(server_);
		server_ = null;
		throw (IOException)e.fillInStackTrace();
	    }
	}

        // Process the exchange attributes reply.
	if (rep instanceof IFSExchangeAttrRep)
	{
	    preferredServerCCSID_ = rep.getPreferredCCSID();
	    converter_ = ConverterImplRemote.getConverter(preferredServerCCSID_, system_);
	}
    }
  }

  // Disconnect from the byte stream server.
  private void connectionDropped(ConnectionDroppedException e)
    throws ConnectionDroppedException
  {
    if (server_ != null)
    {
      system_.disconnectServer(server_);
      server_ = null;
    }
    throw (ConnectionDroppedException)e.fillInStackTrace();
  }


  /**
   Deletes the integrated file system object represented by this object.
   **/
  public int delete0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = converter_.stringToByteArray(path_);

    // Determine if this is a file or directory and instantiate the
    // appropriate type of delete request.
    IFSDataStreamReq req =
      new IFSDeleteFileReq(pathname, preferredServerCCSID_);
    try
    {
      if (isDirectory0() == IFSReturnCodeRep.SUCCESS)
      {
        req = new IFSDeleteDirReq(pathname, preferredServerCCSID_);
      }
    }
    catch (Exception e)
    {
      if (Trace.isTraceOn() && Trace.isTraceWarningOn())
        Trace.log(Trace.WARNING,
                 "Unable to determine if file or directory.\n" + e.toString());
    }

    // Delete this entry.
    ClientAccessDataStream ds = null;
    try
    {
      // Send a delete request.
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted");
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify that the request was successful.
    int rc = 0;
    if (ds instanceof IFSReturnCodeRep)
    {
      rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc != IFSReturnCodeRep.SUCCESS)
      {
        if (Trace.isTraceOn() && Trace.isTraceErrorOn())
          Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
      }
    }
    else
    {
      // Unknown data stream.
      Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
      throw new
        InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                               InternalErrorException.DATA_STREAM_UNKNOWN);
    }

    // Clear any cached file attributes.
    attributes_ = null;

    return (rc);
  }


  /**
   Determines if the integrated file system object represented by this object exists.
   **/
  public int exists0(String name)
    throws IOException, AS400SecurityException
  {
    int returnCode = IFSReturnCodeRep.SUCCESS;

    // Ensure that we are connected to the server.
    connect();

    // @A8D if (attributes_ == null)
    // @A8D {
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
      // Attempt to list the attributes of the specified file.
      try
      {
        Vector replys = listAttributes(name);
        if (replys != null && replys.size() == 1)
        {
          returnCode = IFSReturnCodeRep.SUCCESS;
          attributes_ = (IFSListAttrsRep) replys.elementAt(0);
        }
      }
      catch (AS400SecurityException e)
      {
        returnCode = IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY;
      }
    // @A8D }
    return (returnCode);
  }


  /**
   Returns the path name of the integrated file system object represented by this object.  This is the full path starting at the root directory.
   @return The absolute path name of this integrated file system object.
   **/
  String getAbsolutePath()
  {
    return path_;
  }


  /**
   Determines the amount of unused storage space in the file system.
   @return The number of bytes of storage available.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.

   **/
  public long getFreeSpace()
    throws IOException
  {
    long freeSpace = 0L;

    // Ensure that we are connected to the server.
    try
    {
      connect();
    }
    catch (AS400SecurityException e)
    {
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Query the available space.
    ClientAccessDataStream ds = null;
    try
    {
      // Issue a query space request.
      IFSQuerySpaceReq req = new IFSQuerySpaceReq(true);
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted");
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify the reply.
    int rc = 0;
    if (ds instanceof IFSQuerySpaceRep)
    {
      freeSpace = ((IFSQuerySpaceRep) ds).getFreeSpace();
    }
    else if (ds instanceof IFSReturnCodeRep)
    {
      rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc != IFSReturnCodeRep.SUCCESS)
      {
        Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
      }
      throw new ExtendedIOException(rc);
    }
    else
    {
      // Unknown data stream.
      Trace.log(Trace.ERROR, "Unknown reply data stream ",
                ds.getReqRepID());
      throw new
        InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                               InternalErrorException.DATA_STREAM_UNKNOWN);
    }

    return freeSpace;
  }



  /**
   Determines if the integrated file system object represented by this object is a directory.
  **/
  public int isDirectory0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    IFSListAttrsRep reply = attributes_;
    if (reply == null)
    {
      try
      {
        // Attempt to list the attributes of the specified file.
        Vector replys = listAttributes(path_);

        // If this is a directory then there must be exactly one reply.
        if (replys != null && replys.size() == 1)
        {
          reply = (IFSListAttrsRep) replys.elementAt(0);
          attributes_ = reply;
        }
      }
      catch (AS400SecurityException e)
      {
        reply = null;
        returnCode = IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY;
      }
    }

    // Determine if the file attributes indicate a directory.
    if (reply != null)
    {
      boolean answer = false;
      switch(reply.getObjectType())
      {
        case IFSListAttrsRep.DIRECTORY:
        case IFSListAttrsRep.FILE:
          answer = ((reply.getFixedAttributes() &
                     IFSListAttrsRep.FA_DIRECTORY) != 0);
          break;
        case IFSListAttrsRep.AS400_OBJECT:
          // AS/400 libraries and database files look like directories.
          answer = (path_.endsWith(".LIB") || path_.endsWith(".FILE") ||
                    path_.endsWith(".LIB" + IFSFile.separator) ||
                    path_.endsWith(".FILE" + IFSFile.separator));
          break;
        default:
          answer = false;
      }
      if (answer == true)
      {
        returnCode = IFSReturnCodeRep.SUCCESS;
      }
    }
    return returnCode;
  }


  /**
   Determines if the integrated file system object represented by this object is a "normal" file.<br>
   A file is "normal" if it is not a directory or a container of other objects.
   **/
  public int isFile0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    IFSListAttrsRep reply = attributes_;
    if (reply == null)
    {
      try
      {
        // Attempt to list the attributes of the specified file.
        Vector replys = listAttributes(path_);

        // If this is a file then there must be exactly one reply.
        if (replys != null && replys.size() == 1)
        {
          reply = (IFSListAttrsRep) replys.elementAt(0);
          attributes_ = reply;
        }
      }
      catch (AS400SecurityException e)
      {
        reply = null;
        returnCode = IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY;
      }
    }

    // Determine if the file attributes indicate a directory.
    if (reply != null)
    {
      boolean answer = false;
      switch(reply.getObjectType())
      {
        case IFSListAttrsRep.DIRECTORY:
        case IFSListAttrsRep.FILE:
          answer = ((reply.getFixedAttributes() &
                     IFSListAttrsRep.FA_DIRECTORY) == 0);
          break;
        case IFSListAttrsRep.AS400_OBJECT:
          // AS/400 libraries and database files look like directories.
          answer = !(path_.endsWith(".LIB") || path_.endsWith(".FILE") ||
                     path_.endsWith(".LIB" + IFSFile.separator) ||
                     path_.endsWith(".FILE" + IFSFile.separator));
          break;
        default:
          answer = false;
      }
      if (answer == true)
      {
        returnCode = IFSReturnCodeRep.SUCCESS;
      }
    }
    return returnCode;
  }


  /**
   Determines the time that the integrated file system object represented by this object was last modified.
   **/
  public long lastModified0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    long modificationDate = 0L;

    // Attempt to list the attributes of the specified file.
    // Note: Do not use cached attributes, since they may be out of date.
    Vector replys = listAttributes(path_);

    // There should only be one reply.
    if (replys != null && replys.size() == 1)
    {
      IFSListAttrsRep reply = (IFSListAttrsRep) replys.elementAt(0);
      attributes_ = reply;
      modificationDate = reply.getModificationDate();
    }

    return modificationDate;
  }


  /**
   Determines the length of the integrated file system object represented by this object.
   **/
  public long length0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    long size = 0L;

    // Attempt to list the attributes of the specified file.
    // Note: Do not use cached attributes, since they may be out of date.
    Vector replys = listAttributes(path_);

    // There should only be one reply.
    if (replys != null && replys.size() == 1)
    {
      IFSListAttrsRep reply = (IFSListAttrsRep) replys.elementAt(0);
      attributes_ = reply;
      size = reply.getSize();
    }

    return size;
  }


  // Fetch list attributes reply(s) for the specified path.
  private Vector listAttributes(String path)
    throws IOException, AS400SecurityException
  {
    // Assume the caller has already done a connect().

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = converter_.stringToByteArray(path);

    // Process attribute replies.
    Vector replys = new Vector(256);
    IFSListAttrsReq req = new IFSListAttrsReq(pathname,
                                              preferredServerCCSID_);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted");
      throw new InterruptedIOException(e.getMessage());
    }

    // @A1A
    int rc = -1;        // @A1A

    boolean done = false;
    do
    {
      if (ds instanceof IFSListAttrsRep)
      {
        replys.addElement(ds);
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        // If the return code is NO_MORE_FILES then all files
        // that match the specification have been returned.
        rc = ((IFSReturnCodeRep) ds).getReturnCode();

        if (rc == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY
        ||  rc == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
        {
          replys = null;
          throw new AS400SecurityException(AS400SecurityException.DIRECTORY_ENTRY_ACCESS_DENIED);
        }

        if (rc != IFSReturnCodeRep.NO_MORE_FILES &&
            rc != IFSReturnCodeRep.FILE_NOT_FOUND &&
            rc != IFSReturnCodeRep.PATH_NOT_FOUND)
        {
          Trace.log(Trace.ERROR, "Error getting attributes for " + path +
                    ": IFSReturnCodeRep return code = ", rc);  // @A9C
          throw new ExtendedIOException(rc);
        }

      }
      else
      {
        // Unknown data stream.
        Trace.log(Trace.ERROR, "Unknown reply data stream for " + path, ds.data_);  // @A9C
        throw new
          InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                                 InternalErrorException.DATA_STREAM_UNKNOWN);
      }

      // Fetch the next reply if not already done.
      done = ((IFSDataStream) ds).isEndOfChain();
      if (!done)
      {
        try
        {
          ds = (ClientAccessDataStream) server_.receive(req.getCorrelation());
        }
        catch(ConnectionDroppedException e)
        {
          Trace.log(Trace.ERROR, "Byte stream server connection lost.");
          connectionDropped(e);
        }
        catch(InterruptedException e)
        {
          Trace.log(Trace.ERROR, "Interrupted");
          throw new InterruptedIOException(e.getMessage());
        }
      }
    }
    while (!done);

    // @A1A
    if (rc == IFSReturnCodeRep.PATH_NOT_FOUND) {        // @A1A
        // If the directory or file does not exist, then return NULL.
        replys = null;                                  // @A1A
    }                                                   // @A1A
    else {                                              // @A1A
        // Set the vector capacity to the current size.
        replys.trimToSize();
    }                                                   // @A1A

    return replys;
  }


  // @A7A
  // List the files/directories in the specified directory.
  // Returns null if specified file or directory does not exist.
  public String[] listDirectoryContents(String directoryPath)
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    Vector replys = listAttributes(directoryPath);
    String[] names = null;

    // Add the name for each file or directory in the specified directory,
    // to the array of names.

    // @A1C
    // Changed the behavior of the list() to conform to that of the JDK1.1.x
    // so that a NULL is returned if and only if the directory or file represented
    // by this IFSFile object doesn't exist.
    //
    // Original code:
    // if (replys != null && replys.size() != 0)
    if (replys != null)                             // @A1C
    {
      names = new String[replys.size()];
      int j = 0;
      for (int i = 0; i < replys.size(); i++)
      {
        IFSListAttrsRep reply = (IFSListAttrsRep) replys.elementAt(i);
        String name = converter_.byteArrayToString(reply.getName());
        if (!(name.equals(".") || name.equals("..")))
        {
          names[j++] = name;
        }
      }

      if (j == 0)
      {
        // @A1C
        //
        // Original code:
        // names = null;
        names = new String[0];      // @A1C
      }
      else if (names.length != j)
      {
        // Copy the names to an array of the exact size.
        String[] newNames = new String[j];
        System.arraycopy(names, 0, newNames, 0, j);
        names = newNames;
      }
    }

    return names;
  }


  /**
   Creates an integrated file system directory whose path name is specified by this object.
   **/
  public int mkdir0(String directory)
    throws IOException, AS400SecurityException
  {
    // Ensure that the path name is set.
    connect();

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    try
    {
      // Convert the directory name to the AS/400 CCSID.
      byte[] pathname = converter_.stringToByteArray(directory);

      // Send a create directory request.
      IFSCreateDirReq req = new IFSCreateDirReq(pathname,
                                                preferredServerCCSID_);
      ClientAccessDataStream ds =
        (ClientAccessDataStream) server_.sendAndReceive(req);

      // Verify the reply.
      if (ds instanceof IFSReturnCodeRep)
      {
        returnCode = ((IFSReturnCodeRep) ds).getReturnCode();
        if (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY
        ||  returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
        {
          throw new AS400SecurityException(AS400SecurityException.DIRECTORY_ENTRY_ACCESS_DENIED);
        }
      }
      else
      {
        // Unknown data stream.
        Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
        throw new
          InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                                 InternalErrorException.DATA_STREAM_UNKNOWN);
      }
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted");
      throw new InterruptedIOException(e.getMessage());
    }

    return returnCode;
  }


  /**
   Creates an integrated file system directory whose path name is specified by this object. In addition, create all parent directories as necessary.
    **/
  public int mkdirs0()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    // Traverse up the parent directories until the first parent
    // directory that exists is found, saving each parent directory
    // as we go.
    boolean success = false;
    Vector nonexistentDirs = new Vector();
    String directory = path_;
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    returnCode = exists0(directory);
    if (returnCode != IFSReturnCodeRep.SUCCESS)
    {
      do
      {
        nonexistentDirs.addElement(directory);
        directory = IFSFile.getParent(directory);
      }
      while (directory != null & (exists0(directory) != IFSReturnCodeRep.SUCCESS));
    } else
    {
    	returnCode = IFSReturnCodeRep.DUPLICATE_DIR_ENTRY_NAME;
    }

    // Create each parent directory in the reverse order that
    // they were saved.
    for (int i = nonexistentDirs.size(); i > 0; i--)
    {
      // Get the name of the next directory to create.
      try
      {
        directory = (String) nonexistentDirs.elementAt(i - 1);
      }
      catch(Exception e)
      {
        Trace.log(Trace.ERROR, "Error fetching element from vector.\n" +
                  "length = " + nonexistentDirs.size() + " index = ",
                  i - 1);
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }

      // Create the next directory.
      returnCode = mkdir0(directory);
      if (returnCode != IFSReturnCodeRep.SUCCESS)
      {
        // Failed to create a directory.
        break;
      }
    }

    return returnCode;
  }


  /**
   Renames the integrated file system object specified by this object to have the path name of <i>file</i>.  Wildcards are not permitted in this file name.
   @param file The new file name.
   **/
  public int renameTo0(IFSFileImpl file)
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    connect();

    // Assume the argument has been validated by the public class.

    // Rename the file.
    boolean success = false;
    ClientAccessDataStream ds = null;
    IFSFileImplRemote otherFile = (IFSFileImplRemote)file;
    try
    {
      // Convert the path names to the AS/400 CCSID.
      byte[] oldName = converter_.stringToByteArray(path_);
      byte[] newName = converter_.stringToByteArray(otherFile.getAbsolutePath());

      // Issue a rename request.
      IFSRenameReq req = new IFSRenameReq(oldName, newName,
                                          preferredServerCCSID_,
                                          false);
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted");
      throw new InterruptedIOException(e.getMessage());
    }

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    // Verify the reply.
    if (ds instanceof IFSReturnCodeRep)
    {
      returnCode = ((IFSReturnCodeRep) ds).getReturnCode();
      success = (returnCode ==
                 IFSReturnCodeRep.SUCCESS);
    }
    else
    {
      // Unknown data stream.
      Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
      throw new
        InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                               InternalErrorException.DATA_STREAM_UNKNOWN);
    }

    if (success)
    {
      path_ = otherFile.getAbsolutePath();

      // Clear any cached attributes.
      attributes_ = null;
    }

    return returnCode;
  }


  /**
   Changes the last modified time of the integrated file system object represented by this object to <i>time</i>.
   @param time The desired last modification time (measured in milliseconds since January 1, 1970 00:00:00 GMT).
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public boolean setLastModified(long time)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    try
    {
      connect();
    }
    catch (AS400SecurityException e)
    {
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Issue a change attributes request.
    ClientAccessDataStream ds = null;
    try
    {
      // Convert the path name to the AS/400 CCSID.
      byte[] pathname = converter_.stringToByteArray(path_);

      IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                    preferredServerCCSID_,
                                                    0, time, 0);
      ds = (ClientAccessDataStream) server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted");
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify the reply.
    boolean success = false;
    if (ds instanceof IFSReturnCodeRep)
    {
      success = (((IFSReturnCodeRep) ds).getReturnCode() ==
                 IFSReturnCodeRep.SUCCESS);
    }
    else
    {
      // Unknown data stream.
      Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
      throw new
        InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                               InternalErrorException.DATA_STREAM_UNKNOWN);
    }

    // Clear any cached attributes.
    attributes_ = null;

    return success;
  }

  /**
   Sets the file path.
   @param path The absolute file path.
   **/
  public void setPath(String path)
  {
    // Assume the argument has been validated by the public class.

    // Ensure that the path is not altered after the connection is
    // established.
    if (server_ != null)
    {
      throw new ExtendedIllegalStateException("path",
                               ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // If the specified path doesn't start with the separator character,
    // add one.  All paths are absolute for IFS.
    String newPath;
    if (path.length() == 0 || path.charAt(0) != separatorChar)
    {
      newPath = separator + path;
    }
    else
    {
      newPath = path;
    }

    // Update the path value.
    path_ = newPath;
  }



  /**
   Sets the system.
   @param system The AS/400 system object.
   **/
  public void setSystem(AS400Impl system)
  {
    // Assume the argument has been validated by the public class.

    system_ = (AS400ImplRemote)system;
  }

}
