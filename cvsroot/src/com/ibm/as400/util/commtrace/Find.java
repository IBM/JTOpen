///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Find.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * Easily allow for searching of a TextArea.<br>
 * Presents the user with a dialog which allows them to enter text and search for the text in the specified TextArea.
 */
class Find extends WindowAdapter implements KeyListener {
    private JTextArea ta;
    private JFrame find;
    private JButton fnext,
				    cancel;
    private boolean isClosed=false;
    private JCheckBox casebox,
				      wrapbox,
				      reverse;
    private JTextField input;
    private int index=0; // Our position in the text area
    private String search="";

    /** 
     * Creates a Find Dialog to search the specified text area.      
     * @param ta        the TextArea to search.
     */
    public Find(JTextArea ta) {
		this.ta = ta;
		createDialog();
    }

    /**
     * Attempts to send the find dialog to the front of the screen.
     */
    public void toFront() {
		find.toFront();
    }

    /**
     * Creates the find Dialog and presents it to the user. 
     */
    private void createDialog() {
		if(find!=null) {
		    find.toFront();
		    return;
		}
		find = new JFrame(ResourceBundleLoader_ct.getText("Find"));
		JPanel pnl = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		pnl.setLayout((gridbag));
		GridBagConstraints c = new GridBagConstraints();

		input = new JTextField(30);
		fnext = new JButton(ResourceBundleLoader_ct.getText("FindNext"));
		fnext.addKeyListener(this); 
		cancel = new JButton(ResourceBundleLoader_ct.getText("Cancel"));
		cancel.addKeyListener(this);
		casebox = new JCheckBox(ResourceBundleLoader_ct.getText("MatchCase"),true);
		wrapbox = new JCheckBox(ResourceBundleLoader_ct.getText("WrapSearch"),true);
		reverse = new JCheckBox(ResourceBundleLoader_ct.getText("Reverse"),false);

		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridwidth=GridBagConstraints.RELATIVE;
		c.gridheight=GridBagConstraints.RELATIVE;
		gridbag.setConstraints(input,c);
		pnl.add(input);
	
		c.fill=GridBagConstraints.BOTH;
		c.gridheight = 2;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.gridheight=GridBagConstraints.RELATIVE;

		gridbag.setConstraints(fnext,c);
		pnl.add(fnext);
		fnext.addActionListener(new ActionListener() { // When the user selects the find button search the TextArea
				public void actionPerformed(ActionEvent e) {
				    findnext(input.getText());
				} 
		    }
		);

		pnl.add(wrapbox);
		pnl.add(casebox);
		
		c.gridwidth=GridBagConstraints.RELATIVE;
		c.gridheight=GridBagConstraints.REMAINDER;
		gridbag.setConstraints(reverse,c);
		pnl.add(reverse);
	
		c.gridheight = 2;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.gridheight=GridBagConstraints.REMAINDER;
		gridbag.setConstraints(cancel,c);
		pnl.add(cancel);
		cancel.addActionListener(new ActionListener() { // Close the find dialog when the user selects the cancel button
				public void actionPerformed(ActionEvent e) {
				    close(); 
				}
			}
		);

		find.getContentPane().add(pnl);
		find.addWindowListener(this);
	
		//find.pack();
		find.setSize(400,100);
		find.setVisible(true);
		find.invalidate();
		find.validate();
		find.repaint();
    }

    /**
     * Finds the next occurance of the given string in the textarea and selects it.
     *
     * @param search    The string to search for.
     */
    public void findnext(String search) {
		int tmp = 0;
		this.search = search;
		String searchText = ta.getText();
		if(!casebox.isSelected()) { 
		    searchText = searchText.toLowerCase();
		    search = search.toLowerCase(); 
		}
		if(search == null) {
		} else if(search.length() == 0) {
		    CommTrace.error(find,"Error","No String specified");
		} else {
			if(reverse.isSelected()) {
				tmp = searchText.lastIndexOf(search,index-search.length());
			} else {
		    	tmp = searchText.indexOf(search,index+search.length());
			}
	    
		    if(tmp != -1) { // Search text found
				ta.select(tmp,tmp+search.length());
				index=tmp;
		    } else { // Search text not found
				if(index!=0 && wrapbox.isSelected()) { // If the user wants to wrap the search, search again.
				    index = 0;
				    findnext(search);
				} else {
					CommTrace.error(find,"Error","Text not found");
				}
				tmp = 0;
			}
		}
		index=tmp;
		find.invalidate();
		find.validate();
		find.repaint();
    }

	/**
	 * Invoked when a window is in the process of being closed. 
	 * @param e The event for this window.
	 */
    public void windowClosing(WindowEvent e) {
		if(e.getSource() == find) {
		    close();
		}
    }

    /**
     * Closes the find dialog.
     */
    public void close() {
		isClosed=true;
		find.setVisible(false);		
    }

    /**
     * @return true when the find dialog was closed by the user, false otherwise.
     */
    public boolean isClosed() {
		return isClosed;
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
		int keyCode = e.getKeyCode();
		if(e.getSource()==fnext) { // If the enter key is pressed while our search box is selected search.
		    if(keyCode==KeyEvent.VK_ENTER) {
				findnext(input.getText());
			}
		} else if(e.getSource()==cancel) { // Remove find dialog is the user selects cancel
		    if(keyCode==KeyEvent.VK_ENTER) {
				isClosed=true;
				find.setVisible(false);		
			}
		}
    }
    
	/**
	 * Invoked when a key has been released.
	 * @param e The associated KeyEvent.
	 */
    public void keyReleased(KeyEvent e) {}
}
