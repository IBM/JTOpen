///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ErrorDialogAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.Serializable;



/**
The ErrorDialogAdapter class represents an object
that listens for error events and displays a message dialog
for each error event.

<p>Most errors in the com.ibm.as400.vaccess package are reported using ErrorEvents
rather than throwing exceptions.  An ErrorDialogAdapter object is
useful to give users feedback whenever an error occurs.

<p>The component property is used to determine the frame
that is to be the parent of message dialogs.  If no
component is set, then a default frame will be used.

<p>The following example creates an explorer pane filled
with the contents of a directory in the integrated file
system of an AS/400.  It will use an ErrorDialogAdapter
object to display all errors in a message dialog.

<pre>
// Set up the explorer pane.
AS400ExplorerPane explorerPane = new AS400ExplorerPane ();

// Set up the explorer pane to display the contents
// of a directory.
AS400 system = new AS400 ("MySystem", "Userid", "Password");
VIFSDirectory directory = new VIFSDirectory (system, "/myDirectory");
explorerPane.setRoot (directory);

// Add the explorer pane to a frame.
frame.add (explorerPane);

// Set up the error dialog adapter.
explorerPane.addErrorListener (new ErrorDialogAdapter (frame));
</pre>
**/
public class ErrorDialogAdapter
implements ErrorListener, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String title_ = ResourceLoader.getText ("DLG_ERROR_TITLE");



    // Properties.
    private Component   component_    = null;
    private boolean modal_ = true; //@C1A


/**
Constructs an ErrorDialogAdapter object.
**/
    public ErrorDialogAdapter ()
    {
    }



//@C1C - javadoc
/**
Constructs an ErrorDialogAdapter object. The dialog is modal by default.

@param  component     Determines the parent frame for
                      dialogs.
**/
    public ErrorDialogAdapter (Component component)
    {
        component_ = component;
    }


//@C1A
/**
Constructs an ErrorDialogAdapter object.

@param  component     Determines the parent frame for
                      dialogs.
@param  modal         Specifies whether this dialog should be modal.
**/
    public ErrorDialogAdapter (Component component, boolean modal)
    {
        component_ = component;
        modal_ = modal;
    }


/**
Invoked when an error has occurred.  This will display
a message dialog with the error message.

@param  event   The event.
**/
    public void errorOccurred (ErrorEvent event)
    {
        if ((Trace.isTraceOn ()) && (Trace.isTraceErrorOn ()))                  // @B1A
            event.getException ().printStackTrace (Trace.getPrintWriter ());    // @B1A

        String message = VUtilities.getExceptionText (event.getException ());
        message = VUtilities.formatHelp2 (message, 50);         // @A1A

//@C1D        // Note:  This is a modal dialog.
//@C1D        JOptionPane.showMessageDialog (component_, message, title_,
//@C1D            JOptionPane.ERROR_MESSAGE);
// @C1: Changed ErrorDialogAdapter to be either modal or non-modal.
// The reason for having a modal option is to allow the caller to setup
// an ErrorDialogAdapter that isn't modal by default. In some cases,
// a modal EDA will hang the Swing thread if the EDA is displayed during
// the creation of another visual component. This is a Swing bug for which
// there is no known fix. A non-modal EDA will not block the current thread
// when it is shown, which allows Swing to paint the parent component.
//@C2D Performing a dialog.toFront() seems to be as close to modal behavior
//@C2D that a non-modal dialog can get.
        JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE); //@C1A
        JDialog dialog = pane.createDialog(component_, title_);                 //@C1A
        dialog.setModal(modal_);                                                //@C1A
        dialog.setResizable(false);                                             //@C1A
        dialog.pack();                                                          //@C1A
        // @C2D dialog.toFront();                                                       //@C1A
        dialog.show();                                                          //@C1A
    }



/**
Returns the component that determines the parent frame
for dialogs.

@return The component, or null if none has been set.
**/
    public Component getComponent ()
    {
        return component_;
    }



/**
Copyright.
**/
    private static String getCopyright ()
    {
        return Copyright_v.copyright;
    }



/**
Sets the component that determines the parent frame
for dialogs.

@param  component   The component, or null to use a
                    default frame.
**/
    public void setComponent (Component component)
    {
        component_ = component;
    }



}

