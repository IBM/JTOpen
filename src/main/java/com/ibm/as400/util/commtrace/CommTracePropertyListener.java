///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: CommTracePropertyListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Listens for PropertyChanges to our OptionPanes. The correct 
 * action is performed based on the property change that occured.
 */
class CommTracePropertyListener implements PropertyChangeListener {
    private CommTrace comm;

	/**
	 * Constructs a new CommTracePropertyListener.
	 * @param comm The CommTrace object.
	 */
    public CommTracePropertyListener(CommTrace comm) {
		this.comm = comm;
    }
	
	/**
	 * This method gets called when a bound property is changed.
	 * @param e The event that occured
	 */
    public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		JOptionPane op = comm.getOptionPane();
		JDialog dialog = comm.getDialog();
		if (dialog.isVisible() && (e.getSource() == op)
			&& (prop.equals(JOptionPane.VALUE_PROPERTY) ||	// Make sure that the dialog had a 
			prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {   // property change and not someother widget. 

			Object value = op.getValue();
			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}

			// Reset the JOptionPane's value.
			op.setValue(JOptionPane.UNINITIALIZED_VALUE);
			if (value.equals(comm.getButtonString1())) {
				comm.getDialog().setVisible(false);
				if(comm.getRemoteButton().isSelected()) {
					comm.formatremote();
				} else if(comm.getLocalButton().isSelected()) {
					comm.format();
				}
			
			} else { // user closed dialog or clicked cancel
				dialog.setVisible(false);
			}
		}
    }
}
