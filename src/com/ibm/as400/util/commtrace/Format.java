///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandLineArguments;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.IFSTextFileOutputStream;
import com.ibm.as400.access.Trace;

/**
 * The Format object is an interface between the raw trace file and the records the trace file contains.<br>
 * A example program:<br>
 * 
 * <pre>
 * Format f = new Format("/path/to/file");
 * f.setFilterProperties(fmtprop); // Sets the filtering properties for this format
 * f.formatProlog(); // Format the prolog
 * Prolog pro = f.getProlog();	
 * System.out.println(pro.toString());
 * if(!pro.invalidData()) { // The is not a valid trace
 *	 Frame rec; 
 *	 while((rec=f.getNextRecord())!=null) { // Get the records
 *	 	System.out.print(rec.getRecNum()); // Print out the Frame Number
 *	 	System.out.println(rec.getTime()); // Print out the time
 *	 	IPPacket p = rec.getPacket(); // Get this records packet
 *	 	Header h = p.getHeader(); // Get the first header
 *	 	if(p.getType()==IPPacket.IP6) { // If IP6 IPPacket
 *	 		if(h.getType()==Header.IP6) { // If IP6 Header
 *				IP6Header ip6 = (IP6Header) h; // Cast to IP6 so we can access methods
 *				System.out.println(h.getName()); // Print the name
 *				System.out.println("IP6 src:"+ip6.getSrcAddr() + " dst:" + ip6.getDstAddr());
 *				System.out.println(ip6.printHexHeader()); // Print the header as hex 
 *				// Print a string representation of the header.
 *				System.out.println(ip6.toString(prop)); 
 *				while((h=h.getNextHeader())!=null) { // Get the rest of the headers
 *					if(h.getType()==Header.TCP) { // If its a TCP header
 *						TCPHeader tcp = (TCPHeader) h; // Cast so we can access methods
 *						System.out.println("TCP src:" + tcp.getSrcPort() + " dst:" + tcp.getDstPort()); 
 *						System.out.println(tcp.toString(prop));
 *					} else if(h.getType()==Header.UDP) { // If its a UDP header
 *						UDPHeader udp = (UDPHeader) h; // Cast so we can access methods
 *						System.out.println("UDP src:" + udp.getSrcPort() + " dst:" + udp.getDstPort()); 
 *						System.out.println(udp.toString(prop));
 *					}
 *				}
 *			}
 *		}
 *	 } 
 * }
</pre>
 * 
 * Format can be run as a program as follows:<br>
 * <blockquote><pre>
 * <b>java com.ibm.as400.commtrace.Format</b> [ options ]<br></pre></blockquote>
 * Options:
 * <dl>
 * The first arguement is the system to connect to
 * <dt><b>-u/-userID</b></dt><dd>The userID for the system</dd>
 * <dt><b>-p/-password</b></dt><dd>The password for the system</dd>
 * <dt><b>-t/-trace</b></dt><dd>The trace to parse</dd>
 * <dt><b>-o/-outfile</b></dt><dd> The file to store the output in</dd>
 * <dt><b>-c/-current</b></dt><dd> Will connect to localhost with the current userID and password</dd>
 * <dt><b>-v/-verbose</b> [true|false]</dt>
 * <dt><b>-logfile</b></dt><dd> The file to store the Trace.log to</dd>
 * <dt><b>-ip/-ipaddress</b></dt><dd> The IP address to filter by</dd>
 * <dt><b>-ip2/-ipaddress2</b></dt><dd> The second IP address to filter by</dd>
 * <dt><b>-port</b></dt><dd> The port number to filter by</dd>
 * <dt><b>-broadcast</b> [*YES|*NO]</dt><dd> Print broadcast frames</dd>
 * <dt><b>-starttime</b></dt><dd> The start of the display range in MMddyyyykkmmssSSS notation(see java.text.SimpleDateFormat)</dd>
 * <dt><b>-endtime</b></dt><dd> The end of the display range in MMddyyyykkmmssSSS notation(see java.text.SimpleDateFormat)</dd>
 * <dt><b>-starttimelong</b></dt><dd> The start of the display range in milliseconds since the epoc</dd>
 * <dt><b>-endtimelong</b></dt><dd> The end of the display range in milliseconds since the epoc</dd>
 * </dl>
 */
public class Format {
	private final String ALL= "*ALL", NO= "*NO", CLASS="Format";
	private AS400 sys= null;
	private boolean createdSys=false; // True if we created the system so we should close it
	private InputStream file= null;
	private Progress progress= null;
	// The progress dialog which display the format progress to the user
	private byte[] data= null; // The raw data of a specific packet
	private Prolog pro_= null;
	// The prolog of this trace. Needed so we can get the title and date of this trace
	private String filename= null, // The path and filename of the file to format
					outfile= null, // The path and filename of the file to put the traced data to
					fmtBroadcast= "Y"; // User wants to see broadcast frames

	private BitBuf nxtRecLen;
	private int ifsrecs= 0, // The number of records in this commtrace
				tcprecs= 0, // The number of tcp records in this commtrace    
				numrecs= 0; // The number of records processed

	private FormatProperties filter_;
	private ObjectInputStream serin;

	/**
	 * Default constructor.
	 */
	public Format() {
	}

	/**
	 * Creates a new Format.<br>
	 * Initializes the MRI.<br>
	 * Takes an iSeries object as an argument which will be used for 
	 * all iSeries operations.<br>
	 * 
	 * @param sys The system that this object should connect to.
	 */
	public Format(AS400 sys) {
		this.sys= sys;
	}

   /** 
	* Constructs a new <code>Format</code> object.<br>
	* Initializes the MRI.<br>
	* Formats the Prolog.<br>
	* Sets up the Filters.<br>
	* @param prop The FormatProperties object to filter by.
	* @param outfile The file to write the formatted trace data to.
	* @param infile The name of the file to read from.
	*/
	public Format(FormatProperties prop, String outfile, String infile) {
		this.filter_= prop;
		this.outfile= outfile;
		this.filename = infile;

		// If the user didn't specify a file name gracefully exit
		if (infile == null) {
			close();
			return;
		}
		// Attempt to open a stream on the file
		try {
			file= new BufferedInputStream(new FileInputStream(infile));
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".Format()" + "Error opening:" + infile, e);
			}
			close(); // Clean up this thread
			return;
		}

		formatProlog();
	}

	/** 
	 * Creates a Format object on the given binary trace.<br>
	 * Initializes the MRI.<br>
	 * Formats the Prolog.<br>
	 * Sets up the Filters.<br>
	 * @param args The command line arguments to be used to format the file.<br>
	 */
	public Format(String[] args) {
		// Create a vector to hold all the possible command line arguments. 
		Vector v= new Vector();
		v.addElement("-userID");
		v.addElement("-password");
		v.addElement("-trace");
		v.addElement("-filename");
		v.addElement("-outfile");
		v.addElement("-current");
		v.addElement("-verbose");
		v.addElement("-logfile");
		v.addElement("-country");
		v.addElement("-language");
		v.addElement("-ipaddress");
		v.addElement("-ipaddress2");
		v.addElement("-port");
		v.addElement("-broadcast");
		v.addElement("-starttime");
		v.addElement("-endtime");
		v.addElement("-starttimelong");
		v.addElement("-endtimelong");

		// Create a Hashtable to map shortcuts to the command line arguments. 
		Hashtable shortcuts= new Hashtable();
		shortcuts.put("-u", "-userID");
		shortcuts.put("-p", "-password");
		shortcuts.put("-t", "-trace");
		shortcuts.put("-o", "-outfile");
		shortcuts.put("-c", "-current");
		shortcuts.put("-v", "-verbose");
		shortcuts.put("-l", "-language");
		shortcuts.put("-co", "-country");
		shortcuts.put("-ip", "-ipaddress");
		shortcuts.put("-ip2", "-ipaddress2");

		// Create a CommandLineArguments object with the args array passed 
		// into main(String args[]) along with the vector and hashtable just 
		// created. 
		CommandLineArguments arguments= new CommandLineArguments(args, v, shortcuts);

		// Get the system that the user wants to run to. 
		String system= arguments.getOptionValue("");

		// Get the user ID that the user wants to log in with. 
		String uid= arguments.getOptionValue("-userID");

		// Get the password that the user wants to log in with. 
		String pwd= arguments.getOptionValue("-password");

		// If specified the user wants to logon to localhost with the current 
		// userID and password
		String cur= arguments.getOptionValue("-current");

		// The name of the file to trace
		String filename= arguments.getOptionValue("-trace");
		this.filename= filename;

		// The name of the output file to save to
		String outfile= arguments.getOptionValue("-outfile");
		this.outfile= outfile;

		// Enable debugging or not
		String verbose= arguments.getOptionValue("-verbose");

		String logfile= arguments.getOptionValue("-logfile");

		// The language code
		String language= arguments.getOptionValue("-language");

		// The country code
		String country= arguments.getOptionValue("-country");

		// If supposed to be verbose set the trace
		if (verbose != null && verbose.equals("true")) {
			Trace.setTraceErrorOn(true); // Enable error messages
			Trace.setTraceWarningOn(true); // Enable warning messages
			Trace.setTraceInformationOn(true); // Enable warning messages
			Trace.setTraceOn(true); // Turns on traceing
		}

		// If log output should be sent to a file	
		if (logfile != null) {
			try {
				Trace.setFileName(logfile);
			} catch (IOException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(
						Trace.ERROR,
						CLASS + ".Format" + "Can't set trace file name, defaulting to outputing trace data to standard out");
				}
			}
		}

		// Set up the filter properties for this trace
		filter_= new FormatProperties();
		
		String filterIP= arguments.getOptionValue("-ipaddress");
		String filterIP2= arguments.getOptionValue("-ipaddress2");
		String filterPort= arguments.getOptionValue("-port");
		String filterBdcst= arguments.getOptionValue("-broadcast");
		String filterSTime= arguments.getOptionValue("-starttime");
		String filterETime= arguments.getOptionValue("-endtime");
		String filterSTimelong= arguments.getOptionValue("-starttimelong");
		String filterETimelong= arguments.getOptionValue("-endtimelong");
		boolean setUpFilters=false;
		
		// Set the filter properties that were specified
		if (filterIP != null && !filterIP.equals(ALL)) {
			filter_.setIPAddress(filterIP);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "IP Filtering enabled");
			}
		}
		if (filterIP2 != null && !filterIP2.equals(ALL)) {
			filter_.setSecondIPAddress(filterIP2);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "IP Filtering enabled");
			}
		}
		if (filterBdcst != null && !filterBdcst.equals(ALL)) {
			filter_.setBroadcast(filterBdcst);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "Broadcast Filtering enabled");
			}
		}
		if (filterPort != null && !filterPort.equals(ALL)) {
			filter_.setPort(filterPort);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "Port Filtering enabled");
			}
		}
		if (filterSTime != null && !filterSTime.equals(ALL)) {
			filter_.setStartTime(filterSTime);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "Beginning Timestamp Filtering enabled");
			}
			setUpFilters=true; // We need to parse the argument into a timestamp
		}
		if (filterETime != null && !filterETime.equals(ALL)) {
			filter_.setEndTime(filterETime);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "End Timestamp Filtering enabled");
			}
		}
		if (filterSTimelong != null && !filterSTimelong.equals(ALL)) {
			if(filterETime!=null) { // The End time with a MMddyyyykkmmssSSS format was 
									// specified. Remove it since a millisecond time was also specified.
				String time=null;
				filter_.setEndTime(null);
			}
			filter_.setStartTime(filterSTimelong);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "Beginning Timestamp Filtering enabled");
			}
		}
		if (filterETimelong != null && !filterETimelong.equals(ALL)) {
			filter_.setEndTime(filterETimelong);
			if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
				Trace.log(Trace.INFORMATION,CLASS + ".Format() " + "End Timestamp Filtering enabled");
			}
		}

		if(filename==null) { // User didn't specify a file
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".Format() " + "Outfile not specified");
			}
			return;
		}
			
		if (cur != null) { // If use CURRENT
			sys= new AS400();
			createdSys=true;
			try {
				// Opens a input stream on the file
				file= new IFSFileInputStream(sys, filename);
			} catch (FileNotFoundException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".Format() " + "File " + filename + " not found", e);
				}
				return;
			} catch (IOException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".Format() " + "Error opening " + filename, e);
				}
				return;
			} catch (AS400SecurityException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".Format() " + "Security exception opening file", e);
				}
				return;
			}
			// User wants to format a file on their local PC
		} else if (system == null || uid == null || pwd == null) {
			try {
				file= new BufferedInputStream(new FileInputStream(filename));
			} catch (FileNotFoundException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".Format() " + "File " + filename + " not found", e);
				}
				return;
			}
			// User wants to format a file on the AS400
		} else {
			sys= new AS400(system, uid, pwd);
			createdSys=true;
			try {
				// Opens a input stream on the file
				file= new IFSFileInputStream(sys, filename);
			} catch (FileNotFoundException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".Format() " + "File " + filename + " not found", e);
				}
				return;
			} catch (IOException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".Format() " + "Error opening" + filename, e);
				}
				return;
			} catch (AS400SecurityException e) {
				if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
					Trace.log(Trace.ERROR,CLASS + ".Format() " + "Security exception opening file", e);
				}
				return;
			}
		}

		if(!formatProlog()) {
			if(setUpFilters) {
				setUpFilters(); // Change the time filters into time in milliseconds
			}
		}
	}

	/** 
	 * Creates a Format object on the given local file.<br>
	 * @param filename The file to format.
	 */
	public Format(String filename) {
		this.filename= filename;
		if(filename==null) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".Format() " + "File not specified");
			}
			return;
		}
		try {
			file= new BufferedInputStream(new FileInputStream(filename));
			// Opens a input stream on the local file
		} catch (FileNotFoundException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".Format() " + "File " + filename + " not found", e);
			}
			return;
		}
	}

	/**
	 * Creates a Format object on the given IFSFileInputStream.<br>
	 * Initializes the MRI.<br>
	 * Formats the Prolog.<br>
	 * Sets up the Filters.<br>
	 * @param file The input stream to read the data off of.
	 */
	public Format(IFSFileInputStream file) {
		this.file= file;
		formatProlog();
	}

	/**
	 * Changes the TIMESTART and TIMEEND of the Properties into longs with a value of time from the epoc.
	 */
	private void setUpFilters() {
		if(filter_==null) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".setUpFilters() " + "Filters are not initialized");
			}
			return;
		}
		try {
			SimpleDateFormat df= new SimpleDateFormat("MMddyyyykkmmssSSS");
			// The format of the date after the record to filter on replaces the start time

			String starttime= filter_.getStartTime();
			String endtime= filter_.getEndTime();

			if (starttime == null && endtime == null) { // If no time filters do nothing
			} else if (endtime == null) { // If only start time initialize start time
				// Set the property to the milliseconds since the epoc
				filter_.setStartTime(Long.toString(df.parse(starttime).getTime()));
			} else { // Initialize both
				// Set the filter properties to the milliseconds since the epoc
				filter_.setStartTime(Long.toString(df.parse(starttime).getTime()));
				filter_.setEndTime(Long.toString(df.parse(endtime).getTime()));
			}
		} catch (ParseException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".setUpFilters() " + "Invalid Time argument", e);
			}
		}
	}

	/**
	 * Sets the Properties for this Format.
	 * @param prop Properties for this Format.
	 */
	public void setFilterProperties(FormatProperties prop) {
		filter_=prop;
	}
	
	/**
	 * Sets the output file to open a OutputStream on.
	 * @param outfile The file to open a OutputStream on.
	 */
	public void setOutFile(String outfile) {
		this.outfile=outfile;
	}
	
	/**
	 * Sets the input stream to read the data from.
	 * @param infile The open InputStream to read from.
	 */
	public void setInFileStream(InputStream infile) {
		file=infile;
	}
	
	/**
	 * Sets the iSeries to use for all iSeries connections.
	 * @param system The system to connect to.
	 */
	public void setSystem(AS400 system) {
		sys=system;
	}

	/**
	 * Formats the trace and sends the output to an IFS text file on the system we are bound to. 
	 * @return A error code if any.
	 */
	public int toIFSTxtFile() {
		IFSTextFileOutputStream out;
		if(sys==null) { // sys isn't specified
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSTxtFile() " +  "Error the system wasn't specified");
			}
			return 1;
		}
		if(outfile==null) { // outfile isn't specified
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSTxtFile() " +  "Error the out file wasn't specified");
			}
			return 1;
		}
			
		try {
			out= new IFSTextFileOutputStream(sys, outfile);
			// Opens an output stream on the local file
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSTxtFile() " +  "Error opening " + outfile, e);
			}
			return 1;
		} catch (AS400SecurityException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSTxtFile() " +  "Security exception opening file", e);
			}
			return 1;
		}
		int recPrinted=0;
		Frame rec;
		try {
			out.write(pro_.toString());
			while ((rec= getNextRecord()) != null) {
				if(recPrinted%5==0) {
					out.write(addBanner());
				}
				out.write(rec.toString());
				recPrinted++;
			}
			out.write(addEndBanner());
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSTxtFile() " +  "Error writing to " + filename, e);
			}
			return 1;
		}
		return 0;
	}

	/**
	 * Formats the trace and sends the output to an IFS text file on the system we are bound to. 
	 * @return A error code if any.
	 */
	public int toLclTxtFile() {
		Writer out;

		if(outfile==null) { // outfile isn't specified
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclTxtFile() " +  "Error the out file wasn't specified");
			}
			return 1;
		}
		
		try {
			out= new BufferedWriter(new FileWriter(outfile));
			// Opens an output stream on the local file
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclTxtFile() " + "Error opening " + outfile, e);
			}
			return 1;
		}
		
		int recPrinted=0;
		Frame rec;
		try {
			out.write(pro_.toString());
			while ((rec= getNextRecord()) != null) {
				if(recPrinted%5==0) {
					out.write(addBanner());
				}
				out.write(rec.toString());
				recPrinted++;
			}
			out.write(addEndBanner());
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclTxtFile() " +  "Error writing to " + filename, e);
			}
			return 1;
		}
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclTxtFile() " + "Error closing " + filename, e);
			}
			return 1;
		}
		return 0;
	}

	/**
	 * Format the trace and write the results to a binary IFS file on the system 
	 * we are connected to.
	 * @return A error code if any.
	 */
	public int toIFSBinFile() {
		ObjectOutputStream out;
		Frame rec;

		if(pro_==null) { // Constructed incorrectly
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSBinFile() " + "Error the prolog wasn't formatted");
			}
			return 1;
		}
		
		if (pro_.invalidData()) { // If the prolog had invalid data return
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSBinFile() " + "Not a valid iSeries CommTrace");
			}
			return 1;
		}

		try {
			out= new ObjectOutputStream(new IFSFileOutputStream(sys, outfile));
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSBinFile() " + "Error opening " + outfile, e);
			}
			return 1;
		} catch (AS400SecurityException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSBinFile() " + "Security exception opening file " + outfile, e);
			}
			return 1;
		}

		try {
			out.writeUTF(pro_.toString());
			out.writeInt(ifsrecs);
			while ((rec= getNextRecord()) != null) {
				String record= rec.toString(filter_);
				if (record != "") {
					out.writeUTF(record);
				}
			}
			out.writeUTF(addEndBanner());
		} catch (NotSerializableException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSBinFile() " + "Error object not serializable " + outfile, e);
			}
			return 1;
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSBinFile() " + "Error writing file " + outfile, e);
			}
			return 1;
		}
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toIFSBinFile() " + "Error closing file " + outfile, e);
			}
			return 1;
		}
		return 0;
	}

	/**
	 * Format the trace and write the results to a binary file on the local PC.
	 * @return A error code if any.
	 */
	public int toLclBinFile() {
		ObjectOutputStream out;
		Frame rec;

		if(pro_==null) { // Constructed incorrectly
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclBinFile() " + "Error the prolog wasn't formatted");
			}
			return 1;
		}
		
		if (pro_.invalidData()) { // If the prolog had invalid data return
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclBinFile() " + "Not a valid iSeries CommTrace");
			}
			return 1;
		}

		String showprogress= filter_.getProgress();
		if (showprogress != null && showprogress.equals(FormatProperties.TRUE)) {
			// Start the progress bar 
			progress= new Progress(("Formating " + filename), ifsrecs, " records");
			Thread progThread= new Thread(progress, "ProgDiag");
			progress.setThread(progThread);
			progThread.start();
		}
		
		// If no output file specified use the same as the current trace but append the extension onto it.
		if(outfile==null) {
			
		}

		try {
			out= new ObjectOutputStream(new FileOutputStream(outfile));
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclBinFile() " + "Error opening " + outfile, e);
			}
			return 1;
		}

		try {
			out.writeUTF(pro_.toString());
			out.writeInt(ifsrecs);
			if (progress == null) {
				while ((rec= getNextRecord()) != null) {
					String record= rec.toString(filter_);
					if (record != "") {
						out.writeUTF(record);
					}
				}
			} else {
				while ((rec= getNextRecord()) != null && !progress.isCanceled()) {
					String record= rec.toString(filter_);
					if (record != "") {
						out.writeUTF(record);
					}
				}
			}

			out.writeUTF(addEndBanner());
		} catch (NotSerializableException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclBinFile() " + "Error object not serializable " + outfile, e);
			}
			return 1;
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclBinFile() " + "Error writing file " + outfile, e);
			}
			return 1;
		}

		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toLclBinFile() " + "Error closing file " + outfile, e);
			}
			return 1;
		}
		return 0;

	}

	/**
	 * Formats the recs and writes them out.
	 * @return An error code if any.
	 */
	private int toBinFile(ObjectOutputStream out) {
		Frame rec;
		try {
			out.writeUTF(pro_.toString());
			out.writeInt(ifsrecs);
			while ((rec= getNextRecord()) != null) {
				out.writeUTF(rec.toString());
			}
			out.writeUTF(addEndBanner());
		} catch (NotSerializableException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".toBinFile() " + "Error object not serializable " + outfile, e);
			}
			return 1;
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR, CLASS + ".toBinFile() " + "Error writing file " + outfile, e);
			}
			return 1;
		}
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR, CLASS + ".toBinFile() " + "Error closing file " + outfile, e);
			}
			return 1;
		}
		return 0;
	}

	/**
	 * Opens an ObjectInputStream and IFSFileInputStream on the file specified earlier. Used for displaying previously formatted traces.
	 * @return An error code if any.
	 */
	public int openIFSFile() {
		return openIFSFile(outfile);
	}

	/**
	 * Opens an ObjectInputStream and IFSFileInputStream on the outfile. Used for displaying previously formatted traces.
	 * @return An error code if any.
	 */
	public int openIFSFile(String outfile) {
		this.outfile= outfile;
		if(outfile==null) { // Outfile wasn't set
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".openIFSFile()" + "Outfile not specified");
			}
			return 1;
		}
		
		try {
			serin= new ObjectInputStream(new IFSFileInputStream(sys, outfile));
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".openIFSFile()" + "Error opening " + outfile,e);
			}
			return 1;
		} catch (AS400SecurityException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".openIFSFile()" + "Security exception opening file " + outfile,e);
			}
			return 1;
		}
		return 0;
	}

	/**
	 * Opens an ObjectInputStream and FileInputStream on the file specified on the command line. Used for displaying previously formatted traces.
	 * @return An error code if any.
	 */
	public int openLclFile() {
		return openLclFile(outfile);
	}

	/**
	 * Opens an ObjectInputStream and FileInputStream on the outfile. Used for displaying previously formatted traces.
	 * @param outfile The file to read in.
	 * @return int An error code if any.
	 */
	public int openLclFile(String outfile) {
		this.outfile= outfile;
		
		if(outfile==null) { // Outfile wasn't set
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".openLclFile() " + "Outfile not specified");
			}
			return 1;
		}
		
		try {
			serin= new ObjectInputStream(new FileInputStream(outfile));
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".openLclFile() " + "Error opening " + outfile, e);
			}
			return 1;
		}
		return 0;
	}

	/**
	 * Closes this format object.
	 * @return An error code if any.
	 */
	public int close() {
		if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
			Trace.log(Trace.INFORMATION,CLASS + ".close()");
		}

		try {
			if (file != null) {
				file.close();
			}
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".close() " + "Error closing " + filename, e);
			}
			return 1;
		}
		try {
			if (serin != null) {
				serin.close();
			}
			if (sys != null && createdSys) {
				sys.disconnectAllServices();
			}
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".close() " + "Error closing " + outfile, e);
			}
			return 1;
		}
		if (progress != null) { // Close the progress Thread
			progress.setThread(null);
		}
		return 0;
	}

	/**
	 * Reads a Frame from the input stream. Records are stored as Strings.
	 * @return String
	 */
	public String getRecFromFile() {
		String utf= null;
		try {
			utf= serin.readUTF();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".getRecFromFile() " + "Error reading file", e);
			}
			return null;
		} catch (NullPointerException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".getRecFromFile() " + "Error file not opened ", e);
			}
			return null;
		}
		return utf;
	}

	/**
	 * Reads in an int from the input stream. 
	 * @return int
	 */
	public int getIntFromFile() {
		int tmp= 0;
		try {
			tmp= serin.readInt();
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".getIntFromFile() " + "Error reading file", e);
			}
			return -1;
		} catch (NullPointerException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".getIntFromFile() " + "Error file not opened", e);
			}
			return -1;
		}
		return tmp;
	}

	/**
	 * Formats the prolog.<br>
	 * Sets the length of the first record.<br>
	 * Sets the total number of records.<br>
	 * @return true if this trace contains invalid data.
	 */
	public boolean formatProlog() {
		if(file==null) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".formatProlog() " + "Input file not opened");
			}
			return false;
		}

		read(0, 258); // Read in the prolog plus the next record length
		BitBuf bb= new BitBuf(data);
		nxtRecLen= new BitBuf(bb, 2048, 16); // Store the length of the next record
		pro_= new Prolog(bb, filter_); // Create and parse the prolog
		ifsrecs= pro_.getNumRecs(); // Store the total number of records
		return pro_.invalidData();
	}

	/**
	 * Retrieves the next record from the given trace.<br>
	 * This method discards all non TCP records.
	 * @return Frame
	 */
	public Frame getNextRecord() {
		if(pro_==null) { // Not initialized correctly
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".getNextRecord() " + "Prolog not formatted.");
			}
			return null;
		}
		
		if (!pro_.invalidData()) {
			Frame rec= getNext();
			while (rec != null) {
				if (!rec.isTCP()) {
					rec= getNext();
				} else {
					tcprecs++;
					return rec;
				}
			}
			return rec;
		} else {
			return null;
		}
	}

	/**
	 * Subroutine which allows us to get the next record 
	 * @return Frame 
	 */
	private Frame getNext() {
		BitBuf bb;
		int recLen= 0;
		if (progress != null) { // Progress is being displayed
			progress.updateProgress(numrecs);
		}
		if (numrecs < ifsrecs) { // While we still have records to process
			numrecs++;
			if (numrecs == ifsrecs) {
				// If this is the last record we don't want to read past the end of the file.
				read(0, nxtRecLen.toInt());
				bb= new BitBuf(data);
				recLen= (nxtRecLen.toInt() - 2) * 8;
			} else {
				read(0, nxtRecLen.toInt());
				// Read in the next records plus the length of the following record
				bb= new BitBuf(data);
				recLen= (nxtRecLen.toInt() * 8) - 16;
				nxtRecLen= new BitBuf(bb, recLen, 16);
			}
			return (new Frame(pro_, bb.slice(0, recLen)));
		} else {
			return null;
		}
	}

	/** 
	 * Reads len bytes of the file starting at off offest.
	 * @param off       the offset to start reading at.
	 * @param len       the number of bytes to read.                     
	 */
	private void read(int off, int len) {
		data= new byte[off + len];
		try {
			file.read(data, off, len); // Reads and stores the input in the data array
		} catch (IOException e) {
			if (Trace.isTraceOn() && Trace.isTraceErrorOn()) {
				Trace.log(Trace.ERROR,CLASS + ".read() " + "Error reading file",e);
			}
		}
	}

	/** 
	 * Return a String containing the banner. 
	 * @return	String containing the banner.
	 */
	public String addBanner() {
	    StringBuffer banner = new StringBuffer();
		String record= ResourceBundleLoader_ct.getText("Record");
		String mac= ResourceBundleLoader_ct.getText("MACAddress");

		banner.append(pro_.getTitle());
		banner.append(pro_.getDate());
		banner.append(record);
		banner.append("       ");
		banner.append(ResourceBundleLoader_ct.getText("Data"));
		banner.append("      ");
		banner.append(record);
		banner.append("                     ");
		banner.append(ResourceBundleLoader_ct.getText("Destination"));
		banner.append("     ");
		banner.append(ResourceBundleLoader_ct.getText("Source"));
		banner.append("           ");
		banner.append(ResourceBundleLoader_ct.getText("Frame"));
		banner.append("\n");
		banner.append(ResourceBundleLoader_ct.getText("Number"));
		banner.append("  ");
		banner.append(ResourceBundleLoader_ct.getText("S/R"));
		banner.append("  ");
		banner.append(ResourceBundleLoader_ct.getText("Length"));
		banner.append("    ");
		banner.append(ResourceBundleLoader_ct.getText("Timer"));
		banner.append("                      ");
		banner.append(mac);
		banner.append("     ");
		banner.append(mac);
		banner.append("      ");
		banner.append(ResourceBundleLoader_ct.getText("Format"));
		banner.append("\n");
		banner.append("------  ---  ------    ------------               --------------  --------------   ------");
		banner.append("\n");
		return banner.toString();
	}

	/** Add the end banner. */
	private String addEndBanner() {
		return (
			"\n* * * * * * * * * * * * *    "
				+ ResourceBundleLoader_ct.getText("EOCP")
				+ "    * * * * * * * * * * * * *\n");
	}

	/**
	 * Returns the number of records that have been formated.
	 */
	public int getRecsProcessed() {
		return numrecs;
	}

	/**
	 * Returns the prolog of this trace. 
	 * @return Prolog
	 */
	public Prolog getProlog() {
		return pro_;
	}

	/**
	 * Returns the number of tcp records in this trace. 
	 * @return	The number of tcp records in this trace.
	 */
	public int getNumberOfTCPRecords() {
		return tcprecs;
	}

	/**
	 * Called by the JVM to Format a file.
	 * @param args The arguments from the command line.
	 */
	public static void main(String[] args) {
		Format f= new Format(args);
		f.toIFSBinFile();
	}
}
