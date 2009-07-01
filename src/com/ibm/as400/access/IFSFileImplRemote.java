///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2007 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @D7 - 07/25/2007 - Add allowSortedRequests to the listDirectoryDetails()
//                    method to resolve problem of issuing PWFS List Attributes 
//                    request with both "Sort" indication and "RestartByID" 
//                    which is documented to be an invalid combination.
// @D8 - 10/04/2007 - Remove obsolete code for determining whether QSYS objects
//                    are to be treated as a "directory" or a "file".
//                    For QSYS objects, those which may be treated as directories
//                    have the attribute IFSListAttrsRep.DIRECTORY.
//                    For QSYS objects, those which may be treated as files
//                    have the attribute IFSListAttrsRep.FILE.
//                    All other QSYS objects are neither dirs/files (e.g OUTQ, 
//                    DSPF, TAPF, or PRTF objects).
// @D9 - 04/03/2008 - Add clearCachedAttributes() to clear impl cache attributes. 
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InterruptedIOException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.beans.PropertyVetoException;

/**
 Provides a full remote implementation for the IFSFile class.
 **/
class IFSFileImplRemote
implements IFSFileImpl
{
  // Used for debugging only.  This should always be false for production.
  // When this is false, all debug code will theoretically compile out.
  private static final boolean DEBUG = false;  // @B2A

  private static final boolean IS_RESTART_NAME = true;  // mnemonic for use in argument lists
  private static final boolean NO_RESTART_NAME = IS_RESTART_NAME; // mnemonic
  private static final boolean SORT_LIST = true;  // mnemonic
  private static final int     NO_MAX_GET_COUNT = -1;  // mnemonic
  private static final int     UNINITIALIZED = -1;

  // Constants for QlgAccess(), from system definitions file "unistd.h"
  //private static final int ACCESS_MODE_READ    = 0x04;  // R_OK: test for read permission
  //private static final int ACCESS_MODE_WRITE   = 0x02;  // W_OK: test for write permission
  private static final int ACCESS_MODE_EXECUTE = 0x01;  // X_OK: test for execute permission
  //private static final int ACCESS_MODE_EXISTS  = 0x00;  // F_OK: test for existence of file

  // Static initialization code.
  static
  {
    // Add all byte stream reply data streams of interest to the
    // server's reply data stream hash table.
    AS400Server.addReplyStream(new IFSListAttrsRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSOpenRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSCreateDirHandleRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSQuerySpaceRep(), AS400.FILE);
    AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
  }

  transient private IFSListAttrsRep attributesReply_; // "list attributes" reply

  private IFSFileDescriptorImplRemote fd_ = new IFSFileDescriptorImplRemote(); // @B2A

  private boolean isSymbolicLink_;
  private boolean determinedIsSymbolicLink_;
  private boolean sortLists_;  // whether file-lists are returned from the File Server in sorted order
  private RemoteCommandImpl servicePgm_;  // Impl object for remote command host server.


  /**
   Determines if the application can execute the integrated file system object represented by this object.  If the file does not exist, returns false.
   **/
  public boolean canExecute()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    return canAccess(ACCESS_MODE_EXECUTE);
  }


  /**
   Determines if the applet or application can read from the integrated file system object represented by this object.
   **/
  public boolean canRead()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    int rc = fd_.checkAccess(IFSOpenReq.READ_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN);
    return (rc == IFSReturnCodeRep.SUCCESS);
    // Design note: The QlgAccess() API gives somewhat different results in certain scenarios.
    // Using IFSOpenReq appears to be a bit more "thorough".
  }


  /**
   Determines if the applet or application can write to the integrated file system object represented by this object.
   **/
  public boolean canWrite()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    int rc = fd_.checkAccess(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN);
    return (rc == IFSReturnCodeRep.SUCCESS);
    // Design note: The QlgAccess() API gives somewhat different results in certain scenarios.
    // Using IFSOpenReq appears to be a bit more "thorough".
  }


  /**
   Calls QlgAccess() to determine whether the current user can access the file in the specified mode.
   If the file does not exist, returns false.
   Note: The QlgAccess API was introduced in V5R1.
   **/
  private boolean canAccess(int accessMode)
    throws IOException, AS400SecurityException
  {
    // Assume that the caller has already connected to the server.

    if (fd_.getSystemVRM() < 0x00050100)
    {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Server is pre-V5R1, so canAccess() is returning false.");
      return false;
    }

    // We will call the QlgAccess API, to determine whether the current user can access the file in the specified mode.
    // Note: According to the spec for QlgAccess: "If the [user profile] has *ALLOBJ special authority, access() will indicate success for R_OK, W_OK, or X_OK even if none of the permission bits are set."
    // Note: QlgAccess() is only _conditionally_ threadsafe.

    boolean result;
    try
    {
      // Create the pgm call object
      if (servicePgm_ == null) {
        setupServiceProgram();
      }

      ProgramParameter[] parameters = new ProgramParameter[]
      {
        // Parameter 1: Qlg_Path_Name_T *Path_Name (input) :
        new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
        // Parameter 2: int amode (input) :
        new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(accessMode))
      };

      // Call the service program.
      byte[] returnedBytes = servicePgm_.runServiceProgram("QSYS", "QP0LLIB1", "QlgAccess", parameters);
      if (returnedBytes == null)
      {
        Trace.log(Trace.ERROR, "Call to QlgAccess() returned null.");
        throw new AS400Exception(servicePgm_.getMessageList());
      }

      int returnValue = BinaryConverter.byteArrayToInt(returnedBytes, 0);

      switch (returnValue)
      {
        case -1:  // this indicates that we got an "errno" back
          {
            result = false;
            int errno = BinaryConverter.byteArrayToInt(returnedBytes, 4);
            switch (errno)
            {
              case 3025:  // ENOENT: "No such path or directory."
                // Assume that we got this error because the file isn't a symlink.
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgAccess() for file " + fd_.path_ + ". Assuming that the file does not exist.");
                break;
              case 3401:  // EACCES: "Permission denied."
                // Assume that we got this error because we don't have the specified access.
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgAccess() for file " + fd_.path_ + ". Assuming that the user does not have the specified access.");
                break;
              default:    // some other errno
                Trace.log(Trace.ERROR, "Received errno "+errno+" from QlgAccess() for file " + fd_.path_);
                throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR, errno);
                // Note: An ErrnoException might be more appropriate, but the ErrnoException constructor requires an AS400 object, and we don't have one to give it.
            }
            break;
          }

        case 0:  // the call to QlgAccess() was successful.
          {
            result = true;
            break;
          }
        default:  // This should never happen. The API spec says it only returns 0 or -1.
          {
            Trace.log(Trace.ERROR, "Received unexpected return value " + returnValue + " from QlgAccess() for file " + fd_.path_);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE, "QlgAccess()", returnValue);
          }
      }
    }
    catch (AS400SecurityException e) {
      throw e;
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      Trace.log(Trace.ERROR, "Error while determining accessibility of file.", e);
      throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
    }

    return result;
  }


  /**
   Calls QlgChmod() to reset the access mode for the file.
   // If the file does not exist, returns false.
   Note: The QlgChmod API was introduced in V5R1.
   **/
  public boolean setAccess(int accessType, boolean enableAccess, boolean ownerOnly)
    throws IOException, AS400SecurityException
  {
    // Assume that the caller has already connected to the server.

    // We will call the QlgChmod API, to determine whether the current user can access the file in the specified mode.
    // Note: QlgChmod() is only _conditionally_ threadsafe.

    if (fd_.getSystemVRM() < 0x00050100)
    {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Server is pre-V5R1, so setAccess() is not supported.");
      return false;
    }

    try
    {
      // Create the pgm call object
      if (servicePgm_ == null) {
        setupServiceProgram();
      }

      // Get the current access modes, so that we can selectively turn on/off the desired bit(s).
      int oldAccessMode = getAccess();

      int bitMask = accessType << 6;  // for example: 0000400 == [ 'read' for owner ] 
      if (!ownerOnly) {
        bitMask = bitMask | (accessType << 3) | accessType;
        // for example: 0000444 == [ 'read' for owner, group, and other ]
      }

      int newAccessMode;
      if (enableAccess) {
        newAccessMode = oldAccessMode | bitMask;  // selectively turn bits _on_
      }
      else {  // disable access
        newAccessMode = oldAccessMode & ~bitMask;  // selectively turn bits _off_
      }
      newAccessMode = newAccessMode & 0007777;  // QlgChmod can only set the low 12 bits

      ProgramParameter[] parameters = new ProgramParameter[]
      {
        // Parameter 1: Qlg_Path_Name_T *Path_Name (input) :
        new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
        // Parameter 2: int amode (input) :
        new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(newAccessMode))
      };

      // Call the service program.
      byte[] returnedBytes = servicePgm_.runServiceProgram("QSYS", "QP0LLIB1", "QlgChmod", parameters);
      if (returnedBytes == null)
      {
        Trace.log(Trace.ERROR, "Call to QlgChmod() returned null.");
        throw new AS400Exception(servicePgm_.getMessageList());
      }

      int returnValue = BinaryConverter.byteArrayToInt(returnedBytes, 0);

      switch (returnValue)
      {
        case -1:  // this indicates that we got an "errno" back
          {
            int errno = BinaryConverter.byteArrayToInt(returnedBytes, 4);
            Trace.log(Trace.ERROR, "Received errno " + errno + " from QlgChmod() for file " + fd_.path_);
            throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR, errno);
          }

        case 0:  // the call to QlgChmod() was successful.
          {
            break;
          }
        default:  // This should never happen. The API spec says it only returns 0 or -1.
          {
            Trace.log(Trace.ERROR, "Received unexpected return value " + returnValue + " from QlgChmod() for file " + fd_.path_);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE, "QlgChmod()", returnValue);
          }
      }
    }
    catch (AS400SecurityException e) {
      throw e;
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      Trace.log(Trace.ERROR, "Error while determining accessibility of file.", e);
      throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
    }

    return true;
  }


  /**
   Calls QlgStat() to get status information about the file.
   If the file does not exist, returns null.
   Note: The QlgStat API was introduced in V5R1.  Do not call this method without checking VRM.
   **/
  private int getAccess()
    throws IOException, AS400SecurityException, ObjectDoesNotExistException
  {
    // Assume that the caller has already connected to the server.
    // Assume that the caller has already verified that the server is V5R1 or higher.

    // We will call the QlgStat API, to get status information about the file.
    // Note: QlgStat() is only _conditionally_ threadsafe.

    int statInfo = 0;
    try
    {
      // Create the pgm call object
      if (servicePgm_ == null) {
        setupServiceProgram();
      }

      int bufferSizeProvided = 128;  // large enough to accommodate a 'stat' structure

      ProgramParameter[] parameters = new ProgramParameter[]
      {
        // Parameter 1: Qlg_Path_Name_T *Path_Name (input) :
        new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
        // Parameter 2: struct stat *buf (output) :
        new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, bufferSizeProvided),
      };

      // Call the service program.
      byte[] returnedBytes = servicePgm_.runServiceProgram("QSYS", "QP0LLIB1", "QlgStat", parameters);
      if (returnedBytes == null)
      {
        Trace.log(Trace.ERROR, "Call to QlgStat() returned null.");
        throw new AS400Exception(servicePgm_.getMessageList());
      }

      int returnValue = BinaryConverter.byteArrayToInt(returnedBytes, 0);

      switch (returnValue)
      {
        case -1:  // this indicates that we got an "errno" back
          {
            int errno = BinaryConverter.byteArrayToInt(returnedBytes, 4);
            switch (errno)
            {
              case 3025:  // ENOENT: "No such path or directory."
                // Assume that we got this error because the file isn't a symlink.
                Trace.log(Trace.ERROR, "Received errno "+errno+" from QlgStat() for file " + fd_.path_ + ". Assuming that the file does not exist.");
                throw new ObjectDoesNotExistException(fd_.path_, ObjectDoesNotExistException.OBJECT_DOES_NOT_EXIST);
              case 3401:  // EACCES: "Permission denied."
                // Assume that we got this error because we don't have the specified access.
                Trace.log(Trace.ERROR, "Received errno "+errno+" from QlgStat() for file " + fd_.path_ + ". Assuming that the user does not have permission to access the file.");
                throw new AS400SecurityException(fd_.path_, AS400SecurityException.DIRECTORY_ENTRY_ACCESS_DENIED);
              default:    // some other errno
                Trace.log(Trace.ERROR, "Received errno " + errno + " from QlgStat() for file " + fd_.path_);
                throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR, errno);
            }
          }

        case 0:  // the call to QglStat() was successful.
          {
            // Parse the "file modes" from the returned stat structure (second parameter).
            statInfo = parseStatInfo(parameters[1].getOutputData());
            break;
          }
        default:  // This should never happen. The API spec says it only returns 0 or -1.
          {
            Trace.log(Trace.ERROR, "Received unexpected return value " + returnValue + " from QlgStat() for file " + fd_.path_);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_RETURN_CODE, "QlgStat()", returnValue);
          }
      }
    }
    catch (AS400SecurityException e) {
      throw e;
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      Trace.log(Trace.ERROR, "Error while determining accessibility of file.", e);
      throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
    }

    return statInfo;
  }

  /**
  Clear the cached attributes.  This is needed when cached attributes
  need to be refreshed.           
  **/
  public void clearCachedAttributes()                  //@D9A
  {
    attributesReply_ = null;
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
   If file does not exist, create it.  If the file
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

    // Convert the path name to the server CCSID.
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
  //without a call to the server.
  /**
   Determines if a file is a directory without a call to the server.
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
//          * Deleted the following case... treat as default (false)               @D8A
//          * For example, OUTQ, DSPF, PRTF, and TAPF objects are NOT directories  @D8A
//          * LIB, PF, LF, SRCPF are returned as IFSListAttrsRep.DIRECTORY         @D8A
//          * SAVF is returned as IFSListAttrsRep.FILE                             @D8A
//      case IFSListAttrsRep.AS400_OBJECT:                                         @D8D
//         // Server libraries and database files look like directories            @D8D
//         String nameUpper = name.toUpperCase();         // @C2a                  @D8D
//         answer = (nameUpper.endsWith(".LIB") ||                                 @D8D
//                   nameUpper.endsWith(".FILE")); //B1C Changed path_ to name     @D8D
//                   //@C2c
//                   //@B1D Removed code that checked for file separators
//                   //|| path_.endsWith(".LIB" + IFSFile.separator) ||
//                   //path_.endsWith(".FILE" + IFSFile.separator));
//         break;                                                                  @D8D*/
      default:
         answer = false;
    }
    return answer;
  }

  //@B1A Moved code from isFile() to support determining if a file is a file
  //without a call to the server.
  /**
   Determines if a file is a file without a call to the server.
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
//          * Deleted the following case... treat as default (false)               @D8A
//          * For example, OUTQ, DSPF, PRTF, and TAPF objects are NOT files        @D8A
//          * LIB, PF, LF, SRCPF are returned as IFSListAttrsRep.DIRECTORY         @D8A
//          * SAVF is returned as IFSListAttrsRep.FILE                             @D8A
//      case IFSListAttrsRep.AS400_OBJECT:                                         @D8D
//         //Server libraries and database files look like directories.
//         String nameUpper = name.toUpperCase();         // @C2a                  @D8D
//         answer = !(nameUpper.endsWith(".LIB") ||                                @D8D 
//                    nameUpper.endsWith(".FILE")); //B1C Changed path_ to name    @D8D
//                  //@C2c
//                  //@B1D Removed code that checked for file separators
//                  //|| path_.endsWith(".LIB" + IFSFile.separator) ||
//                  //path_.endsWith(".FILE" + IFSFile.separator));
//         break;                                                                  @D8D*/
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

    returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
    // Attempt to list the attributes of the specified file.
    IFSListAttrsRep attrs = getAttributeSetFromServer(name);
    if (attrs != null)
    {
      returnCode = IFSReturnCodeRep.SUCCESS;
      attributesReply_ = attrs;
    }

    return (returnCode);
    // Design note:
    // The QlgAccess() API gives somewhat different results in certain scenarios.
    // For example, in one test it returned 'true' when the directory didn't exist.
    // Using IFSListAttrsReq appears to be more reliable.
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

  private IFSListAttrsRep getAttributeSetFromServer(String filePath)
    throws IOException, AS400SecurityException
  {
    IFSListAttrsRep reply = null;

    // Attempt to list the attributes of the specified file.
    Vector replys = listAttributes(filePath, NO_MAX_GET_COUNT, null, NO_RESTART_NAME, !SORT_LIST);
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
    fd_.connect();
    return fd_.getCCSID();
  }


  private static final boolean SPACE_AVAILABLE  = true;
  private static final boolean SPACE_TOTAL      = false;
  /**
   Determines the amount of unused storage space in the file system.
   @param forUserOnly Whether to report only the space for the user. If false, report space in entire file system.
   @return The number of bytes of storage available.
   **/
  public long getFreeSpace(boolean forUserOnly)
    throws IOException, AS400SecurityException
  {
    long spaceAvailable = getAmountOfSpace(forUserOnly, SPACE_AVAILABLE);

    // Design note:  When querying the space available for a specific user,
    // the File Server team advises us to make two queries:
    // First query the space available to the user (within the user profile's "Maximum Storage Allowed" limit).
    // Then query the total space available in the file system.
    // The smaller of the two values is what we should then report to the application.
    if (forUserOnly)
    {
      long spaceAvailableInFileSystem = getAmountOfSpace(false, SPACE_AVAILABLE);
      spaceAvailable = Math.min(spaceAvailable, spaceAvailableInFileSystem);
    }

    return spaceAvailable;
  }


  /**
   Determines the total amount of storage space in the file system.
   @param forUserOnly Whether to report only the space for the user. If false, report space in entire file system.
   @return The number of bytes of storage.
   **/
  public long getTotalSpace(boolean forUserOnly)
    throws IOException, AS400SecurityException
  {
    return getAmountOfSpace(forUserOnly, SPACE_TOTAL);
  }


  /**
   Returns the amount of storage space.
   @param forUserOnly Whether to report only the space for the user. If false, report space in entire file system.
   @param availableSpaceOnly Whether to report only the space available. If false, report total space, rather than just available space.
   @return The number of bytes of storage.
   **/
  private long getAmountOfSpace(boolean forUserOnly, boolean availableSpaceOnly)
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    long amountOfSpace = 0L;
    int directoryHandle;
    ClientAccessDataStream ds = null;
    int rc = 0;

    if (forUserOnly)  // prepare to get space info for specific user
    {
      // Special value for file handle, indicating that space attributes for the user should be retrieved, rather than space attributes of the file system.
      // According to the PWSI Datastream Spec:
      // "When the client sends 0 as the working directory handle ... the space characteristics of the user are returned instead of the characteristics of the system."
      directoryHandle = 0;
    }
    else  // prepare to get space info for the entire file system
    {
      // To query the file system, we need to specify a "working directory handle" to the directory.  So first, get a handle.
      String path = fd_.path_;
      if (isDirectory() != IFSReturnCodeRep.SUCCESS) {
        path = IFSFile.getParent(fd_.path_);
      }
      byte[] pathname = fd_.getConverter().stringToByteArray(path);
      try
      {
        // Issue a Create Working Directory Handle request.
        IFSCreateDirHandleReq req = new IFSCreateDirHandleReq(pathname, fd_.preferredServerCCSID_);
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

      // Verify that we got a handle back.
      rc = 0;
      if (ds instanceof IFSCreateDirHandleRep)
      {
        directoryHandle = ((IFSCreateDirHandleRep) ds).getHandle();
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
        rc = ((IFSReturnCodeRep) ds).getReturnCode();
        if (rc != IFSReturnCodeRep.SUCCESS)
        {
          Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
        }
        throw new ExtendedIOException(fd_.path_, rc);
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
    }

    // Query the amount of space.
    ds = null;
    try
    {
      // Issue a query space request.
      IFSQuerySpaceReq req = new IFSQuerySpaceReq(directoryHandle);
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
    rc = 0;
    if (ds instanceof IFSQuerySpaceRep)
    {
      if (availableSpaceOnly) {
        amountOfSpace = ((IFSQuerySpaceRep) ds).getFreeSpace();
      }
      else {
        amountOfSpace = ((IFSQuerySpaceRep) ds).getTotalSpace();
      }
    }
    else if (ds instanceof IFSReturnCodeRep)
    {
      rc = ((IFSReturnCodeRep) ds).getReturnCode();
      if (rc != IFSReturnCodeRep.SUCCESS)
      {
        Trace.log(Trace.ERROR, "IFSReturnCodeRep return code = ", rc);
      }
      throw new ExtendedIOException(fd_.path_, rc);
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

    return amountOfSpace;
  }


  /**
   Returns the name of the user profile that is the owner of the file.
   Returns "" if called against a directory.
   **/
  public String getOwnerName()
    throws IOException, AS400SecurityException
  {
    // Design note: This method demonstrates how to get attributes that are returned in the OA1* structure (as opposed to the OA2).
    String ownerName = null;

    fd_.connect();
    // The 'owner name' field is in the OA1 structure; the flag is in the first Flags() field.
    try
    {
      IFSListAttrsRep reply = fd_.listObjAttrs1(IFSObjAttrs1.OWNER_NAME_FLAG, 0);
      if (reply != null) {
        ownerName = reply.getOwnerName(fd_.system_.getCcsid());
      }
      else {
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Return code from getOwnerName: " + fd_.errorRC_);
        if (fd_.errorRC_ == IFSReturnCodeRep.FILE_NOT_FOUND ||
            fd_.errorRC_ == IFSReturnCodeRep.PATH_NOT_FOUND)
        {
          throw new ExtendedIOException(fd_.path_, ExtendedIOException.PATH_NOT_FOUND);
        }
      }
    }
    catch (ExtendedIOException e) {
      if (e.getReturnCode() == ExtendedIOException.DIR_ENTRY_EXISTS) {
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unable to determine owner of directory.", e);
      }
      else throw e;
    }

    return (ownerName == null ? "" : ownerName);
  }


  // Design note: The following is an alternative implementation of getOwnerName(), using the Qp0lGetAttr API.

//  /**
//   Returns the name of the user profile that is the owner of the file.
//   Returns null if *NOUSRPRF or if error.
//   **/
//  private String getOwnerName(AS400 system)
//    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
//  {
//    String ownerName = null;
//    int pathnameCCSID = fd_.converter_.getCcsid();
//    ProgramParameter[] parms = new ProgramParameter[7];
//
//    //
//    // Parameter 0 (input, reference): The "path name" structure.
//
//    ByteArrayOutputStream baos = new ByteArrayOutputStream(600);
//    byte[] zeros = { (byte)0, (byte)0, (byte)0, (byte)0, (byte)0,
//                     (byte)0, (byte)0, (byte)0, (byte)0, (byte)0 };
//    // BINARY(4): CCSID of pathname and path delimiter
//    baos.write(BinaryConverter.intToByteArray(pathnameCCSID),0,4);
//    // CHAR(2): Country or region ID.  X'0000' == Use the current job country or region ID.
//    baos.write(zeros,0,2);
//    // CHAR(3): Language ID.  X'000000' == Use the current job language ID.
//    baos.write(zeros,0,3);
//    // CHAR(3): Reserved.  Must be set to hexadecimal zeros.
//    baos.write(zeros,0,3);
//    // BINARY(4): Path type indicator.  0 == The path name is a character string, and the path name delimiter character is 1 character long.
//    baos.write(zeros,0,4);
//    // BINARY(4): Length of path name (in bytes).
//    byte[] pathnameBytes = fd_.getPathnameAsBytes();
//    baos.write(BinaryConverter.intToByteArray(pathnameBytes.length),0,4);
//    // CHAR(2): Path name delimiter character.
//    baos.write(fd_.converter_.stringToByteArray("/"),0,2);
//    // CHAR(10): Reserved.  Must be set to hexadecimal zeros.
//    baos.write(zeros,0,10);
//    // CHAR(*): Path name.
//    baos.write(pathnameBytes, 0, pathnameBytes.length);
//
//    parms[0] = new ProgramParameter(baos.toByteArray());
//    setPassByReference(parms[0]);
//
//    //
//    // Parameter 1 (input, reference): The attribute identifiers array.
//
//    baos.reset();
//    // BINARY(4): Number of requested attributes.
//    baos.write(BinaryConverter.intToByteArray(1),0,4);
//    // BINARY(4): Attribute identifier.  11 == QP0L_ATTR_AUTH, public/private authorities
//    baos.write(BinaryConverter.intToByteArray(11),0,4);
//    parms[1] = new ProgramParameter(baos.toByteArray());
//    setPassByReference(parms[1]);
//
//    //
//    // Parameter 2 (input, reference): Buffer for the returned attribute values.
//    parms[2] = new ProgramParameter(64);
//    setPassByReference(parms[2]);
//
//    //
//    // Parameter 3 (input): Buffer size provided.
//    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(64));
//
//    //
//    // Parameter 4 (output, reference): Buffer size needed.
//    parms[4] = new ProgramParameter(4);
//    setPassByReference(parms[4]);
//
//    //
//    // Parameter 5 (output, reference): Number of bytes returned.
//    parms[5] = new ProgramParameter(4);
//    setPassByReference(parms[5]);
//
//    // Parameter 6 (input): Follow symlink.  0 == Do not follow symlink; 1 == follow symlink
//    boolean followSymlink = false;  // this should probably be a parameter on the method
//    int follow = (followSymlink ? 1 : 0);
//    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(follow));
//
//    ServiceProgramCall spc = new ServiceProgramCall(system, "/QSYS.LIB/QP0LLIB2.SRVPGM", "Qp0lGetAttr", ServiceProgramCall.RETURN_INTEGER, parms);
//    //pc.suggestThreadsafe(true);
//
//    if (!spc.run()) {
//      throw new AS400Exception(spc.getMessageList());
//    }
//
//    // Check the returned byte counts.
//    int bufSizeNeeded = BinaryConverter.byteArrayToInt(parms[4].getOutputData(), 0);
//    int numBytesReturned = BinaryConverter.byteArrayToInt(parms[5].getOutputData(), 0);
//
//    // The 'Object Owner' field is the CHAR(10) at offset 16 in the output data.
//    byte[] outputData = parms[2].getOutputData();
//
//    int ccsid = fd_.system_.getCcsid();  // system CCSID (usually EBCDIC)
//    ConvTable conv = ConvTable.getTable(ccsid, null);
//    ownerName = conv.byteArrayToString(outputData, 16, 10).trim();
//    if (ownerName.equals("*NOUSRPRF")) ownerName = null;
//
//    return ownerName;
//  }


  // @B7a
  /**
   Returns the file's owner's "user ID" number.
   Returns -1 if error.
   **/
  public long getOwnerUID()
    throws IOException, AS400SecurityException        // @C0c
  {
    fd_.connect();
    IFSListAttrsRep reply = fd_.listObjAttrs2(); // the "owner UID" field is in the OA2 structure
    if (reply != null)
    {
      return reply.getOwnerUID();
    }
    else return -1L;        // @C0c
  }


  /**
   Returns the path of the integrated file system object that is directly pointed to by the symbolic link represented by this object.  Returns <tt>null</tt> if the file is not a symbolic link, does not exist, or is in an unsupported file system.
   <p>
   This method is not supported for files in the following file systems:
   <ul>
   <li>QSYS.LIB
   <li>Independent ASP QSYS.LIB
   <li>QDLS
   <li>QOPT
   <li>QNTC
   </ul>

   @return The path directly pointed to by the symbolic link, or <tt>null</tt> if the IFS object is not a symbolic link or does not exist. Depending on how the symbolic link was defined, the path may be either relative or absolute.

   @exception IOException If an error occurs while communicating with the system.
   @exception AS400SecurityException If a security or authority error occurs.
   **/
  public String getPathPointedTo()
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    // We will call the QlgReadlink API, to determine the path of the immediately pointed-to file.  Note that QlgReadlink is only _conditionally_ threadsafe.

    String resolvedPathname = null;
    try
    {
      // Create the pgm call object
      if (servicePgm_ == null) {
        setupServiceProgram();
      }

      int bufferSizeProvided = 1024;  // large enough for most anticipated paths

      ProgramParameter[] parameters = new ProgramParameter[]
      {
        // Parameter 1: Qlg_Path_Name_T *path (input) :
        new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
        // Parameter 2: Qlg_Path_Name_T *buf (output) :
        new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, bufferSizeProvided),
        // Parameter 3: size_t bufsiz (input) :
        new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(bufferSizeProvided))
      };

      final int HEADER_LENGTH = 32;  // fixed header of returned Qlg_Path_Name_T structure

      boolean repeatRun;
      do
      {
        repeatRun = false;
        // Call the service program.
        byte[] returnedBytes = servicePgm_.runServiceProgram("QSYS", "QP0LLIB1", "QlgReadlink", parameters);
        if (returnedBytes == null)
        {
          Trace.log(Trace.ERROR, "Call to QlgReadlink() returned null.");
          throw new AS400Exception(servicePgm_.getMessageList());
        }

        int returnValue = BinaryConverter.byteArrayToInt(returnedBytes, 0);

        if (returnValue == -1)  // this indicates that we got an "errno" back
        {
          int errno = BinaryConverter.byteArrayToInt(returnedBytes, 4);
          switch (errno)
          {
            case 3021:  // EINVAL: "The value specified for the argument is not correct."
              // Assume that we got this error because the file isn't a symlink.
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgReadlink() for file " + fd_.path_ + ". Assuming that the file is not a symbolic link.");
              break;
            case 3025:  // ENOENT: "No such path or directory."
              // Assume that we got this error because the file isn't a symlink.
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgReadlink() for file " + fd_.path_ + ". Assuming that the file does not exist.");
              break;
            default:    // some other errno
              Trace.log(Trace.ERROR, "Received errno " + errno + " from QlgReadlink() for file " + fd_.path_);
              throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR, errno);
          }
        }

        else if ((returnValue + HEADER_LENGTH) > bufferSizeProvided)
        {
          repeatRun = true;
          // Note: returnValue is number of bytes required to hold complete path.
          int bufferSizeNeeded = returnValue + HEADER_LENGTH;
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "QlgReadlink() buffer too small. Buffer size provided: " + bufferSizeProvided + ". Buffer size needed: " + bufferSizeNeeded+". Calling QlgReadLink() with larger buffer.");
          bufferSizeProvided = bufferSizeNeeded;
          parameters[1].setOutputDataLength(bufferSizeProvided);
          parameters[2].setInputData(BinaryConverter.intToByteArray(bufferSizeProvided));
        }

        else  // We allocated a sufficiently large buffer for the returned data.
        {
          // Parse the pathname from the returned pathname structure (second parameter).
          resolvedPathname = parsePathName(parameters[1].getOutputData());
        }
      }
      while (repeatRun);
    } // try
    catch (AS400SecurityException e) {
      throw e;
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      Trace.log(Trace.ERROR, "Error while resolving symbolic link.", e);
      throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
    }

    return resolvedPathname;
  }


//
// Path name structure for QlgAccess():
// 
//  0 INPUT 	BINARY(4)  CCSID
//  4 INPUT 	CHAR(2)    Country or region ID
//  6 INPUT 	CHAR(3)    Language ID
//  9 INPUT 	CHAR(3)    Reserved
// 12 INPUT 	BINARY(4)  Path type indicator
// 16 INPUT 	BINARY(4)  Length of path name
// 20 INPUT 	CHAR(2)    Path name delimiter character
// 22 INPUT 	CHAR(10)   Reserved
// 32 INPUT 	CHAR(*)    Path name
// 
// 
// Value of "path type indicator" field:
//   2:  The path name is a character string, and the path name delimiter character is 2 characters long.
//


  // Utility method to convert String path into path name parameter used by the QlgAccess() API.
  private byte[] createPathName() throws IOException
  {
    ConverterImplRemote conv = new ConverterImplRemote();
    conv.setCcsid(1200, fd_.system_);
    int pathLength = fd_.path_.length();  // number of Unicode chars

    byte[] buf = new byte[32 + pathLength * 2];    // 2 bytes per Unicode char
    BinaryConverter.intToByteArray(1200, buf, 0);  // CCSID
    BinaryConverter.intToByteArray(2, buf, 12);    // path type indicator
    BinaryConverter.intToByteArray(pathLength * 2, buf, 16); // length of path name (#bytes)
    conv.stringToByteArray("/", buf, 20, 2);       // path name delimiter
    conv.stringToByteArray(fd_.path_, buf, 32);    // path name
    return buf;
  }


  // Utility method to parse the path name parameter returned by the QlgAccess() API.
  private String parsePathName(byte[] buf) throws IOException
  {
    ConverterImplRemote conv = new ConverterImplRemote();
    conv.setCcsid(1200, fd_.system_);
    int offset = 0;
    int nameLength;
    if (DEBUG)
    {
      System.out.println("Buffer length: " + buf.length);
      System.out.println("CCSID: " + BinaryConverter.byteArrayToInt(buf, offset));
      offset += 4;
      //System.out.println("Country: " + conv.byteArrayToString(buf, offset, 2));
      offset += 2;
      //System.out.println("LangID: " + conv.byteArrayToString(buf, offset, 3));
      offset += 3;
      offset += 3;  // reserved field
      System.out.println("Path type: " + BinaryConverter.byteArrayToInt(buf, offset));
      offset += 4;
      nameLength = BinaryConverter.byteArrayToInt(buf, offset);
      System.out.println("Path name length: " + nameLength);
      offset += 4;
      System.out.println("Delimiter: " + conv.byteArrayToString(buf, offset, 2));
      offset += 2;
      offset += 10;  // reserved field
    }
    else {
      offset += 16;
      nameLength = BinaryConverter.byteArrayToInt(buf, offset);
      offset += 16;
    }

    // We will assume that the caller has verified that the buffer was big enough to accommodate the returned data.
    String pathname = conv.byteArrayToString(buf, offset, nameLength);
    return pathname;
  }


  // Utility method to parse the structure returned by the QlgStat() API.
  private int parseStatInfo(byte[] buf) throws IOException
  {
    ConverterImplRemote conv = new ConverterImplRemote();
    conv.setCcsid(37, fd_.system_); // always EBCDIC

    int fileMode = BinaryConverter.byteArrayToInt(buf, 0);
    return fileMode;


// This is for future reference, in case we ever want to exploit other returned fields.
// Note that the QlgStat structure is specified in system header files stat.h and types.h
//
//    int offset = 0;
//    System.out.println("File mode (octal): " + Integer.toOctalString(fileMode) );
//    offset += 4;
//    System.out.println("File serial number: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Number of links: " + BinaryConverter.byteArrayToShort(buf, offset));
//    offset += 2;
//    offset += 2;  // reserved field
//    System.out.println("UID of owner: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Group ID: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("File size (#bytes): " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Time of last access: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Time of last mod: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Time of status chg: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Device ID: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Block size (#bytes): " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Allocation size (#bytes): " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Object type: " + conv.byteArrayToString(buf, offset, 10)); // field is 11 characters, null-terminated, so exclude the final null
//    offset += 11;
//    offset += 1;  // reserved
//    System.out.println("Data codepage: " + BinaryConverter.byteArrayToShort(buf, offset));
//    offset += 2;
//    System.out.println("Data CCSID: " + BinaryConverter.byteArrayToShort(buf, offset));
//    offset += 2;
//    System.out.println("Device ID: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Number of links: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    System.out.println("Device ID (64bit): " + BinaryConverter.byteArrayToLong(buf, offset));
//    offset += 8;
//    System.out.println("File system ID (64bit): " + BinaryConverter.byteArrayToLong(buf, offset));
//    offset += 8;
//    System.out.println("Mount ID: " + BinaryConverter.byteArrayToInt(buf, offset));
//    offset += 4;
//    offset += 32;  // reserved
//    System.out.println("Serial# gen ID: " + BinaryConverter.byteArrayToInt(buf, offset));
  }


  // @B5a
  // Returns zero-length string if the file has no subtype.
  public String getSubtype()
    throws IOException, AS400SecurityException
  {
    String subtype = "";

    // Ensure that we are connected to the server.
    fd_.connect();

    // Convert the path name to the server CCSID.
    byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

    boolean needCodePage;
    if (fd_.getSystemVRM() >= 0x00060100 &&
        fd_.path_.indexOf("/QSYS.LIB") != -1) {
      needCodePage = true;
    }
    else needCodePage = false;

    // Prepare the List Attributes request.

    // Set up the list of Extended Attributes Names.
    byte[] eaName_TYPE = fd_.converter_.stringToByteArray(".TYPE");
    int eaNameBytesLength;
    byte[][] eaNamesList;
    // Special handling for QSYS files, starting in V6R1.
    // Starting in V6R1, for QSYS files, the ".TYPE" EA value field is returned in the CCSID of the object.
    // Prior to V6R1, the field is always returned in EBCDIC.
    if (needCodePage)
    {
      byte[] eaName_CODEPAGE = fd_.converter_.stringToByteArray(".CODEPAGE");
      eaNameBytesLength = eaName_TYPE.length + eaName_CODEPAGE.length;
      eaNamesList = new byte[][] { eaName_TYPE, eaName_CODEPAGE };
    }
    else  // not in QSYS, or pre-V6R1
    {
      eaNameBytesLength = eaName_TYPE.length;
      eaNamesList = new byte[][] { eaName_TYPE };
    }

    IFSListAttrsReq req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,
                              IFSListAttrsReq.NO_AUTHORITY_REQUIRED, NO_MAX_GET_COUNT,
                              null, false, eaNamesList, eaNameBytesLength, false, fd_.patternMatching_);  // @C3c

    Vector replys = fd_.listAttributes(req);

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
      Hashtable extendedAttributes = reply.getExtendedAttributeValues();
      byte[] subtypeAsBytes = (byte[])extendedAttributes.get(".TYPE");
      if (subtypeAsBytes != null)
      {
        int ccsid;
        if (!needCodePage) {
          ccsid = 37; // the returned bytes are in EBCDIC
        }
        else {
          // Get the ".CODEPAGE" extended attribute value from the reply.
          byte[] codepageAsBytes = (byte[])extendedAttributes.get(".CODEPAGE");
          // The .CODEPAGE attribute is returned as 2 bytes in little-endian format.
          // Therefore we need to swap the bytes.
          byte[] swappedBytes = new byte[2];  // the codepage is returned in 2 bytes
          swappedBytes[0] = codepageAsBytes[1];
          swappedBytes[1] = codepageAsBytes[0];
          ccsid = BinaryConverter.byteArrayToUnsignedShort(swappedBytes,0);
          if (ccsid == 1400) ccsid = 1200;  // codepage 1400 corresponds to CCSID 1200
        }
        try {
          subtype = (new CharConverter(ccsid)).byteArrayToString(subtypeAsBytes, 0).trim();
        }
        catch (java.io.UnsupportedEncodingException e) {
          Trace.log(Trace.ERROR, "Unrecognized codepage returned: " + ccsid, e);
          subtype = "??";
        }
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
      attributesReply_ = getAttributeSetFromServer(fd_.path_);
    }

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
      attributesReply_ = getAttributeSetFromServer(fd_.path_);
    }

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

    if (fd_.getSystemVRM() < 0x00050300)
    {
      if (Trace.traceOn_) Trace.log(Trace.WARNING, "Server is V5R2 or lower, so isSymbolicLink() will always report false.");
      return false;
    }

    // Temporary workaround, until better File Server support is in place.
    // if (attributesReply_ != null)
    // {
    // System.out.println("DEBUG IFSFileImplRemote.isSymbolicLink(): attributesReply_ != null");
    // result = attributesReply_.isSymbolicLink(fd_.serverDatastreamLevel_);
    // }
    // else
    //
    if (!determinedIsSymbolicLink_)
    {
      // Note: As of V5R3, we can't get accurate symbolic link info by querying the attrs of a specific file.
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
        IFSCachedAttributes[] attrList = listDirectoryDetails(wildCardPattern, dirPath, NO_MAX_GET_COUNT, pathBytes, IS_RESTART_NAME, !SORT_LIST);

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
    else  // the system is V5R2 (and therefore, datastream level is 3)
    {
      // Convert the path name to the server CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      // Send the List Attributes request.  Indicate that we want the "8-byte file size".
      IFSListAttrsReq req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,
                                                IFSListAttrsReq.NO_AUTHORITY_REQUIRED, NO_MAX_GET_COUNT,
                                                null, true, null, 0, true, fd_.patternMatching_);  // @C3c
      Vector replys = fd_.listAttributes(req);

      if (replys == null) {
        if (fd_.errorRC_ != 0) {
          throw new ExtendedIOException(fd_.path_, fd_.errorRC_);
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



  // Fetch list attributes reply(s) for the specified path.
  private Vector listAttributes(String path, int maxGetCount, byte[] restartNameOrID, boolean isRestartName, boolean sortList)           // @D4C @C3c
    throws IOException, AS400SecurityException
  {
    // Assume connect() has already been done.

    // Convert the pathname to the server CCSID.
    byte[] pathname = fd_.converter_.stringToByteArray(path);

    // Prepare the 'list attributes' request.
    IFSListAttrsReq req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,                              // @D4A
                                              IFSListAttrsReq.NO_AUTHORITY_REQUIRED, maxGetCount,               // @D4A
                                              restartNameOrID,                                       // @D4A @C3c
                                              isRestartName, // @C3a
                                              null, 0, false, fd_.patternMatching_);
    
    if (sortList) req.setSorted(true);
    return fd_.listAttributes(req);  // Note: This does setFD() on each returned IFSListAttrsRep..
  }


  // @A7A
  // List the files/directories in the specified directory.
  // Returns null if specified file or directory does not exist.
  public String[] listDirectoryContents(String directoryPath)
    throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    Vector replys = listAttributes(directoryPath, NO_MAX_GET_COUNT, null, NO_RESTART_NAME, sortLists_);
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
                                                    boolean isRestartName,      // @C3A
                                                    boolean sortList)
     throws IOException, AS400SecurityException
  {
    // Ensure that we are connected to the server.
    fd_.connect();

    IFSCachedAttributes[] fileAttributes = null;

    try
    {
      // Design note: Due to a limitation in the File Server, if we specify a "filename pattern", we cannot get OA1 or OA2 structures in the reply.
      // Only "handle-based" requests can get OA* structures in the reply; and a handls is specific to a single file.
      // This prevents us, for example, from obtaining the "name of file owner" values for an entire list of files at once; rather, we must obtain that attribute one file at a time.
      Vector replys = listAttributes(pathPattern, maxGetCount, restartNameOrID, isRestartName, sortList);

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
             // file is an invalid symbolic link (circular or points
             // to a non-existent object).  Such a link cannot
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
      throw e;
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
    return listDirectoryDetails(pathPattern, directoryPath, maxGetCount, restartNameBytes, IS_RESTART_NAME, sortLists_);                                        //@D7C
  }

  // @C3a
  // List the files/directories details in the specified directory.
  // Returns null if specified file or directory does not exist.
  public IFSCachedAttributes[] listDirectoryDetails(String pathPattern,
                                                    String directoryPath,
                                                    int maxGetCount,
                                                    byte[] restartID,
                                                    boolean allowSortedRequests) //@D7C
     throws IOException, AS400SecurityException
  {
    boolean sortParameter = (allowSortedRequests ? sortLists_ : false); //@D7A
    return listDirectoryDetails(pathPattern, directoryPath, maxGetCount, restartID, !IS_RESTART_NAME, sortParameter); //@D7C
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
      // Convert the directory name to the server CCSID.
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
          throw new AS400SecurityException(directory, AS400SecurityException.DIRECTORY_ENTRY_ACCESS_DENIED);
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
      // Convert the path names to the server CCSID.
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
   Sets the file's "data CCSID" tag.
   **/
  public boolean setCCSID(int ccsid)
    throws IOException, AS400SecurityException
  {
    // To change the file data CCSID, we need to get the file's current attributes (in an OA2 structure), reset the 'CCSID' field, and then send back the modified OA2 struct in a Change Attributes request.

    fd_.connect();
    IFSListAttrsRep reply = fd_.listObjAttrs2();  // get current attributes (OA2 structure)
    if (reply == null) {
      if (fd_.errorRC_ != 0) {
        throw new ExtendedIOException(fd_.path_, fd_.errorRC_);
      }
      else throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }

    boolean success = false;
    IFSObjAttrs2 objAttrs = reply.getObjAttrs2(); // get the OA2* structure

    // Sanity-check the length: If it's an OA2a or OA2b, the length will be 144 bytes.  If it's an OA2c, the length will be 160 bytes.
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Length of returned OA2* structure (should be 144 or 160): " + objAttrs.length());

    // Reset the "CCSID of the object" field in the OA2* structure.
    objAttrs.setCCSID(ccsid, fd_.serverDatastreamLevel_);

    // Issue a change attributes request.
    ClientAccessDataStream ds = null;
    try
    {
      // Convert the path name to the server CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                    fd_.preferredServerCCSID_,
                                                    objAttrs,
                                                    fd_.serverDatastreamLevel_);
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
      if (rc == IFSReturnCodeRep.SUCCESS) {
        success = true;
        fd_.setCCSID(ccsid); // update the cached CCSID value in the fd_
      }
      else {
        if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error setting file data CCSID: " +
                                      "IFSReturnCodeRep return code = ", rc);
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
   **/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean setFixedAttributes(int attributes)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    fd_.connect();

    // Issue a change attributes request.
    ClientAccessDataStream ds = null;
    try
    {
      // Convert the path name to the server CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                    fd_.preferredServerCCSID_,
                                                    attributes,
                                                    true,
                                                    fd_.serverDatastreamLevel_);
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
   **/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean setHidden(boolean attribute)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    fd_.connect();

    boolean success = false;

    // Setting the fixed attributes of a file involves
    // replacing the current set of attributes.  The first
    // set is to get the current set.
    IFSListAttrsRep attributes = getAttributeSetFromServer(fd_.path_);

    if (attributes != null)
    {
       // Now that we have the current set of attributes, figure
       // out if the bit is currently on.
       int currentFixedAttributes = attributes.getFixedAttributes();
       boolean currentHiddenBit = (currentFixedAttributes & 2) != 0;

       // If current does not match what the user wants we need to go
       // to the server to fix it.
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
              // Convert the path name to the server CCSID.
              byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

              IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                            fd_.preferredServerCCSID_,
                                                            newAttributes,
                                                            true,
                                                            fd_.serverDatastreamLevel_);
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


  // @B8c
  /**
   Changes the last modified time of the integrated file system object represented by this object to <i>time</i>.
   @param time The desired last modification time (measured in milliseconds
   since January 1, 1970 00:00:00 GMT), or -1 to set the last modification time to the current system time.

   @return true if successful; false otherwise.
   **/

  public boolean setLastModified(long time)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated by the public class.

    int fileHandle = UNINITIALIZED;
    boolean success = false;

    // Ensure that we are connected to the server.
    try
    {
      fd_.connect();

      if (time == -1)  // @B8a
      {
        // We are setting modification time to "current system time".
        // To do that, we will simply read and write-back the first byte in the file.

        // Open the file for read/write.
        fileHandle = fd_.createFileHandle(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN);
        if (fileHandle == UNINITIALIZED) return false;
        else fd_.setOpen(true, fileHandle);  // inform the descriptor of the file handle

        byte[] buffer = new byte[1];  // buffer for reading/writing a single byte

        // If we're setting timestamp to "current system time", we'll need to know how big the file is.
        boolean fileIsEmpty = false;  // @B8a
        if (time == -1) fileIsEmpty = (length()==0 ? true : false);  // @B8a

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
      }
      else  // the caller specified a last-modified time
      {
        // Issue a change attributes request.
        ClientAccessDataStream ds = null;
        try
        {
          // Convert the path name to the server CCSID.
          byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

          IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                        fd_.preferredServerCCSID_,
                                                        0, time, 0,
                                                        fd_.serverDatastreamLevel_);
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

      // Clear any cached attributes.
      attributesReply_ = null;
    }
    finally {
      if (fileHandle != UNINITIALIZED) fd_.close(fileHandle);
    }

    return success;
  }


  // @B8a
  /**
   Sets the length of the integrated file system object represented by this object.  The file can be made larger or smaller.  If the file is made larger, the contents of the new bytes of the file are undetermined.
   @param length The new length, in bytes.
   @return true if successful; false otherwise.
   **/
  public boolean setLength(int length)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    fd_.connect();

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


  // Sets a ProgramParameter to "pass by reference".
  private static final void setPassByReference(ProgramParameter parm)
  {
    try { parm.setParameterType(ProgramParameter.PASS_BY_REFERENCE); }
    catch (PropertyVetoException pve) {}  // should never happen
  }


  /**
   Sets the pattern-matching behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is PATTERN_POSIX.
   @param patternMatching Either {@link IFSFile#PATTERN_POSIX PATTERN_POSIX}, {@link IFSFile#PATTERN_POSIX_ALL PATTERN_POSIX_ALL}, or {@link IFSFile#PATTERN_OS2 PATTERN_OS2}
   **/
  public void setPatternMatching(int patternMatching)
  {
    fd_.patternMatching_ = patternMatching;
  }


  /**
   Alters the read only attribute of the object.  If <i>attribute</i>
   is true, the bit is turned on.  If <i>attribute</i> is turned off,
   the bit is turned off.

   @param attributes The new state of the read only attribute

   @return true if successful; false otherwise.
   **/
   // @D1 - new method because of changes to java.io.file in Java 2.

  public boolean setReadOnly(boolean attribute)
    throws IOException, AS400SecurityException
  {
    // Assume the argument has been validated by the public class.

    // Ensure that we are connected to the server.
    fd_.connect();

    boolean success = false;
    IFSListAttrsRep attributes = getAttributeSetFromServer(fd_.path_);

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
              // Convert the path name to the server CCSID.
              byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

              IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname,
                                                            fd_.preferredServerCCSID_,
                                                            newAttributes,
                                                            true,
                                                            fd_.serverDatastreamLevel_);
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
   Sets the sorting behavior used when files are listed by any of the <tt>list()</tt> or <tt>listFiles()</tt> methods.  The default is <tt>false</tt> (unsorted).
   @param sort If <tt>true</tt>: Return lists of files in sorted order.
   If <tt>false</tt>: Return lists of files in whatever order the file system provides.
   **/
  public void setSorted(boolean sort)
  {
    sortLists_ = sort;
  }


  /**
   Sets the system.
   @param system The server object.
   **/
  public void setSystem(AS400Impl system)
  {
    // Assume the argument has been validated by the public class.

    fd_.system_ = (AS400ImplRemote)system;
  }


  // Setup remote command object on first touch.  Synchronized to protect instance variables.  This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
  protected synchronized void setupServiceProgram() throws IOException
  {
    // If not already setup.
    if (servicePgm_ == null)
    {
      if (fd_.system_.canUseNativeOptimizations())
      {
        try
        {
          servicePgm_ = (RemoteCommandImpl)Class.forName("com.ibm.as400.access.RemoteCommandImplNative").newInstance();
          // Avoid direct reference - it can cause NoClassDefFoundError at class loading time on Sun JVM's.
        }
        catch (Throwable e) {
          // A ClassNotFoundException would be unexpected, since canUseNativeOptions() returned true.
          Trace.log(Trace.WARNING, "Unable to instantiate class RemoteCommandImplNative.", e);
        }
      }
      if (servicePgm_ == null)
      {
        servicePgm_ = new RemoteCommandImplRemote();
      }
      servicePgm_.setSystem(fd_.system_);
    }
  }

}
