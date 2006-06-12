///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: FormatDisplay.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.Trace;
import com.ibm.as400.vaccess.FileFilter;
import com.ibm.as400.vaccess.IFSFileDialog;

/**
 * FormatDisplay is used to format traces. The actual formatting is done by the Format class.<br>
 * FormatDisplay is also used to display formated traces from disk.<br>
 */
class FormatDisplay extends WindowAdapter implements Runnable {
	private final String ALL = "*ALL",
						YES = "*YES",
						NO = "*NO",
						CLASS="FormatDisplay";
	private AS400 sys; // The system we are connected to
	private Thread fmtThread=null; // This thread
	private Format fmt=null; // The format object. Our interface to the trace
    private Find find=null; // The find dialog to search our text area
	private String filterIPaddr_, // The filter arguments passed to the class
					filterIPaddr2_,
					filterPort_,
					filterBdcst_; 
    private String currPage="", // The current page  
		    lastPage="", // The previously viewed page
		    slastPage="",// The oldest page
			path=null, // The path of the file we are displaying
			file=null, // The name of the file we are displaying
			filename=null, // The path that contains the file to be traced, and the name of the file
			outfile=null; // The name of the file to save to 
    private int nextRecLen = 0,index=0;
	/** Run a local display operation */
	public final static int OPEN = 1; 
	/** Run a remote display operation */
	public final static int OPENRMT = 2;
	/** Run a format operation and displays the output */
	public final static int FMT = 3; 
	
	private final static String SAVEEXT = ".bin"; // The extension to append to our formated trace output
    private int ifsrecs=0, // The number of records in this commtrace 
				recsdisp=0, // The number of records displayed so far
				page=0, // The page we are showing. 0 - current, 1 - lastPage, 2 - slastPage
				oper; // The operation to be run
	// Format Menubar items
    private JMenuItem fsave, // File Menu
					    fclose,
						ffind, // Edit Menu
					    fcopy,
					    fclear,
					    fcut,
					    fpaste;
    private JButton next,
				    prev;
    private JComboBox numberList_;
    private JLabel msg;
    private JTextArea formattrace;
    private JFrame l;

    /**
	 * Creates a FormatDisplay and performs the specified operation.
	 * @param filterIPaddr The IP address filter.
 	 * @param filterIPaddr2 The IP address filter.
 	 * @param filterPort The port filter.
 	 * @param filterBdcst The broadcast filter.
	 * @param oper The operation to be performed.
     */
    public FormatDisplay(String filterIPaddr, String filterIPaddr2, String filterPort, String filterBdcst,int oper) {
		filterIPaddr_ = filterIPaddr;
		filterIPaddr2_ = filterIPaddr2;
		filterPort_ = filterPort;
		filterBdcst_ = filterBdcst;
		this.oper = oper;

		FileDialog fd;
		if(oper==FMT) {
			// Ask the user for the file to format
			fd = new FileDialog(CommTrace.getMainFrame(),ResourceBundleLoader_ct.getText("FormatDialog"),FileDialog.LOAD);
			fd.show(); // Show the dialog
			String path = fd.getDirectory();
			filename = fd.getFile();

			// If the didn't specify a file return
			if(path==null || filename==null) {
			    return;
			}
			filename = path + filename;
		} else if(oper==OPEN || oper==OPENRMT) {
			open();
		}
	}

    /**
	 * Displays a formated trace. Used to display a file remotely.
     */
    public FormatDisplay(String path,String file,AS400 sys,int oper) {
		this.oper = oper;
		this.sys = sys;

		// The path and file that the user just remotely formated if available
		this.path = path; 
		this.file = file;

		if(oper==OPENRMT || oper==OPEN) {
			open();
		}
	}

    /** 
     * Runs the specified FormatDisplay operation. 
     */
    public void run() {
		// True until we have formated/displayed the file the user specified
		boolean fmt=true; 
		Thread myThread = Thread.currentThread();
		while(fmtThread==myThread) {
		    if(fmt) {
				if(oper==FMT) {
					if(filename==null) { // The user didn't select a file
						return;
					}
								
					// Display file dialog so user can specify a place to save the trace
					FileDialog fd = new FileDialog(CommTrace.getMainFrame(),ResourceBundleLoader_ct.getText("SaveAs"),FileDialog.SAVE);
					fd.setFile(filename + SAVEEXT);
					fd.show();
					String outpath = fd.getDirectory();
					String outfilename = fd.getFile();
					
					if(outfilename==null) { // User didn't sepecify a file
						return;
					}
					outfile = outpath + outfilename;
					// Initalize the format object
					FormatProperties prop = new FormatProperties();
					prop.setProgress(FormatProperties.TRUE);
					
					// Set the filter properties that the user specified
					if(!filterIPaddr_.equals(ALL)) {
						prop.setIPAddress(filterIPaddr_);
						if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
							Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Filtering on IP address");
		   		 		}
					} 
					if(!filterIPaddr2_.equals(ALL)) {
						prop.setSecondIPAddress(filterIPaddr2_);
						if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
							Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Filtering on IP address");
					    }
					}
					if(!filterPort_.equals(ALL)) {
						prop.setPort(filterPort_);
						if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
							Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Filtering on port number");
					    }
					}
					if(filterBdcst_.equals(NO)) {
						prop.setBroadcast(filterBdcst_);
						if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
							Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Filtering broadcast records");
					    }
					}

					this.fmt = new Format(prop,outfile,filename); // Create the format
					long strtime = System.currentTimeMillis(); // Record start time of trace
					this.fmt.toLclBinFile(); // Format and output to the file specified
					long endtime = System.currentTimeMillis(); // Record end time of trace
					if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
						Trace.log(Trace.INFORMATION,CLASS + ".run() " + "Format Start/End/Total Time:" + strtime + "/" + endtime + "/" + (endtime - strtime)); 
					}
					this.fmt.openLclFile(); // Open the outfile for display
				    showOutput(); // Display the output

					fmt = false; // We are done with the user operation.
				} else if(oper==OPEN || oper==OPENRMT) {
					showOutput();
					fmt=false; // We are done with the user operation
				}
		    } else { 
				try { // Idle until the thread is ended
				    Thread.sleep(1000);
				} catch (InterruptedException e) {
				// the VM doesn't want us to sleep anymore,
				// so get back to work
				}
		    }
		}
		this.fmt.close(); // Close the Format before exiting
    }

    /**
     * Creates a frame and displays the output. 
     */
    private void showOutput() {
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + ".showOutput() " + "Showing Output"); 
		}

		ObjectInputStream ser;
		l = new JFrame("Comm Trace");
		formattrace = new JTextArea("",10,80);
		JMenuBar formatmb = new JMenuBar();
		JMenu ffile = new JMenu(ResourceBundleLoader_ct.getText("File"));
		JMenu fedit = new JMenu(ResourceBundleLoader_ct.getText("Edit"));
		JScrollPane p = new JScrollPane(formattrace);

		// Navigation
		JPanel north = new JPanel();
		north.setLayout(new GridLayout(0,1));
		JPanel nav = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		nav.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
	
		c.anchor=GridBagConstraints.EAST;
		c.gridwidth=3;
		c.gridheight=3;

		// Create the Show Label
		JLabel lshow = new JLabel(ResourceBundleLoader_ct.getText("Show"));
		gridbag.setConstraints(lshow,c);
		nav.add(lshow);

		// Create the TextField which allows the user to enter the number of 
		// records they want displayed at a time
		c.anchor=GridBagConstraints.CENTER;
		c.gridwidth=GridBagConstraints.RELATIVE;
		String[] numberStrings = { "10", "50", "100", "1000", "10000" };

		// Create the combo box, select item at index 4.
		numberList_ = new JComboBox(numberStrings);
		numberList_.setSelectedIndex(2);
		gridbag.setConstraints(numberList_,c);
		nav.add(numberList_);

		// Create the frames label
		c.anchor=GridBagConstraints.CENTER;
		c.gridwidth=GridBagConstraints.REMAINDER;
		JLabel lframes = new JLabel(ResourceBundleLoader_ct.getText("ShowDescription"));
		gridbag.setConstraints(lframes,c);
		nav.add(lframes);
		
		// Create the message description label
		c.anchor=GridBagConstraints.WEST;
		c.gridwidth=GridBagConstraints.RELATIVE;
		c.gridheight=GridBagConstraints.RELATIVE;
		JLabel msgdesc = new JLabel(ResourceBundleLoader_ct.getText("DisplayDescription"));
		gridbag.setConstraints(msgdesc,c);
		nav.add(msgdesc);

		// Create the message label
		c.gridwidth=GridBagConstraints.REMAINDER;
		msg = new JLabel(ResourceBundleLoader_ct.getText("Prolog"));
		gridbag.setConstraints(msg,c);
		nav.add(msg);

		// Create the previous button 
		c.anchor=GridBagConstraints.CENTER;
		c.gridwidth=GridBagConstraints.RELATIVE;
		c.gridheight=GridBagConstraints.REMAINDER;
		prev = new JButton(ResourceBundleLoader_ct.getText("Prev"));
		prev.addActionListener(new CommTraceDisplayListener(this));
		gridbag.setConstraints(prev,c);
		nav.add(prev);

		// Create the Next button
		c.anchor=GridBagConstraints.WEST;
		c.gridwidth=GridBagConstraints.REMAINDER;
		next = new JButton(ResourceBundleLoader_ct.getText("Next"));	
		next.addActionListener(new CommTraceDisplayListener(this));
		gridbag.setConstraints(next,c);
		nav.add(next);
		north.add(nav);

    	String record = ResourceBundleLoader_ct.getText("Record");
		String mac = ResourceBundleLoader_ct.getText("MACAddress");
		
		StringBuffer Banner1 = new StringBuffer();
		Banner1.append(record);
		Banner1.append("       ");
		Banner1.append(ResourceBundleLoader_ct.getText("Data"));
		Banner1.append("      ");
		Banner1.append(record);
		Banner1.append("                     ");
		Banner1.append(ResourceBundleLoader_ct.getText("Destination"));
		Banner1.append("     ");
		Banner1.append(ResourceBundleLoader_ct.getText("Source"));
		Banner1.append("           ");
		Banner1.append(ResourceBundleLoader_ct.getText("Frame"));
		StringBuffer Banner2 = new StringBuffer();
		Banner2.append(ResourceBundleLoader_ct.getText("Number"));
		Banner2.append("  ");
		Banner2.append(ResourceBundleLoader_ct.getText("S/R"));
		Banner2.append("  ");
		Banner2.append(ResourceBundleLoader_ct.getText("Length"));
		Banner2.append("    ");
		Banner2.append(ResourceBundleLoader_ct.getText("Timer"));
		Banner2.append("                      ");
		Banner2.append(mac);
		Banner2.append("     ");
		Banner2.append(mac);
		Banner2.append("      ");
		Banner2.append(ResourceBundleLoader_ct.getText("Format"));
		String Banner3 = "------  ---  ------    ------------               --------------  --------------   ------";

		// Banner
		JPanel ban = new JPanel();
		ban.setLayout(new GridLayout(3,1));
		JLabel banner  = new JLabel(Banner1.toString()),
		       banner2 = new JLabel(Banner2.toString()),
		       banner3 = new JLabel(Banner3);
		banner.setFont(new Font("Monospaced", Font.PLAIN, 12));
		banner2.setFont(new Font("Monospaced", Font.PLAIN, 12));
		banner3.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ban.add(banner);
		ban.add(banner2);
		ban.add(banner3);

	    // Format Menu Bar
	    // File Menu
	    fsave = new JMenuItem(ResourceBundleLoader_ct.getText("Save"),KeyEvent.VK_S); // Create the menu and its key binding
	    fsave.setAccelerator(KeyStroke.getKeyStroke( // Bind the key binding to the menu item
						KeyEvent.VK_S, ActionEvent.CTRL_MASK));
						
		// Disable save if they are displaying a previously traced file
		if(oper==OPEN || oper==OPENRMT) {
			fsave.setEnabled(false);
		}

	    fclose = new JMenuItem(ResourceBundleLoader_ct.getText("Close"),KeyEvent.VK_W);
	    fclose.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_W, ActionEvent.CTRL_MASK));

	    // Edit Menu
	    ffind = new JMenuItem(ResourceBundleLoader_ct.getText("Find"),KeyEvent.VK_F);
	    ffind.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_F, ActionEvent.CTRL_MASK));
	    fcopy = new JMenuItem(ResourceBundleLoader_ct.getText("Copy"),KeyEvent.VK_C);
	    fcopy.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_C, ActionEvent.CTRL_MASK));
	    fclear = new JMenuItem(ResourceBundleLoader_ct.getText("Clear"));
	    fcut = new JMenuItem(ResourceBundleLoader_ct.getText("Cut"),KeyEvent.VK_X);
	    fcut.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_X, ActionEvent.CTRL_MASK));
	    fpaste = new JMenuItem(ResourceBundleLoader_ct.getText("Paste"),KeyEvent.VK_V);
	    fpaste.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_V, ActionEvent.CTRL_MASK));

		// Add the menu items and their action listener
	    ffile.add(fsave);
	    fsave.addActionListener(new CommTraceDisplayListener(this));
		ffile.add(fclose);
	    fclose.addActionListener(new CommTraceDisplayListener(this));

	    fedit.add(ffind);
	    ffind.addActionListener(new CommTraceDisplayListener(this));
	    fedit.add(fcopy);
	    fcopy.addActionListener(new CommTraceDisplayListener(this));
	    fedit.add(fclear);
	    fclear.addActionListener(new CommTraceDisplayListener(this));
	    fedit.add(fcut);
	    fcut.addActionListener(new CommTraceDisplayListener(this));
	    fedit.add(fpaste);
	    fpaste.addActionListener(new CommTraceDisplayListener(this));
	    
	    // Disable cut and past
	    fcut.setEnabled(false);
	    fpaste.setEnabled(false);
	    
	    formatmb.add(ffile);
	    formatmb.add(fedit);
	    l.setJMenuBar(formatmb);

		formattrace.enableInputMethods(true);
		// Need a monospaced font otherwise formatting will not look correct
		formattrace.setFont(new Font("Monospaced", Font.PLAIN, 12));

		if(fmt==null) { // If format is null we were never initialized so just return
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.ERROR, CLASS + ".showOutput() " + "initialization failed"); 
			}
			return;
		}

		showRecs(1); // Read in and display the prolog
		msg.setText("Prolog");// Viewing the prolog
		recsdisp=0;
		ifsrecs=fmt.getIntFromFile(); // Get the total number of records that this trace contains

		l.getContentPane().add(p,BorderLayout.CENTER);
		l.getContentPane().add(north,BorderLayout.SOUTH);
		l.getContentPane().add(ban,BorderLayout.NORTH);
        l.setBackground(Color.black);
		l.setForeground(Color.white);
	
		l.addWindowListener(this);
		l.setSize(800,600);
		l.setVisible(true);
    }

    /** 
     * Displays numrecs in the TextArea. 
     * @param numrecs   the number of records to show.
	 * @return returns false when there are no more records to display.
     */
    public boolean showRecs(int numrecs) {
		int tcprecs = fmt.getNumberOfTCPRecords();
		
		// Display the wait cursor
		((Component) l).setCursor(new Cursor(Cursor.WAIT_CURSOR));
							
		if(page==0) { // If we are supposed to view a new page
			if(oper==OPEN || oper==OPENRMT) { // We are reading the data in from a stream 
				int end = recsdisp+numrecs;
				if(end>ifsrecs) {
					msg.setText(recsdisp + "-" + ifsrecs + " " + ResourceBundleLoader_ct.getText("of") + " " + ifsrecs + " " + ResourceBundleLoader_ct.getText("possible"));  
				} else {
					msg.setText(recsdisp + "-" + (recsdisp+numrecs) + " " + ResourceBundleLoader_ct.getText("of") + " " + ifsrecs + " " + ResourceBundleLoader_ct.getText("possible"));
				}
				recsdisp = end;

				if(lastPage.equals("")) { 
					// Save the page we are currently viewing
					lastPage = formattrace.getText();
				} else {
				    slastPage = lastPage;
				    lastPage = formattrace.getText();
				}

				formattrace.setText(null); // Clear the TextArea
				for(int i=0;i<numrecs;i++) {
					String rec=null;
					if((rec = fmt.getRecFromFile())!=null) {
						formattrace.append(rec);
					} else { // No more records to display so send an error back
						// Display the default cursor
						((Component) l).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						return false;
					}
				}
			} else {
				// Set the message of the text label that says where in the 
				// trace we are
				int end = recsdisp+numrecs;

				// If the user requested to view records past the end of what 
				// we have set the limit to the last record
				if(end>tcprecs) { 
				    msg.setText(recsdisp + "-" + (tcprecs) + " " + ResourceBundleLoader_ct.getText("of") + " " + tcprecs);  
				} else {
				    msg.setText(recsdisp + "-" + (end) + " " + ResourceBundleLoader_ct.getText("of") + " " + tcprecs);  
				}
				recsdisp = end; 
				if(lastPage.equals("")) { 
					// Save the page we are currently viewing
					lastPage = formattrace.getText();
				} else {
				    slastPage = lastPage;
				    lastPage = formattrace.getText();
				}
	
				formattrace.setText(null); // Clear the TextArea
				for(int i=0;i<numrecs;i++) {
					String rec=null;
					if((rec = fmt.getRecFromFile())!=null) {
						formattrace.append(rec);
					} else { // No more records to display so send an error back
						// Display the default cursor
						((Component) l).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						return false;
					}
				}
			}
		} else if(page==1) { // The user wants to view the current page 
		    formattrace.setText(currPage);
			// If the user is reading a file of the disk set the message 
			// correctly
			if(tcprecs==0) { 
				// User is reading past the end of the available records 
				if(recsdisp>ifsrecs) { 
					msg.setText(recsdisp + "-" + ifsrecs + " " + ResourceBundleLoader_ct.getText("of") + " " + ifsrecs + " " + ResourceBundleLoader_ct.getText("possible"));  
				} else {
					msg.setText(recsdisp-numrecs + "-" + (recsdisp) + " " + ResourceBundleLoader_ct.getText("of") + " " + ifsrecs + " " + ResourceBundleLoader_ct.getText("possible"));  
				}
				if(recsdisp>=ifsrecs) {
					// Display the default cursor
					((Component) l).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					page=0; // User is viewing current page
					return false;
				}
			} else {
				// User is attempting to view past the end of the available 
				// number of records
				if(recsdisp>tcprecs) { 
					msg.setText(recsdisp + "-" + (tcprecs) + " " + ResourceBundleLoader_ct.getText("of") + " " + tcprecs);  
				} else {
					msg.setText(recsdisp-numrecs + "-" + (recsdisp) + " " + ResourceBundleLoader_ct.getText("of") + " " + tcprecs);  
				}
				if(recsdisp>=tcprecs) {
					// Display the default cursor
					((Component) l).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					page=0; // User is viewing current page
					return false;
				}
			}
		    page=0; // User is viewing current page
		} else if(page==2) { // The user wants to view the 2nd to last page
		    formattrace.setText(lastPage);
		    msg.setText(ResourceBundleLoader_ct.getText("PreviousPage"));
		    page=1; // User is viewing 2nd to last page
		}
		
		// Display the default cursor
		((Component) l).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		return true;
    }

    /** Show the previous page. */
    public void showPrev() {
		if(page==0) {
		    currPage = formattrace.getText();
		    formattrace.setText(lastPage);
		    msg.setText(ResourceBundleLoader_ct.getText("PreviousPage"));
		    page=1;
		} else if(page==1) {
		    formattrace.setText(slastPage);
		    msg.setText(ResourceBundleLoader_ct.getText("PreviousPage") + " 2");
		    page=2; 
		} else if(page==2) {
		    CommTrace.error(l,"Not Found","Only the two previous pages are stored");
		}
    }

	/**
	 * Opens a formated file for reading.<br>
	 * Can be either a local file or a remote IFS file.
	 */
	public void open() {
		if(oper==OPEN) {
			// Display file dialog so user can specify a file to open
			FileDialog fd = new FileDialog(CommTrace.getMainFrame(),ResourceBundleLoader_ct.getText("OpenDialog"),FileDialog.LOAD);
			fd.show();
			String path = fd.getDirectory();
			String file = fd.getFile();

			if(path == null || file == null) { // User cancled the FileDialog
				return;
			}

			fmt = new Format();
			fmt.openLclFile(path + file);
			outfile = path + file;
		} else if(oper==OPENRMT) {
			if(path!="" && file !="") {
				fmt = new Format(sys);
				fmt.openIFSFile(path+"/" + file + FormatRemote.EXT);
			} else {
				IFSFileDialog fd = new IFSFileDialog((new JFrame()), ResourceBundleLoader_ct.getText("FiletoView"), sys);

				FileFilter [] filterList = {new FileFilter(ResourceBundleLoader_ct.getText("AllFiles"), "*.*"),      	
											new FileFilter(ResourceBundleLoader_ct.getText("CommTraceFiles"), "*.bin")};
				fd.setFileFilter(filterList, 0);
			
				fd.setDirectory(path);
				fd.setFileName(file);
				String fullpath="";
				if (fd.showDialog() == IFSFileDialog.OK) {
					fullpath = fd.getAbsolutePath();    // get fully qualified file
					fmt = new Format(sys);
					fmt.openIFSFile(fullpath);
				}
			}
		}
	}

    /** 
     * Saves the output from the trace to the user specified location as ASCII 
	 * text.
     */
    public boolean save() {
		// Display file dialog so user can specify a place to save the trace
		FileDialog fd = new FileDialog(l,ResourceBundleLoader_ct.getText("SaveAs"),FileDialog.SAVE);
		if(filename==null) {
			int index;
			String file = outfile;
			if((index = outfile.indexOf('.'))!=-1) {
				file = outfile.substring(0,index);
			}
			fd.setFile(file + ".txt");
		} else {
			fd.setFile(filename + ".txt");
		}
		fd.show();
		String path = fd.getDirectory();
		String file = fd.getFile();
		Writer out;
		
		// Set the cursor to a wait cursor
		((Component)l).setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		try {
			out = new BufferedWriter(new FileWriter(path + file)); // Opens an output stream on the local file
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".save() " + "Error opening " + path + file,e); 
			}
		    return false;
		}
		
		try {
			Format fmtlcl = new Format();
			fmtlcl.openLclFile(outfile);
			String prolog = fmtlcl.getRecFromFile();
			out.write(prolog);
			int numrecs = fmtlcl.getIntFromFile();
			int recPrinted=0;
			String rec;
			while((rec = fmtlcl.getRecFromFile())!=null) {
				if(recPrinted%5==0) {
					out.write(fmt.addBanner());
				}
				out.write(rec);
				recPrinted++;
			}
			fmtlcl.close();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".save() " + "Error writing to " + path + file,e); 
			}
		    return false;
		}
		
		try {
			out.close();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".save() " + "Error closing " + path + file,e); 
			}
		    return false;	
		}
		// Set the cursor back to the default cursor
		((Component)l).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		return true;
    }

    /** Add the end banner. */
    private String addEndBanner() {
		return("\n* * * * * * * * * * * * *    " +
				ResourceBundleLoader_ct.getText("EOCP") +
				"    * * * * * * * * * * * * *\n");
    }

    
    /** 
     * Closes the output window and ends the thread. 
     */
    public void close() {
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + ".close()"); 
		}

		if(l!=null) {
			l.setVisible(false);
		}
		if(fmt!=null) {
			fmt.close();
		}
		fmtThread=null;
    }

    /** 
     * Sets the correct window invisible based on the user input. 
     * Called by the JVM when the user attempts to close our window. 
     */

    public void windowClosing(WindowEvent e) {
		if (e.getSource() == l) {
		    close(); 
		}
    }

    /**
     * Creates a Find object to search our TextArea.
     */
    public void find() {
		if(find==null || find.isClosed()) {
		    find = new Find(formattrace);
		} else {
		    find.toFront();
		}
    }

	/** Copies the selected text. */
    public void copy() {
		formattrace.copy();
    }

	/** Clears the text area. */
    public void clear() {
		formattrace.setText("");
    }

	/** Cuts the selected text. */
    public void cut() {
		formattrace.cut();
    }

	/** Pastes the copied text into the text area. */
    public void paste() {
		formattrace.paste();
    }
    
    /** 
     * Returns the thread this object executes under.
     * @return Thread
     */
    public Thread getThread() {
    	return fmtThread;
    }
    
    /**
     * Sets the thread to execute under.
     * @param Thread
     */
    public void setThread(Thread tr) {
    	fmtThread = tr;
    }
    
    /**
     * Returns the JComboBox with which the user selects the number of records to display.
     * @return JComboBox
     */
    public JComboBox getNumberList() {
    	return numberList_;
    }

	/**
	 * Returns the Save Menu Item.
	 * @return JMenuItem
	 */    
	public JMenuItem getSaveMenuItem() {
		return fsave;
	}
	
	/**
	 * Returns the Close Menu Item.
	 * @return JMenuItem
	 */    
	public JMenuItem getCloseMenuItem() {
		return fclose;
	}
	
	/**
	 * Returns the Find Menu Item.
	 * @return JMenuItem
	 */    
	public JMenuItem getFindMenuItem() {
		return ffind;
	}
	
	/**
	 * Returns the Copy Menu Item.
	 * @return JMenuItem
	 */    
	public JMenuItem getCopyMenuItem() {
		return fcopy;
	}
	
	/**
	 * Returns the Clear Menu Item.
	 * @return JMenuItem
	 */    
	public JMenuItem getClearMenuItem() {
		return fclear;
	}
	
	/**
	 * Returns the Cut Menu Item.
	 * @return JMenuItem
	 */    
	public JMenuItem getCutMenuItem() {
		return fcut;
	}
	
	/**
	 * Returns the Paste Menu Item.
	 * @return JMenuItem
	 */    
	public JMenuItem getPasteMenuItem() {
		return fpaste;
	}
	
	/**
	 * Returns the Next Button.
	 * @return JButton
	 */    
	public JButton getNextButton() {
		return next;
	}
	
	/**
	 * Returns the Previous Button.
	 * @return JButton
	 */    
	public JButton getPrevButton() {
		return prev;
	}
	
	/**
	 * Returns the main frame for the Display of records.
	 * @return JFrame
	 */    
	public JFrame getDisplayFrame() {
		return l;
	}
}
