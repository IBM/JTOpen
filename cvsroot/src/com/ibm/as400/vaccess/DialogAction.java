///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DialogAction.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
The DialogAction class is an action that displays a
dialog box for an object in a modeless dialog.
**/
abstract class DialogAction
extends AbstractVAction
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // MRI.
    private static final String cancelText_             = ResourceLoader.getText ("DLG_CANCEL");
    private static final String okText_                 = ResourceLoader.getText ("DLG_OK");
    private boolean dialogLocked_ = false;

    // private data
    JDialog dialog                              = null; // Private.

/**
Constructs a DialogAction object.
**/
    public DialogAction (VObject object)
    {
        super (object);
    }


/**
Returns the component for the dialog box.

@return The component.
**/
    abstract public JComponent getInputComponent();


/**
Performs the action of displaying the dialog box.

@param  context The action context.
**/
    public void perform (VActionContext context)
    {
        String title = getText ();

        // Create the buttons.
        JButton cancelButton  = new JButton (cancelText_);
        JButton okButton      = new JButton (okText_);
        okButton.setSelected(true);

        // Listen to the buttons.
        ActionListener_ actionListener = new ActionListener_ (cancelButton,
                                                              okButton);
        okButton.addActionListener (actionListener);
        cancelButton.addActionListener (actionListener);

        // Set up the button pane.
        JPanel buttonPanel = new JPanel ();
        buttonPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
        buttonPanel.add (okButton);
        buttonPanel.add (cancelButton);

        // Get the graphical component.
        JComponent component = null;
        try {
            component = getInputComponent ();
            }
        catch (Exception e)
            {
            fireError (e);
            return;
            }

        // Layout the dialog.
        dialog = new JDialog (context.getFrame (), title, false);
        dialog.getContentPane ().setLayout (new BorderLayout ());
        dialog.getContentPane ().add ("South", buttonPanel);
        dialog.getContentPane ().add ("Center", component);
        dialog.setResizable (false);
        dialog.pack ();
        dialog.addWindowListener (new WindowListener_ ());

        // Show the dialog.
        dialog.show ();

        // @A1
        // Set default focus for QueuedMessageReplyAction          
        if (this instanceof QueuedMessageReplyAction) {        
           for (int i=0; i<component.getComponentCount(); i++) {
              if (component.getComponent(i) instanceof JTextField)
              {
                 component.getComponent(i).requestFocus();
                 return;
              }             
           }
        }
    }


/**
Performs the real action.
**/
    abstract public void perform2 ();

/**
If the real action did'nt perform successfully, make dialog keep being visible.
**/
    public void lockDialog()
    {
        dialogLocked_ = true;
    }


/**
The ActionListener_ class processes button clicks.
**/
    private class ActionListener_ implements ActionListener
    {
        private JButton                 cancelButton_;
        private JButton                 okButton_;

        public ActionListener_ (JButton cancelButton,
                                JButton okButton)
        {
            cancelButton_       = cancelButton;
            okButton_           = okButton;
        }

        public void actionPerformed (ActionEvent event)
        {
            Object source = event.getSource ();
            if (source == cancelButton_)
            {
                dialog.dispose();
                return;
            }
            if (source == okButton_)
            {
                dialogLocked_ = false;
                try
                {
                    perform2();
                    fireObjectChanged ();
                    if (dialogLocked_ == false)
                        dialog.dispose();
                } catch (Exception e)
                {
                    fireError (e);
                }

            }
        }
    }


/**
The WindowListener_ class processes window closes.
**/
    private class WindowListener_ extends WindowAdapter
    {

        public WindowListener_ ()
        {
        }

        public void windowClosing (WindowEvent event)
        {
            dialog.dispose ();
        }
    }

}  // end DialogAction class
