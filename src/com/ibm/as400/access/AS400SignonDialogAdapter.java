///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400SignonDialogAdapter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.TextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

// Implements all of the listeners that the PasswordDialog, ChangePasswordDialog, and SignonDialog classes need.
class AS400SignonDialogAdapter implements ActionListener, FocusListener, KeyListener, WindowListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private Dialog c_ = null;

    private Component focalPoint_ = null;

    public AS400SignonDialogAdapter(Dialog listenee)
    {
        c_ = listenee;
    }

    // Get the component whose focus we saved.
    public Component getFocalPoint()
    {
        return focalPoint_;
    }

    // Save this component's focus for later.
    public void setFocalPoint(Component c)
    {
        focalPoint_ = c;
    }

    // We're an action listener.
    public void actionPerformed(ActionEvent event)
    {
        c_.setVisible(false);
    }

    // We're a focus listener.
    public void focusGained(FocusEvent event)
    {
        focalPoint_ = event.getComponent();
    }

    // We're a focus listener.
    public void focusLost(FocusEvent event)
    {
        // Cancel selection if field loses focus.
        if(event.getSource() instanceof TextComponent)
        {
            ((TextComponent)event.getSource()).setSelectionEnd(0);
        }
    }

    // We're a key listener.
    public void keyPressed(KeyEvent event)
    {
        if(event.getKeyCode() == KeyEvent.VK_ENTER)
        {
            c_.setVisible(false);
        }
    }

    // We're a key listener.
    public void keyReleased(KeyEvent event)
    {
    }

    // We're a key listener.
    public void keyTyped(KeyEvent event)
    {
    }

    // We're a window listener.
    public void windowActivated(WindowEvent event)
    {
        if(event.getWindow().isVisible())
        {
            focalPoint_.requestFocus();
        }
    }

    // We're a window listener.
    public void windowClosed(WindowEvent event)
    {
    }

    // We're a window listener.
    public void windowClosing(WindowEvent event)
    {
        c_.dispose();
    }

    // We're a window listener.
    public void windowDeactivated(WindowEvent event)
    {
    }

    // We're a window listener.
    public void windowDeiconified(WindowEvent event)
    {
        if(event.getWindow().isVisible())
        {
            focalPoint_.requestFocus();
        }
    }

    // We're a window listener.
    public void windowIconified(WindowEvent event)
    {
    }

    // We're a window listener.
    public void windowOpened(WindowEvent event)
    {
        if(event.getWindow().isVisible())
        {
            focalPoint_.requestFocus();
        }
    }
}
