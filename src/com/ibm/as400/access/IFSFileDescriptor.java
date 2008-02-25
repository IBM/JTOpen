///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileDescriptor.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @A1 2008-02-22 Change setFileOffset() parameter from int to long 
//     This is a private method and calls the impl.setFileOffset which already
//     takes a long.
//     setFileOffset() is called from IFSRandomAccessFile.seek(long)
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;


/**
The IFSFileDescriptor class represents an integrated file system file descriptor.
Instances of the file descriptor class serve as an opaque handle to the underlying structure representing an open file or an open socket.   Applications should not create their own file descriptors.<br>
Here is an example of two input streams sharing a file descriptor:
<pre>
    AS400 as400 = new AS400("as400");
    IFSFileInputStream is1 = new IFSFileInputStream(as400, "/Dir/File");
    IFSFileInputStream is2 = new IFSFileInputStream(is1.getFD());
</pre>
Reading in one object advances the current file position of all objects that share the same descriptor.
@see IFSFileInputStream#getFD
@see IFSFileOutputStream#getFD
@see IFSRandomAccessFile#getFD
**/
public final class IFSFileDescriptor
  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;



  private transient long        fileOffset_;
  private transient Object      parent_;  // The object that instantiated
                                          // this IFSDescriptor.
  private String                path_ = "";
  private int                   shareOption_;
  private AS400                 system_;
  private boolean               closed_ = false;

  private transient Boolean     fileOffsetLock_ = new Boolean("true");
                         // Semaphore for synchronizing access to fileOffset_.

  private transient IFSFileDescriptorImpl impl_;


/**
Constructs an IFSFileDescriptor object.
**/
  public IFSFileDescriptor()
  {
  }

  IFSFileDescriptor(AS400  system,
                    String path,
                    int    shareOption,
                    Object parent)
  {
    system_ = system;
    path_ = path;
    shareOption_ = shareOption;
    parent_ = parent;
  }

  IFSFileDescriptor(int    shareOption,
                    Object parent)
  {
    shareOption_ = shareOption;
    parent_ = parent;
  }

  /**
   Chooses the appropriate implementation.
   **/
  void chooseImpl ()
  {
    if (impl_ == null)
    {
      impl_ = (IFSFileDescriptorImpl) system_.loadImpl2
        ("com.ibm.as400.access.IFSFileDescriptorImplRemote",
         "com.ibm.as400.access.IFSFileDescriptorImplProxy");

      // Get the "impl" object for the parent, so we can pass it to the impl_.
      Object parentImpl = null;
      if (parent_ != null)
      {
        // Get the impl object for the parent.
        //@A3D Class clazz = parent_.getClass ();
        //@A3D Method method = clazz.getDeclaredMethod ("getImpl", new Class[] {});
        //@A3D parentImpl = method.invoke (parent_, new Object[] {});
        //@A3A
        String className = parent_.getClass().getName();
        if (parent_ instanceof IFSFileInputStream) {
          parentImpl = ((IFSFileInputStream)parent_).getImpl();
        }
        else if (parent_ instanceof IFSFileOutputStream) {
          parentImpl = ((IFSFileOutputStream)parent_).getImpl();
        }
        else if (parent_ instanceof IFSTextFileInputStream) {
          parentImpl = ((IFSTextFileInputStream)parent_).getImpl();
        }
        else if (parent_ instanceof IFSTextFileOutputStream) {
          parentImpl = ((IFSTextFileOutputStream)parent_).getImpl();
        }
        else if (parent_ instanceof IFSRandomAccessFile) {
          parentImpl = ((IFSRandomAccessFile)parent_).getImpl();
        }
        else {
          Trace.log(Trace.ERROR, "IFSFileDescriptor has invalid parent: " + className);
          throw new InternalErrorException (InternalErrorException.UNEXPECTED_EXCEPTION);
        }
      }

      impl_.initialize(fileOffset_, parentImpl,
                       path_, shareOption_, system_.getImpl());
    }
  }

  void close()
  {
    if (impl_ != null) impl_.close();
    closed_ = true;
  }


  /**
   Fires events to indicate file has been closed.
   This static method is provided as a utility for use by other classes.
   **/
  static void fireClosedEvents(Object source, Vector fileListeners)
  {
    FileEvent event = new FileEvent(source, FileEvent.FILE_CLOSED);
    synchronized(fileListeners)
    {
      Enumeration e = fileListeners.elements();
      while (e.hasMoreElements())
      {
        FileListener listener = (FileListener)e.nextElement();
        listener.fileClosed(event);
      }
    }
  }


  /**
   Fires events to indicate file has been modified.
   This static method is provided as a utility for use by other classes.
   **/
  static void fireModifiedEvents(Object source, Vector fileListeners)
  {
    FileEvent event = new FileEvent(source, FileEvent.FILE_MODIFIED);
    synchronized(fileListeners)
    {
      Enumeration e = fileListeners.elements();
      while (e.hasMoreElements())
      {
        FileListener listener = (FileListener)e.nextElement();
        listener.fileModified(event);
      }
    }
  }


  /**
   Fires events to indicate file has been opened.
   This static method is provided as a utility for use by other classes.
   **/
  static void fireOpenedEvents(Object source, Vector fileListeners)
  {
    FileEvent event = new FileEvent(source, FileEvent.FILE_OPENED);
    synchronized(fileListeners)
    {
      Enumeration e = fileListeners.elements();
      while (e.hasMoreElements())
      {
        FileListener listener = (FileListener)e.nextElement();
        listener.fileOpened(event);
      }
    }
  }


  // Design note: The policy for 'get' and 'is' methods is:
  // If impl_ is null, or the field is never reset by the ImplRemote,
  // simply return the local copy of the data member.
  // If impl_ is non-null, and the field can be reset by the ImplRemote,
  // get the value from impl_ (and disregard the local value).
  // Note that various IFSxxxImplRemote classes are given direct access to impl_.

  IFSFileDescriptorImpl getImpl()
  {
    if (impl_ == null) chooseImpl();
    return impl_;
  }

  /**
   Returns the file's "data CCSID" setting.
   **/
  int getCCSID()
    throws IOException
  {
    if (impl_ == null) chooseImpl();
    return impl_.getCCSID();
  }

  long getFileOffset()
  {
    if (impl_ == null)
    {
      return fileOffset_;
    }
    else
      return impl_.getFileOffset();
  }

  String getPath()
  {
    return path_;  // this field is never reset by the ImplRemote
  }

  int getShareOption()
  {
    return shareOption_;  // this field is never reset by the ImplRemote
  }

  AS400 getSystem()
  {
    return system_;  // this field is never reset by the ImplRemote
  }

  void incrementFileOffset(int fileOffsetIncrement)
  {
    if (impl_ == null)
    {
      synchronized(fileOffsetLock_)
      {
        fileOffset_ += fileOffsetIncrement;
      }
    }
    else
      impl_.incrementFileOffset(fileOffsetIncrement);
  }

  boolean isClosed()
  {
    return closed_;
  }

  boolean isOpen()
  {
    if (impl_ == null)
      return false;
    else
      return impl_.isOpen();
  }

  /**
   Restores the state of this object from an object input stream.
   @param ois The stream of state information.
   @exception IOException If an error occurs while communicating with the server.
   @exception ClassNotFoundException
   **/
  private void readObject(java.io.ObjectInputStream ois)
    throws IOException, ClassNotFoundException
  {
    // Restore the non-static and non-transient fields.
    ois.defaultReadObject();

    // Initialize the transient fields.
    fileOffset_ = 0;
    parent_ = null;
    fileOffsetLock_ = new Boolean("true");
    impl_ = null;
  }

  void setFileOffset(long fileOffset) //@A1C - Change parameter from int to long
  {
    if (impl_ == null)
    {
      synchronized(fileOffsetLock_)
      {
        fileOffset_ = fileOffset;
      }
    }
    else
      impl_.setFileOffset(fileOffset);
  }

  void setPath(String path)
  {
    if (impl_ == null)
      path_ = path;
    else
      throw new ExtendedIllegalStateException("path",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
  }

  void setShareOption(int shareOption)
  {
    if (impl_ == null)
      shareOption_ = shareOption;
    else
      throw new ExtendedIllegalStateException("shareOption",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
  }

  void setSystem(AS400 system)
  {
    if (impl_ == null)
      system_ = system;
    else
      throw new ExtendedIllegalStateException("system",
                      ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
  }

  /**
   Force the system buffers to synchronize with the underlying device.
  **/                                                                      // $A1
  public void sync() throws IOException
  {
    if (impl_ == null)
    {
      if (parent_ == null)
      {
        Trace.log(Trace.ERROR, "Parent is null, nothing to synchronize.");
        // Tolerate the error.
      }
      else if (parent_ instanceof IFSRandomAccessFile)
      {
        ((IFSRandomAccessFile)parent_).flush();
      }
      else if (parent_ instanceof IFSFileOutputStream)
      {
        ((IFSFileOutputStream)parent_).flush();
      }
      else
      {
        Trace.log(Trace.ERROR, "Parent does not have a flush() method: " +
                               parent_.getClass().getName());
        // Tolerate the error.
      }
    }
    else
    {
      impl_.sync();
    }
  }

/**
Determines if this file descriptor represents an open file.
@return true if this file descriptor represents a valid, open file; false otherwise.
**/
  public boolean valid()
  {
    return isOpen();
  }
}




