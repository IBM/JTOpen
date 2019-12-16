///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: OutputThread.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import com.ibm.as400.access.Trace;
import com.ibm.as400.access.JavaApplicationCall;

/**
 * The thread where remote standard out is sent when running JavaApplication call
 */
class OutputThread implements Runnable {
	private JavaApplicationCall jaCall_;
	private Thread outThread_;

	public OutputThread(JavaApplicationCall jaCall) {
		jaCall_= jaCall;
	}
	/**
	 * Returns the currently running format Thread
	 * @return The Thread 
	 */
	public Thread getThread() {
		return outThread_;
	}

	/**
	 * Sets the thread. 
	 * @param Thread The thread this object is running under.
	 */
	public void setThread(Thread tr) {
		outThread_= tr;
	}

	/**
	 * Starts the Output Thread to recieve debug info from the remotely running Java program.
	 */
	public void run() {
		Thread myThread= Thread.currentThread();
		while (outThread_ == myThread) {
			String s= jaCall_.getStandardOutString();
			if (s != null)
				Trace.log(Trace.INFORMATION, s);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// the VM doesn't want us to sleep anymore,
				// so get back to work
			}
		}
	}
}