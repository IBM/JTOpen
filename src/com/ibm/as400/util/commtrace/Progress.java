///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Progress.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import com.ibm.as400.access.Trace;
import java.awt.Toolkit;

/**
 * To show a progress dialog while the file is being downloaded so our user knows what is happening.
 */
class Progress implements Runnable {
    private ProgressMonitor progressMonitor;
    private String msg,file,endmsg;
    private int total;
	private Thread progThread_; // Controls the execution of the Progress Dialog
	private static final String CLASS="Progress";

    /**
     * Creates the ProgressDialog.
     * @param file  The name of the file we are monitoring.
     * @param total The total number of records we are processing.
     */
    public Progress(String action,int total,String description) {
		this.total = total;
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + ".Progress() " + "Initializing Progress Dialog"); 
		}
		progressMonitor = new ProgressMonitor(null,
                                    action,
                                    "", 0, total);
		msg=action;
		endmsg = "% of " + total + description;
    }
   
    /**
     * Updates the progress Bar.
     * @param msg   The number of records processed.
     */
    public void updateProgress(int num) {
		progressMonitor.setProgress(num);
		progressMonitor.setNote((int)(num*100)/total + endmsg);
    }

	/**
	 * Returns the currently running format Thread.
	 * @return Thread
	 */
	public Thread getThread() {
		return progThread_;
	}
	
	/**
	 * Sets the thread. 
	 * @param Thread The thread this object is running under.
	 */
	public void setThread(Thread tr) {
		progThread_ = tr;
	}

    /**
     * called when our thread is invoked with the Thread.start() method.
     * @see Thread
     */
    public void run() {
		Thread myThread = Thread.currentThread();
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Progress Dialog.getMillisToPopup():" + progressMonitor.getMillisToPopup()); 
		}
		while(progThread_==myThread) {
			if (progressMonitor.isCanceled()) {
				progressMonitor.close();
				progThread_=null;
				Toolkit.getDefaultToolkit().beep();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e){
			// the VM doesn't want us to sleep anymore,
			// so get back to work
			}
		}
		progThread_=null;
		progressMonitor.close();
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Closing Progress Dialog"); 
		}
    }

	/**
	 * The state of the Progress Dialog.
	 * @return true if the dialog has been canceled.
	 */
	public boolean isCanceled() {
		return progressMonitor.isCanceled();
	}
}
