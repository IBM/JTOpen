///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VPropertiesAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;



/**
The VPropertiesAction class is an action that presents a
properties pane for a server resource in a modeless dialog.

<p>If a property dialog is already visible for the object,
then it will be given focus.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VPropertiesAction objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>

@see VPropertiesPane
@deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class VPropertiesAction
implements VAction, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String applyText_              = ResourceLoader.getText ("DLG_APPLY");
    private static final String cancelText_             = ResourceLoader.getText ("DLG_CANCEL");
    private static final String okText_                 = ResourceLoader.getText ("DLG_OK");
    private static final String propertiesActionText_   = ResourceLoader.getText ("ACTION_PROPERTIES");
    private static final String propertiesDialogText_   = ResourceLoader.getText ("DLG_PROPERTIES_TITLE");



    // Properties.
    private boolean             enabled_                = true;
    private VObject             object_                 = null;



    // Private data.
    private static Hashtable    visibleDialogs_         = new Hashtable ();



    // Event support.
    transient ErrorEventSupport     errorEventSupport_;         // Private.
    transient VObjectEventSupport   objectEventSupport_;        // Private.
    transient WorkingEventSupport   workingEventSupport_;       // Private.



/**
Constructs a VPropertiesAction object.
**/
    public VPropertiesAction ()
    {
        initializeTransient ();
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.


@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
        workingEventSupport_.addWorkingListener (listener);
    }



/**
Applies the changes.

@param propertiesPane   The properties pane.
**/
    void applyChanges (VPropertiesPane propertiesPane) // Private.
    {
        try {
            propertiesPane.applyChanges ();
        }
        catch (Exception e) {
            errorEventSupport_.fireError (e);
        }
    }



/**
Disposes a property dialog.

@param  object The object.
**/
    static void disposeDialog (VObject object) // Private.
    {
        JDialog dialog = (JDialog) visibleDialogs_.get (object);
        dialog.dispose ();
        visibleDialogs_.remove (object);
    }




/**
Returns the object with which the action is associated.

@return The object.
**/
    public VObject getObject ()
    {
        return object_;
    }



/**
Returns the text for the properties action.

@return The text for the properties action.
**/
    public String getText ()
    {
        return propertiesActionText_;
    }



/**
Indicates if the action is enabled.

@return true if the action is enabled; false otherwise.
**/
    public boolean isEnabled ()
    {
        return enabled_;
    }



/**
Initializes transient data.
**/
    private void initializeTransient ()
    {
        // Initialize event support.
        errorEventSupport_      = new ErrorEventSupport (this);
        objectEventSupport_     = new VObjectEventSupport (this);
        workingEventSupport_    = new WorkingEventSupport (this);
    }



/**
Performs the action.

@param  context   The action context.
**/
    public void perform (VActionContext context)
    {
        if (context == null)
            throw new NullPointerException ("context");

        if ((enabled_ == true) && (object_ != null)) {

            VPropertiesPane propertiesPane = object_.getPropertiesPane ();
            final VPropertiesPane propertiesPane2 = propertiesPane; // For inner classes.

            final VObject object2 = object_; // For inner classes.

            if (propertiesPane != null) {

                // If the dialog is already showing, then just
                // give it the focus.
                if (visibleDialogs_.containsKey (object_)) {
                    JDialog dialog = (JDialog) visibleDialogs_.get (object_);
                    dialog.toFront ();
                    dialog.requestFocus ();
                }

                // Otherwise, create and show a new dialog.
                else {
                    String title = ResourceLoader.substitute (propertiesDialogText_, object_.getText ()); // @A1C

                    // Bubble events up from properties pane.
                    propertiesPane.addErrorListener (errorEventSupport_);
                    propertiesPane.addVObjectListener (objectEventSupport_);
                    propertiesPane.addWorkingListener (workingEventSupport_);

                    // Apply button.
                    JButton applyButton = new JButton (applyText_);
                    final JButton applyButton2 = applyButton; // For inner classes.
                    applyButton.addActionListener (new ActionListener () {
                        public void actionPerformed (ActionEvent event) {
                            applyChanges (propertiesPane2);
                            applyButton2.setEnabled (false);
                        }});
                    applyButton.setEnabled (false);

                    // Cancel button.
                    JButton cancelButton = new JButton (cancelText_);
                    cancelButton.addActionListener (new ActionListener () {
                        public void actionPerformed (ActionEvent event) {
                            propertiesPane2.removeErrorListener (errorEventSupport_);
                            propertiesPane2.removeVObjectListener (objectEventSupport_);
                            propertiesPane2.removeWorkingListener (workingEventSupport_);
                            disposeDialog (object2);
                        }});

                    // Ok button.
                    JButton okButton = new JButton (okText_);
                    okButton.addActionListener (new ActionListener () {
                        public void actionPerformed (ActionEvent event) {
                            applyChanges (propertiesPane2);
                            propertiesPane2.removeErrorListener (errorEventSupport_);
                            propertiesPane2.removeVObjectListener (objectEventSupport_);
                            propertiesPane2.removeWorkingListener (workingEventSupport_);
                            disposeDialog (object2);
                        }});

                    // Set up the button pane.
                    JPanel buttonPanel = new JPanel ();
                    buttonPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
                    buttonPanel.add (okButton);
                    buttonPanel.add (cancelButton);
                    buttonPanel.add (applyButton);
                    okButton.setSelected (true);

                    // Get the graphical component.
                    Component component = propertiesPane.getComponent ();

                    // Layout the dialog.
                    JDialog dialog = new JDialog (context.getFrame (), title, false);
                    dialog.getContentPane ().setLayout (new BorderLayout ());
                    dialog.getContentPane ().add ("South", buttonPanel);
                    dialog.getContentPane ().add ("Center", component);
                    dialog.setResizable (false);
                    dialog.pack ();
                    dialog.addWindowListener (new WindowAdapter () {
                        public void windowClosing (WindowEvent event) {
                            disposeDialog (object2);
                        }});

                    // Listen to the properties pane, so that we can
                    // enable the apply button when needed.
                    propertiesPane.addChangeListener (new ChangeListener () {
                        public void stateChanged (ChangeEvent event) {
                            applyButton2.setEnabled (true);
                        }});

                    // Show the dialog.
                    showDialog (object2, dialog);
                }
            }
        }
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
Sets the enabled state of the action.

@param enabled true if the action is enabled; false otherwise.
**/
    public void setEnabled (boolean enabled)
    {
        enabled_ = enabled;
    }



/**
Sets the object with which the action is associated.

@param object   The object.
**/
    public void setObject (VObject object)
    {
        if (object == null)
            throw new NullPointerException ("object");

        object_ = object;
    }



/**
Shows a property dialog.

@param  object   The object.
@param  dialog   The dialog.
**/
    static void showDialog (VObject object, JDialog dialog) // Private.
    {
        visibleDialogs_.put (object, dialog);
        dialog.show ();
    }



/**
Returns the string representation of the properties action.

@return The string representation of the properties action.
**/
    public String toString ()
    {
        return propertiesActionText_;
    }



}
