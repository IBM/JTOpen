///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSFileDescriptorImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;


/**
 Provides a full remote implementation for the IFSFileDescriptor class.
 **/
class IFSFileDescriptorImplRemote
implements IFSFileDescriptorImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



  private String x = Copyright.copyright;
  private ConverterImplRemote converter_;
  private int         fileHandle_;
  private int         preferredServerCCSID_;
  private int         fileOffset_;
          boolean     isOpen_;
          boolean     isOpenAllowed_ = true;
  private Object      parent_;  // The object that instantiated this IFSDescriptor.
  private String      path_ = "";
  private AS400Server server_;  // Note: AS400Server is not serializable.
  private int         shareOption_;
  private AS400ImplRemote system_;

  private Boolean     fileOffsetLock_ = new Boolean("true");
                         // Semaphore for synchronizing access to fileOffset_.

  public void close()
  {
    isOpen_ = false;
  }

  ConverterImplRemote getConverter()
  {
    return converter_;
  }

  int getFileHandle()
  {
    return fileHandle_;
  }

  public int getFileOffset()
  {
    return fileOffset_;
  }

  Object getParent()
  {
    return parent_;
  }

  String getPath()
  {
    return path_;
  }

  int getPreferredCCSID()
  {
    return preferredServerCCSID_;
  }

  AS400Server getServer()
  {
    return server_;
  }

  int getShareOption()
  {
    return shareOption_;
  }

  AS400ImplRemote getSystem()
  {
    return system_;
  }

  public void incrementFileOffset(int fileOffsetIncrement)
  {
    synchronized(fileOffsetLock_)
    {
      fileOffset_ += fileOffsetIncrement;
    }
  }

  public void initialize(int fileOffset, Object parentImpl, String path, int shareOption,
                         AS400Impl system)
  {
    fileOffset_           = fileOffset;
    parent_               = parentImpl;
    path_                 = path;
    shareOption_          = shareOption;
    system_               = (AS400ImplRemote) system;
  }

  public boolean isOpen()
  {
    return isOpen_;
  }

  boolean isOpenAllowed()
  {
    return isOpenAllowed_;
  }

  void setConverter(ConverterImplRemote converter)
  {
    converter_ = converter;
  }

  void setFileHandle(int fileHandle)
  {
    fileHandle_ = fileHandle;
  }

  public void setFileOffset(int fileOffset)
  {
    synchronized(fileOffsetLock_)
    {
      fileOffset_ = fileOffset;
    }
  }

  void setOpen(boolean state)
  {
    isOpen_ = state;
  }

  void setOpenAllowed(boolean state)
  {
    isOpenAllowed_ = state;
  }

  void setPreferredCCSID(int ccsid)
  {
    preferredServerCCSID_ = ccsid;
  }

  void setServer(AS400Server server)
  {
    server_ = server;
  }

  /**
   Force the system buffers to synchronize with the underlying device.
  **/                                                                      // $A1
  public void sync() throws IOException
  {
    if (parent_ == null)
    {
      Trace.log(Trace.ERROR, "IFSFileDescriptor.sync() was called when parent is null.");
    }
    // Note: UserSpaceImplRemote creates an IFSFileDescriptorImplRemote directly.
    else if (parent_ instanceof IFSRandomAccessFileImplRemote)
    {
      ((IFSRandomAccessFileImplRemote)parent_).flush();
    }
    else if (parent_ instanceof IFSFileOutputStreamImplRemote)
    {
      ((IFSFileOutputStreamImplRemote)parent_).flush();
    }
    else
    {
      Trace.log(Trace.WARNING, "IFSFileDescriptor.sync() was called " +
      "when parent is neither an IFSRandomAccessFile nor an IFSFileOutputStream.");
    }
  }

}




