///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
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
  transient private IFSListAttrsRep attributesReply_; // "list attributes" reply
  transient private IFSCachedAttributes cachedAttributes_; // cached attributes
  transient private int errorRC_;  // error return code, in cases where listAttributes returns null

  private IFSFileDescriptorImplRemote fd_ = new IFSFileDescriptorImplRemote(); // @B2A

  private boolean isSymbolicLink_;
  private boolean determinedIsSymbolicLink_;
  private int patternMatching_ = IFSFile.PATTERN_DEFAULT;  // pattern-matching semantics


  // Used for debugging only.  This should always be false for production.
  // When this is false, all debug code will theoretically compile out.
  private static final boolean DEBUG = false;  // @B2A

  // Static initialization code.
  static
  {
    // Add all byte stream reply data streams of interest to the
    // AS400 server's reply data stream hash table.
    AS400Server.addReplyStream(new IFSListAttrsRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSOpenRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSQuerySpaceRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
  }

  /**
   Determines if the applet or application can read from the integrated file system object represented by this object.
   **/
  public int canRead()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    return (fd_.checkAccess(IFSOpenReq.READ_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN)); //@D1C
  }


  /**
   Determines if the applet or application can write to the integrated file system object represented by this object.
   **/
  public int canWrite()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    return (fd_.checkAccess(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN)); //@D1C
  }


  /**
   Copies the current file to the specified path.
   **/
  public boolean copyTo(String destinationPath, boolean replace)
    throws IOException, AS400SecurityException, ObjectAlreadyExistsException
  {
    fd_.connect();
    if (Trace.traceOn_ && replace==false && fd_.getSystemVRM() < 0x00050300) {
      Trace.log(Trace.WARNING, "Server is V5R2 or lower, so the 'Do not replace' argument will be ignored.");
    }

    // If the source is a directory, verify that the destination doesn't already exist.
    if (isDirectory() == IFSReturnCodeRep.SUCCESS &&
        exists(destinationPath) == IFSReturnCodeRep.SUCCESS) {
      throw new ObjectAlreadyExistsException(destinationPath, ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);
    }

    return fd_.copyTo(destinationPath, replace);
  }

  /**
   @D3a created0 is a new method
   Determines the time that the integrated file system object represented by this object was created.
   **/
  public long created()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    long creationDate = 0L;


    // Attempt to list the attributes of the specified file.
    // Note: Do not use cached attributes, since they may be out of date.
    IFSListAttrsRep attrs = getAttributeSetFromServer(fd_.path_);
    if (attrs != null)
    {
      attributesReply_ = attrs;
      creationDate = attrs.getCreationDate();
    }

    return creationDate;
  }




  /**
   If files does not exist, create it.  If the files
   does exist, return an error.  The goal is to atomically
   create a new file if and only if the file does not
   yet exist.
  **/
  // @D1 - new method because of changes to java.io.file in Java 2.

  public int createNewFile()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    return (fd_.checkAccess(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_CREATE_FAIL));
  }




  /**
   Deletes the integrated file system object represented by this object.
   **/
  public int delete()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

    // Determine if this is a file or directory and instantiate the
    // appropriate type of delete request.
    IFSDataStreamReq req =
      new IFSDeleteFileReq(pathname, fd_.preferredServerCCSID_);
    try
    {
      if (isDirectory() == IFSReturnCodeRep.SUCCESS)
      {
        req = new IFSDeleteDirReq(pathname, fd_.preferredServerCCSID_);
      }
    }
    catch (Exception e)
    {
      if (Trace.traceOn_) Trace.log(Trace.WARNING,
                 "Unable to determine if file or directory.\n" + e.toString());
    }

    // Delete this entry.
    ClientAccessDataStream ds = null;
    try
    {
      // Send a delete request.
      ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      fd_.connectionDropped(e);
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
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
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
    attributesReply_ = null;

    return (rc);
  }

  //@B1A Moved code from isDirectory() to support determining if a file is a directory
  //without a call to the AS/400.
  /**
   Determines if a file is a directory without a call to the AS/400.
   **/
  private boolean determineIsDirectory(IFSListAttrsRep attributeList)
  {
    boolean answer = false;
    // Determine if the file attributes indicate a directory.
    // Don't need to check if attributeList == null because it has already been
    // checked by the two methods that call it.  Also don't check converter
    // because it is set by a connect() method before calling this.
    String name = fd_.converter_.byteArrayToString(attributeList.getName(/*fd_.serverDatastreamLevel_*/));
    switch (attributeList.getObjectType())
    {
      case IFSListAttrsRep.DIRECTORY:
      case IFSListAttrsRep.FILE:
         answer = ((attributeList.getFixedAttributes() & IFSListAttrsRep.FA_DIRECTORY) != 0);
         break;
      case IFSListAttrsRep.AS400_OBJECT:
         // AS/400 libraries and database files look like directories
         String nameUpper = name.toUpperCase();         // @C2a
         answer = (nameUpper.endsWith(".LIB") ||
                   nameUpper.endsWith(".FILE")); //B1C Changed path_ to name
                   //@C2c
                   //@B1D Removed code that checked for file separators
                   //|| path_.endsWith(".LIB" + IFSFile.separator) ||
                   //path_.endsWith(".FILE" + IFSFile.separator));
         break;
      default:
         answer = false;
    }
    return answer;
  }

  //@B1A Moved code from isFile() to support determining if a file is a file
  //without a call to the AS/400.
  /**
   Determines if a file is a file without a call to the AS/400.
   **/
  private boolean determineIsFile(IFSListAttrsRep attributeList)
  {
    boolean answer = false;
    // Determine if the file attributes indicate a file.
    // Don't need to check if attributeList == null because it has already been
    // checked by the two methods that call it.   Also don't check converter
    // because it is set by a connect() method before calling this.
    String name = fd_.converter_.byteArrayToString(attributeList.getName(/*fd_.serverDatastreamLevel_*/));
    switch(attributeList.getObjectType())
    {
      case IFSListAttrsRep.DIRECTORY:
      case IFSListAttrsRep.FILE:
         answer = ((attributeList.getFixedAttributes() & IFSListAttrsRep.FA_DIRECTORY) == 0);
         break;
      case IFSListAttrsRep.AS400_OBJECT:
         //AS/400 libraries and database files look like directories.
         String nameUpper = name.toUpperCase();         // @C2a
         answer = !(nameUpper.endsWith(".LIB") ||
                    nameUpper.endsWith(".FILE")); //B1C Changed path_ to name
                  //@C2c
                  //@B1D Removed code that checked for file separators
                  //|| path_.endsWith(".LIB" + IFSFile.separator) ||
                  //path_.endsWith(".FILE" + IFSFile.separator));
         break;
      default:
         answer = false;
    }
    return answer;
  }

  /**
   Determines if the integrated file system object represented by this object exists.
   **/
  public int exists()
    throws IOException, AS400SecurityException
  {
    return exists(fd_.path_);
  }


  //@B4a
  /**
   Determines if the integrated file system object represented by this object exists.
   **/
  private int exists(String name)
    throws IOException, AS400SecurityException
  {
    int returnCode = IFSReturnCodeRep.SUCCESS;

    // Ensure that we are connected to the server.
    fd_.connect();

    // @A8D if (attributesReply_ == null)
    // @A8D {
      returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
      // Attempt to list the attributes of the specified file.
      try
      {
        IFSListAttrsRep attrs = getAttributeSetFromServer(name);
        if (attrs != null)
        {
          returnCode = IFSReturnCodeRep.SUCCESS;
          attributesReply_ = attrs;
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
    return fd_.path_;
  }


  /**
   Get a list attribute reply from the server for a single entity (get the attributes
   of a specific object, not the attributes of every file in a directory).
  **/
  // @D1 - new method because of changes to java.io.file in Java 2.

  private IFSListAttrsRep getAttributeSetFromServer(String file)
    throws IOException, AS400SecurityException
  {
    IFSListAttrsRep reply = null;

    // Attempt to list the attributes of the specified file.
    Vector replys = listAttributes(file, -1, null, true);
    // Note: This does setFD() on each returned IFSListAttrsRep.

    // If this is a directory then there must be exactly one reply.
    if (replys != null && replys.size() == 1)
    {
      reply = (IFSListAttrsRep) replys.elementAt(0);
    }

    return reply;
  }


  // @B4a
  /**
   Returns the file's data CCSID.  Returns -1 if failure or if directory.
   **/
  public int getCCSID()
    throws IOException, AS400SecurityException
  {
    IFSListAttrsRep reply = listAttributes();
    if (reply != null)
    {
      //reply.setServerDatastreamLevel(fd_.serverDataStreamLevel_);  // @B6d
      //reply.setFD(fd_);                  // @B6a
      return reply.getCCSID(fd_.serverDatastreamLevel_);
    }
    else return -1;
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
      fd_.connect();
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
      ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      fd_.connectionDropped(e);
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


  // @B7a
  /**
   Returns the file's owner's "user ID" number.
   Returns -1 if error.
   **/
  public long getOwnerUID()
    throws IOException, AS400SecurityException        // @C0c
  {
    IFSListAttrsRep reply = listAttributes();
    if (reply != null)
    {
      // Note: No need to do a setFD(fd_) on the reply, since offset of "owner" field is consistent across the various OA2* structures.
      return reply.getOwnerUID();
    }
    else return -1L;        // @C0c
  }


  // @B5a
  // Returns zero-length string if the file has no subtype.
  public String getSubtype()
    throws IOException, AS400SecurityException
  {
    String subtype = "";

    // Ensure that we are connected to the server.
    fd_.connect();

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

    // Send the List Attributes request.
    byte[] extendedAttrName = fd_.converter_.stringToByteArray(".TYPE");
    IFSListAttrsReq req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,
                              IFSListAttrsReq.NO_AUTHORITY_REQUIRED, -1,
                              null, false, extendedAttrName, false);  // @C3c
    if (patternMatching_ != IFSFile.PATTERN_DEFAULT) req.setPatternMatching(patternMatching_);
    Vector replys = listAttributes(req);

    // Verify that we got at least one reply.
    if (replys == null) {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received null from listAttributes(req).");
    }
    else if (replys.size() == 0) {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received no replies from listAttributes(req).");
    }
    else
    {
      if (replys.size() > 1) {
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received multiple replies from listAttributes(req) (" +
                  replys.size() + ")");
      }
      IFSListAttrsRep reply = (IFSListAttrsRep)replys.elementAt(0);
      byte[] subtypeAsBytes = reply.getExtendedAttributeValue(/*fd_.serverDatastreamLevel_*/);
      if (subtypeAsBytes != null)
      {
        // Note: The EA value field is always returned in EBCDIC (ccsid=37).
        subtype = (new CharConverter(37)).byteArrayToString(subtypeAsBytes).trim();
      }
    }
    return subtype;
  }


  /**
   Determines if the integrated file system object represented by this object is a directory.
  **/
  public int isDirectory()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    if (attributesReply_ == null)
    {
      try
      {
        attributesReply_ = getAttributeSetFromServer(fd_.path_);
      }
      catch (AS400SecurityException e)
      {
        returnCode = IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY;
      }
    }

    //@B1D Moved this code to determineIsDirectory().
    //Determine if the file attributes indicate a directory.
    //if (reply != null)
    //{
    //  boolean answer = false;
    //  switch(reply.getObjectType())
    //  {
    //    case IFSListAttrsRep.DIRECTORY:
    //    case IFSListAttrsRep.FILE:
    //      answer = ((reply.getFixedAttributes() &
    //                 IFSListAttrsRep.FA_DIRECTORY) != 0);
    //      break;
    //    case IFSListAttrsRep.AS400_OBJECT:
    //      // AS/400 libraries and database files look like directories.
    //      answer = (path_.endsWith(".LIB") || path_.endsWith(".FILE") ||
    //                path_.endsWith(".LIB" + IFSFile.separator) ||
    //                path_.endsWith(".FILE" + IFSFile.separator));
    //      break;
    //    default:
    //      answer = false;
    //  }
    //@B1D Deleted during move for rework below.
    //  if (answer == true)
    //  {
    //   returnCode = IFSReturnCodeRep.SUCCESS;
    //  }
    //}

    //@B1A Added code to call determineIsDirectory().
    if (attributesReply_ != null)
    {
       if (determineIsDirectory(attributesReply_))
          returnCode = IFSReturnCodeRep.SUCCESS;
    }

    return returnCode;
  }


  /**
   Determines if the integrated file system object represented by this object is a "normal" file.<br>
   A file is "normal" if it is not a directory or a container of other objects.
   **/
  public int isFile()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    if (attributesReply_ == null)
    {
      try
      {
          attributesReply_ = getAttributeSetFromServer(fd_.path_);
      }
      catch (AS400SecurityException e)
      {
        returnCode = IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY;
      }
    }

    //@B1D Moved this code to determineIsFile().
    //Determine if the file attributes indicate a directory.
    //if (reply != null)
    //{
    //  boolean answer = false;
    //  switch(reply.getObjectType())
    //  {
    //    case IFSListAttrsRep.DIRECTORY:
    //    case IFSListAttrsRep.FILE:
    //      answer = ((reply.getFixedAttributes() &
    //                 IFSListAttrsRep.FA_DIRECTORY) == 0);
    //      break;
    //    case IFSListAttrsRep.AS400_OBJECT:
    //      // AS/400 libraries and database files look like directories.
    //      answer = !(path_.endsWith(".LIB") || path_.endsWith(".FILE") ||
    //                 path_.endsWith(".LIB" + IFSFile.separator) ||
    //                 path_.endsWith(".FILE" + IFSFile.separator));
    //      break;
    //    default:
    //      answer = false;
    //  }
    //@B1D Deleted this during move for rework below.
    //  if (answer == true)
    //  {
    //    returnCode = IFSReturnCodeRep.SUCCESS;
    //  }
    //}

    //@B1A Added code to call determineIsFile().
    if (attributesReply_ != null)
    {
       if (determineIsFile(attributesReply_))
          returnCode = IFSReturnCodeRep.SUCCESS;
    }

    return returnCode;
  }

  /**
   Determines if the integrated file system object represented by this
   object has its hidden attribute set.
  **/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean isHidden()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    boolean result = false;

    if (attributesReply_ == null)
    {
        // Attempt to get the attributes of this object.
        attributesReply_ = getAttributeSetFromServer(fd_.path_);
    }

    // Determine if the file attributes indicate hidden.
    if (attributesReply_ != null)
    {
      result = (attributesReply_.getFixedAttributes() & IFSListAttrsRep.FA_HIDDEN) != 0;
    }

    return result;
  }

  /**
   Determines if the integrated file system object represented by this
   object has its hidden attribute set.
  **/
  // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean isReadOnly()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    boolean result = false;

    if (attributesReply_ == null)
    {
        // Attempt to get the attributes of this object.
        attributesReply_ = getAttributeSetFromServer(fd_.path_);
    }

    // Determine if the file attributes indicate hidden.
    if (attributesReply_ != null)
    {
      result = (attributesReply_.getFixedAttributes() & IFSListAttrsRep.FA_READONLY) != 0;
    }

    return result;
  }


  /**
   Determines if the integrated file system object represented by this object is a symbolic link.
   **/
  public boolean isSymbolicLink()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    if (Trace.traceOn_ && fd_.getSystemVRM() < 0x00050300) {
      Trace.log(Trace.WARNING, "Server is V5R2 or lower, so isSymbolicLink() will always report false.");
      return false;
    }

    /* Temporary workaround, until better File Server support is in place.
     if (attributesReply_ != null)
     {
     System.out.println("DEBUG IFSFileImplRemote.isSymbolicLink(): attributesReply_ != null");
     result = attributesReply_.isSymbolicLink(fd_.serverDatastreamLevel_);
     }
     else
     */
    if (!determinedIsSymbolicLink_)
    {
      // Note: For now (V5R2 and the follow-on release), we can't get accurate symbolic link info by querying the attrs of a specific file.
      // Instead, we must query the contents of the parent directory.
      int pathLen = fd_.path_.length();
      if (pathLen <= 1) {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Path length is less than 2, so assuming not a symbolic link: " + fd_.path_);
        isSymbolicLink_ = false;
        determinedIsSymbolicLink_ = true;
      }
      else
      {
        // Do a wildcard search.
        StringBuffer wildCardPatternBuf = new StringBuffer(fd_.path_);
        wildCardPatternBuf.setCharAt(pathLen-1, '*');
        String wildCardPattern = wildCardPatternBuf.toString();
        String dirPath = wildCardPattern.substring(0,1+wildCardPattern.lastIndexOf('/'));

        byte[] pathBytes = fd_.converter_.stringToByteArray(wildCardPattern);
        IFSCachedAttributes[] attrList = listDirectoryDetails(wildCardPattern, dirPath, -1, pathBytes, true);

        IFSCachedAttributes attrs = null;
        String filename = fd_.path_.substring(1+(fd_.path_.lastIndexOf('/')));
        for (int i=0; attrs == null && i<attrList.length; i++)
        {
          // Note: No need to compare full pathnames, since we know the directory.
          if (attrList[i].name_.equals(filename)) {
            attrs = attrList[i];
          }
        }
        if (attrs != null)
        {
          isSymbolicLink_ = attrs.isSymbolicLink_;
          determinedIsSymbolicLink_ = true;
        }
        else
        {
          if (Trace.traceOn_) Trace.log(Trace.ERROR, "Received zero matches from listDirectoryDetails() against parent of " + wildCardPattern.toString());
          isSymbolicLink_ = false;
          determinedIsSymbolicLink_ = true;
        }
      }
    }

    return isSymbolicLink_;
  }




  /**
   @D3a lastAccessed0 is a new method
   Determines the time that the integrated file system object represented by this object was last accessed.
   **/
  public long lastAccessed()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    long accessDate = 0L;


    // Attempt to list the attributes of the specified file.
    // Note: Do not use cached attributes, since they may be out of date.
    IFSListAttrsRep attrs = getAttributeSetFromServer(fd_.path_);
    if (attrs != null)
    {
      attributesReply_ = attrs;
      accessDate = attrs.getAccessDate();
    }

    return accessDate;
  }


  /**
   Determines the time that the integrated file system object represented by this object was last modified.
   **/
  public long lastModified()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    long modificationDate = 0L;


    // Attempt to list the attributes of the specified file.
    // Note: Do not use cached attributes, since they may be out of date.
    IFSListAttrsRep attrs = getAttributeSetFromServer(fd_.path_);
    if (attrs != null)
    {
      attributesReply_ = attrs;
      modificationDate = attrs.getModificationDate();
    }

    return modificationDate;
  }


  /**
   Determines the length of the integrated file system object represented by this object.
   **/
  public long length()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    long size = 0L;

    if (fd_.getSystemVRM() >> 8 != 0x00000502)  // system is other than V5R2   @C1c
    {
      // Attempt to list the attributes of the specified file.
      // Note: Do not use cached attributes, since they may be out of date.
      IFSListAttrsRep attrs = getAttributeSetFromServer(fd_.path_);

      if (attrs != null)
      {
        attributesReply_ = attrs;
        size = attrs.getSize(fd_.serverDatastreamLevel_);
      }
    }
    else  // the system is V5R2                         @C1a - added this entire 'else' block
    {
      // Convert the path name to the AS/400 CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      // Send the List Attributes request.  Indicate that we want the "8-byte file size".
      IFSListAttrsReq req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,
                                                IFSListAttrsReq.NO_AUTHORITY_REQUIRED, -1,
                                                null, true, null, true);  // @C3c
      if (patternMatching_ != IFSFile.PATTERN_DEFAULT) req.setPatternMatching(patternMatching_);
      Vector replys = listAttributes(req);

      if (replys == null) {
        if (errorRC_ != 0) {
          throw new ExtendedIOException(errorRC_);
        }
        else throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }
      else if (replys.size() == 0) {
        // Assume this simply indicates that the file does not exist.
        if (Trace.traceOn_) {
          Trace.log(Trace.WARNING, "Received no replies from listAttributes(req).");
        }
      }
      else
      {
        if ( replys.size() > 1 &&
             Trace.traceOn_ )
        {
            Trace.log(Trace.WARNING, "Received multiple replies from listAttributes(req) (" +
                      replys.size() + ")");
        }
        IFSListAttrsRep reply = (IFSListAttrsRep)replys.elementAt(0); // use first reply
        size = reply.getSize8Bytes(/*fd_.serverDatastreamLevel_*/);
      }
    }
    return size;
  }


  //@B4a
  // Fetch list attributes reply(s) for the specified file handle.
  private Vector listAttributes(int fileHandle)
      throws IOException, AS400SecurityException
  {
    // Assume connect() has already been done.

    // Process attribute replies.
    IFSListAttrsReq req = new IFSListAttrsReq(fileHandle, (short)0x44);  // Object Attribute 2
    if (patternMatching_ != IFSFile.PATTERN_DEFAULT) req.setPatternMatching(patternMatching_);

    return listAttributes(req);
  }


  // Fetch list attributes reply(s) for the specified path.
  private Vector listAttributes(String path, int maxGetCount, byte[] restartNameOrID, boolean isRestartName)           // @D4C @C3c
    throws IOException, AS400SecurityException
  {
    // Assume connect() has already been done.

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = fd_.converter_.stringToByteArray(path);

    // Process attribute replies.
    IFSListAttrsReq req;                                                                            // @D4C
    if (maxGetCount < 0) {   // a Get Count of -1 means "return all entries"              // @D4A
      req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_);                             // @D4C
      if (patternMatching_ != IFSFile.PATTERN_DEFAULT) req.setPatternMatching(patternMatching_);
    }
    else {                                                                                          // @D4A
//@C3d  byte[] restartNameAsBytes = null;                                                           // @D4A
//@C3d  if (restartName != null)                                                                    // @D4A
//@C3d      restartNameAsBytes = fd_.converter_.stringToByteArray(restartName);                     // @D4A

        req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,                              // @D4A
                                  IFSListAttrsReq.NO_AUTHORITY_REQUIRED, maxGetCount,               // @D4A
                                  restartNameOrID,                                       // @D4A @C3c
                                  isRestartName, // @C3a
                                  null, false);  // @B5a @C3c
        if (patternMatching_ != IFSFile.PATTERN_DEFAULT) req.setPatternMatching(patternMatching_);
    }
    return listAttributes(req);  // Note: This does setFD() on each returned IFSListAttrsRep..
  }


  //@B4c
  // Submit the specified request, and fetch list attributes reply(s).
  // The returned Vector contains IFSListAttrsRep objects.
  private Vector listAttributes(IFSListAttrsReq req)
    throws IOException, AS400SecurityException
  {
    Vector replys = new Vector(256);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      fd_.connectionDropped(e);
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
          Trace.log(Trace.ERROR, "Error getting file attributes: " +
                    "IFSReturnCodeRep return code = ", rc);
          throw new AS400SecurityException(AS400SecurityException.DIRECTORY_ENTRY_ACCESS_DENIED);
        }

        if (rc != IFSReturnCodeRep.SUCCESS &&                               // @D4A
            rc != IFSReturnCodeRep.NO_MORE_FILES &&
            rc != IFSReturnCodeRep.FILE_NOT_FOUND &&
            rc != IFSReturnCodeRep.PATH_NOT_FOUND)
        {
          Trace.log(Trace.ERROR, "Error getting file attributes: " +
                    "IFSReturnCodeRep return code = ", rc);  // @A9C
          throw new ExtendedIOException(rc);
        }

      }
      else
      {
        // Unknown data stream.
        Trace.log(Trace.ERROR, "Unknown reply data stream: ", ds.data_);  // @A9C
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
          ds = (ClientAccessDataStream) fd_.server_.receive(req.getCorrelation());
        }
        catch(ConnectionDroppedException e)
        {
          Trace.log(Trace.ERROR, "Byte stream server connection lost.");
          fd_.connectionDropped(e);
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
    errorRC_ = 0;
    if (rc == IFSReturnCodeRep.PATH_NOT_FOUND) {        // @A1A
        // If the directory or file does not exist, then return NULL.
        errorRC_ = rc;
        replys = null;                                  // @A1A
    }                                                   // @A1A
    else {                                              // @A1A
        // Set the vector capacity to the current size.
        replys.trimToSize();
    }                                                   // @A1A

    return replys;
  }

  // @B7a - This code was formerly located in getCcsid().
  // Open the file, list the file attributes, and close the file.
  // May return null, for example if the file is a directory.
  private IFSListAttrsRep listAttributes()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    // Do an open, and get a file handle.
    // Note: In order to get an OA2 structure back from the
    // "List File Attributes" request, we must specify the file
    // by handle rather than by name.

    // Convert the path name to the AS/400 CCSID.
    byte[] pathname = fd_.getConverter().stringToByteArray(fd_.path_);

    // Request that the file can be opened.
    IFSOpenReq req = new IFSOpenReq(pathname, fd_.preferredServerCCSID_,
                                    fd_.preferredServerCCSID_,
                                    IFSOpenReq.READ_ACCESS,
                                    IFSOpenReq.DENY_NONE,
                                    IFSOpenReq.NO_CONVERSION, 8);
    ClientAccessDataStream ds = null;
    try
    {
      ds = (ClientAccessDataStream) fd_.getServer().sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      fd_.connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted", e);
      throw new InterruptedIOException(e.getMessage());
    }

    // Verify that the open request was successful.
    int fileHandle = -1;
    errorRC_ = 0;
    if (ds instanceof IFSOpenRep)
    {
      fileHandle = ((IFSOpenRep)ds).getFileHandle();
      fd_.setOpen(true, fileHandle);                    // @B8c
    }
    else if (ds instanceof IFSReturnCodeRep)
    {
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (Trace.traceOn_) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
      if (rc == IFSReturnCodeRep.DUPLICATE_DIR_ENTRY_NAME) {
        // We get that RC if it's a directory.
        errorRC_ = rc;
        return null;       // @B7c
      }
      else {
        throw new ExtendedIOException(rc);
      }
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

    // Do a list attributes, specifying the handle, and indicating that we
    // want an OA2 structure in the reply.

    IFSListAttrsRep reply = null;
    Vector replys = listAttributes(fileHandle);

    // Verify that we got exactly one reply.
    if (replys == null) {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received null from listAttributes(fileHandle).");
    }
    else if (replys.size() == 0) {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received no replies from listAttributes(fileHandle).");
    }
    else if (replys.size() > 1) {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received multiple replies from listAttributes(fileHandle) (" +
                replys.size() + ")");
    }
    else
    {
      reply = (IFSListAttrsRep) replys.elementAt(0);
    }

    fd_.close0();  // B8c

    return reply;
  }


  // @A7A
  // List the files/directories in the specified directory.
  // Returns null if specified file or directory does not exist.
  public String[] listDirectoryContents(String directoryPath)
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    Vector replys = listAttributes(directoryPath, -1, null, true);
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
        String name = fd_.converter_.byteArrayToString(reply.getName(/*dsl*/));
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


  // @B1A Added this function to support caching attributes.
  // @C3C Morphed this method by adding a parameter and making it private.
  // List the files/directories details in the specified directory.
  // Returns null if specified file or directory does not exist.
  private IFSCachedAttributes[] listDirectoryDetails(String pathPattern,
                                                     String directoryPath,
                                                    int maxGetCount,            // @D4A
                                                    byte[] restartNameOrID,     // @C3C
                                                    boolean isRestartName)      // @C3A
     throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();
    IFSCachedAttributes[] fileAttributes = null;

    try
    {
      Vector replys = listAttributes(pathPattern, maxGetCount, restartNameOrID, isRestartName);

      // Add each file or directory in the specified directory,
      // to the array of files.

      int j = 0;
      if (replys != null)
      {
        fileAttributes = new IFSCachedAttributes[replys.size()];
        int dsl = fd_.serverDatastreamLevel_;
        for (int i = 0; i < replys.size(); i++)
        {
          IFSListAttrsRep reply = (IFSListAttrsRep) replys.elementAt(i);
          String name = fd_.converter_.byteArrayToString(reply.getName(/*dsl*/));
          if (!(name.equals(".") || name.equals("..")))
          {
             // isDirectory and isFile should be different unless the
             // file is a invalid symbolic link (circular or points
             // to a non-existant object).  This kind of link cannot
             // be resolved and both determineIsDirectory and
             // determineIsFile will return false.  Regular symbolic links
             // will resolve.  For example, a symbolic link to a file will return
             // true from isFile and false from determineIsDirectory.
             boolean isDirectory = determineIsDirectory(reply);
             boolean isFile = determineIsFile(reply);
             IFSCachedAttributes attributes = new IFSCachedAttributes(reply.getAccessDate(),
                 reply.getCreationDate(), reply.getFixedAttributes(), reply.getModificationDate(),
                 reply.getObjectType(), reply.getSize(dsl), name, directoryPath, isDirectory, isFile, reply.getRestartID(), reply.isSymbolicLink(dsl)); //@B3A @C3C
             fileAttributes[j++] = attributes;
           }
        }
      }//end if

      if (j == 0)
      {
        fileAttributes = new IFSCachedAttributes[0];    //@B3C
      }
      else if (fileAttributes.length != j)
      {
        //Copy the attributes to an array of the exact size.
        IFSCachedAttributes[] newFileAttributes = new IFSCachedAttributes[j];   //@B3C
        System.arraycopy(fileAttributes, 0, newFileAttributes, 0, j);    //@B3C
        fileAttributes = newFileAttributes;
      }
    }
    catch (AS400SecurityException e)
    {
      fileAttributes = null;
    }
    return fileAttributes;
  }


  // @B1A Added this function to support caching attributes.
  // @C3c Moved logic from this method into new private method.
  // List the files/directories details in the specified directory.
  // Returns null if specified file or directory does not exist.
  public IFSCachedAttributes[] listDirectoryDetails(String pathPattern,
                                                    String directoryPath,
                                                    int maxGetCount,            // @D4A
                                                    String restartName)         // @D4A
     throws IOException, AS400SecurityException
  {
    byte[] restartNameBytes = fd_.converter_.stringToByteArray(restartName);                     // @C3M
    return listDirectoryDetails(pathPattern, directoryPath, maxGetCount, restartNameBytes, true);
  }

  // @C3a
  // List the files/directories details in the specified directory.
  // Returns null if specified file or directory does not exist.
  public IFSCachedAttributes[] listDirectoryDetails(String pathPattern,
                                                    String directoryPath,
                                                    int maxGetCount,
                                                    byte[] restartID)
     throws IOException, AS400SecurityException
  {
    return listDirectoryDetails(pathPattern, directoryPath, maxGetCount, restartID, false);
  }



  /**
   Creates an integrated file system directory whose path name is specified by this object.
   **/
  public int mkdir(String directory)
    throws IOException, AS400SecurityException
  {
    // Ensure that the path name is set.
    fd_.connect();

    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    try
    {
      // Convert the directory name to the AS/400 CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(directory);

      // Send a create directory request.
      IFSCreateDirReq req = new IFSCreateDirReq(pathname,
                                                fd_.preferredServerCCSID_);
      ClientAccessDataStream ds =
        (ClientAccessDataStream) fd_.server_.sendAndReceive(req);

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
      fd_.connectionDropped(e);
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
  public int mkdirs()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    // Traverse up the parent directories until the first parent
    // directory that exists is found, saving each parent directory
    // as we go.
    boolean success = false;
    Vector nonexistentDirs = new Vector();
    String directory = fd_.path_;
    int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

    returnCode = exists(directory);
    if (returnCode != IFSReturnCodeRep.SUCCESS)
    {
      do
      {
        nonexistentDirs.addElement(directory);
        directory = IFSFile.getParent(directory);
      }
      while ((directory != null) && (exists(directory) != IFSReturnCodeRep.SUCCESS));
    } else
    {
     returnCode = IFSReturnCodeRep.DUPLICATE_DIR_ENTRY_NAME;
    }

    // Create each parent directory in the reverse order that
    // they were saved.
    for (int i = nonexistentDirs.size(); i > 0; i--)
    {
      // Get the name of the next directory to create.
      //try                                                                  //@B1D
      //{                                                                    //@B1D
        directory = (String) nonexistentDirs.elementAt(i - 1);
      //}                                                                    //@B1D
      //catch(Exception e)                                                   //@B1D
      //{                                                                    //@B1D
      //  Trace.log(Trace.ERROR, "Error fetching element from vector.\n" +   //@B1D
      //            "length = " + nonexistentDirs.size() + " index = ",      //@B1D
      //            i - 1);
      //  throw new InternalErrorException(InternalErrorException.UNKNOWN);  //@B1D
      //}

      // Create the next directory.
      returnCode = mkdir(directory);
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
  public int renameTo(IFSFileImpl file)
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    // Assume the argument has been validated by the public class.

    // Rename the file.
    boolean success = false;
    ClientAccessDataStream ds = null;
    IFSFileImplRemote otherFile = (IFSFileImplRemote)file;
    try
    {
      // Convert the path names to the AS/400 CCSID.
      byte[] oldName = fd_.converter_.stringToByteArray(fd_.path_);
      byte[] newName = fd_.converter_.stringToByteArray(otherFile.getAbsolutePath());

      // Issue a rename request.
      IFSRenameReq req = new IFSRenameReq(oldName, newName,
                                          fd_.preferredServerCCSID_,
                                          false);
      ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      fd_.connectionDropped(e);
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
      if (returnCode == IFSReturnCodeRep.SUCCESS)
        success = true;
      else
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error renaming file: " +
                    "IFSReturnCodeRep return code = ", returnCode);
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
      fd_.path_ = otherFile.getAbsolutePath();

      // Clear any cached attributes.
      attributesReply_ = null;
    }

    return returnCode;
  }


  /**
   Sets the file's data CCSID.
   **/
  public boolean setCCSID(int ccsid)
    throws IOException, AS400SecurityException
  {
    // To change the file data CCSID, we need to get the file's current attributes (in an OA2* structure), reset the CCSID field, and then send back the modified OA2* struct in a Change Attributes request.

    IFSListAttrsRep reply = listAttributes();  // get current attributes
    if (reply == null) {
      if (errorRC_ != 0) {
        throw new ExtendedIOException(errorRC_);
      }
      else throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
    boolean success = false;
    byte[] oaStruct = reply.getOA(); // get the OA2* structure

    // Sanity-check the length: If it's an OA2a or OA2b, the length will be 150 bytes (144 plus a 6-byte LLCP).  If it's an OA2c, the length will be 166 bytes (160 plus LLCP).
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Length of returned OA2* structure (should be 150 or 166): " + oaStruct.length);

    // Reset the 2-byte "CCSID" field in the OA structure.
    byte[] ccsidBytes = BinaryConverter.shortToByteArray((short)ccsid);
    int ccsidOffsetInStruct = reply.getCCSIDOffset(fd_.serverDatastreamLevel_);
    System.arraycopy(ccsidBytes, 0, oaStruct, ccsidOffsetInStruct, 2);

    // Issue a change attributes request.
    ClientAccessDataStream ds = null;
    try
    {
      // Convert the path name to the AS/400 CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                    fd_.preferredServerCCSID_,
                                                    oaStruct);
      ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      fd_.connectionDropped(e);
    }
    catch(InterruptedException e)
    {
      Trace.log(Trace.ERROR, "Interrupted");
      throw new InterruptedIOException(e.getMessage());
    }

    if (ds instanceof IFSReturnCodeRep)
    {
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc == IFSReturnCodeRep.SUCCESS)
        success = true;
      else
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error setting file data CCSID: " +
                                      "IFSReturnCodeRep return code = ", rc);
    }
    else
    {
      // Unknown data stream.
      Trace.log(Trace.ERROR, "Unknown reply data stream ", ds.data_);
      throw new
        InternalErrorException(Integer.toHexString(ds.getReqRepID()),
                               InternalErrorException.DATA_STREAM_UNKNOWN);
    }

    return success;
  }



  /**
   Changes the fixed attributes (read only, hidden, etc.) of the integrated
   file system object represented by this object to <i>attributes</i>.

   @param attributes The set of attributes to apply to the object.  Note
                     these attributes are not ORed with the existing
                     attributes.  They replace the existing fixed
                     attributes of the file.
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   **/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean setFixedAttributes(int attributes)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    try
    {
      fd_.connect();
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
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                    fd_.preferredServerCCSID_,
                                                    attributes,
                                                    true);
      ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
    }
    catch(ConnectionDroppedException e)
    {
      Trace.log(Trace.ERROR, "Byte stream server connection lost.");
      fd_.connectionDropped(e);
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
      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc == IFSReturnCodeRep.SUCCESS)
        success = true;
      else
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error setting file attributes: " +
                  "IFSReturnCodeRep return code = ", rc);
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
    attributesReply_ = null;

    return success;
  }



  /**
   Alters the hidden attribute of the object.  If <i>attribute</i>
   is true, the bit is turned on.  If <i>attribute</i> is turned off,
   the bit is turned off.

   @param attributes The new state of the hidden attribute.  The hidden
                     attribute is the second bit from the right.

   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   **/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean setHidden(boolean attribute)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    try
    {
      fd_.connect();
    }
    catch (AS400SecurityException e)
    {
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = false;
    IFSListAttrsRep attributes = null;

    // Setting the fixed attributes of a file involves
    // replacing the current set of attributes.  The first
    // set is to get the current set.
    try
    {
       attributes = getAttributeSetFromServer(fd_.path_);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, "Failed to get attribute set", e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    if (attributes != null)
    {
       // Now that we have the current set of attributes, figure
       // out if the bit is currently on.
       int currentFixedAttributes = attributes.getFixedAttributes();
       boolean currentHiddenBit = (currentFixedAttributes & 2) != 0;

       // If current does not match what the user wants we need to go
       // to the as/400 to fix it.
       if (currentHiddenBit != attribute)
       {
          int newAttributes;

          // If the user wants hidden on set the bit else the
          // user wants the bit off so clear it.
          if (attribute)
             newAttributes = currentFixedAttributes + 2;
          else
             newAttributes = currentFixedAttributes & 0x7ffffffd;

              // Issue a change attributes request.
          ClientAccessDataStream ds = null;
          try
          {
              // Convert the path name to the AS/400 CCSID.
              byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

              IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                            fd_.preferredServerCCSID_,
                                                            newAttributes,
                                                            true);
              ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
          }
          catch(ConnectionDroppedException e)
          {
             Trace.log(Trace.ERROR, "Byte stream server connection lost.");
             fd_.connectionDropped(e);
          }
          catch(InterruptedException e)
          {
             Trace.log(Trace.ERROR, "Interrupted");
             throw new InterruptedIOException(e.getMessage());
          }

          if (ds instanceof IFSReturnCodeRep)
          {
             int rc = ((IFSReturnCodeRep) ds).getReturnCode();
             if (rc == IFSReturnCodeRep.SUCCESS)
               success = true;
             else
               if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error setting hidden attribute: " +
                         "IFSReturnCodeRep return code = ", rc);
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
          attributesReply_ = null;

       }
       else
          success = true;
    }
    return success;
  }


  /**
   Sets the pattern-matching behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is PATTERN_POSIX.
   @param patternMatching Either {@link IFSFile#PATTERN_POSIX PATTERN_POSIX}, {@link IFSFile#PATTERN_POSIX_ALL PATTERN_POSIX_ALL}, or {@link IFSFile#PATTERN_OS2 PATTERN_OS2}
   **/
  public void setPatternMatching(int patternMatching)
  {
    patternMatching_ = patternMatching;
  }


  // @B8c
  /**
   Changes the last modified time of the integrated file system object represented by this object to <i>time</i>.
   @param time The desired last modification time (measured in milliseconds
   since January 1, 1970 00:00:00 GMT), or -1 to set the last modification time to the current system time.

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

    boolean fileIsEmpty = false;  // @B8a

    // Ensure that we are connected to the server.
    try
    {
      fd_.connect();
      // If we're setting timestamp to "current system time", we'll need to know how big the file is.
      if (time == -1) fileIsEmpty = (length()==0 ? true : false);  // @B8a

    }
    catch (AS400SecurityException e)
    {
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = false;

    if (time != -1)  // @B8a
    {
      // Issue a change attributes request.
      ClientAccessDataStream ds = null;
      try
      {
        // Convert the path name to the AS/400 CCSID.
        byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

        IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                      fd_.preferredServerCCSID_,
                                                      0, time, 0);
        ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
        Trace.log(Trace.ERROR, "Byte stream server connection lost.");
        fd_.connectionDropped(e);
      }
      catch(InterruptedException e)
      {
        Trace.log(Trace.ERROR, "Interrupted");
        throw new InterruptedIOException(e.getMessage());
      }

      // Verify the reply.
      if (ds instanceof IFSReturnCodeRep)
      {
        int rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc == IFSReturnCodeRep.SUCCESS)
          success = true;
        else
          if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error setting last-modified date: " +
                    "IFSReturnCodeRep return code = ", rc);
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
    else  // we are setting modification time to "current system time"
    {
      // Open the file for read/write, setting the file handle in fd_.
      int rc = fd_.checkAccess(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN, true);  // leave the file open
      if (rc != IFSReturnCodeRep.SUCCESS)
      {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to open file: " +
                  "IFSReturnCodeRep return code = ", rc);
        return false;
      }

      byte[] buffer = new byte[1];  // buffer for reading/writing a single byte

      if (fileIsEmpty)
      {
        // Update last-modification date by writing one byte (the value doesn't matter).
        fd_.writeBytes(buffer, 0, 1, true);

        // Reset the file size to zero.
        success = fd_.setLength(0);
      }
      else // the file is not empty
      {
        // Read the first byte.
        if (1 == fd_.read(buffer, 0, 1))
        {
          // Write back the first byte.
          fd_.setFileOffset(0);
          fd_.writeBytes(buffer, 0, 1, true);
          success = true;
        }
        else
        {
          if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read first byte of file.");
          success = false;
        }
      }

      fd_.close0();
    }

    // Clear any cached attributes.
    attributesReply_ = null;

    return success;
  }


  // @B8a
  /**
   Sets the length of the integrated file system object represented by this object.  The file can be made larger or smaller.  If the file is made larger, the contents of the new bytes of the file are undetermined.
   @param length The new length, in bytes.
   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   **/
  public boolean setLength(int length)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    try
    {
      fd_.connect();
    }
    catch (AS400SecurityException e)
    {
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Clear any cached attributes.
    attributesReply_ = null;

    return fd_.setLength(length);
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
    if (fd_.server_ != null)
    {
      throw new ExtendedIllegalStateException("path",
                               ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    // If the specified path doesn't start with the separator character,
    // add one.  All paths are absolute for IFS.
    String newPath;
    if (path.length() == 0 || path.charAt(0) != IFSFile.separatorChar)
    {
      newPath = IFSFile.separator + path;
    }
    else
    {
      newPath = path;
    }

    // Update the path value.
    fd_.path_ = newPath;
  }


  /**
   Alters the read only attribute of the object.  If <i>attribute</i>
   is true, the bit is turned on.  If <i>attribute</i> is turned off,
   the bit is turned off.

   @param attributes The new state of the read only attribute

   @return true if successful; false otherwise.

   @exception ConnectionDroppedException If the connection is dropped unexpectedly.
   @exception ExtendedIOException If an error occurs while communicating with the AS/400.
   @exception InterruptedIOException If this thread is interrupted.
   @exception ServerStartupException If the AS/400 server cannot be started.
   @exception UnknownHostException If the AS/400 system cannot be located.
   **/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean setReadOnly(boolean attribute)
    throws IOException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    try
    {
      fd_.connect();
    }
    catch (AS400SecurityException e)
    {
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    boolean success = false;
    IFSListAttrsRep attributes = null;

    try
    {
       attributes = getAttributeSetFromServer(fd_.path_);
    }
    catch (AS400SecurityException e)
    {
      Trace.log(Trace.ERROR, "Failed to get attribute set", e);
      throw new ExtendedIOException(ExtendedIOException.ACCESS_DENIED);
    }

    // Same as setHidden -- setting fixed attributes is a total replacement
    // of the fixed attributes.  So, we have to get the current set, fix up
    // the readonly bit, then put them back.
    if (attributes != null)
    {
       int currentFixedAttributes = attributes.getFixedAttributes();
       boolean currentReadOnlyBit = (currentFixedAttributes & 1) != 0;

       // If the bit is not currently set to what the user wants.
       if (currentReadOnlyBit != attribute)
       {
          int newAttributes;

          // If the user wants readonly on, add to the current set.
          // If the user wants it off, clear the bit.
          if (attribute)
             newAttributes = currentFixedAttributes + 1;
          else
             newAttributes = currentFixedAttributes & 0x7ffffffe;

              // Issue a change attributes request.
          ClientAccessDataStream ds = null;
          try
          {
              // Convert the path name to the AS/400 CCSID.
              byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

              IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                            fd_.preferredServerCCSID_,
                                                            newAttributes,
                                                            true);
              ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
          }
          catch(ConnectionDroppedException e)
          {
             Trace.log(Trace.ERROR, "Byte stream server connection lost.");
             fd_.connectionDropped(e);
          }
          catch(InterruptedException e)
          {
             Trace.log(Trace.ERROR, "Interrupted");
             throw new InterruptedIOException(e.getMessage());
          }

          if (ds instanceof IFSReturnCodeRep)
          {
             int rc = ((IFSReturnCodeRep) ds).getReturnCode();
             if (rc == IFSReturnCodeRep.SUCCESS)
               success = true;
             else
               if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error setting read-only attribute: " +
                         "IFSReturnCodeRep return code = ", rc);
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
          attributesReply_ = null;

       }
       else
          success = true;
    }
    return success;
  }




  /**
   Sets the system.
   @param system The AS/400 system object.
   **/
  public void setSystem(AS400Impl system)
  {
    // Assume the argument has been validated by the public class.

    fd_.system_ = (AS400ImplRemote)system;
  }

}
