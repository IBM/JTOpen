///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




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

        // See if "restart by name" is supported for this directory.     @A1a
        String path = file_.getPath().toUpperCase();
        if (path.startsWith("/QSYS.LIB") || path.startsWith("/QDLS.LIB")) {
          isRestartByNameSupported_ = true;
        }
        else {
          isRestartByNameSupported_ = false;
          Trace.log(Trace.WARNING,
                    "Restart-by-name is not supported for directory " + path);
        }

        loadPendingBlock(null);// "Prime the pump" with the first block.  @A1a
        nextBlock();
    }


    public boolean hasMoreElements()
    {
        return ((contents_ != null && index_ < contents_.length) ||
                (contentsPending_ != null));  // @A1c
    }


    // @A1a
    private void loadPendingBlock(String restartName)
    throws AS400SecurityException, IOException
    {
      // Design note: Using contents_ and contentsPending_ allows us to "look ahead" and detect end-of-list in all situations, including when the number of matching files in the directory is an exact multiple of MAXIMUM_GET_COUNT_.
      contentsPending_ = file_.listFiles0(filter_, pattern_, MAXIMUM_GET_COUNT_, restartName);
      if (contentsPending_.length == 0) contentsPending_ = null;
    }


    // Assumes loadPendingBlock() has been called once, to initialize contentsPending_.
    private void nextBlock()
    throws AS400SecurityException, IOException
    {
      // @A1a
      if (contentsPending_ == null) {
        Trace.log(Trace.DIAGNOSTIC,
                  "nextBlock() was called when contentsPending_==null.");
        return;
      }

      // Move the pending-contents into current-contents.
      contents_ = contentsPending_;
      index_ = 0;

      // If contentsPending held fewer than max_get_count entries, we know that there are no more entries remaining to be read from the server.
      if (contents_.length < MAXIMUM_GET_COUNT_) {
        contentsPending_ = null; // We're done.
      }
      else // length == max_get_count
      {
        if (isRestartByNameSupported_)
        {
          // Load the next block from the system.
          loadPendingBlock(restartName_);
        }
        else
        {
          // "Restart by name" only works in QSYS and QDLS.
          Trace.log(Trace.WARNING,
                    "Only the first " + MAXIMUM_GET_COUNT_ + " list entries were read from system.");
          contentsPending_ = null; // We're done.
        }
      }

      if (contentsPending_ != null) {
        restartName_ = contentsPending_[contentsPending_.length - 1].getName();
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
                nextBlock();
            }
            catch(Exception e) {
                throw new NoSuchElementException();
            }
            return contents_[index_++];
        }
    }


}

