///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSEditAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.FileEvent;
import com.ibm.as400.access.FileListener;
import com.ibm.as400.access.Trace;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;



/**
The IFSEditAction class represents the action of editing or
viewing an IFS file.
**/
class IFSEditAction
implements VAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String         copyText_           = ResourceLoader.getText ("MENU_COPY");
    private static final String         cutText_            = ResourceLoader.getText ("MENU_CUT");
    private static final String         editActionText_     = ResourceLoader.getText ("ACTION_EDIT");
    private static final String         editText_           = ResourceLoader.getText ("MENU_EDIT");
    private static final String         exitText_           = ResourceLoader.getText ("MENU_EXIT");
    private static final String         fileText_           = ResourceLoader.getText ("MENU_FILE");
    private static final String         pasteText_          = ResourceLoader.getText ("MENU_PASTE");
    private static final String         saveConfirmText_    = ResourceLoader.getText ("DLG_CONFIRM_SAVE");
    private static final String         saveConfirmTitle_   = ResourceLoader.getText ("DLG_CONFIRM_SAVE_TITLE");
    private static final String         saveText_           = ResourceLoader.getText ("MENU_SAVE");
    private static final String         selectAllText_      = ResourceLoader.getText ("MENU_SELECT_ALL");
    private static final String         viewActionText_     = ResourceLoader.getText ("ACTION_VIEW");



    // Private data.
    private boolean                     allowChanges_   = true;
    private boolean                     enabled_        = true;
    private IFSFile                     file_           = null;
    private VObject                     object_         = null;



    // Event support.
    private ErrorEventSupport           errorEventSupport_    = new ErrorEventSupport (this);
    private VObjectEventSupport         objectEventSupport_   = new VObjectEventSupport (this);
    private WorkingEventSupport         workingEventSupport_  = new WorkingEventSupport (this);



/**
Constructs an IFSEditAction object.

@param  object          The object.
@param  file            The file.
@param  allowChanges    true if changes are allowed, false otherwise.
**/
    public IFSEditAction (VObject object,
                          IFSFile file,
                          boolean allowChanges)
    {
        object_         = object;
        file_           = file;
        allowChanges_   = allowChanges;
    }



/**
Adds an error listener.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a VObjectListener.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a working listener.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Cleans up before an edit session is done.  If the document has
been modified, this will the prompt the user for a save.

@param document     The document.
@param frame        The frame.
@param allowCancel  true to allow cancel, false otherwise.
**/
    void exit (IFSTextFileDocument document,
               JFrame frame,
               boolean allowCancel) // Private.
    {
        boolean cancelled = false;
        if (document.isModified ()) {
            int optionType = allowCancel ? JOptionPane.YES_NO_CANCEL_OPTION
                : JOptionPane.YES_NO_OPTION;
            int response = JOptionPane.showConfirmDialog (frame,
                saveConfirmText_, saveConfirmTitle_, optionType);
            switch (response) {
                case JOptionPane.YES_OPTION:
                    save (document);
                    break;
                case JOptionPane.NO_OPTION:
                default:
                    break;
                case JOptionPane.CANCEL_OPTION:
                    cancelled = true;
                    break;
            }
        }

        if (cancelled == false) {
            // Make the frame go away.
            frame.setVisible (false);
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
Returns the localized text for the action.

@return The text.
**/
    public String getText ()
    {
        return allowChanges_ ? editActionText_ : viewActionText_;
    }



/**
Indicates if the action is enabled.

@return true if the action is enabled, false otherwise.
**/
    public boolean isEnabled ()
    {
        return enabled_;
    }



/**
Performs the action.

@param  context The action context.
**/
    public void perform (VActionContext context)
    {
        if (Trace.isTraceOn()) {
            if (allowChanges_)
                Trace.log (Trace.INFORMATION, "Editing file ["
                    + file_.getName () + "].");
            else
                Trace.log (Trace.INFORMATION, "Viewing file ["
                    + file_.getName () + "].");
        }

        // Create the pieces.
        IFSTextFileDocument document = new IFSTextFileDocument (file_);
        document.load ();

        JTextPane textPane = new JTextPane (document);
        textPane.setEditable (allowChanges_);
        // @A1D textPane.setPreferredSize (new java.awt.Dimension (400, 400));

        document.addWorkingListener (new WorkingCursorAdapter (textPane));

        JScrollPane scrollPane = new JScrollPane (textPane);
        scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Create the window, part 1.
        JFrame frame = new JFrame (file_.getPath ());
        frame.getRootPane ().setPreferredSize (new java.awt.Dimension (400, 400)); // @A1A

        // Create the menu bar.
        JMenuBar        menuBar = new JMenuBar();
        JMenu           menu;
        JMenuItem       menuItem;

        final IFSTextFileDocument   document2   = document;
        final JFrame                frame2      = frame;
        final JTextPane             textPane2   = textPane;

        // Create the file menu.
        menu = new JMenu (fileText_);

        menuItem = new JMenuItem (saveText_);
        menuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent event) { save (document2); }
            });
        menuItem.setEnabled (allowChanges_);
        menu.add (menuItem);

        menu.addSeparator ();

        menuItem = new JMenuItem (exitText_);
        menuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent event) { exit (document2, frame2, true); }
            });
        menu.add (menuItem);

        menuBar.add (menu);

        // Create the edit menu.
        menu = new JMenu (editText_);

        menuItem = new JMenuItem (cutText_);
        menuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent event) { textPane2.cut (); }
            });
        menuItem.setEnabled (allowChanges_);
        menu.add (menuItem);

        menuItem = new JMenuItem (copyText_);
        menuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent event) { textPane2.copy (); }
            });
        menuItem.setEnabled (true);
        menu.add (menuItem);

        menuItem = new JMenuItem (pasteText_);
        menuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent event) { textPane2.paste (); }
            });
        menuItem.setEnabled (allowChanges_);
        menu.add (menuItem);

        menu.addSeparator ();

        menuItem = new JMenuItem (selectAllText_);
        menuItem.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent event) { textPane2.selectAll (); }
            });
        menuItem.setEnabled (true);
        menu.add (menuItem);

        menuBar.add (menu);

        // Create the window, part 2.
        frame.addWindowListener (new WindowAdapter () {
            public void windowClosing (WindowEvent event) { exit (document2, frame2, false); }
            });
        frame.getRootPane ().setJMenuBar (menuBar); // @C0C
        frame.getContentPane ().setLayout (new BorderLayout ());
        frame.getContentPane ().add ("Center", scrollPane);
        frame.pack ();
        frame.show ();
    }



/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.removeVObjectListener (listener);
    }



/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Saves the file.

@param document     The document.
**/
    void save (IFSTextFileDocument document) // Private.
    {
        document.save ();
        object_.load ();
        objectEventSupport_.fireObjectChanged (object_);
    }



/**
Sets the enabled state of the action.

@param enabled true if the action is enabled, false otherwise.
**/
    public void setEnabled (boolean enabled)
    {
        enabled_ = enabled;
    }



/**
Returns the text for the action.

@return The text.
**/
    public String toString ()
    {
        return getText ();
    }


}

