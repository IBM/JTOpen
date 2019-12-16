///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DocumentEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Vector;



/**
The DocumentEventSupport class represents a list of
DocumentListeners.  This is also a DocumentListener
and will dispatch all document events.
**/
class DocumentEventSupport
implements DocumentListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private transient   DocumentListener[]      listeners_      = new DocumentListener[0]; // For speed.
    private transient   Vector                  listenersV_     = new Vector ();
    private             Object                  source_;



/**
Constructs a DocumentEventSupport object.

@param  source          The source of the events.
**/
    public DocumentEventSupport (Object source)
    {
        source_ = source;
    }



/**
Adds a listener.

@param  listener    The listener.
**/
    public void addDocumentListener (DocumentListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        listenersV_.addElement (listener);
        synchronized (listeners_) {
            listeners_ = new DocumentListener[listenersV_.size()];
            listenersV_.copyInto (listeners_);
        }
    }



/**
Processes a changed update event.

@param  event       The event.
**/
    public void changedUpdate (DocumentEvent event)
    {
        fireChangedUpdate (event);
    }



/**
Fires a changed update event.

@param  event   The event.
**/
    public void fireChangedUpdate (DocumentEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].changedUpdate (event);
        }
    }



/**
Fires a insert update event.

@param  event   The event.
**/
    public void fireInsertUpdate (DocumentEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].insertUpdate (event);
        }
    }



/**
Fires a remove update event.

@param  event   The event.
**/
    public void fireRemoveUpdate (DocumentEvent event)
    {
        synchronized (listeners_) {
            for (int i = 0; i < listeners_.length; ++i)
                listeners_[i].removeUpdate (event);
        }
    }



/**
Processes a insert update event.

@param  event       The event.
**/
    public void insertUpdate (DocumentEvent event)
    {
        fireInsertUpdate (event);
    }



/**
Removes a listener.

@param  listener    The listener.
**/
    public void removeDocumentListener (DocumentListener listener)
    {
        if (listener == null)
            throw new NullPointerException ("listener");

        if (listenersV_.removeElement (listener)) {
            synchronized (listeners_) {
                listeners_ = new DocumentListener[listenersV_.size()];
                listenersV_.copyInto (listeners_);
            }
        }
    }


/**
Processes a remove update event.

@param  event       The event.
**/
    public void removeUpdate (DocumentEvent event)
    {
        fireRemoveUpdate (event);
    }



}


