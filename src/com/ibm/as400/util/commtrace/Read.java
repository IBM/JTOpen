///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Read.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.Trace;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * The Read class is used to read an IFS file down from an iSeries and store it 
 * in the local PC.<br>
 * A Progress Dialog is displayed and updated so the user is kept current as to
 * the progression of the download.
 */
class Read implements Runnable {

	private String rmtfile, // The remote file to download
	lclfile; // The local file to save to
	private AS400 sys; // The system to connect to
	private CommTrace comm; // The commtrace object so we manage our thread
	private Progress pro; // The progress Dialog
	private Thread proThread; // The progress dialog thread
	// The tread to transfer a file to the local PC
	private Thread readThread_= null;
	private int bytes= 0; // The number of bytes read

	private static final String CLASS = "Read";

	/**
	 * Creates a read object which will open an IFSFileStream on the remote file
	 * from the given system and read into the local file.
	 * @param rmtfile   Path of the remote file to download
	 * @param lclfile   Path of the local file to save to
	 * @param sys	    The system to connect to
	 */
	public Read(CommTrace comm, String rmtfile, String lclfile, AS400 sys) {
		this.comm= comm;
		this.rmtfile= rmtfile;
		this.lclfile= lclfile;
		this.sys= sys;
	}

	/**
	 * Transfers the file to the local PC.
	 */
	public void run() {
		byte[] tmp= null;
		int len= 0;
		bytes= 0;
		Thread myThread= Thread.currentThread();

		while (readThread_ == myThread) {
			IFSFileInputStream file= null;
			BufferedOutputStream out= null;
			try {
				file= new IFSFileInputStream(sys, rmtfile);
				out= new BufferedOutputStream(new FileOutputStream(lclfile));
				pro= new Progress(("Retrieving " + rmtfile), file.available(), " bytes");
				proThread= new Thread(pro, "ProgDiag");
				proThread.start();

				int filesize= file.available();
				if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
					Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Transfer file size:" + filesize);
				}

				int selection=
					JOptionPane.showConfirmDialog(
						CommTrace.getMainFrame(),
						"Start transfer of " + rmtfile + "(" + (filesize / 1024) + " KB)?",
						"Transfer",
						JOptionPane.YES_NO_OPTION);
				if (selection == 1) { // The user cancled the download
					return;
				} else if (selection == 0) { // The user okayed the download				
					byte[] data= new byte[4096];

					// Read while there is data, the Progress dialog isn't cancled, 
					// and the thread is still running 
					while ((len= file.read(data)) != -1
						&& !pro.isCanceled()
						&& readThread_ != null) {
						// Create an array the size of our read. 
						tmp= new byte[len];
						// Copy from input buffer to tmp buffer
						System.arraycopy(data, 0, tmp, 0, len);
						// Write tmp to file.
						out.write(tmp);
						// Increment the total byte counter
						bytes += len;
						pro.updateProgress(bytes);
					}
					// The transfer was canceled so delete the file
					if (pro.isCanceled() || readThread_ == null) {
						file.close();
						out.close();
						File local= new File(lclfile);
						if (local.exists()) {
							local.delete();
						}
					}
				}
			} catch (IOException e) {
				CommTrace.error("File Error", "Error reading/writing file");
			} catch (AS400SecurityException e) {
				CommTrace.error("Security Error", "Security problem opening file");
			}
			if (!pro.isCanceled() && readThread_ != null) {
				if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
					Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Transfering completed");
				}
				JOptionPane.showMessageDialog(
					CommTrace.getMainFrame(),
					"Transfer of " + rmtfile + " completed",
					"Transfer Complete",
					JOptionPane.INFORMATION_MESSAGE);
			}
			readThread_= null; // Tell everyone we are done

			try {
				file.close();
				out.close();
			} catch (IOException e) {
				CommTrace.error("File Error", "Error closing file");
			} catch (NullPointerException e) {
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// the VM doesn't want us to sleep anymore,
				// so get back to work
			}
		}
	}

	/**
	 * Returns the number of bytes that have been read off this stream.
	 * @return The number of bytes
	 */
	public int getBytesRead() {
		return bytes;
	}

	/**
	 * Returns the thread that this read process is executing under.
	 * @returns Thread
	 */
	public Thread getThread() {
		return readThread_;
	}

	/**
	 * Sets the thread this process is executing under.
	 * @param Thread The thread to execute under
	 */
	public void setThread(Thread thread) {
		readThread_= thread;
	}
}
