///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: CommTrace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import java.io.IOException;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.JScrollPane;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.CommandLineArguments;
import com.ibm.as400.access.InternalErrorException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.vaccess.FileFilter;
import com.ibm.as400.vaccess.IFSFileDialog;

/**
 * Provides a front end for the {@link Format Format} class, to display and transfer a communications trace file.<br>
 * The trace must originate from a system (running OS/400 V5R2 or greater) and reside in the IFS directory structure.<br>
 * The trace should be created with the following sequence of commands:
 * <pre>
 * STRCMNTRC
 * ENDCMNTRC
 * DMPCMNTRC
 * </pre>
 *
 * The next step is to either format the trace file, or transfer it to the local PC.
 * The format can be done in two different ways.<br>
 * <ul><li>Using the "Format" button on the main window.</li>
 * <li>Using the Commtrace-&gt;Format menu option</li>
 * </ul>
 * The two options are equivalent.<br>
 * The format that will be performed can be affected by changing the selection 
 * of the "Remote" and "Local" radio button.<br>
 * 
 * <p>
 * If Remote is selected:<br>
 * The file will be formatted remotely on the IBM i system using 
 * JavaCommandCall. The output file will be an IFS file in the same directory as the trace but with a .bin extension 
 * appended. The progress of the format operation is not relayed to the 
 * Commtrace program unless the -verbose option is specified. A dialog will appear when the format has completed.<br>
 * The file will then be displayed on the local PC. Any previous format can be displayed by selecting
 * the "Display" button or using the Commtrace-&gt;Display menu option.<br>
 * The formatted file could also be transfered to the local PC using the "Transfer" 
 * button or Commtrace-&gt;Transfer menu option and then displayed locally using 
 * the "Display" method.<br></p>
 * <p>If Local is selected:<br>
 * The file must be transfered to the local PC using the transfer methods 
 * described above. A progress dialog will appear showing the transfer as it 
 * progresses.<br>
 * The file can then be formatted by selecting the Format button or menu option.<br>
 * A progress dialog will appear when formatting and display the progress 
 * of the format.<br>
 * After the formatting is complete the trace will be displayed as described 
 * in the FormatDisplay class.<br></p>
 *
 * CommTrace can be run as an application as follows:
 * <blockquote><pre>
 * <b>java com.ibm.as400.commtrace.Commtrace</b> [options]</pre></blockquote>
 * Options:
 * <dl>
 * <dt><b>-verbose/-v</b> [true|false]</dt>
 * <dd>
 * Specifies whether to print status and other information to System.out
 * </dd>
 */

public class CommTrace extends WindowAdapter {
    private AS400 sys_; // The system our connections will be made on.
	private FormatDisplay format; // Formats a communications trace
	//Commtrace.MainFrame_ Has to be static so our dialogs have something to be bound to.
	// Otherwise the dialogs will get lost easily.
    private static JFrame MainFrame_;
    
	private static Locale currentLocale_;
	
    private static final String ALL = "*ALL",
    							YES = "*YES",
    							VERSION = "1.0",
								CLASS = "CommTrace",
								NO="*NO";
    
    // Save the path to the file we most recently transfered so we can open the
	// IFSFile dialog in that location.
    private String path;
    
    // The user if the program is verbose or not
    private String verbose_;

	// The arguments passed to this program
	private String[] pargs;

	// Classes that layout the structure of our frames
    private GridBagLayout gridbag;
    private GridBagConstraints c;

	// The basic fields in a dialog
    private JOptionPane optionPane;
    private JDialog dialog;
    private JTextField [] args;
    private JCheckBox fmtbox_;
    private String btnString1;
    private String btnString2;

    // Default Menubar Items
    // File Menu
    private JMenuItem disconnect,
		    exit;
	private JMenuItem display; // Commtrace Menu
	private JMenuItem transfer,
		    mformat,
		    about; // Help Menu

    // About Items
    private JFrame fabout;
    private JButton aokay;

	// Buttons on the main window
    private JButton [] b = new JButton[5];
    static final int TRANSFER = 0;
    static final int FORMAT = 1;
	static final int OPEN = 2;
	static final int FMTRMT = 3;
	static final int OPENRMT = 4;
	
	// Lets the user select if this class is to read and format from a local or remote location
    private JRadioButton remote,
						local;

	// The client used to format a trace remotely
	private FormatRemote fmtcl=null;
	
    /**
     * Base constructor which creates a Commtrace.
     * @param args The command line arguments
     */
    public CommTrace(String[] args) {

		// Parse the arguments passed to the program
		Vector v = new Vector(); 
		v.addElement("-verbose");

		// Create a Hashtable to map shortcuts to the command line arguments. 
		Hashtable shortcuts = new Hashtable();
		shortcuts.put("-v", "-verbose");
		
		// Create a CommandLineArguments object with the args array passed into main(String args[]) 
		// along with the vector and hashtable just created. 
		CommandLineArguments arguments = new CommandLineArguments(args, v, shortcuts);
		
		// Get the verbosity of the program 
		verbose_ = arguments.getOptionValue("-verbose");
		if(verbose_==null) {
			verbose_="false";
		}
		
		// If supposed to be verbose set the trace
		if(verbose_!=null && verbose_.equals("true")) {
			Trace.setTraceErrorOn(true);          // Enable error messages
			Trace.setTraceWarningOn(true);        // Enable warning messages
			Trace.setTraceInformationOn(true);        // Enable warning messages
			Trace.setTraceOn(true); // Turns on traceing
		}
		
		pargs = args; // Save the arguments

		MainFrame_ = new JFrame("CommTrace");
		MainFrame_.getContentPane().setLayout((gridbag = new GridBagLayout()));
		c = new GridBagConstraints();
		MainFrame_.setFont(new Font("Helvetica", Font.PLAIN, 14));

		createMenu(); // Create the Menubar for our program
		createPanels(); // Fill the frame with buttons etc

        MainFrame_.setBackground(Color.black);
		MainFrame_.setForeground(Color.white);

		MainFrame_.addWindowListener(this);
		MainFrame_.setSize(525,300);
		MainFrame_.setVisible(true);
    }

    /**
     * Adds the button to the specified panel.                
     * @param button	number of the button in our button array.
     * @param label		the text shown on the button.
     * @param constraints		The GridBagConstraints of this button.
     * @param panel		The panel to attach the button to.   
     */
    private void addbutton(int button,String l,GridBagConstraints c,JPanel p) {
		b[button] = new JButton(l);
		b[button].setBackground(Color.black);
		b[button].setForeground(Color.white);
		b[button].addActionListener(new CommTraceListener(this));
		b[button].addKeyListener(new CommTraceListener(this));
		gridbag.setConstraints(b[button],c);
		p.add(b[button]);
    }
   
    /**
     * Creates the Panels and objects on the main window.
     */
    private void createPanels() {
		JPanel pnl = new JPanel();
		pnl.setLayout((gridbag = new GridBagLayout()));
        
        // Set the gridbag settings
		c.fill=GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridheight = 7;
		c.insets = new Insets(3, 3, 3, 3);
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.anchor=GridBagConstraints.CENTER;
		gridbag.setConstraints(pnl,c);
	
		// Create and add the RadioButtons
		ButtonGroup bg = new ButtonGroup();
		remote = new JRadioButton(ResourceBundleLoader_ct.getText("Remote"));
		remote.addActionListener(new CommTraceListener(this));
		local = new JRadioButton(ResourceBundleLoader_ct.getText("Local"));
		local.addActionListener(new CommTraceListener(this));
		local.setSelected(true);
		bg.add(local);
		bg.add(remote);
		
		gridbag.setConstraints(local,c);
		pnl.add(local);
		
		c.gridwidth=GridBagConstraints.RELATIVE;
		c.fill=GridBagConstraints.HORIZONTAL;
		addbutton(TRANSFER,ResourceBundleLoader_ct.getText("Transfer"),c,pnl);
		c.gridwidth=GridBagConstraints.REMAINDER;
		JLabel trandesc = new JLabel(ResourceBundleLoader_ct.getText("TransferDescription"));
		gridbag.setConstraints(trandesc,c);
		pnl.add(trandesc);
		
		c.gridwidth=GridBagConstraints.RELATIVE;
		addbutton(OPEN,ResourceBundleLoader_ct.getText("Display"),c,pnl);
		c.gridwidth=GridBagConstraints.REMAINDER;
		JLabel opendesc = new JLabel(ResourceBundleLoader_ct.getText("DisplayButtonDescription"));
		gridbag.setConstraints(opendesc,c);
		pnl.add(opendesc);
		
		c.gridwidth=GridBagConstraints.RELATIVE;
		addbutton(FORMAT,ResourceBundleLoader_ct.getText("Format"),c,pnl);

		c.gridwidth=GridBagConstraints.REMAINDER;
		JLabel formatdesc = new JLabel(ResourceBundleLoader_ct.getText("FormatDescription"));
		gridbag.setConstraints(formatdesc,c);
		pnl.add(formatdesc);

		gridbag.setConstraints(remote,c);		
		pnl.add(remote);
		c.gridwidth=GridBagConstraints.RELATIVE;
		addbutton(OPENRMT,ResourceBundleLoader_ct.getText("Display"),c,pnl);
		c.gridwidth=GridBagConstraints.REMAINDER;
		JLabel dispdsc = new JLabel(ResourceBundleLoader_ct.getText("DisplayRemoteDescription"));
		gridbag.setConstraints(dispdsc,c);
		pnl.add(dispdsc);
		
		c.gridwidth=GridBagConstraints.RELATIVE;
		addbutton(FMTRMT,ResourceBundleLoader_ct.getText("Format"),c,pnl);
		c.gridwidth=GridBagConstraints.REMAINDER;
		JLabel fmtdsc = new JLabel(ResourceBundleLoader_ct.getText("FormatRemoteDescription"));
		gridbag.setConstraints(fmtdsc,c);
		pnl.add(fmtdsc);

		b[FMTRMT].setEnabled(false);
		b[OPENRMT].setEnabled(false);
		MainFrame_.getContentPane().add(pnl);
    }

    /**
     * Creates the menu's and attaches the listeners.  
     */
    private void createMenu() {
	    JMenuBar mb = new JMenuBar();
	    JMenu file = new JMenu(ResourceBundleLoader_ct.getText("File"));
	    JMenu commtrace = new JMenu(ResourceBundleLoader_ct.getText("Commtrace"));
	    JMenu help = new JMenu(ResourceBundleLoader_ct.getText("Help"));


	    file.setMnemonic(KeyEvent.VK_F);
	    
	    // File menu
	    disconnect = new JMenuItem(ResourceBundleLoader_ct.getText("Disconnect"),KeyEvent.VK_D);
	    disconnect.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_D, ActionEvent.CTRL_MASK));
	    exit = new JMenuItem(ResourceBundleLoader_ct.getText("Exit"),KeyEvent.VK_Q);
	    exit.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
	    // Commtrace Menu
	    display = new JMenuItem(ResourceBundleLoader_ct.getText("Display"),KeyEvent.VK_M);
	    display.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	    transfer = new JMenuItem(ResourceBundleLoader_ct.getText("Transfer"),KeyEvent.VK_T);
	    transfer.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_T, ActionEvent.CTRL_MASK));
	    mformat = new JMenuItem(ResourceBundleLoader_ct.getText("Format"),KeyEvent.VK_R);
	    mformat.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_R, ActionEvent.CTRL_MASK));
	    // Help menu
	    about = new JMenuItem(ResourceBundleLoader_ct.getText("AboutCommtrace"));
	  
	    // Add the menu items to the correct menu.
	    file.add(disconnect);
	    disconnect.addActionListener(new CommTraceListener(this));
	    file.add(exit);
	    exit.addActionListener(new CommTraceListener(this));

	    commtrace.add(transfer);
	    transfer.addActionListener(new CommTraceListener(this));
   	    commtrace.add(display);
	    display.addActionListener(new CommTraceListener(this));
	    commtrace.add(mformat);
	    mformat.addActionListener(new CommTraceListener(this));

	    help.add(about);
	    about.addActionListener(new CommTraceListener(this));

	    mb.add(file);
	    mb.add(commtrace);
	    mb.add(help);
	    MainFrame_.setJMenuBar(mb);
    }
    
    /**
     * Indicates if the program is supposed to be verbose or not.
     * @return "true" if program should be verbose.
     */
    String isVerbose() {
    	return verbose_;
    }

    /**
     * Returns the main frame for this program.
     * @return JFrame
     */
    static JFrame getMainFrame() {
    	return MainFrame_;
    }
	
    /**
     * Displays the about box for the program.
     */
    void about() {
		fabout = new JFrame(ResourceBundleLoader_ct.getText("About"));
		JPanel pnl = new JPanel();
		JLabel title = new JLabel(ResourceBundleLoader_ct.getText("Commtrace"));
		title.setFont((new Font("Helvetica", Font.BOLD, 18)));
		JLabel version = new JLabel(ResourceBundleLoader_ct.getText("Version") + " " + VERSION);
		version.setFont((new Font("Helvetica", Font.PLAIN, 12)));
		JLabel copyright = new JLabel(ResourceBundleLoader_ct.getText("Copyright"));
		copyright.setFont((new Font("Helvetica", Font.PLAIN, 12)));
		pnl.setLayout((gridbag = new GridBagLayout()));
		c = new GridBagConstraints();

		aokay = new JButton(ResourceBundleLoader_ct.getText("OK"));

		c.gridwidth=GridBagConstraints.REMAINDER;
		c.gridheight=GridBagConstraints.RELATIVE;
		c.gridheight = 1;
		gridbag.setConstraints(title,c);
		pnl.add(title);

		c.gridwidth=GridBagConstraints.REMAINDER;
		c.gridheight=GridBagConstraints.RELATIVE;
		c.gridheight = 1;
		gridbag.setConstraints(version,c);
		pnl.add(version);

		c.gridwidth=GridBagConstraints.REMAINDER;
		c.gridheight=GridBagConstraints.RELATIVE;
		c.gridheight = 1;
		gridbag.setConstraints(copyright,c);
		pnl.add(copyright);

		c.gridheight = 1;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.gridheight=GridBagConstraints.RELATIVE;
		gridbag.setConstraints(aokay,c);
		pnl.add(aokay);
		aokay.addActionListener(new CommTraceListener(this));

		fabout.getContentPane().add(pnl);
		fabout.addWindowListener(this);
	
		fabout.setSize(300,120);
		fabout.setVisible(true);
		fabout.invalidate();
		fabout.validate();
		fabout.repaint();
    }

	/**
	 * Disconnects from the previously connected IBM i system.
	 */
	void disconnect() {
		sys_.disconnectAllServices(); 
		// Create a new system object so we will be able to connect when needed 
		sys_ = null; 
	}

    /**
	 * Displays an IFSFileDialog to let the user specify a file they want 
	 * retrieved.<br>
	 * If the user pressed OK a FileDialog is presented to allow the user to 
	 * specify a place to save the file.<br>
     * Then transfers the file from the IBM i system.
     */
    void transfer() {
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + ".transfer() " + "Transfering file"); 
		}
		if(sys_==null) {
			sys_ = new AS400();
		}

		IFSFileDialog fd = new IFSFileDialog((new JFrame()), "File Open", sys_);

		FileFilter [] filterList = {new FileFilter(ResourceBundleLoader_ct.getText("AllFiles"), "*.*")};
		fd.setFileFilter(filterList, 0);
		if(path!=null) {
			fd.setDirectory(path);
		}

		String fullpath="";
		if (fd.showDialog() == IFSFileDialog.OK) {
			path=fd.getDirectory();
			fullpath = fd.getAbsolutePath();    // get fully qualified file

			FileDialog localfd = new FileDialog(MainFrame_,ResourceBundleLoader_ct.getText("SaveAs"),FileDialog.SAVE);
			localfd.setFile(fd.getFileName()); 
			localfd.show();
			String path = localfd.getDirectory();
			String file = localfd.getFile();

			if(file!=null) {
				Read r = new Read(this,fullpath,path+file,sys_);
				Thread read = new Thread(r,"Read");
				r.setThread(read);
				read.start();
			}
		}
    }

	/**
	 * Opens up a previously saved trace that was formatted with this program.
	 */
	void open() {
		if(format!=null) {
			format.setThread(null);
			format.close();
		}
		if(local.isSelected()) { // Open a local file
			format = new FormatDisplay("","","","",FormatDisplay.OPEN);
		} else if(remote.isSelected()) { // Open a remote file
			if(sys_==null) {
				sys_ = new AS400();
			}

			if(fmtcl!=null) {
				format = new FormatDisplay(fmtcl.getPath(),fmtcl.getFile(),sys_,FormatDisplay.OPENRMT);
			} else {
				format = new FormatDisplay("","",sys_,FormatDisplay.OPENRMT);
			}
		}
		else { // Neither 'local' nor 'remote' is selected - this should never happen.
            Trace.log(Trace.ERROR,CLASS + ".open() " + "Neither 'local' nor 'remote' is selected.");
		   throw new InternalErrorException(InternalErrorException.UNKNOWN);
		}
		Thread fmtTr = new Thread(format,"Format");
		format.setThread(fmtTr); 
		fmtTr.start();
	}

    /**
     * Displays a Format dialog. Which allows the user to specify filtering 
	 * options.<br>
     * After the format button is clicked the format method is called.
     */
    void formatoptions() {
		args = new JTextField[3];
		args[0] = new JTextField(ALL);
		args[1] = new JTextField(ALL);
		args[2] = new JTextField(ALL);
		fmtbox_ = new JCheckBox(ResourceBundleLoader_ct.getText("FmtBdcst"));
		fmtbox_.setSelected(true);
		
		btnString1 = ResourceBundleLoader_ct.getText("Format");
		btnString2 = ResourceBundleLoader_ct.getText("Cancel");
		Object[] array = {ResourceBundleLoader_ct.getText("Src/DestIPAddr"),args[0],
				ResourceBundleLoader_ct.getText("Src/DestIPAddr"),args[1],
				ResourceBundleLoader_ct.getText("IPPortnum"),args[2],
				fmtbox_};
		String msg = "";
		Object[] options = {btnString1,btnString2};
		optionPane = new JOptionPane(array,
										JOptionPane.QUESTION_MESSAGE,
										JOptionPane.YES_NO_OPTION,
										null,
										options,
										options[0]);
		dialog = new JDialog(MainFrame_,ResourceBundleLoader_ct.getText("FormatOpt"),true);
		dialog.setContentPane(optionPane);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                //
                // Instead of directly closing the window,
                // we're going to change the JOptionPane's
                // value property.
                // 
                    optionPane.setValue(new Integer(
                                        JOptionPane.CLOSED_OPTION));
            }
        });

        for(int i=0;i<args.length;i++) {	
		    args[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				    optionPane.setValue(btnString1);
				}
			});
		}

		optionPane.addPropertyChangeListener(new CommTracePropertyListener(this));

		dialog.setFont(new Font("Helvetica", Font.PLAIN, 14));
		dialog.setResizable(false);

		dialog.addWindowListener(this);
		dialog.setSize(300,300);
		dialog.show();
    }

	/**
	 * Formats the given IFS file remotely on an AS400.
	 * Displays the results locally using the FormatDisplay class. 
	 */
	void formatremote() {
		if(sys_==null) {
			sys_ = new AS400();
		}
		String box;
		if(fmtbox_.isSelected()) {
			box=YES;
		} else {
			box=NO;
		}
		// Start the client to format the file
		fmtcl = new FormatRemote(sys_,verbose_,args[0].getText(),args[1].getText(),args[2].getText(),box,this);
		fmtcl.setThread(new Thread(fmtcl,"FormatClient"));
		fmtcl.getThread().start();
	}

    /**
     * Creates and starts the local format thread.
     */
    void format() {
    	if(format!=null) {
    		format.setThread(null);
			format.close();
		}
		String box;
		if(fmtbox_.isSelected()) {
			box=YES;
		} else {
			box=NO;
		}
		format = new FormatDisplay(args[0].getText(),args[1].getText(),args[2].getText(),box,FormatDisplay.FMT);
		Thread fmtTr = new Thread(format,"Format");
		format.setThread(fmtTr); 
		fmtTr.start();
    }
   
    /**
     * Cleanly close down our program and exit the JVM.
     */
    void exit() {
		if(format!=null) { // Close down our format display object
			format.close();
		}
		System.exit(0);
    }

    /**
     * Invalidates our frame so the JVM is forced to redraw it.
     */
    void framerepaint() {
		MainFrame_.invalidate();
		MainFrame_.validate();
		MainFrame_.repaint();
    }

    /**
     * To show an error dialog with the given text attached
     * to the specified component.
     * @param component component to attach to.
     * @param title	title of our error window.
     * @param msg	message displayed in the error dialog.
     */
    static void error(Component component,String title,String msg) {
		JOptionPane.showMessageDialog(component,
					msg,
                    title,
                    JOptionPane.ERROR_MESSAGE);
    }


    /**
     * To show an error dialog with the given text attached.
     * to the frame of our Commtrace object.
     * @param title	title of our error window.
     * @param msg	message displayed in the error dialog.
     */
    static void error(String title,String msg) {
		JOptionPane.showMessageDialog(CommTrace.MainFrame_,
						msg,
                        title,
                        JOptionPane.ERROR_MESSAGE);
    }

	/**
	 * Invoked when a window is in the process of being closed. 
	 * @param e The event for this window.
	 */
    public void windowClosing(WindowEvent e) {
    	Object source = e.getSource();
		if(source == MainFrame_) {
		    exit();
		} else if(source == fabout) {
			fabout.setVisible(false);
		}
    }
	
	/**
	 * Returns the JOptionPane.
	 * @return JOptionPane
	 */  
    JOptionPane getOptionPane() {
    	return optionPane;
    }
  
  	/**
	 * Returns the dialog for the JOptionPane.
	 * @return JDialog
	 */    
 	JDialog getDialog() {  
    	return dialog;
 	}
 	
 	/**
	 * Returns the arguemnts for the JOptionPane.
	 * @return JTextField[]
	 */  	
 	JTextField[] getArguments() {
 		return args;
 	}
 
 	/**
	 * Returns the string on our JOptionPane button.
	 * @return String
	 */  	
 	String getButtonString1() {
    	return btnString1;
 	}
 	
	/**
	 * Returns the string on our JOptionPane button.
	 * @return String
	 */    
 	String getButtonString2() {
 		return btnString2;
 	}

	/**
	 * Returns the Disconnect Menu Item.
	 * @return JMenuItem
	 */    
	JMenuItem getDisconnectMenuItem() {
		return disconnect;
	}
	
	/**
	 * Returns the Exit Menu Item.
	 * @return JMenuItem
	 */    
	JMenuItem getExitMenuItem() {
		return exit;
	}
		
	/**
	 * Returns the Open Menu Item.
	 * @return JMenuItem
	 */    
	JMenuItem getDisplayMenuItem() {
		return display;
	}
		
	/**
	 * Returns the Transfer Menu Item.
	 * @return JMenuItem
	 */    
	JMenuItem getTransferMenuItem() {
		return transfer;
	}
		
	/**
	 * Returns the Format Menu Item.
	 * @return JMenuItem
	 */    
	JMenuItem getFormatMenuItem() {
		return mformat;
	}
	
	/**
	 * Returns the About Menu Item.
	 * @return JMenuItem
	 */    
	JMenuItem getAboutMenuItem() {
		return about;
	}
		
	/**
	 * Returns the About Frame.
	 * @return JFrame
	 */    
	JFrame getAboutFrame() {
		return fabout;
	}
		
	/**
	 * Returns the About Okay Button.
	 * @return JButton
	 */    
	JButton getAboutOkayButton() {
		return aokay;
	}
		
	/**
	 * Returns the buttons for this frame.
	 * @return JButton[]
	 */    
	JButton[] getButtons() {
		return b;
	}
	
	/**
	 * Returns the remote radio button.
	 * @return JRadioButton
	 */    
	JRadioButton getRemoteButton() {
		return remote;
	} 
	
	/**
	 * Returns the local radio button.
	 * @return JRadioButton
	 */    
	JRadioButton getLocalButton() {
		return local;
	}
	
	/**
	 * Sets the FormatRemote
	 * @param fmt The FormatRemote object
	 */
	void setFormatRemote(FormatRemote fmt) {
		fmtcl = fmt;
	}
	
	/**
	 * Called by the JVM to start the CommTrace Utility.
	 * @param args The arguments from the command line.
	 */
    public static void main(String[] args) {
		new CommTrace(args);
    }
}

