///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataQueueDocument.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.DataQueue;
import com.ibm.as400.access.DataQueueEntry;
import com.ibm.as400.access.DataQueueEvent;
import com.ibm.as400.access.DataQueueListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;



/**
The DataQueueDocument class represents an underlying model
for text components, where the text is entries from a
data queue on an AS/400.

<p>Use this class in conjuction with any JTextComponent or
any other component that works with the Document interface.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>DataQueueDocument objects generate the following events:
<ul>
    <li>DataQueueEvent
    <li>DocumentEvent
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>UndoableEditEvent
    <li>WorkingEvent
</ul>

<p>The following example creates a document which contains
the next entry in a data queue on an AS/400.  It then
presents the document in a JTextField object.
<pre>
// Set up the document and the JTextField.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
DataQueueDocument document = new DataQueueDocument (system, "/QSYS.LIB/MYLIB.LIB/MYDATAQ.DTAQ");
JTextField textField = new JTextField (document, "", 50);
<br>
// Add the JTextField to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add(new JScrollPane(textField));


<br>
// Read the next entry from the data queue.
document.read ();
</pre>
**/
//
// Implementation notes:
//
// * I decided to contain the other document as opposed to extending
//   it so that our interface does not need to document what type
//   of document we are.
//
// * I do not think that it is necessary to test the methods
//   that just do a load and the call the underlying document.
//
public class DataQueueDocument
implements Document, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    private DataQueue                           dataQueue_         = null;



    // Private data.
    transient private PlainDocument             document_;



    // Event support.
    transient private DocumentEventSupport      documentEventSupport_ ;
    transient private DataQueueEventSupport     dataQueueEventSupport_;
    transient private ErrorEventSupport         errorEventSupport_;
    transient private PropertyChangeSupport     propertyChangeSupport_;
    transient private UndoableEditEventSupport  undoableEditEventSupport_;
    transient private VetoableChangeSupport     vetoableChangeSupport_;
    transient private WorkingEventSupport       workingEventSupport_;



/**
Constructs a DataQueueDocument object.
**/
    public DataQueueDocument ()
    {
        dataQueue_ = new DataQueue ();
        initializeTransient ();
    }



/**
Constructs a DataQueueDocument object.

@param      system                   The AS/400 on which the data queue resides.
@param      path                     The fully qualified integrated file system path name of the data queue. The path  must be in the format of /QSYS.LIB/libname.LIB/dataQueue.DTAQ.  The library and queue name must each be 10 characters or less.
**/
    public DataQueueDocument (AS400 system, String path)
    {
        dataQueue_ = new DataQueue (system, path);
        initializeTransient ();
    }



/**
Adds a listener to be notified when a data queue event occurs.

@param  listener  The listener.
**/
    public void addDataQueueListener (DataQueueListener listener)
    {
        dataQueueEventSupport_.addDataQueueListener (listener);
    }



/**
Adds a listener to be notified when a document event occurs.

@param  listener  The listener.
**/
    public void addDocumentListener (DocumentListener listener)
    {
        documentEventSupport_.addDocumentListener (listener);
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener  The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }



/**
Adds an undoable edit listener to be notified when undoable
edits are made to the document.

@param  listener  The listener.
**/
    public void addUndoableEditListener (UndoableEditListener listener)
    {
        undoableEditEventSupport_.addUndoableEditListener (listener);
    }



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener  The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Returns a position that will track changes as the document is altered.
If the relative position is null, then the start  of the document will
be used.

@param offset   The offset from the start of the document.
@return         The position.

@exception      BadLocationException    If the given offset does not
                                        represent a valid location in the
                                        document.
**/
    public synchronized Position createPosition (int offset)
        throws BadLocationException
    {
        return document_.createPosition (offset);
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Returns the root element that views should be based upon
unless some other mechanism for assigning views to element
structures is provided.

@return The root element.
**/
    public synchronized Element getDefaultRootElement ()
    {
        return document_.getDefaultRootElement ();
    }



/**
Returns a position that represents the end of the document.
The position returned can be counted on to track change and stay
located at the end of the document.

@return The end position.
**/
    public synchronized Position getEndPosition ()
    {
        return document_.getEndPosition ();
    }



/**
Returns the length of the document.

@return The length of the document in characters.
**/
    public synchronized int getLength ()
    {
        return document_.getLength ();
    }



/**
Returns the fully qualified integrated file system path name of the data queue.

@return  The fully qualified integrated file system path name of the data queue.
**/
    public String getPath ()
    {
        return dataQueue_.getPath ();
    }



/**
Returns a property value associated with the document.

@param  key     The property key.
@return         The property value.
**/
    public synchronized Object getProperty (Object key)
    {
        return document_.getProperty (key);
    }



/**
Returns the root elements.

@return The root elements.
**/
    public synchronized Element[] getRootElements ()
    {
        return document_.getRootElements ();
    }



/**
Returns a position that represents the start of the document.
The position returned can be counted on to track change and stay
located at the beginning of the document.

@return The start position.
**/
    public synchronized Position getStartPosition ()
    {
        return document_.getStartPosition ();
    }



/**
Returns the system on which the data queue resides.

@return  The system on which the data queue resides.
**/
    public AS400 getSystem()
    {
        return dataQueue_.getSystem ();
    }



/**
Returns the text contained within the specified portion of
the document.

@param offset   The offset into the document representing
                the desired start of the text.
@param length   The length of the text.
@return         The text.

@exception      BadLocationException    If the given offset and length does
                                        not represent a valid range in the
                                        document.
**/
    public synchronized String getText (int offset, int length)
        throws BadLocationException
    {
        return document_.getText (offset, length);
    }



/**
Stores the text contained within the specified portion of
the document in a segment.

@param offset   The offset into the document representing
                the desired start of the text.
@param length   The length of the text.
@param text     The segment in which to store the text.

@exception      BadLocationException    If the given offset and length does
                                        not represent a valid range in the
                                        document.
**/
    public synchronized void getText (int offset, int length, Segment text)
        throws BadLocationException
    {
        document_.getText (offset, length, text);
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        document_                   = new PlainDocument ();

        // Initialize the event support.
        dataQueueEventSupport_      = new DataQueueEventSupport (this);
        documentEventSupport_       = new DocumentEventSupport (this);
        errorEventSupport_          = new ErrorEventSupport (this);
        propertyChangeSupport_      = new PropertyChangeSupport (this);
        undoableEditEventSupport_   = new UndoableEditEventSupport (this);
        vetoableChangeSupport_      = new VetoableChangeSupport (this);
        workingEventSupport_        = new WorkingEventSupport (this);

        document_.addDocumentListener (documentEventSupport_);
        document_.addUndoableEditListener (undoableEditEventSupport_);
        dataQueue_.addDataQueueListener (dataQueueEventSupport_);
   }



/**
Inserts text into the document.  A position marks a location
in the document between items.  If the attributes that have been
defined exactly match the current attributes defined at the position,
the element representing the content at that position will simply be
expanded. If the attributes defined are different, a new content
element will be created that matches the attributes.

@param offset       The offset into the document representing
                    the insertion position.
@param text         The text.
@param attributes   The attributes to associate with the inserted content,
                    or null if there are no attributes.

@exception      BadLocationException    If the given offset does not
                                        represent a valid position in the
                                        document.
**/
    public synchronized void insertString (int offset, String text, AttributeSet attributes)
        throws BadLocationException
    {
        document_.insertString (offset, text, attributes);
    }



/**
Reads an entry from the data queue without removing it from the queue,
and stores the text of the entry in the document.  The entry replaces the current content. This method will not wait for entries if none are on the queue. System and path must be set prior
to calling this method.
**/
    public void peek ()
    {
        workingEventSupport_.fireStartWorking ();

        try {
            // Clear the contents of the document.
            document_.remove (0, document_.getLength ());

            // Peek the data queue.
            DataQueueEntry entry = dataQueue_.peek ();
            String contents;
            if (entry != null)
                contents = entry.getString ();
            else
                contents = "";

            // Store the contents of the document.
            document_.insertString (0, contents, null);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        workingEventSupport_.fireStopWorking ();
    }



/**
Reads an entry from the data queue without removing it from the queue,
and stores the text of the entry in the document.  The entry replaces the current content. System and path must be set prior to calling this method.

@param wait The number of seconds to wait if the queue contains no
            entries. -1 means to wait until an entry is available.
**/
    public void peek (int wait)
    {
        workingEventSupport_.fireStartWorking ();

        try {
            // Clear the contents of the document.
            document_.remove (0, document_.getLength ());

            // Peek the data queue.
            DataQueueEntry entry = dataQueue_.peek (wait);
            String contents;
            if (entry != null)
                contents = entry.getString ();
            else
                contents = "";

            // Store the contents of the document.
            document_.insertString (0, contents, null);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        workingEventSupport_.fireStopWorking ();
    }



/**
Sets a property value associated with the document.

@param  key     The property key.
@param  value   The property value.
**/
    public synchronized void putProperty (Object key, Object value)
    {
        document_.putProperty (key, value);
    }



/**
Reads an entry from the data queue and removes it from the queue,
and stores the text of the entry in the document. The entry replaces the current content. This method will not wait for entries if none are on the queue. System and path must be set prior
to calling this method.
**/
    public void read ()
    {
        workingEventSupport_.fireStartWorking ();

        try {
            // Clear the contents of the document.
            document_.remove (0, document_.getLength ());

            // Peek the data queue.
            DataQueueEntry entry = dataQueue_.read ();
            String contents;
            if (entry != null)
                contents = entry.getString ();
            else
                contents = "";

            // Store the contents of the document.
            document_.insertString (0, contents, null);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        workingEventSupport_.fireStopWorking ();
    }



/**
Reads an entry from the data queue and removes it from the queue,
and stores the text of the entry in the document. The entry replaces the current content.  System and path must be set prior to calling this method.

@param wait The number of seconds to wait if the queue contains no
            entries. -1 means to wait until an entry is available.
**/
    public void read (int wait)
    {
        workingEventSupport_.fireStartWorking ();

        try {
            // Clear the contents of the document.
            document_.remove (0, document_.getLength ());

            // Peek the data queue.
            DataQueueEntry entry = dataQueue_.read (wait);
            String contents;
            if (entry != null)
                contents = entry.getString ();
            else
                contents = "";

            // Store the contents of the document.
            document_.insertString (0, contents, null);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        workingEventSupport_.fireStopWorking ();
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }



/**
Removes text from the document.

@param offset   The offset into the document representing
                the desired start of the text.
@param length   The length of the text.

@exception      BadLocationException    If the given offset and length does
                                        not represent a valid range in the
                                        document.
**/
    public synchronized void remove (int offset, int length)
        throws BadLocationException
    {
        document_.remove (offset, length);
    }



/**
Removes a data queue listener.

@param  listener  The listener.
**/
    public void removeDataQueueListener (DataQueueListener listener)
    {
        dataQueueEventSupport_.removeDataQueueListener (listener);
    }



/**
Removes a document listener.

@param  listener  The listener.
**/
    public void removeDocumentListener (DocumentListener listener)
    {
        documentEventSupport_.removeDocumentListener (listener);
    }



/**
Removes an error listener.

@param  listener  The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Removes an undoable edit listener.

@param  listener  The listener.
**/
    public void removeUndoableEditListener (UndoableEditListener listener)
    {
        undoableEditEventSupport_.removeUndoableEditListener (listener);
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener (VetoableChangeListener listener)
    {
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Removes a working listener.

@param  listener  The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Renders the document.  This allows the model to be safely rendered in
the presence of currency, if the model supports being updated
asynchronously. The given runnable will be executed in a way that allows
it to safely read the model with no changes while the runnable is being
executed. The runnable itself may not make any mutations.

@param  runnable    The runnable.
**/
    public void render (Runnable runnable)
    {
        document_.render (runnable);
    }



/**
Sets the fully qualified integrated file system path name of the data queue.

@param path The fully qualified integrated file system path name of the data queue. The path  must be in the format of /QSYS.LIB/libname.LIB/dataQueue.DTAQ.  The library and queue name must each be 10 characters or less.


@exception PropertyVetoException If the change is vetoed.
**/
    public void setPath (String path)
        throws PropertyVetoException
    {
        String oldValue = dataQueue_.getPath ();
        String newValue = path;
        vetoableChangeSupport_.fireVetoableChange ("path", oldValue, newValue);

        dataQueue_.setPath (newValue);

        propertyChangeSupport_.firePropertyChange ("path", oldValue, newValue);
    }



/**
Sets the AS/400 system on which the data queue resides.

@param  system  The AS/400 system on which the data queue resides.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        AS400 oldValue = dataQueue_.getSystem ();
        AS400 newValue = system;
        vetoableChangeSupport_.fireVetoableChange ("system", oldValue, newValue);

        dataQueue_.setSystem (system);

        propertyChangeSupport_.firePropertyChange ("system", oldValue, newValue);
    }



/**
Writes the contents of the document to the data queue.The entry replaces the current content. System and path must be set prior
to calling this method.
**/
    public void write ()
    {
        workingEventSupport_.fireStartWorking ();

        // Write the contents.
        try {
            dataQueue_.write (document_.getText (0, document_.getLength ()));
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        workingEventSupport_.fireStopWorking ();
    }



}
