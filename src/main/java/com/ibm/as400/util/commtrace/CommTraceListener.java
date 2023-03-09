///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: CommTraceListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;

/**
 * CommTraceListener is a Listener for the Commtrace object. This class allows widgets on
 * the screen to respond to user events that occur on that widget.
 */
class CommTraceListener implements ActionListener,KeyListener {
    private CommTrace c;
    private Format fmt;
    
    /**
     * Constructs a CommTraceListener
     * @param c This CommTrace
     */
    public CommTraceListener(CommTrace c) {
		this.c = c;
    }
   
   
   	/**
   	 * Invoked when an action occurs.
   	 * @param e The Event that occured.
   	 */
    public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		JButton[] b = c.getButtons();
		if(source == c.getDisconnectMenuItem()) {
			c.disconnect();
		} else if(source == c.getExitMenuItem()) {
			c.exit();
		} else if(source == c.getDisplayMenuItem()) {
			c.open();
		} else if(source == c.getTransferMenuItem()) {
			c.transfer();
		} else if(source == c.getFormatMenuItem()) {
			c.formatoptions();
		} else if(source == c.getAboutMenuItem()) {
			c.about();
		} else if(source == b[c.TRANSFER]) {
			c.transfer();
		} else if(source == b[c.FORMAT]) {
			c.formatoptions();
		} else if(source == b[c.OPEN]) {
			c.open();
		} else if(source == b[c.FMTRMT]) {
			c.formatoptions();
		} else if(source == b[c.OPENRMT]) {
			c.setFormatRemote(null); // Make sure the file dialog shows
			c.open();
		} else if(source == c.getAboutOkayButton()) {
			c.getAboutFrame().setVisible(false);
		} else if(source == c.getRemoteButton()) {
			b[c.TRANSFER].setEnabled(false);
			b[c.FORMAT].setEnabled(false);
			b[c.OPEN].setEnabled(false);
			b[c.FMTRMT].setEnabled(true);
			b[c.OPENRMT].setEnabled(true);
		} else if(source == c.getLocalButton()) {
			b[c.TRANSFER].setEnabled(true);
			b[c.FORMAT].setEnabled(true);
			b[c.OPEN].setEnabled(true);
			b[c.FMTRMT].setEnabled(false);
			b[c.OPENRMT].setEnabled(false);
		}
		c.framerepaint(); // Make sure the frame is going to be redrawn
    }

	/**
	 * Invoked when a key has been typed.
	 * @param e The associated KeyEvent.
	 */
    public void keyTyped(KeyEvent e) {}

	/**
	 * Invoked when a key has been pressed.
	 * @param e The associated KeyEvent.
	 */
	public void keyPressed(KeyEvent e) {
		JButton[] b = c.getButtons();
		Object source = e.getSource();
		int keyCode = e.getKeyCode();
		if(source==b[c.TRANSFER]) { 
		    if(keyCode==KeyEvent.VK_ENTER) {
				c.transfer();
			}
		} else if(source==b[c.FORMAT]) { 
		    if(keyCode==KeyEvent.VK_ENTER) {
				if(c.getRemoteButton().isSelected()) {
					c.formatremote();
				} else if(c.getLocalButton().isSelected()) {
					c.formatoptions();
				}		
			}
		} else if(source==b[c.OPEN]) { 
		    if(keyCode==KeyEvent.VK_ENTER) {
				c.open();
			}
		}
    }

	/**
	 * Invoked when a key has been released.
	 * @param e The associated KeyEvent.
	 */
    public void keyReleased(KeyEvent e) {}
}


