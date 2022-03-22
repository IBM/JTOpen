///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: CommTraceDisplayListener.java
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
import javax.swing.JButton;

/**
 * The CommTraceDisplayListener class is a Listener for the FormatDisplay class.<br>
 * It allows objects on the FormatDisplay window to respond to user events.
 */
class CommTraceDisplayListener implements ActionListener {
    private FormatDisplay fmt;
    
    /**
     * Constructs a new CommTraceDisplayListener
     * @param fmt The FormatDisplay object
     */
    public CommTraceDisplayListener(FormatDisplay fmt) {
		this.fmt = fmt;
    }
   
   	/**
   	 * Invoked when an action occurs.
   	 * @param e The Event that occured
   	 */
    public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		JButton next = fmt.getNextButton();
	    if(source == fmt.getSaveMenuItem()) {
			fmt.save();
	    } else if(source == fmt.getCloseMenuItem()) {
			fmt.close();
	    } else if(source == fmt.getFindMenuItem()) {
			fmt.find();
	    } else if(source == fmt.getCopyMenuItem()) {
			fmt.copy();
	    } else if(source == fmt.getClearMenuItem()) {
			fmt.clear();
	    } else if(source == fmt.getCutMenuItem()) {
			fmt.cut();
	    } else if(source == fmt.getPasteMenuItem()) {
			fmt.paste();
	    } else if(source == next) {
			// showRecs returns false when there are no more records. So we disable the 
			// button when that occurs.
			next.setEnabled(fmt.showRecs(Integer.parseInt(fmt.getNumberList().getSelectedItem().toString())));
	    } else if(source == fmt.getPrevButton()) {
			fmt.showPrev();
			// User clicked previous so reenable the next button.
			if(!next.isEnabled()) {
				next.setEnabled(true); 
			}
	    }
    }
}