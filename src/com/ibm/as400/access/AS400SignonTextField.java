///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400SignonTextField.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// Implements a text field that only accepts valid characters for an AS/400 userid or password, with a maximum of 10 characters total.
// This text field will auto-uppercase any lowercase letters that are typed.
// It also rejects any characters that are not allowable in an AS/400 userid or password.  The allowed characters are A-Z, a-z, 0-9, and the @, #, $, and _ characters.
// Up to 10 characters may be typed into this text field.
// Beeps are issued if the character limit is reached or invalid characters are entered.
// Note:  This class becomes obsolete when the new password support becomes integrated into the AS/400.
class AS400SignonTextField extends TextField implements KeyListener
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static final long serialVersionUID = 4L;
    private final String allowableChars_ = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$#_@";

    public AS400SignonTextField()
    {
        super();
        addKeyListener(this);
    }

    public void keyTyped(KeyEvent e)
    {
        char c = e.getKeyChar();

        if (c == KeyEvent.VK_BACK_SPACE)
        {
            return;
        }

        switch (e.getKeyCode())
        {
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_TAB:
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_CAPS_LOCK:
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_END:
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_INSERT:
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_CANCEL:
            case KeyEvent.VK_CLEAR:
                return;
        }

        if (getText().length() < 10) // Max length is 10.
        {
            if (allowableChars_.indexOf(c) > -1)
            {
                e.setKeyChar(Character.toUpperCase(c));
                return;
            }
        }

        // Rejected -- either too many chars, or bad char entered.
        e.consume();
        Toolkit.getDefaultToolkit().beep();
    }

    public void keyPressed(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
    }
}
