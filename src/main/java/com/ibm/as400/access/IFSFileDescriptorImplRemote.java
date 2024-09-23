///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileDescriptorImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @A3 - 10/18/2007 - Update finalize0() method to not send TWO close requests
//                    to the server.  When garbage collection finds a descriptor
//                    that has not been closed, it calls the finalize method on 
//                    one of the various IFSFilexxx classes which calls the 
//                    finalize0() method of this class.
//                    This class was sending TWO close requests to the server 
//                    for the same fileHandle_.  Normally, the second close would
//                    silently fail.  However, in some cases another thread might
//                    issue an open request for another file BETWEEN the two close
//                    requests sent by the garbage collection thread.  This 
//                    results in the newly opened file being incorrectly closed.
//                    Fix to finalize0() is to issue ONE close request to the 
//                    File server.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;


/**
 Provides a full remote implementation for the IFSFileDescriptor class.
 **/
class IFSFileDescriptorImplRemote implements IFSFileDescriptorImpl
{
    private static final int UNINITIALIZED = -1;
    private static final int MAX_BYTES_PER_READ = 16000000;  // limit of file server

    // Note: We allow direct access to some of these fields, for performance.
    ConverterImplRemote converter_;
    private int         fileHandle_ = UNINITIALIZED;
    int                 preferredServerCCSID_;
    private int         fileDataCCSID_ = UNINITIALIZED;
    private int         fileAsp_ = UNINITIALIZED;
    private int         fileSystemType_ = UNINITIALIZED;
    int                 serverDatastreamLevel_;
    int                 requestedDatastreamLevel_;
    private long        fileOffset_;
    boolean             isOpen_;
    boolean             isOpenAllowed_ = true;
    private Object      parent_;  // The object that instantiated this IFSDescriptor.
    String              path_ = "";
    byte[]              pathnameBytes_;
    transient Object    serverLock_ = new Object(); 
    AS400Server         server_;  // Note: AS400Server is not serializable.
    private int         shareOption_;
    AS400ImplRemote     system_;
    private Object      fileOffsetLock_ = new Object(); // Semaphore for synchronizing access to fileOffset_.
    private int         maxDataBlockSize_ = 1024;
    private Integer     systemVRM_;
    transient int       errorRC_;  // error return code from most recent request
    int                 patternMatching_ = IFSFile.PATTERN_DEFAULT;  // pattern-matching semantics
  
    // Used for debugging only.  This should always be false for production.
    private static final boolean DEBUG = false;
  
    String fileOwnerName_ = null;

    static
    {
        // Add all byte stream reply data streams of interest to the
        // AS400 server's reply data stream hash table.
        AS400Server.addReplyStream(new IFSCloseRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSExchangeAttrRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSLockBytesRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSWriteRep(), AS400.FILE);
        AS400Server.addReplyStream(new IFSReadRep(), AS400.FILE);
    }


  // Cast an IFSFileDescriptorImpl object into an IFSFileDescriptorImplRemote.
  // Used by various IFS*ImplRemote classes.
  static IFSFileDescriptorImplRemote castImplToImplRemote(IFSFileDescriptorImpl fd)
  {
      try {
          return (IFSFileDescriptorImplRemote)fd;
      }
      catch (ClassCastException e)
      {
          Trace.log(Trace.ERROR, "Argument is not an instance of IFSFileDescriptorImplRemote", e);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
      }
  }

  // Determine if the directory entry can be accessed in the specified manner.
  // Returns the returnCode that was set (as a side effect) by createFileHandle().
  int checkAccess(int access, int openOption) throws IOException, AS400SecurityException
  {
      int fileHandle = UNINITIALIZED;
      try {
          fileHandle = createFileHandle(access, openOption);
      } finally {
          if (fileHandle != UNINITIALIZED)
              close(fileHandle); // we don't need this handle anymore
      }
      
      return errorRC_;
  }

  @Override
  public void close()
  {
      try {
          close0();
      }
      catch (IOException e) {
          Trace.log(Trace.ERROR, "Error while closing file " + path_, e);
      }
  }

  public void close0() throws IOException 
  {
      isOpen_ = false;
      close(fileHandle_);
      fileHandle_ = UNINITIALIZED;
  }

  void close(int fileHandle) throws IOException
  {
      if (fileHandle == UNINITIALIZED)
          return;

      // Close the file. Send a close request to the server.
      ClientAccessDataStream ds = null;
      IFSCloseReq req = new IFSCloseReq(fileHandle);
      try {
          ds = (ClientAccessDataStream) server_.sendAndReceive(req);
      }
      catch (ConnectionDroppedException e) {
          Trace.log(Trace.ERROR, "Byte stream connection lost during close", e);
          connectionDropped(e);
      }
      catch (InterruptedException e) {
          Trace.log(Trace.ERROR, "Interrupted", e);
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      // Validate the reply.
      if (ds instanceof IFSCloseRep)
      {
          int rc = ((IFSCloseRep) ds).getReturnCode();
          if (rc != 0) {
              Trace.log(Trace.ERROR, "IFSCloseRep return code", rc);
              throw new ExtendedIOException(path_, rc);
          }
      }
      else if (ds instanceof IFSReturnCodeRep) 
      {
          int rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc != IFSReturnCodeRep.SUCCESS) {
              Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
              throw new ExtendedIOException(path_, rc);
          }
      } 
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
      }
  }

  /**
   Establishes communications with the server.
   **/
  void connect() throws IOException, AS400SecurityException
  {
      if (server_ != null)
          return;
      
      if (system_ == null)
          throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

      try {
          server_ = system_.getConnection(AS400.FILE, false /* forceNewConnection */, false /* skip signon server */ );
      }
      catch (AS400SecurityException e) {
          Trace.log(Trace.ERROR, "Access to byte stream server on '" + system_.getSystemName() + "' denied.", e);
          throw e;
      }

      exchangeServerAttributes();
  }

  /**
   Disconnects from the byte stream server.
   **/
  void connectionDropped(ConnectionDroppedException e) throws ConnectionDroppedException
  {
      if (server_ != null)
      {
          system_.disconnectServer(server_);
          server_ = null;
          try {
              close();
          } 
          catch (Exception exc) { } // Note: Not relevant for IFSFileImplRemote.
      }
      
      Trace.log(Trace.ERROR, "Byte stream connection lost.");
      throw e;
  }

  /**
   * Copies a file or directory to another file or directory.
  **/
  boolean copyTo(String destinationPath, boolean replace) throws AS400SecurityException, IOException
  {
      ClientAccessDataStream ds = null;
      IFSCopyReq req = new IFSCopyReq(path_, destinationPath, replace);
      
      try {
          ds = (ClientAccessDataStream) server_.sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
          Trace.log(Trace.ERROR, "Byte stream connection lost during copy", e);
          connectionDropped(e);
      }
      catch(InterruptedException e)
      {
          Trace.log(Trace.ERROR, "Interrupted", e);
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }
      
      if (ds instanceof IFSReturnCodeRep)
      {
          int rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc != IFSReturnCodeRep.SUCCESS)
          {
              String path = (rc == IFSReturnCodeRep.DUPLICATE_DIR_ENTRY_NAME ? destinationPath : path_);
              throwSecurityExceptionIfAccessDenied(path,rc); // check for "access denied"
              Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
              throw new ExtendedIOException(path, rc);
          }
          
          return true;
      }

      Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
      throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
  }  

  // Opens the file for 'read' access, and returns a file handle.
  private final int createFileHandle() throws IOException, AS400SecurityException {
      return createFileHandle(IFSOpenReq.READ_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN);
  }

  // Opens the file with specified access and option, and returns a file handle.
  // If failure, sets errorRC_ and returns UNINITIALIZED.
  int createFileHandle(int access, int openOption) throws IOException, AS400SecurityException
  {
      int returnCode = IFSReturnCodeRep.FILE_NOT_FOUND;
      int fileHandle = UNINITIALIZED;
      errorRC_ = 0;

      // Try to open the file for the specified type of access.
      try
      {
          // Process an open file request.
          // For 3rd parm (file data CCSID), specify 0 since we don't care about CCSID.
          IFSOpenReq req = new IFSOpenReq(getPathnameAsBytes(), preferredServerCCSID_, 0, access, 
                                          IFSOpenReq.DENY_NONE, IFSOpenReq.NO_CONVERSION, openOption, serverDatastreamLevel_);
          ClientAccessDataStream ds = (ClientAccessDataStream) server_.sendAndReceive(req);
          if (ds instanceof IFSOpenRep)
          {
              // The open was successful.  Close the file if appropriate.
               returnCode = IFSReturnCodeRep.SUCCESS;
               fileHandle = ((IFSOpenRep) ds).getFileHandle();  // @B8a
          }
          else if (ds instanceof IFSReturnCodeRep)
          {
              returnCode = ((IFSReturnCodeRep) ds).getReturnCode();
              throwSecurityExceptionIfAccessDenied(path_,returnCode); // check for "access denied"
          }
          else
          {
              Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
              throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
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
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      if (returnCode != IFSReturnCodeRep.SUCCESS)
      {
          if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
          {
              Trace.log(Trace.DIAGNOSTIC, "Unable to open file " + path_ + ": " +
                                          "IFSReturnCodeRep return code", descriptionForReturnCode(returnCode));
          }
          
          errorRC_ = returnCode;
          return UNINITIALIZED;
      }

      return fileHandle;
  }

  void exchangeServerAttributes() throws IOException, AS400SecurityException
  {
      synchronized (serverLock_)
      {
          DataStream ds = server_.getExchangeAttrReply();
          IFSExchangeAttrRep rep = null;
          try { 
              rep = (IFSExchangeAttrRep)ds;
          }
          catch (ClassCastException e)
          {
              if (ds instanceof IFSReturnCodeRep)
              {
                  int rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
                  Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
                  throw new ExtendedIOException(path_, rc);
              }
              else
              {
                  String className = ( ds == null ? "null" : ds.getClass().getName());
                  Trace.log(Trace.ERROR, "Unexpected reply from Exchange Server Attributes: " + className);
                  throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
              }
          }

          // Note: For releases after V5R4, we ask for Datastream Level 16;
          // for V5R3 thru V5R4, we ask for Datastream Level 8;
          // for V4R5 thru V5R2, we ask for Datastream Level 2;
          // for earlier systems, we ask for Datastream Level 0.
          if (getSystemVRM() >= 0x00060100)
            requestedDatastreamLevel_ = 16;
          else if (getSystemVRM() >= 0x00050300)
            requestedDatastreamLevel_ = 8;
          else if (getSystemVRM() >= 0x00040500)
            requestedDatastreamLevel_ = 2;
          else
            requestedDatastreamLevel_ = 0;
          
          if (rep == null)
          {
              ds = null;
              
              try
              {
                  int[] preferredCcsids;
                  
                  // Datastream level 8 was introduced in release V5R3. Present a list of preferred CCSIDs, if possible.
                  if (getSystemVRM() >= 0x00050300)
                      preferredCcsids = new int[] {0x04b0,0x34b0,0xf200}; // UTF-16, new or old Unicode.
                  else if (getSystemVRM() >= 0x00040500)
                      preferredCcsids = new int[] {0x34b0,0xf200}; // New or old Unicode.
                  else
                  { 
                      // System is pre-V4R5.  Exchange attr's the old way.
                      // Note: Pre-V4R5 systems hang when presented with multiple
                      // preferred CCSIDs in the exchange of attributes.
                      preferredCcsids = new int[] {0xf200}; // Old Unicode only.
                  }

                  // Use GMT date/time, don't use posix style return codes,
                  // use PC pattern matching semantics,
                  // maximum data transfer size of 0xffffffff.
                  ds = server_.sendExchangeAttrRequest(
                         new IFSExchangeAttrReq(true, false,
                                                IFSExchangeAttrReq.PC_PATTERN_MATCH,
                                                0xffffffff,
                                                requestedDatastreamLevel_,
                                                preferredCcsids));
                  rep = (IFSExchangeAttrRep)ds;
              }
              catch(ConnectionDroppedException e)
              {
                  Trace.log(Trace.ERROR, "Byte stream server connection lost");
                  connectionDropped(e);
              }
              catch(InterruptedException e)
              {
                  Trace.log(Trace.ERROR, "Interrupted", e);
                  system_.disconnectServer(server_);
                  server_ = null;
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }
              catch(IOException e)
              {
                  Trace.log(Trace.ERROR, "I/O error during attribute exchange.");
                  system_.disconnectServer(server_);
                  server_ = null;
                  throw e;
              }
              catch(ClassCastException e)
              {
                  if (ds instanceof IFSReturnCodeRep)
                  {
                      int rc = ((IFSReturnCodeRep) ds).getReturnCode();
                      throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
                      Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
                      throw new ExtendedIOException(path_, rc);
                  }
                  else
                  {
                      String className = ( ds == null ? "null" : ds.getClass().getName());
                      Trace.log(Trace.ERROR, "Unexpected reply from Exchange Server Attributes: " + className);
                      throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
                  }
              }
          }

          // Process the exchange attributes reply.
          if (rep != null)
          {
              maxDataBlockSize_ = ((IFSExchangeAttrRep) rep).getMaxDataBlockSize();
              preferredServerCCSID_ = rep.getPreferredCCSID();
              serverDatastreamLevel_ = rep.getDataStreamLevel();
              setConverter(ConverterImplRemote.getConverter(preferredServerCCSID_, system_));
              
              if (DEBUG)
              {
                  System.out.println("DEBUG: IFSFileDescriptorImplRemote.exchangeServerAttributes(): " +
                                     "preferredServerCCSID_ == " + preferredServerCCSID_);
                  int[] list = rep.getPreferredCCSIDs();
                  for (int i=0; i<list.length; i++)
                      System.out.println("-- Server's preferred CCSID (#" + i+1 + "): " + list[i]);
                  System.out.println("-- Server's dataStreamLevel : " + Integer.toHexString(serverDatastreamLevel_));
              }
          }
          else
          {
              Trace.log(Trace.ERROR, "Null reply data stream");
              system_.disconnectServer(server_);
              server_ = null;
              throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN);
          }
      }
  }

  private static String descriptionForReturnCode(int rc)
  {
    switch (rc)
    {
      case 4636: // 0x121C
        return rc+": Not authorized to command. (Check for user exit program.)";
        // Occurs mainly when user is not authorized to the user exit program.
      case 4692: // 0x1254
        return rc+": Command check. (Check for user exit program.)";
        // Occurs mainly when the user exit program is either not found, deleted, or deleted and recreated but the QSERVER subsystem has not been ended and restarted.
      case 4702: // 0x125E
        return rc+": Closed with damage.";
        // File was closed but had an error in the file server close function.
      case 3391: // 0x0D3F
        return rc+": File is temporarily unavailable.";
      case 3392: // 0x0D40
        return rc+": Directory is temporarily unavailable.";
      case 3393: // 0x0D41
        return rc+": Working directory handle is not valid.";
        // May get return code 6 (handle invalid) instead of 3393.
      case 3394: // 0x0D42
        return rc+": File handle is not valid.";
        // May get return code 6 (handle invalid) instead.
      case 3395: // 0x0D43
        return rc+": User handle is not valid.";
        // May get return code 6 (handle invalid) instead.
      case 3396: // 0x0D44
        return rc+": User is temporarily unavailable.";
      default: return Integer.toString(rc);
    }
  }


  /**
   Ensures that the file output stream is closed when there are no more
   references to it.
   **/
  // Note: We call this "finalize0" because a "finalize" method would need to be protected; and we want other classes to call this method.
  void finalize0() throws Throwable
  {
      try 
      {
          if (fileHandle_ != UNINITIALIZED)
          {
              // Close the file. Send a close request to the server.
              IFSCloseReq req = new IFSCloseReq(fileHandle_);
              try {
                  server_.sendAndDiscardReply(req);
              } 
              finally {
                  isOpen_ = false;
                  fileHandle_ = UNINITIALIZED;
              }
          }
      }
      catch(Throwable e) {
          Trace.log(Trace.ERROR, "Error during finalization.", e);
      }
      finally {
          super.finalize();
      }
  }

  /**
   Forces any buffered output bytes to be written.
   **/
  void flush()  throws IOException, AS400SecurityException
  {
      // Request that changes be committed to disk.
      IFSCommitReq req = new IFSCommitReq(fileHandle_);
      ClientAccessDataStream ds = null;
      
      try {
          ds = (ClientAccessDataStream) server_.sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
          Trace.log(Trace.ERROR, "Byte stream server connection lost");
          connectionDropped(e);
      }
      catch(InterruptedException e)
      {
          Trace.log(Trace.ERROR, "Interrupted", e);
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
           throwException.initCause(e);
           throw throwException;
      }

      // Verify that the request was successful.
      if (ds instanceof IFSReturnCodeRep)
      {
          int rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc != IFSReturnCodeRep.SUCCESS)
          {
              throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
              Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
              throw new ExtendedIOException(path_, rc);
          }
      }
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
      }
  }

  ConverterImplRemote getConverter() {
    return converter_;
  }
  
  public String getOwnerName() throws IOException
  {
      if (fileOwnerName_ == null) 
      {
          try 
          {
              connect();
              IFSListAttrsRep reply = listObjAttrs12(IFSObjAttrs1.OWNER_NAME_FLAG, 0);
              if (reply != null) 
              {
                  fileOwnerName_ = reply.getOwnerName(system_.getCcsid());
                  if (fileDataCCSID_ == UNINITIALIZED)
                      fileDataCCSID_ = reply.getCCSID(serverDatastreamLevel_);
              }
              else
              {
                  if (errorRC_ == IFSReturnCodeRep.FILE_NOT_FOUND || errorRC_ == IFSReturnCodeRep.PATH_NOT_FOUND)
                      throw new ExtendedIOException(path_, ExtendedIOException.PATH_NOT_FOUND);
                  
                  // Cannot create file handle to object, so let us try the other way
                  return getOwnerNameByUserHandle(true);
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
          catch (AS400SecurityException e) {
              throw new ExtendedIOException(path_, ExtendedIOException.ACCESS_DENIED);
          }
      }      
      
      return (fileOwnerName_ == null ? "" : fileOwnerName_);
   }

   public int getCCSID(boolean retrieveAll) throws IOException
   {
       // A CCSID value of zero is not valid.  Just try to get it again.
       if (fileDataCCSID_ == UNINITIALIZED)
       {
           try
           {
               connect();

               IFSListAttrsRep reply = null;
               if (retrieveAll)
                   reply = listObjAttrs12(IFSObjAttrs1.OWNER_NAME_FLAG, 0);
               else
                   reply = listObjAttrs2(); // the 'ccsid' field is in the OA2 structure
               
               if (reply != null)
               {
                   fileDataCCSID_ = reply.getCCSID(serverDatastreamLevel_);
                   if (retrieveAll && fileOwnerName_ == null)
                       fileOwnerName_ = reply.getOwnerName(system_.getCcsid());
               }
               else
               {
                   if (errorRC_ == IFSReturnCodeRep.FILE_NOT_FOUND || errorRC_ == IFSReturnCodeRep.PATH_NOT_FOUND)
                       throw new ExtendedIOException(path_, ExtendedIOException.PATH_NOT_FOUND);
                   
                   // Cannot create file handle to object, so let us try the other way
                   return getCCSID();
               }
           } 
           catch (AS400SecurityException e) {
               throw new ExtendedIOException(path_, ExtendedIOException.ACCESS_DENIED);
           }
       }
       
       return fileDataCCSID_;
   }

  @Override
  public int getCCSID() throws IOException
  {
      if (fileDataCCSID_ == UNINITIALIZED)
      {
          ClientAccessDataStream ds = null;

          try
          {
              connect();
      
              //create user handle
              int userHandle = UNINITIALIZED, objectHandle = UNINITIALIZED;
              try
              {
                  // In 7.5 and prior releases, need to create user handle for IFS tables to be initialized.
                  userHandle = (getSystem().getVRM() <= 0x00070500) ? system_.createUserHandle() : 0;
    
                  try
                  {
                      byte[] path = getConverter().stringToByteArray(path_);
    
                      IFSLookupReq req = new IFSLookupReq(path, preferredServerCCSID_, userHandle, IFSLookupReq.OA12, IFSObjAttrs1.OWNERANAME_ASP_FLAS, 0);
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
                      InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                      throwException.initCause(e);
                      throw throwException;
                  }
    
                  int rc = 0;
                  if (ds instanceof IFSLookupRep)
                  {
                      objectHandle = ((IFSLookupRep) ds).getHandle();
                      retrieveAttributes(ds, objectHandle);
                  }
                  else if (ds instanceof IFSReturnCodeRep)
                  {
                      rc = ((IFSReturnCodeRep) ds).getReturnCode();
                      if (rc != IFSReturnCodeRep.SUCCESS) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
    
                      throw new ExtendedIOException(path_, rc);
                  }
                  else
                  {
                      Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
                      throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
                  }
              }
              finally
              {
                  if (objectHandle != UNINITIALIZED)
                      freeHandle(objectHandle);
              }
          }
          catch (AS400SecurityException e) {
              throw new ExtendedIOException(path_, ExtendedIOException.ACCESS_DENIED);
          }
      }
      
      return fileDataCCSID_;
  }

  int getFileHandle() {
    return fileHandle_;
  }

  @Override
  public long getFileOffset(){
    return fileOffset_;
  }

  Object getParent() {
    return parent_;
  }

  String getPath() {
    return path_;
  }

  final byte[] getPathnameAsBytes()
  {
      if (pathnameBytes_ == null)
          pathnameBytes_ = converter_.stringToByteArray(path_);

      return pathnameBytes_;
  }

  int getPreferredCCSID() {
    return preferredServerCCSID_;
  }

  AS400Server getServer() {
    return server_;
  }

  int getShareOption() {
    return shareOption_;
  }

  AS400ImplRemote getSystem() {
    return system_;
  }


  // Determine the system version.
  int getSystemVRM()
  {
      if (systemVRM_ == null)
          systemVRM_ = system_.getVRM();

      return systemVRM_;
  }

  @Override
  public void incrementFileOffset(long fileOffsetIncrement)
  {
      synchronized(fileOffsetLock_) {
          fileOffset_ += fileOffsetIncrement;
      }
  }

  @Override
  public void initialize(long fileOffset, Object parentImpl, String path, int shareOption, AS400Impl system)
  {
      fileOffset_           = fileOffset;
      parent_               = parentImpl;
      path_                 = path;
      shareOption_          = shareOption;
      system_               = (AS400ImplRemote) system;
  }

  @Override
  public boolean isOpen() {
      return isOpen_;
  }

  boolean isOpenAllowed() {
      return isOpenAllowed_;
  }

  // Submit the specified request, and fetch list attributes reply(s).
  // The returned Vector contains IFSListAttrsRep objects.
  Vector listAttributes(IFSListAttrsReq req) throws IOException, AS400SecurityException
  {
      // Assume connect() has already been done.

      errorRC_ = 0;
      Vector replys = new Vector(256);
      ClientAccessDataStream ds = null;
      
      try {
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
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      int rc = -1;
      boolean done = false;
      
      do
      {
          if (ds instanceof IFSListAttrsRep)
              replys.addElement(ds);
          else if (ds instanceof IFSReturnCodeRep)
          {
              // If the return code is NO_MORE_FILES then all files
              // that match the specification have been returned.
              rc = ((IFSReturnCodeRep) ds).getReturnCode();

              if (rc != IFSReturnCodeRep.SUCCESS &&
                  rc != IFSReturnCodeRep.NO_MORE_FILES &&
                  rc != IFSReturnCodeRep.FILE_NOT_FOUND &&
                  rc != IFSReturnCodeRep.PATH_NOT_FOUND)
              {
                  throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
                  Trace.log(Trace.ERROR, "Error getting file attributes for file " + path_ + ": " +
                                         "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
                  throw new ExtendedIOException(path_, rc);
              }
          }
          else
          {
              Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);  // @A9C
              throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
          }

          // Fetch the next reply if not already done.
          done = ((IFSDataStream) ds).isEndOfChain();
          if (!done)
          {
              try {
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
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }
          }
      }
      while (!done);

      if (rc == IFSReturnCodeRep.PATH_NOT_FOUND)
      {
          // If the directory or file does not exist, then return NULL.
          errorRC_ = rc;
          replys = null;
      }
      else
      {
          // Set the vector capacity to the current size.
          replys.trimToSize();
      }

      return replys;
  }


  // Open a single file, list the "type 1" (OA1) attributes, and close the file.
  // May return null, for example if the file is a directory.
  IFSListAttrsRep listObjAttrs1(int flags1, int flags2) throws IOException, AS400SecurityException {
      return listObjAttrs(IFSListAttrsReq.OA1, flags1, flags2);
  }


  // Open a single file, list the "type 2" (OA2) attributes, and close the file.
  // May return null, for example if the file is a directory.
  IFSListAttrsRep listObjAttrs2() throws IOException, AS400SecurityException {
      return listObjAttrs(IFSListAttrsReq.OA2, 0, 0);
  }
  
  IFSListAttrsRep listObjAttrs12(int flags1, int flags2) throws IOException, AS400SecurityException {
      return listObjAttrs(IFSListAttrsReq.OA12, flags1, flags2);
  }

  // Open a single file, list the file attributes, and close the file.
  // Returns null if the file doesn't exist or is a directory.
  private IFSListAttrsRep listObjAttrs(int attrsType, int flags1, int flags2) throws IOException, AS400SecurityException
  {
      // Assume connect() has already been done.

      IFSListAttrsRep reply = null;
      int fileHandle = UNINITIALIZED;

      // Design note: In order to get an OA* structure back in the "List File Attributes" reply, 
      // we must specify the file by handle rather than by name.

      boolean usedGlobalHandle = false;
      try
      {
          // Open the file, and obtain a file handle.
          if (fileHandle_ != UNINITIALIZED) 
          {   
              fileHandle = fileHandle_;
              usedGlobalHandle = true;
          }
          else
          {
              fileHandle = createFileHandle();
              if (fileHandle == UNINITIALIZED)
              {
                  if (Trace.traceOn_) Trace.log(Trace.ERROR, "Unable to create handle to file " + path_ + ". IFSReturnCodeRep return code", errorRC_);
                  return null;
              }
          }

          // Send a 'list attributes' request, specifying the file handle we just created,
          // and indicating that we want an OA2 structure in the reply.

          IFSListAttrsReq req1 = new IFSListAttrsReq(fileHandle, attrsType, flags1, flags2);
          req1.setPatternMatching(patternMatching_);

          Vector replys = listAttributes(req1);

          // Verify that we got exactly one reply.
          if (replys == null) {
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received null from listAttributes(fileHandle).");
          }
          else if (replys.size() == 0) {
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received no replies from listAttributes(fileHandle).");
          }   
          else if (replys.size() > 1) {
              if (Trace.traceOn_) Trace.log(Trace.WARNING, "Received multiple replies from listAttributes(fileHandle) (" + replys.size() + ")");
          }
          else {
              reply = (IFSListAttrsRep) replys.elementAt(0);
          }
      }
      finally
      {
          if(!usedGlobalHandle && fileHandle != UNINITIALIZED)
              close(fileHandle);
      }

      return reply;
  }


  /**
   Places a lock on the file at the specified bytes.
   @param offset The first byte of the file to lock (zero is the first byte).
   @param length The number of bytes to lock.
   @return A key for undoing this lock.
   **/
  IFSKey lock(long length) throws IOException, AS400SecurityException {
      return lock(fileOffset_, length);
  }

  IFSKey lock(long offset, long length) throws IOException, AS400SecurityException
  {
      // Assume the arguments have been validated by the caller.

      // Attempt to lock the file.
      ClientAccessDataStream ds = null;
      
      try
      {
          // Issue a mandatory, exclusive lock bytes request.  Mandatory
          // means that the file system enforces the lock by causing any
          // operation which conflicts with the lock to fail.  Exclusive
          // means that only the owner of the lock can read or write the
          // locked area.
          IFSLockBytesReq req = new IFSLockBytesReq(fileHandle_, true, false, offset, length, serverDatastreamLevel_);
          ds = (ClientAccessDataStream) server_.sendAndReceive(req);
      }
      catch(ConnectionDroppedException e)
      {
          Trace.log(Trace.ERROR, "Byte stream server connection lost");
          connectionDropped(e);
      }
      catch(InterruptedException e)
      {
          Trace.log(Trace.ERROR, "Interrupted", e);
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      // Verify the reply.
      if (ds instanceof IFSLockBytesRep)
      {
          int rc = ((IFSLockBytesRep) ds).getReturnCode();
          if (rc != 0)
          {
              Trace.log(Trace.ERROR, "IFSLockBytesRep return code", rc);
              throw new ExtendedIOException(path_, rc);
          }
      }
      else if (ds instanceof IFSReturnCodeRep)
      {
          int rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc != IFSReturnCodeRep.SUCCESS)
          {
              throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
              Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
              throw new ExtendedIOException(path_, rc);
          }
      }
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
      }

      // Generate the key for this lock.
      IFSKey key = new IFSKey(fileHandle_, offset, length, true);

      return key;
  }

  /**
   Reads up to <i>length</i> bytes of data from this input stream into <i>data</i>,
   starting at the array offset <i>dataOffset</i>.
   @param data The buffer into which the data is read.
   @param offset The start offset of the data in the buffer.
   @param length The maximum number of bytes to read
   @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of file has been reached.
   **/
  int read(byte[] data, int dataOffset, int length) throws IOException, AS400SecurityException
  {
      // Assume the arguments have been validated by the public class.

      // If length is zero then return zero.
      if (length == 0)
          return 0;

      int totalBytesRead = 0;
      int bytesRemainingToRead = length;
      boolean endOfFile = false;
      
      while (totalBytesRead < length && !endOfFile)
      {
          // If the number of bytes being requested is greater than 16 million, then submit multiple requests for smaller chunks.  
          // The File Server has a limit that is somewhat below 16 megabytes (allowing for headers, etc), so 16 _million_ bytes is a safe limit.

          // Issue the read data request.
          int bytesToReadThisTime = Math.min(bytesRemainingToRead, MAX_BYTES_PER_READ);
          IFSReadReq req = new IFSReadReq(fileHandle_, fileOffset_, bytesToReadThisTime, serverDatastreamLevel_);
          ClientAccessDataStream ds = null;
          
          try{
              ds = (ClientAccessDataStream) server_.sendAndReceive(req);
          }
          catch(ConnectionDroppedException e)
          {
              Trace.log(Trace.ERROR, "Byte stream server connection lost");
              connectionDropped(e);
          }
          catch(InterruptedException e)
          {
              Trace.log(Trace.ERROR, "Interrupted", e);
              InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
              throwException.initCause(e);
              throw throwException;
          }

          // Receive replies until the end of chain.
          boolean endOfChain = false;
          int bytesReadByThisRequest = 0;
          
          do
          {
              if (ds instanceof IFSReadRep)
              {
                  // Copy the data from the reply to the data parameter.
                  byte[] buffer = ((IFSReadRep) ds).getData();
                  if (buffer.length > 0)
                  {
                      System.arraycopy(buffer, 0, data, dataOffset, buffer.length);
                      bytesReadByThisRequest += buffer.length;
                      dataOffset += buffer.length;
                  }
                  else // no data returned. This implies end-of-file (e.g. if file is empty).
                  {
                      bytesReadByThisRequest = -1;
                      endOfFile = true;
                  }
              }
              else if (ds instanceof IFSReturnCodeRep)
              {
                  // Check for failure.
                  int rc = ((IFSReturnCodeRep) ds).getReturnCode();

                  if (rc == IFSReturnCodeRep.SUCCESS)
                  {  // It worked, so nothing special to do here.
                  }
                  else if (rc == IFSReturnCodeRep.NO_MORE_DATA)
                  {
                      // End of file.
                      bytesReadByThisRequest = -1;
                      endOfFile = true;
                  }
                  else  // none of the above
                  {
                      throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
                      Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
                      throw new ExtendedIOException(path_, rc);
                  }
              }
              else
              {
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }

              // Get the next reply if not end of chain.
              endOfChain = ((IFSDataStream) ds).isEndOfChain();
              if (!endOfChain)
              {
                  try {
                      ds = (ClientAccessDataStream) server_.receive(req.getCorrelation());
                  }
                  catch(ConnectionDroppedException e)
                  {
                      Trace.log(Trace.ERROR, "Byte stream server connection lost");
                      connectionDropped(e);
                  }
                  catch(InterruptedException e)
                  {
                      Trace.log(Trace.ERROR, "Interrupted", e);
                      InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                      throwException.initCause(e);
                      throw throwException;
                  }
              }
          }
          while (!endOfChain);

          // Advance the file pointer.
          if (bytesReadByThisRequest > 0)
          {
              incrementFileOffset(bytesReadByThisRequest);
              totalBytesRead += bytesReadByThisRequest;
              bytesRemainingToRead -= bytesReadByThisRequest;
          }
      }

      // If we have read zero bytes and hit end-of-file, indicate that by returning -1.
      // Otherwise return total number of bytes read.
      return (endOfFile && totalBytesRead == 0 ? -1 : totalBytesRead);
  }

  void setConverter(ConverterImplRemote converter) {
    converter_ = converter;
  }

  void setCCSID(int ccsid) throws IOException {
      fileDataCCSID_ = ccsid;
  }

  public void setFileOffset(long fileOffset)
  {
      synchronized (fileOffsetLock_) {
          fileOffset_ = fileOffset;
      }
  }

  boolean setLength(long length) throws IOException, AS400SecurityException
  {
      // Assume that we are connected to the server.

      // Prepare to issue a 'change attributes' request.
      ClientAccessDataStream ds = null;
      int fileHandle = UNINITIALIZED;

      try
      {
          // Open the file for read/write, get a file handle, and call 'change attributes'.
          fileHandle = createFileHandle(IFSOpenReq.WRITE_ACCESS, IFSOpenReq.OPEN_OPTION_FAIL_OPEN);
          if (fileHandle == UNINITIALIZED)
          {
              if (Trace.traceOn_) Trace.log(Trace.ERROR, "Unable to create handle to file " + path_ + ". IFSReturnCodeRep return code", errorRC_);
              return false;
          }
          
          IFSChangeAttrsReq req = new IFSChangeAttrsReq(fileHandle, length, serverDatastreamLevel_);
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
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }
      finally {
          close(fileHandle);  // we don't need this handle anymore
      }

      // Verify the reply.
      boolean success = false;
      if (ds instanceof IFSReturnCodeRep)
      {
          int rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc == IFSReturnCodeRep.SUCCESS)
              success = true;
          else
          {
              Trace.log(Trace.ERROR, path_ + ": IFSReturnCodeRep return code", descriptionForReturnCode(rc));

              throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
          }
      }
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
          throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
      }

      // Back off the file pointer if needed.
      if (fileOffset_ > length)
          fileOffset_ = length;

      return success;
  }

  // Ignores fileHandle if state==false.
  void setOpen(boolean state, int fileHandle)
  {
      if (state == true)
      {
          if (fileHandle == UNINITIALIZED)
          {
              Trace.log(Trace.ERROR, "Called setOpen with invalid file handle: " + fileHandle);
              throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
          }
          else
          {
              if (fileHandle != fileHandle_) close(); // close currently-open handle if different
              fileHandle_ = fileHandle;
          }
      }
      
      isOpen_ = state;
  }

  void setOpenAllowed(boolean state) {
      isOpenAllowed_ = state;
  }

  void setPreferredCCSID(int ccsid) {
      preferredServerCCSID_ = ccsid;
  }

  void setServer(AS400Server server) {
      server_ = server;
  }
   
  @Override
  public void sync() throws IOException
  {
      // Note: UserSpaceImplRemote creates an IFSFileDescriptorImplRemote directly.

      if (parent_ == null)
          Trace.log(Trace.ERROR, "IFSFileDescriptor.sync() was called when parent is null.");
      else if (parent_ instanceof IFSRandomAccessFileImplRemote)
          ((IFSRandomAccessFileImplRemote)parent_).flush();
      else if (parent_ instanceof IFSFileOutputStreamImplRemote)
          ((IFSFileOutputStreamImplRemote)parent_).flush();
      else
      {
          Trace.log(Trace.WARNING, "IFSFileDescriptor.sync() was called " +
                                   "when parent is neither an IFSRandomAccessFile nor an IFSFileOutputStream.");
      }
  }

  private static final void throwSecurityExceptionIfAccessDenied(String path, int returnCode) throws AS400SecurityException
  {
      if (returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_DIR_ENTRY ||
          returnCode == IFSReturnCodeRep.ACCESS_DENIED_TO_REQUEST)
      {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Access denied to file " + path + ". " +
                                                          "IFSReturnCodeRep return code", returnCode);
          throw new AS400SecurityException(path, AS400SecurityException.DIRECTORY_ENTRY_ACCESS_DENIED);
      }
  }


  /**
   Undoes a lock on this file.
   @param key The key for the lock.
   **/
  void unlock(IFSKey key) throws IOException, AS400SecurityException
  {
      // Assume the argument has been validated by the caller.

      // Verify that this key is compatible with this file.
      if (key.fileHandle_ != fileHandle_)
      {
          Trace.log(Trace.ERROR, "Attempt to use IFSKey on different file stream.");
          throw new ExtendedIllegalArgumentException("key", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }

      // Attempt to unlock the file.
      ClientAccessDataStream ds = null;
      IFSUnlockBytesReq req = new IFSUnlockBytesReq(key.fileHandle_, key.offset_,
                                                    key.length_, key.isMandatory_, serverDatastreamLevel_);
       try {
           ds = (ClientAccessDataStream) server_.sendAndReceive(req);
       }
       catch(ConnectionDroppedException e)
       {
           Trace.log(Trace.ERROR, "Byte stream server connection lost");
           connectionDropped(e);
       }
       catch(InterruptedException e)
       {
           Trace.log(Trace.ERROR, "Interrupted", e);
           InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
           throwException.initCause(e);
           throw throwException;
       }

       // Verify the reply.
       if (ds instanceof IFSReturnCodeRep)
       {
           int rc = ((IFSReturnCodeRep) ds).getReturnCode();
           if (rc != IFSReturnCodeRep.SUCCESS)
           {
               throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
               Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
               throw new ExtendedIOException(path_, rc);
           }
       }
       else
       {
           Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
           throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
       }
  }


  // Common code used by IFSFileOutputStreamImplRemote and IFSRandomAccessFileImplRemote.
  /**
   Writes <i>length</i> bytes from the byte array <i>data</i>, starting at <i>dataOffset</i>, to this File.
   @param data The data.
   @param dataOffset The start offset in the data.
   @param length The number of bytes to write.
   @parm forceToStorage Whether data must be written before the server replies.
   **/
  void writeBytes(byte[] data, int dataOffset, int length) throws IOException, AS400SecurityException {
      writeBytes(data, dataOffset, length, false);
  }
  
  void writeBytes(byte[] data,  int dataOffset, int length, boolean forceToStorage) throws IOException, AS400SecurityException
  {
      // Assume the arguments have been validated by the caller.

      // Send write requests until all data has been written.
      while(length > 0)
      {
          // Determine how much data can be written on this request.
          int writeLength = (length > maxDataBlockSize_ ? maxDataBlockSize_ : length);

          // Build the write request.  Set the chain bit if there is
          // more data to write.
          IFSWriteReq req = new IFSWriteReq(fileHandle_, fileOffset_,
                                            data, dataOffset, writeLength,
                                            0xffff, forceToStorage, serverDatastreamLevel_);
          if (length - writeLength > 0)
          {
              // Indicate that there is more to write.
              req.setChainIndicator(1);
          }

          // Send the request.
          ClientAccessDataStream ds = null;
          try {
              ds = (ClientAccessDataStream) server_.sendAndReceive(req);
          }
          catch(ConnectionDroppedException e)
          {
              Trace.log(Trace.ERROR, "Byte stream server connection lost");
              connectionDropped(e);
          }
          catch(InterruptedException e)
          {
              Trace.log(Trace.ERROR, "Interrupted", e);
              InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
              throwException.initCause(e);
              throw throwException;
          }

          // Check the reply.
          if (ds instanceof IFSWriteRep)
          {
               IFSWriteRep rep = (IFSWriteRep) ds;
               int rc = rep.getReturnCode();
               if (rc != 0)
               {
                   Trace.log(Trace.ERROR, "IFSWriteRep return code", rc);
                   throw new ExtendedIOException(path_, rc);
               }

               // Advance the file pointer the length of the data written.
               int lengthWritten = writeLength - rep.getLengthNotWritten();
               incrementFileOffset(lengthWritten);
               dataOffset += lengthWritten;
               length -= lengthWritten;

               // Ensure that all data requested was written.
               if (lengthWritten != writeLength)
               {
                   Trace.log(Trace.ERROR, "Incomplete write.  Only " +
                                          Integer.toString(lengthWritten) + " bytes of a requested " +
                                          Integer.toString(writeLength) + " were written.");
                   throw new ExtendedIOException(path_, ExtendedIOException.UNKNOWN_ERROR);
               }
          }
          else if (ds instanceof IFSReturnCodeRep)
          {
              int rc = ((IFSReturnCodeRep) ds).getReturnCode();
              if (rc != IFSReturnCodeRep.SUCCESS)
              {
                  throwSecurityExceptionIfAccessDenied(path_,rc); // check for "access denied"
                  Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", descriptionForReturnCode(rc));
                  throw new ExtendedIOException(path_, rc);
              }
          }
          else
          {
              Trace.log(Trace.ERROR, "Unknown reply data stream", ds.data_);
              throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
          }
      }
  }
  
  public int getServerDatastreamLevel() {
      return serverDatastreamLevel_;
  }
  
  public void freeHandle(int objectHandle) throws IOException, AS400SecurityException{
      IFSFreeHandleReq req = new IFSFreeHandleReq(objectHandle);
      server_.send(req);
  }
  
  public int getASP() throws IOException, AS400SecurityException 
  {
      if (fileAsp_ == UNINITIALIZED)
      { 
          ClientAccessDataStream ds = null;
          int rc = 0;

          connect();

          // Ensure that we are connected to the server.
          byte[] pathname = getConverter().stringToByteArray(path_);

          //create user handle
          int userHandle = UNINITIALIZED, objectHandle = UNINITIALIZED;
          try
          {
              // In 7.5 and prior releases, need to create user handle for IFS tables to be initialized.
              userHandle = (getSystem().getVRM() <= 0x00070500) ? system_.createUserHandle() : 0;
              
              try
              {
                  // Issue a Look up request to create an object handle.
                  IFSLookupReq req = new IFSLookupReq(pathname, preferredServerCCSID_, userHandle, IFSLookupReq.OA12, IFSObjAttrs1.OWNERANAME_ASP_FLAS, 0); 
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
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }

              rc = 0;
              if (ds instanceof IFSLookupRep)
              {
                  objectHandle = ((IFSLookupRep) ds).getHandle();
                  retrieveAttributes(ds, objectHandle);  //@AC7A 
              }
              else if (ds instanceof IFSReturnCodeRep)
              {
                  rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  if (rc != IFSReturnCodeRep.SUCCESS)
                      Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);

                  throw new ExtendedIOException(path_, rc);
              }
              else
              {
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }
          }
          finally
          {
              if (objectHandle != UNINITIALIZED)
                  freeHandle(objectHandle);
          }
      }

      return fileAsp_;
  }
  
  public String getOwnerNameByUserHandle(boolean forceRetrieve) throws IOException, AS400SecurityException
  {
      Trace.log(Trace.INFORMATION, "Owner Name " + (fileOwnerName_ == null? "is null" : fileOwnerName_));
      
      if (forceRetrieve || fileOwnerName_ == null)
      {
          Trace.log(Trace.INFORMATION, "force retrieve for Owner Name.");
          ClientAccessDataStream ds = null;
          int rc = 0;

          // Ensure that we are connected to the server.
          connect();
          byte[] pathname = getConverter().stringToByteArray(path_);
        
          //create user handle
          int userHandle = UNINITIALIZED, objectHandle = UNINITIALIZED;
          try
          {
              // In 7.5 and prior releases, need to create user handle for IFS tables to be initialized.
              userHandle = (getSystem().getVRM() <= 0x00070500) ? system_.createUserHandle() : 0;
              
              try
              {
                  // Issue a Look up request to create an object handle.
                  IFSLookupReq req = new IFSLookupReq(pathname, preferredServerCCSID_, userHandle, IFSLookupReq.OA12, IFSObjAttrs1.OWNERANAME_ASP_FLAS, 0); 
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
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }

              // Verify that we got a handle back.
              rc = 0;
              if (ds instanceof IFSLookupRep)
              {
                  objectHandle = ((IFSLookupRep) ds).getHandle();
                  retrieveAttributes(ds, objectHandle);  //@AC7A
              }
              else if (ds instanceof IFSReturnCodeRep)
              {
                  rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  if (rc != IFSReturnCodeRep.SUCCESS) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
                  throw new ExtendedIOException(path_, rc);
              } 
              else
              {
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }
          }
          finally
          {
              if(objectHandle != UNINITIALIZED)
                  freeHandle(objectHandle);
          }
      }
      
      return (fileOwnerName_ == null ? "" : fileOwnerName_);
  }
  
  public String getFileSystemType() throws IOException, AS400SecurityException
  {
      String typeString = "Unknown";
    
      if (fileSystemType_ == UNINITIALIZED)
      {
          ClientAccessDataStream ds = null;
          int rc = 0;
          connect();

          int objectHandle = UNINITIALIZED;
          byte[] pathname = getConverter().stringToByteArray(path_);

          //create user handle
          int userHandle = UNINITIALIZED;
          try
          {
              userHandle = (getSystem().getVRM() <= 0x00070500) ? system_.createUserHandle() : 0;
              
              try
              {
                  // Issue a Look up request to create an object handle.
                  IFSLookupReq req = new IFSLookupReq(pathname, preferredServerCCSID_, userHandle, IFSLookupReq.OA12, IFSObjAttrs1.OWNERANAME_ASP_FLAS, 0);
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
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }

              rc = 0;
              if (ds instanceof IFSLookupRep)
              {
                  objectHandle = ((IFSLookupRep) ds).getHandle();
                  retrieveAttributes(ds, objectHandle); //@AC7A 
              }
              else if (ds instanceof IFSReturnCodeRep)
              {
                  rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  if (rc != IFSReturnCodeRep.SUCCESS)
                      Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);

                  throw new ExtendedIOException(path_, rc);
              }
              else
              {
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }
          }
          finally
          {
              if (objectHandle != UNINITIALIZED)
                  freeHandle(objectHandle);
          }
      }
      
    switch(fileSystemType_)
    {
        case 1 : typeString = "EPFS"; break;
        case 2 : typeString = "QDLS"; break;
        case 3 : typeString = "QSYS"; break;
        case 4 : typeString = "NFS"; break;
        case 5 : typeString = "LRFS"; break;
        case 6 : typeString = "QOPT"; break;
        case 7 : typeString = "QRFS"; break;
        case 9 : typeString = "EPFSP"; break;
        case 11 : typeString = "QNETC"; break;
        case 12 : typeString = "QDTL"; break;
        case 13 : typeString = "IEPFS"; break;
        case 14 : typeString = "ASPQSYS"; break;
    }
    
    return typeString;
  }

  private void retrieveAttributes(ClientAccessDataStream ds, int objectHandle) throws IOException, AS400SecurityException
  {
      fileAsp_ = ((IFSLookupRep) ds).getASP();
      if (fileAsp_ < 1)  fileAsp_ = UNINITIALIZED;
      
      fileOwnerName_ = ((IFSLookupRep) ds).getOwnerName(system_.getCcsid());
      
      fileDataCCSID_ = ((IFSLookupRep) ds).getCCSID(serverDatastreamLevel_);
      if (fileDataCCSID_ < 1)  fileDataCCSID_ = UNINITIALIZED;

      int userHandle =  (getSystem().getVRM() <= 0x00070500) ? system_.getUserHandle() : 0;    
      
      int rc = 0;
      ds = null;
      try
      {
          IFSGetFileSystemReq req = new IFSGetFileSystemReq(objectHandle, userHandle);
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
          InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
          throwException.initCause(e);
          throw throwException;
      }

      // Verify the reply.
      rc = 0;
      if (ds instanceof IFSGetFileSystemRep)
          fileSystemType_ = ((IFSGetFileSystemRep) ds).getFileSystemType();
      else if (ds instanceof IFSReturnCodeRep)
      {
          rc = ((IFSReturnCodeRep) ds).getReturnCode();
          if (rc != IFSReturnCodeRep.SUCCESS) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
          throw new ExtendedIOException(path_, rc);
      }
      else
      {
          Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
          throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, Integer.toHexString(ds.getReqRepID()),null);
      }
  }
}




