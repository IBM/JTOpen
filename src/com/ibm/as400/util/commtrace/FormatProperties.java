///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: FormatProperties.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Properties;

/**
 * Values to use for setting the Properties object that is sent to the Format class.<br>
 * Example:<br>
 * <pre>
 *   FormatProperties prop = new FormatProperties();
 *	prop.setProgress(FormatProperties.TRUE);
 *	// Set the filter properties that the user specified
 *	if(!filterIPaddr.equals("*ALL")) {
 *		prop.setIPAddress(filterIPaddr);
 *	}</pre>			
 */
public class FormatProperties {
	private Properties prop_;
	public static final String TRUE="True";
	public static final String YES="*YES";
	public static final String NO="*NO";
	public static final String FALSE="False";
	
	static final String FILTER="Filter";
	static final String IPADDRESS="IPAddress";
	static final String IPADDRESS2="IPAddress2";
	static final String PORT="Port";
	static final String BROADCAST="Broadcast";
	/** Should be a string with the time in milliseconds since the epoc. */
	static final String TIMESTART="TimeStart";
	/** Should be a string with the time in milliseconds since the epoc. */	
	static final String TIMEEND="TimeEnd";
	static final String PROGRESS="PROGRESS";
	
	public FormatProperties() {
		prop_ = new Properties();
		prop_.setProperty(FILTER,TRUE);
	}
	
	/**
	 * Sets the IPAddress filter to the given IPAddress.
	 * @param IPAddress The IPAddress to filter on.
	 */
	public void setIPAddress(String IPAddress) {
		prop_.setProperty(IPADDRESS,IPAddress);
	}
	
	/**
	 * Sets the second IPAddress filter to the given IPAddress.
	 * @param IPAddress The IPAddress to filter on.
	 */
	public void setSecondIPAddress(String IPAddress) {
		prop_.setProperty(IPADDRESS2,IPAddress);
	}	
	
	/**
	 * Sets the port filter to the given port.
	 * @param port The port to filter on.
	 */
	public void setPort(String port) {
		prop_.setProperty(PORT,port);
	}
	
	/**
	 * Sets the broadcast filter.
	 * @param broadcast FormatProperties.YES or FormatProperties.NO.
	 */
	public void setBroadcast(String broadcast) {
		prop_.setProperty(BROADCAST,broadcast);
	}
	
	/**
	 * Sets the start time filter in milliseconds since the epoc.
	 * @param starttime The time to start filtering at.
	 */
	public void setStartTime(String starttime) {
		prop_.setProperty(TIMESTART,starttime);
	}	

	/**
	 * Sets the end time filter in milliseconds since the epoc.
	 * @param endtime The time to end filtering.
	 */
	public void setEndTime(String endtime) {
		prop_.setProperty(TIMEEND,endtime);
	}	

	/**
	 * Sets if the progress monitor should be displayed or not.
	 * @param progress FormatProperties.TRUE or FormatProperties.FALSE.
	 */
	public void setProgress(String progress) {
		prop_.setProperty(PROGRESS,progress);
	}
	
	/**
	 * Gets the IPAddress filter
	 * @return The IPAddress to filter on
	 */
	public String getIPAddress() {
		return (String) prop_.getProperty(IPADDRESS);
	}
	
	/**
	 * Gets the second IPAddress filter.
	 * @return The second IPAddress to filter on.
	 */
	public String getSecondIPAddress() {
		return (String) prop_.getProperty(IPADDRESS2);
	}	
	
	/**
	 * Gets the Port filter.
	 * @return The Port to filter on.
	 */
	public String getPort() {
		return (String) prop_.getProperty(PORT);
	}
	
	/**
	 * Gets the Broadcast filter.
	 * @return The Broadcast filter.
	 */
	public String getBroadcast() {
		return (String) prop_.getProperty(BROADCAST);
	}
	
	/**
	 * Gets the start time filter
	 * @return The start time to filter on
	 */
	public String getStartTime() {
		return (String) prop_.getProperty(TIMESTART);
	}	

	/**
	 * Gets the end time filter.
	 * @return The end time to filter on.
	 */
	public String getEndTime() {
		return (String) prop_.getProperty(TIMEEND);
	}	

	/**
	 * Property set if progress should be displayed.
	 * @return The progress property.
	 */
	public String getProgress() {
		return (String) prop_.getProperty(PROGRESS);
	}	
}