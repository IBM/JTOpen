///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSTextFileDocument.java
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
import com.ibm.as400.access.ConvTableReader;
import com.ibm.as400.access.ConvTableWriter;
import com.ibm.as400.access.FileListener;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSTextFileInputStream;
import com.ibm.as400.access.IFSTextFileOutputStream;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
///import java.io.BufferedReader;
///import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;



/**
The IFSTextFileDocument class implements an underlying model
for text components, where the text is the contents of a text
file located in the integrated file system of an AS/400.
You must explicitly call load() to load the information from
the AS/400.

<p>Use this class in conjuction with any JTextComponent or
any other component that works with the Document interface.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>IFSTextFileDocument objects generate the following events:
<ul>
    <li>DocumentEvent
    <li>ErrorEvent
    <li>FileEvent
    <li>PropertyChangeEvent
    <li>UndoableEditEvent
    <li>WorkingEvent
</ul>

<p>The following example creates a document which contains
the contents of a text file in the integrated file system
of an AS/400.  It then presents the document in a JTextArea
object.

<pre>
// Set up the document and the JTextArea.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
IFSTextFileDocument document = new IFSTextFileDocument (system, "/myFile");
JTextArea textArea = new JTextArea (document);
<br>
// Add the JTextArea to a frame.
JFrame frame = new JFrame ("My Window");
frame.getContentPane().add(new JScrollPane(textArea));

<br>
// Load the information from the AS/400.
document.load ();
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
// * We need to be a StyledDocument, not just a Document, in order
//   for us to be usable in a JTextPane, which handles word-wrapping.
//
public class IFSTextFileDocument
implements StyledDocument, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Properties.
    private IFSFile                         file_                       = null;



    // Private data.
    transient private StyledDocument        document_;
    transient         boolean               modified_; // Private.



    // Event support.
    transient private DocumentEventSupport  documentEventSupport_ ;
    transient private DocumentListener      documentListener_;
    transient private ErrorEventSupport     errorEventSupport_;
    transient private FileEventSupport      fileEventSupport_;
    transient private PropertyChangeSupport propertyChangeSupport_;
    transient private UndoableEditEventSupport  undoableEditEventSupport_;
    transient private VetoableChangeSupport vetoableChangeSupport_;
    transient private WorkingEventSupport   workingEventSupport_;



/**
Constructs a IFSTextFileDocument object.
**/
    public IFSTextFileDocument ()
    {
        file_ = new IFSFile ();
        initializeTransient ();
    }



/**
Constructs a IFSTextFileDocument object.

@param      file                     The file.
**/
    public IFSTextFileDocument (IFSFile file)
    {
        file_ = file;
        initializeTransient ();
    }



/**
Constructs a IFSTextFileDocument object.

@param      system                   The AS/400 system on which the file resides.
@param      path                     The fully qualified path name of the file that this object represents.
**/
    public IFSTextFileDocument (AS400 system, String path)
    {
        file_ = new IFSFile (system, path);
        initializeTransient ();
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
Adds a listener to be notified when a file event occurs.

@param  listener  The listener.
**/
    public void addFileListener (FileListener listener)
    {
        fileEventSupport_.addFileListener (listener);
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
Adds a style into the logical style hierarchy.

@param name     The name of the style.
@param parent   The parent style.
@return         The style.
**/
    public synchronized Style addStyle (String name, Style parent)
    {
        return document_.addStyle (name, parent);
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
Returns a position that will track change as the document is altered.
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
Returns the background color based on a set of attributes.

@param  attributes  The attributes.
@return             The background color.
**/
    public synchronized Color getBackground (AttributeSet attributes)
    {
        // In Swing 1.0, the DefaultStyledDocument.getBackground()
        // throws an Error - not implemented.  This will hang
        // testcases, so we should not pass this on.  This is already
        // reported in Java's Bug Parade, Bug Id 4109225.
        try {
            return document_.getBackground (attributes);
        }
        catch (Error e) {
            return null;
        }
    }



/**
Returns the element that represents the character that is at
a given offset within the document.

@param offset   The offset to the character.
@return         The element.
**/
    public synchronized Element getCharacterElement (int offset)
    {
        return document_.getCharacterElement (offset);
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

@return The end position of the document.
**/
    public synchronized Position getEndPosition ()
    {
        return document_.getEndPosition ();
    }



/**
Returns the font based on a set of attributes.

@param  attributes  The attributes.
@return             The font.
**/
    public synchronized Font getFont (AttributeSet attributes)
    {
        return document_.getFont (attributes);
    }



/**
Returns the foreground color based on a set of attributes.

@param  attributes  The attributes.
@return             The foreground color.
**/
    public synchronized Color getForeground (AttributeSet attributes)
    {
        return document_.getForeground (attributes);
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
Returns the logical style for a given offset within the document.

@param offset   The offset within the document.
@return         The logical style at the specified offset.
**/
    public synchronized Style getLogicalStyle (int offset)
    {
        return document_.getLogicalStyle (offset);
    }



/**
Returns the element that represents the paragraph that encloses
a given offset within the document.

@param offset   The offset within the document.
@return         The element that represents the paragraph.
**/
    public synchronized Element getParagraphElement (int offset)
    {
        return document_.getParagraphElement (offset);
    }



/**
The path name of the file.

@return  The path name of the file, or "" if the path has not been set.
**/
    public String getPath ()
    {
        return file_.getPath ();
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

@return The start position of the document.
**/
    public synchronized Position getStartPosition ()
    {
        return document_.getStartPosition ();
    }



/**
Returns a named style.

@param name     The name of the style.
@return         The style.
**/
    public synchronized Style getStyle (String name)
    {
        return document_.getStyle (name);
    }



/**
Returns the AS/400 system on which the file resides.

@return  The system, or null if the system has not been set.
**/
    public AS400 getSystem()
    {
        return file_.getSystem ();
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
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        document_               = new DefaultStyledDocument ();
        modified_               = false;

        // Initialize the event support.
        documentEventSupport_   = new DocumentEventSupport (this);
        documentListener_       = new DocumentListener_ ();
        errorEventSupport_      = new ErrorEventSupport (this);
        fileEventSupport_       = new FileEventSupport (this);
        propertyChangeSupport_  = new PropertyChangeSupport (this);
        undoableEditEventSupport_   = new UndoableEditEventSupport (this);
        vetoableChangeSupport_  = new VetoableChangeSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);

        document_.addDocumentListener (documentEventSupport_);
        document_.addDocumentListener (documentListener_);
        document_.addUndoableEditListener (undoableEditEventSupport_);
        file_.addFileListener (fileEventSupport_);
   }



/**
Indicates if the document has been modified since it was
last read or written.

@return true if the document has been modified; false
        otherwise.
**/
    public boolean isModified ()
    {
        return modified_;
    }



/**
Loads the contents of the document from the file on the
AS/400.
**/
    public void load ()
    {
        workingEventSupport_.fireStartWorking ();

        try {
            // Clear the contents of the document.
            document_.remove (0, document_.getLength ());

            // Open the input stream.
            IFSTextFileInputStream input = new IFSTextFileInputStream (file_.getSystem(),
                file_, IFSTextFileInputStream.SHARE_ALL);
            input.addFileListener (fileEventSupport_);
            ///BufferedReader reader = new BufferedReader (new InputStreamReader (input));   // @C2d
            ConvTableReader reader = new ConvTableReader (input, file_.getCCSID());  // @C2a

            // Load the contents.
            char[] charArray = new char[512];
            int count = 0;
            int position = 0;

            while (true) {
                count = reader.read (charArray, 0, 512);
                if (count > 0) {
                    document_.insertString (position, new String (charArray, 0, count), null);
                    position += count;
                }
                else
                    break;
            }

            // Close the input stream.
            reader.close ();
            input.close ();
            input.removeFileListener (fileEventSupport_);
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }

        modified_ = false;
        addDocumentListener (documentListener_);

        // Done loading.
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
Removes a file listener.

@param  listener  The listener.
**/
    public void removeFileListener (FileListener listener)
    {
        fileEventSupport_.removeFileListener (listener);
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
Removes a style from the logical style hierarchy.

@param name     The name of the style.
**/
    public synchronized void removeStyle (String name)
    {
        document_.removeStyle (name);
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
Saves the contents of the document to the file on the AS/400.
This will creates the file if it does not already exist.
**/
    public void save ()
    {
        workingEventSupport_.fireStartWorking ();

        IFSTextFileOutputStream output = null;
        ///BufferedWriter      writer = null;  // @C2d
        ConvTableWriter     writer = null;  // @C2a

        try {
            // Open the output stream.
            output = new IFSTextFileOutputStream (file_.getSystem(),
                file_, IFSTextFileOutputStream.SHARE_ALL, false);
            output.addFileListener (fileEventSupport_);
            ///writer = new BufferedWriter (new OutputStreamWriter (output));  // @C2d
            writer = new ConvTableWriter (output, file_.getCCSID());  // @C2a

            // Save the contents.
            writer.write (document_.getText (0, document_.getLength ()));
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }
        finally {
            // Close the input stream.
            if (writer != null) {
                try {
                    writer.close ();
                }
                catch (Exception e) {
                    errorEventSupport_.fireError (e);
                }
            }
            if (output != null) {
                try {
                    output.close ();
                }
                catch (Exception e) {
                    errorEventSupport_.fireError (e);
                }
                finally {
                    output.removeFileListener (fileEventSupport_);
                }
            }
        }

        modified_ = false;
        addDocumentListener (documentListener_);
        workingEventSupport_.fireStopWorking ();
    }



/**
Sets the element attributes used for the given range of
existing content in the document.

@param  offset      The offset to the start of the change.
@param  length      The length of the change.
@param  attributes  The attributes.
@param  replace     Indicates whether or not the previous attributes should be cleared before the new attributes are set. If true, true to replace the previous attributes with these
                    attributes; false to merge them.
**/
    public synchronized void setCharacterAttributes (int offset,
                                                     int length,
                                                     AttributeSet attributes,
                                                     boolean replace)
    {
        document_.setCharacterAttributes (offset, length, attributes, replace);
    }



/**
Sets the logical style for a given offset within the document.

@param offset   The offset within the document.
@param style    The logical style.
**/
    public synchronized void setLogicalStyle (int offset, Style style)
    {
        document_.setLogicalStyle (offset, style);
    }



/**
Sets the element attributes used for the paragraphs enclosing the
given range of existing content in the document.

@param  offset      The offset to the start of the change.
@param  length      The length of the change.
@param  attributes  The attributes.
@param  replace     Indicates whether or not the previous attributes should be cleared before the new attributes are set. If true, this will replace the previous attributes entirely.  If false, the new attributes will be merged with the previous attributes.
**/
    public synchronized void setParagraphAttributes (int offset,
                                                     int length,
                                                     AttributeSet attributes,
                                                     boolean replace)
    {
        document_.setParagraphAttributes (offset, length, attributes, replace);
    }



/**
Sets the path name of the file.

@param path The path name of the file.



@exception PropertyVetoException If the change is vetoed.
**/
    public void setPath (String path)
        throws PropertyVetoException
    {
        String oldValue = file_.getPath ();
        String newValue = path;
        vetoableChangeSupport_.fireVetoableChange ("path", oldValue, newValue);

        file_.removeFileListener (fileEventSupport_);

        AS400 system = file_.getSystem ();
        file_ = new IFSFile ();
        file_.setPath (path);
        if (system != null)
            file_.setSystem (system);

        file_.addFileListener (fileEventSupport_);

        propertyChangeSupport_.firePropertyChange ("path", oldValue, newValue);
    }



/**
Sets the AS/400 system on which the file resides.

@param  system  The AS/400 system on which the file resides.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        AS400 oldValue = file_.getSystem ();
        AS400 newValue = system;
        vetoableChangeSupport_.fireVetoableChange ("system", oldValue, newValue);

        file_.removeFileListener (fileEventSupport_);

        String path = file_.getPath ();
        file_ = new IFSFile ();
        file_.setSystem (system);
        if (path != null)
            file_.setPath (path);

        file_.addFileListener (fileEventSupport_);

        propertyChangeSupport_.firePropertyChange ("system", oldValue, newValue);
    }



/**
Returns the string representation.  This is the name of the file.

@return The string representation of the file name.
**/
    public String toString ()
    {
        return file_.getName ();
    }



/**
The DocumentListener_ class processes document events.  Once it
is modified the first time, it will remove itself as a listener.
This saves on subsequent event firings, but must be added each
time the modified flag is set back to true.
**/
    private class DocumentListener_ implements DocumentListener
    {
        public void changedUpdate (DocumentEvent event)
        {
            modified_ = true;
            removeDocumentListener (this);
        }

        public void insertUpdate (DocumentEvent event)
        {
            modified_ = true;
            removeDocumentListener (this);
        }

        public void removeUpdate (DocumentEvent event)
        {
            modified_ = true;
            removeDocumentListener (this);
        }
    }



}
