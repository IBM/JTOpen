///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ChangePasswordDialog.java
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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;

// This class implements a dialog which allows the user to change the specified password.
class ChangePasswordDialog extends Dialog
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private AS400SignonDialogAdapter listener_;
    private TextField systemNameTextField_;
    private TextField userIdTextField_;
    private TextField oldTextField_;  // Text field for current password.
    private TextField newTextField_;  // Text field for new password.
    private TextField confirmTextField_;  // Text field for password confirmation.
    private Button okButton_;
    private Button cancelButton_;
    private Label passwordLabel_;
    private Panel centeringPanel_;

    // Construct a change password dialog having the specified title.
    // @param  parent  The parent Window.
    // @param  titleText  The window title.
    ChangePasswordDialog(Frame parent, String titleText)
    {
        super(parent, titleText, true);

        listener_ = new AS400SignonDialogAdapter(this);

        // Create a GridBagLayout manager.
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(8, 8, 0, 8);
        setLayout(new GridBagLayout());
        setResizable(false);

        // Set the background color to light gray.
        setBackground(Color.lightGray);

        // Create the 'System:' Label and add to the panel.
        Label label = new Label(ResourceBundleLoader.getCoreText("DLG_SYSTEM_LABEL"), Label.LEFT);
        add(this, label, constraints, 0, 0, 1, 1);

        // Create the system text field and add to the panel.
        systemNameTextField_ = new TextField(10);
        systemNameTextField_.setEnabled(false);
        add(this, systemNameTextField_, constraints, 1, 0, 1, 1);

        // Create the 'User ID:' Label and add to the panel.
        label = new Label(ResourceBundleLoader.getCoreText("DLG_USER_ID_LABEL"), Label.LEFT);
        add(this, label, constraints, 0, 1, 1, 1);

        // Create the user id text field and add to the panel.
        userIdTextField_ = new TextField(10);
        userIdTextField_.setEnabled(false);
        add(this, userIdTextField_, constraints, 1, 1, 1, 1);
        passwordLabel_ = new Label(ResourceBundleLoader.getText("DLG_PASSWORDS_LABEL"), Label.CENTER);
        add(this, passwordLabel_, constraints, 0,2,1,1);

        // Create the old password label.
        label = new Label(ResourceBundleLoader.getText("DLG_OLD_LABEL"), Label.LEFT);
        add(this, label, constraints, 0, 3, 1, 1);

        // Create the old password text field.
        oldTextField_ = new AS400SignonTextField();
        oldTextField_.setEchoChar('*');
        oldTextField_.addFocusListener(listener_);
        oldTextField_.addKeyListener(listener_);
        add(this, oldTextField_, constraints, 1, 3, 1, 1);

        // Create the new password label.
        label = new Label(ResourceBundleLoader.getText("DLG_NEW_LABEL"), Label.LEFT);
        add(this, label, constraints, 0, 4, 1, 1);

        // Create the new password text field.
        newTextField_ = new AS400SignonTextField();
        newTextField_.setEchoChar('*');
        newTextField_.addFocusListener(listener_);
        newTextField_.addKeyListener(listener_);
        add(this, newTextField_, constraints, 1, 4, 1, 1);

        // Create the confirm password label.
        label = new Label(ResourceBundleLoader.getText("DLG_CONFIRM_LABEL"), Label.LEFT);
        add(this, label, constraints, 0, 5, 1, 1);

        // Create the confirm password text field.
        confirmTextField_ = new AS400SignonTextField();
        confirmTextField_.setEchoChar('*');
        confirmTextField_.addFocusListener(listener_);
        confirmTextField_.addKeyListener(listener_);
        add(this, confirmTextField_, constraints, 1, 5, 1, 1);

        // Create panels to hold the 'OK', and 'Cancel' buttons.
        centeringPanel_ = new Panel();
        centeringPanel_.setLayout(new FlowLayout(FlowLayout.CENTER));
        constraints.insets = new Insets(8, 8, 8, 8);
        add(this, centeringPanel_, constraints, 0, 6, 2, 1);
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(1, 2, 8, 0));
        centeringPanel_.add(buttonPanel);

        // Create the OK, cancel buttons.
        okButton_ = new Button(ResourceBundleLoader.getCoreText("DLG_OK_BUTTON"));
        okButton_.addActionListener(listener_);
        okButton_.addFocusListener(listener_);
        okButton_.addKeyListener(listener_);
        buttonPanel.add(okButton_);
        cancelButton_ = new Button(ResourceBundleLoader.getCoreText("DLG_CANCEL_BUTTON"));
        cancelButton_.addActionListener(listener_);
        cancelButton_.addFocusListener(listener_);
        cancelButton_.addKeyListener(listener_);
        buttonPanel.add(cancelButton_);

        // Arrange the components in the dialog.
        pack();

        // Add a listener for window events.
        addWindowListener(listener_);
    }

    // Added paint method to draw the border around the password boxes.
    public void paint(Graphics gc)
    {
        Rectangle pos = passwordLabel_.getBounds();
        int left = pos.x/2;
        int top = pos.height/2 + pos.y;
        int right = (getBounds().width + confirmTextField_.getBounds().x + confirmTextField_.getBounds().width) / 2;
        int bottom = (centeringPanel_.getBounds().y + confirmTextField_.getBounds().y + confirmTextField_.getBounds().height) / 2;

        gc.setColor(Color.white); // Use white to make it look bevelled!
        gc.drawRect(left+1, top+1, right-left, bottom-top);

        gc.setColor(Color.black);
        gc.drawRect(left, top, right-left, bottom-top);

        super.paint(gc);
    }

    // Display the dialog and wait for user response.
    // @param  system  The AS/400 system name.
    // @param  userId  The user ID.
    // @return  true if the the user selects 'OK'; false if the user selects 'Cancel'.
    boolean prompt(String systemName, String userId)
    {
        // Set the system name and user ID text fields.
        systemNameTextField_.setText(systemName);
        userIdTextField_.setText(userId);

        // Clear the password text fields.
        oldTextField_.setText("");
        newTextField_.setText("");
        confirmTextField_.setText("");

        // Request focus.
        listener_.setFocalPoint(oldTextField_);

        // Make the window visible.  The call to show() will block the current thread until we hide or dispose of this dialog.
        show();

        return listener_.getFocalPoint() != cancelButton_;
    }

    // This method returns the old password entered by the user.  Note: since the text field echoes asterisks to the user, the text returned by this method is the characters that were typed; not the visible contents of the text field.
    // @return  The password.
    String getOldPassword()
    {
        return oldTextField_.getText();
    }

    // This method returns the new password entered by the user.  Note: since the text field echoes asterisks to the user, the text returned by this method is the characters that were typed; not the visible contents of the text field.
    // @return  The password.
    String getNewPassword()
    {
        return newTextField_.getText();
    }

    // This method returns the confirmation password entered by the user.  Note: since the text field echoes asterisks to the user, the text returned by this method is the characters that were typed; not the visible contents of the text field.
    // @return  The password.
    String getConfirmPassword()
    {
        return confirmTextField_.getText();
    }

    // This methods adds a user interface component to the specified container using the specified constraints.
    // @param  container  The container to add the component to.
    // @param  component  The user interface component to add.
    // @param  constraints  The constraints for the component.
    // @param  x  The x coordinate of the leftmost cell of the component.
    // @param  y  The y coordinate of the topmost cell of the component.
    // @param  width  The horizontal measurement of the component in cells.
    // @param  height  The vertical measurement of the component in cells.
    protected void add(Container container, Component component, GridBagConstraints constraints, int x, int y, int width, int height)
    {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        LayoutManager layout = container.getLayout();
        if (layout != null && layout instanceof GridBagLayout)
        {
            ((GridBagLayout)layout).setConstraints(component, constraints);
        }
        container.add(component);
    }
}
