///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: PasswordDialog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Button;
import java.awt.Checkbox;
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
import java.awt.TextField;

/**
 The PasswordDialog class is a dialog for prompting end-users for a system name, a user ID, and/or a password.  End-user programs will typically not need to use this class directly.  Instead, such programs should allow the AS400 class to display the dialog when necessary.
 **/
public class PasswordDialog extends Dialog
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Implementation notes:
    // * There is a concern that making this class public makes it too easy for external developers to display a Toolbox signon prompt and gather end-user passwords.  The general consensus is that this would be easy enough for most Java developers to do, anyway (without this class)!
    // * We are intentionally NOT exposing control over the checkboxes.  No reason other than to minimize the public interface.

    // Private data.
    private AS400SignonDialogAdapter listener_;
    private TextField systemNameTextField_;
    private TextField userIdTextField_;
    private TextField passwordTextField_;
    private Checkbox defaultUserCheckbox_;
    private Checkbox cachePasswordCheckbox_;
    private Button okButton_;
    private Button cancelButton_;

    /**
     Constructs a PasswordDialog object.
     @param  parent  The parent frame.
     @param  titleText  The title text.
     **/
    public PasswordDialog(Frame parent, String titleText)
    {
        this(parent, titleText, false);
    }

    PasswordDialog(Frame parent, String titleText, boolean showCheckbox)
    {
        super(parent, titleText, true);

        listener_ = new AS400SignonDialogAdapter(this);

        // Create a GridBagLayout manager.
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(8, 8, 0, 8);
        setLayout(layout);

        setResizable(false);

        // Set the background color to light gray.
        setBackground(Color.lightGray);

        // Create the 'System:' Label and add to the panel.
        Label label = new Label(ResourceBundleLoader.getCoreText("DLG_SYSTEM_LABEL"), Label.LEFT);
        add(label, layout, constraints, 0, 0, 1, 1);

        // Create the system text field and add to the panel.
        systemNameTextField_ = new TextField(10);
        systemNameTextField_.addFocusListener(listener_);
        systemNameTextField_.addKeyListener(listener_);
        add(systemNameTextField_, layout, constraints, 1, 0, 1, 1);

        // Create the 'User ID:' Label and add to the panel.
        label = new Label(ResourceBundleLoader.getCoreText("DLG_USER_ID_LABEL"), Label.LEFT);
        add(label, layout, constraints, 0, 1, 1, 1);

        // Create the user id text field and add to the panel.
        userIdTextField_ = new AS400SignonTextField();
        userIdTextField_.addFocusListener(listener_);
        userIdTextField_.addKeyListener(listener_);
        add(userIdTextField_, layout, constraints, 1, 1, 1, 1);

        // Create the 'Password:' Label and add to the panel.
        label = new Label(ResourceBundleLoader.getCoreText("DLG_PASSWORD_LABEL"), Label.LEFT);
        add(label, layout, constraints, 0, 2, 1, 1);

        // Create the password text field and add to the panel.
        passwordTextField_ = new AS400SignonTextField();
        passwordTextField_.setEchoChar('*');
        passwordTextField_.addFocusListener(listener_);
        passwordTextField_.addKeyListener(listener_);
        add(passwordTextField_, layout, constraints, 1, 2, 1, 1);

        // Create the default checkbox.
        defaultUserCheckbox_ = new Checkbox(ResourceBundleLoader.getCoreText("DLG_DEFAULT_PASSWORD_CHECK_BOX"));
        defaultUserCheckbox_.addFocusListener(listener_);
        defaultUserCheckbox_.addKeyListener(listener_);

        cachePasswordCheckbox_ = new Checkbox(ResourceBundleLoader.getCoreText("DLG_CACHE_PASSWORD_CHECK_BOX"));
        cachePasswordCheckbox_.addFocusListener(listener_);
        cachePasswordCheckbox_.addKeyListener(listener_);

        if (showCheckbox)
        {
            // Create panels to hold the checkboxes.
            Panel centeringPanel = new Panel();
            centeringPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            add(centeringPanel, layout, constraints, 0, 3, 2, 2);
            Panel checkboxPanel = new Panel();
            checkboxPanel.setLayout(new GridLayout(2, 1, 2, 2));
            centeringPanel.add(checkboxPanel);

            // Add the default checkbox.
            checkboxPanel.add(defaultUserCheckbox_);

            // Add the cache password checkbox.
            checkboxPanel.add(cachePasswordCheckbox_);
        }

        // Create panels to hold the 'OK', and 'Cancel' buttons.
        Panel centeringPanel = new Panel();
        centeringPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        constraints.insets = new Insets(8, 8, 8, 8);
        add(centeringPanel, layout, constraints, 0, 5, 2, 1);
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(1, 2, 8, 0));
        centeringPanel.add(buttonPanel);

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
        addFocusListener(listener_);
    }

    private void add(Component component, GridBagLayout layout, GridBagConstraints constraints, int x, int y, int width, int height)
    {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        layout.setConstraints(component, constraints);
        add(component);
    }

    void disableDefaultUserCheckbox()
    {
        defaultUserCheckbox_.setEnabled(false);
    }

    void disablePasswordCacheCheckbox()
    {
        cachePasswordCheckbox_.setEnabled(false);
    }

    void enableDefaultUserCheckbox()
    {
        defaultUserCheckbox_.setEnabled(true);
    }

    void enablePasswordCacheCheckbox()
    {
        cachePasswordCheckbox_.setEnabled(true);
    }

    boolean getDefaultState()
    {
        return defaultUserCheckbox_.getState();
    }

    /**
     Returns the password.
     @return  The password.
     **/
    String getPassword()
    {
        return passwordTextField_.getText();
    }

    boolean getPasswordCacheState()
    {
        return cachePasswordCheckbox_.getState();
    }

    /**
     Returns the system name.
     @return  The system name.
     **/
    public String getSystemName()
    {
        return systemNameTextField_.getText();
    }

    /**
     Returns the user ID.
     @return  The user ID.
     **/
    public String getUserId()
    {
        return userIdTextField_.getText();
    }

    void setDefaultUserState(boolean state)
    {
        defaultUserCheckbox_.setState(state);
    }

    void setPasswordCacheState(boolean state)
    {
        cachePasswordCheckbox_.setState(state);
    }

    /**
     Sets the system name.
     @param  systemName  The system name.
     **/
    public void setSystemName(String systemName)
    {
        systemNameTextField_.setText(systemName);
        systemNameTextField_.setEnabled(false);
    }

    /**
     Sets the user ID.
     @param  userID  The user ID.
     **/
    public void setUserId(String userId)
    {
        userIdTextField_.setText(userId);
    }

    /**
     Displays the password dialog.
     @return  true if the dialog was exited using the OK button, false otherwise.
     **/
    boolean prompt()
    {
        passwordTextField_.setText("");

        // Start the focus in the appropriate field.
        if (getSystemName().length() == 0)
            listener_.setFocalPoint(systemNameTextField_);
        else if (getUserId().length() == 0)
            listener_.setFocalPoint(userIdTextField_);
        else
            listener_.setFocalPoint(passwordTextField_);

        // Make the window visible.  The call to show() will block the current thread until we hide or dispose of this dialog.
        super.show();

        return listener_.getFocalPoint() != cancelButton_;
    }
}
