///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;


class IFSFileEnumeration
implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";




    // The block size is hardcoded based on the value chosen by OpNav.  
    // For now, it is not configurable.
    private static final int MAXIMUM_GET_COUNT_ = 128;



    private IFSFile[]       contents_;
    private IFSFile[]       contentsPending_;  // Staging area for contents_   @A1a
    private IFSFile         file_;
    private IFSFileFilter   filter_;
    private int             index_;
    private String          pattern_;
    private String          restartName_;
    private byte[]          restartID_;  // @C3a
    private boolean         isRestartByNameSupported_;  // @A1a



    IFSFileEnumeration(IFSFile file, IFSFileFilter filter, String pattern)
    throws AS400SecurityException, IOException
    {
        file_ = file;
        filter_ = filter;
        pattern_ = pattern;

        // @A1a:
        // Note from the File Server team on 02/05/01:
        // "The vnode architecture allows a file system to use the cookie
        // (Restart Number) or a Restart Name to find the entry
        // that processing should start at.
        // QDLS and QSYS allow Restart Name, but /root (EPFS) does not."

        // "Restart by name" only works for QSYS.LIB and QDLS.       @A1a
        String path = file_.getPath().toUpperCase();
        if (path.startsWith("/QSYS.LIB") || path.startsWith("/QDLS.LIB") ||
            path.startsWith("/QDLS/") || (path.indexOf("/QSYS.LIB") <= 11)) {  // @C3a  //@D1C - added support to look for /IASPNAME../QSYS.LIB
          isRestartByNameSupported_ = true;
          contentsPending_ = loadPendingBlock((String)null);// "Prime the pump" with the first block.  @A1a @C3c
          if (contentsPending_ != null) {                          // @C3a
            restartName_ = contentsPending_[contentsPending_.length - 1].getName();
          }
        }
        else { // Use "restart by ID".
          isRestartByNameSupported_ = false;
//@C3d    if (Trace.traceOn_) Trace.log(Trace.WARNING,
//@C3d        "Restart-by-name is not supported for directory " + path);
          contentsPending_ = loadPendingBlock((byte[])null);// "Prime the pump" with the first block.  @C3a
          if (contentsPending_ != null) {                          // @C3a
            restartID_ = contentsPending_[contentsPending_.length - 1].getRestartID();
          }
        }

        getNextBlock();
    }


    public boolean hasMoreElements()
    {
        return ((contents_ != null && index_ < contents_.length) ||
                (contentsPending_ != null));  // @A1c
    }


    // @A1a @C3c
    private IFSFile[] loadPendingBlock(String restartName)
    throws AS400SecurityException, IOException
    {
      // Design note: Using contents_ and contentsPending_ allows us to "look ahead" and detect end-of-list in all situations, including when the number of matching files in the directory is an exact multiple of MAXIMUM_GET_COUNT.
      IFSFile[] block = file_.listFiles0(filter_, pattern_, MAXIMUM_GET_COUNT_, restartName);
      if (block.length == 0) block = null;  // Never return an empty list.
      return block;
    }


    // @C3a
    private IFSFile[] loadPendingBlock(byte[] restartID)
    throws AS400SecurityException, IOException
    {
      // Design note: Using contents_ and contentsPending_ allows us to "look ahead" and detect end-of-list in all situations, including when the number of matching files in the directory is an exact multiple of MAXIMUM_GET_COUNT.
      IFSFile[] block = file_.listFiles0(filter_, pattern_, MAXIMUM_GET_COUNT_, restartID);
      if (block.length == 0) block = null;  // Never return an empty list.
      return block;
    }


    // Transfers the "pending block" into the "current block".
    // Assumes loadPendingBlock() has been called at least once, to initialize contentsPending_.
    private void getNextBlock()
    throws AS400SecurityException, IOException
    {
      // @A1a
      if (contentsPending_ == null) {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC,
                  "getNextBlock() was called when contentsPending_==null.");
        return;
      }

      // Transfer the "pending contents" into "current contents".
      contents_ = contentsPending_;
      contentsPending_ = null;                                            // @C3M
      index_ = 0;

      // If contentsPending held fewer than max_get_count entries, we know that there are no more entries remaining to be read from the server.

      // Note: Prior to V5R2, the file list returned by the "List Contents of Directory" request included "." and "..", which get weeded out by IFSFileImplRemote, so we need to check for (max_get_count - 2).     @C3A
      if (contents_.length < MAXIMUM_GET_COUNT_-2) {                      // @C3C
        // We're done.                                                    // @C3C
      }
      else // length == max_get_count
      {
        if (isRestartByNameSupported_)
        {
          if (restartName_ == null) {                                     // @C3A
            if (Trace.traceOn_) Trace.log(Trace.ERROR,
                                          "getNextBlock() was called when restartName_==null.");
            return;
          }
          // Load the next block from the system.
          contentsPending_ = loadPendingBlock(restartName_);              // @C3c
          if (contentsPending_ != null) {                                 // @C3a
            restartName_ = contentsPending_[contentsPending_.length - 1].getName();
          }
        }
        else
        {
          if (restartID_ == null) {                                       // @C3a
            if (Trace.traceOn_) Trace.log(Trace.ERROR,
                                          "getNextBlock() was called when restartID_==null.");
            return;
          }
          // Load the next block from the system.
          contentsPending_ = loadPendingBlock(restartID_);                // @C3c
          if (contentsPending_ != null) {                                 // @C3a
            restartID_ = contentsPending_[contentsPending_.length - 1].getRestartID();
          }
        }
      }
    }


    public Object nextElement()
    {
        if (index_ < contents_.length)
            return contents_[index_++];
        else if (contentsPending_ == null)
            throw new NoSuchElementException();
        else {
            try {
                getNextBlock();
            }
            catch(Exception e) {
                throw new NoSuchElementException();
            }
            return contents_[index_++];
        }
    }


}
