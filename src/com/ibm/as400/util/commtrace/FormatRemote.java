///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: FormatRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.JavaApplicationCall;
import com.ibm.as400.vaccess.FileFilter;
import com.ibm.as400.vaccess.IFSFileDialog;
import java.awt.Cursor;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * Used to remotely format a iSeries trace residing in a IFS directory.
 */
class FormatRemote implements Runnable {
	private String classpath_ = "/QIBM/ProdData/OS400/JT400/lib/JT400Native.jar";
	private Thread fmtThread;
	private JavaApplicationCall jaCall;
	public static final String EXT = ".bin", CLASS="FormatRemote";
	private AS400 sys;
	private String file = null,
					path = null,
					filterIPaddr_= null,
					filterIPaddr2_= null,
					filterPort_= null,
					filterBdcst_= null;

	private String verbose_;
	private CommTrace com_;

	/**
	 * Constructs a FormatRemote object.
	 * @param sys The system to connect to.
	 * @param verbose The verbosity of this format.
	 * @param filterIPaddr The IP address to filter on.
 	 * @param filterIPaddr2 The second IP address to filter on.
 	 * @param filterPort The port to filter on.
 	 * @param filterBdcst Filter broadcast or not.
 	 * @param com This CommTrace object.
	 */
	public FormatRemote(
		AS400 sys,
		String verbose,
		String filterIPaddr,
		String filterIPaddr2,
		String filterPort,
		String filterBdcst,
		CommTrace com) {
		try {
			this.sys = sys;
			filterIPaddr_=filterIPaddr;
			filterIPaddr2_=filterIPaddr2;
			filterPort_=filterPort;
			filterBdcst_=filterBdcst;
			verbose_ = verbose;
			com_ = com;

			// Construct a JavaApplicationCall object.
			jaCall = new JavaApplicationCall(sys);

			// Set the Java application to be run.
			jaCall.setJavaApplication("com.ibm.as400.commtrace.Format");
			// Set the classpath environment variable used by the AS/400's
			// JVM so it can find the class to run.
			jaCall.setClassPath(
				classpath_);

		} catch (Exception e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".FormatRemote() " + "Exception in FormatRemote", e);
			}
		}
	}

	/**
	 * returns the currently running format Thread.
	 * @return Thread
	 */
	public Thread getThread() {
		return fmtThread;
	}

	/**
	 * Sets the thread. 
	 * @param tr The thread this object is running under.
	 */
	public void setThread(Thread tr) {
		fmtThread = tr;
	}
	
	/**
	 * Returns the file choosen by the user.
	 * @return String
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Returns the path choosen by the user.
	 * @return String
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Runs the FormatClient which queries the user for a file and then. 
	 * sends the filename to the FormatServer and waits for it to return.
	 */
	public void run() {
		Thread myThread = Thread.currentThread();
		while (fmtThread == myThread) {

			IFSFileDialog fd =
				new IFSFileDialog((new JFrame()), "Pick file to Format", sys);

			FileFilter[] filterList = { new FileFilter("All files (*.*)", "*.*")};
			fd.setFileFilter(filterList, 0);

			String fullpath = "";
			try {
				if (fd.showDialog() == IFSFileDialog.OK) {
					String host = sys.getSystemName();
					fullpath = fd.getAbsolutePath(); // get fully qualified filename
					file = fd.getFileName();
					path = fd.getDirectory();

					String[] args2 =
						{ "-c", "true", "-v", verbose_, "-t", fullpath, "-o", fullpath + EXT, "-ip", filterIPaddr_, "-ip2", filterIPaddr2_, "-port", filterPort_, "-broadcast", filterBdcst_};

					jaCall.setParameters(args2);

					// Start the thread that will receive standard output
					if (verbose_.equals("true")) {
						OutputThread ot = new OutputThread(jaCall);
						Thread otThread = new Thread(ot, "OutputThread");
						ot.setThread(otThread);
						otThread.start();
					}

					// Start the program.  The call to run() will not return
					// until the AS/400 Java program completes.  If the Toolbox
					// cannot start the Java program, false is returned with
					// a list of AS/400 message objects indicating why the program
					// could not start.
					if(com_!=null) {
						((Component) CommTrace.getMainFrame()).setCursor(
							new Cursor(Cursor.WAIT_CURSOR));
					}
					if (jaCall.run() != true) {
						AS400Message[] messageList = jaCall.getMessageList();
						for (int msg = 0; msg < messageList.length; msg++)
							Trace.log(Trace.ERROR,CLASS + ".run() " + (messageList[msg].toString()));
					}
					if(com_!=null) {
						((Component) CommTrace.getMainFrame()).setCursor(
							new Cursor(Cursor.DEFAULT_CURSOR));
						JOptionPane.showMessageDialog(
							CommTrace.getMainFrame(),
							"Format of " + fullpath + " on " + host + " compeleted",
							"Format Complete",
							JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} catch (Exception e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".run() " + "Exception in FormatRemote", e);
				}
			}
			fmtThread = null; // End this thread
			if(com_!=null) {
				com_.open(); // Call the open function to open the file we just formatted
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// the VM doesn't want us to sleep anymore,
				// so get back to work
			}
		}
	}
}