///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FileEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.FileEvent;
import com.ibm.as400.access.FileListener;
import java.util.Vector;



/**
The FileEventSupport class represents a list of FileListeners.
This is also a FileListener and will dispatch all file events.
**/
class FileEventSupport
implements FileListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   FileListener[]  listeners_      = new FileListener[0]; // For speed.
    private transient   Vector          listenersV_     = new Vector ();
    private             Object          source_;



/**
Constructs a FileEventSupport object.

@param  source      The source of the events.
**/
    public FileEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addFileListener (FileListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new FileListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Processes a file closed event.

@param  event   The event.
**/
    public void fileClosed (FileEvent event)
    {
        fireFileClosed ();
    }



/**
Processes a file created event.

@param  event   The event.
**/
    public void fileCreated (FileEvent event)
    {
        fireFileCreated ();
    }



/**
Processes a file deleted event.

@param  event   The event.
**/
    public void fileDeleted (FileEvent event)
    {
        fireFileDeleted ();
    }



/**
Processes a file modified event.

@param  event   The event.
**/
    public void fileModified (FileEvent event)
    {
        fireFileModified ();
    }



/**
Processes a file open event.

@param  event   The event.
**/
    public void fileOpened (FileEvent event)
    {
         fireFileOpened ();
    }



/**
Fires a file closed event.
**/
    public void fireFileClosed ()
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].fileClosed (new FileEvent (source_, FileEvent.FILE_CLOSED));
        }
    }



/**
Fires a file created event.
**/
    public void fireFileCreated ()
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].fileCreated (new FileEvent (source_, FileEvent.FILE_CREATED));
        }
    }



/**
Fires a file deleted event.
**/
    public void fireFileDeleted ()
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].fileClosed (new FileEvent (source_, FileEvent.FILE_DELETED));
        }
    }



/**
Fires a file modified event.
**/
    public void fireFileModified ()
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].fileClosed (new FileEvent (source_, FileEvent.FILE_MODIFIED));
        }
    }



/**
Fires a file open event.
**/
    public void fireFileOpened ()
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].fileClosed (new FileEvent (source_, FileEvent.FILE_OPENED));
        }
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeFileListener (FileListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new FileListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }


}


