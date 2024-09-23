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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Provides a full remote implementation for the IFSFile class.
 **/
class IFSFileImplRemote implements IFSFileImpl
{
    // Used for debugging only.  This should always be false for production.
    private static final boolean DEBUG = false;

    private static final boolean IS_RESTART_NAME = true;
    private static final boolean NO_RESTART_NAME = IS_RESTART_NAME;
    private static final boolean SORT_LIST = true;
    private static final int     NO_MAX_GET_COUNT = -1;
    private static final int     UNINITIALIZED = -1;

    // Constants for QlgAccess(), from system definitions file "unistd.h"
    //private static final int ACCESS_MODE_READ    = 0x04;  // R_OK: test for read permission
    //private static final int ACCESS_MODE_WRITE   = 0x02;  // W_OK: test for write permission
    private static final int ACCESS_MODE_EXECUTE = 0x01;  // X_OK: test for execute permission
    //private static final int ACCESS_MODE_EXISTS  = 0x00;  // F_OK: test for existence of file

    static
    {
        // Add all byte stream reply data streams of interest to the
        // server's reply data stream hash table.
        AS400Server.addReplyStream(new IFSListAttrsRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSOpenRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSCreateDirHandleRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSQuerySpaceRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSLookupRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSGetFileSystemRep(), AS400.FILE);
    }

    transient private IFSListAttrsRep attributesReply_; // "list attributes" reply

    private IFSFileDescriptorImplRemote fd_ = new IFSFileDescriptorImplRemote();

    private Boolean isSymbolicLink_;
    private boolean sortLists_;  // whether file-lists are returned from the File Server in sorted order
    private RemoteCommandImpl rmtCmd_;  // Impl object for remote command host server.

    private byte[] qualifiedFileName_;
    private Integer databaseFileAttributes_;


    @Override
    public boolean canExecute() throws IOException, AS400SecurityException
    {
        // Ensure that we are connected to the server.
        fd_.connect();

        return canAccess(ACCESS_MODE_EXECUTE);
    }

    @Override
    public boolean canRead() throws IOException, AS400SecurityException
    {
        // Ensure that we are connected to the server.
        fd_.connect();

        int rc = fd_.checkAccess(IFSOpenReq.READ_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN);
        return (rc == IFSReturnCodeRep.SUCCESS);
        // Design note: The QlgAccess() API gives somewhat different results in certain scenarios.
        // Using IFSOpenReq appears to be a bit more "thorough".
    }

    @Override
    public boolean canWrite() throws IOException, AS400SecurityException
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
  private boolean canAccess(int accessMode) throws IOException, AS400SecurityException
  {
      // Assume that the caller has already connected to the server.

      if (fd_.getSystemVRM() < 0x00050100)
      {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Server is pre-V5R1, so canAccess() is returning false.");
          return false;
      }

      // We will call the QlgAccess API, to determine whether the current user can access the file in the specified
      // mode.
      // Note: According to the spec for QlgAccess: "If the [user profile] has *ALLOBJ special authority, access() will
      // indicate success for R_OK, W_OK, or X_OK even if none of the permission bits are set."
      // Note: QlgAccess() is only _conditionally_ threadsafe.

      boolean result;
      try
      {
          // Create the pgm call object
          if (rmtCmd_ == null)
              setupRemoteCommand();

          ProgramParameter[] parameters = new ProgramParameter[] {
                  // Parameter 1: Qlg_Path_Name_T *Path_Name (input) :
                  new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
                  // Parameter 2: int amode (input) :
                  new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(accessMode))
          };

          // Call the service program.
          byte[] returnedBytes = rmtCmd_.runServiceProgram("QSYS", "QP0LLIB1", "QlgAccess", parameters);
          if (returnedBytes == null)
          {
              Trace.log(Trace.ERROR, "Call to QlgAccess() returned null.");
              throw new AS400Exception(rmtCmd_.getMessageList());
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
                          if (Trace.traceOn_) 
                              Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgAccess() for file " 
                                                          + fd_.path_ + ". Assuming that the file does not exist.");
                          break;
                      case 3401:  // EACCES: "Permission denied."
                          // Assume that we got this error because we don't have the specified access.
                          if (Trace.traceOn_) 
                              Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgAccess() for file " 
                                                          + fd_.path_ + ". Assuming that the user does not have the specified access.");
                          break;
                          default:    // some other errno
                              Trace.log(Trace.ERROR, "Received errno "+errno+" from QlgAccess() for file " + fd_.path_);
                              
                              // Note: An ErrnoException might be more appropriate, but the ErrnoException constructor 
                              // requires an AS400 object, and we don't have one to give it.
                              throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR, errno);
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
      catch (AS400SecurityException|IOException e) {
          throw e;
      } catch (Exception e) {
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
  @Override
  public boolean setAccess(int accessType, boolean enableAccess, boolean ownerOnly) throws IOException, AS400SecurityException
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
          if (rmtCmd_ == null)
              setupRemoteCommand();

          // Get the current access modes, so that we can selectively turn on/off the desired bit(s).
          int oldAccessMode = getAccess();

          int bitMask = accessType << 6;  // for example: 0000400 == [ 'read' for owner ] 
          if (!ownerOnly) 
          {
              bitMask = bitMask | (accessType << 3) | accessType;
              // for example: 0000444 == [ 'read' for owner, group, and other ]
          }

          int newAccessMode;
          if (enableAccess)
              newAccessMode = oldAccessMode | bitMask;  // selectively turn bits _on_
          else {  
              // disable access
              newAccessMode = oldAccessMode & ~bitMask;  // selectively turn bits _off_
          }
          newAccessMode = newAccessMode & 0007777;  // QlgChmod can only set the low 12 bits

          ProgramParameter[] parameters = new ProgramParameter[] {
                  // Parameter 1: Qlg_Path_Name_T *Path_Name (input) :
                  new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
                  // Parameter 2: int amode (input) :
                  new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray(newAccessMode))
          };

          // Call the service program.
          byte[] returnedBytes = rmtCmd_.runServiceProgram("QSYS", "QP0LLIB1", "QlgChmod", parameters);
          if (returnedBytes == null)
          {
              Trace.log(Trace.ERROR, "Call to QlgChmod() returned null.");
              throw new AS400Exception(rmtCmd_.getMessageList());
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
      catch (AS400SecurityException | IOException e) {
          throw e;
      } catch (Exception e) {
          Trace.log(Trace.ERROR, "Error while determining accessibility of file.", e);
          throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
      }

      return true;
  }


  /**
   Calls QlgStat() to get status information about the file.
   If the file does not exist, throws an ObjectDoesNotExistException.
   Note: The QlgStat API was introduced in V5R1.  Do not call this method without checking VRM.
   **/
  private int getAccess() throws IOException, AS400SecurityException, ObjectDoesNotExistException
  {
      // Assume that the caller has already connected to the server.
      // Assume that the caller has already verified that the server is V5R1 or higher.

      // We will call the QlgStat API, to get status information about the file.
      // Note: QlgStat() is only _conditionally_ threadsafe.

      int statInfo = 0;
      try
      {
          // Create the pgm call object
          if (rmtCmd_ == null)
              setupRemoteCommand();

          int bufferSizeProvided = 128;  // large enough to accommodate a 'stat' structure

          ProgramParameter[] parameters = new ProgramParameter[] {
                  // Parameter 1: Qlg_Path_Name_T *Path_Name (input) :
                  new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, createPathName()),
                  // Parameter 2: struct stat *buf (output) :
                  new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, bufferSizeProvided)
          };

          // Call the service program.
          byte[] returnedBytes = rmtCmd_.runServiceProgram("QSYS", "QP0LLIB1", "QlgStat", parameters);
          if (returnedBytes == null)
          {
              Trace.log(Trace.ERROR, "Call to QlgStat() returned null.");
              throw new AS400Exception(rmtCmd_.getMessageList());
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
      catch (AS400SecurityException|IOException e) {
          throw e;
      }
      catch (Exception e) {
          Trace.log(Trace.ERROR, "Error while determining accessibility of file.", e);
          throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
      }

      return statInfo;
  }

  @Override
  public void clearCachedAttributes()
  {
      attributesReply_ = null;
      databaseFileAttributes_ = null;
  }

  @Override
  public boolean copyTo(String destinationPath, boolean replace) throws IOException, AS400SecurityException, ObjectAlreadyExistsException
  {
      fd_.connect();
      if (Trace.traceOn_ && replace==false && fd_.getSystemVRM() < 0x00050300)
          Trace.log(Trace.WARNING, "Server is V5R2 or lower, so the 'Do not replace' argument will be ignored.");

      // If the source is a directory, verify that the destination doesn't already exist.
      if (isDirectory() == IFSReturnCodeRep.SUCCESS && exists(destinationPath) == IFSReturnCodeRep.SUCCESS)
          throw new ObjectAlreadyExistsException(destinationPath, ObjectAlreadyExistsException.OBJECT_ALREADY_EXISTS);

      return fd_.copyTo(destinationPath, replace);
  }


  @Override
  public long created() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      long creationDate = 0L;

      // Attempt to list the attributes of the specified file.
      // Note: Do not use cached attributes, since they may be out of date.
      IFSListAttrsRep attrs = getAttributeSetFromServer(fd_.path_);
      if (attrs != null) {
          attributesReply_ = attrs;
          creationDate = attrs.getCreationDate();
      }

      return creationDate;
  }

  @Override
  public int createNewFile() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      return (fd_.checkAccess(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_CREATE_FAIL));
  }

  @Override
  public int delete() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      // Convert the path name to the server CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      // Determine if this is a file or directory and instantiate the
      // appropriate type of delete request.
      IFSDataStreamReq req = new IFSDeleteFileReq(pathname, fd_.preferredServerCCSID_);
      try
      {
          if (!isSymbolicLink() && isDirectory() == IFSReturnCodeRep.SUCCESS )
              req = new IFSDeleteDirReq(pathname, fd_.preferredServerCCSID_);
      }
      catch (Exception e)
      {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unable to determine if file or directory.\n" + e.toString());
      
          // If the server has been disconnected, go ahead and throw an exception
          // to avoid the null pointer exception from fs_.server_
          if (fd_.server_ == null)
          {
              if (e instanceof IOException)
                  throw (IOException) e;
              else
                  throw new IOException(e);
          }
      }

      // Delete this entry.
      ClientAccessDataStream ds = null;
      try {
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
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      // Verify that the request was successful.
      int rc = 0;
      if (ds instanceof IFSReturnCodeRep)
      {
          rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc != IFSReturnCodeRep.SUCCESS) {
              if (Trace.traceOn_) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
          }
      } 
      else 
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, Integer.toHexString(ds.getReqRepID()), null);
      }

      // Clear any cached file attributes.
      attributesReply_ = null;

      return (rc);
  }

  /**
   Determines if a file is a directory without a call to the server.
   **/
  private boolean determineIsDirectory(IFSListAttrsRep attributeList)
  {
      boolean answer = false;
      // Determine if the file attributes indicate a directory.
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

  /**
   Determines if a file is a file without a call to the server.
   **/
  private boolean determineIsFile(IFSListAttrsRep attributeList)
  {
      boolean answer = false;
      // Determine if the file attributes indicate a file.
      switch (attributeList.getObjectType())
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

  @Override
  public int exists() throws IOException, AS400SecurityException {
      return exists(fd_.path_);
  }

  /**
   Determines if the integrated file system object represented by this object exists.
   **/
  private int exists(String name) throws IOException, AS400SecurityException
  {
      int returnCode = IFSReturnCodeRep.SUCCESS;

      // Ensure that we are connected to the server.
      fd_.connect();

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
      catch (AS400SecurityException e) {
          returnCode = IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY;
          // Note: This is consistent with the behavior of java.io.File on IBM JVMs.
          // On IBM i, java.io.File.exists() returns false if the profile is denied access to the file being queried.
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
  String getAbsolutePath() {
      return fd_.path_;
  }

  /**
   Get a list attribute reply from the server for a single entity (get the attributes
   of a specific object, not the attributes of every file in a directory).
  **/
  private IFSListAttrsRep getAttributeSetFromServer(String filePath) throws IOException, AS400SecurityException
  {
      IFSListAttrsRep reply = null;

      // Attempt to list the attributes of the specified file.
      Vector replys = listAttributes(filePath, NO_MAX_GET_COUNT, null, NO_RESTART_NAME, !SORT_LIST);
      // Note: This does setFD() on each returned IFSListAttrsRep.

      // If this is a directory then there must be exactly one reply.
      if (replys != null && replys.size() == 1)
          reply = (IFSListAttrsRep) replys.elementAt(0);

      return reply;
  }

  @Override
  public int getCCSID(boolean retrieveAll) throws IOException, AS400SecurityException {
      return fd_.getCCSID(retrieveAll);
  }

  @Override
  public int getCCSID() throws IOException, AS400SecurityException {
      return fd_.getCCSID();
  }

  private static final boolean SPACE_AVAILABLE  = true;
  private static final boolean SPACE_TOTAL      = false;

  @Override
  public long getAvailableSpace(boolean forUserOnly) throws IOException, AS400SecurityException
  {
      long spaceAvailable = getAmountOfSpace(forUserOnly, SPACE_AVAILABLE);

      // Design note: When querying the space available for a specific user,
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

  @Override
  public long getTotalSpace(boolean forUserOnly) throws IOException, AS400SecurityException {
      return getAmountOfSpace(forUserOnly, SPACE_TOTAL);
  }

  /**
   Returns the amount of storage space.
   @param forUserOnly Whether to report only the space for the user. If false, report space in entire file system.
   @param availableSpaceOnly Whether to report only the space available. If false, report total space, rather than just available space.
   @return The number of bytes of storage.
   Returns special value Long.MAX_VALUE if the system reports "no maximum".
   **/
  private long getAmountOfSpace(boolean forUserOnly, boolean availableSpaceOnly) throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      long amountOfSpace = 0L;
      int directoryHandle;
      ClientAccessDataStream ds = null;
      int rc = 0;

      if (forUserOnly) // prepare to get space info for specific user
      {
          // Special value for file handle, indicating that space attributes for the user should be retrieved, rather
          // than space attributes of the file system.
          // According to the PWSI Datastream Spec:
          // "When the client sends 0 as the working directory handle ... the space characteristics of the user are
          // returned instead of the characteristics of the system."
          directoryHandle = 0;
      }
      else // prepare to get space info for the entire file system
      {
          // To query the file system, we need to specify a "working directory handle" to the directory. So first, get a
          // handle.
          String path = fd_.path_;
          if (isDirectory() != IFSReturnCodeRep.SUCCESS)
              path = IFSFile.getParent(fd_.path_);

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
              InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
              throwException.initCause(e);
              throw throwException;
          }

          // Verify that we got a handle back.
          rc = 0;
          if (ds instanceof IFSCreateDirHandleRep)
              directoryHandle = ((IFSCreateDirHandleRep) ds).getHandle();
          else if (ds instanceof IFSReturnCodeRep)
          {
              rc = ((IFSReturnCodeRep) ds).getReturnCode();
              if (rc != IFSReturnCodeRep.SUCCESS) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
              throw new ExtendedIOException(fd_.path_, rc);
          }
          else
          {
              Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
              throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
          }
      }

      // Query the amount of space.
      ds = null;
      try
      {
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
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      // Verify the reply.
      rc = 0;
      if (ds instanceof IFSQuerySpaceRep)
          amountOfSpace = (availableSpaceOnly) ? ((IFSQuerySpaceRep) ds).getSpaceAvailable() : ((IFSQuerySpaceRep) ds).getTotalSpace();
      else if (ds instanceof IFSReturnCodeRep)
      {
          rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc != IFSReturnCodeRep.SUCCESS) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
          throw new ExtendedIOException(fd_.path_, rc);
      }
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, Integer.toHexString(ds.getReqRepID()), null);
      }

      return amountOfSpace;
  }

  @Override
  public String getFileSystemType() throws IOException, AS400SecurityException {
      return fd_.getFileSystemType();
  }

  @Override
  public String getOwnerName(boolean retrieveAll) throws IOException, AS400SecurityException
  {
      // Design note: This method demonstrates how to get attributes that are returned in the OA1* structure (as opposed to the OA2).
      String ownerName = null;

      fd_.connect();
      
      // The 'owner name' field is in the OA1 structure; the flag is in the first Flags() field.
      try
      {
          IFSListAttrsRep reply = null;
          if (retrieveAll)
              return fd_.getOwnerName();
          else
              reply = fd_.listObjAttrs1(IFSObjAttrs1.OWNER_NAME_FLAG, 0);

          if (reply != null)
              ownerName = reply.getOwnerName(fd_.system_.getCcsid());
          else 
          {
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "getOwnerName: " + "IFSReturnCodeRep return code", fd_.errorRC_);
              
              if (fd_.errorRC_ == IFSReturnCodeRep.FILE_NOT_FOUND || fd_.errorRC_ == IFSReturnCodeRep.PATH_NOT_FOUND)
                  throw new ExtendedIOException(fd_.path_, ExtendedIOException.PATH_NOT_FOUND);
          }
      } 
      catch (ExtendedIOException e)
      {
          if (e.getReturnCode() == ExtendedIOException.DIR_ENTRY_EXISTS) {
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unable to determine owner of directory.", e);
          } 
          else
              throw e;
      }

      return (ownerName == null ? "" : ownerName);
  }

@Override
public String getOwnerName() throws IOException, AS400SecurityException
{
    // Design note: This method demonstrates how to get attributes that are returned in the OA1* structure (as opposed to the OA2).
    String ownerName = null;

    fd_.connect();
    
    // The 'owner name' field is in the OA1 structure; the flag is in the first Flags() field.
    try
    {
        IFSListAttrsRep reply = fd_.listObjAttrs1(IFSObjAttrs1.OWNER_NAME_FLAG, 0);
        if (reply != null)
            ownerName = reply.getOwnerName(fd_.system_.getCcsid());
        else
        {
            if (Trace.traceOn_) Trace.log(Trace.WARNING, "getOwnerName: IFSReturnCodeRep return code", fd_.errorRC_);
        
            if (fd_.errorRC_ == IFSReturnCodeRep.FILE_NOT_FOUND || fd_.errorRC_ == IFSReturnCodeRep.PATH_NOT_FOUND)
                throw new ExtendedIOException(fd_.path_, ExtendedIOException.PATH_NOT_FOUND);
        }
    }
    catch (ExtendedIOException e)
    {
        if (e.getReturnCode() == ExtendedIOException.DIR_ENTRY_EXISTS) {
            if (Trace.traceOn_) Trace.log(Trace.WARNING, "Unable to determine owner of directory.", e);
        }
        else 
            throw e;
    }

    return (ownerName == null ? "" : ownerName);
}
  
 @Override
 public String getOwnerNameByUserHandle(boolean forceRetrieve) throws IOException, AS400SecurityException {
     return fd_.getOwnerNameByUserHandle(forceRetrieve);
 }

 @Override
 public int getASP() throws IOException, AS400SecurityException {
     return fd_.getASP();
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



  @Override
  public long getOwnerUID() throws IOException, AS400SecurityException
  {
      fd_.connect();
      IFSListAttrsRep reply = fd_.listObjAttrs2();
      
      return (reply != null) ? reply.getOwnerUID() : -1;
  }

  @Override
  public String getPathPointedTo() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      // We will call the QlgReadlink API, to determine the path of the immediately pointed-to file. Note that
      // QlgReadlink is only _conditionally_ threadsafe.

      String resolvedPathname = null;
      try
      {
          // Create the pgm call object
          if (rmtCmd_ == null)
              setupRemoteCommand();
          
          int bufferSizeProvided = 1024;  // large enough for most anticipated paths

          ProgramParameter[] parameters = new ProgramParameter[] {
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
              byte[] returnedBytes = rmtCmd_.runServiceProgram("QSYS", "QP0LLIB1", "QlgReadlink", parameters);
              if (returnedBytes == null)
              {
                  Trace.log(Trace.ERROR, "Call to QlgReadlink() returned null.");
                  throw new AS400Exception(rmtCmd_.getMessageList());
              }

              int returnValue = BinaryConverter.byteArrayToInt(returnedBytes, 0);

              if (returnValue == -1)  // this indicates that we got an "errno" back
              {
                  int errno = BinaryConverter.byteArrayToInt(returnedBytes, 4);
                  switch (errno)
                  {
                  case 3021:  // EINVAL: "The value specified for the argument is not correct."
                      // Assume that we got this error because the file isn't a symlink.
                      if (Trace.traceOn_) 
                          Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgReadlink() for file " + fd_.path_ + ". Assuming that the file is not a symbolic link.");
                      break;
                  case 3025:  // ENOENT: "No such path or directory."
                      // Assume that we got this error because the file isn't a symlink.
                      if (Trace.traceOn_) 
                          Trace.log(Trace.DIAGNOSTIC, "Received errno "+errno+" from QlgReadlink() for file " + fd_.path_ + ". Assuming that the file does not exist.");
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
                  if (Trace.traceOn_) 
                      Trace.log(Trace.DIAGNOSTIC, "QlgReadlink() buffer too small. Buffer size provided: " + bufferSizeProvided + ". Buffer size needed: " + bufferSizeNeeded+". Calling QlgReadLink() with larger buffer.");
          
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
      }
      catch (AS400SecurityException|IOException e) {
          throw e;
      }
      catch (Exception e)
      {
          Trace.log(Trace.ERROR, "Error while resolving symbolic link.", e);
          throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
      }

      return resolvedPathname;
  }


//
// Path name structure for QlgAccess():
// 
//  0 INPUT     BINARY(4)  CCSID
//  4 INPUT     CHAR(2)    Country or region ID
//  6 INPUT     CHAR(3)    Language ID
//  9 INPUT     CHAR(3)    Reserved
// 12 INPUT     BINARY(4)  Path type indicator
// 16 INPUT     BINARY(4)  Length of path name
// 20 INPUT     CHAR(2)    Path name delimiter character
// 22 INPUT     CHAR(10)   Reserved
// 32 INPUT     CHAR(*)    Path name
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
      int pathLength = fd_.path_.length(); // number of Unicode chars

      byte[] buf = new byte[32 + pathLength * 2]; // 2 bytes per Unicode char
      BinaryConverter.intToByteArray(1200, buf, 0); // CCSID
      BinaryConverter.intToByteArray(2, buf, 12); // path type indicator
      BinaryConverter.intToByteArray(pathLength * 2, buf, 16); // length of path name (#bytes)
      conv.stringToByteArray("/", buf, 20, 2); // path name delimiter
      conv.stringToByteArray(fd_.path_, buf, 32); // path name
      
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
          // System.out.println("Country: " + conv.byteArrayToString(buf, offset, 2));
          offset += 2;
          // System.out.println("LangID: " + conv.byteArrayToString(buf, offset, 3));
          offset += 3;
          offset += 3; // reserved field
          System.out.println("Path type: " + BinaryConverter.byteArrayToInt(buf, offset));
          offset += 4;
          nameLength = BinaryConverter.byteArrayToInt(buf, offset);
          System.out.println("Path name length: " + nameLength);
          offset += 4;
          System.out.println("Delimiter: " + conv.byteArrayToString(buf, offset, 2));
          offset += 2;
          offset += 10; // reserved field
      }
      else 
      {
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


  @Override
  public String getSubtype() throws IOException, AS400SecurityException
  {
      String subtype = "";

      // Ensure that we are connected to the server.
      fd_.connect();
    
      // Convert the path name to the server CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

      boolean needCodePage = (fd_.getSystemVRM() >= 0x00060100 && fd_.path_.indexOf("/QSYS.LIB") != -1);

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
                                                null, false, eaNamesList, eaNameBytesLength, false, fd_.patternMatching_);

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
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received multiple replies from listAttributes(req) (" + replys.size() + ")");
          }
          
          IFSListAttrsRep reply = (IFSListAttrsRep)replys.elementAt(0);
          Hashtable extendedAttributes = reply.getExtendedAttributeValues();
          byte[] subtypeAsBytes = (byte[])extendedAttributes.get(".TYPE");
          
          if (subtypeAsBytes != null)
          {
              int ccsid;
              if (!needCodePage)
                  ccsid = 37; // the returned bytes are in EBCDIC
              else 
              {
                  // Get the ".CODEPAGE" extended attribute value from the reply.
                  byte[] codepageAsBytes = (byte[])extendedAttributes.get(".CODEPAGE");
                  // The .CODEPAGE attribute is returned as 2 bytes in little-endian format.
                  // Therefore we need to swap the bytes.
                  byte[] swappedBytes = new byte[2];  // the codepage is returned in 2 bytes
                  swappedBytes[0] = codepageAsBytes[1];
                  swappedBytes[1] = codepageAsBytes[0];
                  ccsid = BinaryConverter.byteArrayToUnsignedShort(swappedBytes,0);
                  if (ccsid == 1400) 
                      ccsid = 1200;  // codepage 1400 corresponds to CCSID 1200
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

  @Override
  public boolean isSourcePhysicalFile() throws IOException, AS400SecurityException, AS400Exception
  {
      // Assume that the caller has verified that the file is a Physical File in QSYS.

      // Layout of first 2 attribute bytes returned in FILD0100 format:
      //
      // BIT(2): Reserved.
      // BIT(1): Type of file. If on, the file is a logical database file.
      // If off, a physical database file.
      // BIT(1): Reserved.
      // BIT(1): File type (FILETYPE). If on, the file is a source file (*SRC).
      // If off, a data file (*DATA).
      // BIT(1): Reserved.
      // BIT(1): Access path. If on, the file has a keyed sequence access path.
      // If off, an arrival sequence access path.
      // BIT(1): Reserved.
      // BIT(1): Record format level check (LVLCHK).
      // If on, the record format level identifiers are checked when the file is opened (*YES).
      // If off, they are not checked when the file is opened (*NO).
      // BIT(1): Select/omit. If on, the file is a select/omit logical file.
      // BIT(4): Reserved.
      // BIT(1): Double-byte character set (DBCS) or Graphic data.
      // If on, the file's record format(s) contains DBCS or Graphic data fields.
      // BIT(1): Double-byte character set (DBCS) or Graphic literals.
      // If on, the file's record format(s) contains DBCS or Graphic literals.
      //

      // Examine the FILETYPE bit (the 12th bit from the right).
      // If the bit is on, that indicates the file is a source file (*SRC).
      // If the bit is off, that indicates the file is a data file (*DATA).
      int attributeFlags = retrieveDatabaseFileAttributes();
      return ((attributeFlags & 0x00000800) != 0);  // 12th bit from the right
  }

  /**
   Call QDBRTVFD (if necessary) to get additional status information about the file.
   If the file does not exist, throws an ObjectDoesNotExistException.
   **/
  private int retrieveDatabaseFileAttributes() throws IOException, AS400SecurityException, AS400Exception
  {
      if (databaseFileAttributes_ == null)
      {
          try
          {
              int bufferSizeProvided = 400; // the FILD0100-format structure occupies 400 bytes
              ProgramParameter[] parameters = new ProgramParameter[] {
                      // Receiver variable, Output, Char(*)
                      new ProgramParameter(bufferSizeProvided),
                      // Length of receiver variable, Input, Binary(4)
                      new ProgramParameter(BinaryConverter.intToByteArray(bufferSizeProvided)),
                      // Qualified returned file name, Output, Char(20)
                      new ProgramParameter(20),
                      // Format name, Input, Char(8) : EBCDIC 'FILD0100'
                      new ProgramParameter(new byte[] { (byte)0xC6, (byte)0xC9, (byte)0xD3, (byte)0xC4, (byte)0xF0, (byte)0xF1, (byte)0xF0, (byte)0xF0 } ),
                      // Qualified file name, Input, Char(20) : fileName + libraryName
                      new ProgramParameter(composeQualifiedNameBytes()),
                      // Record format name, Input, Char(10) : 10 EBCDIC blanks
                      new ProgramParameter(new byte[] { (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 } ),
                      // Override processing, Input, Char(1) : EBCDIC '0' (no override processing)
                      new ProgramParameter(new byte[] { (byte)0xF0 } ),
                      // System, Input, Char(10) : EBCDIC '*LCL' (local files only)
                      new ProgramParameter(new byte[] { (byte)0x5C, (byte)0xD3, (byte)0xC3, (byte)0xD3, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 } ),
                      // Format type, Input, Char(10) : EBCDIC '*INT'
                      new ProgramParameter(new byte[] { (byte)0x5C, (byte)0xC9, (byte)0xD5, (byte)0xE3, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 } ),
                      // Error Code, I/O, Char(*)
                      new ErrorCodeParameter()
              };

              // Create the pgm call object
              if (rmtCmd_ == null)
                  setupRemoteCommand();

              if (!rmtCmd_.runProgram("QSYS", "QDBRTVFD", parameters)) // conditionally threadsafe
                  throw new AS400Exception(rmtCmd_.getMessageList());

              byte[] outputData = parameters[0].getOutputData();
              int bytesReturned = BinaryConverter.byteArrayToInt(outputData, 0);
              if (bytesReturned < 10)
              {
                  Trace.log(Trace.ERROR, "Insufficient output bytes returned from QDBRTVFD: " + bytesReturned);
                  throw new InternalErrorException(fd_.path_, InternalErrorException.UNKNOWN);
              }

               // Grab the "attribute bytes". These are the 2 bytes starting at offset 8.
              databaseFileAttributes_ = Integer.valueOf(BinaryConverter.byteArrayToUnsignedShort(outputData, 8));
          }
          catch (AS400Exception|AS400SecurityException|IOException e) {
              throw e;
          }
          catch (Exception e) {
              Trace.log(Trace.ERROR, "Error while retrieving database file attributes.", e);
              throw new ExtendedIOException(fd_.path_, ExtendedIOException.UNKNOWN_ERROR);
          }
      }

      return databaseFileAttributes_.intValue();
  }

  // Setup qualified file name program parameter object on first touch.  Synchronized to protect instance variables.  
  // This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
  private synchronized byte[] composeQualifiedNameBytes() throws IOException
  {
      // If not already setup.
      if (qualifiedFileName_ == null)
      {
          CharConverter converter = new CharConverter(37); // converts Unicode chars to EBCDIC bytes

          // Start with 20 EBCDIC spaces.
          qualifiedFileName_ = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };

          // Parse out the library and filename from the path.
          QSYSObjectPathName qsysPath = new QSYSObjectPathName(fd_.path_);
          String libraryName = qsysPath.getLibraryName();
          String fileName    = qsysPath.getObjectName();

          // Put the converted file name at the beginning of the array.
          converter.stringToByteArray(fileName, qualifiedFileName_, 0);
          // Put the converted library name at position ten.
          converter.stringToByteArray(libraryName, qualifiedFileName_, 10);
      }

      return qualifiedFileName_;
  }

  @Override
  public int isDirectory() throws IOException, AS400SecurityException 
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

      if (attributesReply_ == null)
          attributesReply_ = getAttributeSetFromServer(fd_.path_);

      if (attributesReply_ != null)
          if (determineIsDirectory(attributesReply_))
              returnCode = IFSReturnCodeRep.SUCCESS;

      return returnCode;
  }

  @Override
  public int isFile() throws IOException, AS400SecurityException 
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

      if (attributesReply_ == null)
          attributesReply_ = getAttributeSetFromServer(fd_.path_);

      if (attributesReply_ != null)
          if (determineIsFile(attributesReply_))
              returnCode = IFSReturnCodeRep.SUCCESS;

      return returnCode;
  }

  @Override
  public boolean isHidden() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      boolean result = false;

      // Attempt to get the attributes of this object if we do not have them.
      if (attributesReply_ == null)
          attributesReply_ = getAttributeSetFromServer(fd_.path_);

      // Determine if the file attributes indicate hidden.
      if (attributesReply_ != null)
          result = (attributesReply_.getFixedAttributes() & IFSListAttrsRep.FA_HIDDEN) != 0;

      return result;
  }
  
  private boolean isInQsys() 
  {
      // TODO This code is flawed, since it does not take into account iASP paths. Needs to be corrected.
      
      String lowercasePath = fd_.path_.toLowerCase().replaceAll("//+", "/");
      return lowercasePath.equals("/qsys.lib") || lowercasePath.startsWith("/qsys.lib/");
  }

  @Override
  public boolean isReadOnly() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      boolean result = false;

      // Attempt to get the attributes of this object if we do not have them.
      if (attributesReply_ == null)
          attributesReply_ = getAttributeSetFromServer(fd_.path_);

      // Determine if the file attributes indicate hidden.
      if (attributesReply_ != null)
          result = (attributesReply_.getFixedAttributes() & IFSListAttrsRep.FA_READONLY) != 0;

      return result;
  }

  @Override
  public boolean isSymbolicLink() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      if (fd_.getSystemVRM() < 0x00050300)
      {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Server is V5R2 or lower, so isSymbolicLink() will always report false.");
          return false;
      }

      if (isSymbolicLink_ == null)
      {
          // QSYS doesn't support symbolic links, so no need to check
          if(isInQsys()) 
          {
              isSymbolicLink_ = false;
              return isSymbolicLink_;
          }
          
          // Note: As of V5R3, we can't get accurate symbolic link info by querying the attrs of a specific file.
          // Instead, we must query the contents of the parent directory.
          int pathLen = fd_.path_.length();
          if (pathLen <= 1)
          {
              if (Trace.traceOn_) 
                  Trace.log(Trace.DIAGNOSTIC, "Path length is less than 2, so assuming not a symbolic link: " + fd_.path_);
              
              isSymbolicLink_ = false;
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
                  if (attrList[i].name_.equals(filename))
                      attrs = attrList[i];
              }
              
              if (attrs != null)
                  isSymbolicLink_ = attrs.isSymbolicLink_;
              else
              {
                  if (Trace.traceOn_) 
                      Trace.log(Trace.ERROR, "Received zero matches from listDirectoryDetails() against parent of " + wildCardPattern);
          
                  isSymbolicLink_ = false;
              }
          }
      }

      return isSymbolicLink_;
  }

  @Override
  public long lastAccessed() throws IOException, AS400SecurityException
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

  @Override
  public long lastModified() throws IOException, AS400SecurityException
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

  @Override
  public long length() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      long size = 0L;

      if (fd_.getSystemVRM() >> 8 != 0x00000502)
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
      else // the system is V5R2 (and therefore, datastream level is 3)
      {
          // Convert the path name to the server CCSID.
          byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

          // Send the List Attributes request. Indicate that we want the "8-byte file size".
          IFSListAttrsReq req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,
                                                    IFSListAttrsReq.NO_AUTHORITY_REQUIRED, NO_MAX_GET_COUNT, 
                                                    null, true, null, 0, true, fd_.patternMatching_);
          Vector replys = fd_.listAttributes(req);

          if (replys == null)
          {
              if (fd_.errorRC_ != 0)
                  throw new ExtendedIOException(fd_.path_, fd_.errorRC_);
              else
                  throw new InternalErrorException(InternalErrorException.UNKNOWN);
          } 
          else if (replys.size() == 0)
          {
              // Assume this simply indicates that the file does not exist.
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received no replies from listAttributes(req).");
          } 
          else 
          {
              if (replys.size() > 1 && Trace.traceOn_)
                  Trace.log(Trace.WARNING, "Received multiple replies from listAttributes(req) (" + replys.size() + ")");

              IFSListAttrsRep reply = (IFSListAttrsRep) replys.elementAt(0); // use first reply
              size = reply.getSize8Bytes(/* fd_.serverDatastreamLevel_ */);
          }
      }
      
      return size;
  }

  // Fetch list attributes reply(s) for the specified path.
  private Vector listAttributes(String path, int maxGetCount, byte[] restartNameOrID, boolean isRestartName, boolean sortList)
    throws IOException, AS400SecurityException
  {
      // Assume connect() has already been done.
      
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "list attributes for: " + path);

      // Convert the pathname to the server CCSID.
      byte[] pathname = fd_.converter_.stringToByteArray(path);

      // Prepare the 'list attributes' request.
      IFSListAttrsReq req = new IFSListAttrsReq(pathname, fd_.preferredServerCCSID_,   
                                              IFSListAttrsReq.NO_AUTHORITY_REQUIRED, maxGetCount, 
                                              restartNameOrID, isRestartName,  null, 0, false, fd_.patternMatching_);
    
      if (sortList) 
          req.setSorted(true);
    
      return fd_.listAttributes(req);  // Note: This does setFD() on each returned IFSListAttrsRep..
  }


  @Override
  public String[] listDirectoryContents(String directoryPath) throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      Vector replys = listAttributes(directoryPath, NO_MAX_GET_COUNT, null, NO_RESTART_NAME, sortLists_);
      String[] names = null;

      // Add the name for each file or directory in the specified directory,
      // to the array of names.

      // Changed the behavior of the list() to conform to that of the JDK1.1.x
      // so that a NULL is returned if and only if the directory or file represented
      // by this IFSFile object doesn't exist.
      if (replys != null)
      {
          names = new String[replys.size()];
          int j = 0;
          for (int i = 0; i < replys.size(); i++)
          {
              IFSListAttrsRep reply = (IFSListAttrsRep) replys.elementAt(i);
              String name = fd_.converter_.byteArrayToString(reply.getName(/* dsl */));
              
              if (!(name.equals(".") || name.equals("..")))
                  names[j++] = name;
          }

          if (j == 0) 
              names = new String[0];
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

  // Added this function to support caching attributes.
  // List the files/directories details in the specified directory.
  // Returns null if specified file or directory does not exist.
  private IFSCachedAttributes[] listDirectoryDetails(String pathPattern, String directoryPath,
                                                    int maxGetCount,  byte[] restartNameOrID, boolean isRestartName,
                                                    boolean sortList) throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      IFSCachedAttributes[] fileAttributes = null;

      try
      {
          // Design note: Due to a limitation in the File Server, if we specify a "filename pattern", we cannot get OA1
          // or OA2 structures in the reply.
          // Only "handle-based" requests can get OA* structures in the reply; and a handls is specific to a single
          // file.
          // This prevents us, for example, from obtaining the "name of file owner" values for an entire list of files
          // at once; rather, we must obtain that attribute one file at a time.
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
                      // to a non-existent object). Such a link cannot
                      // be resolved and both determineIsDirectory and
                      // determineIsFile will return false. Regular symbolic links
                      // will resolve. For example, a symbolic link to a file will return
                      // true from isFile and false from determineIsDirectory.
                      boolean isDirectory = determineIsDirectory(reply);
                      boolean isFile = determineIsFile(reply);
                      IFSCachedAttributes attributes = new IFSCachedAttributes(reply.getAccessDate(),
                            reply.getCreationDate(), reply.getFixedAttributes(), reply.getModificationDate(),
                            reply.getObjectType(), reply.getSize(dsl), name, directoryPath, isDirectory, isFile, 
                            reply.getRestartID(), reply.isSymbolicLink(dsl), reply.getFileSystemType(dsl));
                      fileAttributes[j++] = attributes;
                  }
              }
          }

          if (j == 0)
              fileAttributes = new IFSCachedAttributes[0];
          else if (fileAttributes.length != j)
          {
              //Copy the attributes to an array of the exact size.
              IFSCachedAttributes[] newFileAttributes = new IFSCachedAttributes[j];
              System.arraycopy(fileAttributes, 0, newFileAttributes, 0, j);
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

  @Override
  public IFSCachedAttributes[] listDirectoryDetails(String pathPattern, String directoryPath,
                                                    int maxGetCount, String restartName) throws IOException, AS400SecurityException
  {
      byte[] restartNameBytes = fd_.converter_.stringToByteArray(restartName);
      return listDirectoryDetails(pathPattern, directoryPath, maxGetCount, restartNameBytes, IS_RESTART_NAME, sortLists_);                                        //@D7C
  }


  @Override
  public IFSCachedAttributes[] listDirectoryDetails(String pathPattern, String directoryPath,
                                                    int maxGetCount, byte[] restartID,
                                                    boolean allowSortedRequests) throws IOException, AS400SecurityException
  {
      boolean sortParameter = (allowSortedRequests ? sortLists_ : false);
      return listDirectoryDetails(pathPattern, directoryPath, maxGetCount, restartID, !IS_RESTART_NAME, sortParameter);
  }

  @Override
  public int mkdir(String directory) throws IOException, AS400SecurityException
  {
      // Ensure that the path name is set.
      fd_.connect();

      int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;

      try
      {
          // Convert the directory name to the server CCSID.
          byte[] pathname = fd_.converter_.stringToByteArray(directory);

          // Send a create directory request.
          IFSCreateDirReq req = new IFSCreateDirReq(pathname, fd_.preferredServerCCSID_);
          ClientAccessDataStream ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);

          // Verify the reply.
          if (ds instanceof IFSReturnCodeRep)
          {
              returnCode = ((IFSReturnCodeRep) ds).getReturnCode();
              if (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY
                      || returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
                  throw new AS400SecurityException(directory, AS400SecurityException.DIRECTORY_ENTRY_ACCESS_DENIED);
          } 
          else 
          {
              Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
              throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
          }
      } 
      catch (ConnectionDroppedException e)
      {
          Trace.log(Trace.ERROR, "Byte stream server connection lost.");
          fd_.connectionDropped(e);
      } 
      catch (InterruptedException e)
      {
          Trace.log(Trace.ERROR, "Interrupted");
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      return returnCode;
  }

  @Override
  public int mkdirs() throws IOException, AS400SecurityException
  {
      // Ensure that we are connected to the server.
      fd_.connect();

      // Traverse up the parent directories until the first parent
      // directory that exists is found, saving each parent directory
      // as we go.
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
      } 
      else
          returnCode = IFSReturnCodeRep.DUPLICATE_DIR_ENTRY_NAME;

      // Create each parent directory in the reverse order that
      // they were saved.
      for (int i = nonexistentDirs.size(); i > 0; i--)
      {
          directory = (String) nonexistentDirs.elementAt(i - 1);

          // Create the next directory.
          returnCode = mkdir(directory);
          if (returnCode != IFSReturnCodeRep.SUCCESS)
          {
              // Unable to create a directory.
              break;
          }
      }

      return returnCode;
  }

  @Override
  public int renameTo(IFSFileImpl file) throws IOException, AS400SecurityException
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
          IFSRenameReq req = new IFSRenameReq(oldName, newName, fd_.preferredServerCCSID_, false);
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
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
      // Verify the reply.
      if (ds instanceof IFSReturnCodeRep)
      {
          returnCode = ((IFSReturnCodeRep) ds).getReturnCode();
          if (returnCode == IFSReturnCodeRep.SUCCESS)
              success = true;
          else if (Trace.traceOn_)
              Trace.log(Trace.ERROR, "Error renaming file: IFSReturnCodeRep return code", returnCode);
      }
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, Integer.toHexString(ds.getReqRepID()), null);
      }

      if (success)
      {
          fd_.path_ = otherFile.getAbsolutePath();

          // Clear any cached attributes.
          attributesReply_ = null;
      }

      return returnCode;
  }



  @Override
  public boolean setCCSID(int ccsid) throws IOException, AS400SecurityException
  {
      // To change the file data CCSID, we need to get the file's current attributes (in an OA2 structure), reset the
      // 'CCSID' field, and then send back the modified OA2 struct in a Change Attributes request.

      fd_.connect();
      IFSListAttrsRep reply = fd_.listObjAttrs2(); // get current attributes (OA2 structure)
      if (reply == null)
      {
          if (fd_.errorRC_ != 0)
              throw new ExtendedIOException(fd_.path_, fd_.errorRC_);
          else
              throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }

      boolean success = false;
      byte[] objAttrs2Null = new byte[reply.getObjAttrs2().getLength()];
      Arrays.fill(objAttrs2Null, (byte) 0xFF);
      IFSObjAttrs2 objAttrs = new IFSObjAttrs2(objAttrs2Null);

      // Sanity-check the length: If it's an OA2a or OA2b, the length will be 144 bytes. If it's an OA2c, the length
      // will be 160 bytes.
      if (Trace.traceOn_)
          Trace.log(Trace.DIAGNOSTIC, "Length of returned OA2* structure (should be 144 or 160): " + objAttrs.length());

      // Reset the "CCSID of the object" field in the OA2* structure.
      objAttrs.setCCSID(ccsid, fd_.serverDatastreamLevel_);

      // Issue a change attributes request.
      ClientAccessDataStream ds = null;
      try 
      {
          // Convert the path name to the server CCSID.
          byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

          IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname, fd_.preferredServerCCSID_, objAttrs, fd_.serverDatastreamLevel_);
          ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
      }
      catch (ConnectionDroppedException e) {
          Trace.log(Trace.ERROR, "Byte stream server connection lost.");
          fd_.connectionDropped(e);
      }
      catch (InterruptedException e)
      {
          Trace.log(Trace.ERROR, "Interrupted");
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      if (ds instanceof IFSReturnCodeRep)
      {
          int rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc == IFSReturnCodeRep.SUCCESS)
          {
              success = true;
              fd_.setCCSID(ccsid); // update the cached CCSID value in the fd_
          } 
          else if (Trace.traceOn_)
              Trace.log(Trace.ERROR, "Error setting file data CCSID: IFSReturnCodeRep return code", rc);
      } 
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, Integer.toHexString(ds.getReqRepID()), null);
      }

      return success;
  }

  @Override
  public boolean setFixedAttributes(int attributes) throws IOException, AS400SecurityException
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

          IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname, fd_.preferredServerCCSID_, attributes, true,
                  fd_.serverDatastreamLevel_);
          ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
      }
      catch (ConnectionDroppedException e) {
          Trace.log(Trace.ERROR, "Byte stream server connection lost.");
          fd_.connectionDropped(e);
      }
      catch (InterruptedException e) {
          Trace.log(Trace.ERROR, "Interrupted");
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      // Verify the reply.
      boolean success = false;
      if (ds instanceof IFSReturnCodeRep)
      {
          int rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc == IFSReturnCodeRep.SUCCESS)
              success = true;
          else if (Trace.traceOn_)
              Trace.log(Trace.ERROR, "Error setting file attributes: IFSReturnCodeRep return code", rc);
      } 
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, Integer.toHexString(ds.getReqRepID()), null);
      }

      // Clear any cached attributes.
      attributesReply_ = null;

      return success;
  }

  @Override
  public boolean setHidden(boolean attribute) throws IOException, AS400SecurityException
  {
      // Assume the argument has been validated by the public class.

      // Ensure that we are connected to the server.
      fd_.connect();

      boolean success = false;

      // Setting the fixed attributes of a file involves
      // replacing the current set of attributes. The first
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

                  IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname, fd_.preferredServerCCSID_, newAttributes, true, fd_.serverDatastreamLevel_);
                  ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
              } 
              catch (ConnectionDroppedException e) {
                  Trace.log(Trace.ERROR, "Byte stream server connection lost.");
                  fd_.connectionDropped(e);
              }
              catch (InterruptedException e) {
                  Trace.log(Trace.ERROR, "Interrupted");
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }

              if (ds instanceof IFSReturnCodeRep)
              {
                  int rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  if (rc == IFSReturnCodeRep.SUCCESS)
                      success = true;
                  else if (Trace.traceOn_)
                      Trace.log(Trace.ERROR, "Error setting hidden attribute: " + "IFSReturnCodeRep return code", rc);
              }
              else
              {
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }

              // Clear any cached attributes.
              attributesReply_ = null;

          } 
          else
              success = true;
      }
      
      return success;
  }

  @Override
  public boolean setLastModified(long time) throws IOException, AS400SecurityException
  {
      // Assume the argument has been validated by the public class.

      int fileHandle = UNINITIALIZED;
      boolean success = false;

      // Ensure that we are connected to the server.
      try
      {
          fd_.connect();

          if (time == -1)
          {
              // We are setting modification time to "current system time".
              // To do that, we will simply read and write-back the first byte in the file.

              // Open the file for read/write.
              fileHandle = fd_.createFileHandle(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN);
              if (fileHandle == UNINITIALIZED)
                  return false;
              else
                  fd_.setOpen(true, fileHandle); // inform the descriptor of the file handle

              byte[] buffer = new byte[1]; // buffer for reading/writing a single byte

              // If we're setting timestamp to "current system time", we'll need to know how big the file is.
              boolean fileIsEmpty = false; // @B8a
              if (time == -1)
                  fileIsEmpty = (length() == 0 ? true : false); // @B8a

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
                      if (Trace.traceOn_) Trace.log(Trace.ERROR, "Unable to read first byte of file.");
                      success = false;
                  }
              }
          }
          else // the caller specified a last-modified time
          {
              // Issue a change attributes request.
              ClientAccessDataStream ds = null;
              try
              {
                  // Convert the path name to the server CCSID.
                  byte[] pathname = fd_.converter_.stringToByteArray(fd_.path_);

                  IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname, fd_.preferredServerCCSID_, 0, time, 0, fd_.serverDatastreamLevel_);
                  ds = (ClientAccessDataStream) fd_.server_.sendAndReceive(req);
              } 
              catch (ConnectionDroppedException e) {
                  Trace.log(Trace.ERROR, "Byte stream server connection lost.");
                  fd_.connectionDropped(e);
              }
              catch (InterruptedException e) {
                  Trace.log(Trace.ERROR, "Interrupted");
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }

              // Verify the reply.
              if (ds instanceof IFSReturnCodeRep)
              {
                  int rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  if (rc == IFSReturnCodeRep.SUCCESS)
                      success = true;
                  else if (Trace.traceOn_)
                      Trace.log(Trace.ERROR, "Error setting last-modified date: IFSReturnCodeRep return code", rc);
              } 
              else
              {
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }
          }

          // Clear any cached attributes.
          attributesReply_ = null;
      }
      finally
      {
          if (fileHandle != UNINITIALIZED)
              fd_.close(fileHandle);
      }

      return success;
  }


  @Override
  public boolean setLength(int length) throws IOException, AS400SecurityException
  {
      // Assume the argument has been validated by the public class.

      // Ensure that we are connected to the server.
      fd_.connect();

      // Clear any cached attributes.
      attributesReply_ = null;

      return fd_.setLength(length);
  }

  @Override
  public void setPath(String path)
  {
      // Assume the argument has been validated by the public class.

      // Ensure that the path is not altered after the connection is
      // established.
      if (fd_.server_ != null)
          throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

      // If the specified path doesn't start with the separator character,
      // add one. All paths are absolute for IFS.
      String newPath;
      if (path.length() == 0 || path.charAt(0) != IFSFile.separatorChar)
          newPath = IFSFile.separator + path;
      else
          newPath = path;

      // Update the path value.
      fd_.path_ = newPath;
  }

  @Override
  public void setPatternMatching(int patternMatching)
  {
      fd_.patternMatching_ = patternMatching;
  }

  @Override
  public boolean setReadOnly(boolean attribute) throws IOException, AS400SecurityException
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

                  IFSChangeAttrsReq req = new IFSChangeAttrsReq(pathname, fd_.preferredServerCCSID_,
                                                                newAttributes, true, fd_.serverDatastreamLevel_);
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
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }

              if (ds instanceof IFSReturnCodeRep)
              {
                  int rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  if (rc == IFSReturnCodeRep.SUCCESS)
                      success = true;
                  else if (Trace.traceOn_) 
                      Trace.log(Trace.ERROR, "Error setting read-only attribute: IFSReturnCodeRep return code", rc);
              }
              else
              {
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }

              // Clear any cached attributes.
              attributesReply_ = null;
          }
          else
              success = true;
      }
      
      return success;
  }

  @Override
  public void setSorted(boolean sort)
  {
      sortLists_ = sort;
  }

  @Override
  public void setSystem(AS400Impl system)
  {
    // Assume the argument has been validated by the public class.

    fd_.system_ = (AS400ImplRemote)system;
  }

  // Setup remote service program object on first touch.  Synchronized to protect instance variables.  
  // This method can safely be called multiple times because it checks for a previous call before changing the instance variables.
  protected synchronized void setupRemoteCommand() throws IOException
  {
      if (rmtCmd_ != null) 
          return;
      
      if (fd_.system_.canUseNativeOptimizations())
      {
          try {
              // Avoid direct reference - it can cause NoClassDefFoundError at class loading time on Sun JVM's.
              rmtCmd_ = (RemoteCommandImpl) Class.forName("com.ibm.as400.access.RemoteCommandImplNative").newInstance();
          } 
          catch (Throwable e) {
              Trace.log(Trace.WARNING, "Unable to instantiate class RemoteCommandImplNative.", e);
          }
      }
      
      // Use remote implementation if not set.
      if (rmtCmd_ == null)
          rmtCmd_ = new RemoteCommandImplRemote();

      rmtCmd_.setSystem(fd_.system_);
  }

  @Override
  public String getDescription() throws IOException, AS400SecurityException
  {
      // We must use doQp0lGetAttr to get the description in order to get 
      // the description in CCSID of the job for QSYS.LIB objects, and not 
      // in the CCSID of the file server.  That is how QSYS.LIB object 
      // gives back the description. So if you have NLS characters in 
      // the description, it will be converted to the file server job, which 
      // is not what we want. 
      String descr = (String)doQp0lGetAttr(QP0L_ATTR_TEXT);

      return (descr != null) ? descr : "";
  }
  
  /*
   * INTERNAL USE ONLY
   * Checks to see if names in path contain forward slash, which is problematic when 
   * interacting with file server. Quoted names for QSYS.LIB objects can have a forward slash.
   */
  static boolean isPathProblematic(QSYSObjectPathName p) 
  {
      return (   p.getLibraryName().indexOf('/') != -1 
              || p.getObjectName().indexOf('/')  != -1
              || p.getMemberName().indexOf('/')  != -1);
  }
  
  static String trimRight(String s) 
  {
      if (s == null || s.length() == 0)
          return "";
      
      int endIndex = 0;
      int len = s.length();
      for (endIndex=len -1; endIndex > -1; endIndex--) 
      {
          char c = s.charAt(endIndex);
          if (c != ' ')
              break;
      }
      return s.substring(0, endIndex +1);
  }
  
  /*
   * IFS has problems with getting description which is DBCS from QSYS objects.
   */
  
  private static final byte              ebcdicDelim   = 97;
  private static final byte              nullDelim     = 0;
  private static final byte[]            hexZeros      = new byte[50];
  private static final AS400Bin4         intConverter  = new AS400Bin4();
  private static final AS400UnsignedBin4 uintConverter = new AS400UnsignedBin4();
  private static final AS400Bin8         longConverter = new AS400Bin8();

  private static final int QP0L_ATTR_ASP          = 13;
  private static final int QP0L_ATTR_DATA_SIZE_64 = 14;
  private static final int QP0L_ATTR_CCSID        = 27;
  private static final int QP0L_ATTR_TEXT         = 48;
  
  private Object doQp0lGetAttr(int attr) throws IOException, AS400SecurityException
  {    
      // Create the pgm call object
      if (rmtCmd_ == null)
          setupRemoteCommand();
      
      Object result = null;

      try 
      {
          ByteArrayOutputStream bo =  new ByteArrayOutputStream();
    
          ConvTable conv = ConvTable.getTable(fd_.system_.getCcsid(), null);
          
          ProgramParameter[] parms = new ProgramParameter[7];
          
          // =======
          // PARAMETER #1 ===== Path name format
          
          // QSYS.LIB paths may contain quoted object names that has '/' character in the name, 
          // such as '/qsys.lib/"new/lib".lib'.  When using IFS APIs, we need to replace delimiter
          // with something else, which in this case is hex 00. 
          byte[] pathBytes = null;
          byte delim       = nullDelim;
    //
    //      if (sysObject.isQSYSObject() && isPathProblematic(sysObject.getQSYSPath()))
    //      {
    //          QSYSObjectPathName qsysPath  = sysObject.getQSYSPath();
    //          
    //          String asp = qsysPath.getAspName();
    //          String lib = qsysPath.getLibraryName();
    //          String obj = qsysPath.getObjectName();
    //          String mbr = qsysPath.getMemberName();
    //          
    //          // Use hex 00 as delimiter
    //          if (!asp.isEmpty())
    //          {
    //              bo.write(delim);
    //              if (asp.charAt(0) == '/')
    //                  bo.write(conv.stringToByteArray(asp.substring(1)));
    //              else
    //                  bo.write(conv.stringToByteArray(asp));
    //          }
    //          
    //          if (!lib.isEmpty()) 
    //          {
    //              bo.write(delim);
    //              bo.write(conv.stringToByteArray(lib + ".LIB"));
    //          }
    //
    //          if (!obj.isEmpty())
    //          {
    //              bo.write(delim);
    //              bo.write(conv.stringToByteArray(obj + "." + qsysPath.getObjectType()));
    //          }
    //
    //          if (!mbr.isEmpty())
    //          {
    //              bo.write(delim);
    //              bo.write(conv.stringToByteArray(mbr + ".MBR"));
    //          }
    //          
    //          pathBytes = bo.toByteArray();
    //      }
    //      else 
          {
              delim     = ebcdicDelim;
              pathBytes = conv.stringToByteArray(fd_.path_);
          }
                
          bo.reset();
    
          bo.write(hexZeros, 0, 16);
          bo.write(BinaryConverter.intToByteArray((int)pathBytes.length)); // length of path
          bo.write(delim);   // path delimiter character
          bo.write(hexZeros, 0, 11);
          bo.write(pathBytes);   // path 
          
          parms[0] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, bo.toByteArray());
          
          // =======
          // PARAMETER #2 ===== attribute array
          int numAttrs = 1;
          bo.reset();
          bo.write(BinaryConverter.intToByteArray(numAttrs));     // Number of attributes to return
          bo.write(BinaryConverter.intToByteArray(attr));    
    
          parms[1] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, bo.toByteArray());
    
          // =======
          // PARAMETER #3 ===== output buffer
          parms[2] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 200);
    
          // =======
          // PARAMETER #4 ===== output buffer size
          parms[3] = new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray((int)200));
    
          // =======
          // PARAMETER #5 ===== output buffer size needed
          parms[4] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 4);
          
          // =======
          // PARAMETER #6 ===== number of bytes returned
          parms[5] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, 4);
    
          // =======
          // PARAMETER #7 ===== follow symbolic link
          parms[6]  = new ProgramParameter(ProgramParameter.PASS_BY_VALUE, BinaryConverter.intToByteArray((int)1));
    
          // Call the service program.
          byte[] returnedBytes = rmtCmd_.runServiceProgram("QSYS", "QP0LLIB2", "Qp0lGetAttr", parms);
          if (returnedBytes == null)
          {
              Trace.log(Trace.ERROR, "Call to Qp0lGetAttr() returned null.");
              throw new AS400Exception(rmtCmd_.getMessageList());
          }

          int returnValue = BinaryConverter.byteArrayToInt(returnedBytes, 0);
          if (returnValue != 0)
          {
              int errno = BinaryConverter.byteArrayToInt(returnedBytes, 4);
              Trace.log(Trace.ERROR, "Qp0lGetAttr errno=: " + errno);
              return null;
          }
          
          /** 
           * Output buffer format
           * 0   0   BINARY(4)   Offset to next attribute entry
           * 4   4   BINARY(4)   Attribute identification
           * 8   8   BINARY(4)   Size of attribute data
           * 12  C   CHAR(4)     Reserved
           * 16  10  CHAR(*)     Attribute data
           */
    
          byte[] outBuf = parms[2].getOutputData();
    
          int offset=0;
          long nextOffset=-1;
          while (nextOffset != 0)
          {
              nextOffset        = uintConverter.toLong(outBuf, offset+0);
              long attrID       = uintConverter.toLong(outBuf, offset+4);
              long attrDataSize = uintConverter.toLong(outBuf, offset+8);
              
              if (attrDataSize > 0)
              {
                  if (attrID == QP0L_ATTR_DATA_SIZE_64)
                      result = longConverter.toObject(outBuf, offset+16); // long long
                  else if (attrID == QP0L_ATTR_CCSID)
                      result  = intConverter.toObject(outBuf, offset+16); 
                  else if (attrID == QP0L_ATTR_ASP)
                      result  = intConverter.toObject(outBuf, offset+16);
                  else if (attrID == QP0L_ATTR_TEXT) 
                  {
                      String description = conv.byteArrayToString(outBuf, offset+16, (int)attrDataSize); // max of 50
                      
                      // Cobol sets text field to hex zeros....sigh
                      description = description.replace('\0', ' ');
                      description = trimRight(description);
                      
                      result = description;
                  }
              }
              
              if (result != null)
                  break;
              
              offset = (int)nextOffset;
          }
      }
      catch (IOException | AS400SecurityException e) {
          throw e;
      }
      catch (Exception e) {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Qp0lGetAttr exception ", e);
      }
      
      return result;
  }
}
