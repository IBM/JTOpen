///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: MessageDialog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.util.StringTokenizer;

// This class represents a dialog window that is used to display a message and optionally allow a choice to be made by the user.
class MessageDialog extends Dialog
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private Button positiveButton_ = null;  // Push button for OK or Yes.
    private Button negativeButton_ = null;  // Push button for No.
    private AS400SignonDialogAdapter listener_ = null;

    // Construct a message dialog that displays a message and one or two pushbuttons.
    // @param  parent  The parent Window.
    // @param  messageText  The message to display.
    // @param  titleText  The window title text.
    // @param  allowChoice  true for yes & no buttons, false for OK button.
    MessageDialog(Frame parent, String messageText, String titleText, boolean allowChoice)
    {
        // Create a frame.
        super(parent, titleText, true);

        listener_ = new AS400SignonDialogAdapter(this);

        // Create a GridBagLayout manager having three rows and four columns.
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(8, 8, 0, 8);
        setLayout(layout);

        setResizable(false);

        // Set the background color to light gray.
        setBackground(Color.lightGray);

        // Note: Labels can't display multiple lines.  This is we use a Panel.
        Panel panel = new Panel();
        StringTokenizer tokenizer = new StringTokenizer(messageText, "\n");
        panel.setLayout(new GridLayout(tokenizer.countTokens(), 1));
        while(tokenizer.hasMoreTokens())
        {
            panel.add(new Label(tokenizer.nextToken()));
        }
        add(panel, layout, constraints, 0, 0, 1, 1);

        // Create panels to hold the buttons of the specified button layout.
        Panel centeringPanel = new Panel();
        centeringPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        constraints.insets = new Insets(8, 8, 8, 8);
        add(centeringPanel, layout, constraints, 0, 1, 1, 1);
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(1, 2, 8, 0));
        centeringPanel.add(buttonPanel);

        // Add the buttons to the panel.  Add event listeners to the buttons.
        if (allowChoice)
        {
            positiveButton_ = new Button(ResourceBundleLoader.getText("DLG_YES_BUTTON"));
            positiveButton_.addActionListener(listener_);
            positiveButton_.addFocusListener(listener_);
            positiveButton_.addKeyListener(listener_);
            buttonPanel.add(positiveButton_);
            negativeButton_ = new Button(ResourceBundleLoader.getText("DLG_NO_BUTTON"));
            negativeButton_.addActionListener(listener_);
            negativeButton_.addFocusListener(listener_);
            negativeButton_.addKeyListener(listener_);
            buttonPanel.add(negativeButton_);
        }
        else
        {
            positiveButton_ = new Button(ResourceBundleLoader.getCoreText("DLG_OK_BUTTON"));
            positiveButton_.addActionListener(listener_);
            positiveButton_.addFocusListener(listener_);
            positiveButton_.addKeyListener(listener_);
            buttonPanel.add(positiveButton_);
        }

        // Arrange the components in the dialog.
        pack();

        // Add a listener for window events.
        addWindowListener(listener_);
    }

    // Display the dialog and wait for user response.
    // @return  true if user pressed OK or Yes; false if user pressed No.
    boolean display()
    {
        // Set focus on the default button.
        listener_.setFocalPoint(positiveButton_);

        // Make the window visible.  The call to show() will block the current thread until we hide or dispose of this dialog.
        show();

        return listener_.getFocalPoint() != negativeButton_;
    }

    // This methods adds a user interface component to the specified GridBagLayout manager using the specified constraints.
    // @param  component  The user interface component to add.
    // @param  layout  The GridBagLayout manager.
    // @param  constraints  The constraints for the component.
    // @param  x  The x coordinate of the leftmost cell of the component.
    // @param  y  The y coordinate of the topmost cell of the component.
    // @param  width  The horizontal measurement of the component in cells.
    // @param  height  The vertical measurement of the component in cells.
    protected void add(Component component, GridBagLayout layout, GridBagConstraints constraints, int x, int y, int width, int height)
    {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        layout.setConstraints(component, constraints);
        add(component);
    }
}
